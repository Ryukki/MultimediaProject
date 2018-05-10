package com.polsl.multimedia.MultimediaProject.repositories;

import com.polsl.multimedia.MultimediaProject.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    //List<AppUser> findAll();

    //Optional<AppUser> findById(Long id);

    boolean existsByUsername(String username);

    AppUser findByUsername(String username);

    // existById(long id);

}
