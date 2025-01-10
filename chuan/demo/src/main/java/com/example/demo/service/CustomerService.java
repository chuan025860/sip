package com.example.demo.service;



import com.example.demo.bean.Customer;
import com.example.demo.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    private PasswordEncoder pwdEncoder;

    public Customer findByID(Integer loginID) {
        Optional<Customer> optional = customerRepository.findById(loginID);
        if (optional.isPresent()) {
            Customer customer = optional.get();
            return customer;
        } else {
            return null;
        }
    }


//    public Customer findCustomerByEmail(String email) {
//        Customer customer = customerRepository.findCustomerByEmail(email);
//        return customer;
//    }
//
//    public Boolean checkCustomerByEmail(String email) {
//        Customer customer = customerRepository.findCustomerByEmail(email);
//        return customer != null;
//    }

    public boolean insert(Customer customer) {
        try {
            String encodedPwd = pwdEncoder.encode(customer.getPassword());
            customer.setPassword(encodedPwd);
            customerRepository.save(customer);
            return true; // 成功保存時回傳 true
        } catch (Exception e) {
            e.printStackTrace(); // 可選：處理異常並記錄錯誤
            return false; // 發生錯誤時回傳 false
        }
    }

//    public Boolean resetPwd(String resetMail, String newPwd) {
//        Customer customer = customerRepository.findCustomerByEmail(resetMail);
//        if (customer != null) {
//            String encodedPwd = pwdEncoder.encode(newPwd);
//            customer.setPassword(encodedPwd);
//            customerRepository.save(customer);
//            return true;
//        }
//        return false;
//    }


    public Customer checkLogin(String phone, String inputPwd) {
        Customer customer = customerRepository.findCustomerByPhone(phone);
        //比對加密
        if (customer != null) {
            if (pwdEncoder.matches(inputPwd, customer.getPassword())) {
                return customer;
            }
        }
        return null;
    }

    public boolean  checkUserExists(String phone) {
        return  customerRepository.checkUserExists(phone);
    }

    public void updateCustomer(Customer customer) {
        customerRepository.save(customer);
    }



}
