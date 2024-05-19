package com.krisnovak.springboot.demo.planttracker.dao;

import com.krisnovak.springboot.demo.planttracker.entity.Account;
import com.krisnovak.springboot.demo.planttracker.entity.Device;
import com.krisnovak.springboot.demo.planttracker.entity.Plant;
import com.krisnovak.springboot.demo.planttracker.entity.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

//TODO: Need to add what these throw
public interface PlantTrackerDAO {

    /**
     * Function that returns a managed account if the associated email and passwordCurrent match
     * an account in the database
     * @param theAccount The account to find in the database
     * @return A managed account
     * @throws EmptyResultDataAccessException Thrown if no account exists with the provided email and passwordCurrent
     */
    public Account findAccount(Account theAccount);

    /**
     * Function that adds a new account to the database using the provided email and passwordNew fields
     * @param theAccount The account to add to the database
     * @return The provided account
     * @throws DataIntegrityViolationException Thrown if the account is a duplicate,
     * if one of the variables in Account is empty or null,
     * or if one of the variables in Account is too long for the database
     */
    public Account add(Account theAccount);

    /**
     * Function that updates an account in the database
     * @param theAccount The account to update in the database
     * @return A managed version of the provided account
     * @throws DataIntegrityViolationException Thrown if one of the variables in Account is empty or null,
     * or if one of the variables in Account is too long for the database
     */
    public Account save(Account theAccount);

    /**
     * Function that deletes an account in the database
     * @param theAccount The account to delete in the database
     * @return The provided account
     */
    public Account delete(Account theAccount);

    /**
     * Function that finds and returns a managed session by the provided session ID
     * @param sessionID The text of the field Session ID provided in the cookie of the client request
     * @return A managed session
     * @throws EmptyResultDataAccessException Thrown if no session linked to the session ID exists
     */
    public Session findSessionBySessionID(String sessionID);

    /**
     * Function that finds and returns a managed plant by the provided plant ID
     * @param plantID The ID of a plant in the database
     * @return A managed plant
     * @throws EmptyResultDataAccessException Thrown if no plant associated with the Plant ID exists
     */
    public Plant findPlantByPlantID(int plantID);

    /**
     * Function that returns all plants linked to an account
     * @param theAccount The account for whom plants are being retrieved
     * @return A list of all managed plants linked to an account
     */
    public List<Plant> findAllPlants(Account theAccount);
    /**
     * Function that returns all plants in the database
     * @return A list of all managed plants in the database
     */
    public List<Plant> findAllPlants();

    /**
     * A function that saves a provided plant
     * @param thePlant The plant to save
     * @return A managed version of the provided plant
     * @throws DataIntegrityViolationException Thrown if one of the variables in Plant is too long for the database
     */
    public Plant save(Plant thePlant);

    /**
     * A function that adds a provided plant
     * @param thePlant The plant to add
     * @return The provided plant (which should be managed now)
     * @throws DataIntegrityViolationException Thrown if one of the variables in Plant is too long for the database
     */
    public Plant add(Plant thePlant);

    /**
     * A function that deletes a provided plant
     * @param thePlant The plant to delete
     * @return The provided plant
     */

    public Plant delete(Plant thePlant);

    /**
     * Function that finds and returns a managed plant associated with a particular Plant registration ID
     * @param registrationID The registration ID of a plant
     * @return A managed plant linked to thee provided registration ID
     * @throws EmptyResultDataAccessException Thrown if no plant linked to the provided registration ID is found
     */
    public Plant findPlantByRegistrationID(String registrationID);

    /**
     * Function that deletes a provided session
     * @param theSession The session to delete from the database
     * @return The provided session
     */
    public Session delete(Session theSession);

}
