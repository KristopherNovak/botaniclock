package com.krisnovak.springboot.demo.planttracker.service;

import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseCookie;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlantTrackerServiceImplTests {

    @Mock
    private PlantTrackerDAO plantTrackerDAO;

    @InjectMocks
    private PlantTrackerServiceImpl plantTrackerService;

    //Tests for public Account signUp(Account theAccount);

    //Tests for public void changePassword(Account theAccount);

    //Tests for public Account deleteAccount(Account theAccount);

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
