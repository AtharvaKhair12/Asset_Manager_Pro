package com.nam.assetmanager.service;

import com.nam.assetmanager.model.User;
import com.nam.assetmanager.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void saveUser(User user, String siteURL) throws MessagingException, UnsupportedEncodingException {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_ADMIN");
        String randomCode = String.format("%06d", new Random().nextInt(1000000));
        user.setVerificationCode(randomCode);
        user.setEnabled(false);
        userRepository.save(user);
        emailService.sendVerificationEmail(user, siteURL);
    }

    @Transactional
    public void saveEmployee(User user, String siteURL) throws MessagingException, UnsupportedEncodingException {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_EMPLOYEE");
        String randomCode = String.format("%06d", new Random().nextInt(1000000));
        user.setVerificationCode(randomCode);
        user.setEnabled(false);
        userRepository.save(user);
        emailService.sendVerificationEmail(user, siteURL);
    }

    public boolean verify(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);
        if (user == null || user.isEnabled()) {
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEnabled(true);
            userRepository.save(user);
            return true;
        }
    }
}