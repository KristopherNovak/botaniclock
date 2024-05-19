package com.krisnovak.springboot.demo.planttracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.service.RandomString;
import com.krisnovak.springboot.demo.planttracker.service.S3Bucket;
import jakarta.persistence.*;
import org.imgscalr.Scalr;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Class that represents the information associated with a Plant
 */
@Entity
@Table(name="plant")
public class Plant {

    //How long a registration ID can be in characters
    static public final int MAXIMUM_REGISTRATION_ID_LENGTH = 20;

    //Maximum image resolution to be stored in S3 in KB
    private static final int MAX_IMAGE_RESOLUTION = 1000;


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    @JsonProperty("id")
    private int id;
    @Column(name="plant_name")
    @JsonProperty("plantName")
    private String plantName;
    //could instead be file_path instead of image_url
    @Transient
    @JsonProperty("imageURL")
    private String imageURL;
    @Column(name="last_watered")
    @JsonProperty("lastWatered")
    private LocalDate lastWatered;
    @Column(name="watering_interval")
    @JsonProperty("wateringInterval")
    private int wateringInterval;
    @Column(name="registration_id")
    @JsonProperty("registrationID")
    private String registrationID;

    @Transient
    @JsonProperty("image")
    private File image;

    @Column(name="image_key")
    private String imageKey;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name="account_id")
    @JsonIgnore
    private Account account;

    private Plant(){}

    /**
     * Function used to create a new plant if the provided session is valid
     * @param sessionID Text of the SessionID field in the received cookie in a client request
     * @param plantTrackerDAO The data access object for retrieving a session from the database
     * @throws InvalidSessionException Thrown if the provided session ID is not linked to a valid session
     */
    public Plant(String sessionID, PlantTrackerDAO plantTrackerDAO) throws InvalidSessionException{
        this.id = 0;
        Session managedSession = Session.managedInstance(sessionID, plantTrackerDAO);
        this.account = managedSession.getAccount();
        this.registrationID = RandomString.generateRandomString(MAXIMUM_REGISTRATION_ID_LENGTH);
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getImageURL() {
        return imageURL;
    }

    private void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public LocalDate getLastWatered() {
        return lastWatered;
    }

    public void setLastWatered(LocalDate lastWatered) {
        this.lastWatered = lastWatered;
    }

    public int getWateringInterval() {
        return wateringInterval;
    }

    public void setWateringInterval(int wateringInterval) {
        this.wateringInterval = wateringInterval;
    }

    public String getRegistrationID() {
        return registrationID;
    }

    private void setRegistrationID(String registrationID){this.registrationID = registrationID;}

    public Account getAccount() {
        return account;
    }
    private void setAccount(Account theAccount){this.account = theAccount;}

    public File getImage() {return image;}

    public void setImage(File image) {
        this.image = image;
    }

    public String getImageKey() {
        return imageKey;
    }

    private void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    @Override
    public String toString() {
        return "Plant{" +
                "id=" + id +
                ", plantName='" + plantName + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", lastWatered=" + lastWatered +
                ", wateringInterval=" + wateringInterval +
                ", registrationID='" + registrationID + '\'' +
                '}';
    }

    /**
     * Function that returns a managed instance of Plant if the plant ID is linked to an existing plant
     * and the session ID is linked to the same account as the plant
     * @param plantID Database ID of the plant to retrieve
     * @param sessionID Text of the SessionID field in the received cookie in a client request
     * @param plantTrackerDAO Database Access Object for retrieving the plant and session from the database
     * @return A managed plant linked to both the plant ID and the session ID
     * @throws InvalidSessionException Thrown if the session ID is not linked to a valid session
     * @throws InvalidPlantException Thrown if the plant ID has an invalid format (not an integer),
     * a plant linked to the plant ID does not exist, or the account associated with the session ID
     * does not match the account associated with the found plant
     */
    public static Plant managedInstance(String plantID, String sessionID, PlantTrackerDAO plantTrackerDAO) throws InvalidSessionException, InvalidPlantException {

        //Determine if the provided plant ID is an integer
        Integer plantIDInt;
        try{plantIDInt = Integer.valueOf(plantID);
        }catch(NumberFormatException e){
            throw new InvalidPlantException("Plant ID should be an integer value");
        }

        //Return a managed instance of the plant using the integer version of the plant ID
        return managedInstance(plantIDInt, sessionID, plantTrackerDAO);

    }

    /**
     * Function that returns a managed instance of Plant if the plant ID is linked to an existing plant
     * and the session ID is linked to the same account as the plant
     * @param plantID Database ID of the plant to retrieve
     * @param sessionID Text of the SessionID field in the received cookie in a client request
     * @param plantTrackerDAO Database Access Object for retrieving the plant and session from the database
     * @return A managed plant linked to both the plant ID and the session ID
     * @throws InvalidSessionException Thrown if the session ID is not linked to a valid session
     * @throws InvalidPlantException Thrown if a plant linked to the plant ID does not exist or
     * the account associated with the session ID does not match the account associated with the found plant
     */
    public static Plant managedInstance(int plantID, String sessionID, PlantTrackerDAO plantTrackerDAO) throws InvalidSessionException, InvalidPlantException{

        //First pull up the session from the database
        Session managedSession = Session.managedInstance(sessionID, plantTrackerDAO);

        //Then verify that the provided plant ID exists
        Plant managedPlant;
        try {managedPlant = plantTrackerDAO.findPlantByPlantID(plantID);
        } catch(EmptyResultDataAccessException e){
            throw new InvalidPlantException("Plant with provided Plant ID does not exist");
        }

        //Then verify that the account associated with the plant matches the account associated with the session
        String emailLinkedToPlant = managedPlant.getAccount().getEmail();
        String emailLinkedToSession = managedSession.getAccount().getEmail();

        if(!emailLinkedToPlant.equals(emailLinkedToSession)){
            throw new InvalidPlantException("Plant ID does not exist with provided account");
        }

        return managedPlant;

    }

    /**
     * Function that returns a managed instance of a plant if the device has the right credentials (an account email linked to a Plant registration ID)
     * @param theDevice The connected device with an account email and a plant registration ID
     * @param plantTrackerDAO The data access object for locating the plant and the account
     * @return A managed plant linked to both the account email and the plant registration ID
     * @throws InvalidPlantException if the registration ID is not linked to a plant, if the account email is not
     * linked to an account, or if the registration ID and the account email are not linked to the same account
     */
    public static Plant managedInstance(Device theDevice, PlantTrackerDAO plantTrackerDAO){

        //Set up the error message to throw if something goes wrong
        String errorMessage= "No plant with the registration ID and linked to provided account email could be found";

        //Attempt to grab a managed plant using the provided registration ID
        Plant managedPlant;
        try{managedPlant = plantTrackerDAO.findPlantByRegistrationID(theDevice.getRegistrationID());
        }catch(EmptyResultDataAccessException e){
            throw new InvalidPlantException(errorMessage);
        }

        //Grab the account associated with the registration ID
        Account accountAssociatedWithRegistrationID = managedPlant.getAccount();

        //Ensure that the provided email matches the email associated with the plant
        if(!theDevice.getAccountEmail().equals(accountAssociatedWithRegistrationID.getEmail())){
            throw new InvalidPlantException(errorMessage);
        }

        return managedPlant;
    }

    /**
     * Function that returns a list of all plants associated with the account linked to a particular session ID
     * @param sessionID Text of the SessionID field in the received cookie in a client request
     * @param plantTrackerDAO Database Access Object for retrieving the plant and session from the database
     * @return A list of managed plants associated with
     * @throws InvalidSessionException Thrown if the provided session ID is not linked to a valid session
     */
    public static List<Plant> allManagedInstances(String sessionID, PlantTrackerDAO plantTrackerDAO) throws InvalidSessionException{

        Session managedSession = Session.managedInstance(sessionID, plantTrackerDAO);

        return plantTrackerDAO.findAllPlants(managedSession.getAccount());

    }

    /**
     * Function that sets a plant with an image URL using the provided s3Bucket
     * @param s3Bucket The bucket from which an image URL is being retrieved
     */
    public void generateAndSetImageURL(S3Bucket s3Bucket){
        this.imageURL = s3Bucket.generateImageURL(this.imageKey);
    }

    /**
     * Function that sets an image key for a plant using the provided image and s3Bucket
     * @param image The image to provide to S3
     * @param s3Bucket The bucket that is used to upload to S3
     * @return The image key that was generated
     * @throws IOException Thrown if the provided image can't be processed by the S3Bucket
     */
    public String generateAndSetImageKey(BufferedImage image, S3Bucket s3Bucket) throws IOException{

        //Resize the image if it's greater than 500KB in size
        image = resizeImageIfTooLarge(image);

        //Delete the previous image stored in the plant grabbed from the server
        s3Bucket.deleteImage(this.imageKey);

        //Put the image in the S3 bucket
        String imageKey = s3Bucket.addImage(image);

        //Set the key in the S3 bucket
        this.imageKey = imageKey;

        return imageKey;

    }

    /**
     * Function that resizes an image if it is larger than the maximum size configured for the plant object
     * @param image The image to resize
     * @return The same image or a resized version of the image
     */
    private static BufferedImage resizeImageIfTooLarge(BufferedImage image){

        //Grab image resolution
        int imageResolutionKB = ((image.getHeight() * image.getWidth())/1000);

        //No need to modify the image if it's not too large
        if(imageResolutionKB < MAX_IMAGE_RESOLUTION)
            return image;

        //If image is too large, figure out how much it needs to be scaled
        int targetHeight = ((image.getHeight()*MAX_IMAGE_RESOLUTION)/imageResolutionKB);
        int targetWidth = ((image.getWidth()*MAX_IMAGE_RESOLUTION)/imageResolutionKB);

        //Create the scaled image
        return Scalr.resize(image, targetWidth);
    }
}
