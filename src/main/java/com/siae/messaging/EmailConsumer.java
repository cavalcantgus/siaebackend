package com.siae.messaging;

import com.siae.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.persistence.Id;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
@Component
public class EmailConsumer {

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void consume(EmailMessage emailMessage) {
        try {
            String html = new String(
                    getClass().getClassLoader().getResourceAsStream("templates/notification" +
                            "-entrega.html").readAllBytes(),
                    StandardCharsets.UTF_8
            );
            String link = "http://localhost:5173/siaefrontend/login";

            String htmlLink = html.replace("${userName}", emailMessage.getUsername());
            htmlLink = htmlLink.replace("${confirmationUrl}", link);

            emailService.sendEmail(emailMessage.getTo(), emailMessage.getSubject(), htmlLink);
            System.out.println("Message sent to " + emailMessage.getTo() + emailMessage.getBody());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
