package pl.aticode.civilworkoffers.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pl.aticode.civilworkoffers.dao.LogoSloganFooterRepository;
import pl.aticode.civilworkoffers.dao.MainPagesRepository;
import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;
import pl.aticode.civilworkoffers.entity.home.MainPages;
import pl.aticode.civilworkoffers.entity.home.PageType;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.Role;
import pl.aticode.civilworkoffers.entity.user.User;

/**
 * Class for save to database basic data needs to working this application.
 *
 * @author aticode.pl
 *
 */
@Service
public class InitApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(InitApplicationService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MainPagesRepository homeRepository;
    @Autowired
    private LogoSloganFooterRepository logoSloganFooterRepository;

    /**
     * Insert basic data to database. Basic data are necessary to start
     * application for new application owner.
     *
     * @throws Exception
     */
    @PostConstruct
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertBasicDataToDatabase() throws Exception {
 
	..............................................
    }
}
