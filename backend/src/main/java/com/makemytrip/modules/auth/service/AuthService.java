package com.makemytrip.modules.auth.service;

import com.makemytrip.modules.auth.exception.EmailAlreadyRegisteredException;
import com.makemytrip.modules.auth.model.User;
import com.makemytrip.modules.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }
        User user = userRepository.findByEmail(email);
        if (user == null || user.getPassword() == null) {
            return null;
        }

        try {
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        } catch (IllegalArgumentException ignored) {
            // Handle legacy records that may still store plaintext passwords.
        }

        if (password.equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return user;
        }
        return null;
    }

    public User signup(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new EmailAlreadyRegisteredException();
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public User editprofile(String id, User updatedUser) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            return userRepository.save(user);
        }
        return null;
    }
}
