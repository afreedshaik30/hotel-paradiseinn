package com.afreedshaik30.hotelparadiseinn.jwt;

import com.afreedshaik30.hotelparadiseinn.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
/*
   OncePerRequestFilter: Ensures the filter is executed once per request.
   This class intercepts every request to:
        1. Check if there is a JWT in the Authorization header.
        2. Validate it.
        3. If valid, set the user as authenticated in the Spring Security context.
 */

    private JWTUtils jwtUtils;
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JWTAuthFilter(JWTUtils jwtUtils, CustomUserDetailsService customUserDetailsService){
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
    }
    /*
       jwtUtils: Used to extract the username from the JWT and validate the token.
       customUserDetailsService: Used to load user details from the database.
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        if(authHeader == null || authHeader.isBlank()){
            filterChain.doFilter(request,response);
            return;
        }

        jwtToken = authHeader.substring(7);
        userEmail = jwtUtils.extractUsername(jwtToken);

        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            if(jwtUtils.isValidToken(jwtToken, userDetails)){
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
            }
        }
        filterChain.doFilter(request, response);
    }
}
/*
 [Incoming HTTP Request]
        ↓
 [JWTAuthFilter]
    └── Check Authorization header
    └── Extract token
    └── Validate token
    └── Load user from DB
    └── Set SecurityContextHolder
        ↓
 [Next Filter / Controller]


  Steps:-

1. Reads the Authorization header
2.	Extracts JWT token and parses username
3.	Validates the token using JWTUtils
4.	Loads user from DB
5.	Sets the authentication into SecurityContextHolder
6.	Continues processing the request

1. Get JWT Token from Request Header
         final String authHeader = request.getHeader("Authorization");
   Looks for the Authorization header, which should contain:
         Authorization: Bearer <token>

2. Check if Header is Missing or Blank
        if(authHeader == null || authHeader.isBlank()){
            filterChain.doFilter(request,response);
            return;
        }
   If there's no token, skip this filter and continue processing the request.

3. Extract JWT and Username
        jwtToken = authHeader.substring(7);
        userEmail = jwtUtils.extractUsername(jwtToken);
   substring(7) removes "Bearer " to isolate the token.
   extractUsername() pulls the username/email from the token.

4. Check If User Is Already Authenticated
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){

   Only proceed if:
     A username was extracted from the token.
     No authentication has been set yet (to avoid overwriting an existing authenticated user).

5. Load UserDetails and Validate Token
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
        if(jwtUtils.isValidToken(jwtToken, userDetails)){
   Load user from DB using the email from the token.
   Validate that:
        The token is correctly signed.
        The token is not expired.
        The username from the token matches the user details.
6. Set Authentication in SecurityContext
       SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
       UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
       token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
       securityContext.setAuthentication(token);
       SecurityContextHolder.setContext(securityContext);

   Creates a new empty SecurityContext.
   Creates a Spring Security Authentication object (UsernamePasswordAuthenticationToken) with:
       the user's details
       null credentials (we don’t need password here)
       the user's authorities (roles)
   Adds request-specific details (like IP address).
   Puts this into the global SecurityContextHolder so Spring Security knows the user is logged in.

7.  Continue the Filter Chain
        filterChain.doFilter(request, response);
    Passes the request to the next filter (or controller) now that the user is authenticated.
*/