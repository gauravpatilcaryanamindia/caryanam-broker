package com.caryanam.caryanam_broker.entity;
import com.caryanam.caryanam_broker.enums.Role;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "P_Owner")
public class PropertyOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownerId;

    private String fullName;
    private String mobileNumber;
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

}

