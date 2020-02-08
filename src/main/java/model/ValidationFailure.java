package model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ValidationFailure {
    private String errorMessage;
}
