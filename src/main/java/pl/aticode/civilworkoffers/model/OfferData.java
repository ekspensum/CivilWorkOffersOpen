/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author aticode.pl
 */
@Getter @Setter
public class OfferData {
    
    private Long offerRequestId;
    private String offerRequestContent;
    private LocalDate offerRequestRegister;
    private String offerRequestEmployee;
    private String employeePhotoBase64;
    private Long offerId;
    private String offerContent;
    private LocalDateTime offerSave;
    private LocalDate offerSend;
    private List<OfferAttachmentData> offerAttachmentDataList;
}
