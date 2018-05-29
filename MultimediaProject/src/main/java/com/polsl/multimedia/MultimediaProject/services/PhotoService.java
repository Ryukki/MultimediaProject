package com.polsl.multimedia.MultimediaProject.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import com.polsl.multimedia.MultimediaProject.DTO.PhotoParams;
import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.models.Photo;
import com.polsl.multimedia.MultimediaProject.repositories.PhotoRepository;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityExistsException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    //for test on local machine only
    /*#######################################################################################################*/
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createTestPhotos(){
        AppUser dummyAppUser = userService.getUserWithUsername("user");

        Photo photo = new Photo();
        photo.setUserID(dummyAppUser);
        photo.setAuthor("user");
        photo.setCameraName("phone");
        photo.setLatitude(10.9);
        photo.setExposure("11D");
        photo.setPhotoName("photo");
        photo = photoRepository.save(photo);
        dummyAppUser.getPhotos().add(photo);

        photo = new Photo();
        photo.setUserID(dummyAppUser);
        photo.setAuthor("user");
        photo.setCameraName("camera");
        photo.setLatitude(12.9);
        photo.setExposure("11D");
        photo.setPhotoName("photo");
        photo = photoRepository.save(photo);
        dummyAppUser.getPhotos().add(photo);

        photo = new Photo();
        photo.setUserID(dummyAppUser);
        photo.setAuthor("another");
        photo.setCameraName("camera");
        photo.setLatitude(12.9);
        photo.setExposure("1D");
        photo.setPhotoName("photo");
        photo = photoRepository.save(photo);
        dummyAppUser.getPhotos().add(photo);

        userRepository.save(dummyAppUser);
    }
    /*#######################################################################################################*/

    public Photo getPhotoWithId(Long id){
        Optional<Photo> photo = photoRepository.findById(id);
        if(photo.isPresent()){
            return photo.orElse(new Photo());
        }
        return null;
    }

    public Long addPhoto(AppUser appUser, MultipartFile multipartFile) throws IOException, EntityExistsException, ImageProcessingException {
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


        Metadata metadata = ImageMetadataReader.readMetadata(photoFile);
        photo = readExIf(photo, metadata);

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

    private Photo readExIf(Photo photo, Metadata metadata){
        for (Directory directory : metadata.getDirectories()) {
            if(directory.getName().equals("Exif SubIFD")){
                for (Tag tag : directory.getTags()) {
                    try{
                        System.out.println(tag.getTagName());
                        switch (tag.getTagName()){
                            case "Image Description":
                                photo.setDescription(tag.getDescription());
                                break;
                            case "Date/Time Original":
                                String exifDate = tag.getDescription();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.ENGLISH);
                                photo.setDate(dateFormat.parse(exifDate));
                                break;
                            case "Make":
                            case "Model":
                                String cameraName = "";
                                cameraName += directory.getString(0x10f) + " ";
                                cameraName += directory.getString(0x110);
                                photo.setCameraName(cameraName);
                                break;
                            case "Artist":
                                photo.setAuthor(tag.getDescription());
                                break;
                            case "GPS":
                                Collection<GpsDirectory> gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory.class);
                                for (GpsDirectory gpsDirectory : gpsDirectories) {
                                    // Try to read out the location, making sure it's non-zero
                                    GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                                    if (geoLocation != null && !geoLocation.isZero()) {
                                        photo.setLatitude(geoLocation.getLatitude());
                                        photo.setLongitude(geoLocation.getLongitude());
                                        break;
                                    }
                                }
                                break;
                            case "Exposure Time":
                                photo.setExposure(tag.getDescription());
                                break;
                            case "Focal Length":
                                photo.setFocalLength(tag.getDescription());
                                break;
                            case "Max Aperture Value":
                                photo.setMaxAperture(tag.getDescription());
                                break;
                            default:
                                break;
                        }
                    }catch (ParseException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return photo;
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
        photo.setAuthor(photoParams.getAuthor());
        photo.setDescription(photoParams.getDescription());
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

    public List<Long> filterPhotos(AppUser appUser, Map<String, String> rules){
        List<Long> idList = new ArrayList<>();
        List<Photo> photoList;
        String sort = rules.get("sort");
        if(sort!=null && sort.equals("Asc")){
            photoList = photoRepository.findAllByUserIDOrderByDateAsc(appUser);
        }else{
            photoList = photoRepository.findAllByUserIDOrderByDateDesc(appUser);
        }

        for(Map.Entry<String, String> rule: rules.entrySet()){
            if(!photoList.isEmpty()){
                photoList = filterList(rule, photoList);
            }
        }
        for(Photo photo: photoList){
            idList.add(photo.getId());
        }
        return idList;
    }

    private List<Photo> filterList(Map.Entry<String, String> rule, List<Photo> list){
        switch (rule.getKey()){
            case "author":
                list.removeIf(p -> !p.getAuthor().equals(rule.getValue()));
                return list;
            case "cameraName":
                list.removeIf(p -> !p.getCameraName().equals(rule.getValue()));
                return list;
            case "exposure":
                list.removeIf(p -> !p.getExposure().equals(rule.getValue()));
                return list;
            case "aperture":
                list.removeIf(p -> !p.getMaxAperture().equals(rule.getValue()));
                return list;
            case "focalLength":
                list.removeIf(p -> !p.getFocalLength().equals(rule.getValue()));
                return list;
            case "longitude":
                list.removeIf(p -> !p.getLongitude().equals(Double.parseDouble(rule.getValue())));
                return list;
            case "latitude":
                list.removeIf(p -> !p.getLatitude().equals(Double.parseDouble(rule.getKey())));
                return list;
            default:
                return list;
        }
    }
}
