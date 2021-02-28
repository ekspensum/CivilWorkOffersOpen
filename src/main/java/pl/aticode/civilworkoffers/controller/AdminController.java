package pl.aticode.civilworkoffers.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;
import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.HibernateSearchService;
import pl.aticode.civilworkoffers.service.HomeService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.PasswordService;
import pl.aticode.civilworkoffers.service.UserService;

@Controller
@SessionAttributes(names = {"employee", "admin", "customer", "image", "editUserId", "mainPages", "logoSloganFooter", "logoSession"})
public class AdminController {

    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private CharFilterService charFilterService;
    @Autowired
    private HibernateSearchService hibernateSearchService;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private HomeService homeService;
    @Autowired
    private OfferService offerService;

    /**
     * Get data to display for administrator.
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/admin/main")
    public String main(Model model) {
        List<Employee> allEmployees = userService.getAllEmployees();
        model.addAttribute("numberOfEmployees", allEmployees.size());
        List<Customer> allCustomers = userService.getAllCustomers();
        model.addAttribute("numberOfCustomers", allCustomers.size());
        return "admin/main";
    }

    /**
     * Get data to add new employee.
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/admin/addemployee")
    public String addEmployee(Model model) {
        model.addAttribute("employee", new Employee());
        loadLanguages(userService, model);
        return "admin/user/addEmployee";
    }

    /**
     * Creates new employee.
     *
     * @param employee
     * @param bindingResult
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping(path = "/admin/addemployee")
    public String addEmployee(@Valid Employee employee, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (!userService.checkDistinctLoginWithRegisterUser(employee.getUser().getUsername())) {
            bindingResult.rejectValue("user.username", "user.username.distinct");
        }
        try {
            if (!bindingResult.hasErrors()) {
                StringBuilder forbiddenCharMessage = new StringBuilder();
                charFilterService.doCharFilter(forbiddenCharMessage, employee);
                userService.addNewEmployee(employee);
                logger.info("Added new Employee: " + employee.getFirstName() + " " + employee.getLastName());
                redirectAttributes.addFlashAttribute("message", "admin.success.add.employee");
                redirectAttributes.addFlashAttribute("forbiddenCharMessage", forbiddenCharMessage);
                return "redirect:/admin/main";
            }
        } catch (Exception e) {
            logger.error("ERROR add employee: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("message", "admin.defeat.add.employee");
            return "redirect:/admin/main";
        }
        loadLanguages(userService, model);
        return "admin/user/addEmployee";
    }


}
