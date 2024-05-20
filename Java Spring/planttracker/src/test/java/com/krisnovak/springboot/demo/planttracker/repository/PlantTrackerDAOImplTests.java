package com.krisnovak.springboot.demo.planttracker.repository;

import com.krisnovak.springboot.demo.planttracker.PlanttrackerApplication;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAOImpl;
import com.krisnovak.springboot.demo.planttracker.entity.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class PlantTrackerDAOImplTests {

    private PlantTrackerDAO plantTrackerDAO;

    @Autowired
    public PlantTrackerDAOImplTests(PlantTrackerDAO plantTrackerDAO) {
        this.plantTrackerDAO = plantTrackerDAO;
    }

    @Test
    public void plantTrackerDAO_addAccount_returnsAccountWithNonZeroID(){
        //Create a fake account
        Account theAccount = new Account("test", "password");

        plantTrackerDAO.add(theAccount);

        Assertions.assertNotEquals(theAccount.getId(), 0);

    }



}
