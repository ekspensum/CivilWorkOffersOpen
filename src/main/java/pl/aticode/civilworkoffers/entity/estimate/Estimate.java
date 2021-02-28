package pl.aticode.civilworkoffers.entity.estimate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import pl.aticode.civilworkoffers.entity.user.Employee;

@Entity
@Table(name = "estimate")
@Getter @Setter
@Indexed
@NamedQueries({
    @NamedQuery(name = "findOrphanEstimatesEmployee", query = "SELECT estimate FROM OfferRequest offerRequest RIGHT JOIN offerRequest.estimate estimate WHERE offerRequest IS NULL AND estimate.employee = :employee"),
    @NamedQuery(name = "findAllEstimatesEmployee", query = "SELECT estimate FROM Estimate estimate WHERE estimate.employee = :employee")
})
public class Estimate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Field
    @Size(min = 5, max = 255)
    @Pattern(regexp = "^[^~`!#$%^*+={\\[}\\]\\|:;\"'<>]*$")
    private String description;

    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[^~`!#$%^*+={\\[}\\]\\|:;\"',<>]*$")
    private String typeOfCivilWork;

    @OneToMany(mappedBy = "estimate", orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Cascade(CascadeType.ALL)
    @Valid
    private List<EstimateItem> estimateItem;
    
    @Field
    @SortableField
    private LocalDateTime lastSaved;

    @ManyToOne
    private Employee employee;

}
