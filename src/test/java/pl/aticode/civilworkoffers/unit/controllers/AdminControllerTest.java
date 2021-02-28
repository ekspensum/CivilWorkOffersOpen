package pl.aticode.civilworkoffers.unit.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.springframework.validation.BindingResult;

import pl.aticode.civilworkoffers.controller.AdminController;
import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;
import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.HibernateSearchService;
import pl.aticode.civilworkoffers.service.HomeService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.PasswordService;
import pl.aticode.civilworkoffers.service.UserService;

class AdminControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AdminController adminController;
    @Mock
    private UserService userService;
    @Mock
    private OfferService offerService;
    @Mock
    private CharFilterService charFilterService;
    @Mock
    private HibernateSearchService hibernateSearchService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private HomeService homeService;
    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    void testMain() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/main"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/main"))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfEmployees", 0))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfCustomers", 0));
    }

    @Test
    void testAddEmployeeModel() throws Exception {
        String[] languages = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languages);
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/addemployee"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/addEmployee"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("employee"))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", languages));
    }

    @Test
    void testAddEmployeeEmployeeBindingResultModelRedirectAttributes() throws Exception {
        String[] languages = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languages);
        User user = new User();
        user.setUsername("username");
        user.setPasswordField("passwordField");
        Employee employee = new Employee();
        employee.setFirstName("firstName");
        employee.setLastName("lastName");
        employee.setEmail("email@email.pl");
        employee.setLanguage("pl");
        employee.setUser(user);
        Mockito.when(userService.checkDistinctLoginWithRegisterUser(employee.getUser().getUsername())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/addemployee")
                .sessionAttr("employee", employee))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/addEmployee"))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", languages));

        Mockito.when(userService.checkDistinctLoginWithRegisterUser(employee.getUser().getUsername())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/addemployee")
                .sessionAttr("employee", employee))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.add.employee"));

        Mockito.doThrow(new Exception("TEST")).when(userService).addNewEmployee(employee);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/addemployee")
                .sessionAttr("employee", employee))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.defeat.add.employee"));
    }

    @Test
    void testEditEmployeeModel() throws Exception {
        List<Employee> allEmployees = new ArrayList<>();
        Mockito.when(userService.getAllEmployees()).thenReturn(allEmployees);
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/editemployee"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editEmployee"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("allEmployeesNoLogged"));
    }

    @Test
    void testEditEmployeeEmployeeStringStringStringModelRedirectAttributes() throws Exception {
        String employeeId = "3";
        String editEmployeeButton = "toEdit";
        String resetPasswordButton = "resetPass";
        Employee employee = new Employee();
        Mockito.when(userService.getEmployee(employeeId)).thenReturn(employee);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editemployee")
                .param("employeeId", employeeId)
                .param("editEmployeeButton", editEmployeeButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editEmployee"))
                .andExpect(MockMvcResultMatchers.model().attribute("employee", employee));

        employeeId = "0";
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editemployee")
                .param("employeeId", employeeId)
                .param("editEmployeeButton", editEmployeeButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        editEmployeeButton = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editemployee")
                .param("employeeId", employeeId)
                .param("editEmployeeButton", editEmployeeButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editEmployee"))
                .andExpect(MockMvcResultMatchers.model().attribute("resetedPassword", "YES"));

        editEmployeeButton = "toEdit";
        Mockito.doThrow(new Exception("TEST")).when(userService).editEmployee(employee);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editemployee")
                .param("editEmployeeButton", editEmployeeButton)
                .sessionAttr("employee", employee))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        Mockito.doThrow(new Exception("TEST")).when(passwordService).resetPassword(employee);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editemployee")
                .param("resetPasswordButton", resetPasswordButton)
                .sessionAttr("employee", employee))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editEmployee"));
    }

    @Test
    void testAssignEmployeeModel() throws Exception {
    	mockMvc.perform(MockMvcRequestBuilders.get("/admin/assignemployee"))
    			.andExpect(MockMvcResultMatchers.status().isOk())
    			.andExpect(MockMvcResultMatchers.view().name("admin/user/assignEmployee"))
    			.andExpect(MockMvcResultMatchers.model().attribute("dateFrom", LocalDate.now().minusDays(7)))
    			.andExpect(MockMvcResultMatchers.model().attribute("dateTo", LocalDate.now()));
    }
    
    @Test
    void testAssignEmployeeStringModelRedirectAttributes() throws Exception {
    	String dateFrom = LocalDate.now().minusDays(7).toString();
    	String dateTo = LocalDate.now().toString();
    	String search = "search";
    	String offerRequestData = null;
    	String[] employeeIdArray = {"1"};
    	LocalDateTime localDateTimeNow = LocalDateTime.now();
    	
    	List<OfferRequest> offerRequestList = new ArrayList<>();
    	Mockito.when(offerService.getOfferRequestsByDate(localDateTimeNow, localDateTimeNow.plusDays(1))).thenReturn(offerRequestList);
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/admin/assignemployee")
    			.param("dateFrom", dateFrom)
    			.param("dateTo", dateTo)
    			.param("search", search)
    			.param("offerRequestData", offerRequestData)
    			.param("employeeId", employeeIdArray))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.view().name("admin/user/assignEmployee"))
				.andExpect(MockMvcResultMatchers.model().attribute("dateFrom", dateFrom))
				.andExpect(MockMvcResultMatchers.model().attribute("dateTo", dateTo))
				.andExpect(MockMvcResultMatchers.model().attribute("offerRequestsByDate", offerRequestList));
    	
    	search = null;
    	offerRequestData = "2;3";
    	mockMvc.perform(MockMvcRequestBuilders.post("/admin/assignemployee")
    			.param("dateFrom", dateFrom)
    			.param("dateTo", dateTo)
    			.param("search", search)
    			.param("offerRequestData", offerRequestData)
    			.param("employeeId", employeeIdArray))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
				.andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.assign.employee"));
    	
    	Mockito.doThrow(new Exception()).when(offerService).assignEmployee(offerRequestData, employeeIdArray);
    	search = null;
    	offerRequestData = "2;3";
    	mockMvc.perform(MockMvcRequestBuilders.post("/admin/assignemployee")
    			.param("dateFrom", dateFrom)
    			.param("dateTo", dateTo)
    			.param("search", search)
    			.param("offerRequestData", offerRequestData)
    			.param("employeeId", employeeIdArray))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
				.andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.defeat.assign.employee"));
    }
    
    @Test
    void testAddCustomerModel() throws Exception {
        List<CustomerType> allCustomerTypesExpect = new ArrayList<>();
        Mockito.when(userService.getAllCustomerTypes()).thenReturn(allCustomerTypesExpect);
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.get("/admin/addcustomer"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/addCustomer"))
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
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/addcustomer")
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/addCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", languages));

        Mockito.when(userService.checkDistinctLoginWithRegisterUser(customer.getUser().getUsername())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/addcustomer")
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        Mockito.doThrow(new Exception()).when(userService).addNewCustomer(customer, false);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/addcustomer")
                .sessionAttr("customer", customer))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));
    }

    @Test
    void testEditCustomerModel() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/editcustomer"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editCustomer"));
    }

    @Test
    void testEditCustomerCustomerStringStringStringStringStringStringModelRedirectAttributes() throws Exception {
        String searchCustomerButton = "SEARCH";
        String searchCustomerField = "something to search";
        String selectCustomerButton = "SELECT";
        String selectedCustomerId = "1";
        String editCustomerButton = "toEdit";
        String resetPasswordButton = "resetPass";
        List<Customer> customersListExpect = new ArrayList<>();
        customersListExpect.add(new Customer());
        customersListExpect.add(new Customer());
        Mockito.when(hibernateSearchService.searchCustomerNameRegonStreetPhoneByKeywordQuery(searchCustomerField)).thenReturn(customersListExpect);
        List<CustomerType> allCustomerTypesExpect = new ArrayList<>();
        Mockito.when(userService.getAllCustomerTypes()).thenReturn(allCustomerTypesExpect);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.post("/admin/editcustomer")
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("editCustomerButton", editCustomerButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editCustomer"))
                .andReturn().getModelAndView().getModel();
        @SuppressWarnings("unchecked")
        List<Customer> customersListActual = (List<Customer>) model.get("foundCustomers");
        assertEquals(2, customersListActual.size());
        @SuppressWarnings("unchecked")
        List<CustomerType> allCustomerTypesActual = (List<CustomerType>) model.get("allCustomerTypes");
        assertNotNull(allCustomerTypesActual);

        searchCustomerButton = null;
        Customer customer = new Customer();
        Mockito.when(userService.getCustomer(selectedCustomerId)).thenReturn(customer);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editcustomer")
                .sessionAttr("customer", customer)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("editCustomerButton", editCustomerButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("customer", customer));

        selectCustomerButton = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editcustomer")
                .sessionAttr("customer", customer)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("editCustomerButton", editCustomerButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        Mockito.doThrow(new Exception()).when(userService).editCustomer(customer);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editcustomer")
                .sessionAttr("customer", customer)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("editCustomerButton", editCustomerButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        editCustomerButton = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editcustomer")
                .sessionAttr("customer", customer)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("editCustomerButton", editCustomerButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editCustomer"));

        Mockito.doThrow(new Exception()).when(passwordService).resetPassword(customer);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editcustomer")
                .sessionAttr("customer", customer)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("editCustomerButton", editCustomerButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/editCustomer"));
    }

    @Test
    void testRemoveCustomer() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/removecustomer"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/removeCustomer"));
    }

    @Test
    void testRemoveCustomerStringStringStringStringStringCustomerModelRedirectAttributes() throws Exception {
        String searchCustomerButton = "SEARCH";
        String searchCustomerField = "something to search";
        String selectCustomerButton = "SELECT";
        String selectedCustomerId = "1";
        String removeCustomerButton = "toRemove";
        List<Customer> customersListExpect = new ArrayList<>();
        customersListExpect.add(new Customer());
        customersListExpect.add(new Customer());
        Mockito.when(hibernateSearchService.searchCustomerNameRegonStreetPhoneByKeywordQuery(searchCustomerField)).thenReturn(customersListExpect);
        List<CustomerType> allCustomerTypesExpect = new ArrayList<>();
        Mockito.when(userService.getAllCustomerTypes()).thenReturn(allCustomerTypesExpect);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.post("/admin/removecustomer")
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("removeCustomerButton", removeCustomerButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/removeCustomer"))
                .andReturn().getModelAndView().getModel();
        @SuppressWarnings("unchecked")
        List<Customer> customersListActual = (List<Customer>) model.get("foundCustomers");
        assertEquals(2, customersListActual.size());
        @SuppressWarnings("unchecked")
        List<CustomerType> allCustomerTypesActual = (List<CustomerType>) model.get("allCustomerTypes");
        assertNotNull(allCustomerTypesActual);

        searchCustomerButton = null;
        Customer customer = new Customer();
        Mockito.when(userService.getCustomer(selectedCustomerId)).thenReturn(customer);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/removecustomer")
                .sessionAttr("customer", customer)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("removeCustomerButton", removeCustomerButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/removeCustomer"))
                .andExpect(MockMvcResultMatchers.model().attribute("customer", customer));

        selectCustomerButton = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/removecustomer")
                .sessionAttr("customer", customer)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("removeCustomerButton", removeCustomerButton))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.remove.customer"));

        Mockito.doThrow(new Exception()).when(userService).removeCustomer(customer);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/removecustomer")
                .sessionAttr("customer", customer)
                .param("searchCustomerButton", searchCustomerButton)
                .param("searchCustomerField", searchCustomerField)
                .param("selectCustomerButton", selectCustomerButton)
                .param("selectedCustomerId", selectedCustomerId)
                .param("removeCustomerButton", removeCustomerButton))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.defeat.remove.customer"));
    }

    @Test
    void testSelfEditAdminModel() throws Exception {
        User user = new User();
        user.setId(13L);
        Employee adminExpect = new Employee();
        byte[] imageExpect = new byte[10];
        adminExpect.setPhoto(imageExpect);
        adminExpect.setUser(user);
        Mockito.when(userService.getLoggedEmployee()).thenReturn(adminExpect);
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.get("/admin/selfedit"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/selfEdit"))
                .andReturn().getModelAndView().getModel();

        Employee adminActual = (Employee) model.get("employee");
        assertEquals(13, adminActual.getUser().getId());
        byte[] imageActualt = (byte[]) model.get("image");
        assertEquals(10, imageActualt.length);
        Long editUserId = (Long) model.get("editUserId");
        assertEquals(13, editUserId);
        String[] languagesActual = (String[]) model.get("languages");
        assertEquals("pl", languagesActual[0]);
    }

    @Test
    void testSelfEditAdminEmployeeBindingResultMultipartFileIntegerByteArrayModelRedirectAttributes() throws Exception {
        User user = new User();
        user.setUsername("username123");
        user.setPasswordField("passwordField123");
        Employee adminExpect = new Employee();
        adminExpect.setFirstName("firstName");
        adminExpect.setLastName("lastName");
        adminExpect.setEmail("email@email.pl");
        adminExpect.setLanguage("pl");
        byte[] image = "photoFile".getBytes();
        adminExpect.setPhoto(image);
        adminExpect.setUser(user);
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        MockMultipartFile photo = new MockMultipartFile("newPhoto", "".getBytes());
        Integer editUserIdExpect = 33;
        Mockito.when(userService.checkDistinctLoginWithEditUser(adminExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(false);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/selfedit")
                .file(photo)
                .sessionAttr("employee", adminExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/selfEdit"))
                .andReturn().getModelAndView().getModel();
        String[] languagesActual = (String[]) model.get("languages");
        assertEquals("pl", languagesActual[0]);
        Employee adminActual = (Employee) model.get("employee");
        assertEquals(9, adminActual.getPhoto().length);
        assertEquals("username123", adminActual.getUser().getUsername());
        Integer editUserIdActual = (Integer) model.get("editUserId");
        assertEquals(33, editUserIdActual);

        photo = new MockMultipartFile("newPhoto", new byte[600001]);
        Mockito.when(userService.checkDistinctLoginWithEditUser(adminExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/selfedit")
                .file(photo)
                .sessionAttr("employee", adminExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user/selfEdit"));

        User loggedUser = new User();
        loggedUser.setUsername("otherUsername");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        photo = new MockMultipartFile("newPhoto", "file".getBytes());
        Mockito.when(userService.checkDistinctLoginWithEditUser(adminExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/selfedit")
                .file(photo)
                .sessionAttr("employee", adminExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/logoutpage"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.edit.self"));

        loggedUser.setUsername("username123");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/selfedit")
                .file(photo)
                .sessionAttr("employee", adminExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.success.edit.self"));

        Mockito.doThrow(new Exception()).when(userService).selfEditEmployee(adminActual);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/selfedit")
                .file(photo)
                .sessionAttr("employee", adminExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "admin.defeat.edit.self"));

    }

    @Test
    void testEditHomePageModel() throws Exception {
        MainPages mainPages = new MainPages();
        when(homeService.getMainPages(PageType.HOME)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/edithome"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/homePage"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testEditHomePageMainPagesBindingResultModel() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.HOME);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/edithome")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/homePage"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));

        mainPages.setContent("content have more then 10 characters.");
        ArgumentCaptor<MainPages> argumentCaptor = ArgumentCaptor.forClass(MainPages.class);
        doNothing().when(homeService).updateMainPagesRecord(argumentCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/edithome")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/homePage"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
        assertEquals("content have more then 10 characters.", argumentCaptor.getValue().getContent());
        assertEquals(PageType.HOME, argumentCaptor.getValue().getPageType());
    }

    @Test
    void testEditAboutusModel() throws Exception {
        MainPages mainPages = new MainPages();
        when(homeService.getMainPages(PageType.ABOUTUS)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/editaboutus"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/aboutus"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testEditAboutusMainPagesBindingResultModel() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.ABOUTUS);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editaboutus")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/aboutus"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));;

        mainPages.setContent("content have more then 10 characters.");
        ArgumentCaptor<MainPages> argumentCaptor = ArgumentCaptor.forClass(MainPages.class);
        doNothing().when(homeService).updateMainPagesRecord(argumentCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/editaboutus")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/aboutus"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
        assertEquals("content have more then 10 characters.", argumentCaptor.getValue().getContent());
        assertEquals(PageType.ABOUTUS, argumentCaptor.getValue().getPageType());
    }

    @Test
    void testEditPortfolioModel() throws Exception {
        MainPages mainPages = new MainPages();
        when(homeService.getMainPages(PageType.PORTFOLIO)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/portfolio"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/portfolio"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));;
    }

    @Test
    void testEditPortfolioMainPagesBindingResultModel() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.PORTFOLIO);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/portfolio")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/portfolio"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));

        mainPages.setContent("content have more then 10 characters.");
        ArgumentCaptor<MainPages> argumentCaptor = ArgumentCaptor.forClass(MainPages.class);
        doNothing().when(homeService).updateMainPagesRecord(argumentCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/portfolio")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/portfolio"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
        assertEquals("content have more then 10 characters.", argumentCaptor.getValue().getContent());
        assertEquals(PageType.PORTFOLIO, argumentCaptor.getValue().getPageType());
    }

    @Test
    void testEditReferencesModel() throws Exception {
        MainPages mainPages = new MainPages();
        when(homeService.getMainPages(PageType.REFERENCES)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/references"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/references"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testEditReferencesMainPagesBindingResultModel() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.REFERENCES);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/references")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/references"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));

        mainPages.setContent("content have more then 10 characters.");
        ArgumentCaptor<MainPages> argumentCaptor = ArgumentCaptor.forClass(MainPages.class);
        doNothing().when(homeService).updateMainPagesRecord(argumentCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/references")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/references"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
        assertEquals("content have more then 10 characters.", argumentCaptor.getValue().getContent());
        assertEquals(PageType.REFERENCES, argumentCaptor.getValue().getPageType());
    }

    @Test
    void testEditServicesModel() throws Exception {
        MainPages mainPages = new MainPages();
        when(homeService.getMainPages(PageType.SERVICES)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/services"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/services"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testEditServicesMainPagesBindingResultModel() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.SERVICES);
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/services")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/services"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));

        mainPages.setContent("content have more then 10 characters.");
        ArgumentCaptor<MainPages> argumentCaptor = ArgumentCaptor.forClass(MainPages.class);
        doNothing().when(homeService).updateMainPagesRecord(argumentCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/services")
                .sessionAttr("mainPages", mainPages))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/services"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
        assertEquals("content have more then 10 characters.", argumentCaptor.getValue().getContent());
        assertEquals(PageType.SERVICES, argumentCaptor.getValue().getPageType());
    }

    @Test
    void testEditLogoAndSloganModel() throws Exception {
        LogoSloganFooter logoSloganFooterExpect = new LogoSloganFooter();
        when(homeService.getLogoSloganFooter()).thenReturn(logoSloganFooterExpect);
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/logosloganfooter"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/logoSloganFooter"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("logoSloganFooter"));
    }

    @Test
    void testEditLogoAndSloganLogoSloganFooterBindingResultMultipartFileByteArrayModelRedirectAttributes() throws Exception {
        LogoSloganFooter logoSloganFooter = new LogoSloganFooter();
        MockMultipartFile newLogo = new MockMultipartFile("newLogo", "".getBytes());
        byte[] logoSession = "logoSession".getBytes();
        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/logosloganfooter")
                .file(newLogo)
                .sessionAttr("logoSession", logoSession)
                .sessionAttr("logoSloganFooter", logoSloganFooter))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        newLogo = new MockMultipartFile("newLogo", "newLogoMoreThen10BandLessThen600000B".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/logosloganfooter")
                .file(newLogo)
                .sessionAttr("logoSession", logoSession)
                .sessionAttr("logoSloganFooter", logoSloganFooter))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        newLogo = new MockMultipartFile("newLogo", new byte[600001]);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/logosloganfooter")
                .file(newLogo)
                .sessionAttr("logoSession", logoSession)
                .sessionAttr("logoSloganFooter", logoSloganFooter))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/home/logoSloganFooter"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("logoSloganFooter"));

        newLogo = new MockMultipartFile("newLogo", "newLogoMoreThen10BandLessThen600000B".getBytes());
        Mockito.doThrow(new Exception()).when(homeService).updateLogoSloganFooter(logoSloganFooter);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/logosloganfooter")
                .file(newLogo)
                .sessionAttr("logoSession", logoSession)
                .sessionAttr("logoSloganFooter", logoSloganFooter))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));
    }

}
