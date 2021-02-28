package pl.aticode.civilworkoffers.entity.home;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;
import pl.aticode.civilworkoffers.entity.user.User;

@Entity
@Table(name = "main_pages")
@Getter @Setter
@NamedQueries({
	@NamedQuery(name = "findMainPageWithPrameter", query = "SELECT mainPages FROM MainPages mainPages WHERE mainPages.pageType = :pageType")
})
public class MainPages implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private PageType pageType;
	
	@Size(min = 10, max = 1024000)
	@Type(type = "text")
	private String content;
	
	private LocalDateTime registerDateTime;
	private LocalDateTime editDateTime;
	
	@OneToOne
	private User userRegister;
	
	@OneToOne
	private User userEdit;
}
