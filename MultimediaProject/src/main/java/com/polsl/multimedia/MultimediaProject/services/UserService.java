package com.polsl.multimedia.MultimediaProject.services;

import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.models.Photo;
import com.polsl.multimedia.MultimediaProject.repositories.PhotoRepository;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PhotoService photoService;

    @PostConstruct
    private void createDummyUser(){
        AppUser dummyAppUser = new AppUser(passwordEncoder.encode("user"), "user");
        userRepository.save(dummyAppUser);
    }

    public AppUser createUser(String username, String password){
        AppUser appUser = new AppUser(username, passwordEncoder.encode(password));
        appUser = userRepository.save(appUser);
        return appUser;
    }

    public AppUser getUserWithUsername(String username){
        AppUser appUser = null;
        if(userRepository.existsByUsername(username)){
            appUser = userRepository.findByUsername(username);
        }
        return appUser;
    }

    public String getUsersPhotoPath(AppUser appUser, Long photoId){
        Photo photo = photoService.getPhotoWithId(photoId);
        if(photo!=null){
            List<Photo> photoList = appUser.getPhotos();
            if(photoList.contains(photo)){
                return photo.getNormalResolutionPath();
            }
        }
        return "";
    }

    public boolean checkLoginCredentials(String username, String password){
        AppUser appUser = getUserWithUsername(username);
        if(appUser !=null && appUser.getPassword().equals(password)){
            return true;
        }
        return false;
    }

    public List<Photo> getAllPhotos(AppUser appUser){
        return appUser.getPhotos();
    }
}
