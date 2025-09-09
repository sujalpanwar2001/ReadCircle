package com.sujal.readcircle.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
@EnableAutoConfiguration(exclude = {
        MailSenderAutoConfiguration.class,
        JmxAutoConfiguration.class
})
public class TestConfig {

    @Bean
    @Primary
    @Profile("test")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.disable());

        return http.build();
    }

    @Bean
    @Primary
    @Profile("test")
    public JwtDecoder mockJwtDecoder() {
        return mock(JwtDecoder.class);
    }

    @Bean
    @Primary
    @Profile("test")
    public MailSender mockMailSender() {
        MailSender mockSender = mock(MailSender.class);
        // Configure mock to do nothing when send is called
        doNothing().when(mockSender).send(any(SimpleMailMessage.class));
        return mockSender;
    }
}