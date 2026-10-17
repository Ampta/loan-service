package com.ampta.repository;

import com.ampta.entity.enums.Role;
import com.ampta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findByVerificationToken(String verificationToken);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByCustomer_CustomerId(Long customerId);

    Optional<User> findByRole(Role role);
}

