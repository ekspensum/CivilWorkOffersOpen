package pl.aticode.civilworkoffers.entity.user;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "user")
@Getter @Setter
@Indexed
@NamedQueries({
	@NamedQuery(name = "findUserByUserName", query = "SELECT user FROM User user WHERE user.username = :username"),
	@NamedQuery(name = "findUserBySocialUserId", query = "SELECT user FROM Customer customer INNER JOIN customer.user user WHERE customer.socialUserId = :socialUserId"),
    @NamedQuery(name = "removeUser", query = "DELETE FROM User user WHERE user = :user")
})
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Pattern(regexp="^[a-zA-Z0-9_@.-]{3,40}$")
	@Field
	private String username;
	
	private String password;
    @Field
	private boolean enabled;
	
	@Size(min = 4, max = 24)
	@Transient
	private String passwordField;
	
	@ManyToMany
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<Role> roles;

}

