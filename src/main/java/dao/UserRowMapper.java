package dao;

import model.IceCreamPreference;
import model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int arg1) throws SQLException {
        String iceCreamPreference = rs.getString("ice_cream_preference");
        return User.builder()
            .email(rs.getString("email"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .id(rs.getInt("id"))
            .iceCreamPreference(iceCreamPreference == null || iceCreamPreference.isEmpty() ? null :
                    IceCreamPreference.valueOf(iceCreamPreference))
            .build();
    }
}
