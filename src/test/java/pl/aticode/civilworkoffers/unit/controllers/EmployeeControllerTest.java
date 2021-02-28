package pl.aticode.civilworkoffers.unit.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pl.aticode.civilworkoffers.controller.EmployeeController;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.UserService;

class EmployeeControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private EmployeeController employeeController;
    @Mock
    private UserService userService;
    @Mock
    private OfferService offerService;
    @Mock
    private CharFilterService charFilterService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    void testMain() throws Exception {
    	List<OfferRequest> allEmployeeOfferRequests = new ArrayList<>();
    	allEmployeeOfferRequests.add(new OfferRequest());
        Mockito.when(offerService.getAllEmployeeOfferRequests()).thenReturn(allEmployeeOfferRequests);
        mockMvc.perform(MockMvcRequestBuilders.get("/employee/main"))
        		.andExpect(MockMvcResultMatchers.status().isOk())
        		.andExpect(MockMvcResultMatchers.view().name("employee/main"))
        		.andExpect(MockMvcResultMatchers.model().attribute("totalNoOfEmployeeOfferRequests", allEmployeeOfferRequests.size()));
    }

    @Test
    void testSelfEditEmployeeModel() throws Exception {
        User user = new User();
        user.setId(13L);
        Employee employeeExpect = new Employee();
        byte[] imageExpect = new byte[10];
        employeeExpect.setPhoto(imageExpect);
        employeeExpect.setUser(user);
        Mockito.when(userService.getLoggedEmployee()).thenReturn(employeeExpect);
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.get("/employee/selfedit"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("employee/selfEdit"))
                .andReturn().getModelAndView().getModel();

        Employee employeeActual = (Employee) model.get("employee");
        assertEquals(13, employeeActual.getUser().getId());
        byte[] imageActualt = (byte[]) model.get("image");
        assertEquals(10, imageActualt.length);
        long editUserId = (long) model.get("editUserId");
        assertEquals(13, editUserId);
        String[] languagesActual = (String[]) model.get("languages");
        assertEquals("pl", languagesActual[0]);
    }

    @Test
    void testSelfEditEmployeeEmployeeBindingResultMultipartFileIntegerByteArrayModelRedirectAttributes() throws Exception {
        User user = new User();
        user.setUsername("username123");
        user.setPasswordField("passwordField123");
        Employee employeeExpect = new Employee();
        employeeExpect.setFirstName("firstName");
        employeeExpect.setLastName("lastName");
        employeeExpect.setEmail("email@email.pl");
        employeeExpect.setLanguage("pl");
        byte[] image = "photoFile".getBytes();
        employeeExpect.setPhoto(image);
        employeeExpect.setUser(user);
        String[] languagesExpect = {"pl", "en"};
        Mockito.when(userService.getLanguages()).thenReturn(languagesExpect);
        MockMultipartFile photo = new MockMultipartFile("newPhoto", "".getBytes());
        Integer editUserIdExpect = 33;
        Mockito.when(userService.checkDistinctLoginWithEditUser(employeeExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(false);
        Map<String, Object> model = mockMvc.perform(MockMvcRequestBuilders.multipart("/employee/selfedit")
                .file(photo)
                .sessionAttr("employee", employeeExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("employee/selfEdit"))
                .andReturn().getModelAndView().getModel();
        String[] languagesActual = (String[]) model.get("languages");
        assertEquals("pl", languagesActual[0]);
        Employee employeeActual = (Employee) model.get("employee");
        assertEquals(9, employeeActual.getPhoto().length);
        assertEquals("username123", employeeActual.getUser().getUsername());
        Integer editUserIdActual = (Integer) model.get("editUserId");
        assertEquals(33, editUserIdActual);

        photo = new MockMultipartFile("newPhoto", new byte[600001]);
        Mockito.when(userService.checkDistinctLoginWithEditUser(employeeExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/employee/selfedit")
                .file(photo)
                .sessionAttr("employee", employeeExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("employee/selfEdit"));

        User loggedUser = new User();
        loggedUser.setUsername("otherUsername");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        photo = new MockMultipartFile("newPhoto", "file".getBytes());
        Mockito.when(userService.checkDistinctLoginWithEditUser(employeeExpect.getUser().getUsername(), editUserIdExpect)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/employee/selfedit")
                .file(photo)
                .sessionAttr("employee", employeeExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/logoutpage"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "employee.success.edit.self"));
        
        loggedUser.setUsername("username123");
        Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/employee/selfedit")
                .file(photo)
                .sessionAttr("employee", employeeExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "employee.success.edit.self"));

        Mockito.doThrow(new Exception()).when(userService).selfEditEmployee(employeeActual);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/employee/selfedit")
                .file(photo)
                .sessionAttr("employee", employeeExpect)
                .sessionAttr("image", image)
                .sessionAttr("editUserId", editUserIdExpect))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
                .andExpect(MockMvcResultMatchers.flash().attribute("message", "employee.defeat.edit.self"));
    }

}
