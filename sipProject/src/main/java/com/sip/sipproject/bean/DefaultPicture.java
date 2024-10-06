package com.sip.sipproject.bean;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.Bean;

@Entity
@Table(name = "DefaultPicture")
@Data
public class DefaultPicture {
    @Id
    @Column(name = "defaultPictureID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer defaultPictureID;

    @Column(name = "pictureName")
    private String pictureName;

    @Lob
    @Column(name = "imageData")
    private byte[] imageData;
}
