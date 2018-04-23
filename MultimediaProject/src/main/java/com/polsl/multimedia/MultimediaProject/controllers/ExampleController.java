package com.polsl.multimedia.MultimediaProject.controllers;

import com.polsl.multimedia.MultimediaProject.services.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * Created by Ryukki on 22.03.2018.
 */
@Controller
@RequestMapping(value="/example")
public class ExampleController {
    @Autowired
    private ExampleService exampleService;

    @Autowired
    private ApplicationContext appContext;

    @RequestMapping(value = "/welcome")
    public String welcomeUser(@RequestParam String name, Model model){
        model.addAttribute("message", exampleService.welcomeUser(name));
        return "examplePage";
    }

    @RequestMapping(value = "/picture")
    public String showPicture(Model model){
        try {
            model.addAttribute("picture", appContext.getResource("/tempPhotos/test.jpg").getFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
return "";
    }
}
