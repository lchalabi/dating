package model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private List<User> users;
    private List<ValidationFailure> failures;
}
