package com.makemytrip.modules.auth.controller;

import com.makemytrip.modules.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.makemytrip.modules.auth.dto.LoginRequest;
import com.makemytrip.modules.auth.model.User;
import com.makemytrip.modules.auth.service.AuthService;
import com.makemytrip.modules.auth.repository.UserRepository;
import com.makemytrip.security.AuthContext;
import com.makemytrip.security.JwtService;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/user/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.getEmail(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token, user));
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
    public ResponseEntity<User> editprofile(
            @RequestParam String id,
            @RequestBody User updatedUser,
            Authentication authentication) {
        String currentUserId = AuthContext.userId(authentication);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!id.equals(currentUserId) && !AuthContext.hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User updated = authService.editprofile(id, updatedUser);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getallusers(Authentication authentication) {
        if (!AuthContext.hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
