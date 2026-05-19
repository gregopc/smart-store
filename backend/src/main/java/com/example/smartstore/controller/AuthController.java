package com.example.smartstore.controller;

import com.example.smartstore.domain.User;
import com.example.smartstore.dto.AuthenticationDTO;
import com.example.smartstore.dto.LoginResponseDTO;
import com.example.smartstore.dto.RegisterDTO;
import com.example.smartstore.repository.UserRepository;
import com.example.smartstore.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.example.smartstore.domain.User;
import com.example.smartstore.dto.AuthenticationDTO;
import com.example.smartstore.dto.LoginResponseDTO;
import com.example.smartstore.dto.RegisterDTO;
import com.example.smartstore.event.UserActionEventPublisher;
import com.example.smartstore.event.UserActionType;
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
@Tag(name = "Auth", description = "Operações de autenticação")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserActionEventPublisher userActionEventPublisher;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        User authenticatedUser = (User) auth.getPrincipal();
        var token = tokenService.generateToken(authenticatedUser);

        userActionEventPublisher.publish(userActionEventPublisher
                .newEvent(UserActionType.USER_LOGGED_IN, authenticatedUser, "POST /auth/login")
                .build());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @Operation(summary = "Registra um novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "400", description = "E-mail já está em uso")
    })
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
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
