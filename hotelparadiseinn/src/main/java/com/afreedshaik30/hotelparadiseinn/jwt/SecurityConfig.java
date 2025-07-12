package com.afreedshaik30.hotelparadiseinn.jwt;

import com.afreedshaik30.hotelparadiseinn.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // Tells Spring this class defines beans (like filters, providers).
@EnableMethodSecurity // Allows use of annotations like @PreAuthorize("hasRole('ADMIN')") on controller methods.
@EnableWebSecurity // Enables Spring Security for web apps.
public class SecurityConfig {
/*
   This class configures:
        Which routes are public vs. secured
        How users are authenticated (custom user service + password encoder)
        How JWT filters are applied to each request
*/

    private final CustomUserDetailsService customUserDetailsService;
    private final JWTAuthFilter jwtAuthFilter;

    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JWTAuthFilter jwtAuthFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.csrf(AbstractHttpConfigurer::disable) // Disables CSRF protection (safe in stateless JWT-based APIs).
                .cors(Customizer.withDefaults()) // Enables CORS with default configuration. Allows frontend from different origins to call your backend.
                .authorizeHttpRequests((request) -> request.requestMatchers("/auth/**", "/rooms/**","/bookings/**").permitAll().anyRequest().authenticated())
                // Routes under /auth, /rooms, and /bookings are public (no login required).
                // All other routes need authentication (authenticated()).
                .sessionManagement((manager) -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Tells Spring Security not to use sessions (stateless), because you're using JWT for authentication.
                .authenticationProvider(authenticationProvider())
                // Tells Spring Security to use your custom provider (defined below) to authenticate users.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                // Adds your custom JWTAuthFilter before Spring’s default login filter.
                // This ensures JWT token validation is done on every request before anything else.
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }
    /*
       This is the engine that checks:
          Username exists
          Password matches (using the encoder)
       Uses your CustomUserDetailsService and BCryptPasswordEncoder.
    */

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    /*
       Hashes passwords using BCrypt (industry standard).
       Used both:
            When saving passwords
            When verifying passwords during login
    */

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
    /*
       This allows you to inject AuthenticationManager wherever needed (e.g., in your /login controller).
       It delegates authentication to your AuthenticationProvider.
    */
}

/*
   🔐 Login Flow (e.g., /auth/login)
      1. User sends email + password to your login endpoint.
      2. You use AuthenticationManager to authenticate.
      3. If successful, you generate a JWT token using JWTUtils.
      4. You send the token back to the client.

   🔐 Secured Request Flow (e.g., /users/1)
      1. User sends request with Authorization: Bearer <token>.
      2. JWTAuthFilter intercepts the request.
      3. It:
            Extracts username from JWT
            Loads user details
            Validates token
            Sets SecurityContext so Spring knows who the user is

      4. Controller method is executed with user details available.
*/