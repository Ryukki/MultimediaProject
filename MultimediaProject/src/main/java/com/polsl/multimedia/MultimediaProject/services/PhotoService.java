package com.polsl.multimedia.MultimediaProject.services;

import com.polsl.multimedia.MultimediaProject.repositories.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Service
public class PhotoService {

    @Autowired
    PhotoRepository photoRepository;
}
