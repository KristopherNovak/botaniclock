package com.krisnovak.springboot.demo.planttracker.dao;

import com.krisnovak.springboot.demo.planttracker.PlanttrackerApplication;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAOImpl;
import com.krisnovak.springboot.demo.planttracker.entity.Account;
import com.krisnovak.springboot.demo.planttracker.entity.Plant;
import com.krisnovak.springboot.demo.planttracker.entity.Session;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;

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
        
    }


    //tests for public Account delete(Account theAccount);

    //tests for public Session findSessionBySessionID(String sessionID);

    //tests for public Plant findPlantByPlantID(int plantID);

    //tests for public List<Plant> findAllPlants(Account theAccount);

    //tests for public List<Plant> findAllPlants();

    //tests for public Plant save(Plant thePlant);

    //tests for public Plant add(Plant thePlant);

    //tests for public Plant delete(Plant thePlant);

    //tests for public Plant findPlantByRegistrationID(String registrationID);

    //tests for public Session delete(Session theSession);



}
