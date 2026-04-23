package com.caryanam.caryanam_broker.entity;



import com.caryanam.caryanam_broker.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String fullName;
    private String mobileNumber;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

//    private boolean isPremiumActive;   // access control
//    private String paymentStatus;

    @Column(nullable = false)
    private boolean premiumActive = false;

    @Column(nullable = false)
    private String premiumStatus = "NONE";
}