package com.polsl.multimedia.MultimediaProject.controllers;

import com.polsl.multimedia.MultimediaProject.services.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Ryukki on 22.03.2018.
 */
@Controller
@RequestMapping(value="/example")
public class ExampleController {
    @Autowired
    private ExampleService exampleService;

    @RequestMapping(value = "/welcome")
    public String welcomeUser(@RequestParam String name, Model model){
        model.addAttribute("message", exampleService.welcomeUser(name));
        return "examplePage";
    }
}
