
main();

function main(){
  setUpRouterBox();
}

//function to set up the submit button in the router box to connect to router
function setUpRouterBox(){
  //Add a listener to check when the user attempt to log in
  const form = document.querySelector(".router-box__form");
  form.addEventListener("submit", connectToRouter);
}

//function to connect to router
async function connectToRouter(event) {
  //prevent default form submission behavior
  event.preventDefault();


  let ssid = document.querySelector(".router-box__form__ssid").value;
  let routerPassword = document.querySelector(".router-box__form__routerPassword").value;
  try {
    const response = await fetch("botaniclock.local/", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: '{"ssid": "'+ssid+'", "routerPassword":"'+routerPassword+'"}'
    });
    
    let routerBoxErrorArea = document.getElementById("router-box__error-area");

    if(response.ok){
        routerBoxErrorArea.textContent = "Your router information has been received";
        routerBoxErrorArea.style.color = "green";
 
        //Clear out the values from the form in the change password box
        document.querySelector(".router-box__form__ssid").value = "";
        document.querySelector(".router-box__form__routerPassword").value = "";
        return;
    }

  } catch (e) {
    console.error(e);
    let routerBoxErrorArea = document.getElementById("router-box__error-area");
    routerBoxErrorArea.textContent = "Device Error. Please try again";
    routerBoxErrorArea.style.color = "red";
  }
}