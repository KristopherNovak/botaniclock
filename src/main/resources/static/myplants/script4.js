//TODO: Need to figure out where the 404 is coming from after adding a plant


//spacing of plant boxes in REM
const PLANT_BOX_SPACING = 17;
//starting location of top plant box in REM
const PLANT_BOX_STARTING_LOCATION = 21;

main();

function main(){
  //Grab plant data from database and initialize any previously added plant boxes as well as the add/plus button
  setUpPlantBoxesAndAddButton();

  //Add listener for menu logic- have settings box brought up when menu button clicked first time, then hide menu if anywhere else is clocked
  setUpMenuButton();

  //Add a listener to check when a user logs out- if they log out, then request for the cookie to be removed
  setUpLogOutButton();
}

//This function is used to set up the plant boxes and the add button as well as to give the add button its appropriate functionality
async function setUpPlantBoxesAndAddButton(){
    //Retrieve info about each plant from the server
    const plantInfo = await getPlantInfo();

    //Determine how many plant boxes there should be (e.g., how many plants are registered with a user)
    const plantBoxNumber = plantInfo.length;

    //Generate each plant box and populate them according to the info provided by the server
    for (let i=0;i<plantBoxNumber;i++){
      generatePlantBox(i, plantInfo[i].id, plantInfo[i].plantName, plantInfo[i].lastWatered,plantInfo[i].wateringInterval, plantInfo[i].imageURL);
    }

    //Generate the add button and calculate it's corresponding location relative to the last plant box
    const addBox = document.getElementById('add-box');
    let addBoxLocation = PLANT_BOX_STARTING_LOCATION+ plantBoxNumber*PLANT_BOX_SPACING;
    addBox.style.top = addBoxLocation +'rem';

    //Add listener that adds new plant boxes each time the plus button is pressed
    //TO DO: solve the height issue with the button
    addBox.addEventListener("click", addNewPlantBox);

}


//This function gets all the plant info from the data base and converts it to a JSON object
async function getPlantInfo(){
  try {
          const response = await fetch("/api/v1/plants");
          res = await response.json();
          return res;
      } catch (e) {
          console.error(e);
    }
}

//This function generates and places a plant box on the screen
//plantBoxIndex should be unique relative to any other time the function was called (typically it would represent the next index in a sequence)
//plantID corresponds to the ID of the plant in the database
//plantName corresponds to the name of the plant in the database
//lastWateringDate corresponds to the last time the plant was watered stored in the database
//wateringInterval correpsonds to how often the plant needs to be watered stored in the database
//each of plantID, plantName, lastWateringDate, and wateringInterval should be retrieved from the database before calling this function
function generatePlantBox(plantBoxIndex, plantID, plantName, lastWateringDate, wateringInterval, imageURL){

  let plantBoxID = "plant-box-" + plantBoxIndex;

  //Save the id for the plant box for the plantInfo page
  sessionStorage.setItem(plantBoxID, plantID);

  //Get information to put in plant box
  let temporalInformation = getTemporalInformation(wateringInterval, lastWateringDate);
  let plantNameText = getPlantNameText(plantName);

  //Add plantbox to page
  let plantBoxes = document.getElementById('plant-boxes');
  imageURL = (imageURL !== null) ? imageURL : "";
  plantBoxes.insertAdjacentHTML('beforeend', '<section id="'+plantBoxID+'" class="plant-box"> <div class="plant-box__image-box"> <img class="plant-box__image-box__image" src="'+imageURL+'" alt=""></div> <h2 class="plant-box__name">'+plantNameText+'</h2> <p class="plant-box__last-watering-date">Last Watered: '+temporalInformation.lastWateringDate+'</p> <p class="plant-box__next-watering-date">Next Reminder: '+temporalInformation.nextWateringDate+'</p>')
  
  //Acquire the new plant box
  let plantBox = plantBoxes.lastChild;

  //Place the plant box in the proper location relative to the top
  let plantBoxTop = PLANT_BOX_STARTING_LOCATION + plantBoxIndex*PLANT_BOX_SPACING;
  plantBox.style.top = plantBoxTop + 'rem';
  
  //give each plant box the ability to redirect to new page and to save
  plantBox.addEventListener("click",(event)=>{
      //point to the plantBox that was clicked to allow plantInfo to generate info for the right box
      sessionStorage.setItem("currentPlant", plantBox.id);
      //navigate to the plantInfo page
      window.location.href = "/myplants/plantInfo/";
  })
}

//This function takes in a plant name and either returns the plant name or displays TBD if the received input is not a string
function getPlantNameText(plantName){
  let plantNameDisplayText = (typeof plantName === "string") ?
                              plantName :
                              "TBD";

  return plantNameDisplayText;
}

//This function takes in a watering interval (how often a plant needs to be watered) and a last watering date (the last time a plant was watered)
//If the watering interval and last watering date are valid, then a next watering date is calculated
//and the function returns temproal information including how the last watering date and next watering date should be displayed
//If either the watering interval or the last watering date are invalid, the next watering date would be returned as TBD
//Additionally, if the last watering date is invalid, it will be returned as TBD
function getTemporalInformation(wateringInterval, lastWateringDate){
  let wateringIntervalInt = parseInt(wateringInterval);
  let lastWateringDateUTC = new Date(lastWateringDate);
  let lastWateringDateLocal = (lastWateringDateUTC.getTime() === 0 || lastWateringDateUTC.toString() === "Invalid Date") ? 
                              new Date(0) : 
                              new Date(lastWateringDateUTC.getTime() + lastWateringDateUTC.getTimezoneOffset()*60000);

  let wateringIntervalDisplayText = (wateringIntervalInt === NaN || wateringIntervalInt < 1 || wateringIntervalInt.toString() === "NaN") ? 
                                "TBD" : 
                                "Once every " + wateringIntervalInt.toString() + " days";                            
  
  let lastWateringDateDisplayText = (lastWateringDateLocal.getTime() === 0) ? 
                                "TBD" : 
                                lastWateringDateLocal.toDateString();
  let nextWateringDateDisplayText = (lastWateringDateDisplayText === "TBD" || wateringIntervalDisplayText === "TBD") ? 
                                "TBD" : 
                                (new Date(lastWateringDateLocal.getTime() + (wateringIntervalInt*24*60*60*1000)).toDateString());

  let temporalInformation = {
    'lastWateringDate' : lastWateringDateDisplayText,
    'nextWateringDate' : nextWateringDateDisplayText
  }
  
  return temporalInformation;
}

//Function to add a brand new text box to the database and screen upon pressing the add button
function addNewPlantBox(event){
  let addBox = document.getElementById('add-box');

  //remove the 'rem' from the top property of the addbox
  let topAddBox = parseInt(addBox.style.top.slice(0,-3));

  //add default information for the plant box
  let plantInfoText = '{"plantName": "New Plant"}';

  //calculate a unique ID for the new plant box
  let plantIndex = (topAddBox-PLANT_BOX_STARTING_LOCATION)/PLANT_BOX_SPACING;

  //Add default plant info to the database
  addPlantInfo(plantInfoText, plantIndex);

  //Move the add button to its new location
  addBox.style.top = topAddBox+PLANT_BOX_SPACING+'rem';
}

//function to add the plant information for a new plant box to the database
//the plantInfo is a JSON style string including the info to send to the database
//the plantIndex is where in the order of plant boxes the new plant box should be (typically it is the next in the sequence)
async function addPlantInfo(plantInfo, plantIndex){
  try {
        const response = await fetch("/api/v1/plants", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: plantInfo
        });
        //if successful, generate the plant box on the screen
        if(response.ok){
          res = await response.json();
          generatePlantBox(plantIndex, res.id, res.plantName, res.lastWatered, res.wateringInterval, res.imageURL);
        }

  } catch (e) {
              console.error(e);
  }
}

//function to trigger the menu button if anywhere on the page is clicked
function setUpMenuButton(){
  document.addEventListener("click", toggleMenu);
}

function toggleMenu(event){
      const menuBox = document.querySelector(".settings-box");
      const menu = document.querySelector(".button--menu");

      //Check if the click was not on the menu button/menu box or if it was on the menu button when the menu box is already displayed
      let cursorNotClickedOnMenuButtonOrMenuBox = !menuBox.contains(event.target) && !menu.contains(event.target);
      let cursorClickedOnMenuButtonAndMenuBoxOpen = menu.contains(event.target) && menuBox.style.display === 'inline-block';
        
      //If either of the above is true, then get rid of the menu box
      if(cursorNotClickedOnMenuButtonOrMenuBox || cursorClickedOnMenuButtonAndMenuBoxOpen){
          menuBox.style.display = "none";
      }
      //Keep menu box up otherwise
      else{
          menuBox.style.display = "inline-block";
      }
}

//function to add event listener to log out button
function setUpLogOutButton(){
  const logout = document.querySelector(".button--logout");
  logout.addEventListener("click", logOut);
}

async function logOut(event){
  //prevent the logout button from going to the link
  event.preventDefault();

  //Attempt to grab an expired cookie
  try {
          const response = await fetch("/api/v1/account/logout", {method: "POST"});
          //if the expired cookie is grabbed, then redirect to front page
          if(response.ok){
            window.location.href = "/";
          }
        } catch (e) {
          console.error(e);
        }
  }