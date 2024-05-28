# Java Spring
Repository including the Spring Boot code for BotaniClock

##Server Files

At a high level, the server implements a RESTful API (in PlantTrackerRestController) that has a number of functions. A few of these endpoints are related to account and session management (/session, /account/login, /account/logout, /account/signup, /account/password, and /account/delete). Other endpoints are related to managing plants as a user (/plants and /plants/<plant_number>). Yet others are related to managing a timestamp for a plant as a device by a plant (/devices). When responding back, the REST controller layer may formulate the body of the response using HTTPResponseBody (using the HTTPResponseBody file) or may return back plant information using the Plant entity.

When a user makes a request, the REST controller layer passes the requests down to the Service layer (PlantTrackerService, implemented by PlantTrackerServiceImpl). The Service layer than processes the request and communicates with either the DAO layer (using PlantTrackerDAO, implemented by PlantTrackerDAOImpl) or Amazon S3 in the case of updating, retrieving, and deleting plant images (using S3Bucket).

The database includes three tables. The first is the Account table that includes account usernames and passwords. The second is the Session table that includes all sessions and their relationship to their respective accounts. The third is the Plant table including all plants and their relationship to their respective accounts. Each of these have a respective entity (Account, Plant, Session) as well as a respective dedicated error (InvalidAccountException, InvalidPlantException, and InvalidSessionException). The Device class represents information received from the device that is by the plant.

AppConfig is responsible for tracking when to send an email to a user if their plant is overdue to be watered and is set to be triggered according to a particular schedule. RestExceptionHandlerRepository is responsible for handling errors that need a particular response back to a client. RandomString is responsible for generating a secure random String. CustomWebMVCConfigurer is responsible for pointing requests to the appropriate static directory.

##Account-related endpoints

A POST on /session endpoint results in the server determining whether a given cookie is valid or not. A cookie is valid if it is not expired and if it has a corresponding Session stored in the Session table within the database.

A POST on /account/login endpoint results in the server attempting to create a Session using the provided Account credentials and provides a corresponding cookie back to the user. A bad request will be returned if the provided account credentials are invalid (they do not match an entry in the Account table within the database)

A POST on /account/logout endpoint results in the server attempting to remove the provided Session from the Session table within the database and provides an expired cookie back to the user.

A POST on /account/signup endpoint results in the server attempting to add an Account for the user to the Account table in the database. A bad request will be returned if the provided credentials are too long, are empty, or if the account would be a duplicate of an account existing in the database.

A POST on /account/password endpoint results in the server attempting to change a password for the provided Account. A bad request will be returned if the email or current password provided by the user is invalid or if the new password is either too long or empty.

A POST on /account/delete endpoint results in the server attempting to delete the provided Account from the Account table in the database. A bad request will be returned if the provided Account credentials are invalid.

##Plant-related endpoints

All Plant-related endpoints require a Session ID to be provided. Before performing any substantial operations, the server first verifies that the Session ID is linked to a valid session in the Session table and then retrieves the Account along with a managed instance of the Session. A Forbidden HTTP status is returned if the provided Session ID is invalid.

A GET on /plants results in the server returning all plants in the Plant table linked to a user (assuming a valid Session ID)

A GET on /plants/{plantID} results in the server returning the plant with the requested plant ID to the user assuming that the session ID is valid, that the plant with the plantID exists, and that the plant is linked to the same account as the Session associated with the session ID. A Not Found HTTP status is returned if the plant ID does not exist or the plant and session ID are not linked to the same account.

A POST on /plants results in the server adding a provided plant to the Plant table for the user linked to the Session ID.

A PUT on /plants results in the server updating a plant associated with a particular plant ID (provided within the body of the request).

A PUT on /plants/{plantID} results in the server attempting to update an image for the plant associated with the particular plant ID. Unlike the other endpoints, this endpoint takes in the file not as json/application, but instead as a multipart/form-data. Assuming the plant ID is valid and the session ID is valid, the server will resize the image (if need be) and will provide the image to the BotaniClock S3 bucket. Then, the server will receive a key for the image that is stored in the Plant table within the database. When a user later requests a GET for that plant, the image key will be used to generate a predesigned URL sent back to the user. Additionally, when the user deletes their account or the plant, the image key will be used to delete the plant from the S3 bucket.

A DELETE on /plants/{plantID} results in the server deleting the plant (assuming that the session ID is valid and that the plant ID is valid for the account linked to the session ID).

##Device-related endpoints

A POST on /devices results on the server indicating whether the provided account email and plant registration ID (an ID for the plant generated when the plant is created) are linked.

A PUT on /devices results in the server updating a timestamp for the plant linked to the provided account email and plant registration ID. Specifically, the server updates the last time the plant was watered to the current day.

##Front-End Files
The resources directory includes the files for the front-end of the website in the static directory. The index.html, style.css, and cookie.js files at the top of the directory are for the front page, which is the default page. The img folder contains the images used on the front page of the website. The login folder includes a page that a user navigates to when logging in and the signup folder contains a page that a user navigates to when signing up. The myplants folder contains a page where a user can view their plants and add additional plants. Additionally, the myplants folder contains a plantInfo folder and a settings folder. The plantInfo folder contains a page where a user can edit their plant information (including photos) and delete their plant and the settings folder contains a page where a user can change their password or delete their account.

Generally, the login and signup pages are accessible from the front page, the myplants page is accessible from the login and signup pages (upon a successful signup), and the settings and plantInfo pages are each accessible from the myplants page.

##Tests
Tests for the server files can be located in the test directory. The folder each test is located in is named the same as the folder the file being tested is located in. Additionally, each test file has a similar name to the file it is testing (PlantTrackerAccountTests tests the Account entity, for instance).
