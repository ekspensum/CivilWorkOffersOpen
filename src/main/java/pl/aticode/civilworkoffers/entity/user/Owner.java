package pl.aticode.civilworkoffers.entity.user;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.pl.REGON;

import lombok.Getter;
import lombok.Setter;
import pl.aticode.civilworkoffers.service.CharFilterService.ActionType;
import pl.aticode.civilworkoffers.service.CharFilterService.CharFilter;

@Entity
@Table(name = "owner")
@Getter
@Setter
@NamedQueries({
    @NamedQuery(name = "findOwnerByUserName", query = "SELECT owner FROM Owner owner INNER JOIN owner.user user WHERE user.username = :username")
})
public class Owner implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, max = 15)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String firstName;

    @Size(min = 3, max = 25)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String lastName;

    @Size(min = 3, max = 50)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String companyName;

    @REGON
    private String regon;

    @Pattern(regexp = "^[0-9]{2}-[0-9]{3}$")
    private String zipCode;

    @Size(min = 2, max = 20)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String city;

    @Size(min = 2, max = 20)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String street;

    @Size(min = 1, max = 10)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String streetNo;

    @Size(min = 0, max = 10)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "(", ")", "+", "=", "{", "[", "}", "]", "\\", "|", ":", ";", "\"", "'", ",", "<", ">", ".", "/"}, action = ActionType.REMOVE)
    private String unitNo;

    @Email
    @NotEmpty
    private String email;

    @Pattern(regexp = "^[0-9-+()]{9,20}$")
    private String phone;

    @NotEmpty
    private String language;

    @Valid
    @OneToOne
    @Cascade({CascadeType.PERSIST, CascadeType.SAVE_UPDATE})
    private User user;

    private LocalDateTime registeredDateTime;
    private LocalDateTime editDateTime;

    @OneToOne
    private User userEdit;

}

