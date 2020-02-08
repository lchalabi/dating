package controller;

import model.User;
import model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getAll();
        return ResponseEntity.of(Optional.ofNullable(users));
    }

    @PostMapping(value = "/edit")
    public ResponseEntity<UserResponse> updateUser(@RequestBody User updateUser) {
        return ResponseEntity.of(Optional.ofNullable(userService.updateUser(updateUser)));
    }

    @PostMapping(value = "/create")
    public ResponseEntity<UserResponse> createUser(@RequestBody User newUser) {
        return ResponseEntity.of(Optional.ofNullable(userService.createUser(newUser)));
    }

    @GetMapping(value = "/likes/{userId}")
    public ResponseEntity<List<User>> getLikes(@PathVariable int userId) {
        List<User> users = userService.getLikes(userId);
        return ResponseEntity.of(Optional.ofNullable(users));
    }
}
