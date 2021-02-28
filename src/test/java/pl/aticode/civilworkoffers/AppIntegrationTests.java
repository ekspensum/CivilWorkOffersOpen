package pl.aticode.civilworkoffers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import pl.aticode.civilworkoffers.controller.CustomerController;
import pl.aticode.civilworkoffers.entity.estimate.Estimate;
import pl.aticode.civilworkoffers.entity.estimate.EstimateItem;
import pl.aticode.civilworkoffers.entity.offer.Offer;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestComment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestContent;
import pl.aticode.civilworkoffers.entity.offer.OfferStage;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.ActivationService;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.CipherService;
import pl.aticode.civilworkoffers.service.HibernateSearchService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.ReCaptchaService;
import pl.aticode.civilworkoffers.service.RemoveCustomerQuartzJobService;
import pl.aticode.civilworkoffers.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:applicationtest.properties")
@TestMethodOrder(OrderAnnotation.class)
class AppIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private CustomerController customerController;
    @Autowired
    private UserService userService;
    @Autowired
    private OfferService offerService;
    @Autowired
    private CharFilterService charFilterService;
    @Autowired
    private CipherService cipherService;
    @Autowired
    private ActivationService activationService;
    @Autowired
    private RemoveCustomerQuartzJobService removeCustomerQuartzJobService;
    @Autowired
    private HibernateSearchService hibernateSearchService;

    @Mock
    private ReCaptchaService reCaptchaService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(1)
    void testAddEditEmployee() throws Exception {
        User user1 = new User();
        user1.setUsername("user-employee1");
        user1.setPasswordField("passwordField");
        Employee employee1 = new Employee();
        employee1.setFirstName("firstName1");
        employee1.setLastName("lastName1");
        employee1.setEmail("email1@email.pl");
        employee1.setLanguage("pl");
        employee1.setUser(user1);
//        add first employee
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/addemployee")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").password("admin").roles("ADMIN"))
                .with(csrf())
                .sessionAttr("employee", employee1))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.add.employee"));
        
        User user2 = new User();
        user2.setUsername("user-employee2");
        user2.setPasswordField("passwordField");
        Employee employee2 = new Employee();
        employee2.setFirstName("firstName2");
        employee2.setLastName("lastName2");
        employee2.setEmail("email2@email.pl");
        employee2.setLanguage("pl");
        employee2.setUser(user2);
//        add second employee
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/addemployee")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").password("admin").roles("ADMIN"))
                .with(csrf())
                .sessionAttr("employee", employee2))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.add.employee"));

        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.get("/admin/editemployee")
                .with(SecurityMockMvcRequestPostProcessors.user("user-employee1").password("admin").roles("ADMIN"))
                .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editEmployee"))
                .andReturn().getModelAndView().getModel();

        List<Employee> allEmployees = (List<Employee>) model.get("allEmployeesNoLogged");
        Employee employeeToEdit = allEmployees.get(0);
        assertEquals("email2@email.pl", employeeToEdit.getEmail());

        user2.setEnabled(false);
        employeeToEdit.setUser(user2);
        String employeeId = "0";
        String editEmployeeButton = "toEdit";
        String resetPasswordButton = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editemployee")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").password("admin").roles("ADMIN"))
                .with(csrf())
                .param("employeeId", employeeId)
                .param("editEmployeeButton", editEmployeeButton)
                .param("resetPasswordButton", resetPasswordButton)
                .sessionAttr("employee", employeeToEdit))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.edit.employee"));
        Employee editingEmployee = userService.getEmployee("2");
        assertEquals(false, editingEmployee.getUser().isEnabled());

        byte[] image = "oldPhotoFile".getBytes();
        employeeToEdit.setPhoto(image);
        Long editUserIdExpect = employeeToEdit.getUser().getId();
        MockMultipartFile photo = new MockMultipartFile("newPhoto", "newPhotoFile".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/employee/selfedit")
                .file(photo)
                .with(SecurityMockMvcRequestPostProcessors.user("user-employee2").password("employee").roles("EMPLOYEE"))
                .with(csrf())
                .sessionAttr("employee", employeeToEdit)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "employee.success.edit.self"));
        editingEmployee = userService.getEmployee("2");
        assertEquals(image.length, editingEmployee.getPhoto().length);
    }

    @Test
    @Order(2)
    void testAddEditCustomer_PART_1() throws Exception {
        customerController = new CustomerController(userService, offerService, charFilterService, cipherService, activationService, reCaptchaService,
                removeCustomerQuartzJobService);
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
        User user = new User();
        user.setUsername("user-customer");
        user.setPasswordField("passwordField");
        Customer customer = new Customer();
        customer.setFirstName("firstName");
        customer.setLastName("lastName");
        customer.setEmail("email@email.pl");
        customer.setPhoto("photo".getBytes());
        customer.setLanguage("pl");
        customer.setRegon("435132621");
        customer.setCompanyName("companyName");
        CustomerType customerType = new CustomerType();
        customerType.setId(1L);
        customer.setCustomerType(customerType);
        customer.setUser(user);

        String reCaptchaResponse = "g-recaptcha-response";
        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/customer/registercustomer")
                .with(csrf())
                .param("g-recaptcha-response", reCaptchaResponse)
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/registerconfirm"));

        Customer customerToActivation = hibernateSearchService.searchCustomerNameRegonStreetPhoneByKeywordQuery(user.getUsername()).get(0);
        String encryptedActivationString = customerToActivation.getActivationString();
        String encodeToken = cipherService.encodeString(encryptedActivationString);
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/activation")
                .with(csrf())
                .param("activationString", encodeToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("customer/activation"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "customer.success.activation"));
    }

    @SuppressWarnings("unchecked")
    @Test
    @Order(3)
    void testAddEditCustomer_PART_2() throws Exception {
        String searchCustomerButton = "SEARCH";
        String editCustomerButton = null;
        String selectCustomerButton = null;
        String resetPasswordButton = null;
        String searchCustomerField = "user-customer";
        String selectedCustomerId = null;
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.post("/admin/editcustomer")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").password("admin").roles("ADMIN"))
                .with(csrf())
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("editCustomerButton", editCustomerButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editCustomer"))
                .andReturn().getModelAndView().getModel();
        List<Customer> foundCustomers = (List<Customer>) model.get("foundCustomers");
        Customer customerToEdit = foundCustomers.get(0);
        User user = customerToEdit.getUser();
        user.setEnabled(false);
        user.setPasswordField("passwordField");
        customerToEdit.setUser(user);

        searchCustomerButton = null;
        editCustomerButton = "toEdit";
        selectCustomerButton = null;
        resetPasswordButton = null;
        searchCustomerField = null;
        selectedCustomerId = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editcustomer")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").password("admin").roles("ADMIN"))
                .with(csrf())
                .sessionAttr("customer", customerToEdit)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("editCustomerButton", editCustomerButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        Long editUserIdExpect = customerToEdit.getUser().getId();
        MockMultipartFile photo = new MockMultipartFile("newPhoto", "".getBytes());
        byte[] imageExpect = new byte[15];
        customerToEdit.setPhoto(imageExpect);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/customer/selfedit")
                .file(photo)
                .with(SecurityMockMvcRequestPostProcessors.user("user-customer").password("passwordField").roles("CUSTOMER"))
                .with(csrf())
                .sessionAttr("customer", customerToEdit)
                .sessionAttr("image", imageExpect)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.success.edit.self"));
        Customer customerActual = userService.getCustomer("1");
        assertEquals(imageExpect.length, customerActual.getPhoto().length);
    }
    
    @Test
    @Order(4)
    void testAddOfferRequestOfferEstimate() throws Exception {
    	String saveOfferRequestButton = "";
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		
		String content = "Offer request content size > 10 characters";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		MultipartFile multipartFile1 = new MockMultipartFile("fileName1", "content1".getBytes());
		MultipartFile multipartFile2 = new MockMultipartFile("fileName2", "content22".getBytes());
		List<MultipartFile> attachments = new ArrayList<>();
		attachments.add(multipartFile1);
		attachments.add(multipartFile2);
		offerRequest.setAttachments(attachments);
		
		Customer customer = userService.getCustomer("1");
    	
        mockMvc.perform(MockMvcRequestBuilders.multipart("/offerrequest/employee/newrequest")
                .with(SecurityMockMvcRequestPostProcessors.user("user-employee1").password("passwordField").roles("EMPLOYEE"))
                .with(csrf())
        		.sessionAttr("offerRequest", offerRequest)
        		.sessionAttr("customer", customer)
        		.param("saveOfferRequestButton", saveOfferRequestButton))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.add.success"));
        OfferRequest offerRequestActual = offerService.getOfferRequest(1);
        assertEquals(content.length(), offerRequestActual.getOfferRequestContent().get(0).getContent().length());
        assertEquals("content22".getBytes().length, offerRequestActual.getOfferRequestAttachment().get(1).getFileSize());
        
		String requestComment = "requestComment";
		List<OfferRequestComment> offerRequestCommentList = new ArrayList<>();
		offerRequest.setOfferRequestComment(offerRequestCommentList);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/comment")
                .with(SecurityMockMvcRequestPostProcessors.user("user-customer").password("passwordField").roles("CUSTOMER"))
                .with(csrf())
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestComment", requestComment))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.comment.success"));
        offerRequestActual = offerService.getOfferRequest(1);
        assertEquals(requestComment.length(), offerRequestActual.getOfferRequestComment().get(0).getComment().length());
        
        Offer offer = new Offer();
        offer.setContent("offerContent");
        List<Offer> initializeOffer = offerService.getInitializeOffer(offerRequest);
        offer.setOfferStage(OfferStage.PREPARE);
        initializeOffer.add(offer);
        offerRequest.setOffer(initializeOffer);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/new")
                .with(SecurityMockMvcRequestPostProcessors.user("user-employee1").password("passwordField").roles("EMPLOYEE"))
                .with(csrf())
        		.sessionAttr("offerRequest", offerRequest)
        		.requestAttr("offerIdx", 0))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/newOffer"))
		        .andExpect(MockMvcResultMatchers.model().attribute("message", "offer.save.success"));
        offerRequestActual = offerService.getOfferRequest(1);
        assertEquals(OfferStage.PREPARE, offerRequestActual.getOffer().get(0).getOfferStage());
   
		String typeOfCivilWorkInput = "type";
		Integer selectBox = 0;
		Integer estimateIdx = 0;
		String selectOption = "addItem";
		Long itemIdBox = 0L;
		String estimateItemDescription = "estimateItemDescription";
		Estimate estimate = new Estimate();
		estimate.setDescription("estimateDescription");
		estimate.setEstimateItem(new ArrayList<EstimateItem>());
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		offerRequest.setEstimate(estimateList);
		
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
                .with(SecurityMockMvcRequestPostProcessors.user("user-employee1").password("passwordField").roles("EMPLOYEE"))
                .with(csrf())
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
        offerRequestActual = offerService.getOfferRequest(1);
        assertEquals("estimateDescription", offerRequestActual.getEstimate().get(0).getDescription());
        assertEquals(LocalDate.now(), offerRequestActual.getEstimate().get(0).getEstimateItem().get(0).getLastSaved());
        assertEquals(1, offerService.getOfferRequestsByDate(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)).size());
    }
    
    @Test
    @Order(5)
    void testRemoveCustomer() throws Exception {
        String searchCustomerButton = "SEARCH";
        String searchCustomerField = "user-customer";
        String selectCustomerButton = null;
        String selectedCustomerId = null;
        String removeCustomerButton = null;
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.post("/admin/removecustomer")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").password("admin").roles("ADMIN"))
                .with(csrf())
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("removeCustomerButton", removeCustomerButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/removeCustomer"))
                .andReturn().getModelAndView().getModel();
        @SuppressWarnings("unchecked")
		List<Customer> foundCustomers = (List<Customer>) model.get("foundCustomers");
        Customer customerToRemove = foundCustomers.get(0);
        assertEquals(15, customerToRemove.getPhoto().length);

        searchCustomerButton = null;
        searchCustomerField = null;
        removeCustomerButton = "toRemove";
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/removecustomer")
                .with(SecurityMockMvcRequestPostProcessors.user("admin").password("admin").roles("ADMIN"))
                .with(csrf())
                .sessionAttr("customer", customerToRemove)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("removeCustomerButton", removeCustomerButton))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.remove.customer"));
        
        assertEquals(0, offerService.getOfferRequestsByDate(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)).size());
        assertEquals(1, offerService.getEstimate(1).getId());
    }
}
