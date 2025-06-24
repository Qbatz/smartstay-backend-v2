package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int addressId;
    private String houseNo;
    private String street;
    private String landMark;
    private String city;
    private String state;
    private int pincode;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users user;
}
