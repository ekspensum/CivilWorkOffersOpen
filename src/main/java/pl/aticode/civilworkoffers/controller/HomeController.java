package pl.aticode.civilworkoffers.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;
import pl.aticode.civilworkoffers.entity.offer.ByteAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestAttachment;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.model.ContactUs;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.CipherService;
import pl.aticode.civilworkoffers.service.HomeService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.PasswordService;
import pl.aticode.civilworkoffers.service.ReCaptchaService;
import pl.aticode.civilworkoffers.service.SendEmail;
import pl.aticode.civilworkoffers.service.UserService;
import pl.aticode.civilworkoffers.service.UserService.SocialCustomerState;

@Controller
public class HomeController {

    private final static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Value(value = "${mail.from}")
    private String mailFrom;
    @Value(value = "${mail.files.max.number}")
    private int maxNumbersFiles;
    @Value(value = "${mail.files.max.size}")
    private int maxSizeFiles;

    @Autowired
    private UserService userService;
    @Autowired
    private ReCaptchaService reCaptchaService;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private CipherService cipherService;
    @Autowired
    private CharFilterService charFilterService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private SendEmail sendEmail;
    @Autowired
    private HomeService homeService;
    @Autowired
    private OfferService offerService;

    public HomeController() {
    }

    public HomeController(int maxNumbersFiles, int maxSizeFiles) {
        this.maxNumbersFiles = maxNumbersFiles;
        this.maxSizeFiles = maxSizeFiles;
    }

    @GetMapping(path = "/")
    public String home(Model model) {
        MainPages mainPages = homeService.getMainPages(PageType.HOME);
        model.addAttribute("mainPages", mainPages);
        return "home/home";
    }

    @GetMapping(path = "/aboutus")
    public String aboutus(Model model) {
        MainPages mainPages = homeService.getMainPages(PageType.ABOUTUS);
        model.addAttribute("mainPages", mainPages);
        return "home/aboutus";
    }

    @GetMapping(path = "/portfolio")
    public String portfolio(Model model) {
        MainPages mainPages = homeService.getMainPages(PageType.PORTFOLIO);
        model.addAttribute("mainPages", mainPages);
        return "home/portfolio";
    }

    @GetMapping(path = "/references")
    public String references(Model model) {
        MainPages mainPages = homeService.getMainPages(PageType.REFERENCES);
        model.addAttribute("mainPages", mainPages);
        return "home/references";
    }

    @GetMapping(path = "/services")
    public String services(Model model) {
        MainPages mainPages = homeService.getMainPages(PageType.SERVICES);
        model.addAttribute("mainPages", mainPages);
        return "home/services";
    }

    @GetMapping(path = "/loginpage")
    public String login(@RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        if (error != null) {
            model.addAttribute("message", "home.login.defeat");
        }
        if (logout != null) {
            model.addAttribute("message", "home.logout");
        }
        return "home/login";
    }

