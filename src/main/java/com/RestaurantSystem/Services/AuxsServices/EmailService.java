package com.RestaurantSystem.Services.AuxsServices;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    public void sendSimpleEmailNoReply(String to, String subject, String htmlPath,  Map<String, String> placeHolders) throws IOException, MessagingException {
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
}