package com.krisnovak.springboot.demo.planttracker.service;

import com.krisnovak.springboot.demo.planttracker.Reflector;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.entity.*;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseCookie;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlantTrackerServiceImplTests {

    @Mock
    private PlantTrackerDAO plantTrackerDAO;

    @Mock
    private S3Bucket s3Bucket;

    @InjectMocks
    private PlantTrackerServiceImpl plantTrackerService;

    //Tests for public Account signUp(Account theAccount);
    @Test
    public void PlantTrackerService_signUp_ReturnsValidNewAccount(){

        Account newAccount = new Account("test", "password");

        Account managedAccount = new Account("test", "password");
        Reflector.setField(managedAccount, "id", 1);
        managedAccount.setPasswordCurrent(managedAccount.getPasswordNew());

        when(plantTrackerDAO.add(ArgumentMatchers.any(Account.class))).thenReturn(managedAccount);
        Account signedUpAccount = plantTrackerService.signUp(newAccount);

        Assertions.assertEquals(signedUpAccount.getId(), 1);
        Assertions.assertEquals("test", signedUpAccount.getEmail());
        Assertions.assertEquals("password", signedUpAccount.getPasswordCurrent());

    }

    //Tests for public void changePassword(Account theAccount);

    @Test
    public void PlantTrackerService_changePassword_ReturnsUpdatedAccount(){

        Account theAccount = new Account("test", "passwordNew");

        Account managedAccount = new Account("test", "passwordOld");
        Reflector.setField(managedAccount, "id", 1);

        when(plantTrackerDAO.findAccount(theAccount)).thenReturn(managedAccount);

        Account updatedAccount = plantTrackerService.changePassword(theAccount);

        Assertions.assertEquals(updatedAccount.getId(), 1);
        Assertions.assertEquals(updatedAccount.getEmail(), "test");
        Assertions.assertEquals(updatedAccount.getPasswordCurrent(), "passwordNew");


    }

    //Tests for public Account deleteAccount(Account theAccount);

    @Test
    public void PlantTrackerService_deleteAccount_ReturnsDeletedAccount(){

        Account theAccount = new Account("test", "password");

        Account managedAccount = new Account("test", null);
        Reflector.setField(managedAccount, "id", 1);
        Reflector.setField(managedAccount, "passwordCurrent", "password");

        List<Plant> thePlants = new ArrayList<Plant>();

        when(plantTrackerDAO.findAccount(theAccount)).thenReturn(managedAccount);
        when(plantTrackerDAO.findAllPlants(managedAccount)).thenReturn(thePlants);
        when(plantTrackerDAO.delete(ArgumentMatchers.any(Account.class))).thenAnswer(i->i.getArguments()[0]);

        Account deletedAccount = plantTrackerService.deleteAccount(theAccount);

        Assertions.assertEquals(deletedAccount.getId(), 1);
        Assertions.assertEquals(deletedAccount.getEmail(), "test");
        Assertions.assertEquals(deletedAccount.getPasswordCurrent(), "password");

    }

    //Tests for public void validateCookie(String sessionID);
    @Test
    public void PlantTrackerService_validateCookie_ReturnsWithoutException(){

        Account theAccount = new Account("test", "password");

        Account managedAccount = new Account("test", null);
        Reflector.setField(managedAccount, "id", 1);
        Reflector.setField(managedAccount, "passwordCurrent", "password");

        when(plantTrackerDAO.findAccount(theAccount)).thenReturn(managedAccount);
        Session managedSession = new Session(theAccount, plantTrackerDAO);
        Reflector.setField(managedSession, "id", 1);
        managedSession.setTimeCreated(System.currentTimeMillis()/1000);
        managedSession.setMaxAge(100);

        when(plantTrackerDAO.findSessionBySessionID(managedSession.getSessionID())).thenReturn(managedSession);
        plantTrackerService.validateCookie(managedSession.getSessionID());

        Assertions.assertDoesNotThrow(()->{plantTrackerService.validateCookie(managedSession.getSessionID());});

    }

    @Test
    public void PlantTrackerService_validateCookie_throwsInvalidSessionExceptionNotFound(){

        String fakeSessionID = "fakeSessionID";

        when(plantTrackerDAO.findSessionBySessionID(fakeSessionID)).thenThrow(InvalidSessionException.class);
        Assertions.assertThrows(InvalidSessionException.class, ()->{plantTrackerService.validateCookie(fakeSessionID);});

    }

    @Test
    public void PlantTrackerService_validateCookie_throwsInvalidSessionExceptionExpired(){

        Account theAccount = new Account("test", "password");

        Account managedAccount = new Account("test", null);
        Reflector.setField(managedAccount, "id", 1);
        Reflector.setField(managedAccount, "passwordCurrent", "password");

        when(plantTrackerDAO.findAccount(theAccount)).thenReturn(managedAccount);
        Session managedSession = new Session(theAccount, plantTrackerDAO);
        Reflector.setField(managedSession, "id", 1);
        managedSession.setTimeCreated((System.currentTimeMillis()/1000)-5);
        managedSession.setMaxAge(3);

        when(plantTrackerDAO.findSessionBySessionID(managedSession.getSessionID())).thenReturn(managedSession);
        Assertions.assertThrows(InvalidSessionException.class, ()->{plantTrackerService.validateCookie(managedSession.getSessionID());});

    }

    //Tests for public ResponseCookie createCookie(Account theAccount);
    @Test
    public void PlantTrackerService_createCookie_createsAndAddsValidCookie(){

        Account theAccount = new Account("test", "password");

        Account managedAccount = new Account("test", null);
        Reflector.setField(managedAccount, "id", 1);
        Reflector.setField(managedAccount, "passwordCurrent", "password");

        when(plantTrackerDAO.findAccount(theAccount)).thenReturn(managedAccount);
        ResponseCookie theCookie = plantTrackerService.createCookie(theAccount);

        Assertions.assertDoesNotThrow(()->{managedAccount.getSessions().get(0);});
        Session newSession = managedAccount.getSessions().get(0);

        Assertions.assertEquals(newSession.getSessionID(), theCookie.getValue());
        Assertions.assertEquals(newSession.getMaxAge(), theCookie.getMaxAge().toSeconds());

    }

    @Test
    public void PlantTrackerService_createCookie_throwsInvalidAccountException(){

        Account theAccount = new Account("test", "password");

        Account managedAccount = new Account("test", null);
        Reflector.setField(managedAccount, "id", 1);
        Reflector.setField(managedAccount, "passwordCurrent", "password");

        when(plantTrackerDAO.findAccount(theAccount)).thenThrow(EmptyResultDataAccessException.class);

        Assertions.assertThrows(InvalidAccountException.class, ()->{plantTrackerService.createCookie(theAccount);});

    }

    //Tests for public ResponseCookie getExpiredCookie(String sessionID);
    @Test
    public void PlantTrackerService_getExpiredCookie_removesCookie(){

        Account theAccount = new Account("test", "password");

        Account managedAccount = new Account("test", null);
        Reflector.setField(managedAccount, "id", 1);
        Reflector.setField(managedAccount, "passwordCurrent", "password");

        when(plantTrackerDAO.findAccount(theAccount)).thenReturn(managedAccount);
        Session managedSession = new Session(theAccount, plantTrackerDAO);
        Reflector.setField(managedSession, "id", 1);
        managedSession.setTimeCreated(System.currentTimeMillis()/1000);
        managedSession.setMaxAge(100);

        managedAccount.addSession(managedSession);

        when(plantTrackerDAO.findSessionBySessionID(managedSession.getSessionID())).thenReturn(managedSession);
        ResponseCookie expiredCookie = plantTrackerService.getExpiredCookie(managedSession.getSessionID());

        Assertions.assertTrue(managedAccount.getSessions().isEmpty());

        Assertions.assertEquals(expiredCookie.getValue(), "");
        Assertions.assertEquals(expiredCookie.getMaxAge().toSeconds(), 0);

    }

    @Test
    public void PlantTrackerService_getExpiredCookie_throwsInvalidSessionException(){

        String fakeSessionID = "fakeSessionID";

        when(plantTrackerDAO.findSessionBySessionID(fakeSessionID)).thenThrow(EmptyResultDataAccessException.class);

        Assertions.assertThrows(InvalidSessionException.class, ()->{plantTrackerService.getExpiredCookie(fakeSessionID);});

    }

    //Tests for public List<Plant> findPlants(String sessionID);
    @Test
    public void PlantTrackerService_findPlants_returnsListOfPlants(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        List<Plant> accountPlants = new ArrayList<Plant>();
        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);

        Plant plant1 = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Plant plant2 = new Plant(theSession.getSessionID(), plantTrackerDAO);

        List<Plant> thePlants = plantTrackerService.findPlants(theSession.getSessionID());

        Assertions.assertIterableEquals(accountPlants, thePlants);
    }

    @Test
    public void PlantTrackerService_findPlants_throwsInvalidSessionException(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        List<Plant> accountPlants = new ArrayList<Plant>();
        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenThrow(EmptyResultDataAccessException.class);

        Assertions.assertThrows(InvalidSessionException.class, ()->{plantTrackerService.findPlants(theSession.getSessionID());});
    }

    //Tests for public Plant findPlantByPlantID(String PlantID, String sessionID);

    @Test
    public void PlantTrackerService_findPlantByPlantID_returnsRequestedPlant(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        List<Plant> accountPlants = new ArrayList<Plant>();
        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);

        Plant plant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(plant, "id", 1);

        when(plantTrackerDAO.findPlantByPlantID(plant.getId())).thenReturn(plant);
        Plant managedPlant = plantTrackerService.findPlantByPlantID(Integer.toString(plant.getId()), theSession.getSessionID());

        Assertions.assertSame(plant, managedPlant);
    }

    @Test
    public void PlantTrackerService_findPlantByPlantID_throwsInvalidSessionException(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        List<Plant> accountPlants = new ArrayList<Plant>();
        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);

        Plant plant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(plant, "id", 1);

        String wrongSessionID = theSession.getSessionID() + 'a';

        when(plantTrackerDAO.findSessionBySessionID(wrongSessionID)).thenThrow(EmptyResultDataAccessException.class);
        Assertions.assertThrows(InvalidSessionException.class, ()->{plantTrackerService.findPlantByPlantID(Integer.toString(plant.getId()), wrongSessionID);});

    }

    @Test
    public void PlantTrackerService_findPlantByPlantID_throwsInvalidPlantException(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        List<Plant> accountPlants = new ArrayList<Plant>();
        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);

        Plant plant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(plant, "id", 1);

        when(plantTrackerDAO.findPlantByPlantID(2)).thenThrow(InvalidPlantException.class);
        Assertions.assertThrows(InvalidPlantException.class, ()->{plantTrackerService.findPlantByPlantID(Integer.toString(2), theSession.getSessionID());});

    }

    //Tests for public Plant addPlant(Plant thePlant, String sessionID);
    @Test
    public void PlantTrackerService_addPlant_returnsManagedPlant(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);

        Plant newPlant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(newPlant, "id", 0);

        when(plantTrackerDAO.add(ArgumentMatchers.any(Plant.class))).thenAnswer(i->{
            Plant thePlant = i.getArgument(0);
            Reflector.setField(thePlant, "id", 1);
            return thePlant;
        });

        Plant addedPlant = plantTrackerService.addPlant(newPlant, theSession.getSessionID());

        Assertions.assertEquals(addedPlant.getId(), 1);
        Assertions.assertEquals(addedPlant.getPlantName(), newPlant.getPlantName());
        Assertions.assertEquals(addedPlant.getLastWatered(), newPlant.getLastWatered());
        Assertions.assertEquals(addedPlant.getWateringInterval(), newPlant.getWateringInterval());
        Assertions.assertNotNull(addedPlant.getRegistrationID());
        Assertions.assertSame(addedPlant.getAccount(), newPlant.getAccount());

    }

    @Test
    public void PlantTrackerService_addPlant_throwsInvalidSessionException(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);

        Plant newPlant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(newPlant, "id", 0);

        String fakeSessionID = theSession.getSessionID() + 'a';

        when(plantTrackerDAO.findSessionBySessionID(fakeSessionID)).thenThrow(InvalidSessionException.class);
        Assertions.assertThrows(InvalidSessionException.class, ()->{plantTrackerService.addPlant(newPlant, fakeSessionID);});

    }

    //Tests for public Plant updatePlant(Plant thePlant, String sessionID);

    @Test
    public void PlantTrackerService_updatePlant_returnsManagedPlant(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);
        Plant updatePlant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(updatePlant, "id", 1);

        when(plantTrackerDAO.findPlantByPlantID(updatePlant.getId())).thenReturn(updatePlant);
        Plant updatedPlant = plantTrackerService.updatePlant(updatePlant, theSession.getSessionID());

        Assertions.assertEquals(updatedPlant.getId(), 1);
        Assertions.assertEquals(updatedPlant.getPlantName(), updatePlant.getPlantName());
        Assertions.assertEquals(updatedPlant.getLastWatered(), updatePlant.getLastWatered());
        Assertions.assertEquals(updatedPlant.getWateringInterval(), updatePlant.getWateringInterval());
        Assertions.assertNotNull(updatedPlant.getRegistrationID());
        Assertions.assertSame(updatedPlant.getAccount(), updatePlant.getAccount());

    }

    @Test
    public void PlantTrackerService_updatePlant_throwsInvalidSessionException(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);

        Plant updatePlant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(updatePlant, "id", 1);

        String fakeSessionID = theSession.getSessionID() + 'a';

        when(plantTrackerDAO.findSessionBySessionID(fakeSessionID)).thenThrow(EmptyResultDataAccessException.class);
        Assertions.assertThrows(InvalidSessionException.class, ()->{plantTrackerService.updatePlant(updatePlant, fakeSessionID);});

    }

    @Test
    public void PlantTrackerService_updatePlant_throwsInvalidPlantException(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);

        Plant updatePlant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(updatePlant, "id", 1);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);
        when(plantTrackerDAO.findPlantByPlantID(1)).thenThrow(EmptyResultDataAccessException.class);
        Assertions.assertThrows(InvalidPlantException.class, ()->{plantTrackerService.updatePlant(updatePlant, theSession.getSessionID());});

    }

    //Tests for public Plant deletePlant(String plantID, String sessionID);

    @Test
    public void PlantTrackerService_deletePlant_returnsPlant(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);
        Plant toDeletePlant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(toDeletePlant, "id", 1);

        when(plantTrackerDAO.findPlantByPlantID(toDeletePlant.getId())).thenReturn(toDeletePlant);
        when(plantTrackerDAO.delete(ArgumentMatchers.any(Plant.class))).thenAnswer(i->i.getArgument(0));
        Plant deletedPlant = plantTrackerService.deletePlant(Integer.toString(toDeletePlant.getId()), theSession.getSessionID());

        Assertions.assertSame(toDeletePlant, deletedPlant);

    }

    @Test
    public void PlantTrackerService_deletePlant_throwsInvalidSessionException(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);
        Plant toDeletePlant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(toDeletePlant, "id", 1);

        String fakeSessionID = theSession.getSessionID() + 'a';

        when(plantTrackerDAO.findSessionBySessionID(fakeSessionID)).thenThrow(EmptyResultDataAccessException.class);

        Assertions.assertThrows(InvalidSessionException.class, ()->{plantTrackerService.deletePlant(Integer.toString(toDeletePlant.getId()), fakeSessionID);});

    }

    @Test
    public void PlantTrackerService_deletePlant_throwsInvalidPlantException(){

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(theSession.getSessionID())).thenReturn(theSession);
        Plant toDeletePlant = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Reflector.setField(toDeletePlant, "id", 1);

        when(plantTrackerDAO.findPlantByPlantID(toDeletePlant.getId())).thenThrow(EmptyResultDataAccessException.class);

        Assertions.assertThrows(InvalidPlantException.class, ()->{plantTrackerService.deletePlant(Integer.toString(toDeletePlant.getId()), theSession.getSessionID());});

    }

    //Tests for public void confirmDeviceRegistration(Device theDevice);

    //Tests for public void updateTimestamp(Device theDevice);

    //Tests for public Plant updatePlantImage(String plantID, String sessionID, MultipartFile theFile) throws IOException;

}
