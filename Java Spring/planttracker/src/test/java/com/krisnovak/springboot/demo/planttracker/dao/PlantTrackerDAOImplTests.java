package com.krisnovak.springboot.demo.planttracker.dao;

import com.krisnovak.springboot.demo.planttracker.PlanttrackerApplication;
import com.krisnovak.springboot.demo.planttracker.Reflector;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAOImpl;
import com.krisnovak.springboot.demo.planttracker.entity.Account;
import com.krisnovak.springboot.demo.planttracker.entity.Plant;
import com.krisnovak.springboot.demo.planttracker.entity.Session;
import jakarta.persistence.NoResultException;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class PlantTrackerDAOImplTests {

    private PlantTrackerDAO plantTrackerDAO;

    @Autowired
    public PlantTrackerDAOImplTests(PlantTrackerDAO plantTrackerDAO) {
        this.plantTrackerDAO = plantTrackerDAO;
    }

    // tests for public Account findAccount(Account theAccount);
    @Test
    public void plantTrackerDAO_findAccount_ReturnsAccount(){

        String email = "test@test.com";
        String password = "password";

        Account theAccount = new Account(email, password);

        plantTrackerDAO.add(theAccount);

        Account foundAccount = plantTrackerDAO.findAccount(theAccount);

        Assertions.assertNotNull(foundAccount);
        Assertions.assertEquals(foundAccount.getEmail(), email);
        Assertions.assertEquals(foundAccount.getPasswordCurrent(), password);
        Assertions.assertTrue(foundAccount.getId() > 0);

    }

    @Test
    public void plantTrackerDAO_findAccount_ThrowsNoResultExceptionEmail(){

        String email1 = "test@test.com";
        String password = "password";

        Account theAccount = new Account(email1, password);

        plantTrackerDAO.add(theAccount);

        String email2 = "tset@tset.com";
        Account anotherAccount = new Account(email2, password);

        Assertions.assertThrows(NoResultException.class, ()->{plantTrackerDAO.findAccount(anotherAccount);});

    }

    @Test
    public void plantTrackerDAO_findAccount_ThrowsNoResultExceptionPassword(){

        String email = "test@test.com";
        String password1 = "password";
        String password2 = "password2";

        Account theAccount = new Account(email, password1);

        plantTrackerDAO.add(theAccount);

        Account anotherAccount = new Account(email, password2);

        Assertions.assertThrows(NoResultException.class, ()->{plantTrackerDAO.findAccount(anotherAccount);});

    }

    //tests for public Account add(Account theAccount);
    //TODO: All potential issues with empty, too long, duplicate account
    @Test
    public void plantTrackerDAO_addAccount_returnsAccountWithNonZeroID(){
        //Create a fake account
        Account theAccount = new Account("test", "password");

        plantTrackerDAO.add(theAccount);

        Assertions.assertNotEquals(theAccount.getId(), 0);

    }

    //tests for public Account save(Account theAccount);
    @Test
    public void plantTrackerDAO_saveAccount_returnsAccountAndUpdates(){

        //Create account and add to database
        Account theAccount = new Account("test", "password");
        plantTrackerDAO.add(theAccount);

        //Create a simulated updated account with a new password
        Account updatedAccount = new Account("test", "newpassword");
        Reflector.setField(updatedAccount, "id", theAccount.getId());

        //Attempt to save the account
        plantTrackerDAO.save(updatedAccount);

        Account foundAccount = plantTrackerDAO.findAccount(updatedAccount);

        Assertions.assertNotNull(foundAccount);
        Assertions.assertEquals(foundAccount.getEmail(), "test");
        Assertions.assertEquals(foundAccount.getPasswordCurrent(), "newpassword");
        Assertions.assertTrue(foundAccount.getId() > 0);

        //Attempt to use the original un-updated credentials
        Account badAccount = new Account("test", "password");

        //Attempting to find the original email and password fails
        Assertions.assertThrows(NoResultException.class, ()->{plantTrackerDAO.findAccount(badAccount);});


    }

    //tests for public Account delete(Account theAccount);
    @Test
    public void plantTrackerDAO_deleteAccount_deletesAccount(){
        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        plantTrackerDAO.delete(newAccount);
        Assertions.assertThrows(NoResultException.class, ()->{plantTrackerDAO.findAccount(newAccount);});
    }

    //tests for public Session findSessionBySessionID(String sessionID);
    @Test
    public void plantTrackerDAO_findSessionBySessionID_findsSession(){

        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        Session newSession = new Session(newAccount, plantTrackerDAO);
        newAccount.addSession(newSession);

        Session foundSession = plantTrackerDAO.findSessionBySessionID(newSession.getSessionID());

        Assertions.assertEquals(foundSession.getSessionID(), newSession.getSessionID());
    }

    @Test
    public void plantTrackerDAO_findSessionBySessionID_throwsNoResultException(){

        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        Session newSession = new Session(newAccount, plantTrackerDAO);
        newSession.setSessionID("fakeSessionID");
        newAccount.addSession(newSession);

        Assertions.assertThrows(NoResultException.class, ()->{plantTrackerDAO.findSessionBySessionID("notFakeSessionID");});
        Assertions.assertNotNull(plantTrackerDAO.findSessionBySessionID("fakeSessionID"));
    }

    //tests for public Plant findPlantByPlantID(int plantID);
    @Test
    public void plantTrackerDAO_findPlantByPlantID_returnsPlant(){

        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        Session newSession = new Session(newAccount, plantTrackerDAO);
        newAccount.addSession(newSession);

        Plant newPlant = new Plant(newSession.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant);

        Plant foundPlant = plantTrackerDAO.findPlantByPlantID(newPlant.getId());

        Assertions.assertEquals(foundPlant.getId(), newPlant.getId());
        Assertions.assertTrue(foundPlant.getId() > 0);
    }

    @Test
    public void plantTrackerDAO_findPlantByPlantID_throwsNoResultException(){

        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        Session newSession = new Session(newAccount, plantTrackerDAO);
        newAccount.addSession(newSession);

        Plant newPlant = new Plant(newSession.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant);

        Assertions.assertThrows(NoResultException.class, ()->{
            plantTrackerDAO.findPlantByPlantID(newPlant.getId()+1);
        });

        Assertions.assertNotNull(plantTrackerDAO.findPlantByPlantID(newPlant.getId()));
    }

    //tests for public List<Plant> findAllPlants(Account theAccount);
    @Test
    public void plantTrackerDAO_findAllPlants_returnsListOnlyLinkedToAccount(){

        Account newAccount1 = new Account("test", "password");
        plantTrackerDAO.add(newAccount1);

        Session newSession1 = new Session(newAccount1, plantTrackerDAO);
        newAccount1.addSession(newSession1);

        Plant newPlant1 = new Plant(newSession1.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant1);

        Account newAccount2 = new Account("test2", "password2");
        plantTrackerDAO.add(newAccount2);

        Session newSession2 = new Session(newAccount2, plantTrackerDAO);
        newAccount2.addSession(newSession2);

        Plant newPlant2 = new Plant(newSession2.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant2);

        Plant newPlant3 = new Plant(newSession1.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant3);

        List<Plant> thePlants = plantTrackerDAO.findAllPlants(newAccount1);

        Assertions.assertEquals(thePlants.size(), 2);
        Assertions.assertEquals(newPlant1.getId(), thePlants.get(0).getId());
        Assertions.assertEquals(newPlant3.getId(), thePlants.get(1).getId());

    }

    @Test
    public void plantTrackerDAO_findAllPlants_returnsEmptyList(){

        Account newAccount1 = new Account("test", "password");
        plantTrackerDAO.add(newAccount1);

        List<Plant> thePlants = plantTrackerDAO.findAllPlants(newAccount1);

        Assertions.assertEquals(thePlants.size(), 0);

    }

    //tests for public List<Plant> findAllPlants();
    @Test
    public void plantTrackerDAO_findAllPlants_returnsListOfAllPlants(){

        Account newAccount1 = new Account("test", "password");
        plantTrackerDAO.add(newAccount1);

        Session newSession1 = new Session(newAccount1, plantTrackerDAO);
        newAccount1.addSession(newSession1);

        Plant newPlant1 = new Plant(newSession1.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant1);

        Account newAccount2 = new Account("test2", "password2");
        plantTrackerDAO.add(newAccount2);

        Session newSession2 = new Session(newAccount2, plantTrackerDAO);
        newAccount2.addSession(newSession2);

        Plant newPlant2 = new Plant(newSession2.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant2);

        Plant newPlant3 = new Plant(newSession1.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant3);

        List<Plant> thePlants = plantTrackerDAO.findAllPlants();

        Assertions.assertEquals(thePlants.size(), 3);
        Assertions.assertEquals(newPlant1.getId(), thePlants.get(0).getId());
        Assertions.assertEquals(newPlant2.getId(), thePlants.get(1).getId());
        Assertions.assertEquals(newPlant3.getId(), thePlants.get(2).getId());

    }

    //tests for public Plant save(Plant thePlant);
    @Test
    public void plantTrackerDAO_savePlant_returnsPlantAndUpdates(){

        //Create a new plant to add to the database
        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        Session newSession = new Session(newAccount, plantTrackerDAO);
        newAccount.addSession(newSession);

        Plant newPlant = new Plant(newSession.getSessionID(), plantTrackerDAO);
        newPlant.setPlantName("plantName1");

        //Add plant to the database
        plantTrackerDAO.add(newPlant);

        //Create a simulated detached updated plant with an updated name
        Plant newPlant2 = new Plant(newSession.getSessionID(), plantTrackerDAO);
        newPlant2.setPlantName("plantName2");
        newPlant2.setLastWatered(newPlant.getLastWatered());
        newPlant2.setWateringInterval(newPlant.getWateringInterval());
        Reflector.setField(newPlant2, "id", newPlant.getId());
        Reflector.setField(newPlant2, "registrationID", newPlant.getRegistrationID());
        Reflector.setField(newPlant2, "account", newPlant.getAccount());

        //Attempt to save the account
        plantTrackerDAO.save(newPlant2);

        Assertions.assertEquals("plantName2", newPlant.getPlantName());

        Plant foundPlant = plantTrackerDAO.findPlantByPlantID(newPlant.getId());
        Assertions.assertNotNull(foundPlant);
        Assertions.assertEquals("plantName2", foundPlant.getPlantName());

    }


    //tests for public Plant add(Plant thePlant);

    @Test
    public void plantTrackerDAO_addPlant_returnsValidPlant(){

        //Create a new plant to add to the database
        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        Session newSession = new Session(newAccount, plantTrackerDAO);
        newAccount.addSession(newSession);

        Plant newPlant = new Plant(newSession.getSessionID(), plantTrackerDAO);
        String plantName = newPlant.getPlantName();
        LocalDate lastWatered = newPlant.getLastWatered();
        int wateringInterval = newPlant.getWateringInterval();
        String registrationID = newPlant.getRegistrationID();
        Account theAccount = newPlant.getAccount();

        //Add plant to the database
        plantTrackerDAO.add(newPlant);

        Assertions.assertTrue(newPlant.getId() > 0);
        Assertions.assertEquals(newPlant.getPlantName(), plantName);
        Assertions.assertEquals(newPlant.getLastWatered(), lastWatered);
        Assertions.assertEquals(newPlant.getWateringInterval(), wateringInterval);
        Assertions.assertEquals(newPlant.getRegistrationID(), registrationID);
        Assertions.assertEquals(newPlant.getAccount(), theAccount);

    }

    //tests for public Plant delete(Plant thePlant);

    @Test
    public void plantTrackerDAO_deletePlant_returnsDeletedPlant(){

        //Create a new plant to add to the database
        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        Session newSession = new Session(newAccount, plantTrackerDAO);
        newAccount.addSession(newSession);

        Plant newPlant = new Plant(newSession.getSessionID(), plantTrackerDAO);

        //Add plant to the database
        plantTrackerDAO.add(newPlant);

        Plant deletedPlant = plantTrackerDAO.delete(newPlant);

        Assertions.assertThrows(NoResultException.class, ()->{plantTrackerDAO.findPlantByPlantID(deletedPlant.getId());});

    }

    //tests for public Plant findPlantByRegistrationID(String registrationID);
    @Test
    public void plantTrackerDAO_findPlantByRegistrationID_returnsPlant(){

        Account newAccount = new Account("test", "password");
        plantTrackerDAO.add(newAccount);

        Session newSession = new Session(newAccount, plantTrackerDAO);
        newAccount.addSession(newSession);

        Plant newPlant = new Plant(newSession.getSessionID(), plantTrackerDAO);
        plantTrackerDAO.add(newPlant);

        Plant foundPlant = plantTrackerDAO.findPlantByRegistrationID(newPlant.getRegistrationID());

        Assertions.assertEquals(foundPlant.getRegistrationID(), newPlant.getRegistrationID());
    }

}
