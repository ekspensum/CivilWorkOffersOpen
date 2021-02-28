package pl.aticode.civilworkoffers.entity.offer;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import pl.aticode.civilworkoffers.entity.estimate.Estimate;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.User;

@Entity
@Table(name = "offer_request")
@Getter @Setter
@NamedQueries({
    @NamedQuery(name = "findAllOfferRequestByCustomer", query = "SELECT offerRequest FROM OfferRequest offerRequest WHERE offerRequest.customer = :customer"),
    @NamedQuery(name = "findAllOfferRequestByEmployee", query = "SELECT offerRequest FROM OfferRequest offerRequest WHERE offerRequest.employee = :employee"),
    @NamedQuery(name = "findOfferRequestByDate", query = "SELECT offerRequest FROM OfferRequest offerRequest WHERE offerRequest.registerDateTime BETWEEN :dateTimeFrom AND :dateTimeTo"),
    @NamedQuery(name = "findOfferRequestByDateEmployee", query = "SELECT offerRequest FROM OfferRequest offerRequest "
            + "WHERE offerRequest.registerDateTime BETWEEN :dateTimeFrom AND :dateTimeTo AND offerRequest.employee = :employee"),
    @NamedQuery(name = "removeOfferRequestForCustomer", query = "DELETE FROM OfferRequest offerRequest WHERE offerRequest.customer = :customer")
})
@Indexed
public class OfferRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    @Valid
    @Cascade(CascadeType.ALL)
    @IndexedEmbedded
    private List<OfferRequestContent> offerRequestContent;

    @OneToMany
    @Valid
    @Cascade(CascadeType.ALL)
    private List<OfferRequestComment> offerRequestComment;
    
    @OneToMany
    @Valid
    @Cascade(CascadeType.ALL)
    private List<OfferRequestAttachment> offerRequestAttachment;
    
    @OneToMany
    @Valid
    @Cascade(CascadeType.ALL)
    private List<Offer> offer;
    
    @OneToMany
    @Valid
    @Cascade({CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.SAVE_UPDATE})
    private List<Estimate> estimate;
    
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Field
    private LocalDate executionDate;

    @Transient
    private List<MultipartFile> attachments;

    @OneToOne
    @IndexedEmbedded
    private Customer customer;

    @OneToOne    
    @IndexedEmbedded(includeEmbeddedObjectId = true)
    private Employee employee;

    @Field
    @SortableField
    private LocalDateTime registerDateTime;
    private LocalDateTime editDateTime;

    @OneToOne
    private User userRegister;

    @OneToOne
    private User userEdit;

}
