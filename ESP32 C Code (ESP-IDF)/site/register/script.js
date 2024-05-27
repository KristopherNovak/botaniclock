
main();

function main(){
  setUpRegisterBox();
}

//function to set up the submit button in the register box to register a device
function setUpRegisterBox(){
  //Add a listener to check when the user attempt to log in
  const form = document.querySelector(".register-box__form");
  form.addEventListener("submit", registerDevice);
}

//function to register a device
async function registerDevice(event) {
  //prevent default form submission behavior
  event.preventDefault();


  let email = document.querySelector(".register-box__form__email").value;
  let registrationID = document.querySelector(".register-box__form__registration").value;
  try {
    const response = await fetch("http://botaniclock.local/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: '{"accountEmail": "'+email+'", "registrationID":"'+registrationID+'"}'
    });
    
    let registerBoxErrorArea = document.getElementById("register-box__error-area");

    if(response.ok){
        registerBoxErrorArea.textContent = "Your device has been registered";
        registerBoxErrorArea.style.color = "green";
 
        //Clear out the values from the form in the change password box
        document.querySelector(".register-box__form__email").value = "";
        document.querySelector(".register-box__form__registration").value = "";
        return;
    }

    registerBoxErrorArea.textContent = "Invalid username or registration ID";
    registerBoxErrorArea.style.color = "red";


  } catch (e) {
    console.error(e);
    let registerBoxErrorArea = document.getElementById("register-box__error-area");
    registerBoxErrorArea.textContent = "Device Error. Please try again";
    registerBoxErrorArea.style.color = "red";
  }
}