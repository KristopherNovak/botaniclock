
main();

function main(){
  //Initialize the submit button for the change password box
  setUpChangePasswordBox();

  //Initialize the submit button for the deleteAccount box
  setupDeleteAccountBox();
}

function setUpChangePasswordBox(){
  //Add a submit event listener for the Change Password box
  const changePasswordForm = document.querySelector(".change-password-box__form");
  changePasswordForm.addEventListener("submit", changePassword);
}


async function changePassword(event) {
    //Prevent default submission behavior
    event.preventDefault();

    //Grab the values from the form
    let email = document.querySelector(".change-password-box__form__email").value;
    let passwordCurrent = document.querySelector(".change-password-box__form__password--current").value;
    let passwordNew = document.querySelector(".change-password-box__form__password--new").value;

    try {
      const response = await fetch("/api/v1/account/password", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: '{"email": "'+email+'", "passwordNew":"'+passwordNew+'", "passwordCurrent":"'+passwordCurrent+'"}'
      });

      let changePasswordErrorArea = document.getElementById("change-password-box__error-area");

      if(response.ok){
      //Add the success text into the change password box
       changePasswordErrorArea.textContent = "Your password has successfully been changed";
       changePasswordErrorArea.style.color = "green";

       //Clear out the values from the form in the change password box
       document.querySelector(".change-password-box__form__email").value = "";
       document.querySelector(".change-password-box__form__password--current").value = "";
       document.querySelector(".change-password-box__form__password--new").value = "";
       return;
      }

      res = await response.json();

      //determine the cause of the error
      let errMessage = "";
      switch(res.message){
        case "EMPTY_FIELD":
            errMessage = "The new password field is empty"
            break;

        case "TOO_LONG_FIELD":
            errMessage = "The entered new password is too long"
            break;

        case "INVALID_ACCOUNT":
            errMessage = "The email or current password is invalid"
            break;
      }

      //display error information in the change password box
      changePasswordErrorArea.textContent = errMessage;
      changePasswordErrorArea.style.color = "red";


    } catch (e) {
      console.error(e);
    }
}

function setupDeleteAccountBox(){
  //Add a submit event listener for the Delete Account box
  const deleteAccountForm = document.querySelector(".delete-account-box__form");
  deleteAccountForm.addEventListener("submit", deleteAccount);
}

async function deleteAccount(event) {
    //prevent default form submission
    event.preventDefault();

    //acquire email and password values from form
    let email = document.querySelector(".delete-account-box__form__email").value;
    let password = document.querySelector(".delete-account-box__form__password").value;

    //attempt to perform delete
    try {
      const response = await fetch("/api/v1/account/delete", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: '{"email": "'+email+'", "passwordCurrent":"'+password+'"}'
      });
      //redirect back to the front page if delete successful
      //otherwise, display error message
      if(response.ok){
        window.location.href = "/";
      } else{document.getElementById("delete-account-box__error-area").textContent = "Incorrect current password or email. Please try again.";}
      

    } catch (e) {
      console.error(e);
    }
}