package com.polsl.multimedia.MultimediaProject.repositories;

import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.models.Photo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Repository
public interface PhotoRepository extends CrudRepository<Photo, Long> {
    Photo findByNormalResolutionPath(String path);

    List<Photo> findAllByUserIDOrderByDateAsc(AppUser appUser);
    List<Photo> findAllByUserIDOrderByDateDesc(AppUser appUser);
}
