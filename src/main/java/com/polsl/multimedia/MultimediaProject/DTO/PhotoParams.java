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
    private double longitude;
    private double latitude;


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

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
