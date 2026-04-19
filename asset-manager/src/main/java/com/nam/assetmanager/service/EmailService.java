package com.nam.assetmanager.service;

import com.nam.assetmanager.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendVerificationEmail(User user, String siteURL) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String senderName = "Asset Manager Pro";
        String subject = "Your Verification Code - Asset Manager Pro";
        String content = "Dear [[name]],<br><br>"
                + "Thank you for registering. Please use the following 6-digit code to verify your account:<br><br>"
                + "<h2 style=\"color: #004085; font-size: 32px; letter-spacing: 5px; text-align: center;\">[[CODE]]</h2><br>"
                + "If you did not request this, please ignore this email.<br><br>"
                + "Thank you,<br>"
                + "Asset Manager Pro Team";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getFullName());
        content = content.replace("[[CODE]]", user.getVerificationCode());

        helper.setText(content, true);

        mailSender.send(message);
    }
}
