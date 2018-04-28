package com.polsl.multimedia.MultimediaProject.restcontrollers;

import com.polsl.multimedia.MultimediaProject.services.PhotoService;
import com.polsl.multimedia.MultimediaProject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Ryukki on 26.04.2018.
 */
@RestController
@RequestMapping(value = "/")
public class AppRestController {
    @Autowired
    UserService userService;

    @Autowired
    PhotoService photoService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(value = "registerUser", method = RequestMethod.POST)
    public Long registerUser(String username, String password){
        return userService.createUser(username, password).getId();
    }
}
