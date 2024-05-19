package com.krisnovak.springboot.demo.planttracker.rest;

import com.krisnovak.springboot.demo.planttracker.entity.*;
import com.krisnovak.springboot.demo.planttracker.service.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@CrossOrigin()
@RestController
@RequestMapping("/api/v1")
public class PlantTrackerRestController {

    private PlantTrackerService plantTrackerService;

    @Autowired
    public PlantTrackerRestController(PlantTrackerService thePlantTrackerService) {
        plantTrackerService = thePlantTrackerService;
    }

    /**
     * Function indicating whether a provided cookie is valid. A cookie is valid if it is stored in the database and not expired
     * @param sessionId The text provided by the cookie in its sessionID field
     * @return an HTTP response with an OK status verifying that the provided cookie is valid.
     * @throws InvalidSessionException Thrown if the provided session is invalid (it's not in the database or is expired)
     */

    @PostMapping("/session")
    public ResponseEntity<HTTPResponseBody> validateCookie(@CookieValue(name = "sessionId", defaultValue = "") String sessionId) {

        //Verify that cookie is stored in the database and not expired
        plantTrackerService.validateCookie(sessionId);

        //Create the body of the HTTP response
        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.OK, "COOKIE_VALID");

        //Send the HTTP response to the client
        return ResponseEntity.ok().body(httpResponseBody);

    }

    /**
     * Function that creates a cookie/token if the provided account is valid (i.e., it matches an account in the database)
     * @param theAccount- An account with an email and current password (passwordCurrent) to be checked
     * @return An HTTP response with an OK status and a new cookie
     * @throws InvalidAccountException Thrown if the email or password do not match that of an account in the database
     */
    @PostMapping("/account/login")
    public ResponseEntity<HTTPResponseBody> logIn(@RequestBody Account theAccount) {

        //Attempt to create a cookie for the provided account
        String cookie = plantTrackerService.createCookie(theAccount).toString();

        //Create the body of the HTTP response
        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.OK, "ACCOUNT_VALID");

        //Send the HTTP response to the client
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie).body(httpResponseBody);

    }

    /**
     * Function that gets an expired cookie
     * @param sessionId The text provided by the cookie in its sessionID field
     * @return An HTTP response with an OK status and an expired cookie
     * @throws InvalidSessionException Thrown in the provided cookie is no longer stored in the database
     */
    @PostMapping("/account/logout")
    public ResponseEntity<HTTPResponseBody> logOut(@CookieValue(name = "sessionId", defaultValue = "") String sessionId) {

        //Create expired cookie to give to client and remove old cookie from database (if it exists)
        ResponseCookie expiredCookie = plantTrackerService.getExpiredCookie(sessionId);

        //Create body of message to send back to client
        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.OK, "LOGGED_OUT");

        //send message with body and cookie to client (HTTP 200)
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, expiredCookie.toString()).body(httpResponseBody);

    }

    /**
     * Function that attempts to add a new account to the database
     * @param theAccount An account with an email and a new password (passwordNew) to be added
     * @return An HTTP response with an OK status and a new cookie
     * @throws DataIntegrityViolationException Thrown if the account is a duplicate account, has a null email or password, or has an email or password that is too long
     */
    @PostMapping("/account/signup")
    public ResponseEntity<HTTPResponseBody> signUp(@RequestBody Account theAccount) {

        //Attempt to sign the user up
        Account newAccount = plantTrackerService.signUp(theAccount);

        //Create body of message to send back to client
        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.OK, "SIGNED_UP");

        //Create a new cookie for the account
        String cookie = plantTrackerService.createCookie(newAccount).toString();

        //Send HTTP message back to the client
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie).body(httpResponseBody);
    }

    /**
     * Function that attempts to change a password for an account
     * @param theAccount Account with an email, a current password to be verified (passwordCurrent), and a new password to be added (passwordNew)
     * @return An HTTP response with an OK status
     * @throws InvalidAccountException Throws if the email or current password do not match that of an account in the database
     */
    @PostMapping("/account/password")
    public ResponseEntity<HTTPResponseBody> changePassword(@RequestBody Account theAccount) {

        //Attempt to change password
        plantTrackerService.changePassword(theAccount);

        //Create body of message to send back to client
        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.OK, "PASSWORD_CHANGED");

        //return message back to the client
        return ResponseEntity.ok().body(httpResponseBody);
    }

    /**
     * Function that attempts to delete an account
     * @param theAccount Account with an email and a current password (passwordCurrent) to be verified
     * @return An HTTP response with an OK status
     * @throws InvalidAccountException Throws if the email or current password do not match that of an account in the database
     */
    @PostMapping("/account/delete")
    public ResponseEntity<HTTPResponseBody> deleteAccount(@RequestBody Account theAccount) {

        //Attempt to delete account
        plantTrackerService.deleteAccount(theAccount);

        //Create body of message to send back to client
        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.OK, "ACCOUNT_DELETED");

        //Send message with body to client
        return ResponseEntity.ok().body(httpResponseBody);
    }

    /**
     * Function that gets information about all plants linked to an account associated with a cookie/session (via the provided session ID)
     * @param sessionID The text provided by the cookie in its sessionID field
     * @return A list of all the plants linked to an account associated with the provided session ID
     * @throws InvalidSessionException Thrown if the provided session ID does not match with any sessions in the database
     */
    @GetMapping("/plants")
    public ResponseEntity<List<Plant>> getPlants(@CookieValue(name = "sessionId", defaultValue = "") String sessionID) {

        //Return all the plants linked to the provided session ID
        return ResponseEntity.ok().body(plantTrackerService.findPlants(sessionID));
    }

    /**
     * Function that returns information about a particular plant within the database
     * @param plantID The ID of the plant stored within the database (should be an integer)
     * @param sessionID The text provided by the cookie in its sessionID field
     * @return The requested plant and its associated information
     * @throws InvalidSessionException Thrown if the provided session ID does not match
     * with any sessions in the database
     * @throws InvalidPlantException Thrown if the plant ID is not an integer,
     * if the plant with the associated ID does not exist in the database,
     * or if the plant does exist but is linked with a different account
     */
    @GetMapping("/plants/{plantID}")
    public ResponseEntity<Plant> getPlant(@PathVariable String plantID, @CookieValue(name = "sessionId", defaultValue = "") String sessionID){

        //Return the requested plant with the plant ID and linked to the session ID
        return ResponseEntity.ok().body(plantTrackerService.findPlantByPlantID(plantID, sessionID));

    }

    /**
     * Function that attempts to add a plant to the database
     * (registration ID and Plant ID will be set by this function)
     * @param thePlant The plant to add to the database
     * @param sessionID The text provided by the cookie in its sessionID field
     * @return The new plant (with its new registration ID and Plant ID)
     * @throws InvalidSessionException Thrown if the provided session ID does not match
     * with any sessions in the database
     * @throws DataIntegrityViolationException Thrown if any of the parameters of plant are too long
     */
    @PostMapping("/plants")
    public ResponseEntity<Plant> addPlant(@RequestBody Plant thePlant, @CookieValue(name = "sessionId", defaultValue = "") String sessionID) {

        //Add a new plant to the database
        Plant newPlant = plantTrackerService.addPlant(thePlant, sessionID);

        //Send an HTTP response to the client with the new plant
        return ResponseEntity.ok().body(newPlant);
    }

    /**
     * Function that attempts to update a plant in the database
     * if it exists and is linked to the same account as the provided session
     * (registration ID will not be updated by this function nor will image, imageKey, or imageURL)
     * @param thePlant The plant to add to the database
     * @param sessionID The text provided by the cookie in its sessionID field
     * @return The updated plant
     * @throws InvalidSessionException Thrown if the provided session ID does not match
     * with any sessions in the database
     * @throws InvalidPlantException Thrown if the provided ID does not match any plants in the database linked to the account associated with the session/cookie
     * @throws DataIntegrityViolationException Thrown if any of the parameters of plant are too long
     */
    @PutMapping("/plants")
    public ResponseEntity<Plant> updatePlant(@RequestBody Plant thePlant, @CookieValue(name = "sessionId", defaultValue = "") String sessionID) {

        //Update the plant in the database
        Plant updatedPlant = plantTrackerService.updatePlant(thePlant, sessionID);

        //Send the updated plant to the client
        return ResponseEntity.ok().body(updatedPlant);
    }

    /**
     * Function that attempts to update the image associated with a plant by storing it in an S3 bucket
     * if the plant exists and is linked to the same account as the provided session
     * (this function only updates image-related fields)
     * @param file The file that contains the image in a multipart/form-data format
     * @param plantID The ID of the plant in the database (should be an integer)
     * @param sessionID The text provided by the cookie in its sessionID field
     * @return The updated plant with an image URL linking to the new image
     * @throws InvalidSessionException Thrown if the provided session ID does not match
     * with any sessions in the database
     * @throws InvalidPlantException Thrown if the provided ID does not match any plants in the database linked to the account associated with the session/cookie
     */
    @PutMapping("/plants/{plantID}")
    public ResponseEntity<Plant> updatePlantImage(  @RequestParam("file") MultipartFile file,
                                                    @PathVariable String plantID,
                                                    @CookieValue(name = "sessionId", defaultValue = "") String sessionID)
                                                    throws IOException{

        //Update the image of the plant linked to the plantID and session ID with the image stored
        //in the file
        Plant thePlant = plantTrackerService.updatePlantImage(plantID, sessionID, file);

        //Send an HTTP response back to the client with a plant that has an updated image URL
        return ResponseEntity.ok().body(thePlant);
    }

    /**
     * Function that attempts to delete a plant from the database
     * @param plantID The ID of the plant in the database (should be an integer)
     * @param sessionID The text provided by the cookie in its sessionID field
     * @return The information of the deleted plant
     * @throws InvalidSessionException Thrown if the provided session ID does not match
     * with any sessions in the database
     * @throws InvalidPlantException Thrown if the provided ID does not match any plants in the database linked to the account associated with the session/cookie
     */
    @DeleteMapping("/plants/{plantID}")
    public ResponseEntity<Plant> deletePlant(@PathVariable String plantID, @CookieValue(name = "sessionId", defaultValue = "") String sessionID) throws IOException{

        //Attempt to delete the plant linked with the plant ID and session ID from the database
        Plant thePlant= plantTrackerService.deletePlant(plantID, sessionID);

        //Return back an HTTP response to the client
        return ResponseEntity.ok().body(thePlant);
    }

    /**
     * Function that attempts to verify that the Plant registration ID and Account email provided by a device exist and are linked
     * Note that there is no need to actually store information about the Device in the database
     * @param theDevice The information provided by the device
     * @return An HTTP message with an OK status indicating that the email and registration ID are linked
     * @throws InvalidPlantException Thrown if no plant can be found that is associated with the provided email
     * and registrationID
     */
    @PostMapping("/devices")
    public ResponseEntity<HTTPResponseBody> registerDevice(@RequestBody Device theDevice) {

        //Confirm that the registration ID and email within the device are linked
        plantTrackerService.confirmDeviceRegistration(theDevice);

        //Create body of the HTTP response to send to the client
        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.OK, "DEVICE_VALID");

        //Send HTTP response to the client
        return ResponseEntity.ok().body(httpResponseBody);
    }

    /**
     * Function that attempts to verify that updates a timestamp of a plant if the device provides
     * a valid Account email and Plant Registration ID that are linked
     * @param theDevice The information provided by the device
     * @return An HTTP message with an OK status indicating that the timestamp was updated
     * @throws InvalidPlantException Thrown if no plant can be found that is associated with the provided email
     * and registrationID
     */
    @PutMapping("/devices")
    @Transactional
    public ResponseEntity<HTTPResponseBody> updateTimestamp(@RequestBody Device theDevice) {

        //Attempt to update the timestamp associated with the Plant whose registration ID is indicated by the device
        plantTrackerService.updateTimestamp(theDevice);

        //Create the HTTP body to send to the client
        HTTPResponseBody httpResponseBody = HTTPResponseBody.newInstance(HttpStatus.OK, "TIMESTAMP_UPDATED");

        //Send back the HTTP response to the client
        return ResponseEntity.ok().body(httpResponseBody);
    }
}
