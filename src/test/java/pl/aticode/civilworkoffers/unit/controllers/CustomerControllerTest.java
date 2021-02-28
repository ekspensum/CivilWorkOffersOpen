package pl.aticode.civilworkoffers.unit.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.dsl.EntityContext;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.QueryContextBuilder;
import org.hibernate.search.query.dsl.TermContext;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.TermTermination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.Scheduler;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pl.aticode.civilworkoffers.controller.CustomerController;
import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.ActivationService;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.CipherService;
import pl.aticode.civilworkoffers.service.HibernateSearchService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.ReCaptchaService;
import pl.aticode.civilworkoffers.service.RemoveCustomerQuartzJobService;
import pl.aticode.civilworkoffers.service.RemoveCustomerQuartzJobService.RemoveCustomerStatus;
import pl.aticode.civilworkoffers.service.SendEmailService;
import pl.aticode.civilworkoffers.service.UserService;

class CustomerControllerTest {

    private MockMvc mockMvc;
    private ActivationService activationService;
    private HibernateSearchService searchsService;

    @InjectMocks
    private CustomerController customerController;
    @Mock
    private UserService userService;
    @Mock
    private OfferService offerService;
    @Mock
    private CharFilterService charFilterService;
    @Mock
    private CipherService cipherService;
    @Mock
    private ReCaptchaService reCaptchaService;
    @Mock
    private RemoveCustomerQuartzJobService removeCustomerQuartzJobService;
    @Mock
    private Scheduler scheduler;
    @Mock
    private SendEmailService sendEmail;
    @Mock
    private MessageSource messageSource;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Session openSession;
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private FullTextSession fullTextSession;
    @Mock
    private MassIndexer massIndexer;
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

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.when(sessionFactory.openSession()).thenReturn(fullTextSession);
        Mockito.when(fullTextSession.createIndexer()).thenReturn(massIndexer);
        Mockito.when(massIndexer.batchSizeToLoadObjects(15)).thenReturn(massIndexer);
        Mockito.when(massIndexer.cacheMode(CacheMode.NORMAL)).thenReturn(massIndexer);
        Mockito.when(massIndexer.threadsToLoadObjects(3)).thenReturn(massIndexer);

        Mockito.when(sessionFactory.getCurrentSession()).thenReturn(fullTextSession);
        Mockito.when(fullTextSession.getSearchFactory()).thenReturn(searchFactory);
        Mockito.when(searchFactory.buildQueryBuilder()).thenReturn(queryContextBuilder);
        Mockito.when(queryContextBuilder.forEntity(Customer.class)).thenReturn(entityContext);
        Mockito.when(entityContext.get()).thenReturn(queryBuilder);

        searchsService = new HibernateSearchService(sessionFactory, userService);
        activationService = new ActivationService(searchsService, userRepository, cipherService);
        customerController = new CustomerController(userService, offerService, charFilterService, cipherService, activationService, reCaptchaService,
                removeCustomerQuartzJobService);
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    void testMainPage() throws Exception {
    	List<OfferRequest> allCustomerOfferRequests = new ArrayList<>();
    	allCustomerOfferRequests.add(new OfferRequest());
        Mockito.when(offerService.getAllCustomerOfferRequests()).thenReturn(allCustomerOfferRequests);
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/main"))
        		.andExpect(MockMvcResultMatchers.status().isOk())
        		.andExpect(MockMvcResultMatchers.view().name("customer/main"))
        		.andExpect(MockMvcResultMatchers.model().attribute("totalNoOfCustomerOfferRequests", allCustomerOfferRequests.size()));
    }

    @Test
    void testRegisterCustomer() throws Exception {
        List<CustomerType> allCustomerTypesExpect = new ArrayList<>();
        Mockito.when(userService.getAllCustomerTypes()).thenReturn(allCustomerTypesExpect);
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.get("/customer/registercustomer"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/registerCustomer"))
                .andReturn().getModelAndView().getModel();
        Customer customer = (Customer) model.get("customer");
        assertNotNull(customer);
        @SuppressWarnings("unchecked")
        List<CustomerType> allCustomerTypesActual = (List<CustomerType>) model.get("allCustomerTypes");
        assertNotNull(allCustomerTypesActual);
        String[] languagesActual = (String[]) model.get("languages");
        assertEquals("pl", languagesActual[0]);
    }

    @Test
    void testAddCustomer() throws Exception {
        String[] languages = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languages);
        String reCaptchaResponse = "g-recaptcha-response";
        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(true);
        User user = new User();
        user.setUsername("username");
        user.setPasswordField("passwordField");
        Customer customer = new Customer();
        customer.setFirstName("firstName");
        customer.setLastName("lastName");
        customer.setEmail("email@email.pl");
        customer.setLanguage("pl");
        customer.setRegon("435132621");
        customer.setCompanyName("companyName");
        CustomerType customerType = new CustomerType();
        customerType.setId(1L);
        customer.setCustomerType(customerType);
        customer.setUser(user);
        Mockito.when(userService.checkDistinctLoginWithRegisterUser(customer.getUser().getUsername())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/registercustomer")
                .param("g-recaptcha-response", reCaptchaResponse)
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/registerCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", languages));

        Mockito.when(userService.checkDistinctLoginWithRegisterUser(customer.getUser().getUsername())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/registercustomer")
                .param("g-recaptcha-response", reCaptchaResponse)
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/registerconfirm"));

        Mockito.doThrow(new Exception("TEST")).when(userService).addNewCustomer(customer, false);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/registercustomer")
                .param("g-recaptcha-response", reCaptchaResponse)
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/registerconfirm"));

        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/registercustomer")
                .param("g-recaptcha-response", reCaptchaResponse)
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/registerCustomer"));
    }

    @Test
    void testRegisterConfirm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/registerconfirm"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/registerConfirm"));
    }

    @Test
    void testGetActivationString() throws Exception {
        String activationString = "encodeActivationStringBase64";
        User user = new User();
        Customer customer = new Customer();
        customer.setFirstName("firstName");
        customer.setLastName("lastName");
        customer.setRegisterDateTime(LocalDateTime.now());
        customer.setUser(user);
        Mockito.when(queryBuilder.keyword()).thenReturn(termContext);
        Mockito.when(termContext.onField("activationString")).thenReturn(termMatchingContext);
        Mockito.when(termMatchingContext.matching(activationString)).thenReturn(termTermination);
        Mockito.when(termTermination.createQuery()).thenReturn(luceneQuery);

        Mockito.when(fullTextSession.createFullTextQuery(luceneQuery, Customer.class)).thenReturn(fullTextQuery);
        Mockito.when(fullTextQuery.uniqueResult()).thenReturn(customer);

        Mockito.when(searchsService.searchCustomerToActivation(activationString)).thenReturn(customer);
        Mockito.when(cipherService.decodeString(activationString)).thenReturn(activationString);

        mockMvc.perform(MockMvcRequestBuilders.get("/customer/activation")
                .param("activationString", activationString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/activation"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.success.activation"));

        customer.setRegisterDateTime(LocalDateTime.now().minusHours(7));
        Mockito.when(searchsService.searchCustomerToActivation(activationString)).thenReturn(customer);
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/activation")
                .param("activationString", activationString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/activation"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.defeat.activation"));
    }

    @Test
    void testSelfEditCustomerModel() throws Exception {
        List<CustomerType> allCustomerTypesExpect = new ArrayList<>();
        Mockito.when(userService.getAllCustomerTypes()).thenReturn(allCustomerTypesExpect);
        User user = new User();
        user.setId(13L);
        Customer customerExpect = new Customer();
        byte[] imageExpect = new byte[10];
        customerExpect.setPhoto(imageExpect);
        customerExpect.setUser(user);
        Mockito.when(userService.getLoggedCustomer()).thenReturn(customerExpect);
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.get("/customer/selfedit"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/selfEdit"))
                .andReturn().getModelAndView().getModel();

        Customer customerActual = (Customer) model.get("customer");
        assertEquals(13, customerActual.getUser().getId());
        @SuppressWarnings("unchecked")
        List<CustomerType> allCustomerTypesActual = (List<CustomerType>) model.get("allCustomerTypes");
        assertNotNull(allCustomerTypesActual);
        String[] languagesActual = (String[]) model.get("languages");
        assertEquals("pl", languagesActual[0]);
        byte[] imageActualt = (byte[]) model.get("image");
        assertEquals(10, imageActualt.length);
        Long editUserId = (Long) model.get("editUserId");
        assertEquals(13, editUserId);
    }

    @Test
    void testSelfEditCustomerCustomerBindingResultStringMultipartFileIntegerByteArrayModelRedirectAttributes() throws Exception {
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        String reCaptchaResponse = "g-recaptcha-response";
        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(true);
        User user = new User();
        user.setUsername("username007");
        user.setPasswordField("passwordField");
        Customer customerExpect = new Customer();
        customerExpect.setFirstName("firstName");
        customerExpect.setLastName("lastName");
        customerExpect.setEmail("email@email.pl");
        customerExpect.setLanguage("pl");
        customerExpect.setRegon("435132621");
        customerExpect.setCompanyName("companyName");
        CustomerType customerType = new CustomerType();
        customerType.setId(1L);
        customerExpect.setCustomerType(customerType);
        byte[] imageExpect = new byte[11];
        customerExpect.setPhoto(imageExpect);
        customerExpect.setUser(user);
        String selfEdit = "enableLoginData";
        Integer editUserIdExpect = 33;
        MockMultipartFile photo = new MockMultipartFile("newPhoto", "".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfedit")
                .file(photo)
                .param("selfEdit", selfEdit)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/selfeditenableloginapp"));

        selfEdit = null;
        Mockito.when(userService.checkDistinctLoginWithEditUser(customerExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(false);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfedit")
                .file(photo)
                .param("selfEdit", selfEdit)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/selfEdit"))
                .andReturn().getModelAndView().getModel();
        Customer customerActual = (Customer) model.get("customer");
        assertEquals(11, customerActual.getPhoto().length);
        assertEquals("username007", customerActual.getUser().getUsername());
        Integer editUserIdActual = (Integer) model.get("editUserId");
        assertEquals(33, editUserIdActual);

        User loggedUser = new User();
        loggedUser.setUsername("otherUsername");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        Mockito.when(userService.checkDistinctLoginWithEditUser(customerExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfedit")
                .file(photo)
                .param("selfEdit", selfEdit)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/logoutpage"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.success.edit.self"));

        loggedUser.setUsername("username007");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfedit")
                .file(photo)
                .param("selfEdit", selfEdit)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.success.edit.self"));
        
        Mockito.doThrow(new Exception("TEST")).when(userService).selfEditCustomer(customerActual);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfedit")
                .file(photo)
                .param("selfEdit", selfEdit)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.defeat.edit.self"));
    }

    @Test
    void testSelfEditCustomerWithLoginAppOptionModel() throws Exception {
        List<CustomerType> allCustomerTypesExpect = new ArrayList<>();
        Mockito.when(userService.getAllCustomerTypes()).thenReturn(allCustomerTypesExpect);
        User user = new User();
        user.setId(13L);
        Customer customerExpect = new Customer();
        byte[] imageExpect = new byte[10];
        customerExpect.setPhoto(imageExpect);
        customerExpect.setUser(user);
        Mockito.when(userService.getLoggedCustomer()).thenReturn(customerExpect);
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.get("/customer/selfeditenableloginapp"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/selfEditWithAppLoginOption"))
                .andReturn().getModelAndView().getModel();

        Customer customerActual = (Customer) model.get("customer");
        assertEquals(13, customerActual.getUser().getId());
        @SuppressWarnings("unchecked")
        List<CustomerType> allCustomerTypesActual = (List<CustomerType>) model.get("allCustomerTypes");
        assertNotNull(allCustomerTypesActual);
        String[] languagesActual = (String[]) model.get("languages");
        assertEquals("pl", languagesActual[0]);
        byte[] imageActualt = (byte[]) model.get("image");
        assertEquals(10, imageActualt.length);
        Long editUserId = (Long) model.get("editUserId");
        assertEquals(13, editUserId);
    }

    @Test
    void testSelfEditCustomerWithLoginAppOptionCustomerBindingResultStringMultipartFileIntegerByteArrayModelRedirectAttributes() throws Exception {
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        String reCaptchaResponse = "g-recaptcha-response";
        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(true);
        User user = new User();
        user.setUsername("username007");
        user.setPasswordField("passwordField");
        Customer customerExpect = new Customer();
        customerExpect.setFirstName("firstName");
        customerExpect.setLastName("lastName");
        customerExpect.setEmail("email@email.pl");
        customerExpect.setLanguage("pl");
        customerExpect.setRegon("435132621");
        customerExpect.setCompanyName("companyName");
        CustomerType customerType = new CustomerType();
        customerType.setId(1L);
        customerExpect.setCustomerType(customerType);
        byte[] imageExpect = new byte[11];
        customerExpect.setPhoto(imageExpect);
        customerExpect.setUser(user);
        Integer editUserIdExpect = 33;
        MockMultipartFile photo = new MockMultipartFile("newPhoto", "".getBytes());

        Mockito.when(userService.checkDistinctLoginWithEditUser(customerExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(false);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfeditenableloginapp")
                .file(photo)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/selfEditWithAppLoginOption"))
                .andReturn().getModelAndView().getModel();
        Customer customerActual = (Customer) model.get("customer");
        assertEquals(11, customerActual.getPhoto().length);
        assertEquals("username007", customerActual.getUser().getUsername());
        Integer editUserIdActual = (Integer) model.get("editUserId");
        assertEquals(33, editUserIdActual);

        User loggedUser = new User();
        loggedUser.setUsername("otherUsername");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        Mockito.when(userService.checkDistinctLoginWithEditUser(customerExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfeditenableloginapp")
                .file(photo)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/logoutpage"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.success.edit.self"));

        loggedUser.setUsername("username007");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        Mockito.when(userService.checkDistinctLoginWithEditUser(customerExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfeditenableloginapp")
                .file(photo)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.success.edit.self"));
        
        Mockito.doThrow(new Exception("TEST")).when(userService).selfEditCustomer(customerActual);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfeditenableloginapp")
                .file(photo)
                .sessionAttr("customer", customerExpect)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.defeat.edit.self"));
    }

    @Test
    void testRemoveCustomer() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/removecustomer"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/removeCustomer"));
    }

    @Test
    void testRemoveCustomerStringModel() throws Exception {
        String removeCustomerJob = "Remove";
        Customer customerToRemove = new Customer();
        Mockito.when(userService.getLoggedCustomer()).thenReturn(customerToRemove);
        Mockito.when(removeCustomerQuartzJobService.runRemoveCustomerJob(customerToRemove)).thenReturn(RemoveCustomerStatus.JOB_REMOVE_ADD_SUCCESS);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/removecustomer")
                .param("removeCustomerJob", removeCustomerJob))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/removeCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.remove.account.added"));

        Mockito.when(removeCustomerQuartzJobService.runRemoveCustomerJob(customerToRemove)).thenReturn(RemoveCustomerStatus.JOB_REMOVE_EXIST);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/removecustomer")
                .param("removeCustomerJob", removeCustomerJob))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/removeCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.remove.account.jobexist"));

        Mockito.when(removeCustomerQuartzJobService.runRemoveCustomerJob(customerToRemove)).thenReturn(RemoveCustomerStatus.JOB_REMOVE_ADD_DEFEAT);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/removecustomer")
                .param("removeCustomerJob", removeCustomerJob))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/removeCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.remove.account.defeat.add"));

        removeCustomerJob = "cancelRemove";

        Mockito.when(removeCustomerQuartzJobService.cancelRemoveCustomerJob(customerToRemove)).thenReturn(RemoveCustomerStatus.JOB_REMOVE_CANCEL_SUCCESS);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/removecustomer")
                .param("removeCustomerJob", removeCustomerJob))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/removeCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.remove.account.canceled"));

        Mockito.when(removeCustomerQuartzJobService.cancelRemoveCustomerJob(customerToRemove)).thenReturn(RemoveCustomerStatus.JOB_REMOVE_CANCEL_DEFEAT);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/removecustomer")
                .param("removeCustomerJob", removeCustomerJob))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/removeCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.remove.account.defeat.delete"));

        Mockito.when(removeCustomerQuartzJobService.cancelRemoveCustomerJob(customerToRemove)).thenReturn(RemoveCustomerStatus.JOB_REMOVE_TRIGGER_NOT_EXIST);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/removecustomer")
                .param("removeCustomerJob", removeCustomerJob))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/removeCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.remove.account.triggernotexist"));

        Mockito.when(removeCustomerQuartzJobService.cancelRemoveCustomerJob(customerToRemove)).thenReturn(RemoveCustomerStatus.JOB_REMOVE_CANCEL_DEFEAT_MAIL_FAILD);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/removecustomer")
                .param("removeCustomerJob", removeCustomerJob))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/removeCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.remove.account.canceled.mailfailed"));
    }

    @Test
    void testmultipleLogins() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/multiplelogins"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/multipleLogins"));
    }
}
