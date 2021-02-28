package pl.aticode.civilworkoffers.dao;

import java.util.List;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.CustomerType;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.Role;
import pl.aticode.civilworkoffers.entity.user.User;


public interface UserRepository {
	
	void saveOwner(Owner owner);
	void updateOwner(Owner owner);
	Owner findOwner(long id);
	Owner findOwner(String username);
	
	void saveEmployee(Employee employee);
	void updateEmployee(Employee employee);
	Employee findEmployee(long id);
	Employee findEmployee(String username);
	List<Employee> findAllEmployees();

	void saveCustomer(Customer customer);
	void updateCustomer(Customer customer);
	Customer findCustomer(long id);
	Customer findCustomer(String username);
	List<Customer> findAllCustomers();
	void removeCustomer(Customer customer);
	
	void saveRole(Role role);
	Role findRole(long id);
	List<Role> findAllRoles();
	
	void updateUser(User user);
	User findUser(String username);
	User findUser(long id);
	List<User> findAllUsers();
	User findSocialUser(String socialUserId);
	
	void saveCustomerType(CustomerType customerType);
	void updateCustomerType(CustomerType customerType);
	CustomerType findCustomerType(long id);
	List<CustomerType> findAllCustomerTypes();
}
