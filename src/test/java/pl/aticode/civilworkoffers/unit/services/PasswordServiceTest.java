package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.CipherService;
import pl.aticode.civilworkoffers.service.PasswordService;
import pl.aticode.civilworkoffers.service.SendEmailService;

class PasswordServiceTest {

    private PasswordService passwordService;
    @Mock
    private SendEmailService sendEmail;
    @Mock
    private MessageSource messageSource;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CipherService cipherService;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        passwordService = new PasswordService("host", sendEmail, messageSource, userRepository, cipherService, passwordEncoder, 8);
    }

    @Test
    void testCreatePassword() {
        assertEquals(8, passwordService.createPassword().length());
    }

    @Test
    void testResetPassword() throws Exception {
        User user = new User();
        user.setUsername("username22");
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setLanguage("pl");
        ArgumentCaptor<User> argumentCaptor1 = ArgumentCaptor.forClass(User.class);
        doNothing().when(userRepository).updateUser(argumentCaptor1.capture());
        passwordService.resetPassword(customer);
        assertEquals("username22", argumentCaptor1.getValue().getUsername());
    }

    @Test
    void testForgetPassword() throws Exception {
        User user1 = new User();
        user1.setEnabled(true);
        user1.setUsername("username11");
        Mockito.when(userRepository.findUser(user1.getUsername())).thenReturn(user1);
        Customer customer = new Customer();
        customer.setId(11L);
        customer.setEmail("email1");
        customer.setUser(user1);
        customer.setActiveLoginForm(true);
        customer.setLanguage("pl");
        Mockito.when(userRepository.findCustomer(user1.getUsername())).thenReturn(customer);
        assertTrue(passwordService.forgetPassword(user1.getUsername(), customer.getEmail()));

        User user2 = new User();
        user2.setEnabled(true);
        user2.setUsername("username22");
        Mockito.when(userRepository.findUser(user2.getUsername())).thenReturn(user2);
        Employee employee = new Employee();
        employee.setId(22L);
        employee.setEmail("email2");
        employee.setLanguage("en");
        employee.setUser(user2);
        Mockito.when(userRepository.findEmployee(user2.getUsername())).thenReturn(employee);
        assertTrue(passwordService.forgetPassword(user2.getUsername(), employee.getEmail()));

        User user4 = new User();
        user4.setEnabled(true);
        user4.setUsername("username44");
        Mockito.when(userRepository.findUser(user4.getUsername())).thenReturn(user4);
        Owner owner = new Owner();
        owner.setId(44L);
        owner.setEmail("email4");
        owner.setLanguage("pl");
        owner.setUser(user4);
        Mockito.when(userRepository.findOwner(user4.getUsername())).thenReturn(owner);
        assertTrue(passwordService.forgetPassword(user4.getUsername(), owner.getEmail()));

        Mockito.when(userRepository.findUser("user")).thenReturn(null);
        assertFalse(passwordService.forgetPassword("user", "email"));
    }

}
