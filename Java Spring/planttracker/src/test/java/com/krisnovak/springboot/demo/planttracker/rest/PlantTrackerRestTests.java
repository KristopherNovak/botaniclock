package com.krisnovak.springboot.demo.planttracker.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnovak.springboot.demo.planttracker.entity.*;
import com.krisnovak.springboot.demo.planttracker.service.PlantTrackerService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@WebMvcTest(controllers = PlantTrackerRestController.class)
@AutoConfigureMockMvc(addFilters=false)
@ExtendWith(MockitoExtension.class)
public class PlantTrackerRestTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlantTrackerService plantTrackerService;

    @Autowired
    private ObjectMapper objectMapper;

    //Tests for @PostMapping("/session") ResponseEntity<HTTPResponseBody>
    //validateCookie(@CookieValue(name = "sessionId", defaultValue = "") String sessionId)

    //Tests for @PostMapping("/account/login")
    // public ResponseEntity<HTTPResponseBody> logIn(@RequestBody Account theAccount)

    //Tests for @PostMapping("/account/logout")
    // public ResponseEntity<HTTPResponseBody> logOut(@CookieValue(name = "sessionId", defaultValue = "") String sessionId)

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
