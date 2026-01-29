package com.nam.assetmanager.service;

import com.nam.assetmanager.model.User;
// Fixed: Changed 'repository' to 'repositories' to match your folder name
import com.nam.assetmanager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void saveUser(User user) {
        // 1. Encrypt the password using BCrypt for security
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Set default role for the Asset Management system
        user.setRole("ROLE_ADMIN");

        // 3. Persist the user to the MySQL database
        userRepository.save(user);
    }
}