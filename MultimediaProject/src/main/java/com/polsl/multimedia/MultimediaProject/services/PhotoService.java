package com.polsl.multimedia.MultimediaProject.services;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.tiff.TiffMetadataReader;
import com.drew.imaging.tiff.TiffReader;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
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
import javafx.scene.image.Image;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.*;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityExistsException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
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
        photo.setAuthor(appUser.getUsername());
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

        for (Directory directory : metadata.getDirectories()) {
            if(directory.getName().equals("Exif SubIFD") || directory.getName().equals("Exif IFD0")){
                for (Tag tag : directory.getTags()) {
                    try{
                        String description = "";
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
                            case "Model":
                                photo.setCameraName(tag.getDescription());
                                break;
                            case "Artist":
                                photo.setAuthor(tag.getDescription());
                                break;
                            case "GPS":

                                break;
                            case "Exposure Time":
                                String number = tag.getDescription().split(" ")[0];
                                try {
                                    Rational rational = new Rational(Double.parseDouble(number));
                                    photo.setExposure(rational.toString() + " sec");
                                }
                                catch(NumberFormatException e) {
                                    photo.setExposure(tag.getDescription());
                                }
                                break;
                            case "Focal Length":
                                description = tag.getDescription().replace(",",".");
                                photo.setFocalLength(description);
                                break;
                                //aperture
                            case "F-Number":
                                description = tag.getDescription().replace(",",".");
                                photo.setMaxAperture(description);
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



    private void changeExifMetadata(Photo photo) throws IOException, ImageReadException, ImageWriteException {
        OutputStream os = null;
        try {
            File oldPhoto = new File(photo.getNormalResolutionPath());
            String[] photoParts = photo.getPhotoName().split("\\.");
            String copyName = photoParts[0] + "copy." + photoParts[1];
            String copyPath = "resources/" + photo.getUserID().getId() + "/" + copyName;
            File newPhoto = new File(copyPath);
            newPhoto.createNewFile();

            TiffOutputSet outputSet = null;
            ImageMetadata metadata = Imaging.getMetadata(oldPhoto);
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (jpegMetadata != null) {
                TiffImageMetadata exif = jpegMetadata.getExif();

                if (exif!=null) {
                    outputSet = exif.getOutputSet();
                }
            }
            if (outputSet == null) {
                outputSet = new TiffOutputSet();
            }
            TiffOutputDirectory exifRoot = outputSet.getOrCreateRootDirectory();

            //exif author
            exifRoot.removeField(TiffTagConstants.TIFF_TAG_ARTIST);
            exifRoot.add(TiffTagConstants.TIFF_TAG_ARTIST, photo.getAuthor());

            //exif device model
            exifRoot.removeField(TiffTagConstants.TIFF_TAG_MODEL);
            exifRoot.add(TiffTagConstants.TIFF_TAG_MODEL, photo.getCameraName());

            TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
            if (photo.getDate() != null) {
                //exif datetime
                SimpleDateFormat exifFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                exifDirectory.removeField(TiffTagConstants.TIFF_TAG_DATE_TIME);
                exifDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, exifFormat.format(photo.getDate()));
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, exifFormat.format(photo.getDate()));
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, exifFormat.format(photo.getDate()));
            }
            //aperture exif
            if (photo.getMaxAperture() != null && !photo.getMaxAperture().isEmpty()) {
                String[] params = photo.getMaxAperture().split("/");
                double number = Double.parseDouble(params[1]);
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FNUMBER);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_FNUMBER, RationalNumber.valueOf(number));
            }
            if (photo.getExposure() != null && !photo.getExposure().isEmpty()) {
                //exposure exif
                String[] exposureParams = photo.getExposure().split(" ");
                String[] params = exposureParams[0].split("/");
                int divisor = Integer.parseInt(params[1]);
                int numerator = Integer.parseInt(params[0]);
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_EXPOSURE_TIME, new RationalNumber(numerator, divisor));
            }
            if (photo.getFocalLength() != null && !photo.getFocalLength().isEmpty()) {
                //focal length exif
                String[] params = photo.getFocalLength().split(" ");
                double number = Double.parseDouble(params[0]);
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH, RationalNumber.valueOf(number));
            }
            if (photo.getLatitude()!=null && photo.getLongitude()!=null) {
                outputSet.setGPSInDegrees(photo.getLongitude(), photo.getLatitude());
            }
            os = new FileOutputStream(newPhoto);
            os = new BufferedOutputStream(os);
            new ExifRewriter().updateExifMetadataLossless(oldPhoto,os,outputSet);
            os.close();
            oldPhoto.delete();
            FileUtils.moveFile(newPhoto, oldPhoto);
        } finally {
            os.close();
        }
    }



    private static void printTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            System.out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            System.out.println(tagInfo.name + ": "
                    + field.getValueDescription());
        }
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
        try {
            changeExifMetadata(photo);
        } catch (IOException | ImageReadException | ImageWriteException e) {
            e.printStackTrace();
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
        return rules.getLatitudeList() == null || rules.getLatitudeList().isEmpty();
    }

    private class Rational {

        private int num, denom;

        public Rational(double d) {
            String s = String.valueOf(d);
            int digitsDec = s.length() - 1 - s.indexOf('.');
            int denom = 1;
            for (int i = 0; i < digitsDec; i++) {
                d *= 10;
                denom *= 10;
            }

            int num = (int) Math.round(d);
            int g = gcd(num, denom);
            this.num = num / g;
            this.denom = denom /g;
        }

        public Rational(int num, int denom) {
            this.num = num;
            this.denom = denom;
        }

        public String toString() {
            return String.valueOf(num) + "/" + String.valueOf(denom);
        }

        public int gcd(int num, int denom) {
            BigInteger b1 = BigInteger.valueOf(num);
            BigInteger b2 = BigInteger.valueOf(denom);
            BigInteger gcd = b1.gcd(b2);
            return gcd.intValue();
        }
    }

}
