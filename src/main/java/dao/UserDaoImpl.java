package dao;

import model.User;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public void createUser(User newUser) {
        final String sql = "insert into users (first_name, last_name , email) values" +
            "(:firstName, :lastName, :email)";
        KeyHolder holder = new GeneratedKeyHolder();

        SqlParameterSource param = new MapSqlParameterSource()
            .addValue("firstName", newUser.getFirstName())
            .addValue("lastName", newUser.getLastName())
            .addValue("email", newUser.getEmail());

        template.update(sql, param, holder);
    }

    @Override
    public List<User> getLikes(int userId) {

        return template.query("select * from users where id in (select user2_id from user_relationships where " +
            "user1_id=" + userId + ")", new UserRowMapper());
    }
}
