/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.entity.offer;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pl.aticode.civilworkoffers.entity.user.Employee;

/**
 *
 * @author aticode.pl
 */
@Entity
@Table(name = "offer_attachment")
@Getter @Setter
public class OfferAttachment implements Serializable {
                
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Lob
    private byte [] file;
    
    private String fileName;
    private String fileType;
    
    @OneToOne
    private Employee employeeRegister;
    private LocalDateTime registerDateTime;
    
    @Enumerated(EnumType.STRING)
    private OfferStage offerStage;
    
    @ManyToOne
    private Offer offer;
}
