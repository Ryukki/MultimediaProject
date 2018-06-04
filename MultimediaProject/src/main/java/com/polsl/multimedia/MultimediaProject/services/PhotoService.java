package com.polsl.multimedia.MultimediaProject.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.GpsDirectory;
import com.icafe4j.image.meta.MetadataType;
import com.icafe4j.image.meta.exif.Exif;
import com.icafe4j.image.meta.exif.ExifTag;
import com.icafe4j.image.tiff.FieldType;
import com.icafe4j.image.tiff.TiffTag;
import com.polsl.multimedia.MultimediaProject.DTO.FilterParams;
import com.polsl.multimedia.MultimediaProject.DTO.PhotoParams;
import com.polsl.multimedia.MultimediaProject.models.AppUser;
import com.polsl.multimedia.MultimediaProject.models.Photo;
import com.polsl.multimedia.MultimediaProject.repositories.PhotoRepository;
import com.polsl.multimedia.MultimediaProject.repositories.UserRepository;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityExistsException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.imgscalr.*;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import com.icafe4j.image.meta.tiff.TiffExif;

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

    //TODO remove
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

        //test(photo.getNormalResolutionPath());

        FileInputStream fin = new FileInputStream(photoFile);
        FileOutputStream fout = new FileOutputStream("resources/test.jpg");
        Exif exif = new TiffExif();
        exif.addExifField(ExifTag.MAX_APERTURE_VALUE, FieldType.RATIONAL, new int[] {10, 600});
        exif.addExifField(ExifTag.EXPOSURE_TIME, FieldType.RATIONAL, new int[] {10, 600});
        com.icafe4j.image.meta.Metadata.insertExif(fin, fout, exif, true);
        fin.close();
        fout.close();

        return photo.getId();
    }

    private  void test(String path){
        Map<MetadataType, com.icafe4j.image.meta.Metadata> metadataMap = null;
        try {
            metadataMap = com.icafe4j.image.meta.Metadata.readMetadata(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Start of metadata information:");
        System.out.println("Total number of metadata entries: " + metadataMap.size());
        int i = 0;
        for(Map.Entry<MetadataType, com.icafe4j.image.meta.Metadata> entry : metadataMap.entrySet()) {
            System.out.println("Metadata entry " + i + " - " + entry.getKey());
            entry.getValue().showMetadata();
            i++;
            System.out.println("-----------------------------------------");
        }
        System.out.println("End of metadata information.");
    }

    private Photo readExIf(Photo photo, Metadata metadata){
        for (Directory directory : metadata.getDirectories()) {
            if(directory.getName().equals("Exif SubIFD")){
                for (Tag tag : directory.getTags()) {
                    try{
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

    public void changeExifMetadata(final File jpegImageFile, final File dst, Photo photo)
            throws IOException, ImageReadException, ImageWriteException {
        OutputStream os = null;
        boolean canThrow = false;
        try {
            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            {
                // Example of how to add a field/tag to the output set.
                //
                // Note that you should first remove the field/tag if it already
                // exists in this directory, or you may end up with duplicate
                // tags. See above.
                //
                // Certain fields/tags are expected in certain Exif directories;
                // Others can occur in more than one directory (and often have a
                // different meaning in different directories).
                //
                // TagInfo constants often contain a description of what
                // directories are associated with a given tag.
                //
                final TiffOutputDirectory exifDirectory = outputSet
                        .getOrCreateExifDirectory();
                // make sure to remove old value if present (this method will
                // not fail if the tag does not exist).
                exifDirectory
                        .removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE,
                        new RationalNumber(3, 10));

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME, new RationalNumber(1,1));

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_MAX_APERTURE_VALUE);
                //exifDirectory.add(ExifTagConstants.EXIF_TAG_MAX_APERTURE_VALUE, new RationalNumber());
                //exifDirectory.add(ExifTagConstants.EXIF_TAG_MAX_APERTURE_VALUE, photo.getMaxAperture());

                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH);
                //exifDirectory.add(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH, photo.getFocalLength());
            }

            {
                // Example of how to add/update GPS info to output set.

                // New York City
                final double longitude = -74.0; // 74 degrees W (in Degrees East)
                final double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees
                // North)

                outputSet.setGPSInDegrees(longitude, latitude);
            }



            final TiffOutputDirectory exifDirectory = outputSet
                    .getOrCreateRootDirectory();
            exifDirectory
                    .removeField(ExifTagConstants.EXIF_TAG_SOFTWARE);
            exifDirectory.add(ExifTagConstants.EXIF_TAG_SOFTWARE,
                    "SomeKind");

            os = new FileOutputStream(dst);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
                    outputSet);

            canThrow = true;
        } finally {
            IOUtils.closeQuietly(os);
        }
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
