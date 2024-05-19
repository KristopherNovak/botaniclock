package com.krisnovak.springboot.demo.planttracker.service;

import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.entity.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlantTrackerServiceImplTests {

    @Mock
    private PlantTrackerDAO plantTrackerDAO;

    @InjectMocks
    private PlantTrackerServiceImpl plantTrackerService;

}
