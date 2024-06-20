package com.krisnovak.springboot.demo.planttracker.service;

import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

//TODO: Might be able to get rid of save function
@Service
public class PlantTrackerServiceImpl implements PlantTrackerService{

    //Data Access Object used to retrieve information from the database
    private PlantTrackerDAO plantTrackerDAO;

    //S3 Bucket where Plant images are stored
    private S3Bucket s3Bucket;

    //S3 Properties File Path
    private final static File s3PropertiesFile = new File("../../../credentials.properties");

    @Autowired
    public PlantTrackerServiceImpl(PlantTrackerDAO thePlantTrackerDAO){
        this.plantTrackerDAO = thePlantTrackerDAO;
        this.s3Bucket = S3Bucket.newInstanceFromPropertiesFile(s3PropertiesFile);
    }

    @Override
    @Transactional
    //signUp is used to add a new account to the database
    //The passed in Account will have its current password updated
    public Account signUp(Account theAccount){

        //Check that an account was provided
        if(theAccount == null){
            throw new NullPointerException("Null Account was provided");
        }
        //Create a new account with the password and email provided in the account
        Account newAccount = new Account(theAccount.getEmail(), theAccount.getPasswordNew());

        //Attempt to add the new account to the database
        return plantTrackerDAO.add(newAccount);

    }

    @Override
    @Transactional
    //changePassword changes an account password
    //If the password is changed, the passed in account should have a new currentPassword
    public Account changePassword(Account theAccount){

        String newPassword = theAccount.getPasswordNew();

        //Attempt to grab a managed instance of the account from the database
        //Should throw an error if the account does not exist in the database
        Account managedAccount = Account.managedInstance(theAccount, plantTrackerDAO);

        //Set the current password to the new password
        managedAccount.setPasswordCurrent(newPassword);

        return managedAccount;

    }

    @Override
    @Transactional
    //Deletes a provided account so long as the username and password are authenticated
    public Account deleteAccount(Account theAccount){

        //Retrieve the account using the provided username
        //This step is necessary because the account provided by the user doesn't have an ID
        Account managedAccount = Account.managedInstance(theAccount, plantTrackerDAO);

        //Find all of the plants in the database and delete their associated images
        List<Plant> thePlants = plantTrackerDAO.findAllPlants(managedAccount);
        for(Plant plant : thePlants) {s3Bucket.deleteImage(plant.getImageKey());}

        //Delete the account
        plantTrackerDAO.delete(managedAccount);

        return managedAccount;
    }

    @Override
    @Transactional
    //validateCookie provides an indication of whether a received cookie is valid or not
    //A cookie is not valid if it's session ID (text value) is not stored in the Session database or if it is expired
    public void validateCookie(String sessionID){
        //Grab the session from the database using the provided session ID
        //If the session cannot be retrieved, then the cookie is not valid
        Session managedSession = Session.managedInstance(sessionID, plantTrackerDAO);

        //If the cookie retrieved from the database is expired, then the cookie is not valid
        if(managedSession.isExpiredSession())
            throw new InvalidSessionException("Cookie was expired");
    }


    //TODO: Perhaps add managed creation for plants?
    @Override
    @Transactional
    //createCookie makes a new cookie in the database and also provides a new cookie to the user (as a ResponseCookie)
    public ResponseCookie createCookie(Account theAccount){

        //Create a new session with a managed account attached (assuming the provided account has valid credentials)
        Session newSession = new Session(theAccount, plantTrackerDAO);

        //Get the managed account the new session
        Account managedAccount = newSession.getAccount();

        //Add the new session to the managed account
        managedAccount.addSession(newSession);

        //Delete any expired sessions that the managed account has
        managedAccount.deleteExpiredSessions();

        //Return back a response cookie for the new session
        return newSession.getResponseCookie();
    }

    @Override
    @Transactional
    //removeCookie removes a particular session from the database and returns an expired cookie to provide to the client
    public ResponseCookie getExpiredCookie(String sessionID){
        //Retrieve account using the session ID and remove the session associated with the session ID from the account
        Session managedSession = Session.managedInstance(sessionID, plantTrackerDAO);

        Account managedAccount = managedSession.getAccount();

        managedAccount.removeSession(managedSession);

        return Session.getExpiredCookie();

    }

    @Override
    public List<Plant> findPlants(String sessionID){
        Session managedSession = Session.managedInstance(sessionID, plantTrackerDAO);

        List<Plant> managedPlants = Plant.allManagedInstances(sessionID, plantTrackerDAO);

        for(Plant plant : managedPlants){
            plant.generateAndSetImageURL(s3Bucket);
        }

        return managedPlants;
    }

    @Override
    public Plant findPlantByPlantID(String plantID, String sessionID){

        Plant managedPlant = Plant.managedInstance(plantID, sessionID, plantTrackerDAO);

        managedPlant.generateAndSetImageURL(s3Bucket);

        return managedPlant;
    }

    @Override
    @Transactional
    public Plant addPlant(Plant thePlant, String sessionID){

        if(thePlant == null){
            throw new NullPointerException("A Null Plant was provided");
        }

        //Give the new plant the attributes of the provided plant
        Plant newPlant = new Plant(sessionID, plantTrackerDAO);
        newPlant.setPlantName(thePlant.getPlantName());
        newPlant.setLastWatered(thePlant.getLastWatered());
        newPlant.setWateringInterval(thePlant.getWateringInterval());

        //TODO: Add including an image?

        //Add the new plant to the managed account
        Account managedAccount = newPlant.getAccount();
        managedAccount.addPlant(newPlant);

        //Save the new plant
        plantTrackerDAO.add(newPlant);

        return newPlant;

    }

    @Override
    @Transactional
    public Plant updatePlant(Plant thePlant, String sessionID){

        if(thePlant == null)
            throw new NullPointerException("A null Plant was provided");

        Plant managedPlant = Plant.managedInstance(thePlant.getId(), sessionID, plantTrackerDAO);
        managedPlant.setPlantName(thePlant.getPlantName());
        managedPlant.setLastWatered(thePlant.getLastWatered());
        managedPlant.setWateringInterval(thePlant.getWateringInterval());

        return managedPlant;

    }

    //TODO: Make sure image in container is deleted along with plant
    @Override
    @Transactional
    public Plant deletePlant(String PlantID, String sessionID){

        Plant managedPlant = Plant.managedInstance(PlantID, sessionID, plantTrackerDAO);

        //Delete plant image from AWS
        s3Bucket.deleteImage(managedPlant.getImageKey());

        plantTrackerDAO.delete(managedPlant);

        return managedPlant;
    }

    @Override
    public void confirmDeviceRegistration(Device theDevice){
        //Confirm that the device is valid by attempting to
        // grab a managed instance of a plant associated with the device
        Plant.managedInstance(theDevice, plantTrackerDAO);
    }

    @Override
    public void updateTimestamp(Device theDevice){

        //Grab a managed instance of a plant using the device information
        Plant managedPlant = Plant.managedInstance(theDevice, plantTrackerDAO);

        //Update the watering time for the plant
        managedPlant.setLastWatered(LocalDate.now());

    }

    @Override
    @Transactional
    public Plant updatePlantImage(String PlantID, String sessionID, MultipartFile theFile) throws IOException{

        //Grab the plant from the database
        Plant managedPlant = Plant.managedInstance(PlantID, sessionID, plantTrackerDAO);

        //Convert the file to an image object
        BufferedImage image = ImageIO.read(theFile.getInputStream());

        managedPlant.generateAndSetImageKey(image, s3Bucket);

        //Set an image URL in the managed plant to pass back to the client
        managedPlant.generateAndSetImageURL(s3Bucket);

        return managedPlant;
    }
}
