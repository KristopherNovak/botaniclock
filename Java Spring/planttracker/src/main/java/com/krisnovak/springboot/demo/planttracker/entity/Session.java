package com.krisnovak.springboot.demo.planttracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.service.RandomString;
import jakarta.persistence.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseCookie;

/**
 * Class used to represent a session linked to an account
 */
@Entity
@Table(name="session")
public class Session {

    //Constant used to set cookie length (in number of characters)
    public static final int MAXIMUM_SESSION_ID_LENGTH = 125;

    //Constant used to set age of cookies (in seconds)
    public static final int MAXIMUM_COOKIE_AGE = 60 * 100;
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    //The ID of the session provided by the cookie
    @Column(name="session_id")
    String sessionID;

    //How long the cookie is valid for
    @Column(name="max_age")
    int maxAge;

    //When the cookie was created in seconds
    @Column(name="time_created")
    long timeCreated;

    //The account linked to the session
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name="account_id")
    @JsonIgnore
    private Account account;

    public Session(){}

    /**
     * Function that creates a new session
     * @param theAccount The account to link to the exception
     * @param plantTrackerDAO A data access object to find the account
     * @throws InvalidAccountException Thrown if the provided account is not valid (the account
     * does not have a username and password that matches one in the database)
     */
    public Session(Account theAccount, PlantTrackerDAO plantTrackerDAO) throws InvalidAccountException {
        this.id = 0;
        this.sessionID = RandomString.generateRandomString(MAXIMUM_SESSION_ID_LENGTH);
        this.timeCreated = System.currentTimeMillis() / 1000;
        this.maxAge = MAXIMUM_COOKIE_AGE;
        this.account = Account.managedInstance(theAccount, plantTrackerDAO);
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String getSessionID() {return sessionID;}

    public void setSessionID(String sessionID) {this.sessionID = sessionID;}

    public Account getAccount() {
        return account;
    }

    private void setAccount(Account theAccount){this.account = theAccount;}

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                "maxAge=" + maxAge +
                "timeCreated=" + timeCreated +
                ", sessionID='" + sessionID + '\'' +
                '}';
    }

    /**
     * Function that checks if a session is expired
     * @return true if a session is expired and false if it is not expired
     */
    public boolean isExpiredSession(){
        long currentTime = System.currentTimeMillis()/1000;
        long startingTime = this.getTimeCreated();
        long timeDifference = currentTime-startingTime;
        if(timeDifference > this.getMaxAge()){
            return true;
        }
        return false;
    }

    /**
     * Function that returns a managed instance of a Session
     * @param sessionID Text of the Session ID in the cookie provided by the client request
     * @param plantTrackerDAO The Data Access Object used to retrieve the managed session from the database
     * @return A managed session
     * @throws InvalidSessionException Thrown if the session ID is not linked to a valid session
     */
    public static Session managedInstance(String sessionID, PlantTrackerDAO plantTrackerDAO) throws InvalidSessionException {
        try{return plantTrackerDAO.findSessionBySessionID(sessionID);
        }catch(EmptyResultDataAccessException | NoResultException e){
            throw new InvalidSessionException("Session with the provided ID not located in the database");
        }
    }

    /**
     * Function that creates a response cookie using the configured fields of the session
     * @return A response cookie
     */
    public ResponseCookie getResponseCookie(){
        return ResponseCookie.from("sessionId", this.sessionID)
                .path("/")
                .sameSite("lax")
                .maxAge(this.maxAge)
                .httpOnly(true)
                .build();
    }

    /**
     * Function that creates an expired cookie
     * @return An expired cookie
     */
    public static ResponseCookie getExpiredCookie(){
        return ResponseCookie.from("sessionId", null)
                .path("/")
                .sameSite("lax")
                .maxAge(0)
                .httpOnly(true)
                .build();
    }
}
