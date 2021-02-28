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
@Table(name = "unit_of_measurement")
@Getter
@Setter
@NamedQueries({
    @NamedQuery(name = "findAllUnitOfMeasurementForEmployee", query = "SELECT unitOfMeasurement FROM UnitOfMeasurement unitOfMeasurement WHERE unitOfMeasurement.employeeRegister = :employee"),
    @NamedQuery(name = "findUnitOfMeasurementByName", query = "SELECT unitOfMeasurement FROM UnitOfMeasurement unitOfMeasurement WHERE unitOfMeasurement.name = :name")
})
public class UnitOfMeasurement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 1, max = 20)
    @CharFilter(forbiddenChar = {"~", "`", "!", "@", "#", "$", "%", "^", "*", "+", "=", "{", "[", "}", "]", "\\", 
        "|", ":", ";", "\"", "'", "<", ">"}, action = CharFilterService.ActionType.REMOVE) // it works when is estimate service
    @Pattern(regexp = "^[^~`!#$%^*+={\\[}\\]\\|:;\"'<>]*$") //it works when is create estimate
    private String name;
    private LocalDateTime registerDateTime;

    @OneToOne
    private Employee employeeRegister;
}
