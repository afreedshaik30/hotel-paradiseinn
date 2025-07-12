package com.afreedshaik30.hotelparadiseinn.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTUtils {

    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds
    private final SecretKey secretKey; //obj

    public JWTUtils() {
        String secret = "aG90ZWxwYXJhZGlzZWlubjIwOFQxQTA1NjA5MDE0NzgyOTg2"; //hotelparadiseinn208T1A05609014782986 to base64 encoded
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }
    /*
       1.secret is a Base64-encoded string (in this case, it represents "hotelparadiseinn").
         It's decoded into bytes.
         Then, those bytes are used to generate a SecretKey object using the algorithm HmacSHA256.
         This key will later be used to sign JWT tokens and verify their authenticity.
    */

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(
                Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
        );
    }
    /*
       4.Parses the JWT token using the secret key (verifies signature).
       Extracts the payload (claims) from the token.
       Applies a function to get a specific claim (subject, expiration, etc).
     */


    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }
    // 3.Calls a generic method extractClaims(...) and retrieves the subject claim (which is the username).


    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }
    /*
       2.A JWT token is built using the builder pattern.
           .subject(userDetails.getUsername()): sets the username as the token’s subject.
           .issuedAt(...): sets when the token was created.
           .expiration(...): sets token expiry time (here: 7 days).
           .signWith(secretKey): signs the token with your secret key (so others can't forge it).
           .compact(): converts the JWT into a compact string (a long encrypted token).
       This token can be sent to the frontend and stored in localStorage or cookies.
    */

    private boolean isTokenExpired(String token){
        return extractClaims(token,Claims::getExpiration).before(new Date());
    }
    /*
       5.Gets the token’s expiration date.
        Compares it with the current time.
        Returns true if the token is expired.
     */

    public boolean isValidToken(String token, UserDetails userDetails){
       final String username = extractUsername(token);
       return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    /*
      6.Extracts the username from the token.
        Compares it with the currently logged-in user's username.
        Also checks if the token is not expired.
        Returns true if everything is valid.
      This method is useful in filter chains to check if a token is still valid before granting access to a protected route.
    */
}

/*
   WORKFLOW
   1. User logs in successfully → backend calls generateToken(userDetails) → sends token to frontend.
   2. Frontend stores token and sends it in headers with future requests.
   3. Backend receives the token, and calls isValidToken(token, userDetails) to check if it's:
        Signed correctly (not forged)
        Not expired
        Belongs to the right user
*/