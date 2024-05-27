package com.krisnovak.springboot.demo.planttracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import jakarta.persistence.*;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents the account of a user
 */
@Entity
@Table(name="account")
public class Account {

    //The ID of the account in the database
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    @JsonIgnore
    private int id;

    //The email for the account
    @Column(name="email")
    @JsonProperty("email")
    private String email;

    //The current password of the account
    @Column(name="password")
    @JsonProperty("passwordCurrent")
    private String passwordCurrent;

    //A requested new password to add to the account
    @Transient
    @JsonProperty("passwordNew")
    private String passwordNew;

    //A list of all the plants owned by the account (NOTE: this is set to lazy fetch)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Plant> plants;

    //A list of all the sessions/cookies that exist for the account
    @OneToMany(mappedBy = "account", cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval = true)
    @JsonIgnore
    private List<Session> sessions;

    private Account(){}

    //Creates a new account
    public Account(String email, String password) {
        this.id = 0;
        this.email = email;
        this.passwordNew = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordCurrent() {
        return passwordCurrent;
    }

    public String getPasswordNew() {
        return passwordNew;
    }

    private void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordCurrent(String password) {
        this.passwordCurrent = password;
    }

    public void setPasswordNew(String passwordNew) {
        this.passwordNew= passwordNew;
    }

    @Override
    public String toString() {
        return "Account{" +
                ", email='" + email + '\'' +
                ", password='" + passwordCurrent + '\'' +
                ", password_new='" + passwordNew + '\'' +
                '}';
    }

    /**
     * Function that adds a new plant to the account
     * @param tempPlant The plant to add to the account
     */
    public void addPlant(Plant tempPlant){
        if(plants == null)
            plants = new ArrayList<Plant>();

        plants.add(tempPlant);
    }

    /**
     * Function that adds a new session/cookie to the account
     * @param tempSession The session to add to the account
     */
    public void addSession(Session tempSession){
        if(sessions == null)
            sessions = new ArrayList<Session>();

        sessions.add(tempSession);
    }

    /**
     * Function that deletes any expired sessions associated with an account
     */
    public void deleteExpiredSessions(){

        //Determine the sessions that are expired
        List<Session> badSessionList = new ArrayList<Session>();
        for(Session s : sessions){
            if(s.isExpiredSession()){
                badSessionList.add(s);
            }
        }

        //Remove the sessions from the list
        for(Session s: badSessionList){
            sessions.remove(s);
        }
    }

    public Session removeSession(Session theSession){

        int removeSessionIndex = -1;
        for(int i = 0; i < sessions.size(); i++){
            if(sessions.get(i).getSessionID().equals(theSession.getSessionID())){
                removeSessionIndex = i;
                break;
            }
        }

        if(removeSessionIndex >= 0) sessions.remove(removeSessionIndex);

        return theSession;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public int getId() {
        return id;
    }

    /**
     * Function that returns a managed instance of an account
     * @param theAccount The account for whom to pull up a managed instance
     * @param plantTrackerDAO The DAO which can be used to find the account
     * @return A managed copy of the account
     * @throws InvalidAccountException Thrown if no account with a matching email and a matching current password is found
     */
    public static Account managedInstance(Account theAccount, PlantTrackerDAO plantTrackerDAO) throws InvalidAccountException {
        try{return plantTrackerDAO.findAccount(theAccount);
        }catch(EmptyResultDataAccessException | NoResultException e){
            throw new InvalidAccountException("Account with the provided credentials not located in the database");
        }
    }

    /**
     * Returns true if the ID is the same, meaning unmanaged accounts will register as the same
     * @param o The supposed account that is being compared with this account
     * @return True if the accounts have the same ID, false if the accounts do not have the same ID
     */
    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }

        if(!o.getClass().equals(Account.class)){
            return false;
        }

        Account otherAccount = (Account) o;

        if(otherAccount.getId() == this.id){
            return true;
        }

        return false;
    }

    @Override
    public int hashCode(){
        return Integer.hashCode(this.getId());
    }

}
