// Place all the behaviors and hooks related to the matching controller here.
// All this logic will automatically be available in application.js.

function detectmob() { 
 if( navigator.userAgent.match(/Android/i)
 || navigator.userAgent.match(/webOS/i)
 || navigator.userAgent.match(/iPhone/i)
 || navigator.userAgent.match(/iPad/i)
 || navigator.userAgent.match(/iPod/i)
 || navigator.userAgent.match(/BlackBerry/i)
 || navigator.userAgent.match(/Windows Phone/i)
 ){
    return true;
  }
 else {
    return false;
  }
};
function createMobileModule(){

};
function createDeModule(){

};
function landingPageInit(){
	var isMobile = detectmob();
    if(isMobile){
    	document.getElementById('deIndizzle').style.display = 'none';
    	createMobileModule();
    }else{
    	document.getElementById('mobileIndizzle').style.display = 'none';
    	createDeModule();
    };

};

