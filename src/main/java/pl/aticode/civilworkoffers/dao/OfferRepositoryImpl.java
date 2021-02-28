/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.dao;

import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class OfferRepositoryImpl implements OfferRepository {
    
    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
    	Session session = sessionFactory.getCurrentSession();
    	return session;
    }
        
    @Override
    public void saveOfferRequest(OfferRequest offerRequest){
        getSession().persist(offerRequest);
    }
    
    @Override
    public void editOfferRequest(OfferRequest offerRequest){
        getSession().saveOrUpdate(offerRequest);
    }

    @Override
    public OfferRequest findOfferRequest(long id) {
        final OfferRequest offerRequest = getSession().find(OfferRequest.class, id);
        Hibernate.initialize(offerRequest.getOfferRequestAttachment());
        Hibernate.initialize(offerRequest.getOfferRequestContent());
        Hibernate.initialize(offerRequest.getOfferRequestComment());
        Hibernate.initialize(offerRequest.getOffer());
        Hibernate.initialize(offerRequest.getEstimate());
        return offerRequest;
    }

    @Override
    public List<OfferRequest> findAllOfferRequest(Customer customer) {
        final List<OfferRequest> resultList = getSession().createNamedQuery("findAllOfferRequestByCustomer", OfferRequest.class).setParameter("customer", customer).getResultList();
        for (OfferRequest offerRequest : resultList) {
            Hibernate.initialize(offerRequest.getOfferRequestContent());
            Hibernate.initialize(offerRequest.getOffer());
        }
        return resultList;
    }

    @Override
    public List<OfferRequest> findAllOfferRequest(Employee employee) {
        return getSession().createNamedQuery("findAllOfferRequestByEmployee", OfferRequest.class).setParameter("employee", employee).getResultList();
    }

    @Override
    public List<OfferRequest> findOfferRequestByDate(LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        return getSession().createNamedQuery("findOfferRequestByDate", OfferRequest.class)
                            .setParameter("dateTimeFrom", dateTimeFrom)
                            .setParameter("dateTimeTo", dateTimeTo)
                            .getResultList();
    }

    @Override
    public OfferRequest findOfferRequestInitializeEstimate(OfferRequest offerRequest) {
        final OfferRequest foundOfferRequest = getSession().find(OfferRequest.class, offerRequest.getId());
        Hibernate.initialize(foundOfferRequest.getEstimate());
        return foundOfferRequest;
    }

    @Override
    public OfferRequest findOfferRequestInitializeOfferAndEstimate(OfferRequest offerRequest) {
        final OfferRequest foundOfferRequest = getSession().find(OfferRequest.class, offerRequest.getId());
        Hibernate.initialize(foundOfferRequest.getOffer());
        Hibernate.initialize(foundOfferRequest.getEstimate());
        return foundOfferRequest;
    }

    @Override
    public List<OfferRequestContent> findOfferRequestContentInitialize(OfferRequest offerRequest) {
        final OfferRequest foundOfferRequest = getSession().find(OfferRequest.class, offerRequest.getId());
        Hibernate.initialize(foundOfferRequest.getOfferRequestContent());
        return foundOfferRequest.getOfferRequestContent();
    }

    @Override
    public ByteAttachment findByteAttachment(long id) {
        return getSession().find(ByteAttachment.class, id);
    }

    @Override
    public OfferRequestAttachment findFile(long id) {
        return getSession().find(OfferRequestAttachment.class, id);
    }

    @Override
    public List<Offer> findOffer(OfferRequest offerRequest) {
        final OfferRequest foundOfferRequest = getSession().find(OfferRequest.class, offerRequest.getId());
        Hibernate.initialize(foundOfferRequest.getOffer());
        return foundOfferRequest.getOffer();
    }

    @Override
    public Offer findOffer(long id) {
        return getSession().find(Offer.class, id);
    }

    @Override
    public Estimate findEstimateDetach(long id) {
        final Estimate estimate = getSession().find(Estimate.class, id);
        getSession().detach(estimate);
        return estimate;
    }

    @Override
    public EstimateItem findEstimateItemDetach(long id) {
        final EstimateItem estimateItem = getSession().find(EstimateItem.class, id);
        getSession().detach(estimateItem);
        return estimateItem;
    }

    @Override
    public List<Estimate> findOrphanEstimatesEmployee(Employee employee) {
        return getSession().createNamedQuery("findOrphanEstimatesEmployee", Estimate.class).setParameter("employee", employee).getResultList();
    }

    @Override
    public List<Estimate> findAllEstimatesEmployee(Employee employee) {
        return getSession().createNamedQuery("findAllEstimatesEmployee", Estimate.class).setParameter("employee", employee).getResultList();
    }

    @Override
    public void detachEstimateFromOfferRequest(OfferRequest offerRequest, Estimate estimate) {
        getSession().createNativeQuery("DELETE FROM offer_request_estimate WHERE OfferRequest_id = :offerRequestId AND estimate_id = :estimateId")
                .setParameter("offerRequestId", offerRequest.getId()).setParameter("estimateId", estimate.getId()).executeUpdate();
    }

    @Override
    public void removeEstimate(long id) {
        final Estimate estimate = getSession().find(Estimate.class, id);
        getSession().remove(estimate);
    }

    @Override
    public OfferAttachment findOfferAttachment(long id) {
        return getSession().find(OfferAttachment.class, id);
    }

    @Override
    public void saveTypeOfCivilWorks(TypeOfCivilWork typeOfCivilWork) {
        getSession().persist(typeOfCivilWork);
    }

    @Override
    public void saveUnitOfMeasurement(UnitOfMeasurement unitOfMeasurement) {
        getSession().persist(unitOfMeasurement);
    }
    
    @Override
    public List<TypeOfCivilWork> findAllTypeOfCivilWorks() {
        return getSession().createQuery("from TypeOfCivilWork", TypeOfCivilWork.class).getResultList();
    }

    @Override
    public List<TypeOfCivilWork> findAllTypeOfCivilWorks(Employee employee) {
        return getSession().createNamedQuery("findAllTypeOfCivilWorksForEmployee", TypeOfCivilWork.class).setParameter("employee", employee).getResultList();
    }

    @Override
    public List<TypeOfCivilWork> findTypeOfCivilWork(String name) {
        return getSession().createNamedQuery("findTypeOfCivilWorkByName", TypeOfCivilWork.class).setParameter("name", name).getResultList();
    }

    @Override
    public void removeTypeOfCivilWork(long id) {
        final TypeOfCivilWork typeOfCivilWork = getSession().find(TypeOfCivilWork.class, id);
        getSession().remove(typeOfCivilWork);
    }

    @Override
    public List<UnitOfMeasurement> findAllUnitOfMeasurement() {
        return getSession().createQuery("from UnitOfMeasurement", UnitOfMeasurement.class).getResultList();
    }

    @Override
    public List<UnitOfMeasurement> findAllUnitOfMeasurement(Employee employee) {
        return getSession().createNamedQuery("findAllUnitOfMeasurementForEmployee", UnitOfMeasurement.class).setParameter("employee", employee).getResultList();
    }

    @Override
    public List<UnitOfMeasurement> findUnitOfMeasurement(String name) {
        return getSession().createNamedQuery("findUnitOfMeasurementByName", UnitOfMeasurement.class).setParameter("name", name).getResultList();
    }

    @Override
    public void removeUnitOfMeasurement(long id) {
        final UnitOfMeasurement unitOfMeasurement = getSession().find(UnitOfMeasurement.class, id);
        getSession().remove(unitOfMeasurement);
    }
    
}
