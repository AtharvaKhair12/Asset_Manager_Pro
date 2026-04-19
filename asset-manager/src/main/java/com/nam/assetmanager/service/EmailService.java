package com.nam.assetmanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    public void sendVerificationEmail(String toEmail, String fullName, String verificationCode, String siteURL)
            throws Exception {
        String verifyURL = siteURL + "/verify?code=" + verificationCode;

        String body = """
                {
                    "from": "AssetManager Pro <onboarding@resend.dev>",
                    "to": ["%s"],
                    "subject": "Verify your AssetManager Account",
                    "html": "<h2>Hello %s</h2><p>Click below to verify your account:</p><a href='%s'>Verify Account</a>"
                }
                """.formatted(toEmail, fullName, verifyURL);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}