package com.polsl.multimedia.MultimediaProject.restcontrollers;

import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import com.polsl.multimedia.MultimediaProject.services.PhotoService;
import com.polsl.multimedia.MultimediaProject.services.UserService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Ryukki on 26.04.2018.
 */
@RestController
public class AppRestController {
    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/registerUser", method = RequestMethod.POST)
    public Long registerUser(@RequestParam String username, @RequestParam String password){
        return userService.createUser(username, password).getId();
    }

    @RequestMapping(value = "/users")
    public List<AppUser> findAllUsers() {
        return userRepository.findAll();
    }

    @RequestMapping(value = "/displayPhoto", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] displayPhoto(@RequestParam Long photoId, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(appUser!=null){
            try {
                String photoPath = userService.getUsersPhotoPath(appUser, photoId);
                InputStream in = FileUtils.openInputStream(new File(photoPath));
                return IOUtils.toByteArray(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @RequestMapping(value = "uploadPhoto", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public Long uploadPhoto(@RequestParam MultipartFile photo, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        try {
            return photoService.addPhoto(appUser, photo);
        } catch (IOException e) {
            e.printStackTrace();
            return -3L;//IOException
        }
    }

    @RequestMapping(value = "updatePhoto", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public Long updatePhoto(@RequestParam MultipartFile photo, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        //TODO
        return 0L;
    }
}
