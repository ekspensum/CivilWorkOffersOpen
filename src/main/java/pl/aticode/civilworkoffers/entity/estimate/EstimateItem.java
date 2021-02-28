package pl.aticode.civilworkoffers.entity.estimate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;

@Entity
@Table(name = "estimate_item")
@Getter
@Setter
@Indexed
public class EstimateItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int sortIndex;

    @Field
    @Size(min = 5, max = 255)
    @Pattern(regexp = "^[^~`!#$%^*+={\\[}\\]\\|:;\"'<>]*$")
    private String description;
    
    @DecimalMax(value = "10000000.0")
    @Column(name="quantity", columnDefinition="Decimal(10,3) default '0.000'")
    private BigDecimal quantity;

    @Field
    @SortableField
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10000000.0")
    private BigDecimal price;

    @Size(min = 1, max = 20)
    @Pattern(regexp = "^[^~`!#$%^*+={\\[}\\]\\|:;\"',<>]*$")
    private String unitOfMeasurement;
    private LocalDate lastSaved;

    @ManyToOne
    private Estimate estimate;
}
