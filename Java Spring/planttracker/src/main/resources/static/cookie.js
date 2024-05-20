checkForCookie();

async function checkForCookie(){
    const response = await fetch("api/v1/session", {method: "POST"});
    if(response.ok){
        document.querySelector(".login-button--header").setAttribute("href", "/myplants");
        document.querySelector(".signup-button--header").setAttribute("href", "/myplants");
    }
}