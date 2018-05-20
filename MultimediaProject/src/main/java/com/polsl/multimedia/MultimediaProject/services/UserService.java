package com.polsl.multimedia.MultimediaProject.services;

import com.google.protobuf.ByteString;
import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.models.Photo;
import com.polsl.multimedia.MultimediaProject.repositories.PhotoRepository;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import javafx.util.converter.ByteStringConverter;
import org.apache.logging.log4j.util.Chars;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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

    public Photo getUsersPhoto(AppUser appUser, Long photoId){
        Photo photo = photoService.getPhotoWithId(photoId);
        if(photo!=null){
            List<Photo> photoList = appUser.getPhotos();
            if(photoList.contains(photo)){
                return photo;
            }
        }
        //TODO throw exception and handle it in controller
        return photo;
    }

    public List<Photo> getUsersPhoto(AppUser appuser) {
        return appuser.getPhotos();
    }

    public boolean userHasPhoto(AppUser appUser, Photo photo){
        List<Photo> photos = appUser.getPhotos();
        if(photos.contains(photo)){
            return true;
        }else{
            return false;
        }
    }

    public boolean checkLoginCredentials(String username, String password){
        AppUser appUser = getUserWithUsername(username);
        if(appUser !=null && passwordEncoder.matches(password, appUser.getPassword())){
            return true;
        }
        return false;
    }

    public String basic(String username, String password){
        return basic(username, password, StandardCharsets.UTF_8);
    }

    private String basic(String username, String password, Charset charset) {
        String usernameAndPassword = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString((usernameAndPassword).getBytes(charset));
        return "Basic " + encoded;
    }

    public List<Photo> getAllPhotos(AppUser appUser){
        return appUser.getPhotos();
    }

    public List<Long> getPhotoIds(AppUser appUser){
        List<Long> returnList = new ArrayList<>();
        for(Photo photo: getAllPhotos(appUser)){
            returnList.add(photo.getId());
        }
        return returnList;
    }
}
