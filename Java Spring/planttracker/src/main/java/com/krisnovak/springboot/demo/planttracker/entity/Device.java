package com.krisnovak.springboot.demo.planttracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import jakarta.persistence.*;

/**
 * Class that represents information provided by a device attempting to update a timestamp for an associated plant
 */
public class Device {

    //Registration ID of a Plant
    @JsonProperty("registrationID")
    @Transient
    private String registrationID;

    //email associated with an account
    //TODO: Change jsonproperty to accountEmail when refactoring C code for device
    @JsonProperty("accountEmail")
    @Transient
    private String accountEmail;

    public Device(){}

    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountUsername(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    @Override
    public String toString() {
        return "Device{" +
                "accountEmail=" + accountEmail +
                ", registrationID='" + registrationID + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o){
        if(o == this) return true;
        if(o == null) return false;
        if(!o.getClass().equals(Device.class)) return false;

        Device otherDevice = (Device) o;

        if(!bothNullOrEqual(otherDevice.getAccountEmail(), this.accountEmail)) return false;
        return bothNullOrEqual(otherDevice.getRegistrationID(), this.registrationID);

    }

    private boolean bothNullOrEqual(Object o1, Object o2){
        if(o1 == null && o2 == null) return true;
        if(o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }

    @Override
    public int hashCode(){
        return this.registrationID.hashCode() + this.accountEmail.hashCode()*31;
    }
}
