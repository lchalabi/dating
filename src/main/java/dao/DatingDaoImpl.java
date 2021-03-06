package dao;

import model.RelationshipStatus;
import model.User;
import model.UserRelationship;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static model.RelationshipStatus.LIKED;

@Repository
public class DatingDaoImpl implements DatingDao {

    private NamedParameterJdbcTemplate template;

    public DatingDaoImpl(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<User> getAll() {
        return template.query("select * from users", new UserRowMapper());
    }

    @Override
    public List<User> getViewableUsers(int userId) {
        return template.query("select * from users where id != " + userId + " and id not in (select user2_id from " +
            "user_relationships where user1_id = " + userId + ") and id not in (select user1_id from " +
            "user_relationships where user2_id = " + userId + " and status = 'BLOCKED' or status = 'DISLIKED')",
            new UserRowMapper());
    }

    @Override
    public Optional<User> getByEmail(String email) {
        String sql = "select * from users where email = :email";

        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("email", email);

        try {
            return Optional.ofNullable(template.queryForObject(sql, param, new UserRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> getById(int userId) {
        String sql = "select * from users where id = :userId";

        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("userId", userId);

        try {
            return Optional.ofNullable(template.queryForObject(sql, param, new UserRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> createUser(User newUser) {
        final String sql = "insert into users (first_name, last_name , email, ice_cream_preference) values" +
            "(:firstName, :lastName, :email, :iceCreamPreference)";
        KeyHolder holder = new GeneratedKeyHolder();

        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("firstName", newUser.getFirstName())
            .addValue("lastName", newUser.getLastName())
            .addValue("email", newUser.getEmail())
            .addValue("iceCreamPreference", (newUser.getIceCreamPreference() == null) ? null :
                newUser.getIceCreamPreference().name());

        template.update(sql, param, holder);

        return getByEmail(newUser.getEmail());
    }

    @Override
    public void updateUser(User updateUser) {
        final String sql = "update users set first_name=:firstName, last_name=:lastName, " +
            "email=:email, ice_cream_preference=:iceCreamPreference where id=:userId";
        KeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("userId", updateUser.getId())
            .addValue("firstName", updateUser.getFirstName())
            .addValue("lastName", updateUser.getLastName())
            .addValue("email", updateUser.getEmail())
            .addValue("iceCreamPreference",
                (updateUser.getIceCreamPreference() == null) ? null : updateUser.getIceCreamPreference().name());
        template.update(sql,param, holder);
    }

    @Override
    public void upsertRelationship(UserRelationship userRelationship) {
        final String sql = "insert into user_relationships (user1_id, user2_id, status, updated_at) " +
            "values (:user1Id, :user2Id, :status, :updatedAt) on conflict (user1_id, user2_id) " +
            "DO UPDATE SET status = excluded.status, updated_at = excluded.updated_at";

        KeyHolder holder = new GeneratedKeyHolder();

        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("user1Id", userRelationship.getUser1Id())
            .addValue("user2Id", userRelationship.getUser2Id())
            .addValue("status", userRelationship.getStatus().name())
            .addValue("updatedAt", Instant.now().toEpochMilli());

        template.update(sql, param, holder);
    }

    @Override
    public Optional<UserRelationship> getUserRelationshipByIds(int user1Id, int user2Id) {
        String sql = "select * from user_relationships where user1_id = :user1Id and user2_id = :user2Id";

        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("user1Id", user1Id)
            .addValue("user2Id", user2Id);

        try {
            return Optional.ofNullable(template.queryForObject(sql, param, new UserRelationshipRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getRelationshipsByStatus(int userId, RelationshipStatus status) {
        return template.query("select * from users where id in (select user2_id from user_relationships where " +
            "user1_id=" + userId + " and status = '" + status.name() + "')", new UserRowMapper());
    }

    @Override
    public List<User> getUsersWhoLiked(int userId) {
        return template.query("select * from users where id in (select user1_id from user_relationships where " +
            "user2_id=" + userId + " and status = '" + LIKED.name() + "')", new UserRowMapper());
    }

    @Override
    public void deleteRelationship(UserRelationship userRelationship) {
        final String sql = "delete from user_relationships where user1_Id=:user1Id and user2_Id=:user2Id";
        Map<String,Object> map= new HashMap<>();
        map.put("user1Id", userRelationship.getUser1Id());
        map.put("user2Id", userRelationship.getUser2Id());
        template.execute(sql, map, (PreparedStatementCallback<Object>) PreparedStatement::executeUpdate);
    }
}
