package pl.aticode.civilworkoffers.unit.controllers;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pl.aticode.civilworkoffers.controller.OwnerController;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.Role;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.PasswordService;
import pl.aticode.civilworkoffers.service.UserService;

class OwnerControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private OwnerController ownerController;
    @Mock
    private UserService userService;
    @Mock
    private CharFilterService charFilterService;
    @Mock
    private PasswordService passwordService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();
    }

    @Test
    void testMain() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/owner/main"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/main"))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfAdmins", 0))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfEmployees", 0))
                .andExpect(MockMvcResultMatchers.model().attribute("numberOfCustomers", 0));
    }

    @Test
    void testAddAdminModel() throws Exception {
        String[] languages = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languages);
        mockMvc.perform(MockMvcRequestBuilders.get("/owner/addadmin"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/addAdmin"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("employee"))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", languages));
    }

    @Test
    void testAddAdminEmployeeBindingResultStringModelRedirectAttributes() throws Exception {
        String[] languages = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languages);
        Role role1 = new Role();
        role1.setId(3L);
        Role role2 = new Role();
        role2.setId(4L);
        List<Role> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        User user = new User();
        user.setRoles(roles);
        user.setUsername("username");
        user.setPasswordField("passwordField");
        Employee admin = new Employee();
        admin.setFirstName("firstName");
        admin.setLastName("lastName");
        admin.setEmail("email@email.pl");
        admin.setLanguage("pl");
        admin.setUser(user);
        Mockito.when(userService.checkDistinctLoginWithRegisterUser(admin.getUser().getUsername())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/addadmin")
                .sessionAttr("employee", admin))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/addAdmin"))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", languages));

        Mockito.when(userService.checkDistinctLoginWithRegisterUser(admin.getUser().getUsername())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/addadmin")
                .sessionAttr("employee", admin))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/owner/main"));

        Mockito.doThrow(new Exception("TEST")).when(userService).addNewAdmin(admin);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/addadmin")
                .sessionAttr("employee", admin))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/owner/main"));
    }

    @Test
    void testEditAdminModel() throws Exception {
        List<Employee> allAdmins = new ArrayList<>();
        Mockito.when(userService.getAllEmployees()).thenReturn(allAdmins);
        mockMvc.perform(MockMvcRequestBuilders.get("/owner/editadmin"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/editAdmin"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("allEmployees"));
    }

    @Test
    void testEditAdminEmployeeStringStringStringStringModelRedirectAttributes() throws Exception {
        String adminId = "3";
        String editAdminButton = "toEdit";
        String resetPasswordButton = "resetPass";
        Role role1 = new Role();
        role1.setId(3L);
        Role role2 = new Role();
        role2.setId(4L);
        List<Role> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        User user = new User();
        user.setRoles(roles);
        Employee admin = new Employee();
        admin.setUser(user);
        Mockito.when(userService.getEmployee(adminId)).thenReturn(admin);
        Mockito.when(userService.getRoleForAdminEmployee()).thenReturn(roles);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/editadmin")
                .param("adminId", adminId)
                .param("editAdminButton", editAdminButton)
                .param("resetPasswordButton", resetPasswordButton))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/editAdmin"))
                .andExpect(MockMvcResultMatchers.model().attribute("employee", admin));

        adminId = "0";
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/editadmin")
                .param("adminId", adminId)
                .param("editAdminButton", editAdminButton)
                .param("resetPasswordButton", resetPasswordButton)
                .sessionAttr("employee", admin))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/owner/main"));

        editAdminButton = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/editadmin")
                .param("adminId", adminId)
                .param("editAdminButton", editAdminButton)
                .param("resetPasswordButton", resetPasswordButton)
                .sessionAttr("employee", admin))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/editAdmin"))
                .andExpect(MockMvcResultMatchers.model().attribute("resetedPassword", "YES"));

        editAdminButton = "toEdit";
        Mockito.doThrow(new Exception("TEST")).when(userService).editAdmin(admin);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/editadmin")
                .param("editAdminButton", editAdminButton)
                .sessionAttr("employee", admin))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/owner/main"));

        Mockito.doThrow(new Exception("TEST")).when(passwordService).resetPassword(admin);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/editadmin")
                .param("resetPasswordButton", resetPasswordButton)
                .sessionAttr("employee", admin))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/editAdmin"));
    }

    @Test
    void testSelfEditOwnerModel() throws Exception {
        User user = new User();
        user.setId(11L);
        Owner owner = new Owner();
        owner.setUser(user);
        Mockito.when(userService.getLoggedOwner()).thenReturn(owner);
        mockMvc.perform(MockMvcRequestBuilders.get("/owner/selfedit"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/selfEdit"))
                .andExpect(MockMvcResultMatchers.model().attribute("owner", owner))
                .andExpect(MockMvcResultMatchers.model().attribute("editUserId", owner.getUser().getId()));
    }

    @Test
    void testSelfEditOwnerOwnerBindingResultIntegerModelRedirectAttributes() throws Exception {
        Integer editUserId = 12;
        User user = new User();
        user.setId(11L);
        user.setUsername("usernameOwner");
        user.setPassword("password");
        Owner owner = new Owner();
        owner.setFirstName("firstName");
        owner.setLastName("lastName");
        owner.setEmail("email@email.pl");
        owner.setLanguage("pl");
        owner.setUser(user);
        Mockito.when(userService.checkDistinctLoginWithEditUser(owner.getUser().getUsername(), editUserId)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/selfedit")
                .sessionAttr("editUserId", editUserId)
                .sessionAttr("owner", owner))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/selfEdit"));

        User loggedUser = new User();
        loggedUser.setUsername("otherUsername");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        Mockito.when(userService.checkDistinctLoginWithEditUser(owner.getUser().getUsername(), editUserId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/selfedit")
                .sessionAttr("editUserId", editUserId)
                .sessionAttr("owner", owner))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/logoutpage"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "owner.success.edit.self"));
        
        loggedUser.setUsername("usernameOwner");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/selfedit")
                .sessionAttr("editUserId", editUserId)
                .sessionAttr("owner", owner))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/owner/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "owner.success.edit.self"));

        Mockito.doThrow(new Exception("TEST")).when(userService).selfEditOwner(owner);
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/selfedit")
                .sessionAttr("editUserId", editUserId)
                .sessionAttr("owner", owner))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/owner/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "owner.defeat.edit.self"));

        owner.setFirstName("firstNameIsTooLong..........");
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/selfedit")
                .sessionAttr("editUserId", editUserId)
                .sessionAttr("owner", owner))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/selfEdit"));
        
        owner.setFirstName("firstName");
        owner.setCompanyName("ab"); // company name is too short
        mockMvc.perform(MockMvcRequestBuilders.post("/owner/selfedit")
                .sessionAttr("editUserId", editUserId)
                .sessionAttr("owner", owner))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("owner/selfEdit"));
    }

}
