document.addEventListener("DOMContentLoaded", function() {
	init();
},false);

function get_url_param( name ) {
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");

	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec( window.location.href );
	
	if ( results == null ) {
		return '';
	} else {
		var retVal = urldecode(results[1]);					
		return retVal;
	}
}

function urldecode(url) {
	  return decodeURIComponent(url.replace(/\+/g, ' '));
}

function init() {
	var errUrl = get_url_param('notFound');
	
	var elErrorTxt = document.getElementById('errorTxt');
	elErrorTxt.innerText = errUrl;

	var errDescr = get_url_param('descr');
	var errCode = get_url_param('errorCode');
	
	var errorTxt = (errDescr) ? errDescr : '';
	errorTxt+= (errCode) ? ' (' + errCode + ')' : '';

	var elErrorCode = document.getElementById('errorCode');
	elErrorCode.innerText = errorTxt;
	
	if (errUrl) {
		document.getElementById('error').setAttribute('style','display: block;');
	} else {
		document.getElementById('intro').setAttribute('style','display: block;');					
	}
}
