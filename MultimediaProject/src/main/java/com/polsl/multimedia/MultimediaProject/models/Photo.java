package com.polsl.multimedia.MultimediaProject.models;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Ryukki on 20.04.2018.
 */

@Entity
@NamedQueries({
        @NamedQuery(name = "Photo.findAll", query = "SELECT p FROM Photo p")
        , @NamedQuery(name = "Photo.findById", query = "SELECT p FROM Photo p WHERE p.id = :id")
        , @NamedQuery(name = "Photo.findByCameraName", query = "SELECT p FROM Photo p WHERE p.cameraName = :cameraName")
        , @NamedQuery(name = "Photo.findByDate", query = "SELECT p FROM Photo p WHERE p.date = :date")
        , @NamedQuery(name = "Photo.findByMiniaturePath", query = "SELECT p FROM Photo p WHERE p.miniaturePath = :miniaturePath")
        , @NamedQuery(name = "Photo.findByNormalResolutionPath", query = "SELECT p FROM Photo p WHERE p.normalResolutionPath = :normalResolutionPath")
        , @NamedQuery(name = "Photo.findByPhotoName", query = "SELECT p FROM Photo p WHERE p.photoName = :photoName")})
public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "camera_name")
    private String cameraName;
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name = "miniature_path")
    private String miniaturePath;
    @Column(name = "normal_resolution_path")
    private String normalResolutionPath;
    @Column(name = "photo_name")
    private String photoName;
    @JoinColumn(name = "userID", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private AppUser userID;

    public Photo() {
    }

    public Photo(Long id) {
        this.id = id;
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
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "javaapplication1.Photo[ id=" + id + " ]";
    }

}