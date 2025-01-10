package com.example.demo.repository;


import com.example.demo.bean.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query(value="from Customer where customerName = :n")
     Customer findCustomerByCustomerName(@Param("n") String name);

    @Query(value="from Customer where phone = :phone")
    Customer findCustomerByPhone(@Param("phone") String phone);

    @Query(value="from Customer where phone = :phone")
    boolean checkUserExists(@Param("phone") String phone);

}
