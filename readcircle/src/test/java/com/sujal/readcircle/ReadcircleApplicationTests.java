package com.sujal.readcircle;

import com.sujal.readcircle.auth.AuthenticationService;
import com.sujal.readcircle.book.BookService;
import com.sujal.readcircle.file.FileStorageService;
import com.sujal.readcircle.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReadcircleApplicationTests {
    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private BookService bookService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JavaMailSender javaMailSender;



    @Test
	void contextLoads() {
	}

}
