package com.polsl.multimedia.MultimediaProject.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ryukki on 20.04.2018.
 */

@Entity
public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "miniature_path")
    private String miniaturePath;

    @Column(name = "normal_resolution_path")
    private String normalResolutionPath;

    @Column(name = "photo_name")
    private String photoName;

    @Column(name = "author")
    private String author;

    @Column(name = "description")
    private String description;

    @JoinColumn(name = "userID", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private AppUser userID;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "camera_name")
    private String cameraName;

    @Column(name = "exposure")
    private String exposure;

    @Column(name = "aperture")
    private String maxAperture;

    @Column(name = "focal_length")
    private String focalLength;

    @Column(name ="longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    public Photo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMiniaturePath() {
        return miniaturePath;
    }

    public void setMiniaturePath(String miniaturePath) {
        this.miniaturePath = miniaturePath;
    }

    public String getNormalResolutionPath() {
        return normalResolutionPath;
    }

    public void setNormalResolutionPath(String normalResolutionPath) {
        this.normalResolutionPath = normalResolutionPath;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public AppUser getUserID() {
        return userID;
    }

    public void setUserID(AppUser userID) {
        this.userID = userID;
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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Photo)) {
            return false;
        }
        Photo other = (Photo) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "javaapplication1.Photo[ id=" + id + " ]";
    }

}