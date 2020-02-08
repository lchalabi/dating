package model;

import lombok.Builder;

@Builder
public class User {
    private Integer id;

    private String firstName;
    private String lastName;
    private String email;
}
