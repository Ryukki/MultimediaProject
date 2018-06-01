package com.polsl.multimedia.MultimediaProject.restcontrollers;

import com.polsl.multimedia.MultimediaProject.DTO.FilterParams;
import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.services.PhotoService;
import com.polsl.multimedia.MultimediaProject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.util.MultiValueMap;

/**
 * Created by Ryukki on 23.05.2018.
 */
@RestController
public class FilteringRestController {
    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    @RequestMapping(value = "/filterPhotos", consumes = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<List<Long>> filterPhotos(@RequestBody FilterParams filterRules, Authentication authentication){
        AppUser appUser = userService.getUserWithUsername(authentication.getName());
        if(appUser!=null){
                return ResponseEntity.ok(photoService.filterPhotos(appUser, filterRules));
        }
        return ResponseEntity.unprocessableEntity().body(null);
    }
}
