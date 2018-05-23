package com.polsl.multimedia.MultimediaProject.services;

import com.polsl.multimedia.MultimediaProject.DTO.PhotoParams;
import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.models.Photo;
import com.polsl.multimedia.MultimediaProject.repositories.PhotoRepository;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityExistsException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import org.imgscalr.*;

/**
 * Created by Ryukki on 20.04.2018.
 */
@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public Photo getPhotoWithId(Long id){
        Optional<Photo> photo = photoRepository.findById(id);
        if(photo.isPresent()){
            return photo.orElse(new Photo());
        }
        return null;
    }

    public Long addPhoto(AppUser appUser, MultipartFile multipartFile) throws IOException, EntityExistsException {
        Photo photo = new Photo();
        String photoName = multipartFile.getOriginalFilename();

        String filePath = "resources/" + appUser.getId();
        new File(filePath).mkdirs();
        String miniaturePath = filePath + "/miniature" + photoName;
        filePath +=  "/" + photoName;

        Photo checkForExistingEntry = photoRepository.findByNormalResolutionPath(filePath);
        if(checkForExistingEntry!=null){
            System.out.println("photo with path \"" + filePath + "\" already in DB");
            throw new EntityExistsException();// -1L;//photo with this path already in DB
        }


        File photoFile = new File(filePath);
        boolean newFile = photoFile.createNewFile();
        if(!newFile){
            System.out.println("File " + filePath + " already exists");
            throw new FileExistsException();//-2L;//file already exists
        }
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(multipartFile.getBytes());
        fos.close();

        createMiniature(photoFile, miniaturePath);

        photo.setPhotoName(photoName);
        photo.setNormalResolutionPath(filePath);
        photo.setMiniaturePath(miniaturePath);
        photo.setUserID(appUser);
        photo = photoRepository.save(photo);
        appUser.getPhotos().add(photo);
        userRepository.save(appUser);

        return photo.getId();
    }

    private void createMiniature(File photoFile, String path){
        try {
            BufferedImage img = ImageIO.read(photoFile);
            BufferedImage scaledImg = Scalr.resize(img, 150);
            String extension = FilenameUtils.getExtension(path);
            ImageIO.write(scaledImg, extension, new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean updatePhoto(AppUser appUser, Long id, MultipartFile multipartFile){
        Photo photo = getPhotoWithId(id);
        if(photo!= null && userService.userHasPhoto(appUser, photo)){
            try{
                File photoFile = new File(photo.getNormalResolutionPath());
                FileOutputStream fos = new FileOutputStream(photoFile, false);
                fos.write(multipartFile.getBytes());
                fos.close();
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public void updatePhotoParams(Photo photo, PhotoParams photoParams){
        photo.setPhotoName(photoParams.getPhotoName());
        photo.setDate(photoParams.getDate());
        photo.setCameraName(photoParams.getCameraName());
        photo.setExposure(photoParams.getExposure());
        photo.setMaxAperture(photoParams.getMaxAperture());
        photo.setFocalLength(photoParams.getFocalLength());
        photo.setLongitude(photoParams.getLongitude());
        photo.setLatitude(photoParams.getLatitude());
        photoRepository.save(photo);
    }

    public boolean deletePhoto(AppUser appUser, Long id){
        Photo photo = getPhotoWithId(id);
        if(photo!= null && userService.userHasPhoto(appUser, photo)){
            try{
                File photoFile = new File(photo.getNormalResolutionPath());
                boolean temp = photoFile.delete();
                photoFile = new File(photo.getMiniaturePath());
                temp = photoFile.delete();
                photoRepository.delete(photo);
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
