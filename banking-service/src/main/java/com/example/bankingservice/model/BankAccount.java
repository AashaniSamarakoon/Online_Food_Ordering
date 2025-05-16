//package com.example.bankingservice.model;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//@Entity
//@Table(name = "bank_accounts")
//@Getter
//@Setter
//public class BankAccount {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String bankName;
//
//    @Column(nullable = false, unique = true)
//    private String accountNumber;
//
//    @Column(nullable = false)
//    private String routingNumber;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private AccountType accountType;
//
//    @Column(nullable = false)
//    private double balance;
//}

package com.example.bankingservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String routingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private double balance = 0.0;



}