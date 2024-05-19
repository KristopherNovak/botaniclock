
main();

function main(){

  //Add a listener to check when the user attempt to log in
  setUpSignUpBox();

}

//function to add listener to submit button in the sign up box
function setUpSignUpBox(){
  const form = document.querySelector(".signup-box__form");
  form.addEventListener("submit", signUp);
}

//function to sign up a user when the sign up box submit button is pressed
async function signUp(event) {
    //prevent default form submission
    event.preventDefault();

    let email = document.querySelector(".signup-box__form__email").value;
    let password = document.querySelector(".signup-box__form__password").value;

    //Attempt to sign up the user
    try {
      const response = await fetch("/api/v1/account/signup", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: '{"email": "'+email+'", "passwordNew":"'+password+'"}'
      });
      //if user is successfully signed up, go to myplants page with newly acquired cookie
      if(response.ok){window.location.href = "/myplants";} 
      
      //if sign up isn't successful, find the appropriate error message and display it
      let errMessage='';
      res = await response.json();

      switch(res.message){
        case "EMPTY_FIELD":
          errMessage="The email or password has an empty field.";
          break;
        case "TOO_LONG":
          errMessage = "The email or password is too long";
          break;
        case "DUPLICATE_ACCOUNT":
          errMessage = "This email is already taken.";
          break;

      }

      document.getElementById("signup-box__error-area").textContent = errMessage;


    } catch (e) {
      console.error(e);
    }
}