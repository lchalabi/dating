package dao;

import model.RelationshipStatus;
import model.UserRelationship;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRelationshipRowMapper implements RowMapper<UserRelationship> {

    @Override
    public UserRelationship mapRow(ResultSet rs, int arg1) throws SQLException {
        return UserRelationship.builder()
            .user1Id(rs.getInt("user1_id"))
            .user2Id(rs.getInt("user2_id"))
            .status(RelationshipStatus.valueOf(rs.getString("status")))
            .build();
    }
}
