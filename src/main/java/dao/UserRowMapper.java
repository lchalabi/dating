package dao;

import model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int arg1) throws SQLException {
        return User.builder()
            .email(rs.getString("email"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .id(rs.getInt("id"))
            .build();
    }
}
