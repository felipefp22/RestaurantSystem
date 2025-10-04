package com.RestaurantSystem.Services.AuxsServices;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {
    @Value("${no.reply.email}")
    private String noReplyEmail;
    @Value("${email.image.url}")
    private String emailImageUrl;

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmailNoReply(String to, String subject, String htmlPath, Map<String, String> placeHolders) throws IOException, MessagingException {
//        ClassPathResource resource = new ClassPathResource(htmlPath);
//        String htmlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
//
//        if (!placeHolders.containsKey("imageUrl")) {
//            placeHolders = new HashMap<>(placeHolders); // make mutable
//            placeHolders.put("imageUrl", "https://deliverysystem.com.br/Imagebanker/DeliverySystemLogo.jpeg");
//        }
//        for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
//            htmlContent = htmlContent.replace("${" + entry.getKey() + "}", entry.getValue());
//        }
//
//
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//        helper.setFrom(noReplyEmail);
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(htmlContent, true); // true = send HTML
//        mailSender.send(message);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(noReplyEmail);
        message.setTo(to);
        message.setSubject(subject);
        for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
            if (entry.getKey().equals("body")) message.setText(entry.getValue());
        }
        mailSender.send(message);
    }

    public void sendSimpleEmailResendsite(String to, String subject, String htmlPath, Map<String, String> placeHolders) throws IOException, MessagingException {
        ClassPathResource resource = new ClassPathResource(htmlPath);
        String htmlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        if (!placeHolders.containsKey("imageUrl")) {
            placeHolders = new HashMap<>(placeHolders);
            placeHolders.put("imageUrl", "https://akitemtrampo.com.br/Imagebanker/RestaurantDeliveryLogo.jpeg");
        }

        for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
            htmlContent = htmlContent.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        Resend resend = new Resend(noReplyEmail);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Restaurant Delivery <onboarding@resend.dev>") // You can use your verified sender later
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("Email sent! ID: " + response.getId());
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}