package model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class User {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
}
