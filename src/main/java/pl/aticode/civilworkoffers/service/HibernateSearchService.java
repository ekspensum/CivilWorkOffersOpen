package pl.aticode.civilworkoffers.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.aticode.civilworkoffers.entity.estimate.Estimate;
import pl.aticode.civilworkoffers.entity.estimate.EstimateItem;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;

import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;

@Service
public class HibernateSearchService {

    private final static Logger logger = LoggerFactory.getLogger(HibernateSearchService.class);

    private SessionFactory sessionFactory;
    private UserService userService;

    @Autowired
    public HibernateSearchService(SessionFactory sessionFactory, UserService userService) {
    	this.userService = userService;
        this.sessionFactory = sessionFactory;
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.openSession());
        try {
            fullTextSession.createIndexer()
                    .batchSizeToLoadObjects(15)
                    .cacheMode(CacheMode.NORMAL)
                    .threadsToLoadObjects(3)
                    .startAndWait();
        } catch (InterruptedException e) {
            logger.error("ERROR create index.", e);
        }
    }

    /**
     * Method searching for customer by username or last name or company name or
     * regon or street or phone or email.
     *
     * @param text
     * @return list of found customers.
     */
    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Customer> searchCustomerNameRegonStreetPhoneByKeywordQuery(String text) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Customer.class)
                .get();
        Query luceneQuery = queryBuilder
                .keyword()
                .fuzzy()
                .withEditDistanceUpTo(2)
                .withPrefixLength(0)
                .onFields("user.username", "lastName", "companyName", "regon", "street", "phone", "email")
                .matching(text)
                .createQuery();
        return fullTextSession.createFullTextQuery(luceneQuery, Customer.class).getResultList();
    }
    
    /**
     * Method searching for active customer by username or last name or company name or regon or street or phone or email.
     * @param text
     * @return active customer list
     */
    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Customer> searchCustomerEnabledNameRegonStreetPhoneByKeywordQuery(String text) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Customer.class)
                .get();
        Query luceneQuery = queryBuilder
                .bool()
                .must(queryBuilder
                        .keyword()
                        .fuzzy()
                        .withEditDistanceUpTo(2)
                        .withPrefixLength(0)
                        .onFields("user.username", "lastName", "companyName", "regon", "street", "phone", "email")
                        .matching(text)
                        .createQuery())
                .must(queryBuilder
                        .keyword()
                        .onField("user.enabled")
                        .matching(Boolean.TRUE)
                        .createQuery())
                .createQuery();
        return fullTextSession.createFullTextQuery(luceneQuery, Customer.class).getResultList();
    }    

    /**
     * Method searching customer by activation string during registration process.
     * @param activationString
     * @return customer or null if not found.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Customer searchCustomerToActivation(String activationString) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Customer.class)
                .get();
        Query luceneQuery = queryBuilder
                .keyword()
                .onField("activationString")
                .matching(activationString)
                .createQuery();
        return (Customer) fullTextSession.createFullTextQuery(luceneQuery, Customer.class).uniqueResult();
    }

    /**
     * Searching offer request for selected employee.
     * @param dateTimeFrom
     * @param dateTimeTo
     * @param dateFrom
     * @param dateTo
     * @param text
     * @return offer request list sorted by registered date time
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<OfferRequest> searchOfferRequestForEmployee(LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, LocalDate dateFrom, LocalDate dateTo, String text) {
        final Employee loggedEmployee = userService.getLoggedEmployee();
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(OfferRequest.class)
                .get();
        Query luceneQuery = queryBuilder
                .bool()
                .must(queryBuilder
                        .keyword()
                        .onField("employee.id").matching(loggedEmployee.getId()).createQuery())
                .must(queryBuilder
                        .range()
                        .onField("registerDateTime").from(dateTimeFrom).to(dateTimeTo).createQuery())
                .must(queryBuilder
                        .range()
                        .onField("executionDate").from(dateFrom).to(dateTo).createQuery())
                .must(queryBuilder
                        .keyword().wildcard()
                        .onFields("customer.lastName", "customer.companyName", "offerRequestContent.content", "customer.phone", "customer.email")
                        .matching("*" + text + "*").createQuery())
                .createQuery();
        final Sort sort = queryBuilder.sort().byField("registerDateTime").desc().createSort();
        @SuppressWarnings("unchecked")
		final List<OfferRequest> resultList = fullTextSession.createFullTextQuery(luceneQuery, OfferRequest.class).setSort(sort).getResultList();
        for (OfferRequest offerRequest : resultList) {
            Hibernate.initialize(offerRequest.getOfferRequestContent());
        }
        return resultList;
    }

    /**
     * Searching estimates by estimate description. Searching type wildcard.
     * @param description
     * @return estimate list sorted by estimate last saved time
     */
    @SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
    public List<Estimate> searchEstimateByDescription(String description) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Estimate.class)
                .get();
        Query luceneQuery = queryBuilder
                .keyword().wildcard()
                .onField("description").matching("*" + description + "*")
                .createQuery();
        final Sort sort = queryBuilder.sort().byField("lastSaved").desc().createSort();
        return fullTextSession.createFullTextQuery(luceneQuery, Estimate.class).setSort(sort).getResultList();
    }

    /**
     * Searching estimate items by item description.  Searching type wildcard.
     * @param description
     * @return estimate item list sorted by price
     */
    @SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.REQUIRED)
    public List<EstimateItem> searchEstimateItemByDescription(String description) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                .buildQueryBuilder()
                .forEntity(EstimateItem.class)
                .get();
        Query luceneQuery = queryBuilder
                .keyword().wildcard()
                .onField("description").matching("*" + description + "*")
                .createQuery();
        final Sort sort = queryBuilder.sort().byField("price").desc().createSort();
        return fullTextSession.createFullTextQuery(luceneQuery, EstimateItem.class).setSort(sort).getResultList();
    }
}
