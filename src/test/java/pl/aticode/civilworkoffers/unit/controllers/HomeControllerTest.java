package pl.aticode.civilworkoffers.unit.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pl.aticode.civilworkoffers.controller.HomeController;
import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;
import pl.aticode.civilworkoffers.entity.offer.ByteAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestAttachment;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.model.ContactUs;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.CipherService;
import pl.aticode.civilworkoffers.service.HomeService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.PasswordService;
import pl.aticode.civilworkoffers.service.ReCaptchaService;
import pl.aticode.civilworkoffers.service.SendEmailService;
import pl.aticode.civilworkoffers.service.UserService;
import pl.aticode.civilworkoffers.service.UserService.SocialCustomerState;

class HomeControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private HomeController homeController;
    @Mock
    private UserService userService;
    @Mock
    private ReCaptchaService reCaptchaService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private CipherService cipherService;
    @Mock
    private CharFilterService charFilterService;
    @Mock
    private MessageSource messageSource;
    @Mock
    private SendEmailService sendEmail;
    @Mock
    private HomeService homeService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2AuthenticationToken oAuth2AuthenticationToken;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RedirectAttributes redirectAttributes;
    @Mock
    private OfferService offerService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    void testHome() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.HOME);
        Mockito.when(homeService.getMainPages(PageType.HOME)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/home"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testAboutus() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.ABOUTUS);
        Mockito.when(homeService.getMainPages(PageType.ABOUTUS)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/aboutus"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/aboutus"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testPortfolio() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.PORTFOLIO);
        Mockito.when(homeService.getMainPages(PageType.PORTFOLIO)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/portfolio"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/portfolio"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testReferences() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.REFERENCES);
        Mockito.when(homeService.getMainPages(PageType.REFERENCES)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/references"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/references"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testServices() throws Exception {
        MainPages mainPages = new MainPages();
        mainPages.setPageType(PageType.SERVICES);
        Mockito.when(homeService.getMainPages(PageType.SERVICES)).thenReturn(mainPages);
        mockMvc.perform(MockMvcRequestBuilders.get("/services"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/services"))
                .andExpect(MockMvcResultMatchers.model().attribute("mainPages", mainPages));
    }

    @Test
    void testLogin() throws Exception {
        String logout = "logout";
        String error = null;
        mockMvc.perform(MockMvcRequestBuilders.get("/loginpage")
                .param("logout", logout)
                .param("error", error))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/login"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "home.logout"));

        logout = null;
        error = "error";
        mockMvc.perform(MockMvcRequestBuilders.get("/loginpage")
                .param("logout", logout)
                .param("error", error))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/login"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "home.login.defeat"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_CUSTOMER");
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        grantedAuthorityList.add(grantedAuthority);
        Authentication authentication = new TestingAuthenticationToken(null, null, grantedAuthorityList);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Mockito.doReturn(authorities).when(userService).getAuthoritiesLoggedUser();
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"));

        grantedAuthority = new SimpleGrantedAuthority("ROLE_OWNER");
        grantedAuthorityList.set(0, grantedAuthority);
        authentication = new TestingAuthenticationToken(null, null, grantedAuthorityList);
        authorities = authentication.getAuthorities();
        Mockito.doReturn(authorities).when(userService).getAuthoritiesLoggedUser();
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/owner/main"));

        grantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        grantedAuthorityList.set(0, grantedAuthority);
        authentication = new TestingAuthenticationToken(null, null, grantedAuthorityList);
        authorities = authentication.getAuthorities();
        Mockito.doReturn(authorities).when(userService).getAuthoritiesLoggedUser();
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/admin/main"));

        grantedAuthority = new SimpleGrantedAuthority("ROLE_EMPLOYEE");
        grantedAuthorityList.set(0, grantedAuthority);
        authentication = new TestingAuthenticationToken(null, null, grantedAuthorityList);
        authorities = authentication.getAuthorities();
        Mockito.doReturn(authorities).when(userService).getAuthoritiesLoggedUser();
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"));

        grantedAuthorityList.clear();
        authentication = new TestingAuthenticationToken(null, null, grantedAuthorityList);
        authorities = authentication.getAuthorities();
        Mockito.doReturn(authorities).when(userService).getAuthoritiesLoggedUser();
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("forward:/loginpage"));
    }

    @Test
    void testLoginSuccessOauth() throws Exception {
        Mockito.when(userService.loginRegisterSocialCustomer(null)).thenReturn(SocialCustomerState.REGISTERED_ENABLED);
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccessOauth"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"));

        Mockito.when(userService.loginRegisterSocialCustomer(null)).thenReturn(SocialCustomerState.NONREGISTERED);
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccessOauth"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.add.socialuser.success"));

        Mockito.when(userService.loginRegisterSocialCustomer(null)).thenReturn(SocialCustomerState.REGISTERED_DISABLED);
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccessOauth"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/error"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.login.socialuser.defeat"));

        Mockito.when(userService.loginRegisterSocialCustomer(oAuth2AuthenticationToken)).thenReturn(SocialCustomerState.REGISTERED_DISABLED);
        mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccessOauth"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/error"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "customer.login.socialuser.defeat"));
    }

    @Test
    void testLogout() throws Exception {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        mockMvc.perform(MockMvcRequestBuilders.get("/logoutpage"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/logout"));
    }

	@Test
	void testOpenFile() throws Exception {
		String id = "13";
		ByteAttachment byteAttachment = new ByteAttachment();
		byteAttachment.setId(11L);
		byteAttachment.setFile("fileAttachment".getBytes());
		OfferRequestAttachment attachment = new OfferRequestAttachment();
		attachment.setByteAttachment(byteAttachment);
		Mockito.when(offerService.getFileAttachment(id)).thenReturn(attachment);
		Mockito.when(offerService.getByteAttachment(attachment.getByteAttachment().getId())).thenReturn(byteAttachment);
		
        mockMvc.perform(MockMvcRequestBuilders.get("/file/13"))
		        .andExpect(MockMvcResultMatchers.status().isOk());
	}
	
    @Test
    void testGet403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/403"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/code403"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "home.login.denied"));
    }

    @Test
    void testForgetPassword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/forgetpassword"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/forgetPassword"));
    }

    @Test
    void testForgetPasswordStringStringStringModel() throws Exception {
        String username = "usern<>ame";
        String email = "email_email.pl";
        String reCaptchaResponse = "reCaptchaResponse";
        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/forgetpassword")
                .param("username", username)
                .param("email", email)
                .param("g-recaptcha-response", reCaptchaResponse))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/forgetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("usernameError", "error"))
                .andExpect(MockMvcResultMatchers.model().attribute("emailError", "error"))
                .andExpect(MockMvcResultMatchers.model().attribute("recaptchaError", "error"));

        username = "username";
        email = "email@email.pl";
        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(true);
        Mockito.when(passwordService.forgetPassword(username, email)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/forgetpassword")
                .param("username", username)
                .param("email", email)
                .param("g-recaptcha-response", reCaptchaResponse))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/forgetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "user.passwordreset.email.send.success"));

        Mockito.when(passwordService.forgetPassword(username, email)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/forgetpassword")
                .param("username", username)
                .param("email", email)
                .param("g-recaptcha-response", reCaptchaResponse))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/forgetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "user.passwordreset.email.send.defeat"));

        Mockito.doThrow(new Exception("TEST")).when(passwordService).forgetPassword(username, email);
        mockMvc.perform(MockMvcRequestBuilders.post("/forgetpassword")
                .param("username", username)
                .param("email", email)
                .param("g-recaptcha-response", reCaptchaResponse))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/forgetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "user.passwordreset.email.send.defeat"));
    }

    @Test
    void testResetPassword() throws Exception {
        Customer customer = new Customer();
        customer.setId(13L);
        String resetPasswordString = "resetPasswordString";
        String decodeToken = customer.getId() + ";" + customer.getClass().getCanonicalName() + ";" + LocalDateTime.now().withNano(0).plusHours(1);
        Mockito.when(cipherService.decodeString(resetPasswordString)).thenReturn(decodeToken);
        String[] tokenArray = decodeToken.split(";");
        Mockito.when(userService.getCustomer(tokenArray[0])).thenReturn(customer);
        ArgumentCaptor<Customer> argumentCaptorCustomer = ArgumentCaptor.forClass(Customer.class);
        doNothing().when(passwordService).resetPassword(argumentCaptorCustomer.capture());
        mockMvc.perform(MockMvcRequestBuilders.get("/resetpassword")
                .param("resetPasswordString", resetPasswordString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/resetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "user.success.resetpassword"));
        assertEquals(13, argumentCaptorCustomer.getValue().getId());

        Employee employee = new Employee();
        employee.setId(33L);
        decodeToken = employee.getId() + ";" + employee.getClass().getCanonicalName() + ";" + LocalDateTime.now().withNano(0).plusHours(1);
        Mockito.when(cipherService.decodeString(resetPasswordString)).thenReturn(decodeToken);
        tokenArray = decodeToken.split(";");
        Mockito.when(userService.getEmployee(tokenArray[0])).thenReturn(employee);
        ArgumentCaptor<Employee> argumentCaptorEmployee = ArgumentCaptor.forClass(Employee.class);
        doNothing().when(passwordService).resetPassword(argumentCaptorEmployee.capture());
        mockMvc.perform(MockMvcRequestBuilders.get("/resetpassword")
                .param("resetPasswordString", resetPasswordString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/resetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "user.success.resetpassword"));
        assertEquals(33, argumentCaptorEmployee.getValue().getId());

        Owner owner = new Owner();
        owner.setId(321L);
        decodeToken = owner.getId() + ";" + owner.getClass().getCanonicalName() + ";" + LocalDateTime.now().withNano(0).plusHours(1);
        Mockito.when(cipherService.decodeString(resetPasswordString)).thenReturn(decodeToken);
        tokenArray = decodeToken.split(";");
        Mockito.when(userService.getOwner(tokenArray[0])).thenReturn(owner);
        ArgumentCaptor<Owner> argumentCaptorOwner = ArgumentCaptor.forClass(Owner.class);
        doNothing().when(passwordService).resetPassword(argumentCaptorOwner.capture());
        mockMvc.perform(MockMvcRequestBuilders.get("/resetpassword")
                .param("resetPasswordString", resetPasswordString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/resetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "user.success.resetpassword"));
        assertEquals(321, argumentCaptorOwner.getValue().getId());

        User user = new User();
        user.setId(99L);
        decodeToken = user.getId() + ";" + user.getClass().getCanonicalName() + ";" + LocalDateTime.now().withNano(0).plusHours(1);
        Mockito.when(cipherService.decodeString(resetPasswordString)).thenReturn(decodeToken);
        mockMvc.perform(MockMvcRequestBuilders.get("/resetpassword")
                .param("resetPasswordString", resetPasswordString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/resetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "user.defeat.resetpassword"));

        decodeToken = owner.getId() + ";" + owner.getClass().getCanonicalName() + ";" + LocalDateTime.now().withNano(0).minusHours(1);
        Mockito.when(cipherService.decodeString(resetPasswordString)).thenReturn(decodeToken);
        mockMvc.perform(MockMvcRequestBuilders.get("/resetpassword")
                .param("resetPasswordString", resetPasswordString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/resetPassword"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "user.defeat.resetpassword.timeout"));
    }

    @Test
    void testContactUs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/contactus"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/contactus"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("contactUs"));
    }

    @Test
    void testSendMessage() throws Exception {
        homeController = new HomeController(5, 500);
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
        List<MultipartFile> multipartFileList = new ArrayList<>();
        multipartFileList.add(new MockMultipartFile("file1", "file1".getBytes()));
        multipartFileList.add(new MockMultipartFile("file2", "file2".getBytes()));
        multipartFileList.add(new MockMultipartFile("file2", "file2".getBytes()));
        ContactUs contactUs = new ContactUs();
        contactUs.setAttachment(multipartFileList);
        contactUs.setReplyEmail("email@mail.pl");
        String reCaptchaResponse = "reCaptchaResponse123";
        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/contactus")
                .flashAttr("contactUs", contactUs)
                .param("g-recaptcha-response", reCaptchaResponse))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/contactus"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "home.contactus.email.send.success"));

        Mockito.doThrow(Exception.class).when(charFilterService).doCharFilter(Mockito.any(), Mockito.any());
        mockMvc.perform(MockMvcRequestBuilders.post("/contactus")
                .flashAttr("contactUs", contactUs)
                .param("g-recaptcha-response", reCaptchaResponse))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/contactus"))
                .andExpect(MockMvcResultMatchers.model().attribute("message", "home.contactus.email.send.defeat"));

        homeController = new HomeController(2, 500);
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
        Mockito.when(reCaptchaService.verify(reCaptchaResponse)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/contactus")
                .flashAttr("contactUs", contactUs)
                .param("g-recaptcha-response", reCaptchaResponse))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home/contactus"));
    }

}
