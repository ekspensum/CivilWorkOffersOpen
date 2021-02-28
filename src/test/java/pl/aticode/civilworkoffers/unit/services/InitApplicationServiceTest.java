package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import pl.aticode.civilworkoffers.dao.LogoSloganFooterRepository;
import pl.aticode.civilworkoffers.dao.MainPagesRepository;
import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;
import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.Role;
import pl.aticode.civilworkoffers.service.InitApplicationService;

class InitApplicationServiceTest {
	
	@InjectMocks
	private InitApplicationService initApplicationService;
	@Mock
	private UserRepository userRepository;
	@Mock
	private MainPagesRepository homeRepository;
	@Mock
	private LogoSloganFooterRepository logoSloganFooterRepository;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testInsertBasicDataToDatabase() throws Exception {
		ArgumentCaptor<Role> argumentCaptor1 = ArgumentCaptor.forClass(Role.class);
		doNothing().when(userRepository).saveRole(argumentCaptor1.capture());
		initApplicationService.insertBasicDataToDatabase();
		assertEquals("ROLE_OWNER", argumentCaptor1.getValue().getRole());
		
		ArgumentCaptor<Owner> argumentCaptor2 = ArgumentCaptor.forClass(Owner.class);
		doNothing().when(userRepository).saveOwner(argumentCaptor2.capture());
		initApplicationService.insertBasicDataToDatabase();
		assertEquals("Imie ownera", argumentCaptor2.getValue().getFirstName());
		assertEquals("497190998", argumentCaptor2.getValue().getRegon());
		assertEquals("Grodzka", argumentCaptor2.getValue().getStreet());
		assertEquals("123-456-789", argumentCaptor2.getValue().getPhone());
		
		ArgumentCaptor<CustomerType> argumentCaptor3 = ArgumentCaptor.forClass(CustomerType.class);
		doNothing().when(userRepository).saveCustomerType(argumentCaptor3.capture());
		initApplicationService.insertBasicDataToDatabase();
		assertEquals("PRIVATE", argumentCaptor3.getValue().getCustomerTypeName());
		
		ArgumentCaptor<MainPages> argumentCaptor4 = ArgumentCaptor.forClass(MainPages.class);
		doNothing().when(homeRepository).saveRecord(argumentCaptor4.capture());
		initApplicationService.insertBasicDataToDatabase();
		assertEquals("Example content / Przykładowa treść / type: "+PageType.SERVICES, argumentCaptor4.getValue().getContent());
		
		ArgumentCaptor<LogoSloganFooter> argumentCaptor5 = ArgumentCaptor.forClass(LogoSloganFooter.class);
		doNothing().when(logoSloganFooterRepository).saveRecord(argumentCaptor5.capture());
		initApplicationService.insertBasicDataToDatabase();
		assertEquals("Slogan or name for My Company / Slogan lub nazwa dla Mojej Firmy", argumentCaptor5.getValue().getSlogan());
	}

}
