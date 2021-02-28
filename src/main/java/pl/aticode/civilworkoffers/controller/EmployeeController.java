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

import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.UserService;

@Controller
@SessionAttributes(names = {"employee", "image", "editUserId"})
public class EmployeeController {

    private final static Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    
    @Autowired
    private UserService userService;
    @Autowired
    private CharFilterService charFilterService;
    @Autowired
    private OfferService offerService;


    /**
     * Get data for logged employee.
     * @param model
     * @return
     */
    @GetMapping(path = "/employee/main")
    public String main(Model model) {
        final List<OfferRequest> allEmployeeOfferRequests = offerService.getAllEmployeeOfferRequests();
        model.addAttribute("totalNoOfEmployeeOfferRequests", allEmployeeOfferRequests.size());
        return "employee/main";
    }

    /**
     * Get employee data to self edit.
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/employee/selfedit")
    public String selfEditEmployee(Model model) {
        Employee employee = userService.getLoggedEmployee();
        model.addAttribute("employee", employee);
        model.addAttribute("image", employee.getPhoto());
        model.addAttribute("editUserId", employee.getUser().getId());
        loadLanguages(userService, model);
        return "employee/selfEdit";
    }

    /**
     * Self edit employee
     * When employee change your user name, will be logout.
     *
     * @param employee
     * @param bindingResult
     * @param newPhoto
     * @param editUserId
     * @param image
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping(path = "/employee/selfedit")
    public String selfEditEmployee(@Valid Employee employee,
            BindingResult bindingResult,
            @RequestParam(name = "newPhoto") MultipartFile newPhoto,
            @SessionAttribute(name = "editUserId") Long editUserId,
            @SessionAttribute(name = "image") byte[] image,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (!userService.checkDistinctLoginWithEditUser(employee.getUser().getUsername(), editUserId)) {
                bindingResult.rejectValue("user.username", "user.username.distinct");
            }
            if (newPhoto.getBytes().length == 0) {
                employee.setPhoto(image);
            } else {
                if (newPhoto.getBytes().length > 50000) {
                    bindingResult.rejectValue("photo", "user.newphoto.size");
                } else {
                    employee.setPhoto(newPhoto.getBytes());
                }
            }
            if (!bindingResult.hasErrors()) {
                StringBuilder forbiddenCharMessage = new StringBuilder();
                charFilterService.doCharFilter(forbiddenCharMessage, employee);
                User loggedUser = userService.getLoggedUser();
                if(!loggedUser.getUsername().equals(employee.getUser().getUsername())) {
                    userService.selfEditEmployee(employee);
                    logger.info("Self edited Employee data: " + employee.getFirstName() + " " + employee.getLastName());
                    redirectAttributes.addFlashAttribute("message", "employee.success.edit.self");
                    redirectAttributes.addFlashAttribute("forbiddenCharMessage", forbiddenCharMessage);
                    return "redirect:/logoutpage";
                } else {
	                userService.selfEditEmployee(employee);
	                logger.info("Self edited Employee data: " + employee.getFirstName() + " " + employee.getLastName());
	                redirectAttributes.addFlashAttribute("message", "employee.success.edit.self");
	                redirectAttributes.addFlashAttribute("forbiddenCharMessage", forbiddenCharMessage);
	                return "redirect:/employee/main";
                }
            }
        } catch (Exception e) {
            logger.error("ERROR self edited employee: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("message", "employee.defeat.edit.self");
            return "redirect:/employee/main";
        }
        loadLanguages(userService, model);
        return "employee/selfEdit";
    }

//	PRIVATE METHODS
    private void loadLanguages(UserService userService, Model model) {
        String[] languages = userService.getLanguages();
        model.addAttribute("languages", languages);
    }

}
