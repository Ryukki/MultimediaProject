package com.polsl.multimedia.MultimediaProject.DTO;

import java.util.List;

/**
 * Created by Ryukki on 01.06.2018.
 */
public class FilterParams {
    private Boolean sortAsc;
    private List<String> authors;
    private List<String> photoNames;
    private List<String> cameraNames;
    private List<String> exposureList;
    private List<String> maxApertureList;
    private List<String> focalLengthList;
    private List<Double> longitudeList;
    private List<Double> latitudeList;

    public Boolean getSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(Boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<String> getPhotoNames() {
        return photoNames;
    }

    public void setPhotoNames(List<String> photoNames) {
        this.photoNames = photoNames;
    }

    public List<String> getCameraNames() {
        return cameraNames;
    }

    public void setCameraNames(List<String> cameraNames) {
        this.cameraNames = cameraNames;
    }

    public List<String> getExposureList() {
        return exposureList;
    }

    public void setExposureList(List<String> exposureList) {
        this.exposureList = exposureList;
    }

    public List<String> getMaxApertureList() {
        return maxApertureList;
    }

    public void setMaxApertureList(List<String> maxApertureList) {
        this.maxApertureList = maxApertureList;
    }

    public List<String> getFocalLengthList() {
        return focalLengthList;
    }

    public void setFocalLengthList(List<String> focalLengthList) {
        this.focalLengthList = focalLengthList;
    }

    public List<Double> getLongitudeList() {
        return longitudeList;
    }

    public void setLongitudeList(List<Double> longitudeList) {
        this.longitudeList = longitudeList;
    }

    public List<Double> getLatitudeList() {
        return latitudeList;
    }

    public void setLatitudeList(List<Double> latitudeList) {
        this.latitudeList = latitudeList;
    }
}
