package pl.aticode.civilworkoffers.entity.offer;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import pl.aticode.civilworkoffers.entity.user.Employee;

@Entity
@Table(name = "offer")
@Getter
@Setter
public class Offer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 10, max = 2048)
    @Type(type = "text")
    private String content;
    
    private LocalDate sendDate;

    @Enumerated(EnumType.STRING)
    private OfferStage offerStage;

    private LocalDateTime saveDateTime;
    
    @OneToMany(mappedBy = "offer", orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<OfferAttachment> offerAttachment;

    @OneToOne
    private Employee employeerRegister;

    @OneToOne
    private Employee employeeSend;
    
}
