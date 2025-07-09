package com.afreedshaik30.hotelparadiseinn.repository;

import com.afreedshaik30.hotelparadiseinn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
//  1. user existsByEmail
    boolean existsByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
