package com.example.smartstore.controller;

import com.example.smartstore.domain.User;
import com.example.smartstore.repository.UserRepository;
import com.example.smartstore.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.get("email"), data.get("password"));
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody Map<String, String> data) {
        if (this.repository.findByEmail(data.get("email")) != null) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = passwordEncoder.encode(data.get("password"));
        User newUser = User.builder()
                .email(data.get("email"))
                .password(encryptedPassword)
                .build();

        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}