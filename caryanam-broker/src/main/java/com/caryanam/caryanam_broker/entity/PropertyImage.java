package com.caryanam.caryanam_broker.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "property_images")
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imageName;
    private String imagePath;
    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    private Double originalSizeMb;
    private Long originalSizeKb;
    private Double compressedSizeMb;
    private Long compressedSizeKb;
}