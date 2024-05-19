
main();

function main(){
  setUpLoginBox();
}

//function to set up the submit button in the login box to a log a user in
function setUpLoginBox(){
  //Add a listener to check when the user attempt to log in
  const form = document.querySelector(".login-box__form");
  form.addEventListener("submit", logIn);
}

//function to log a user in
async function logIn(event) {
  //prevent default form submission behavior
  event.preventDefault();


  let email = document.querySelector(".login-box__form__email").value;
  let password = document.querySelector(".login-box__form__password").value;
  //attempt to log the user in
  try {
    const response = await fetch("/api/v1/account/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: '{"email": "'+email+'", "passwordCurrent":"'+password+'"}'
    });

    //If login successful, redirect the user to the myplants page with their acquired cookie
    //If login is not successful, display the error message in the login box
    if(response.ok){
      window.location.href = "/myplants";
    } else{document.getElementById("login-box__error-area").style.display = "block";}

  } catch (e) {
    console.error(e);
  }
}