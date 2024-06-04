package com.krisnovak.springboot.demo.planttracker.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnovak.springboot.demo.planttracker.Reflector;
import com.krisnovak.springboot.demo.planttracker.configuration.AppConfig;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAOImpl;
import com.krisnovak.springboot.demo.planttracker.entity.*;
import com.krisnovak.springboot.demo.planttracker.service.PlantTrackerService;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


import java.io.IOException;
import java.util.List;

import static org.apache.http.client.methods.RequestBuilder.post;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = PlantTrackerRestController.class)
@AutoConfigureMockMvc(addFilters=false)
@ExtendWith(MockitoExtension.class)
public class PlantTrackerRestTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlantTrackerService plantTrackerService;

    @MockBean
    private AppConfig appConfig;

    @MockBean
    private PlantTrackerDAO plantTrackerDAO;

    @Autowired
    private ObjectMapper objectMapper;

    //Tests for @PostMapping("/session") ResponseEntity<HTTPResponseBody>
    //validateCookie(@CookieValue(name = "sessionId", defaultValue = "") String sessionId)
    @Test
    public void PlantTrackerRestController_validateCookie_ReturnsOkStatusCode() throws Exception{

        Cookie theCookie = new Cookie("sessionId", "fakeSessionID");


        //Throws an invalid session exception if anything but fakeSessionID is passed to validateCookie
        doThrow(InvalidSessionException.class).when(plantTrackerService).validateCookie(AdditionalMatchers.not(ArgumentMatchers.eq("fakeSessionID")));
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/session")
                .cookie(theCookie));

        response.andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void PlantTrackerRestController_validateCookie_Returns403() throws Exception{

        Cookie theCookie = new Cookie("sessionId", "fakeSessionID");

        doThrow(InvalidSessionException.class).when(plantTrackerService).validateCookie(ArgumentMatchers.eq("fakeSessionID"));
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/session")
                .cookie(theCookie));

        response.andExpect(MockMvcResultMatchers.status().isForbidden());
    }


    //Tests for @PostMapping("/account/login")
    // public ResponseEntity<HTTPResponseBody> logIn(@RequestBody Account theAccount)
    @Test
    public void PlantTrackerRestController_logIn_ReturnsOkStatusCode() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);
        ResponseCookie theCookie = theSession.getResponseCookie();

        when(plantTrackerService.createCookie(theAccount)).thenReturn(theCookie);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theAccount)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().value("sessionId", theSession.getSessionID()));

    }

    @Test
    public void PlantTrackerRestController_logIn_Returns400() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerService.createCookie(theAccount)).thenThrow(InvalidAccountException.class);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theAccount)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    //Tests for @PostMapping("/account/logout")
    // public ResponseEntity<HTTPResponseBody> logOut(@CookieValue(name = "sessionId", defaultValue = "") String sessionId)

    @Test
    public void PlantTrackerRestController_logOut_ReturnsOkStatusCode() throws Exception{

        Cookie theCookie = new Cookie("sessionId", "fakeSessionID");
        ResponseCookie expiredCookie = Session.getExpiredCookie();

        when(plantTrackerService.getExpiredCookie("fakeSessionID")).thenReturn(expiredCookie);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/logout")
                .cookie(theCookie));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().maxAge("sessionId", 0));

    }

    @Test
    public void PlantTrackerRestController_logOut_Returns403() throws Exception{

        Cookie theCookie = new Cookie("sessionId", "fakeSessionID");

        when(plantTrackerService.getExpiredCookie("fakeSessionID")).thenThrow(InvalidSessionException.class);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/logout")
                .cookie(theCookie));

        response.andExpect(MockMvcResultMatchers.status().isForbidden());

    }

    //Tests for @PostMapping("/account/signup")
    //public ResponseEntity<HTTPResponseBody> signUp(@RequestBody Account theAccount)

    //Tests for @PostMapping("/account/password")
    //public ResponseEntity<HTTPResponseBody> changePassword(@RequestBody Account theAccount)

    //Tests for @PostMapping("/account/delete")
    //public ResponseEntity<HTTPResponseBody> deleteAccount(@RequestBody Account theAccount)

    //Tests for @GetMapping("/plants")
    //public ResponseEntity<List<Plant>> getPlants(@CookieValue(name = "sessionId", defaultValue = "") String sessionID)

    //Tests for @GetMapping("/plants/{plantID}")
    //public ResponseEntity<Plant> getPlant(@PathVariable String plantID, @CookieValue(name = "sessionId", defaultValue = "") String sessionID)

    //Tests for @PostMapping("/plants")
    //public ResponseEntity<Plant> addPlant(@RequestBody Plant thePlant, @CookieValue(name = "sessionId", defaultValue = "") String sessionID)

    //Tests for @PutMapping("/plants")
    //public ResponseEntity<Plant> updatePlant(@RequestBody Plant thePlant, @CookieValue(name = "sessionId", defaultValue = "") String sessionID)

    // Tests for @PutMapping("/plants/{plantID}")
    //public ResponseEntity<Plant> updatePlantImage(
    // @RequestParam("file") MultipartFile file,
    // @PathVariable String plantID
    // @CookieValue(name = "sessionId", defaultValue = "") String sessionID)

    // Tests for @DeleteMapping("/plants/{plantID}")
    //public ResponseEntity<Plant> deletePlant(@PathVariable String plantID, @CookieValue(name = "sessionId", defaultValue = "") String sessionID) throws IOException

    //Tests for @PostMapping("/devices")
    //public ResponseEntity<HTTPResponseBody> registerDevice(@RequestBody Device theDevice)

    //Tests for @PutMapping("/devices") @Transactional
    //public ResponseEntity<HTTPResponseBody> updateTimestamp(@RequestBody Device theDevice)

}
