package com.krisnovak.springboot.demo.planttracker.exception;

import com.krisnovak.springboot.demo.planttracker.entity.InvalidAccountException;
import com.krisnovak.springboot.demo.planttracker.entity.InvalidPlantException;
import com.krisnovak.springboot.demo.planttracker.entity.InvalidSessionException;
import com.krisnovak.springboot.demo.planttracker.rest.HTTPResponseBody;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

public class PlantTrackerExceptionHandlingTests {

    //Tests for public ResponseEntity<HTTPResponseBody> handleInvalidAccountException(InvalidAccountException e)


    //Tests for public ResponseEntity<HTTPResponseBody> handleInvalidPlantException(InvalidPlantException e)

    //Tests for public ResponseEntity<HTTPResponseBody> handleInvalidSessionException(InvalidSessionException e)


    //Tests for public ResponseEntity<HTTPResponseBody> handleIOException(IOException e)

    //Tests for public ResponseEntity<HTTPResponseBody> handleDataIntegrityViolationException(DataIntegrityViolationException e)

}
