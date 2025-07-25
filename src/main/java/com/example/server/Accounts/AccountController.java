package com.example.server.Accounts;

import java.util.*;

import com.example.server.Utility.JwtUtil;
import com.example.server.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AccountController {

    @Autowired
    private AccountRepo repo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AccountService accountService;

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody Account details) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(details.getEmail(), details.getPassword()));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        UserDetails userDetails = accountService.loadUserByUsername(details.getEmail());
        AuthResponse response = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Account details) {
        boolean success = accountService.registerUser(details);
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Account created successfully"));
    }


    @GetMapping("/details")

    public ResponseEntity<?> getUserFromToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid token header");
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        String email = jwtUtil.extractUserName(token);

        return repo.findByEmail(email)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(null);
    }

}
