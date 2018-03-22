package com.polsl.multimedia.MultimediaProject.restcontrollers;

import com.polsl.multimedia.MultimediaProject.services.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Ryukki on 22.03.2018.
 */
@RestController
@RequestMapping(value = "/api/example")
public class ExampleRestController {
    @Autowired
    private ExampleService exampleService;

    @RequestMapping(value = "/welcome")
    public String welcomeUser(@RequestParam String name){
        return exampleService.welcomeUser(name);
    }
}
