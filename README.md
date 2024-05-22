# BotaniClock
Repository including the code for BotaniClock.

## What Is BotaniClock
BotaniClock is a web app that allows a user to more easily track when they should water their plants. At a high level, it's a service that notifies a user via email the next time a plant needs to be checked for watering. However, instead of requiring a user to go online each time they need to let the service know the plant has been watered, they can much more simply send that information to the server with the click of a single button on a device by the plant.

## How Does It Work
Let's say a user has a plant that they want to water. The first thing they'll do will be to create an account on the website and will create an entry for their plant. While creating the entry, they might add relevant information about the plant, such as a picture of the plant, the name of the plant, and how often it should be watered.

Next, they'll connect with a device that is on or next to the plant. They'll give the device their router SSID and password and will also give relevant information to link the device to the plant (the account email and a registration ID generated when the plant entry is created on the site). 

Finally, whenever the user waters the plant, they'll press the button on the device, causing the device to connect to the server and letting the server know that the plant has been watered. The server, upon receiving this indication, will use this indication to determine when the plant was last watered. It will combine this with how often the plant should be watered to determine the next time the plant should be watered. Then, the server will display this information on the website. Additionally, the server will track when the next watering date passes and will provide an email to the user to remind them to water their plant.

## How Is It Implemented
In this case, the backend server is run on Java Spring, the device is an ESP32 using C code loaded via ESP-IDF, the database is PostgreSQL, and the images are stored in an S3 bucket. The Java Spring code can be found in folder labeled "Java Spring" and the ESP32 code (once it is uploaded to the site) will be found in a folder labeled "ESP32 code."

## Demo
Click on the photo to see BotaniClock in action

[![BotaniClock](https://img.youtube.com/vi/z_e8bmMwRhs/0.jpg)](https://www.youtube.com/watch?v=z_e8bmMwRhs)
