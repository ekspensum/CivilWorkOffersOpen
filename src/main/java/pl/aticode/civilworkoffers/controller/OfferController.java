/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.controller;

import com.itextpdf.text.DocumentException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.aticode.civilworkoffers.entity.offer.Offer;
import pl.aticode.civilworkoffers.entity.offer.OfferAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferStage;
import pl.aticode.civilworkoffers.service.OfferService;

/**
 *
 * @author aticode.pl
 */
@Controller
@SessionAttributes(names = {"offerRequest", "offerIdx"})
public class OfferController {

    private final static Logger logger = LoggerFactory.getLogger(OfferController.class);

    @Autowired
    private OfferService offerService;
    @Autowired
    private MessageSource messageSource;

    /**
     * Get data to show offer.
     * @param offerRequest
     * @param model
     * @return
     */
    @GetMapping(path = "/offer/employee/showoffer")
    public String showOffer(@SessionAttribute(name = "offerRequest", required = false) OfferRequest offerRequest, Model model) {
        if (offerRequest != null) {
            final List<Offer> offers = offerService.getInitializeOffer(offerRequest);
            offerRequest.setOffer(offers);
        }
        return "offer/employee/showOffer";
    }
    
    /**
     * Provide send offer again to customer.
     * @param offerRequest
     * @param sendOfferAgainId
     * @param redirectAttributes
     * @return
     */
    @PostMapping(path = "/offer/employee/showoffer")
    public String showOffer(@SessionAttribute(name = "offerRequest") OfferRequest offerRequest, 
                            @RequestParam(name = "sendOfferAgainId", required = false) Integer sendOfferAgainId,
                            RedirectAttributes redirectAttributes) {
        if (sendOfferAgainId != null) {
            try {
                offerService.sendOfferAgain(offerRequest, sendOfferAgainId);
                logger.info("Sent again Offer to offer request id: {}", offerRequest.getId());
                redirectAttributes.addFlashAttribute("message", "offer.send.success");
                return "redirect:/employee/main";
            } catch (Exception ex) {
                logger.error("ERROR send Offer {}", ex);
                redirectAttributes.addFlashAttribute("message", "offer.send.defeat");
                return "redirect:/employee/main";
            }
        }
        return "offer/employee/showOffer";
    }
 
