package com.example.smartstore.controller;

import com.example.smartstore.domain.User;
import com.example.smartstore.dto.AuthenticationDTO;
import com.example.smartstore.dto.LoginResponseDTO;
import com.example.smartstore.dto.RegisterDTO;
import com.example.smartstore.repository.UserRepository;
import com.example.smartstore.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterDTO data) {
        if (this.userRepository.findByEmail(data.email()) != null) {
            return ResponseEntity.badRequest().body("E-mail já está em uso.");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser = new User();
        newUser.setEmail(data.email());
        newUser.setPassword(encryptedPassword);
        
        this.userRepository.save(newUser);
        return ResponseEntity.ok().build();
    }
}