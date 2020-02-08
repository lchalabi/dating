package model;

public enum ValidationRule {

    EMAIL_ALREADY_EXISTS("Email address must be unique."),
    EMAIL_MUST_BE_SPECIFIED("Email address must be specified."),
    ID_MUST_BE_NULL("Id must be null."),
    ID_MUST_BELONG_TO_A_USER("Id must belong to a user."),
    ID_MUST_NOT_BE_NULL("Id must not be null."),
    FIRST_NAME_MUST_BE_SPECIFIED("First name must be specified."),
    LAST_NAME_MUST_BE_SPECIFIED("Last name must be specified.");

    String desc;

    ValidationRule(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
