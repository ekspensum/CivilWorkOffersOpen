package pl.aticode.civilworkoffers.unit.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import pl.aticode.civilworkoffers.controller.OfferRequestEmployeeController;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestComment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestContent;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.HibernateSearchService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.UserService;

class OfferRequestEmployeeControllerTest {
	
	private MockMvc mockMvc;
	
	@InjectMocks
	private OfferRequestEmployeeController offerRequestEmployeeController;
    @Mock
    private CharFilterService charFilterService;
    @Mock
    private OfferService offerService;
    @Mock
    private HibernateSearchService searchService;
    @Mock
    private UserService userService;

	@BeforeEach
	void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestEmployeeController).build();
	}

	@Test
	void testNewOfferRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/employee/newrequest"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/newRequest"));
	}

	@Test
	void testNewOfferRequestOfferRequestBindingResultStringStringStringStringStringCustomerModelRedirectAttributes() throws Exception {
		String searchCustomerButton = "SEARCH";
		String searchCustomerField = "abc";
		String selectCustomerButton = "SELECT";
		String selectedCustomerId = "1";
		String saveOfferRequestButton = "";
				
		offerRequestEmployeeController = new OfferRequestEmployeeController(10, 512, userService, charFilterService, offerService, searchService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestEmployeeController).build();
        
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		
		String content = "Offer request content size > 10 characters";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		MultipartFile multipartFile1 = new MockMultipartFile("fileName1", "content1".getBytes());
		MultipartFile multipartFile2 = new MockMultipartFile("fileName2", "content2".getBytes());
		List<MultipartFile> attachments = new ArrayList<>();
		attachments.add(multipartFile1);
		attachments.add(multipartFile2);
		offerRequest.setAttachments(attachments);
		
		Customer customer = new Customer();
		
		List<Customer> foundCustomers = new ArrayList<>(); 
		Mockito.when(searchService.searchCustomerEnabledNameRegonStreetPhoneByKeywordQuery(searchCustomerField)).thenReturn(foundCustomers);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/newrequest")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("searchCustomerButton", searchCustomerButton)
        		.param("searchCustomerField", searchCustomerField))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/newRequest"))
		        .andExpect(MockMvcResultMatchers.model().attribute("foundCustomers", foundCustomers));
		
        Mockito.when(userService.getCustomer(selectedCustomerId)).thenReturn(customer);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/newrequest")
        		.param("selectCustomerButton", selectCustomerButton)
        		.param("selectedCustomerId", selectedCustomerId))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/newRequest"))
		        .andExpect(MockMvcResultMatchers.model().attribute("customer", customer))
		        .andExpect(MockMvcResultMatchers.model().attributeExists("offerRequest"));
		
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/newrequest")
        		.sessionAttr("offerRequest", offerRequest)
        		.sessionAttr("customer", customer)
        		.param("saveOfferRequestButton", saveOfferRequestButton))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.add.success"));
        
        Mockito.doThrow(new IOException("TEST")).when(offerService).addOfferRequestByEmployee(offerRequest, customer);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/newrequest")
        		.sessionAttr("offerRequest", offerRequest)
        		.sessionAttr("customer", customer)
        		.param("saveOfferRequestButton", saveOfferRequestButton))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.add.defeat"));
        
        offerRequestEmployeeController = new OfferRequestEmployeeController(1, 512, userService, charFilterService, offerService, searchService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestEmployeeController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/newrequest")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/newRequest"));        
        
        offerRequestEmployeeController = new OfferRequestEmployeeController(10, 5, userService, charFilterService, offerService, searchService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestEmployeeController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/newrequest")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/newRequest"));
        
        offerRequest.setExecutionDate(LocalDate.now().minusDays(2));
        offerRequestEmployeeController = new OfferRequestEmployeeController(10, 512, userService, charFilterService, offerService, searchService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestEmployeeController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/newrequest")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/newRequest"));
	}

	@Test
	void testSearchOfferRequestModel() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/employee/search"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/search"))
		        .andExpect(MockMvcResultMatchers.model().attribute("execDateTo", LocalDate.now().plusDays(14)));
	}

	@Test
	void testSearchOfferRequestStringStringStringStringStringStringModel() throws Exception {
		String registerDateFrom = "2020-01-01";
		String registerDateTo = "2020-01-01";
		String execDateFrom = "2020-01-01";
		String execDateTo = "2020-01-01";
		String text = "text";
		String search = "search";
		LocalDateTime registerDateTimeFrom = LocalDateTime.parse(registerDateFrom + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime registerDateTimeTo = LocalDateTime.parse(registerDateTo + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).plusDays(1);
		List<OfferRequest> offerRequestsList = new ArrayList<>(); 
		Mockito.when(searchService.searchOfferRequestForEmployee(registerDateTimeFrom, registerDateTimeTo, 
                LocalDate.parse(execDateFrom), LocalDate.parse(execDateTo), text)).thenReturn(offerRequestsList);
		
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/search")
        		.param("execDateTo", execDateTo)
        		.param("text", text))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/search"))
		        .andExpect(MockMvcResultMatchers.model().attribute("execDateTo", "2020-01-01"))
		        .andExpect(MockMvcResultMatchers.model().attribute("text", "text"));
        
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/search")
        		.param("registerDateFrom", registerDateFrom)
        		.param("registerDateTo", registerDateTo)
        		.param("search", search)
        		.param("execDateFrom", execDateFrom)
        		.param("execDateTo", execDateTo)
        		.param("text", text))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/search"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerRequestsList", offerRequestsList));
	}

	@Test
	void testMyRequestDetailsOfferRequestModel() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(77L);
		OfferRequest offerRequestAllInitialized = new OfferRequest(); 
		Mockito.when(offerService.getOfferRequest(offerRequest.getId())).thenReturn(offerRequestAllInitialized);
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/employee/details")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/details"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerRequest", offerRequestAllInitialized));
	}

	@Test
	void testMyRequestDetailsLongModel() throws Exception {
		Long offerRequestId = 88L;
		OfferRequest offerRequest = new OfferRequest();
		Mockito.when(offerService.getOfferRequest(offerRequestId)).thenReturn(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/details")
        		.param("offerRequestId", offerRequestId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/offerrequest/employee/details"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerRequest", offerRequest));
	}

	@Test
	void testEditOfferRequestContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/employee/edit/content"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/edit/content"));
	}

	@Test
	void testEditOfferRequestContentOfferRequestStringModelRedirectAttributes() throws Exception {
		String requestContent = "requestContent";
		OfferRequest offerRequest = new OfferRequest();
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		ArgumentCaptor<OfferRequest> offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerService).editOfferRequestContent(offerRequestCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/content")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestContent", requestContent))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.content.success"));
        assertEquals("requestContent", offerRequestCaptor.getValue().getOfferRequestContent().get(0).getContent());
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).editOfferRequestContent(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/content")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestContent", requestContent))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.content.defeat"));
        
        requestContent = "content"; //less than 10 char.
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/content")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestContent", requestContent))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/edit/content"))
		        .andExpect(MockMvcResultMatchers.model().attribute("requestContentError", "offer.request.edit.content.size"));
	}

	@Test
	void testEditOfferRequestFile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/employee/edit/file"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/edit/file"));
	}

	@Test
	void testEditOfferRequestFileOfferRequestBindingResultModelRedirectAttributes() throws Exception {
        offerRequestEmployeeController = new OfferRequestEmployeeController(10, 512, userService, charFilterService, offerService, searchService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestEmployeeController).build();
        
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		
		OfferRequestAttachment offerRequestAttachment1 = new OfferRequestAttachment();
		offerRequestAttachment1.setFileSize(10);
		OfferRequestAttachment offerRequestAttachment2 = new OfferRequestAttachment();
		offerRequestAttachment2.setFileSize(10);
		OfferRequestAttachment offerRequestAttachment3 = new OfferRequestAttachment();
		offerRequestAttachment3.setFileSize(10);
		List<OfferRequestAttachment> offerRequestAttachmentList = new ArrayList<>();
		offerRequestAttachmentList.add(offerRequestAttachment1);
		offerRequestAttachmentList.add(offerRequestAttachment2);
		offerRequestAttachmentList.add(offerRequestAttachment3);
		offerRequest.setOfferRequestAttachment(offerRequestAttachmentList);
		
		String content = "Offer request content size > 10 characters";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		MultipartFile multipartFile1 = new MockMultipartFile("fileName1", "content1".getBytes());
		MultipartFile multipartFile2 = new MockMultipartFile("fileName2", "content2".getBytes());
		List<MultipartFile> attachments = new ArrayList<>();
		attachments.add(multipartFile1);
		attachments.add(multipartFile2);
		offerRequest.setAttachments(attachments);
		
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.file.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).editOfferRequestFile(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.file.defeat"));
        
        offerRequestEmployeeController = new OfferRequestEmployeeController(1, 512, userService, charFilterService, offerService, searchService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestEmployeeController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/edit/file"));        
        
        offerRequestEmployeeController = new OfferRequestEmployeeController(10, 5, userService, charFilterService, offerService, searchService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestEmployeeController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/edit/file"));
	}

	@Test
	void testCommentOfferRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/employee/edit/comment"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/edit/comment"));
	}

	@Test
	void testCommentOfferRequestOfferRequestStringModelRedirectAttributes() throws Exception {
		String requestComment = "requestComment";
		OfferRequest offerRequest = new OfferRequest();
		List<OfferRequestComment> offerRequestCommentList = new ArrayList<>();
		offerRequest.setOfferRequestComment(offerRequestCommentList);
		
		ArgumentCaptor<OfferRequest> offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerService).editOfferRequestComment(offerRequestCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/comment")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestComment", requestComment))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.comment.success"));
        assertEquals("requestComment", offerRequestCaptor.getValue().getOfferRequestComment().get(0).getComment());
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).editOfferRequestComment(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/comment")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestComment", requestComment))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.comment.defeat"));
        
        requestComment = "comment"; //less than 10 char.
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/edit/comment")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestComment", requestComment))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/edit/comment"))
		        .andExpect(MockMvcResultMatchers.model().attribute("requestCommentError", "offer.request.edit.comment.size"));
	}

	@Test
	void testAddCustomerModel() throws Exception {
		String[] languages = {"PL", "EN"}; 
		Mockito.when(userService.getLanguages()).thenReturn(languages);
		List<CustomerType> allCustomerTypes = new ArrayList<>(); 
		Mockito.when(userService.getAllCustomerTypes()).thenReturn(allCustomerTypes);
		
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/employee/addcustomer"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/addCustomer"))
		        .andExpect(MockMvcResultMatchers.model().attributeExists("customer"))
		        .andExpect(MockMvcResultMatchers.model().attribute("languages", languages))
		        .andExpect(MockMvcResultMatchers.model().attribute("allCustomerTypes", allCustomerTypes));
	}

	@Test
	void testAddCustomerCustomerBindingResultModelRedirectAttributes() throws Exception {
        String[] languages = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languages);
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
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/addcustomer")
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("offerrequest/employee/addCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", languages));

        Mockito.when(userService.checkDistinctLoginWithRegisterUser(customer.getUser().getUsername())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/addcustomer")
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "employee.success.add.customer"));

        Mockito.doThrow(new Exception()).when(userService).addNewCustomer(customer, true);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/employee/addcustomer")
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "employee.defeat.add.customer"));
	}

}
