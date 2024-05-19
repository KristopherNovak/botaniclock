const currentPlant = sessionStorage.getItem("currentPlant");
const plantID = sessionStorage.getItem(currentPlant);
main();

function main(){
  //TODO: Eventually put this in a non-deferred script (set up before screen is rendered)
  //Break up the set up menu text content into three functions (first one can be outside function, )

  //Sets up the text content on the plant box menu
  setUpMenuTextContent();

  //Sets up the listeners for the edit button (and the done button hiding behind it)
  setUpEditAndDoneButtonFunctionality();

  //Sets up the listener for the delete button
  setUpDeleteButtonFunctionality();

  //Set up listener for when an image is added
  setUpPlantBoxImageFunctionality();

  setUpRegisterButtonFunctionality();
}

//function that displays the plant information retrieved from the server on the plant box menu
async function setUpMenuTextContent(){
    //get the plant box from the server
    const plantInfo = await getPlantInfo();

    //Add the retrieved info to the plant menu
    document.querySelector(".plant-box__registration-id__entry").textContent = plantInfo.registrationID;
    updateDisplayPlantName(plantInfo.plantName);
    updateDisplayTemporalInformation(plantInfo.wateringInterval, plantInfo.lastWatered);

    //Ensure that the displayed info is shown on the edit form
    document.querySelector(".edit-form__date-last-watered").value = plantInfo.lastWatered
    document.querySelector(".edit-form__water-how-often").value = plantInfo.wateringInterval;
    document.querySelector(".edit-form__name").value = plantInfo.plantName;

    //Add image to screen
    const img = document.querySelector(".plant-box__image-box__image");
    if(plantInfo.imageURL !== null){
      img.src = plantInfo.imageURL;
    }

}

//update the plant name shown on the plant menu box
function updateDisplayPlantName(plantName){
  let plantNameDisplayText = (typeof plantName === "string") ?
                              plantName :
                              "TBD";

  document.querySelector(".plant-box__name__entry").textContent = plantNameDisplayText;
}

//Update the last watering date, the watering interval, and the next watering date shown on the plant menu
//If the wateringInterval is invalid (less than 1 or not a number), then the watering interval displayed is TBD
//If the lastWateringDate is invalid (not a date), then the last watering date displayed is TBD
//If either of the above are invalid, the next watering date displayed is TBD
function updateDisplayTemporalInformation(wateringInterval, lastWateringDate){
  let wateringIntervalInt = parseInt(wateringInterval);
  let lastWateringDateUTC = new Date(lastWateringDate);
  //Convert the retrieved date to local time
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

  
  //display the text
  document.querySelector(".plant-box__date-last-watered__entry").textContent = lastWateringDateDisplayText;
  document.querySelector(".plant-box__water-how-often__entry").textContent = wateringIntervalDisplayText;
  document.querySelector(".plant-box__next-watering-date__entry").textContent = nextWateringDateDisplayText;
}

//Set up the listeners for the edit button and the done button hiding behind the edit button
function setUpEditAndDoneButtonFunctionality(){
    const edit = document.querySelector(".button--edit");
    const done = document.querySelector(".button--done");
    const editFormElements = document.querySelectorAll(".edit-form__entry, .button--done, .edit-form__text--once-every, .edit-form__text--days");
    const typicalElements = document.querySelectorAll(".button--edit, .plant-box__name__entry, .plant-box__date-last-watered__entry, .plant-box__water-how-often__entry")

    edit.addEventListener("click", displayMenuToEditForm);
    edit.editFormElements = editFormElements;
    edit.typicalElements = typicalElements;

    done.addEventListener("click", editFormToDisplayMenu);
    done.editFormElements = editFormElements;
    done.typicalElements = typicalElements;
}

//Function to hide the edit button and show the done button when the edit button is pressed
//This function also hides the text for plant name, watering interval, and last watering date
//In their place is the edit form options for each of these
function displayMenuToEditForm(event){
  event.currentTarget.editFormElements.forEach((element) => element.style.display='block');
  event.currentTarget.typicalElements.forEach((element) => element.style.display='none');
}

//Function to hide the done button and show the edit button when the edit button is pressed
//This function also hides the edit form options for plant name, watering interval, and last watering date
//In their place is the newly added text placed in the edit form options
//Target is needed here because of the await function- alternatively, the event target items could go before await
async function editFormToDisplayMenu(event){
  event.preventDefault();
  try{
    await updatePlant();
    event.target.editFormElements.forEach((element) => element.style.display='none');
    event.target.typicalElements.forEach((element) => element.style.display='block');

  }catch(e){
    console.log(e);
  }
}

//function to set up a listener for the delete button
function setUpDeleteButtonFunctionality(){
  const deleteButton = document.querySelector(".button--delete");
  deleteButton.addEventListener("click", deletePlant);
}

//function to get the plant info from the server based on the plant ID provided by the server when the plant boxes were generated on myplants page
async function getPlantInfo(){
    try {
        const response = await fetch("/api/v1/plants/" + plantID);
        res = await response.json();
        if(response.ok){
            return res;
        }
        return null;
    } catch (e) {
        console.error(e);
  }
}


//function to delete a plant
async function deletePlant(){

    try {
        const response = await fetch("/api/v1/plants/" + plantID, {method: "DELETE"});
        //return to myplants page if a plant is deleted
        if(response.ok){
            window.location = "../";
        }
    } catch (e) {
        console.error(e);
  }
}

//function to update plant name, watering interval, and/or last watering date for a plant
async function updatePlant(){

  //TODO: Either make an error for going back to null or find some way to put in a dummy value (Maybe invalid object or something)
  let newPlantName = document.querySelector(".edit-form__name").value;
  let newLastWatered = document.querySelector(".edit-form__date-last-watered").value;
  let newWateringInterval = document.querySelector(".edit-form__water-how-often").value;


  //TODO: make it so that below parameters don't end up null (in above function)

  try {
    const response = await fetch("/api/v1/plants", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: '{"id": "'+plantID+'", "plantName":"'+newPlantName+'", "lastWatered":"'+newLastWatered+'", "wateringInterval":"'+ newWateringInterval +'"}'
    });
    //If update no server is successful, update displayed plant name,watering interval, and/or last watering date on plant box menu
    if(response.ok){
      updateDisplayPlantName(newPlantName);
      updateDisplayTemporalInformation(newWateringInterval, newLastWatered);
    }

  } catch (e) {
    console.error(e);
  }
}

function setUpPlantBoxImageFunctionality(){
  const imageBox = document.querySelector(".plant-box__image-box__input");
  console.log("Adding image box listener");
  imageBox.addEventListener("change", updatePlantImage);
}

async function updatePlantImage(event){
  const img = document.querySelector(".plant-box__image-box__image");
  const imageBox = document.querySelector(".plant-box__image-box__input");

  let currentPlantName = document.querySelector(".edit-form__name").value;
  let currentLastWatered = document.querySelector(".edit-form__date-last-watered").value;
  let currentWateringInterval = document.querySelector(".edit-form__water-how-often").value;

  let formData = new FormData();
  formData.append("file", event.target.files[0]);
  
  
  try {
    const response = await fetch("/api/v1/plants/" + plantID, {
      method: "PUT",
      body: formData
    });
    //If update no server is successful, update displayed plant name,watering interval, and/or last watering date on plant box menu
    if(response.ok){
      let res = await response.json();
      img.src = res.imageURL;
    }

  } catch (e) {
    console.error(e);
  }
}

function setUpRegisterButtonFunctionality(){
  const registerButton = document.querySelector(".button--register");
  console.log("Setting Up Register Button");
  registerButton.addEventListener("click", registerButtonFunction);
}

function registerButtonFunction(event){
  console.log("Register Button");
  window.open('http://botaniclock.local', '_blank');
}