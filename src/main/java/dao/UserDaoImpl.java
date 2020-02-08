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
}
