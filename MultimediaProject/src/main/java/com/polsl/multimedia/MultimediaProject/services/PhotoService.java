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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Service
public class PhotoService {

    @Autowired
    PhotoRepository photoRepository;

    @Autowired
    UserRepository userRepository;

    public Photo getPhotoWithId(Long id){
        Optional<Photo> photo = photoRepository.findById(id);
        if(photo.isPresent()){
            return photo.orElse(new Photo());
        }
        return null;
    }

    public Long addPhoto(AppUser appUser, MultipartFile multipartFile){
        Photo photo = new Photo();
        String path = PhotoService.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            String decodedPath = "resources/" + appUser.getId();//URLDecoder.decode(path, "UTF-8") + "resources/" + appUser.getId();
            new File(decodedPath).mkdirs();
            decodedPath +=  "/" + multipartFile.getOriginalFilename();
            System.out.println(decodedPath);
            File photoFile = new File(decodedPath);
            photoFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(photoFile);
            fos.write(multipartFile.getBytes());
            fos.close();
            photo.setNormalResolutionPath(decodedPath);
            photo = photoRepository.save(photo);
            List<Photo> listOfUserPhotos = appUser.getPhotos();
            listOfUserPhotos.add(photo);
            appUser.setPhotos(listOfUserPhotos);
            userRepository.save(appUser);
            return photo.getId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1L;
    }
}
