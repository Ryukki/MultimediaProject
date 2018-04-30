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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNormalResolutionPath() {
        return normalResolutionPath;
    }

    public void setNormalResolutionPath(String normalResolutionPath) {
        this.normalResolutionPath = normalResolutionPath;
    }

    public String getMiniaturePath() {
        return miniaturePath;
    }

    public void setMiniaturePath(String miniaturePath) {
        this.miniaturePath = miniaturePath;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }
}
