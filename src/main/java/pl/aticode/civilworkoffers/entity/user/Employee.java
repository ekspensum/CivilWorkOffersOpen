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
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import lombok.Getter;
import lombok.Setter;
import pl.aticode.civilworkoffers.service.CharFilterService.ActionType;
import pl.aticode.civilworkoffers.service.CharFilterService.CharFilter;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NamedQueries({
    @NamedQuery(name = "findEmployeeByUserName", query = "SELECT employee FROM Employee employee INNER JOIN employee.user user WHERE user.username = :username")
})
public class Employee implements Serializable {

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

    @Email
    @NotEmpty
    private String email;

    @Pattern(regexp = "^$|^[0-9-+()]{9,20}$")
    private String phone;

    @Pattern(regexp = "^$|^[0-9]{11}$")
    private String pesel;

    @NotEmpty
    private String language;

    @Size(min = 0, max = 600000)
    @Lob
    private byte[] photo;

    @Valid
    @OneToOne
    @Cascade(CascadeType.ALL)
    private User user;

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
