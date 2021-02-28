package pl.aticode.civilworkoffers.unit.services;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.multipart.MultipartFile;

import pl.aticode.civilworkoffers.service.SendEmailService;

class SendEmailServiceTest {
	
	private SendEmailService sendEmailService;
	
	@Mock
	private JavaMailSender javaMailSender;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		sendEmailService = new SendEmailService("mailFrom", javaMailSender);
	}

	@Test
	void testSendEmailStringStringStringListOfMultipartFile() {
		List<MultipartFile> attachment = new ArrayList<>();
		sendEmailService.sendEmailMultipartFileAttachment("mailTo", "emailSubject", "emailContent", attachment);
	}

	@Test
	void testSendEmailStringStringString() {
		sendEmailService.sendEmail("mailTo", "emailSubject", "emailContent");
	}

}
