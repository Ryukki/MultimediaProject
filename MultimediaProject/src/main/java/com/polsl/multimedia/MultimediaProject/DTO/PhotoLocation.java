package com.polsl.multimedia.MultimediaProject.DTO;

public class PhotoLocation {
    private String miniature;
    private Long photoId;
    private Double latitude;
    private Double longitude;

    public PhotoLocation(String miniature, Long photoId, Double latitude, Double longitude) {
        this.miniature = miniature;
        this.photoId = photoId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getMiniature() {
        return miniature;
    }

    public void setMiniature(String miniature) {
        this.miniature = miniature;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
