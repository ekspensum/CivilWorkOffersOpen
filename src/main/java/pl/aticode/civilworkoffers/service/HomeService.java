package pl.aticode.civilworkoffers.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import pl.aticode.civilworkoffers.dao.LogoSloganFooterRepository;
import pl.aticode.civilworkoffers.dao.MainPagesRepository;
import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;
import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;
import pl.aticode.civilworkoffers.entity.user.User;

@Service
public class HomeService {
	
	private final static Logger logger = LoggerFactory.getLogger(UserService.class); 

	@Autowired
	private MainPagesRepository mainPagesRepository;
	@Autowired
	private LogoSloganFooterRepository logoSloganFooterRepository;
	@Autowired
	private UserService userService;
	
	/**
	 * Method update selected record MainPages depends on id. 
	 * MainPages records saved to database during initialize application (post construct) - see InitApplicationService.  
	 * @param mainPages
	 */
	@CacheEvict(cacheNames = "mainPagesCache", allEntries = true)
	public void updateMainPagesRecord(MainPages mainPages) {
		User loggedUser = userService.getLoggedUser();
		mainPages.setUserEdit(loggedUser);
		mainPages.setEditDateTime(LocalDateTime.now().withNano(0));
		mainPagesRepository.updateRecord(mainPages);
		logger.info("Update MainPages record type: {}", mainPages.getPageType());
	}
	
	/**
	 * Returns main page record depends on selected PageType enum.
	 * @param pageType
	 * @return MainPages one record/object.
	 */
	@Cacheable(cacheNames = "mainPagesCache")
	public MainPages getMainPages(PageType pageType) {
		logger.info("Get MainPages record type {} from database without caching.", pageType);
		return mainPagesRepository.findRecord(pageType);
	}
	
	/**
	 * Method update record LogoSloganFooter. 
	 * In database is only one record LogoSloganFooter which saved during initialize application (post construct) - see InitApplicationService.
	 * @param logoSloganFooter
	 */
	@CacheEvict(cacheNames = "logoSloganFooterCache", allEntries = true)
	public void updateLogoSloganFooter(LogoSloganFooter logoSloganFooter) throws Exception {
		User loggedUser = userService.getLoggedUser();
		logoSloganFooter.setUserEdit(loggedUser);
		logoSloganFooter.setEditDateTime(LocalDateTime.now().withNano(0));
		logoSloganFooterRepository.updateRecord(logoSloganFooter);
		logger.info("Update LogoSloganFooter record.");
	}
	
	/**
	 * Returns LogoSloganFooter.
	 * @return LogoSloganFooter.
	 */
	@Cacheable(cacheNames = "logoSloganFooterCache")
	public LogoSloganFooter getLogoSloganFooter() {
		logger.info("Get LogoSloganFooter record from database without caching.");
		return logoSloganFooterRepository.findRecord(1);
	}
}
