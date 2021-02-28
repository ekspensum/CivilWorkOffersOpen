package pl.aticode.civilworkoffers.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.aticode.civilworkoffers.entity.offer.Offer;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestComment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestContent;

import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.Role;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.UserService;

@Repository
@Transactional(propagation = Propagation.REQUIRED)
public class UserRepositoryImpl implements UserRepository {

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        Session session = sessionFactory.getCurrentSession();
        return session;
    }

    @Override
    public void saveOwner(Owner owner) {
        getSession().persist(owner);
    }

    @Override
    public void updateOwner(Owner owner) {
        getSession().saveOrUpdate(owner);
    }

    @Override
    public Owner findOwner(long id) {
        return getSession().find(Owner.class, id);
    }

    @Override
    public Owner findOwner(String username) {
        Owner owner = null;
        try {
            owner = getSession().createNamedQuery("findOwnerByUserName", Owner.class).setParameter("username", username).getSingleResult();
        } catch (Exception e) {
            logger.error("For user: " + username + " {} ", e.getMessage());
        }
        return owner;
    }

    @Override
    public void saveEmployee(Employee employee) {
        getSession().persist(employee);
    }

    @Override
    public void updateEmployee(Employee employee) {
        getSession().saveOrUpdate(employee);
    }

    @Override
    public Employee findEmployee(long id) {
        return getSession().find(Employee.class, id);
    }

    @Override
    public Employee findEmployee(String username) {
        Employee employee = null;
        try {
            employee = getSession().createNamedQuery("findEmployeeByUserName", Employee.class).setParameter("username", username).getSingleResult();
        } catch (Exception e) {
            logger.error("For user: " + username + " {} ", e.getMessage());
        }
        return employee;
    }

    @Override
    public List<Employee> findAllEmployees() {
        return getSession().createQuery("from Employee", Employee.class).getResultList();
    }

    @Override
    public void saveCustomer(Customer customer) {
        getSession().persist(customer);
    }

    @Override
    public void updateCustomer(Customer customer) {
        getSession().saveOrUpdate(customer);
    }

    @Override
    public Customer findCustomer(long id) {
        return getSession().find(Customer.class, id);
    }

    @Override
    public Customer findCustomer(String username) {
        Customer customer = null;
        try {
            customer = getSession().createNamedQuery("findCustomerByUserName", Customer.class).setParameter("username", username).getSingleResult();
        } catch (Exception e) {
            logger.error("For user: " + username + " {} ", e.getMessage());
        }
        return customer;
    }

    @Override
    public List<Customer> findAllCustomers() {
        return getSession().createQuery("from Customer", Customer.class).getResultList();
    }

    @Override
    public void removeCustomer(Customer customer) {
        final List<OfferRequest> offerRequestList = getSession().createNamedQuery("findAllOfferRequestByCustomer", OfferRequest.class).setParameter("customer", customer).getResultList();
        for (OfferRequest offerRequest : offerRequestList) {
//            delete offer request contents            
            List<OfferRequestContent> offerRequestContentList = offerRequest.getOfferRequestContent();
            if (offerRequestContentList.size() > 0) {
                getSession().createNativeQuery("DELETE FROM offer_request_offer_request_content WHERE OfferRequest_id = :OfferRequest_id")
                        .setParameter("OfferRequest_id", offerRequest.getId()).executeUpdate();
                getSession().createNativeQuery("DELETE FROM offer_request_content WHERE id IN :offerRequestContentList")
                        .setParameterList("offerRequestContentList", offerRequestContentList).executeUpdate();
            }

//            delete offer request comments            
            List<OfferRequestComment> offerRequestCommentList = offerRequest.getOfferRequestComment();
            if (offerRequestCommentList.size() > 0) {
                getSession().createNativeQuery("DELETE FROM offer_request_offer_request_comment WHERE OfferRequest_id = :OfferRequest_id")
                        .setParameter("OfferRequest_id", offerRequest.getId()).executeUpdate();
                getSession().createNativeQuery("DELETE FROM offer_request_comment WHERE id IN :offerRequestCommentList")
                        .setParameterList("offerRequestCommentList", offerRequestCommentList).executeUpdate();
            }

//            delete offer request attachments
            List<OfferRequestAttachment> offerRequestAttachmentList = offerRequest.getOfferRequestAttachment();
            if (offerRequestAttachmentList.size() > 0) {
                getSession().createNativeQuery("DELETE FROM offer_request_offer_request_attachment WHERE OfferRequest_id = :OfferRequest_id")
                        .setParameter("OfferRequest_id", offerRequest.getId()).executeUpdate();
                getSession().createNativeQuery("DELETE FROM offer_request_attachment WHERE id IN :offerRequestAttachmentList")
                        .setParameterList("offerRequestAttachmentList", offerRequestAttachmentList).executeUpdate();

                OfferRequestAttachment offerRequestAttachment;
                for (int i = 0; i < offerRequestAttachmentList.size(); i++) {
                    offerRequestAttachment = offerRequestAttachmentList.get(i);
                    final Long byteAttachmentId = offerRequestAttachment.getByteAttachment().getId();
                    getSession().createNativeQuery("DELETE FROM byte_attachment WHERE id = :byteAttachmentId").setParameter("byteAttachmentId", byteAttachmentId).executeUpdate();
                }
            }
//            delete offers to offer request
            List<Offer> offerList = offerRequest.getOffer();
            if (offerList.size() > 0) {
                getSession().createNativeQuery("DELETE FROM offer_request_offer WHERE OfferRequest_id = :OfferRequest_id")
                        .setParameter("OfferRequest_id", offerRequest.getId()).executeUpdate();
                getSession().createNativeQuery("DELETE FROM offer_attachment WHERE offer_id IN :offerList").setParameterList("offerList", offerList).executeUpdate();
                getSession().createNativeQuery("DELETE FROM offer WHERE id IN :offerList").setParameterList("offerList", offerList).executeUpdate();
            }

//            delete offer request estimates
            getSession().createNativeQuery("DELETE FROM offer_request_estimate WHERE OfferRequest_id = :OfferRequest_id")
                    .setParameter("OfferRequest_id", offerRequest.getId()).executeUpdate();
        }

//        delete offer requests
        getSession().createNamedQuery("removeOfferRequestForCustomer").setParameter("customer", customer).executeUpdate();

//        delete customer and user
        getSession().createNamedQuery("removeCustomer").setParameter("customer", customer).executeUpdate();
        getSession().createNamedQuery("removeUser").setParameter("user", customer.getUser()).executeUpdate();
    }

    @Override
    public void saveRole(Role role) {
        getSession().persist(role);
    }

    @Override
    public Role findRole(long id) {
        return getSession().find(Role.class, id);
    }

    @Override
    public List<Role> findAllRoles() {
        return getSession().createQuery("from Role", Role.class).getResultList();
    }

    @Override
    public void updateUser(User user) {
        getSession().saveOrUpdate(user);
    }

    @Override
    public User findUser(String username) {
        User user = null;
        try {
            user = getSession().createNamedQuery("findUserByUserName", User.class).setParameter("username", username).getSingleResult();
        } catch (Exception e) {
            if (!username.equals("")) {
                logger.error("For user: " + username + " {} ", e.getMessage());
            }
        }
        return user;
    }

    @Override
    public User findUser(long id) {
        return getSession().find(User.class, id);
    }

    @Override
    public List<User> findAllUsers() {
        return getSession().createQuery("from User", User.class).getResultList();
    }

    @Override
    public User findSocialUser(String socialUserId) {
        User user = null;
        try {
            user = getSession().createNamedQuery("findUserBySocialUserId", User.class).setParameter("socialUserId", socialUserId).getSingleResult();
        } catch (Exception e) {
            if (!socialUserId.equals("")) {
                logger.error("For social user id: " + socialUserId + " {} ", e.getMessage());
            }
        }
        return user;
    }

    @Override
    public void saveCustomerType(CustomerType customerType) {
        getSession().persist(customerType);
    }

    @Override
    public void updateCustomerType(CustomerType customerType) {
        getSession().saveOrUpdate(customerType);
    }

    @Override
    public CustomerType findCustomerType(long id) {
        return getSession().find(CustomerType.class, id);
    }

    @Override
    public List<CustomerType> findAllCustomerTypes() {
        return getSession().createQuery("from CustomerType", CustomerType.class).getResultList();
    }

}
