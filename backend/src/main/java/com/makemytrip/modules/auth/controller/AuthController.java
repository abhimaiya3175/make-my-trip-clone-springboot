package com.makemytrip.modules.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.makemytrip.modules.auth.model.User;
import com.makemytrip.modules.auth.service.AuthService;
import com.makemytrip.modules.auth.repository.UserRepository;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user/login")
    public User login(@RequestParam String email, @RequestParam String password) {
        return authService.login(email, password);
    }

    @PostMapping("/user/signup")
    public ResponseEntity<User> signup(@RequestBody User user) {
        return ResponseEntity.ok(authService.signup(user));
    }

    @GetMapping("/user/email")
    public ResponseEntity<User> getuserbyemail(@RequestParam String email) {
        User user = authService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/user/edit")
    public User editprofile(@RequestParam String id, @RequestBody User updatedUser) {
        return authService.editprofile(id, updatedUser);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getallusers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
