/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.aticode.civilworkoffers.service;

import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.aticode.civilworkoffers.entity.user.Employee;

/**
 *
 * @author aticode.pl
 */
@Service
public class DrawEmployeeService {
    
    private final static Logger logger = LoggerFactory.getLogger(DrawEmployeeService.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * Draw active employee to make offer for offer request when number of active employees is more than 1. 
     * @return drawn employee to make offer. 
     * @throws Exception
     */
    public Employee drawEmployee() throws Exception {
        final List<Employee> allEmployees = userService.getAllEmployees();
        for (int i = 0; i < allEmployees.size(); i++) {
            if(!allEmployees.get(i).getUser().isEnabled()){
                allEmployees.remove(i);
            }
        }
        if(allEmployees.size() > 1){
            Random random = new Random();
            final int randomIdx = random.nextInt(allEmployees.size());
            return allEmployees.get(randomIdx);
        } else if(allEmployees.size() == 1){
            return allEmployees.get(0);
        } else {
            String msg = "No regisered or active employee.";
            logger.error(msg);
            throw new Exception(msg);
        }
    }
}
