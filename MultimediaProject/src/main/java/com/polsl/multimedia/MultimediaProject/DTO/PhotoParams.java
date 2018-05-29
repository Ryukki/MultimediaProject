package com.polsl.multimedia.MultimediaProject.DTO;

import com.polsl.multimedia.MultimediaProject.models.Photo;

import java.util.Date;

/**
 * Created by Ryukki on 17.05.2018.
 */
public class PhotoParams {
    private Long id;
    private String photoName;
    private Date date;
    private String cameraName;
    private String exposure;
    private String maxAperture;
    private String focalLength;
    private Double longitude;
    private Double latitude;
    private String author;
    private String description;

    public PhotoParams() {
    }

    public PhotoParams(Photo photo) {
        id = photo.getId();
        photoName = photo.getPhotoName();
        date = photo.getDate();
        cameraName = photo.getCameraName();
        exposure = photo.getExposure();
        maxAperture = photo.getMaxAperture();
        focalLength = photo.getFocalLength();
        longitude = photo.getLongitude();
        latitude = photo.getLatitude();
        author = photo.getAuthor();
        description = photo.getDescription();
    }

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

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getExposure() {
        return exposure;
    }

    public void setExposure(String exposure) {
        this.exposure = exposure;
    }

    public String getMaxAperture() {
        return maxAperture;
    }

    public void setMaxAperture(String maxAperture) {
        this.maxAperture = maxAperture;
    }

    public String getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(String focalLength) {
        this.focalLength = focalLength;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
