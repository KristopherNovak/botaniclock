# BotaniClock
Repository including the Spring Boot code for BotaniClock

At a high level, the server implements a RESTful API (in PlantTrackerRestController) that have a number of functions. A few of these endpoints are related to account and session management (/session, /account/login, /account/logout, /account/signup, /account/password, and /account/delete). Other endpoints are related to managing plants as a user (/plants and /plants/<plant_number>). Yet others are related to managing a timestamp for a plant as a device (/devices). When responding back, the REST controller layer may formulate the body of the response using HTTPResponseBody (using the HTTPResponseBody file) or may return back plant information using the Plant entity.

When a user makes a request, the REST controller layer passes the requests down to the Service layer (PlantTrackerService, implemented by PlantTrackerServiceImpl). The Service layer than processes the request and communicates with either the DAO layer (using PlantTrackerDAO, implemented by PlantTrackerDAOImpl) or Amazon S3 in the case of updating, retrieving, and deleting plant images (using S3Bucket).

The database includes three tables. The first is the Account table that includes account usernames and passwords. The second is the Session table that includes all sessions and their relationship to their respective accounts. The third is the Plant table including all plants and their relationship to their respective accounts. Each of these have a respective entity (Account, Plant, Session) as well as a respective dedicated error (InvalidAccountException, InvalidPlantException, and InvalidSessionException).

AppConfig is responsible for tracking when to send an email to a user if their plant is overdue to be watered and is set to be triggered according to a particular schedule. RestExceptionHandlerRespository is responsible for handling errors that need a particular response back to a client. RandomString is responsible for generating a secure random String. CustomWebMVCConfigurer is responsible for pointing requests to the appropriate static directory.

Additional details about individual services, the website stored in static, and tests to be added shortly.
