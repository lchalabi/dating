package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Builder
@Getter
@Setter
public class User {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private IceCreamPreference iceCreamPreference;
    @JsonIgnore
    private int similarity;

    public int getSimilarity(User otherUser) {
        IceCreamPreference otherIceCreamPreference = otherUser.getIceCreamPreference();
        similarity = 1;
        if (otherIceCreamPreference != null) {
            if (iceCreamPreference == otherIceCreamPreference) {
                similarity = 0;
            }
        }
        return similarity;
    }

    public int likedBy(Set<Integer> likedBy) {
        return likedBy.contains(id) ? 0 : 1;
    }
}
