package com.krisnovak.springboot.demo.planttracker.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.client.methods.RequestBuilder.post;
import static org.mockito.ArgumentMatchers.eq;
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
    @Test
    public void PlantTrackerRestController_signUp_ReturnsOkStatusCode() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);
        ResponseCookie theCookie = theSession.getResponseCookie();

        when(plantTrackerService.signUp(theAccount)).thenReturn(theAccount);
        when(plantTrackerService.createCookie(theAccount)).thenReturn(theCookie);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theAccount)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().value("sessionId", theSession.getSessionID()));

    }

    @Test
    public void PlantTrackerRestController_signUp_Returns400() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerService.signUp(theAccount)).thenThrow(InvalidAccountException.class);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theAccount)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    //Tests for @PostMapping("/account/password")
    //public ResponseEntity<HTTPResponseBody> changePassword(@RequestBody Account theAccount)
    @Test
    public void PlantTrackerRestController_changePassword_ReturnsOkStatusCode() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerService.changePassword(theAccount)).thenReturn(theAccount);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theAccount)));

        response.andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void PlantTrackerRestController_changePassword_Returns400() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerService.changePassword(theAccount)).thenThrow(InvalidAccountException.class);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theAccount)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    //Tests for @PostMapping("/account/delete")
    //public ResponseEntity<HTTPResponseBody> deleteAccount(@RequestBody Account theAccount)
    @Test
    public void PlantTrackerRestController_deleteAccount_ReturnsOkStatusCode() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerService.deleteAccount(theAccount)).thenReturn(theAccount);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theAccount)));

        response.andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    public void PlantTrackerRestController_deleteAccount_Returns400() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerService.deleteAccount(theAccount)).thenThrow(InvalidAccountException.class);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(theAccount)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    //Tests for @GetMapping("/plants")
    //public ResponseEntity<List<Plant>> getPlants(@CookieValue(name = "sessionId", defaultValue = "") String sessionID)

    @Test
    public void PlantTrackerRestController_getPlants_ReturnsPlants() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(ArgumentMatchers.any(String.class))).thenReturn(theSession);
        Plant plant1 = new Plant(theSession.getSessionID(), plantTrackerDAO);
        Plant plant2 = new Plant(theSession.getSessionID(), plantTrackerDAO);
        List<Plant> plants = new ArrayList<>();
        plants.add(plant1);
        plants.add(plant2);

        Cookie theCookie = new Cookie("sessionId", theSession.getSessionID());

        when(plantTrackerService.findPlants(theSession.getSessionID())).thenReturn(plants);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/plants")
                .cookie(theCookie));

        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(plants);

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json));
    }

    @Test
    public void PlantTrackerRestController_getPlants_Returns403BadSession() throws Exception{

        Cookie theCookie = new Cookie("sessionId", "fakeSessionID");

        when(plantTrackerService.findPlants("fakeSessionID")).thenThrow(InvalidSessionException.class);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/plants")
                .cookie(theCookie));

        response.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    //Tests for @GetMapping("/plants/{plantID}")
    //public ResponseEntity<Plant> getPlant(@PathVariable String plantID, @CookieValue(name = "sessionId", defaultValue = "") String sessionID)
    @Test
    public void PlantTrackerRestController_getPlant_ReturnsPlant() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(ArgumentMatchers.any(String.class))).thenReturn(theSession);
        Plant plant1 = new Plant(theSession.getSessionID(), plantTrackerDAO);

        Cookie theCookie = new Cookie("sessionId", theSession.getSessionID());

        when(plantTrackerService.findPlantByPlantID("1", theSession.getSessionID())).thenReturn(plant1);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/plants/1")
                .cookie(theCookie));

        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(plant1);

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json));
    }

    @Test
    public void PlantTrackerRestController_getPlant_Returns403BadSession() throws Exception{

        Cookie theCookie = new Cookie("sessionId", "fakeSessionID");

        when(plantTrackerService.findPlantByPlantID("1", "fakeSessionID")).thenThrow(InvalidSessionException.class);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/plants/1")
                .cookie(theCookie));

        response.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void PlantTrackerRestController_getPlant_Returns404BadPlant() throws Exception{

        Cookie theCookie = new Cookie("sessionId", "fakeSessionID");

        when(plantTrackerService.findPlantByPlantID("1", "fakeSessionID")).thenThrow(InvalidPlantException.class);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/plants/1")
                .cookie(theCookie));

        response.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    //Tests for @PostMapping("/plants")
    //public ResponseEntity<Plant> addPlant(@RequestBody Plant thePlant, @CookieValue(name = "sessionId", defaultValue = "") String sessionID)
    @Test
    public void PlantTrackerRestController_addPlant_ReturnsPlant() throws Exception{

        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(ArgumentMatchers.any(Account.class))).thenReturn(theAccount);
        Session theSession = new Session(theAccount, plantTrackerDAO);

        when(plantTrackerDAO.findSessionBySessionID(ArgumentMatchers.any(String.class))).thenReturn(theSession);
        Plant plant = new Plant(theSession.getSessionID(), plantTrackerDAO);

        Cookie theCookie = new Cookie("sessionId", theSession.getSessionID());

        when(plantTrackerService.addPlant(plant, theSession.getSessionID())).thenReturn(plant);
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/plants")
                .cookie(theCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plant)));

        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(plant);

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(json));
    }



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
