package com.sujal.readcircle.config;

import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.InputStream;

@Configuration
public class TestMailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        // Return a dummy implementation, emails won’t actually be sent
        return new JavaMailSender() {
            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {

            }

            @Override
            public MimeMessage createMimeMessage() {
                return null;
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                return null;
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {

            }
            // You can leave methods empty or throw UnsupportedOperationException
        };
    }
}