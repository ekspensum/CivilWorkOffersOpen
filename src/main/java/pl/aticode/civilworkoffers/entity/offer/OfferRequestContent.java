/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.entity.offer;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.CharFilterService;

/**
 *
 * @author aticode.pl
 */
@Entity
@Table(name = "offer_request_content")
@Getter @Setter
@Indexed
public class OfferRequestContent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CharFilterService.CharFilter(forbiddenChar = {"~","`","^","{","[","}","]","\\","|",";","'","<",">"}, action = CharFilterService.ActionType.REPLACE)
    @Size(min = 10, max = 2048)
    @Type(type = "text")
    @Field
    private String content;
         
    @OneToOne
    private User userRegister;
    private LocalDateTime registerDateTime;

}
