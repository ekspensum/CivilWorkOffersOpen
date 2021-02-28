package pl.aticode.civilworkoffers.entity.home;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Base64;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.CharFilterService.ActionType;
import pl.aticode.civilworkoffers.service.CharFilterService.CharFilter;

@Entity
@Table(name = "logo_slogan_footer")
@Getter @Setter
public class LogoSloganFooter implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Size(min = 0, max = 150)
	@CharFilter(forbiddenChar = {"~","`","$","%","^","=","{","[","}","]","\\","|",":",";","'",",","<",">"}, action = ActionType.REMOVE)
	private String slogan;
	
	@Size(min = 0, max = 150)
	@CharFilter(forbiddenChar = {"~","`","$","%","^","=","{","[","}","]","\\","|",":",";","'",",","<",">"}, action = ActionType.REMOVE)
	private String footer;
	
//	limits file size is in use at AdminController
	@Lob
	private byte [] logo;
	
	private LocalDateTime registerDateTime;
	private LocalDateTime editDateTime;
	
	@OneToOne
	private User userRegister;
	
	@OneToOne
	private User userEdit;
	
	public String getBase64Logo() {
		if(logo == null) {
			return "";
		} else {
			return Base64.getEncoder().encodeToString(this.logo);			
		}
	}
}
