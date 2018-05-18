package com.polsl.multimedia.MultimediaProject.DTO;

public class UploadExifBody {

    public PhotoParams photoParams;
    public Long photoId;


    public PhotoParams getPhotoParams() {
        return photoParams;
    }

    public void setPhotoParams(PhotoParams photoParams) {
        this.photoParams = photoParams;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }
}
