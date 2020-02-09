package model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RestResponse {
    private List<User> users;
    private List<ValidationFailure> failures;
}
