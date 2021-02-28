package pl.aticode.civilworkoffers.entity.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Base64;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import lombok.Getter;
import lombok.Setter;
import pl.aticode.civilworkoffers.service.CharFilterService.ActionType;
import pl.aticode.civilworkoffers.service.CharFilterService.CharFilter;

@Entity
@Table(name = "customer")
@Getter
@Setter
@Indexed
@NamedQueries({
    @NamedQuery(name = "findCustomerByUserName", query = "SELECT customer FROM Customer customer INNER JOIN customer.user user WHERE user.username = :username"),
    @NamedQuery(name = "removeCustomer", query = "DELETE FROM Customer customer WHERE customer = :customer")
})
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Field
    private String socialUserId;

    /*
	 * this field define whether social customer can also use login form (user name and password) in this application. 
     */
    private boolean activeLoginForm;

    @Size(min = 3, max = 15)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String firstName;

    @Size(min = 3, max = 25)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    @Field
    private String lastName;

//	This field has validation in controllers
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    @Field
    private String companyName;

//	This field has validation in controllers
    @Field
    private String regon;

    @Size(min = 0, max = 20)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String country;

    @Pattern(regexp = "^$|^[0-9]{2}-[0-9]{3}$")
    private String zipCode;

    @Size(min = 0, max = 20)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String city;

    @Size(min = 0, max = 20)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    @Field
    private String street;

    @Size(min = 0, max = 10)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String streetNo;

    @Size(min = 0, max = 10)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String unitNo;

    @Email
    @NotEmpty
    @Field
    private String email;

    @Pattern(regexp = "^[0-9-+()]{9,20}$")
    @Field
    private String phone;

    @NotEmpty
    private String language;

    @Size(min = 0, max = 50000)
    @Lob
    private byte[] photo;

    @Valid
    @OneToOne
    @Cascade({CascadeType.PERSIST, CascadeType.SAVE_UPDATE, CascadeType.REMOVE})
    @IndexedEmbedded
    private User user;

    @OneToOne
    private CustomerType customerType;

    @Field
    private String activationString;

    @Transient
    private boolean recaptcha;
    private LocalDateTime registerDateTime;
    private LocalDateTime editDateTime;

    @OneToOne
    private User userRegister;

    @OneToOne
    private User userEdit;

    public String getBase64Photo() {
        if (photo == null) {
            return "";
        } else {
            return Base64.getEncoder().encodeToString(this.photo);
        }
    }
}

