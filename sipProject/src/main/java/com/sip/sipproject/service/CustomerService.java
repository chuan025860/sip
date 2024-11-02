package com.sip.sipproject.service;

import com.sip.sipproject.bean.Customer;
import com.sip.sipproject.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    //是否有googleID
    public boolean checkGoogleIdExistByLoginId(Integer loginId) {
        Optional<Customer> optional = customerRepository.findById(loginId);
        if (optional.isPresent()) {
            Customer customer = optional.get();
            return customer.getGoogleID() != null;
        } else {
            return false;
        }
    }
    //是否有LineID
    public boolean checkLineIdExistByLoginId(Integer loginId) {
        Optional<Customer> optional = customerRepository.findById(loginId);
        if (optional.isPresent()) {
            Customer customer = optional.get();
            return customer.getLineID() != null;
        } else {
            return false;
        }
    }
    public Customer findCustomerByEmail(String email) {
        Customer customer = customerRepository.findCustomerByEmail(email);
        return customer;
    }

    public Boolean checkCustomerByEmail(String email) {
        Customer customer = customerRepository.findCustomerByEmail(email);
        return customer != null;
    }

    public void insert(Customer customer) {
        String encodedPwd = pwdEncoder.encode(customer.getPassword());
        customer.setPassword(encodedPwd);
        customerRepository.save(customer);
    }

    public Boolean resetPwd(String resetMail, String newPwd) {
        Customer customer = customerRepository.findCustomerByEmail(resetMail);
        if (customer != null) {
            String encodedPwd = pwdEncoder.encode(newPwd);
            customer.setPassword(encodedPwd);
            customerRepository.save(customer);
            return true;
        }
        return false;
    }

    public Customer findLineID(String LineID) {
        return customerRepository.findByLineID(LineID);
    }

    public Customer checkLogin(String email, String inputPwd) {
        Customer customer = customerRepository.findCustomerByEmail(email);
        //比對加密
        if (customer != null) {
            if (pwdEncoder.matches(inputPwd, customer.getPassword())) {
                return customer;
            }
        }
        return null;
    }

    public void updateCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    //使用googleID 取得Customer
    public Customer oauth2CheckLogin(String googleID) {//google登入
        return customerRepository.findByGoogleID(googleID);
    }



    @Transactional //清除googleID
    public void clearGoogleID(String loginID) {//清除googleID
        customerRepository.clearGoogleID(loginID);
    }

    @Transactional //新增googleID
    public void bindGoogleID(String loginID, String newGoogleID) {
        customerRepository.bindGoogleID(loginID, newGoogleID);
    }

}
