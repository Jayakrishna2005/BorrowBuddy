package com.borrowbuddy.backend.controller;

import com.borrowbuddy.backend.model.User;
import com.borrowbuddy.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Find user by registration number
        Optional<User> userOpt = userRepository.findByRegistrationNumber(loginRequest.getRegNumber());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // In a real app we'd verify password, but given frontend only passes email and reg, let's just make sure email matches
            if (user.getEmail().equalsIgnoreCase(loginRequest.getEmail())) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(401).body("Email does not match registration number.");
            }
        } else {
            // Auto register the user for this prototype since registration screen might not exist yet
            User newUser = new User();
            newUser.setRegistrationNumber(loginRequest.getRegNumber());
            newUser.setEmail(loginRequest.getEmail());
            newUser.setFullName(loginRequest.getName());
            userRepository.save(newUser);
            return ResponseEntity.ok(newUser);
        }
    }
}

class LoginRequest {
    private String name;
    private String regNumber;
    private String email;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
