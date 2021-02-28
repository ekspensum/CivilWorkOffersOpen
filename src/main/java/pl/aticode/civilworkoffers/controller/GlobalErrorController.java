package pl.aticode.civilworkoffers.controller;

import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GlobalErrorController implements ErrorController {

	private final static Logger logger = LoggerFactory.getLogger(GlobalErrorController.class); 

	@GetMapping(path = "/error")
	public String handlingException(Exception ex, Model model) {
		if(ex instanceof NullPointerException) {
			model.addAttribute("exception", "exception.null");	
		}else if(ex instanceof MailException) {
			model.addAttribute("exception", "exception.mail");
		} else if(ex instanceof ConnectException) {
			model.addAttribute("exception", "exception.connect");	
		} else {
			model.addAttribute("exception", "exception.unknown");			
		}
		logger.error("ERROR from GlobalError controller {}", ex.getMessage());
		return "error";
	}
	
	@Override
	public String getErrorPath() {
		return "error";
	}

}
