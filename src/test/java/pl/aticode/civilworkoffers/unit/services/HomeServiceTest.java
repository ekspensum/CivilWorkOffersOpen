package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import pl.aticode.civilworkoffers.dao.LogoSloganFooterRepository;
import pl.aticode.civilworkoffers.dao.MainPagesRepository;
import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;
import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;
import pl.aticode.civilworkoffers.service.HomeService;
import pl.aticode.civilworkoffers.service.UserService;

class HomeServiceTest {
	
	@InjectMocks
	private HomeService homeService;
	@Mock
	private MainPagesRepository mainPagesRepository;
	@Mock
	private LogoSloganFooterRepository logoSloganFooterRepository;
	@Spy
	private UserService userService;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testUpdateMainPagesRecord() {
		MainPages mainPages = new MainPages();
		mainPages.setContent("content");
		ArgumentCaptor<MainPages> argumentCaptor = ArgumentCaptor.forClass(MainPages.class);
		doNothing().when(mainPagesRepository).updateRecord(argumentCaptor.capture());
		homeService.updateMainPagesRecord(mainPages);
		assertEquals("content", argumentCaptor.getValue().getContent());
	}

	@Test
	void testGetMainPages() {
		MainPages mainPages = new MainPages();
		mainPages.setContent("content");
		Mockito.when(mainPagesRepository.findRecord(PageType.PORTFOLIO)).thenReturn(mainPages);
		assertEquals("content", homeService.getMainPages(PageType.PORTFOLIO).getContent());
	}

	@Test
	void testUpdateLogoSloganFooter() throws Exception {
		LogoSloganFooter logoSloganFooter = new LogoSloganFooter();
		logoSloganFooter.setFooter("footer");
		ArgumentCaptor<LogoSloganFooter> argumentCaptor = ArgumentCaptor.forClass(LogoSloganFooter.class);
		doNothing().when(logoSloganFooterRepository).updateRecord(argumentCaptor.capture());
		homeService.updateLogoSloganFooter(logoSloganFooter);
		assertEquals("footer", argumentCaptor.getValue().getFooter());
	}

	@Test
	void testGetLogoSloganFooter() {
		LogoSloganFooter logoSloganFooter = new LogoSloganFooter();
		logoSloganFooter.setSlogan("slogan");
		Mockito.when(logoSloganFooterRepository.findRecord(1)).thenReturn(logoSloganFooter);
		assertEquals("slogan", homeService.getLogoSloganFooter().getSlogan());
	}

}
