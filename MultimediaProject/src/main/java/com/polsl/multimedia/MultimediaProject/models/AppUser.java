package com.polsl.multimedia.MultimediaProject.models;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Ryukki on 20.04.2018.
 */

@Entity
public class AppUser implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id", unique = true)
    private Long id;
    @Basic(optional = false)
    @Column(name = "password")
    private String password;
    @Basic(optional = false)
    @Column(name = "username", unique = true)
    private String username;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userID")
    private List<Photo> photoCollection;

    public AppUser() {
    }

    public AppUser(Long id) {
        this.id = id;
    }

    public AppUser(Long id, String username, String password) {
        this.id = id;
        this.password = password;
        this.username = username;
    }

    public AppUser(String username, String password) {
        this.password = password;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlTransient
    public List<Photo> getPhotos() {
        return photoCollection;
    }

    public void setPhotos(List<Photo> photoCollection) {
        this.photoCollection = photoCollection;
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
        if (!(object instanceof AppUser)) {
            return false;
        }
        AppUser other = (AppUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "javaapplication1.AppUser[ id=" + id + " ]";
    }

}
