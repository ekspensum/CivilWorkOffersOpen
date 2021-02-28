package pl.aticode.civilworkoffers.entity.estimate;

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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.service.CharFilterService;

@Entity
@Table(name = "type_of_civil_work")
@Getter @Setter
@NamedQueries({
    @NamedQuery(name = "findAllTypeOfCivilWorksForEmployee", query = "SELECT typeOfCivilWork FROM TypeOfCivilWork typeOfCivilWork WHERE typeOfCivilWork.employeeRegister = :employee"),
    @NamedQuery(name = "findTypeOfCivilWorkByName", query = "SELECT typeOfCivilWork FROM TypeOfCivilWork typeOfCivilWork WHERE typeOfCivilWork.name = :name")
})
public class TypeOfCivilWork implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, max = 100)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "+", "=", "{", "[", "}", "]", "\\", 
        "|", ":", ";", "\"", "'", "<", ">"}, action = CharFilterService.ActionType.REMOVE) // it works when is estimate service
    @Pattern(regexp = "^[^~`!#$%^*+={\\[}\\]\\|:;\"'<>]*$") //it works when is create estimate
    private String name;
    
    private LocalDateTime registerDateTime;

    @OneToOne
    private Employee employeeRegister;
}
