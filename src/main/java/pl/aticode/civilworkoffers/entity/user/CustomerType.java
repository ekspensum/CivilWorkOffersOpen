package pl.aticode.civilworkoffers.entity.user;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer_type")
@Getter @Setter
public class CustomerType implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String customerTypeName;
	private String customerTypeDescription;
	
	private LocalDateTime registerDateTime;
	private LocalDateTime editDateTime;
	
	@OneToOne
	private User userRegister;
	
	@OneToOne
	private User userEdit;
}
