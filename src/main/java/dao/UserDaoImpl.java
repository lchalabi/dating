package dao;

import model.RelationshipStatus;
import model.User;
import model.UserRelationship;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static model.RelationshipStatus.BLOCKED;
import static model.RelationshipStatus.LIKED;

@Repository
public class UserDaoImpl implements UserDao {

    private NamedParameterJdbcTemplate template;

    public UserDaoImpl(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<User> getAll() {
        return template.query("select * from users", new UserRowMapper());
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
        final String sql = "insert into users (first_name, last_name , email) values" +
            "(:firstName, :lastName, :email)";
        KeyHolder holder = new GeneratedKeyHolder();

        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("firstName", newUser.getFirstName())
            .addValue("lastName", newUser.getLastName())
            .addValue("email", newUser.getEmail());

        template.update(sql, param, holder);

        return getByEmail(newUser.getEmail());
    }

    @Override
    public void updateUser(User updateUser) {
        final String sql = "update users set first_name=:firstName, last_name=:lastName, " +
            "email=:email where id=:userId";
        KeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("userId", updateUser.getId())
            .addValue("firstName", updateUser.getFirstName())
            .addValue("lastName", updateUser.getLastName())
            .addValue("email", updateUser.getEmail());
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
    public List<User> getBlockedUsers(int userId) {
        return template.query("select * from users where id in (select user2_id from user_relationships where " +
            "user1_id=" + userId + " and status = '" + BLOCKED.name() + "')", new UserRowMapper());
    }

    @Override
    public List<User> getUsersWhoLiked(int userId) {
        return template.query("select * from users where id in (select user1_id from user_relationships where " +
            "user2_id=" + userId + " and status = '" + LIKED.name() + "')", new UserRowMapper());
    }
}
