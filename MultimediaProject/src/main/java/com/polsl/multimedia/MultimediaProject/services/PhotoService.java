package com.polsl.multimedia.MultimediaProject.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import com.polsl.multimedia.MultimediaProject.DTO.FilterParams;
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
import java.util.stream.Collectors;

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

    public List<Long> filterPhotos(AppUser appUser, FilterParams rules){
        List<Long> idList = new ArrayList<>();
        List<Photo> photoList;
        List<Photo> filteredList;
        if(rules.getSortAsc()== null || rules.getSortAsc()){
            photoList = photoRepository.findAllByUserIDOrderByDateAsc(appUser);
        }else{
            photoList = photoRepository.findAllByUserIDOrderByDateDesc(appUser);
        }

        if(checkIfEmptyFilterRules(rules)){
            for(Photo photo: photoList){
                idList.add(photo.getId());
            }
            return idList;
        }

        filteredList = filterList(rules, photoList);
        for(Photo photo: filteredList){
            idList.add(photo.getId());
        }
        return idList;
    }

    private List<Photo> filterList(FilterParams rules, List<Photo> inputPhotoList){
        List<Photo> returnList = inputPhotoList;

        if(rules.getAuthors()!=null && !rules.getAuthors().isEmpty()){
            returnList.retainAll(inputPhotoList.stream().filter(p -> rules.getAuthors().contains(p.getAuthor())).collect(Collectors.toList()));
        }
        if(rules.getCameraNames()!=null && !rules.getCameraNames().isEmpty()){
            returnList.retainAll(inputPhotoList.stream().filter(p -> rules.getCameraNames().contains(p.getCameraName())).collect(Collectors.toList()));
        }
        if(rules.getPhotoNames()!=null && !rules.getPhotoNames().isEmpty()){
            returnList.retainAll(inputPhotoList.stream().filter(p -> rules.getPhotoNames().contains(p.getPhotoName())).collect(Collectors.toList()));
        }
        if(rules.getExposureList()!=null && !rules.getExposureList().isEmpty()){
            returnList.retainAll(inputPhotoList.stream().filter(p -> rules.getExposureList().contains(p.getExposure())).collect(Collectors.toList()));
        }
        if(rules.getMaxApertureList()!=null && !rules.getMaxApertureList().isEmpty()){
            returnList.retainAll(inputPhotoList.stream().filter(p -> rules.getMaxApertureList().contains(p.getMaxAperture())).collect(Collectors.toList()));
        }
        if(rules.getFocalLengthList()!=null && !rules.getFocalLengthList().isEmpty()){
            returnList.retainAll(inputPhotoList.stream().filter(p -> rules.getFocalLengthList().contains(p.getFocalLength())).collect(Collectors.toList()));
        }
        if(rules.getLongitudeList()!=null && !rules.getLongitudeList().isEmpty()){
            returnList.retainAll(inputPhotoList.stream().filter(p -> rules.getLongitudeList().contains(p.getLongitude())).collect(Collectors.toList()));
        }
        if(rules.getLatitudeList()!=null && !rules.getLatitudeList().isEmpty()){
            returnList.retainAll(inputPhotoList.stream().filter(p -> rules.getLatitudeList().contains(p.getLatitude())).collect(Collectors.toList()));
        }
        /*
        A było takie ładne: :(
        for(String value: rule.getValue()){
            switch (rule.getKey()){
                case "author":
                    returnList.addAll(list.stream().filter(p -> p.getAuthor().equals(value)).collect(Collectors.toList()));
                    break;
                case "cameraName":
                    returnList.addAll(list.stream().filter(p -> p.getCameraName().equals(value)).collect(Collectors.toList()));
                    break;
                case "exposure":
                    returnList.addAll(list.stream().filter(p -> p.getExposure().equals(value)).collect(Collectors.toList()));
                    break;
                case "aperture":
                    returnList.addAll(list.stream().filter(p -> p.getMaxAperture().equals(value)).collect(Collectors.toList()));
                    break;
                case "focalLength":
                    returnList.addAll(list.stream().filter(p -> p.getFocalLength().equals(value)).collect(Collectors.toList()));
                    break;
                case "longitude":
                    returnList.addAll(list.stream().filter(p -> p.getLongitude().equals(Double.parseDouble(value))).collect(Collectors.toList()));
                    break;
                case "latitude":
                    returnList.addAll(list.stream().filter(p -> p.getLatitude().equals(Double.parseDouble(value))).collect(Collectors.toList()));
                    break;
                default:
                    break;
            }
        }*/
        return returnList;
    }

    private boolean checkIfEmptyFilterRules(FilterParams rules){
        if(rules.getAuthors()!=null && !rules.getAuthors().isEmpty()){
            return false;
        }
        if(rules.getCameraNames()!=null && !rules.getCameraNames().isEmpty()){
            return false;
        }
        if(rules.getPhotoNames()!=null && !rules.getPhotoNames().isEmpty()){
            return false;
        }
        if(rules.getFocalLengthList()!=null && !rules.getFocalLengthList().isEmpty()){
            return false;
        }
        if(rules.getExposureList()!=null && !rules.getExposureList().isEmpty()){
            return false;
        }
        if(rules.getMaxApertureList()!=null && !rules.getMaxApertureList().isEmpty()){
            return false;
        }
        if(rules.getLongitudeList()!=null && !rules.getLongitudeList().isEmpty()){
            return false;
        }
        if(rules.getLatitudeList()!=null && !rules.getLatitudeList().isEmpty()){
            return false;
        }

        return true;
    }
}
