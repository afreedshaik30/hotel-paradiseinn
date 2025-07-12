package com.afreedshaik30.hotelparadiseinn.service;

import com.afreedshaik30.hotelparadiseinn.dto.LoginRequest;
import com.afreedshaik30.hotelparadiseinn.dto.Response;
import com.afreedshaik30.hotelparadiseinn.dto.UserDTO;
import com.afreedshaik30.hotelparadiseinn.entity.User;
import com.afreedshaik30.hotelparadiseinn.exception.OurException;
import com.afreedshaik30.hotelparadiseinn.jwt.JWTUtils;
import com.afreedshaik30.hotelparadiseinn.repository.UserRepository;
import com.afreedshaik30.hotelparadiseinn.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authManager;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtils jwtUtils, AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authManager = authManager;
    }

    @Override
    public Response register(User user) {
        Response response = new Response();
        try {
            // Check for duplicate email first
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new OurException(user.getEmail() + " already exists");
            }

            // Assign USER role only if not set (allows ADMIN set from controller)
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("USER");
            }

            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Save and return
            User savedUser = userRepository.save(user);
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(savedUser);

            response.setStatusCode(200);
            response.setUser(userDTO);
            response.setMessage("Registration successful");

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during registration: " + e.getMessage());
        }
        return response;
    }


    @Override
    public Response login(LoginRequest loginRequest) {
        Response response = new Response();
        try {
            // 1. Authenticate user credentials
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()
                    )
            );

            // 2. Fetch user from DB
            User user = userRepository.findUserByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new OurException("User not found"));

            // 3. Generate JWT token
            String token = jwtUtils.generateToken(user);

            // 4. Set response details
            response.setStatusCode(200);
            response.setToken(token);
            response.setRole(user.getRole());
            response.setExpirationTime("7 Days"); // optional metadata
            response.setMessage("Token generated successfully");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(401); // Unauthorized
            response.setMessage("Invalid email or password");
        }
        return response;
    }

    @Override
    public Response getAllUsers() {
        Response response = new Response();
        try {
            List<User> userList = userRepository.findAll();
            List<UserDTO> userDTOList = Utils.mapUserListEntityToUserListDTO(userList);

            response.setStatusCode(200);
            response.setMessage("Users fetched successfully");
            response.setUserList(userDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving users: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getUserBookingHistory(String userId) {
        Response response = new Response();
        try {
            Long id = Long.parseLong(userId); // Robust conversion
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new OurException("User not found"));

            UserDTO userDTO = Utils.mapUserEntityToUserDTOPlusUserBookingsAndRoom(user);

            response.setStatusCode(200);
            response.setMessage("Booking history fetched successfully");
            response.setUser(userDTO);
        } catch (NumberFormatException e) {
            response.setStatusCode(400);
            response.setMessage("Invalid user ID format");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching user booking history: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getUserById(String userId) {
        Response response = new Response();
        try {
            Long id = Long.parseLong(userId);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new OurException("User not found"));

            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);
            response.setStatusCode(200);
            response.setMessage("User fetched successfully");
            response.setUser(userDTO);

        } catch (NumberFormatException e) {
            response.setStatusCode(400);
            response.setMessage("Invalid user ID format");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching user by ID: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getMyInfo(String email) {
        Response response = new Response();
        try {
            User user = userRepository.findUserByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);
            response.setStatusCode(200);
            response.setMessage("User info retrieved successfully");
            response.setUser(userDTO);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching user info: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response deleteUser(String userId) {
        Response response = new Response();
        try {
            Long id = Long.parseLong(userId);
            userRepository.findById(id)
                    .orElseThrow(() -> new OurException("User not found"));

            userRepository.deleteById(id);

            response.setStatusCode(200);
            response.setMessage("User deleted successfully");
        } catch (NumberFormatException e) {
            response.setStatusCode(400);
            response.setMessage("Invalid user ID format");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting user: " + e.getMessage());
        }
        return response;
    }
}
