package com.krisnovak.springboot.demo.planttracker.service;

import com.krisnovak.springboot.demo.planttracker.Reflector;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.entity.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
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

        when(plantTrackerDAO.add(ArgumentMatchers.any(Account.class))).thenAnswer(i ->
                {
                    Account theAccount = (Account) i.getArguments()[0];
                    Reflector.setField(theAccount, "id", 1);
                    theAccount.setPasswordCurrent(theAccount.getPasswordNew());
                    return theAccount;
                });
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

        when(Account.managedInstance(theAccount, plantTrackerDAO)).thenReturn(managedAccount);

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
        when(Account.managedInstance(theAccount, plantTrackerDAO)).thenReturn(managedAccount);

        List<Plant> thePlants = new ArrayList<Plant>();
        when(plantTrackerDAO.findAllPlants(managedAccount)).thenReturn(thePlants);

        when(plantTrackerDAO.delete(ArgumentMatchers.any(Account.class))).thenAnswer(i->i.getArguments()[0]);
        Account deletedAccount = plantTrackerService.deleteAccount(theAccount);

        Assertions.assertEquals(deletedAccount.getId(), 1);
        Assertions.assertEquals(deletedAccount.getEmail(), "test");
        Assertions.assertEquals(deletedAccount.getPasswordCurrent(), "password");

    }

    //Tests for public void validateCookie(String sessionID);


    //Tests for public ResponseCookie createCookie(Account theAccount);

    //Tests for public ResponseCookie getExpiredCookie(String sessionID);

    //Tests for public List<Plant> findPlants(String sessionID);

    //Tests for public Plant findPlantByPlantID(String PlantID, String sessionID);

    //Tests for public Plant addPlant(Plant thePlant, String sessionID);

    //Tests for public Plant updatePlant(Plant thePlant, String sessionID) ;

    //Tests for public Plant deletePlant(String plantID, String sessionID);

    //Tests for public void confirmDeviceRegistration(Device theDevice);

    //Tests for public void updateTimestamp(Device theDevice);

    //Tests for public Plant updatePlantImage(String plantID, String sessionID, MultipartFile theFile) throws IOException;

}
