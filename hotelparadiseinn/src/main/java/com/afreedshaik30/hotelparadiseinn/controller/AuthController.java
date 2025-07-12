package com.afreedshaik30.hotelparadiseinn.controller;

import com.afreedshaik30.hotelparadiseinn.dto.LoginRequest;
import com.afreedshaik30.hotelparadiseinn.dto.Response;
import com.afreedshaik30.hotelparadiseinn.entity.User;
import com.afreedshaik30.hotelparadiseinn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // Inject the admin secret key from application.properties
    @Value("${admin.secret.key}")
    private String adminSecretKey;

    // 1.a) Normal User Registration
    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody User user) {
        Response response = userService.register(user);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // 1.b) Admin Registration with Secret Key
    @PostMapping("/admin/register")
    public ResponseEntity<Response> registerAdmin(
            @RequestBody User user,
            @RequestParam String secretKey) {

        Response response = new Response();

        // Step 1: Validate the secret key
        if (!adminSecretKey.equals(secretKey)) {
            response.setStatusCode(403);
            response.setMessage("Invalid Admin Secret Key");
            return ResponseEntity.status(403).body(response);
        }

        // Step 2: Set role forcibly to ADMIN (even if frontend tries to override)
        user.setRole("ADMIN");

        // Step 3: Register the admin using normal user service
        response = userService.register(user);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // 2. Login Endpoint (for both user/admin)
    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody LoginRequest loginRequest) {
        Response response = userService.login(loginRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
