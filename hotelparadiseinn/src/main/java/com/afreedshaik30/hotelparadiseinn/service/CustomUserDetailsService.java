package com.afreedshaik30.hotelparadiseinn.service;

import com.afreedshaik30.hotelparadiseinn.exception.OurException;
import com.afreedshaik30.hotelparadiseinn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username).orElseThrow(() -> new OurException("Email Not Found"));
    }
    /*
        username here actually refers to the email.
        findUserByEmail() is a custom method in UserRepository that returns Optional<User>.

        If the user is found, it's returned.
        If not, it throws your custom exception: OurException("Email Not Found").
    */
}

/* After
        1. User entity implements UserDetails.
        2.Your UserRepository has the method findUserByEmail(String email).

  Purpose of CustomUserDetailsService
      Implements UserDetailsService, a Spring Security interface.
     *Is used by Spring Security to load user data from the database when authenticating a user.
*/
