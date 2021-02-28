package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.CharFilterService.ActionType;
import pl.aticode.civilworkoffers.service.CharFilterService.CharFilter;

class CharFilterServiceTest {
	
	@InjectMocks
	private CharFilterService charFilterService;
	@Mock
	private MessageSource messageSource;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testDoCharFilter() throws Exception {
		ClassForTestCharFilter objectClassForTestCharFilter = new ClassForTestCharFilter();
		objectClassForTestCharFilter.setStringForTestReplace("something string with: { ");
		objectClassForTestCharFilter.setStringForTestRemove("something string with: [ ");
		
		String charToReplace = "?";
		Mockito.when(messageSource.getMessage("forbiddenCharMessage.charToReplace", null, LocaleContextHolder.getLocale())).thenReturn(charToReplace);
		
		StringBuilder forbiddenCharMessage = new StringBuilder();
		charFilterService.doCharFilter(forbiddenCharMessage, objectClassForTestCharFilter);
		
		assertTrue(objectClassForTestCharFilter.getStringForTestReplace().contains("?"));
		assertFalse(objectClassForTestCharFilter.getStringForTestRemove().contains("["));
	}

	
	class ClassForTestCharFilter {
		
		@CharFilter(forbiddenChar = "{", action = ActionType.REPLACE)
		String stringForTestReplace;
		
		@CharFilter(forbiddenChar = "[", action = ActionType.REMOVE)
		String stringForTestRemove;

		public String getStringForTestReplace() {
			return stringForTestReplace;
		}

		public void setStringForTestReplace(String stringForTestReplace) {
			this.stringForTestReplace = stringForTestReplace;
		}

		public String getStringForTestRemove() {
			return stringForTestRemove;
		}

		public void setStringForTestRemove(String stringForTestRemove) {
			this.stringForTestRemove = stringForTestRemove;
		}
	
	}
}
