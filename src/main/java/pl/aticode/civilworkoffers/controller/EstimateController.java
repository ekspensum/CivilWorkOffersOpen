/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import pl.aticode.civilworkoffers.entity.estimate.Estimate;
import pl.aticode.civilworkoffers.entity.estimate.EstimateItem;
import pl.aticode.civilworkoffers.entity.estimate.TypeOfCivilWork;
import pl.aticode.civilworkoffers.entity.estimate.UnitOfMeasurement;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.HibernateSearchService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.UserService;

/**
 *
 * @author aticode.pl
 */
@Controller
@SessionAttributes(names = {"offerRequest", "estimateIdx"})
public class EstimateController {

    private final static Logger logger = LoggerFactory.getLogger(EstimateController.class);
    private String typeOfCivilWorkMatches = "^[^~`!#$%^*+={\\[}\\]\\|:;\"'<>]*$";
    private String unitOfMeasurementMatches = "^[^~`!#$%^*+={\\[}\\]\\|:;\"'<>]*$";

    @Autowired
    private UserService userService;
    @Autowired
    private CharFilterService charFilterService;
    @Autowired
    private OfferService offerService;
    @Autowired
    private HibernateSearchService searchService;

    /**
     * Get data for estimate details.
     * @param offerRequest
     * @param model
     * @return
     */
    @GetMapping(path = "/estimate/details")
    public String estimateDetails(@SessionAttribute(name = "offerRequest", required = false) OfferRequest offerRequest, Model model) {
        if (offerRequest != null) {
            offerService.loadDataForOfferRequestOfferEsimate(offerRequest, model);
        }
        return "estimate/estimateDetails";
    }

    /**
     * Detach estimate from offer request.
     * @param offerRequest
     * @param detachEstimateIdx
     * @param model
     * @return
     */
    @PostMapping(path = "/estimate/details")
    public String estimateDetails(@SessionAttribute(name = "offerRequest", required = false) OfferRequest offerRequest,
            @RequestParam(name = "detachEstimateIdx", required = false) Integer detachEstimateIdx,
            Model model) {
        if (detachEstimateIdx != null) {
            try {
                final Estimate estimate = offerRequest.getEstimate().get(detachEstimateIdx);
                offerService.updateOfferRequestEstimateDetach(offerRequest, estimate);
                logger.info("Detached estimate id {} from offer request.", estimate.getId());
                model.addAttribute("message", "estimate.detach.success");
            } catch (Exception e) {
                logger.error("ERROR detach estimate ", e.getMessage());
                model.addAttribute("message", "estimate.detach.defeat");
            }
        }
        offerService.loadDataForOfferRequestOfferEsimate(offerRequest, model);
        return "estimate/estimateDetails";
    }
    
    /**
     * New estimate.
     * @return
     */
    @GetMapping(path = "/estimate/new")
    public String newEstimate() {
        return "estimate/newEstimate";
    }

    /**
     * Method provide search, copy and connect estimate. 
     * @param searchEstimate
     * @param estimateIdToCopy
     * @param estimateIdToConnect
     * @param estimateData
     * @param offerRequest
     * @param redirectAttributes
     * @param model
     * @return
     */
    @PostMapping(path = "/estimate/new")
    public String newEstimate(@RequestParam(name = "searchEstimate", required = false) String searchEstimate,
            @RequestParam(name = "estimateIdToCopy", required = false) Long estimateIdToCopy,
            @RequestParam(name = "estimateIdToConnect", required = false) Long estimateIdToConnect,
            @RequestParam(name = "estimateData", required = false) String estimateData,
            @SessionAttribute(name = "offerRequest") OfferRequest offerRequest,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (searchEstimate != null) {
            final List<Estimate> orphanEstimateList = offerService.getOrphanEstimatesEmployee();
            model.addAttribute("orphanEstimateList", orphanEstimateList);
            final List<Estimate> estimateList = searchService.searchEstimateByDescription(estimateData);
            model.addAttribute("estimateList", estimateList);
        } else if (estimateIdToCopy != null) {
            try {
                offerService.copyEstimate(offerRequest, estimateIdToCopy);
                logger.info("Copy estimate id {} to offer request id {}", estimateIdToCopy, offerRequest.getId());
                redirectAttributes.addFlashAttribute("message", "estimate.copy.success");
                return "redirect:/estimate/details";
            } catch (Exception ex) {
                logger.error("ERROR copy estimate to offer request id {} {}", offerRequest.getId(), ex.getMessage());
                redirectAttributes.addFlashAttribute("message", "estimate.copy.defeat");
                return "redirect:/estimate/details";
            }
        } else if(estimateIdToConnect != null){
            try {
                offerService.updateOfferRequestEstimateConnect(offerRequest, estimateIdToConnect);
                logger.info("Connect estimate id {} to offer request id {}", estimateIdToConnect, offerRequest.getId());
                redirectAttributes.addFlashAttribute("message", "estimate.connect.success");
                return "redirect:/estimate/details";
            } catch (Exception ex) {
                logger.error("ERROR connect estimate to offer request id {} {}", offerRequest.getId(), ex.getMessage());
                redirectAttributes.addFlashAttribute("message", "estimate.connect.defeat");
                return "redirect:/estimate/details";
            }            
        }
        return "estimate/newEstimate";
    }

