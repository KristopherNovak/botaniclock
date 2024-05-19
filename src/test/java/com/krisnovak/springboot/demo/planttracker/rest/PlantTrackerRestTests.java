package com.krisnovak.springboot.demo.planttracker.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnovak.springboot.demo.planttracker.service.PlantTrackerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PlantTrackerRestController.class)
@AutoConfigureMockMvc(addFilters=false)
@ExtendWith(MockitoExtension.class)
public class PlantTrackerRestTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlantTrackerService plantTrackerService;

    @Autowired
    private ObjectMapper objectMapper;

}
