package com.krisnovak.springboot.demo.planttracker.entity;

import com.krisnovak.springboot.demo.planttracker.Reflector;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.service.S3Bucket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlantTrackerPlantTests {
    @Mock
    private PlantTrackerDAO plantTrackerDAO;

    @Mock
    private Account mockAccount;

    @Mock
    private Account mockAccount2;

    @Mock
    private Session mockSession;

    @Mock
    private Plant mockPlant;

    @Mock
    private Device mockDevice;

    @Mock
    private S3Bucket mockS3Bucket;

    //Tests for new Plant(String sessionID, PlantTrackerDAO, plantTrackerDAO)
    @Test
    public void Plant_newPlant_returnsValidNewPlant(){

        //Create a fake session ID
        String fakeSessionID = "fakeSessionID";

        Account fakeAccount = new Account("test", "password");
        Reflector.setField(fakeAccount, "id", 1);

        when(Account.managedInstance(fakeAccount, plantTrackerDAO)).thenReturn(fakeAccount);
        Session fakeSession = new Session(fakeAccount, plantTrackerDAO);

        //Create a fake managed account
        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenReturn(fakeSession);
        Plant thePlant = new Plant(fakeSessionID, plantTrackerDAO);

        //Assert that all plant parameters have the values that they should have
        Assertions.assertEquals(thePlant.getId(), 0);
        Assertions.assertNull(thePlant.getPlantName());
        Assertions.assertNull(thePlant.getLastWatered());
        Assertions.assertEquals(thePlant.getWateringInterval(), 0);
        Assertions.assertEquals(fakeAccount, thePlant.getAccount());
        Assertions.assertEquals(thePlant.getRegistrationID().length(), Plant.MAXIMUM_REGISTRATION_ID_LENGTH);
        Assertions.assertNull(thePlant.getImageKey());
        Assertions.assertNull(thePlant.getImageURL());
    }

    @Test
    public void Plant_newPlant_throwsInvalidSessionException(){

        //Create a fake session ID
        String fakeSessionID = "fakeSessionID";

        //Create a fake managed account
        Account fakeAccount = new Account("test", "password");
        Reflector.setField(fakeAccount, "id", 1);

        //Attempt to create a plant from the invalid session ID
        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenThrow(new InvalidSessionException(""));
        Assertions.assertThrows(InvalidSessionException.class, ()->{new Plant(fakeSessionID, plantTrackerDAO);});

    }

    //Tests for managedInstance(String plantID, String sessionID, PlantTrackerDAO plantTrackerDAO)
    @Test
    public void Plant_managedInstanceStringPlantID_returnManagedPlant(){

        String fakePlantID = "1";
        String fakeSessionID = "fakeSessionID";

        //When looking for a managed version of a session, just return a mock session
        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenReturn(mockSession);
        //When attempting to get the account of the mock session, then just get a mock account
        when(mockSession.getAccount()).thenReturn(mockAccount);
        //When attempting to find a plant by its ID in the database, just return the mock plant
        when(plantTrackerDAO.findPlantByPlantID(1)).thenReturn(mockPlant);
        //When attempting to get the email of the mock account, then just return fakeEmail
        when(mockAccount.getEmail()).thenReturn("fakeEmail@fakeEmail.com");
        //When attempts to get the account of the mock plant, just return the mock account
        when(mockPlant.getAccount()).thenReturn(mockAccount);
        Plant newPlant = Plant.managedInstance(fakePlantID, fakeSessionID, plantTrackerDAO);

        Assertions.assertSame(newPlant, mockPlant);

    }

    @Test
    public void Plant_managedInstanceStringPlantID_throwInvalidPlantException(){

        String fakePlantID = "invalid";
        String fakeSessionID = "fakeSessionID";

        Assertions.assertThrows(InvalidPlantException.class, ()->{Plant.managedInstance(fakePlantID, fakeSessionID, plantTrackerDAO);});

    }

    //Tests for managedInstance(int plantID, String sessionID, PlantTrackerDAO plantTrackerDAO)

    //Tests for managedInstance(String plantID, String sessionID, PlantTrackerDAO plantTrackerDAO)
    @Test
    public void Plant_managedInstanceIntPlantID_returnManagedPlant(){

        int fakePlantID = 1;
        String fakeSessionID = "fakeSessionID";

        //When looking for a managed version of a session, just return a mock session
        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenReturn(mockSession);
        //When attempting to get the account of the mock session, then just get a mock account
        when(mockSession.getAccount()).thenReturn(mockAccount);
        //When attempting to find a plant by its ID in the database, just return the mock plant
        when(plantTrackerDAO.findPlantByPlantID(1)).thenReturn(mockPlant);
        //When attempting to get the email of the mock account, then just return fakeEmail
        when(mockAccount.getEmail()).thenReturn("fakeEmail@fakeEmail.com");
        //When attempts to get the account of the mock plant, just return the mock account
        when(mockPlant.getAccount()).thenReturn(mockAccount);
        Plant newPlant = Plant.managedInstance(fakePlantID, fakeSessionID, plantTrackerDAO);

        Assertions.assertSame(newPlant, mockPlant);

    }

    @Test
    public void Plant_managedInstanceIntPlantID_throwsInvalidPlantExceptionWhenNoPlantFound(){

        int fakePlantID = 1;
        String fakeSessionID = "fakeSessionID";

        //When looking for a managed version of a session, just return a mock session
        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenReturn(mockSession);
        //When attempting to find a plant by its ID in the database, just return the mock plant
        when(plantTrackerDAO.findPlantByPlantID(1)).thenThrow(EmptyResultDataAccessException.class);

        Assertions.assertThrows(InvalidPlantException.class, ()->{Plant.managedInstance(fakePlantID, fakeSessionID, plantTrackerDAO);});

    }

    @Test
    public void Plant_managedInstanceIntPlantID_throwsInvalidSessionException(){

        int fakePlantID = 1;
        String fakeSessionID = "fakeSessionID";

        //When looking for a managed version of a session, just return a mock session
        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenThrow(InvalidSessionException.class);

        Assertions.assertThrows(InvalidSessionException.class, ()->{Plant.managedInstance(fakePlantID, fakeSessionID, plantTrackerDAO);});

    }

    @Test
    public void Plant_managedInstanceIntPlantID_throwsInvalidPlantExceptionBecauseAccountsDoNotMatch(){

        int fakePlantID = 1;
        String fakeSessionID = "fakeSessionID";

        //When looking for a managed version of a session, just return a mock session
        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenReturn(mockSession);
        //When attempting to get the account of the mock session, then just get a mock account
        when(mockSession.getAccount()).thenReturn(mockAccount);
        //When attempting to find a plant by its ID in the database, just return the mock plant
        when(plantTrackerDAO.findPlantByPlantID(1)).thenReturn(mockPlant);
        //When attempting to get the email of the mock account, then just return fakeEmail
        when(mockAccount.getEmail()).thenReturn("fakeEmail@fakeEmail.com");
        //When attempts to get the account of the mock plant, just return the mock account
        when(mockPlant.getAccount()).thenReturn(mockAccount2);
        when(mockAccount2.getEmail()).thenReturn("emailFake@emailFake.com");

        Assertions.assertThrows(InvalidPlantException.class, ()->{Plant.managedInstance(fakePlantID, fakeSessionID, plantTrackerDAO);});

    }

    //Tests for Plant managedInstance(Device theDevice, PlantTrackerDAO plantTrackerDAO)
    @Test
    public void Plant_managedInstanceDevice_returnsPlant(){

        String fakeRegistrationID = "fakeRegistrationID";
        String fakeEmail = "fakeEmail@fakeEmail.com";

        when(mockDevice.getRegistrationID()).thenReturn(fakeRegistrationID);
        when(mockDevice.getAccountEmail()).thenReturn(fakeEmail);

        when(plantTrackerDAO.findPlantByRegistrationID(fakeRegistrationID)).thenReturn(mockPlant);
        when(mockPlant.getAccount()).thenReturn(mockAccount);
        when(mockAccount.getEmail()).thenReturn(fakeEmail);
        Plant newPlant = Plant.managedInstance(mockDevice, plantTrackerDAO);

        Assertions.assertSame(newPlant, mockPlant);

    }

    @Test
    public void Plant_managedInstanceDevice_throwsInvalidPlantExceptionPlantNotFound(){

        String fakeRegistrationID = "fakeRegistrationID";
        String fakeEmail = "fakeEmail@fakeEmail.com";

        when(mockDevice.getRegistrationID()).thenReturn(fakeRegistrationID);

        when(plantTrackerDAO.findPlantByRegistrationID(fakeRegistrationID)).thenThrow(EmptyResultDataAccessException.class);
        Assertions.assertThrows(InvalidPlantException.class, ()->{Plant.managedInstance(mockDevice, plantTrackerDAO);});

    }

    @Test
    public void Plant_managedInstanceDevice_throwsInvalidPlantExceptionEmailsNotMatched(){

        String fakeRegistrationID = "fakeRegistrationID";
        String fakeEmail = "fakeEmail@fakeEmail.com";
        String emailFake = "emailFake@emailFake.com";

        when(mockDevice.getRegistrationID()).thenReturn(fakeRegistrationID);
        when(mockDevice.getAccountEmail()).thenReturn(fakeEmail);

        when(plantTrackerDAO.findPlantByRegistrationID(fakeRegistrationID)).thenReturn(mockPlant);
        when(mockPlant.getAccount()).thenReturn(mockAccount);
        when(mockAccount.getEmail()).thenReturn(emailFake);

        Assertions.assertThrows(InvalidPlantException.class, ()->{Plant.managedInstance(mockDevice, plantTrackerDAO);});

    }

    //Tests for allManagedInstances(String sessionID, PlantTrackerDAO plantTrackerDAO)
    @Test
    public void Plant_allManagedInstances_returnsAllManagedInstances(){

        String fakeSessionID = "fakeSessionID";

        List<Plant> fakeManagedInstances = new ArrayList();
        fakeManagedInstances.add(mockPlant);
        fakeManagedInstances.add(mockPlant);

        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenReturn(mockSession);
        when(mockSession.getAccount()).thenReturn(mockAccount);
        when(plantTrackerDAO.findAllPlants(mockAccount)).thenReturn(fakeManagedInstances);
        List<Plant> allManagedInstances = Plant.allManagedInstances(fakeSessionID, plantTrackerDAO);

        Assertions.assertSame(fakeManagedInstances, allManagedInstances);
    }

    @Test
    public void Plant_allManagedInstances_throwsInvalidSessionException(){

        String fakeSessionID = "fakeSessionID";

        List<Plant> fakeManagedInstances = new ArrayList();
        fakeManagedInstances.add(mockPlant);
        fakeManagedInstances.add(mockPlant);

        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenThrow(InvalidSessionException.class);

        Assertions.assertThrows(InvalidSessionException.class, ()->{ Plant.allManagedInstances(fakeSessionID, plantTrackerDAO);});
    }

    //Tests for generateAndSetImageURL(S3Bucket s3Bucket)
    @Test
    public void Plant_generateAndSetImageURL_SetsImageURL(){

        String fakeSessionID = "fakeSessionID";

        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenReturn(mockSession);
        when(mockSession.getAccount()).thenReturn(mockAccount);
        Plant newPlant = new Plant(fakeSessionID, plantTrackerDAO);

        String fakeImageKey = "fakeImageKey";
        String fakeImageURL = "https://fakeImageURL.com";

        Reflector.setField(newPlant, "imageKey", fakeImageKey);

        when(mockS3Bucket.generateImageURL(fakeImageKey)).thenReturn(fakeImageURL);
        newPlant.generateAndSetImageURL(mockS3Bucket);

        Assertions.assertEquals(fakeImageURL, newPlant.getImageURL());
    }


    //Tests for generateAndSetImageKey(BufferedImage image, S3Bucket s3Bucket)
    @Test
    public void Plant_generateAndSetImageKey_ReturnsAndSetsImageKey() throws IOException {

        String fakeSessionID = "fakeSessionID";
        BufferedImage image = new BufferedImage(1,1, BufferedImage.TYPE_INT_RGB);

        when(Session.managedInstance(fakeSessionID, plantTrackerDAO)).thenReturn(mockSession);
        when(mockSession.getAccount()).thenReturn(mockAccount);
        Plant newPlant = new Plant(fakeSessionID, plantTrackerDAO);

        String fakeKey = "fakeKey";
        when(mockS3Bucket.addImage(image)).thenReturn(fakeKey);
        String newImageKey = newPlant.generateAndSetImageKey(image, mockS3Bucket);

        Assertions.assertEquals(fakeKey, newImageKey);
        Assertions.assertEquals(newPlant.getImageKey(), fakeKey);

    }

    //Tests for resizeImageIfTooLarge
    @Test
    public void Plant_resizeImageIfTooLarge_ReturnsSameImage() throws IOException {
        String fakeSessionID = "fakeSessionID";
        BufferedImage image = new BufferedImage(1,1, BufferedImage.TYPE_INT_RGB);
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int imageType = image.getType();

        Object[] argObjects = new Object[1];
        argObjects[0] = image;

        BufferedImage result = (BufferedImage) Reflector.invokeMethod(mockPlant, "resizeImageIfTooLarge", argObjects);
        Assertions.assertSame(image, result);
        Assertions.assertEquals(imageHeight, result.getHeight());
        Assertions.assertEquals(imageWidth, result.getWidth());
        Assertions.assertEquals(imageType, result.getType());
    }

    @Test
    public void Plant_resizeImageIfTooLarge_ReturnsDifferentSmallerImage() throws IOException {
        String fakeSessionID = "fakeSessionID";

        Integer maxImageSize = (Integer) Reflector.getField(mockPlant, "MAX_IMAGE_RESOLUTION");

        BufferedImage image = new BufferedImage(maxImageSize*10, maxImageSize*10, BufferedImage.TYPE_INT_RGB);
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int imageType = image.getType();

        Object[] argObjects = new Object[1];
        argObjects[0] = image;

        BufferedImage result = (BufferedImage) Reflector.invokeMethod(mockPlant, "resizeImageIfTooLarge", argObjects);


        Assertions.assertNotSame(image, result);
        Assertions.assertTrue(imageHeight > result.getHeight());
        Assertions.assertTrue(imageWidth > result.getWidth());
        Assertions.assertEquals(imageType, image.getType());
        Assertions.assertNotEquals(result.getHeight(), 0);
        Assertions.assertNotEquals(result.getWidth(), 0);
    }

}
