package com.polsl.multimedia.MultimediaProject.restcontrollers;

import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.services.PhotoService;
import com.polsl.multimedia.MultimediaProject.services.UserService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Ryukki on 26.04.2018.
 */
@RestController
@RequestMapping(value = "/")
public class AppRestController {
    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(value = "registerUser", method = RequestMethod.POST)
    public Long registerUser(String username, String password){
        return userService.createUser(username, password).getId();
    }

    @RequestMapping(value = "displayPhoto", method = RequestMethod.POST, produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] displayPhoto(@RequestParam Long photoId, @RequestHeader("Username") String username){
        AppUser appUser = userService.getUserWithUsername(username);
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
    public Long uploadPhoto(@RequestParam MultipartFile photo, @RequestHeader("Username") String username){
        AppUser appUser = userService.getUserWithUsername(username);
        return photoService.addPhoto(appUser, photo);
    }
}
