package com.krisnovak.springboot.demo.planttracker.dao;

import com.krisnovak.springboot.demo.planttracker.entity.Account;
import com.krisnovak.springboot.demo.planttracker.entity.Plant;
import com.krisnovak.springboot.demo.planttracker.entity.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlantTrackerDAOImpl implements PlantTrackerDAO{

    private EntityManager entityManager;
    @Autowired
    public PlantTrackerDAOImpl(EntityManager theEntityManager){
        entityManager = theEntityManager;
    }

    public Account findAccount(Account theAccount){
        //Check that an actual account was provided
        if(theAccount == null)
            throw new NullPointerException("No account was provided");

        //Create query that will locate an account with a particular email and password
        String queryContent = "FROM Account WHERE email = :email AND passwordCurrent =: passwordCurrent";
        TypedQuery<Account> theQuery = entityManager.createQuery(queryContent, Account.class);
        theQuery.setParameter("email", theAccount.getEmail());
        theQuery.setParameter("passwordCurrent", theAccount.getPasswordCurrent());

        //return the resulting account
        return theQuery.getSingleResult();
    }

    //saveNewAccount adds a new account to the database
    @Override
    public Account add(Account theAccount){

        //If the account has a new password, then set its current password to the new password
        if(theAccount.getPasswordNew() != null)
            theAccount.setPasswordCurrent(theAccount.getPasswordNew());

        //Add the account to the database
        entityManager.persist(theAccount);

        return theAccount;
    }

    @Override
    //Should only be performed with a non-managed entity
    public Account save(Account theAccount){

        if(theAccount == null){
            throw new NullPointerException("No account was provided");
        }

        //If the account has a new password, then set its current password to the new password
        if(theAccount.getPasswordNew() != null)
            theAccount.setPasswordCurrent(theAccount.getPasswordNew());

        //Update the account in the database
        entityManager.merge(theAccount);

        return theAccount;
    }

    @Override
    public Account delete(Account theAccount) {

        if(theAccount == null){
            throw new NullPointerException("No account was provided");
        }

        //remove the account from the database
        entityManager.remove(theAccount);

        return theAccount;
    }

    @Override
    public Session findSessionBySessionID(String sessionID) {

        //Create query to find all sessions that have a given session ID
        String queryContent = "FROM Session WHERE sessionID=:sessionID";
        TypedQuery<Session> theQuery = entityManager.createQuery(queryContent, Session.class);
        theQuery.setParameter("sessionID", sessionID);

        //return the session
        return theQuery.getSingleResult();
    }

    @Override
    public Plant findPlantByPlantID(int plantID) {

        //Create query to find a plant with a particular plant ID
        TypedQuery<Plant> theQuery = entityManager.createQuery("FROM Plant WHERE id=:id", Plant.class);
        theQuery.setParameter("id", plantID);

        //Return back the requested plant
        return theQuery.getSingleResult();
    }

    @Override
    public List<Plant> findAllPlants(Account theAccount) {

        if(theAccount == null){
            throw new NullPointerException("The provided account was null");
        }

        //Create a query to return back all plants linked to the provided account
        TypedQuery<Plant> theQuery = entityManager.createQuery("FROM Plant WHERE account.email=:email ORDER BY id", Plant.class);
        theQuery.setParameter("email", theAccount.getEmail());

        //Return back all the plants that were retrieved
        return theQuery.getResultList();
    }

    //TODO: Replace this with a function with a query to retrieve only the plants that are overdue
    @Override
    public List<Plant> findAllPlants(){
        //Create query for all the plants in the database
        TypedQuery<Plant> theQuery = entityManager.createQuery("FROM Plant", Plant.class);

        //return back all the plants in the database
        return theQuery.getResultList();
    }

    @Override
    public Plant add(Plant thePlant){

        //Add the plant to the database
        entityManager.persist(thePlant);

        //return the plant
        return thePlant;
    }

    //Should only be performed with a non-managed entity
    @Override
    public Plant save(Plant thePlant){
        //Save the plant to the database
        entityManager.merge(thePlant);

        //return the plant
        return thePlant;
    }

    @Override
    public Plant delete(Plant thePlant){

        //Remove the plant from the database
        entityManager.remove(thePlant);

        //return the deleted plant
        return thePlant;
    }

    @Override
    public Plant findPlantByRegistrationID(String registrationID) {
        //Create query to find the plant with the provided registration ID
        TypedQuery<Plant> theQuery = entityManager.createQuery("FROM Plant WHERE registrationID=:registrationID", Plant.class);
        theQuery.setParameter("registrationID", registrationID);

        //Retrieve a list of all plants (hopefully there is only one)
        return theQuery.getSingleResult();

    }

    public Session delete(Session theSession){
        //delete the session from the database
        entityManager.remove(theSession);

        //return back the deleted session
        return theSession;
    }

}
