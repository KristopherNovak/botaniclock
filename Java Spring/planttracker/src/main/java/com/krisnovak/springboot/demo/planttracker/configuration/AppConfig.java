package com.krisnovak.springboot.demo.planttracker.configuration;

import com.krisnovak.springboot.demo.planttracker.dao.PlantTrackerDAO;
import com.krisnovak.springboot.demo.planttracker.entity.Plant;
import com.krisnovak.springboot.demo.planttracker.service.PlantTrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

/**
 * Class used to send emails to users whose plants are overdue to be watered
 */
@Configuration
@EnableScheduling
public class AppConfig {
    private PlantTrackerDAO plantTrackerDAO;

    private JavaMailSender sender;

    @Autowired
    public AppConfig(PlantTrackerDAO thePlantTrackerDAO) {
        plantTrackerDAO = thePlantTrackerDAO;
        sender = getJavaMailSender();
    }

    /**
     * Function that sends an email reminder to any user with a plant overdue to be watered
     */
    @Scheduled(cron = "0 0 * * * *")
    private void sendEmailReminder(){
        //Retrieve all plants from the database
        List<Plant> allPlants = plantTrackerDAO.findAllPlants();

        //The present date
        LocalDate presentDay = LocalDate.now();

        //Last date the plant was watered
        LocalDate lastWatered;

        //Next day the plant needs to be watered
        LocalDate nextWatered;

        //How often (in days) the plant needs to be watered
        int wateringInterval;

        //Determine if an email needs to be sent for each plant
        for (Plant plant : allPlants){
            lastWatered = plant.getLastWatered();
            wateringInterval = plant.getWateringInterval();
            if(lastWatered == null || wateringInterval < 1){
                continue;
            }
            nextWatered = lastWatered.plusDays(wateringInterval);

            //Check if the next watering date is before the current date
            if(presentDay.isAfter(nextWatered) || presentDay.isEqual(nextWatered)){
                //Send an email to the corresponding account
                sendEmail(plant, nextWatered);
            }

        }

    }

    //TODO: Check that this function properly escapes characters before sending the email
    /**
     * Function that sends an email for a particular plant to the user
     * @param thePlant
     * @param nextWatered
     */
    private void sendEmail(Plant thePlant, LocalDate nextWatered){

        //The email address linked to the account associated with the plant
        String email = thePlant.getAccount().getEmail();

        //The name of the plant
        String plantName = thePlant.getPlantName();

        //The date the plant was supposed to be watered
        String overdueDate = nextWatered.toString();

        //The text to be provided in the email
        String subject = plantName + " is ready to be watered";
        String text = "It looks like " + plantName + " was set to be watered on " + overdueDate + ". Be sure to water it now!";

        //Send the email
        sendSimpleMessage(email, subject, text);
        System.out.println("Message sent");
    }

    /**
     * Function that gets a mail sender
     * @return The mail sender
     */
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        //Grab the credentials of the email address used to send the message
        Properties emailProperties = new Properties();
        try{
            emailProperties.load(new FileReader("../../../email.properties"));
        } catch(IOException e){
            throw new RuntimeException("File could not be found");
        }

        //Set the mail sender with the retrieved email username and password
        mailSender.setUsername(emailProperties.getProperty("emailUsername"));
        mailSender.setPassword(emailProperties.getProperty("emailPassword"));

        //Get properties for the mail sender
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    /**
     * Function that sends the message to the user
     * @param to The address to send the message to
     * @param subject The subject header of the message
     * @param text The text of the message
     */
    public void sendSimpleMessage(
            String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@botaniclock.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        sender.send(message);
    }
}
