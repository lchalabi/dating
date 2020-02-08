package model;

import lombok.Builder;

import java.util.List;

@Builder
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String tagLine;
    private List<GenderType> lookingFor;
}
