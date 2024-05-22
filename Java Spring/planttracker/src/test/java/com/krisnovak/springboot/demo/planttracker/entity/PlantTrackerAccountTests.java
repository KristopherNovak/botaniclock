package com.krisnovak.springboot.demo.planttracker.entity;

import com.krisnovak.springboot.demo.planttracker.Reflector;
import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.entity.Account;
import com.krisnovak.springboot.demo.planttracker.service.PlantTrackerServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlantTrackerAccountTests {
    @Mock
    private PlantTrackerDAO plantTrackerDAO;

    @Mock
    private Session mockSession1;

    @Mock
    private Session mockSession2;


    //Tests that the new account has a zero ID (for being persisted in the database)
    //Also asserts that the new password and email are set and the current password is null
    @Test
    public void Account_newAccount_returnsAccountWithProperCredentials(){
        //Create a fake account
        Account theAccount = new Account("test", "password");

        Assertions.assertEquals(theAccount.getId(), 0);
        Assertions.assertEquals(theAccount.getEmail(), "test");
        Assertions.assertEquals(theAccount.getPasswordNew(), "password");
        Assertions.assertNull(theAccount.getPasswordCurrent());

    }

    @Test
    public void Account_deleteExpiredSessions_returnsAccountWithNoSessionsDeleted(){
        Account theAccount = new Account("test", "password");

        List<Session> sessionList = new ArrayList<Session>();
        sessionList.add(mockSession1);
        sessionList.add(mockSession2);

        theAccount.setSessions(sessionList);

        when(mockSession1.isExpiredSession()).thenReturn(false);
        when(mockSession2.isExpiredSession()).thenReturn(false);
        theAccount.deleteExpiredSessions();

        Assertions.assertEquals(sessionList.size(), 2);

    }

    @Test
    public void Account_deleteExpiredSessions_returnsAccountWithAllSessionsDeleted(){
        Account theAccount = new Account("test", "password");

        List<Session> sessionList = new ArrayList<Session>();
        sessionList.add(mockSession1);
        sessionList.add(mockSession2);

        theAccount.setSessions(sessionList);

        when(mockSession1.isExpiredSession()).thenReturn(true);
        when(mockSession2.isExpiredSession()).thenReturn(true);
        theAccount.deleteExpiredSessions();

        Assertions.assertEquals(sessionList.size(), 0);

    }

    //Tests for removeSession
    @Test
    public void Account_removeSession_returnsAccountWithSessionRemoved(){
        Account theAccount = new Account("test", "password");

        List<Session> sessionList = new ArrayList<Session>();
        sessionList.add(mockSession1);
        sessionList.add(mockSession2);

        theAccount.setSessions(sessionList);

        when(mockSession1.getSessionID()).thenReturn("fakeSessionID");
        when(mockSession2.getSessionID()).thenReturn("fakeSessionID2");
        theAccount.removeSession(mockSession1);

        Assertions.assertEquals(sessionList.size(), 1);
        Assertions.assertEquals(sessionList.get(0).getSessionID(), "fakeSessionID2");

    }

    @Test
    public void Account_managedInstance_returnsAccount(){
        Account theAccount = new Account("test", "password");

        Account fakeDatabaseAccount = new Account("test", "password");
        Reflector.setField(fakeDatabaseAccount, "id", 1);

        when(plantTrackerDAO.findAccount(theAccount)).thenReturn(fakeDatabaseAccount);

        Account newAccount = Account.managedInstance(theAccount, plantTrackerDAO);

        Assertions.assertEquals(newAccount.getId(), fakeDatabaseAccount.getId());
    }

    @Test
    public void Account_managedInstance_throwsInvalidAccountException(){
        Account theAccount = new Account("test", "password");

        when(plantTrackerDAO.findAccount(theAccount)).thenThrow(new EmptyResultDataAccessException(0));

        Assertions.assertThrows(InvalidAccountException.class, () -> {Account.managedInstance(theAccount, plantTrackerDAO);});
    }
}
