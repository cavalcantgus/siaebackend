package com.siae.services;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class VerificationTokenService {

    public String buildVerificationLink(String token, String username) throws IOException {
        String html = new String(
                getClass().getClassLoader().getResourceAsStream("templates/confirm-email.html").readAllBytes(),
                StandardCharsets.UTF_8
        );

        String link = "https://siaeserver.com/public/users/confirm-email?token=" + token;
        String htmlLink = html.replace("${confirmationUrl}", link);
        htmlLink = htmlLink.replace("${userName}", username);
        return htmlLink;
    }
}
