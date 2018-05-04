package com.polsl.multimedia.MultimediaProject.services;

import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.models.Photo;
import com.polsl.multimedia.MultimediaProject.repositories.PhotoRepository;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    public Photo getPhotoWithId(Long id){
        Optional<Photo> photo = photoRepository.findById(id);
        if(photo.isPresent()){
            return photo.orElse(new Photo());
        }
        return null;
    }

    public Long addPhoto(AppUser appUser, MultipartFile multipartFile) throws IOException {
        Photo photo = new Photo();

        String filePath = "resources/" + appUser.getId();
        new File(filePath).mkdirs();
        filePath +=  "/" + multipartFile.getOriginalFilename();

        Photo checkForExistingEntry = photoRepository.findByNormalResolutionPath(filePath);
        if(checkForExistingEntry!=null){
            System.out.println("photo with path \"" + filePath + "\" already in DB");
            return -1L;//photo with this path already in DB
        }

        photo.setNormalResolutionPath(filePath);
        photo = photoRepository.save(photo);
        appUser.getPhotos().add(photo);
        userRepository.save(appUser);

        File photoFile = new File(filePath);
        boolean newFile = photoFile.createNewFile();
        if(!newFile){
            System.out.println("File " + filePath + " already exists");
            return -2L;//file already existed
        }
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(multipartFile.getBytes());
        fos.close();

        return photo.getId();
    }
}
