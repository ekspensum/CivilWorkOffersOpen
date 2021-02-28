package pl.aticode.civilworkoffers.model;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import pl.aticode.civilworkoffers.service.CharFilterService.ActionType;
import pl.aticode.civilworkoffers.service.CharFilterService.CharFilter;

@Getter @Setter
public class ContactUs {

	@Size(min=2, max=50)
	@CharFilter(forbiddenChar = {"~","`","^","=","{","[","}","]","\\","|","<",">"}, action = ActionType.REPLACE)
	private String subject;
	
	@Size(min=2, max=300)
	@CharFilter(forbiddenChar = {"~","`","^","=","{","[","}","]","\\","|","<",">"}, action = ActionType.REPLACE)
	private String message;
	
	@Email
	@NotEmpty
	private String replyEmail;
	
	private List<MultipartFile> attachment;
	
	boolean recaptcha;
}
