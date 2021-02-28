package pl.aticode.civilworkoffers.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;

import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.service.ActivationService;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.CipherService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.ReCaptchaService;
import pl.aticode.civilworkoffers.service.RemoveCustomerQuartzJobService;
import pl.aticode.civilworkoffers.service.RemoveCustomerQuartzJobService.RemoveCustomerStatus;
import pl.aticode.civilworkoffers.service.UserService;

@Controller
@SessionAttributes(names = {"customer", "image", "editUserId"})
public class CustomerController {

    private final static Logger logger = LoggerFactory.getLogger(CustomerController.class);

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
    private ReCaptchaService reCaptchaService;
    @Autowired
    private RemoveCustomerQuartzJobService removeCustomerQuartzJobService;

    public CustomerController() {
    }

    public CustomerController(UserService userService, OfferService offerService, CharFilterService charFilterService, CipherService cipherService,
            ActivationService activationService, ReCaptchaService reCaptchaService,
            RemoveCustomerQuartzJobService removeCustomerQuartzJobService) {
        this.userService = userService;
        this.offerService = offerService;
        this.charFilterService = charFilterService;
        this.cipherService = cipherService;
        this.activationService = activationService;
        this.reCaptchaService = reCaptchaService;
        this.removeCustomerQuartzJobService = removeCustomerQuartzJobService;
    }
 
    /**
     * Get data for logged customer.
     * @param model
     * @return 
     */
    @GetMapping(path = "/customer/main")
    public String mainPage(Model model) {
        final List<OfferRequest> allCustomerOfferRequests = offerService.getAllCustomerOfferRequests();
        model.addAttribute("totalNoOfCustomerOfferRequests", allCustomerOfferRequests.size());
        return "customer/main";
    }
    
    /**
     * Get data to add new customer.
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/customer/registercustomer")
    public String registerCustomer(Model model) {
        model.addAttribute("customer", new Customer());
        loadAllCustomerTypes(userService, model);
        loadLanguages(userService, model);
        return "customer/registerCustomer";
    }

    /**
     * Create new customer.
     *
     * @param customer
     * @param bindingResult
     * @param reCaptchaResponse
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping(path = "/customer/registercustomer")
    public String addCustomer(@Valid Customer customer,
            BindingResult bindingResult,
            @RequestParam(name = "g-recaptcha-response") String reCaptchaResponse,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (!userService.checkDistinctLoginWithRegisterUser(customer.getUser().getUsername())) {
                bindingResult.rejectValue("user.username", "user.username.distinct");
            }
            if (customer.getCustomerType().getId() != 3 && !customer.getRegon().matches("^[0-9]{9}$")) {
                bindingResult.rejectValue("regon", "customer.regon.false");
            }
            if (customer.getCustomerType().getId() != 3 && !customer.getCompanyName().matches("^.{3,50}$")) {
                bindingResult.rejectValue("companyName", "customer.company.size");
            }
            if (!reCaptchaService.verify(reCaptchaResponse)) {
                bindingResult.rejectValue("recaptcha", "customer.recaptcha.error");
            }
            if (!bindingResult.hasErrors()) {
                StringBuilder forbiddenCharMessage = new StringBuilder();
                charFilterService.doCharFilter(forbiddenCharMessage, customer);
                userService.addNewCustomer(customer, false);
                logger.info("Registered new customer: " + customer.getFirstName() + " " + customer.getLastName());
                redirectAttributes.addFlashAttribute("message", "customer.success.add");
                redirectAttributes.addFlashAttribute("forbiddenCharMessage", forbiddenCharMessage);
                return "redirect:/customer/registerconfirm";
            }
        } catch (Exception e) {
            logger.error("ERROR register new customer: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("message", "customer.defeat.add");
            return "redirect:/customer/registerconfirm";
        }
        loadAllCustomerTypes(userService, model);
        loadLanguages(userService, model);
        return "customer/registerCustomer";
    }


