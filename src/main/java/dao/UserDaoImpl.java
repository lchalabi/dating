package dao;

import model.User;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    public List<User> getLikes(int userId) {
        return template.query("select * from users where id in (select user2_id from user_relationships where " +
            "user1_id=" + userId + ")", new UserRowMapper());
    }
}
