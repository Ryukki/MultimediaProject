package com.polsl.multimedia.MultimediaProject.repositories;

import com.polsl.multimedia.MultimediaProject.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Ryukki on 20.04.2018.
 */
public interface UserRepository extends CrudRepository <User, Long>{
    //List<User> findAll();

    //Optional<User> findById(Long id);

    boolean existsByUsername(String username);

    User findByUsername(String username);

    boolean existById(long id);

}
