package com.polsl.multimedia.MultimediaProject.services;

import com.polsl.multimedia.MultimediaProject.models.Photo;
import com.polsl.multimedia.MultimediaProject.models.User;
import com.polsl.multimedia.MultimediaProject.repositories.PhotoRepository;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PhotoRepository photoRepository;

    public User getUserWithUsername(String username){
        User user = new User();
        if(userRepository.existsByUsername(username)){
            user = userRepository.findByUsername(username);
        }
        return user;
    }

    public boolean checkLoginCredentials(String username, String password){
        User user = getUserWithUsername(username);
        if(user!=null && user.getPassword().equals(password)){
            return true;
        }
        return false;
    }

    public List<Photo> getAllPhotos(User user){
        return user.getPhotos();
    }
}
