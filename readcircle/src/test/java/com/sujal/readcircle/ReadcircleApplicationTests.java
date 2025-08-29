package com.sujal.readcircle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
class ReadcircleApplicationTests {

	@Test
	void contextLoads() {
	}

}
