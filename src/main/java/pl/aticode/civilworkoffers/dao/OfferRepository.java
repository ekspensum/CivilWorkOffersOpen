/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.dao;

import java.time.LocalDateTime;
import java.util.List;
import pl.aticode.civilworkoffers.entity.estimate.Estimate;
import pl.aticode.civilworkoffers.entity.estimate.EstimateItem;
import pl.aticode.civilworkoffers.entity.estimate.TypeOfCivilWork;
import pl.aticode.civilworkoffers.entity.estimate.UnitOfMeasurement;
import pl.aticode.civilworkoffers.entity.offer.ByteAttachment;
import pl.aticode.civilworkoffers.entity.offer.Offer;
import pl.aticode.civilworkoffers.entity.offer.OfferAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestContent;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;

/**
 *
 * @author aticode.pl
 */
public interface OfferRepository {

    void saveOfferRequest(OfferRequest offerRequest);
    void editOfferRequest(OfferRequest offerRequest);
    OfferRequest findOfferRequest(long id);  
    List<OfferRequest> findAllOfferRequest(Customer customer);
    List<OfferRequest> findAllOfferRequest(Employee employee);
    List<OfferRequest> findOfferRequestByDate(LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo);
    OfferRequest findOfferRequestInitializeEstimate(OfferRequest offerRequest);
    OfferRequest findOfferRequestInitializeOfferAndEstimate(OfferRequest offerRequest);
    List<OfferRequestContent> findOfferRequestContentInitialize(OfferRequest offerRequest);
    
    ByteAttachment findByteAttachment(long id);
    
    OfferRequestAttachment findFile(long id);
    List<Offer> findOffer(OfferRequest offerRequest);
    Offer findOffer(long id);
    
    Estimate findEstimateDetach(long id);
    EstimateItem findEstimateItemDetach(long id);
    List<Estimate> findOrphanEstimatesEmployee(Employee employee);
    List<Estimate> findAllEstimatesEmployee(Employee employee);
    void detachEstimateFromOfferRequest(OfferRequest offerRequest, Estimate estimate);
    void removeEstimate(long id);
    OfferAttachment findOfferAttachment(long id);
    
    void saveTypeOfCivilWorks(TypeOfCivilWork typeOfCivilWork);
    void saveUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement);
    List<TypeOfCivilWork> findAllTypeOfCivilWorks();
    List<TypeOfCivilWork> findAllTypeOfCivilWorks(Employee employee);
    List<TypeOfCivilWork> findTypeOfCivilWork(String name);
    void removeTypeOfCivilWork(long id);
    List<UnitOfMeasurement> findAllUnitOfMeasurement();
    List<UnitOfMeasurement> findAllUnitOfMeasurement(Employee employee);
    List<UnitOfMeasurement> findUnitOfMeasurement(String name);
    void removeUnitOfMeasurement(long id);
}
