package pl.aticode.civilworkoffers.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.User;

@Service
public class ActivationService {

    private final static Logger logger = LoggerFactory.getLogger(ActivationService.class);

    @Value(value = "${host}")
    private String host;

    @Autowired
    private HibernateSearchService searchsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CipherService cipherService;

    public ActivationService() {
    }

    public ActivationService(HibernateSearchService searchsService, UserRepository userRepository, CipherService cipherService) {
        this.searchsService = searchsService;
        this.userRepository = userRepository;
        this.cipherService = cipherService;
    }

    /**
     * Create activation link to use with registration customer.
     *
     * @param customer
     * @return
     */
    public String createActivationLink(Customer customer) {
	....................
    }

    /**
     * Method search customer by activation string. If found then set user
     * enabled and update customer in database.
     *
     * @param activationString
     * @return true if activation correct.
     */
    public boolean setActiveCustomer(String activationString) {
	...................
    }

}
