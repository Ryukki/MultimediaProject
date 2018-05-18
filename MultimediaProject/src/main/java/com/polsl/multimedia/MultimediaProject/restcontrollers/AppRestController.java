package com.polsl.multimedia.MultimediaProject.restcontrollers;

import com.polsl.multimedia.MultimediaProject.DTO.LoginResponse;
import com.polsl.multimedia.MultimediaProject.DTO.PhotoParams;
import com.polsl.multimedia.MultimediaProject.DTO.UploadExifBody;
import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.DTO.UserData;
import com.polsl.multimedia.MultimediaProject.models.Photo;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import com.polsl.multimedia.MultimediaProject.services.PhotoService;
import com.polsl.multimedia.MultimediaProject.services.UserService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityExistsException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
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

    @RequestMapping(value = "/registerUser", method = RequestMethod.POST)
    public ResponseEntity<UserData> registerUser(@RequestBody UserData userData){
        userService.createUser(userData.getUsername(), userData.getPassword());
        return new ResponseEntity<>(userData, HttpStatus.OK);
    }

    @RequestMapping(value = "/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserData userData){
        LoginResponse response = new LoginResponse();
        if(userService.checkLoginCredentials(userData.getUsername(), userData.getPassword())){
            response.setAccessToken(userService.basic(userData.getUsername(), userData.getPassword()));
            response.setMessage("OK");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        response.setMessage("User not found");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/allPhotos")
    public ResponseEntity<List<Long>> allPhotos(Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(appUser!=null){
            return ResponseEntity.ok(userService.getPhotoIds(appUser));
        }
        return ResponseEntity.unprocessableEntity().body(null);
    }

    @RequestMapping(value = "/displayPhoto", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] displayPhoto(@RequestParam Long photoId, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(appUser!=null){
            try {
                Photo photo = userService.getUsersPhoto(appUser, photoId);
                String photoPath = photo.getNormalResolutionPath();
                InputStream in = FileUtils.openInputStream(new File(photoPath));
                return IOUtils.toByteArray(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @RequestMapping(value = "/getParams")
    public ResponseEntity<PhotoParams> getParams(@RequestBody Long photoId, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(appUser!=null){
            Photo photo = userService.getUsersPhoto(appUser, photoId);
            return ResponseEntity.ok(new PhotoParams(photo));

        }
        return ResponseEntity.badRequest().body(new PhotoParams());
    }

    @RequestMapping(value = "/miniature", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] miniature(@RequestParam Long photoId, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(appUser!=null){
            try {
                Photo photo = userService.getUsersPhoto(appUser, photoId);
                String miniaturePath = photo.getMiniaturePath();
                InputStream in = FileUtils.openInputStream(new File(miniaturePath));
                return IOUtils.toByteArray(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @RequestMapping(value = "/uploadPhoto", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<Long> uploadPhoto(@RequestBody MultipartFile photo, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        try {
            return ResponseEntity.ok(photoService.addPhoto(appUser, photo));
        }catch (FileAlreadyExistsException e){
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().body(-2L);//file already exists
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().body(-3L);//IOException
        } catch (EntityExistsException e){
            e.printStackTrace();
            return ResponseEntity.unprocessableEntity().body(-1L);//photo with this path already in DB
        }
    }

    @RequestMapping(value = "/uploadParams", method = RequestMethod.POST)
    public HttpStatus uploadParams(@RequestBody UploadExifBody exifBody, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(appUser!=null){
            Photo photo = photoService.getPhotoWithId(exifBody.photoId);
            if(userService.userHasPhoto(appUser, photo)){
                photoService.updatePhotoParams(photo, exifBody.photoParams);
                return HttpStatus.OK;
            }
        }
        return HttpStatus.NOT_FOUND;
    }

    @RequestMapping(value = "/updatePhoto", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public HttpStatus updatePhoto(@RequestParam MultipartFile photo, @RequestParam Long photoId, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(photoService.updatePhoto(appUser, photoId, photo)){
            return HttpStatus.OK;
        }
        return HttpStatus.NOT_FOUND;
    }

    @RequestMapping(value = "/deletePhoto")
    public HttpStatus deletePhoto(@RequestParam Long photoId, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(photoService.deletePhoto(appUser, photoId)){
            return HttpStatus.OK;
        }
        return HttpStatus.NOT_FOUND;
    }
}
