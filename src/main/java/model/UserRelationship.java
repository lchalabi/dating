package model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRelationship {
    private int user1Id;
    private int user2Id;
    private RelationshipStatus status;
}
