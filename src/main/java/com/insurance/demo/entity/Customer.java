package com.insurance.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private String nomineeName;

    private String nomineeRelation;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}