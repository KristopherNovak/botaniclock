package com.krisnovak.springboot.demo.planttracker.view;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Class currently just used to redirect URLs
 */
@Configuration
public class CustomWebMVCConfigurer implements WebMvcConfigurer {
    /**
     *
     * Function that redirects any request to the appropriate index.html file without typing in index.html or
     * / at the end of a URL
     *
     * @param registry The ViewControllerRegistry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        //All the endpoints for the static portion of the website
        List<String> websiteEndpointList = new ArrayList<String>();
        websiteEndpointList.add("login");
        websiteEndpointList.add("signup");
        websiteEndpointList.add("myplants");
        websiteEndpointList.add("myplants/plantInfo");
        websiteEndpointList.add("myplants/settings");

        //Add a view controller for each of these endpoints
        for(String s : websiteEndpointList){
            registry.addViewController("/" + s).setViewName("redirect:/" + s +"/");
            registry.addViewController("/"+ s + "/").setViewName("forward:/" + s + "/index.html");
        }

    }
}
