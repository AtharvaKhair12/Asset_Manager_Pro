package com.nam.assetmanager.repositories;

import com.nam.assetmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByVerificationCode(String verificationCode);

    List<User> findByRole(String role);
}