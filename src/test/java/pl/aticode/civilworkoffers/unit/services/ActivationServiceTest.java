package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.ActivationService;
import pl.aticode.civilworkoffers.service.CipherService;
import pl.aticode.civilworkoffers.service.HibernateSearchService;

class ActivationServiceTest {
	
	@InjectMocks
	private ActivationService activationService;
	@Mock
	private CipherService cipherService;
	@Mock
	private HibernateSearchService hibernateSearchService;
	@Mock
	private UserRepository userRepository;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testCreateActivationLink() {
		Customer customer = new Customer();
		String activationString = "activationString123";
		customer.setActivationString(activationString);
		Mockito.when(cipherService.encodeString(activationString)).thenReturn(activationString);
		assertTrue(activationService.createActivationLink(customer).contains(activationString));
		assertTrue(activationService.createActivationLink(customer).contains("ACTIVATION LINK"));
	}

	@Test
	void testSetActiveCustomer() {
		User user = new User();
		Customer customer = new Customer();
		customer.setUser(user);
		customer.setRegisterDateTime(LocalDateTime.now().minusHours(1));
		Mockito.when(hibernateSearchService.searchCustomerToActivation("activationString")).thenReturn(customer);
		assertTrue(activationService.setActiveCustomer("activationString"));
		customer.setRegisterDateTime(LocalDateTime.now().minusHours(7));
		assertFalse(activationService.setActiveCustomer("activationString"));
	}

}
