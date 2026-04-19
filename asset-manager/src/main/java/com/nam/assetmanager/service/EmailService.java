package com.nam.assetmanager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import com.nam.assetmanager.model.User;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(User user, String siteURL)
            throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "titanff78@gmail.com";
        String senderName = "AssetManager Pro";
        String subject = "Verify your account";
        String content = "Dear [[name]],<br><br>"
                + "Your verification code is: <b>[[code]]</b><br><br>"
                + "Thank you,<br>AssetManager Pro";

        content = content.replace("[[name]]", user.getFullName());
        content = content.replace("[[code]]", user.getVerificationCode());

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
}