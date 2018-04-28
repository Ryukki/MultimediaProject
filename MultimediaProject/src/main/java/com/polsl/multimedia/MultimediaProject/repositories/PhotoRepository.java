package com.polsl.multimedia.MultimediaProject.repositories;

import com.polsl.multimedia.MultimediaProject.models.Photo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Repository
public interface PhotoRepository extends CrudRepository<Photo, Long> {
}
