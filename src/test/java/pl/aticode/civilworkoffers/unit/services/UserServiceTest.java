package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.client.RestTemplate;

import pl.aticode.civilworkoffers.dao.OfferRepository;
import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.Role;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.ActivationService;
import pl.aticode.civilworkoffers.service.AlertOfferDateExecQuartzJobService;
import pl.aticode.civilworkoffers.service.PasswordService;
import pl.aticode.civilworkoffers.service.SendEmailService;
import pl.aticode.civilworkoffers.service.UserService;

class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Spy
    private PasswordEncoder passwordEncoder;
    @Spy
    private PasswordService passwordService;
    @Mock
    private SendEmailService sendEmail;
    @Mock
    private MessageSource messageSource;
    @Mock
    private ActivationService activationService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;
    @Mock
    private OAuth2AuthenticationToken oAuth2AuthenticationToken;
    @Mock
    private OfferRepository offerRepository;
    @Mock
    private AlertOfferDateExecQuartzJobService alertOfferDateExecQuartzJobService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetAuthoritiesLoggedUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        @SuppressWarnings("rawtypes")
        Collection authorities = Collections.emptyList();
        when(userDetails.getAuthorities()).thenReturn(authorities);
        assertEquals(authorities, userService.getAuthoritiesLoggedUser());
    }

    @Test
    void testCheckDistinctLoginWithRegisterUser() {
        User user = new User();
        String login = "login";
        Mockito.when(userRepository.findUser(login)).thenReturn(user);
        assertFalse(userService.checkDistinctLoginWithRegisterUser(login));
        user = null;
        Mockito.when(userRepository.findUser(login)).thenReturn(user);
        assert (userService.checkDistinctLoginWithRegisterUser(login));
    }

    @Test
    void testCheckDistinctLoginWithEditUser() {
        User user = new User();
        user.setId(13L);
        user.setUsername("login1");
        Mockito.when(userRepository.findUser(13)).thenReturn(user);
        assertTrue(userService.checkDistinctLoginWithEditUser("login1", 13));
        Mockito.when(userRepository.findUser("login2")).thenReturn(null);
        assertTrue(userService.checkDistinctLoginWithEditUser("login2", 13));
        Mockito.when(userRepository.findUser("login2")).thenReturn(new User());
        assertFalse(userService.checkDistinctLoginWithEditUser("login2", 13));
    }

    @Test
    void testGetLoggedUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        String username = "login12";
        when(authentication.getName()).thenReturn(username);
        User user = new User();
        user.setUsername(username);
        Mockito.when(userRepository.findUser("login12")).thenReturn(user);
        assertEquals(username, userService.getLoggedUser().getUsername());
        when(securityContext.getAuthentication()).thenReturn(null);
        assertNull(userService.getLoggedUser());
    }

    @Test
    void testGetRoleForAdminEmployee() {
//        Role role = new Role();
//        role.setId(3);
//        Mockito.when(userRepository.findRole(3)).thenReturn(role);
//        assertEquals(role.getId(), userService.getRoleEmployee().getId());
    }

    @Test
    void testGetAllCustomerTypes() {
        List<CustomerType> customerTypes = new ArrayList<>();
        customerTypes.add(new CustomerType());
        customerTypes.add(new CustomerType());
        customerTypes.add(new CustomerType());
        Mockito.when(userRepository.findAllCustomerTypes()).thenReturn(customerTypes);
        assertEquals(customerTypes.size(), userService.getAllCustomerTypes().size());
    }

    @Test
    void testGetLanguages() {
        String languages = "pl;en";
        String[] languagesArray = languages.split(";");
        UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getLanguages()).thenReturn(languagesArray);
        assertEquals(languagesArray[0], userService.getLanguages()[0]);
    }

    @Test
    void testGetOwner() {
        Owner owner = new Owner();
        owner.setId(13L);
        Mockito.when(userRepository.findOwner(owner.getId())).thenReturn(owner);
        assertEquals(owner.getId(), userService.getOwner("13").getId());
    }

    @Test
    void testGetLoggedOwner() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        String username = "loginOwner";
        when(authentication.getName()).thenReturn(username);
        Owner owner = new Owner();
        owner.setId(33L);
        Mockito.when(userRepository.findOwner(username)).thenReturn(owner);
        assertEquals(owner.getId(), userService.getLoggedOwner().getId());
    }

    @Test
    void testSelfEditOwner() throws Exception {
        User user = new User();
        user.setUsername("username");
        Owner owner = new Owner();
        owner.setUser(user);

        ArgumentCaptor<Owner> argumentCaptor = ArgumentCaptor.forClass(Owner.class);
        doNothing().when(userRepository).updateOwner(argumentCaptor.capture());
        userService.selfEditOwner(owner);
        assertEquals("username", argumentCaptor.getValue().getUser().getUsername());
    }

    @Test
    void testAddNewAdmin() throws Exception {
        User user = new User();
        Employee admin = new Employee();
        admin.setLastName("lastName");
        admin.setLanguage("pl");
        admin.setUser(user);

        ArgumentCaptor<Employee> argumentCaptor = ArgumentCaptor.forClass(Employee.class);
        doNothing().when(userRepository).saveEmployee(argumentCaptor.capture());
        userService.addNewAdmin(admin);
        assertEquals("lastName", argumentCaptor.getValue().getLastName());
    }

    @Test
    void testGetAllAdmins() {
        Role role1 = new Role();
        role1.setRole("ROLE_ADMIN");
        Role role2 = new Role();
        role2.setRole("ROLE_ADMIN");
        List<Role> roles1 = new ArrayList<>();
        roles1.add(role1);
        List<Role> roles2 = new ArrayList<>();
        roles2.add(role2);
        User user1 = new User();
        user1.setRoles(roles1);
        User user2 = new User();
        user2.setRoles(roles2);
        Employee admin1 = new Employee();
        admin1.setUser(user1);
        Employee admin2 = new Employee();
        admin2.setUser(user2);
        List<Employee> admins = new ArrayList<>();
        admins.add(admin1);
        admins.add(admin2);
        Mockito.when(userRepository.findAllEmployees()).thenReturn(admins);
        assertEquals(admins.size(), userService.getAllAdmins().size());
    }

    @Test
    void testEditAdmin() throws Exception {
        Role roleAdmin = new Role();
        roleAdmin.setId(3L);
        List<Role> roles = new ArrayList<>();
        roles.add(roleAdmin);
        User user = new User();
        user.setRoles(roles);
        user.setUsername("username");
        Employee admin = new Employee();
        admin.setUser(user);
        admin.setLastName("lastName");

        ArgumentCaptor<Employee> argumentCaptor = ArgumentCaptor.forClass(Employee.class);
        doNothing().when(userRepository).updateEmployee(argumentCaptor.capture());
        userService.editAdmin(admin);
        assertEquals("lastName", argumentCaptor.getValue().getLastName());
        assertEquals(3, argumentCaptor.getValue().getUser().getRoles().get(0).getId());
    }
    
    @Test
    void testAddNewEmployee() throws Exception {
        User user = new User();
        Employee employee = new Employee();
        employee.setLastName("lastName");
        employee.setLanguage("pl");
        employee.setUser(user);

        ArgumentCaptor<Employee> argumentCaptor = ArgumentCaptor.forClass(Employee.class);
        doNothing().when(userRepository).saveEmployee(argumentCaptor.capture());
        userService.addNewEmployee(employee);
        assertEquals("lastName", argumentCaptor.getValue().getLastName());
    }

    @Test
    void testGetAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee());
        employees.add(new Employee());
        employees.add(new Employee());
        Mockito.when(userRepository.findAllEmployees()).thenReturn(employees);
        assertEquals(employees.size(), userRepository.findAllEmployees().size());
    }

    @Test
    void testGetEmployee() {
        Employee employee = new Employee();
        Mockito.when(userRepository.findEmployee(33)).thenReturn(employee);
        assertNotNull(userRepository.findEmployee(33));
    }

    @Test
    void testGetLoggedEmployee() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        String username = "loginEmployee";
        when(authentication.getName()).thenReturn(username);
        Employee employee = new Employee();
        employee.setId(33L);
        Mockito.when(userRepository.findEmployee(username)).thenReturn(employee);
        assertEquals(employee.getId(), userService.getLoggedEmployee().getId());
    }

    @Test
    void testEditEmployee() throws Exception {
        User user = new User();
        user.setUsername("username");
        Employee employee = new Employee();
        employee.setUser(user);

        ArgumentCaptor<Employee> argumentCaptor = ArgumentCaptor.forClass(Employee.class);
        doNothing().when(userRepository).updateEmployee(argumentCaptor.capture());
        userService.editEmployee(employee);
        assertEquals("username", argumentCaptor.getValue().getUser().getUsername());
    }

    @Test
    void testSelfEditEmployee() throws Exception {
        User user = new User();
        user.setUsername("username");
        Employee employee = new Employee();
        employee.setUser(user);

        ArgumentCaptor<Employee> argumentCaptor = ArgumentCaptor.forClass(Employee.class);
        doNothing().when(userRepository).updateEmployee(argumentCaptor.capture());
        userService.selfEditEmployee(employee);
        assertEquals("username", argumentCaptor.getValue().getUser().getUsername());
    }

    @Test
    void testAddNewCustomer() throws Exception {
        User user = new User();
        Customer customer = new Customer();
        customer.setLastName("lastName");
        customer.setLanguage("pl");
        customer.setUser(user);

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        doNothing().when(userRepository).saveCustomer(argumentCaptor.capture());
        userService.addNewCustomer(customer, false);
        assertEquals("lastName", argumentCaptor.getValue().getLastName());
    }

    @Test
    void testGetAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        customers.add(new Customer());
        customers.add(new Customer());
        customers.add(new Customer());
        Mockito.when(userRepository.findAllCustomers()).thenReturn(customers);
        assertEquals(customers.size(), userRepository.findAllCustomers().size());
    }

    @Test
    void testGetCustomer() {
        Customer customer = new Customer();
        Mockito.when(userRepository.findCustomer(22)).thenReturn(customer);
        assertNotNull(userRepository.findCustomer(22));
    }

    @Test
    void testGetLoggedCustomer() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        String username = "loginCustomer";
        when(authentication.getName()).thenReturn(username);
        Customer customer = new Customer();
        customer.setId(33L);
        Mockito.when(userRepository.findCustomer(username)).thenReturn(customer);
        assertEquals(customer.getId(), userService.getLoggedCustomer().getId());
    }

    @Test
    void testEditCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setLastName("lastName");

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        doNothing().when(userRepository).updateCustomer(argumentCaptor.capture());
        userService.editCustomer(customer);
        assertEquals("lastName", argumentCaptor.getValue().getLastName());
    }

    @Test
    void testSelfEditCustomer() throws Exception {
        User user = new User();
        Customer customer = new Customer();
        customer.setLastName("lastName");
        customer.setUser(user);

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        doNothing().when(userRepository).updateCustomer(argumentCaptor.capture());
        userService.selfEditCustomer(customer);
        assertEquals("lastName", argumentCaptor.getValue().getLastName());
    }

    @Test
    void testRemoveCustomer() throws Exception {
        User user = new User();
        Customer customer = new Customer();
        customer.setLastName("lastName");
        customer.setLanguage("pl");
        customer.setUser(user);
        
		OfferRequest offerRequest1 = new OfferRequest();
		offerRequest1.setId(33L);
		offerRequest1.setRegisterDateTime(LocalDateTime.now().minusDays(2));
		OfferRequest offerRequest2 = new OfferRequest();
		offerRequest2.setRegisterDateTime(LocalDateTime.now());
		List<OfferRequest> offerRequestList = new ArrayList<>();
		offerRequestList.add(offerRequest1);
		offerRequestList.add(offerRequest2);
        Mockito.when(offerRepository.findAllOfferRequest(customer)).thenReturn(offerRequestList);

        ArgumentCaptor<Customer> argumentCaptorCustomer = ArgumentCaptor.forClass(Customer.class);
        doNothing().when(userRepository).removeCustomer(argumentCaptorCustomer.capture());
        @SuppressWarnings("unchecked")
		ArgumentCaptor<List<OfferRequest>> argumentCaptorAlert = ArgumentCaptor.forClass(List.class);
        doNothing().when(alertOfferDateExecQuartzJobService).removeAllAlertsOfferDateExecJobCustomer(argumentCaptorAlert.capture());
        userService.removeCustomer(customer);
        assertEquals("lastName", argumentCaptorCustomer.getValue().getLastName());
        assertEquals(33, argumentCaptorAlert.getValue().get(0).getId());
    }

    @Test
    void testDeleteCustomerPhoto() throws Exception {
        Customer customer = new Customer();
        customer.setPhoto("".getBytes());

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        doNothing().when(userRepository).updateCustomer(argumentCaptor.capture());
        userService.editCustomer(customer);
        assertEquals(0, argumentCaptor.getValue().getPhoto().length);
    }

    @Test
    void testDeleteEmployeePhoto() throws Exception {
        Employee employee = new Employee();
        employee.setPhoto("".getBytes());

        ArgumentCaptor<Employee> argumentCaptor = ArgumentCaptor.forClass(Employee.class);
        doNothing().when(userRepository).updateEmployee(argumentCaptor.capture());
        userService.editEmployee(employee);
        assertEquals(0, argumentCaptor.getValue().getPhoto().length);
    }

}
