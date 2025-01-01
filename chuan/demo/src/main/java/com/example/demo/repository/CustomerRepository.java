package com.example.demo.repository;


import com.example.demo.bean.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query(value="from Customer where customerName = :n")
     Customer findCustomerByCustomerName(@Param("n") String name);
    @Query(value="from Customer where email = :email")
    Customer findCustomerByEmail(@Param("email") String email);

    @Query("FROM Customer WHERE LineID = :lineID")
    Customer findByLineID(@Param("lineID") String lineID);

    @Query(value="FROM Customer WHERE googleID = :googleID")
    Customer findByGoogleID(@Param("googleID") String googleID);

    @Modifying
    @Query(value="UPDATE Customer c SET c.googleID = NULL WHERE c.loginID = :loginID")
    void clearGoogleID(@Param("loginID") String loginID);

    @Modifying
    @Query(value="UPDATE Customer SET googleID = :newGoogleID WHERE loginID = :loginID")
    void bindGoogleID(@Param("loginID") String loginID, @Param("newGoogleID") String newGoogleID);

}
