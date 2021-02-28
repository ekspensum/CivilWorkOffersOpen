/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.entity.offer;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import pl.aticode.civilworkoffers.entity.user.User;

/**
 *
 * @author aticode.pl
 */
@Entity
@Table(name = "offer_request_attachment")
@Getter @Setter
public class OfferRequestAttachment implements Serializable {
                
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @Cascade(CascadeType.ALL)
    private ByteAttachment byteAttachment;
    
    private String fileName;
    private String fileType;
    private int fileSize;
    
    @OneToOne
    private User userRegister;
    private LocalDateTime registerDateTime;

}
