checkForCookie();

async function checkForCookie(){
    const response = await fetch("/api/v1/session", {method: "POST"});
    if(!response.ok){
        window.location.href = "/login";
    }
}