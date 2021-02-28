package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.EntityContext;
import org.hibernate.search.query.dsl.FuzzyContext;
import org.hibernate.search.query.dsl.MustJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.QueryContextBuilder;
import org.hibernate.search.query.dsl.RangeContext;
import org.hibernate.search.query.dsl.RangeMatchingContext;
import org.hibernate.search.query.dsl.RangeMatchingContext.FromRangeContext;
import org.hibernate.search.query.dsl.RangeTerminationExcludable;
import org.hibernate.search.query.dsl.TermContext;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.TermTermination;
import org.hibernate.search.query.dsl.WildcardContext;
import org.hibernate.search.query.dsl.sort.SortContext;
import org.hibernate.search.query.dsl.sort.SortFieldContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import pl.aticode.civilworkoffers.entity.estimate.Estimate;
import pl.aticode.civilworkoffers.entity.estimate.EstimateItem;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.service.HibernateSearchService;
import pl.aticode.civilworkoffers.service.UserService;

class HibernateSearchServiceTest {
	
	private HibernateSearchService hibernateSearchService;
	@Mock
	private UserService userService;
	@Mock
	private Session openSession;
	@Mock
	private SessionFactory sessionFactory;
	@Mock
	private FullTextSession fullTextSession;
	@Mock
	private MassIndexer massIndexer;
	@Mock
	private FuzzyContext fuzzyContext;
	@Mock
	private SearchFactory searchFactory;
	@Mock
	private QueryContextBuilder queryContextBuilder;
	@Mock
	private EntityContext entityContext;
	@Mock
	private QueryBuilder queryBuilder;
	@Mock
	private TermContext termContext;
	@Mock
	private TermMatchingContext termMatchingContext;
	@Mock
	private TermTermination termTermination;
	@Mock
	private Query luceneQuery;
	@Mock
	private FullTextQuery fullTextQuery;
	@SuppressWarnings("rawtypes")
	@Mock
	private BooleanJunction<BooleanJunction> booleanJunction;
	@Mock
	private MustJunction mustJunction;
	@Mock
	private RangeContext rangeContext;
	@Mock
	private RangeMatchingContext rangeMatchingContext;
	@Mock
	private FromRangeContext<LocalDateTime> fromRangeLocalDateTime;
	@Mock
	private FromRangeContext<LocalDate> fromRangeLocalDate;
	@Mock
	private RangeTerminationExcludable rangeTerminationExcludable;
	@Mock
	private WildcardContext wildcardContext;
	@Mock
	private SortContext sortContext;
	@Mock
	private SortFieldContext sortFieldContext;
	@Mock
	private Sort sort;
	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(sessionFactory.openSession()).thenReturn(fullTextSession);
		Mockito.when(fullTextSession.createIndexer()).thenReturn(massIndexer);
		Mockito.when(massIndexer.batchSizeToLoadObjects(15)).thenReturn(massIndexer);
		Mockito.when(massIndexer.cacheMode(CacheMode.NORMAL)).thenReturn(massIndexer);
		Mockito.when(massIndexer.threadsToLoadObjects(3)).thenReturn(massIndexer);
		hibernateSearchService = new HibernateSearchService(sessionFactory, userService);
	}

	@Test
	void testSearchCustomerNameRegonStreetPhoneByKeywordQuery() {
		Mockito.when(sessionFactory.getCurrentSession()).thenReturn(fullTextSession);
		Mockito.when(fullTextSession.getSearchFactory()).thenReturn(searchFactory);
		Mockito.when(searchFactory.buildQueryBuilder()).thenReturn(queryContextBuilder);
		Mockito.when(queryContextBuilder.forEntity(Customer.class)).thenReturn(entityContext);
		Mockito.when(entityContext.get()).thenReturn(queryBuilder);
		
		Mockito.when(queryBuilder.keyword()).thenReturn(termContext);
		Mockito.when(termContext.fuzzy()).thenReturn(fuzzyContext);
		Mockito.when(fuzzyContext.withEditDistanceUpTo(2)).thenReturn(fuzzyContext);
		Mockito.when(fuzzyContext.withPrefixLength(0)).thenReturn(fuzzyContext);
		Mockito.when(fuzzyContext.onFields("user.username", "lastName", "companyName", "regon", "street", "phone", "email")).thenReturn(termMatchingContext);
		String text = "searchingText";
		Mockito.when(termMatchingContext.matching(text)).thenReturn(termTermination);
		Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);
		
		Mockito.when(fullTextSession.createFullTextQuery(luceneQuery, Customer.class)).thenReturn(fullTextQuery);
		List<Customer> customers = new ArrayList<>();
		Mockito.when(fullTextQuery.getResultList()).thenReturn(customers);
		
		assertNotNull(hibernateSearchService.searchCustomerNameRegonStreetPhoneByKeywordQuery(text));
	}

	@Test
	void testSearchCustomerEnabledNameRegonStreetPhoneByKeywordQuery() {
		Mockito.when(sessionFactory.getCurrentSession()).thenReturn(fullTextSession);
		Mockito.when(fullTextSession.getSearchFactory()).thenReturn(searchFactory);
		Mockito.when(searchFactory.buildQueryBuilder()).thenReturn(queryContextBuilder);
		Mockito.when(queryContextBuilder.forEntity(Customer.class)).thenReturn(entityContext);
		Mockito.when(entityContext.get()).thenReturn(queryBuilder);
		
		Mockito.when(queryBuilder.bool()).thenReturn(booleanJunction);
		Mockito.when(queryBuilder.keyword()).thenReturn(termContext);

		Mockito.when(termContext.fuzzy()).thenReturn(fuzzyContext);
		Mockito.when(fuzzyContext.withEditDistanceUpTo(2)).thenReturn(fuzzyContext);
		Mockito.when(fuzzyContext.withPrefixLength(0)).thenReturn(fuzzyContext);
		Mockito.when(fuzzyContext.onFields("user.username", "lastName", "companyName", "regon", "street", "phone", "email")).thenReturn(termMatchingContext);
		String text = "searchingText";
		Mockito.when(termMatchingContext.matching(text)).thenReturn(termTermination);
		Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);
		Mockito.when(queryBuilder.bool().must(luceneQuery)).thenReturn(mustJunction);
		
		Mockito.when(termContext.onField("user.enabled")).thenReturn(termMatchingContext);
		Mockito.when(termMatchingContext.matching(Boolean.TRUE)).thenReturn(termTermination);
		Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);
		Mockito.when(queryBuilder.bool().must(luceneQuery).must(luceneQuery)).thenReturn(mustJunction);
		
		Mockito.when(queryBuilder.bool().must(luceneQuery).must(luceneQuery).createQuery()).thenReturn(luceneQuery);
		
		Mockito.when(fullTextSession.createFullTextQuery(luceneQuery, Customer.class)).thenReturn(fullTextQuery);
		List<Customer> customers = new ArrayList<>();
		Mockito.when(fullTextQuery.getResultList()).thenReturn(customers);
		
		assertNotNull(hibernateSearchService.searchCustomerEnabledNameRegonStreetPhoneByKeywordQuery(text));
	}
	
	@Test
	void testSearchCustomerToActivation() {
		String activationString = "encodeActivationStringBase64";
		
		Mockito.when(sessionFactory.getCurrentSession()).thenReturn(fullTextSession);
		Mockito.when(fullTextSession.getSearchFactory()).thenReturn(searchFactory);
		Mockito.when(searchFactory.buildQueryBuilder()).thenReturn(queryContextBuilder);
		Mockito.when(queryContextBuilder.forEntity(Customer.class)).thenReturn(entityContext);
		Mockito.when(entityContext.get()).thenReturn(queryBuilder);
		
		Mockito.when(queryBuilder.keyword()).thenReturn(termContext);
		Mockito.when(termContext.onField("activationString")).thenReturn(termMatchingContext);
		Mockito.when(termMatchingContext.matching(activationString)).thenReturn(termTermination);
		Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);
		
		Mockito.when(fullTextSession.createFullTextQuery(luceneQuery, Customer.class)).thenReturn(fullTextQuery);
		Customer customer = new Customer();
		Mockito.when(fullTextQuery.uniqueResult()).thenReturn(customer);
		
		assertNotNull(hibernateSearchService.searchCustomerToActivation(activationString));
	}

	@Test
	void testSearchOfferRequestForEmployee() {
		LocalDateTime dateTimeNow = LocalDateTime.now();
		LocalDate dateNow = LocalDate.now();
		Employee employee = new Employee();
		employee.setId(13L);
		Mockito.when(userService.getLoggedEmployee()).thenReturn(employee);
		
		Mockito.when(sessionFactory.getCurrentSession()).thenReturn(fullTextSession);
		Mockito.when(fullTextSession.getSearchFactory()).thenReturn(searchFactory);
		Mockito.when(searchFactory.buildQueryBuilder()).thenReturn(queryContextBuilder);
		Mockito.when(queryContextBuilder.forEntity(OfferRequest.class)).thenReturn(entityContext);
		Mockito.when(entityContext.get()).thenReturn(queryBuilder);
		
		Mockito.when(queryBuilder.bool()).thenReturn(booleanJunction);

		Mockito.when(queryBuilder.keyword()).thenReturn(termContext);
		Mockito.when(termContext.onField("employee.id")).thenReturn(termMatchingContext);
		Mockito.when(termMatchingContext.matching(employee.getId())).thenReturn(termTermination);
		Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);
		Mockito.when(booleanJunction.must(luceneQuery)).thenReturn(mustJunction);
		
		Mockito.when(queryBuilder.range()).thenReturn(rangeContext);
		Mockito.when(rangeContext.onField("registerDateTime")).thenReturn(rangeMatchingContext);
		Mockito.when(rangeMatchingContext.from(dateTimeNow)).thenReturn(fromRangeLocalDateTime);
		Mockito.when(fromRangeLocalDateTime.to(dateTimeNow)).thenReturn(rangeTerminationExcludable);
		Mockito.when(rangeTerminationExcludable.createQuery()).thenReturn(luceneQuery);
		Mockito.when(booleanJunction.must(luceneQuery).must(luceneQuery)).thenReturn(mustJunction);
		
		Mockito.when(rangeContext.onField("executionDate")).thenReturn(rangeMatchingContext);
		Mockito.when(rangeMatchingContext.from(dateNow)).thenReturn(fromRangeLocalDate);
		Mockito.when(fromRangeLocalDate.to(dateNow)).thenReturn(rangeTerminationExcludable);
		Mockito.when(rangeTerminationExcludable.createQuery()).thenReturn(luceneQuery);
		Mockito.when(booleanJunction.must(luceneQuery).must(luceneQuery).must(luceneQuery)).thenReturn(mustJunction);
		
		Mockito.when(termContext.wildcard()).thenReturn(wildcardContext);
		Mockito.when(wildcardContext.onFields("customer.lastName", "customer.companyName", "offerRequestContent.content", "customer.phone", "customer.email")).thenReturn(termMatchingContext);
		String text = "searchingText";
		Mockito.when(termMatchingContext.matching("*"+text+"*")).thenReturn(termTermination);
		Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);
		Mockito.when(booleanJunction.must(luceneQuery).must(luceneQuery).must(luceneQuery).must(luceneQuery)).thenReturn(mustJunction);
		
		Mockito.when(mustJunction.createQuery()).thenReturn(luceneQuery);
		
		Mockito.when(queryBuilder.sort()).thenReturn(sortContext);
		Mockito.when(sortContext.byField("registerDateTime")).thenReturn(sortFieldContext);
		Mockito.when(sortFieldContext.desc()).thenReturn(sortFieldContext);
		Mockito.when(sortFieldContext.createSort()).thenReturn(sort);
		
		Mockito.when(fullTextSession.createFullTextQuery(luceneQuery, OfferRequest.class)).thenReturn(fullTextQuery);
		Mockito.when(fullTextQuery.setSort(sort)).thenReturn(fullTextQuery);
		List<OfferRequest> offerRequests = new ArrayList<>();
		Mockito.when(fullTextQuery.getResultList()).thenReturn(offerRequests);
		
		assertNotNull(hibernateSearchService.searchOfferRequestForEmployee(dateTimeNow, dateTimeNow, dateNow, dateNow, text));
	}
	
	@Test
	void testSearchEstimateByDescription() {
		Mockito.when(sessionFactory.getCurrentSession()).thenReturn(fullTextSession);
		Mockito.when(fullTextSession.getSearchFactory()).thenReturn(searchFactory);
		Mockito.when(searchFactory.buildQueryBuilder()).thenReturn(queryContextBuilder);
		Mockito.when(queryContextBuilder.forEntity(Estimate.class)).thenReturn(entityContext);
		Mockito.when(entityContext.get()).thenReturn(queryBuilder);
		
		Mockito.when(queryBuilder.keyword()).thenReturn(termContext);
		Mockito.when(termContext.wildcard()).thenReturn(wildcardContext);
		Mockito.when(wildcardContext.onField("description")).thenReturn(termMatchingContext);
		String description = "searchingText";
		Mockito.when(termMatchingContext.matching("*"+description+"*")).thenReturn(termTermination);
		Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);

		Mockito.when(queryBuilder.sort()).thenReturn(sortContext);
		Mockito.when(sortContext.byField("lastSaved")).thenReturn(sortFieldContext);
		Mockito.when(sortFieldContext.desc()).thenReturn(sortFieldContext);
		Mockito.when(sortFieldContext.createSort()).thenReturn(sort);
		
		Mockito.when(fullTextSession.createFullTextQuery(luceneQuery, Estimate.class)).thenReturn(fullTextQuery);
		Mockito.when(fullTextQuery.setSort(sort)).thenReturn(fullTextQuery);
		List<Estimate> estimateList = new ArrayList<>();
		Mockito.when(fullTextQuery.getResultList()).thenReturn(estimateList);
		
		assertNotNull(hibernateSearchService.searchEstimateByDescription(description));
	}
	
	@Test
	void searchEstimateItemByDescription() {
		Mockito.when(sessionFactory.getCurrentSession()).thenReturn(fullTextSession);
		Mockito.when(fullTextSession.getSearchFactory()).thenReturn(searchFactory);
		Mockito.when(searchFactory.buildQueryBuilder()).thenReturn(queryContextBuilder);
		Mockito.when(queryContextBuilder.forEntity(EstimateItem.class)).thenReturn(entityContext);
		Mockito.when(entityContext.get()).thenReturn(queryBuilder);
		
		Mockito.when(queryBuilder.keyword()).thenReturn(termContext);
		Mockito.when(termContext.wildcard()).thenReturn(wildcardContext);
		Mockito.when(wildcardContext.onField("description")).thenReturn(termMatchingContext);
		String description = "searchingText";
		Mockito.when(termMatchingContext.matching("*"+description+"*")).thenReturn(termTermination);
		Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);

		Mockito.when(queryBuilder.sort()).thenReturn(sortContext);
		Mockito.when(sortContext.byField("price")).thenReturn(sortFieldContext);
		Mockito.when(sortFieldContext.desc()).thenReturn(sortFieldContext);
		Mockito.when(sortFieldContext.createSort()).thenReturn(sort);
		
		Mockito.when(fullTextSession.createFullTextQuery(luceneQuery, EstimateItem.class)).thenReturn(fullTextQuery);
		Mockito.when(fullTextQuery.setSort(sort)).thenReturn(fullTextQuery);
		List<EstimateItem> estimateList = new ArrayList<>();
		Mockito.when(fullTextQuery.getResultList()).thenReturn(estimateList);
		
		assertNotNull(hibernateSearchService.searchEstimateItemByDescription(description));
	}
}
