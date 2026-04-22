package com.caryanam.caryanam_broker.entity;




import com.caryanam.caryanam_broker.Enum.BhkType;
import com.caryanam.caryanam_broker.Enum.FurnishingType;
import com.caryanam.caryanam_broker.Enum.PropertyType;
import jakarta.persistence.*;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private Double  price;
    private String location;
    private String description;
    private String mobileNumber;
    private Integer likesCount;
    private Integer viewsCount;
    private String status;
    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;
    @Enumerated(EnumType.STRING)
    private BhkType bhkType;
    @Enumerated(EnumType.STRING)
    private FurnishingType furnishing;
    private String carpetArea;
    private String coverImage;
    private String doctypeImages;
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;
   }