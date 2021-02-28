package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.DrawEmployeeService;
import pl.aticode.civilworkoffers.service.UserService;

class DrawEmployeeServiceTest {
	
	@InjectMocks
	private DrawEmployeeService drawEmployeeService;
	@Mock
	private UserService userService;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testDrawEmployee() throws Exception {
		User user1 = new User();
		user1.setEnabled(true);
		Employee employee1 = new Employee();
		employee1.setUser(user1);
		User user2 = new User();
		user2.setEnabled(true);		
		Employee employee2 = new Employee();
		employee2.setUser(user2);
		List<Employee> employeeList = new ArrayList<>();
		employeeList.add(employee1);
		employeeList.add(employee2);
		Mockito.when(userService.getAllEmployees()).thenReturn(employeeList);
		drawEmployeeService.drawEmployee();
		
		user1.setEnabled(false);
		drawEmployeeService.drawEmployee();
		
		user2.setEnabled(false);
		assertThrows(Exception.class, () -> drawEmployeeService.drawEmployee());
	}

}
