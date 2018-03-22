package com.polsl.multimedia.MultimediaProject.services;

import org.springframework.stereotype.Service;

/**
 * Created by Ryukki on 22.03.2018.
 */
@Service
public class ExampleService {
    public String welcomeUser(String name){
        return "Welcome " + name;
    }
}
