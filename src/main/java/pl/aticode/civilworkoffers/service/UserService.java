package pl.aticode.civilworkoffers.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import net.sf.ehcache.CacheManager;
import pl.aticode.civilworkoffers.dao.OfferRepository;
import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.Role;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.model.FacebookUser;
import pl.aticode.civilworkoffers.model.GoogleUser;

@Service
public class UserService {

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value(value = "${languages}")
    private String languages;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private SendEmail sendEmail;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ActivationService activationService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private AlertOfferDateExecQuartzJobService alertOfferDateExecQuartzJobService;

    public static enum SocialCustomerState {
        REGISTERED_ENABLED, REGISTERED_DISABLED, NONREGISTERED;
    }


    /**
     * Method adding new administrator (Employee class) with role ADMIN. Optional, owner can set
     * additional role EMPLOYEE for administrator. Method send email to new
     * administrator with confirmation. Password is random generating and send
     * email.
     * @param employee 
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @CacheEvict(cacheNames = {"allEmployeesCache", "allAdminsCache"}, allEntries = true)
    public void addNewAdmin(Employee employee) throws Exception {
        String password = passwordService.createPassword();
        User user = employee.getUser();
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        employee.setRegisterDateTime(LocalDateTime.now());
        User loggedUser = getLoggedUser();
        employee.setUserRegister(loggedUser);
        userRepository.saveEmployee(employee);
        String emailSubject = messageSource.getMessage("admin.add.email.subject", null, new Locale(employee.getLanguage()));
        String emailContent = messageSource.getMessage("admin.add.email.content",
                new String[]{employee.getFirstName(), employee.getLastName(), employee.getUser().getUsername(), password, sendEmail.getMailFrom()},
                new Locale(employee.getLanguage()));
        sendEmail.sendEmail(employee.getEmail(), emailSubject, emailContent);
    }

