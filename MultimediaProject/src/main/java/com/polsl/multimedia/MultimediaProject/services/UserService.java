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
    PasswordEncoder passwordEncoder;

    @PostConstruct
    private void createDummyUser(){
        AppUser dummyAppUser = new AppUser("user", passwordEncoder.encode("user"));
        userRepository.save(dummyAppUser);
    }

    public AppUser createUser(String username, String password){
        AppUser appUser = new AppUser(username, passwordEncoder.encode(password));
        return userRepository.save(appUser);
    }

    public AppUser getUserWithUsername(String username){
        AppUser appUser = new AppUser();
        if(userRepository.existsByUsername(username)){
            appUser = userRepository.findByUsername(username);
        }
        return appUser;
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
