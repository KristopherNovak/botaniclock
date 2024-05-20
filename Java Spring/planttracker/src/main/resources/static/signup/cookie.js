checkForCookie();

//Function to check if there is an active cookie
//If so, redirect to main page
async function checkForCookie(){

    try {
      const response = await fetch("/api/v1/session", {
        method: "POST",
        mode: "cors"
      });
      res = await response.json();
      if(res.message === "Cookie valid"){
        window.location.href = "/myplants";
      }
      
    } catch (e) {
      console.log(e);
    }
}