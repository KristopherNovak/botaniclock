package com.krisnovak.springboot.demo.planttracker.entity;

import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlantTrackerSessionTests {

    @Mock
    PlantTrackerDAO plantTrackerDAO;

    @Mock
    Account mockAccount;

    @Mock
    Session mockSession;

    //Tests for new Session(Account theAccount, PlantTrackerDAO plantTrackerDAO)
    @Test
    public void Session_newSession_ReturnsNewSession(){
        when(Account.managedInstance(mockAccount, plantTrackerDAO)).thenReturn(mockAccount);
        Session newSession = new Session(mockAccount, plantTrackerDAO);


        Assertions.assertEquals(0, newSession.getId());
        Assertions.assertTrue(newSession.getMaxAge() > 0);
        Assertions.assertTrue(newSession.getMaxAge() <= Session.MAXIMUM_COOKIE_AGE);
        Assertions.assertTrue(newSession.getTimeCreated() > ((System.currentTimeMillis()/1000) - 5));

        Assertions.assertNotNull(newSession.getSessionID());
        Assertions.assertTrue(newSession.getSessionID().length() <= Session.MAXIMUM_SESSION_ID_LENGTH);
        Assertions.assertFalse(newSession.getSessionID().isEmpty());

        Assertions.assertSame(newSession.getAccount(), mockAccount);
    }

    @Test
    public void Session_newSession_ThrowsInvalidAccountException(){
        when(Account.managedInstance(mockAccount, plantTrackerDAO)).thenThrow(InvalidAccountException.class);

        Assertions.assertThrows(InvalidAccountException.class, ()->{new Session(mockAccount, plantTrackerDAO);});
    }

    //Tests for isExpiredSession()
    @Test
    public void Session_isExpiredSession_ReturnsFalse(){
        when(Account.managedInstance(mockAccount, plantTrackerDAO)).thenReturn(mockAccount);
        Session newSession = new Session(mockAccount, plantTrackerDAO);

        Assertions.assertFalse(newSession.isExpiredSession());

    }

    @Test
    public void Session_isExpiredSession_ReturnsTrue(){
        when(Account.managedInstance(mockAccount, plantTrackerDAO)).thenReturn(mockAccount);
        Session newSession = new Session(mockAccount, plantTrackerDAO);

        newSession.setMaxAge(3);
        newSession.setTimeCreated((System.currentTimeMillis()/1000)-5);

        Assertions.assertTrue(newSession.isExpiredSession());
    }

    //Tests for managedInstance(String sessionID, PlantTrackerDAO plantTrackerDAO)
    @Test
    public void Session_managedInstance_ReturnsManagedSession(){

        String fakeSessionID = "fakeSessionID";

        when(plantTrackerDAO.findSessionBySessionID(fakeSessionID)).thenReturn(mockSession);
        Session managedSession = Session.managedInstance(fakeSessionID, plantTrackerDAO);

        Assertions.assertSame(managedSession, mockSession);
    }

    @Test
    public void Session_managedInstance_ThrowsInvalidSessionException(){
        String fakeSessionID = "fakeSessionID";

        when(plantTrackerDAO.findSessionBySessionID(fakeSessionID)).thenThrow(EmptyResultDataAccessException.class);
        Assertions.assertThrows(InvalidSessionException.class, ()->{Session.managedInstance(fakeSessionID, plantTrackerDAO);});

    }

    //Tests for getResponseCookie()

    @Test
    public void Session_getResponseCookie_ReturnValidResponseCookie(){
        when(Account.managedInstance(mockAccount, plantTrackerDAO)).thenReturn(mockAccount);
        Session newSession = new Session(mockAccount, plantTrackerDAO);

        ResponseCookie validResponseCookie = newSession.getResponseCookie();
        Assertions.assertEquals(validResponseCookie.getMaxAge().getSeconds(), newSession.getMaxAge());
        Assertions.assertEquals(validResponseCookie.getValue(), newSession.getSessionID());
        Assertions.assertEquals(validResponseCookie.getName(), "sessionId");
    }

    //Tests for getExpiredCookie()
    @Test
    public void Session_getExpiredCookie_ReturnsExpiredCookie(){
        ResponseCookie expiredCookie = Session.getExpiredCookie();

        Assertions.assertTrue(expiredCookie.getMaxAge().getSeconds() < 1);
    }
}
