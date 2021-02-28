package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.aticode.civilworkoffers.service.ReCaptchaService;

class ReCaptchaServiceTest {
	
	private ReCaptchaService reCaptchaService;

	@BeforeEach
	void setUp() throws Exception {
		reCaptchaService = new ReCaptchaService();
	}

	@Test
	void testVerify() throws IOException {
		assertFalse(reCaptchaService.verify("reCaptchaResponse"));
		assertFalse(reCaptchaService.verify(""));
	}

}
