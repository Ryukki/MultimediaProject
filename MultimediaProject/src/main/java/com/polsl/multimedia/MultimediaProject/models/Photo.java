package com.polsl.multimedia.MultimediaProject.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Ryukki on 20.04.2018.
 */

@Entity
public class Photo {
    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "photoName")
    private String photoName;

    @Column(name = "date")
    private Date date;

    @Column(name = "normalResolutionPath")
    private String normalResolutionPath;

    @Column(name = "miniaturePath")
    private String miniaturePath;

    @Column(name = "cameraName")
    private String cameraName;


}
