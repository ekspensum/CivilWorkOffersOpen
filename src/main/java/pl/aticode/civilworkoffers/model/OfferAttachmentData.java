/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.model;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author aticode.pl
 */
@Getter @Setter
public class OfferAttachmentData {

    private Long attachmentId;
    private String attachmentName;
    private String attachmentType;
    private int attachmentSize;
}
