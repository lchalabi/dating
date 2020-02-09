package model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RelationshipResponse {
    List<ValidationFailure> failures;
    boolean match;
}
