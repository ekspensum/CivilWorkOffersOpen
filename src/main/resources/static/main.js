/**
 * 
 */

function validateLoginForm(logindata, login, password) {
    if (document.loginForm.username.value == "" && document.loginForm.password.value == "") {
        alert(logindata);
        document.loginForm.username.focus();
        return false;
    }
    if (document.loginForm.username.value == "") {
        alert(login);
        document.loginForm.username.focus();
        return false;
    }
    if (document.loginForm.password.value == "") {
        alert(password);
        document.loginForm.password.focus();
        return false;
    }
    return true;
}

function activeLinkMainMenu() {
	$(document).ready(function() {
	   $("[href]").each(function() {
	       if (this.href == window.location.href) {
	           $(this).addClass("active");
	       }
	   });
	});
}

function sentEmailClearForm() {
	document.getElementById("subject").value = "";
	document.getElementById("contentmsg").value = "";
	document.getElementById("replyEmail").value = "";
}

function checkCorrectLoginAndPassword(alertPassword, alertUsername){
	let password = document.getElementById("password").value;
	let password2 = document.getElementById("password2").value;
	if(password == password2){
		let username = document.getElementById("username").innerHTML;
		let login = document.getElementById("login").value;
		if(username !== login){
			return confirm(alertUsername);
		}
	} else {
		alert(alertPassword);
		return false;
	}
}

function setEmailField(){
	let login = document.getElementById("login").value;
	let re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	if(re.test(login)){
		document.getElementById("email").value = login;
	}
}

function setConstraintCustomerType(){
	let customerTypeId = document.getElementById("customerTypeId").value;
	if(customerTypeId == 1 || customerTypeId == 2){
		document.getElementById("companyNameConstraint").innerHTML = "*";
		document.getElementById("companyName").disabled = false;
		document.getElementById("companyName").style.background = "";
		
		document.getElementById("regonConstraint").innerHTML = "*";
		document.getElementById("regon").disabled = false;
		document.getElementById("regon").style.background = "";
	} else {
		document.getElementById("companyNameConstraint").innerHTML = "";
		if(document.getElementById("companyNameError") != null){
			document.getElementById("companyNameError").innerHTML = "";			
		}
		document.getElementById("companyName").disabled = true;
		document.getElementById("companyName").value = "";
		document.getElementById("companyName").style.background = "gray";
		
		document.getElementById("regonConstraint").innerHTML = "";
		if(document.getElementById("regonError") != null){			
			document.getElementById("regonError").innerHTML = "";
		}
		document.getElementById("regon").disabled = true;
		document.getElementById("regon").value = "";
		document.getElementById("regon").style.background = "gray";
	}
}

function validateField(id, char, message) {
    let searchText = document.getElementById(id).value;
    if (searchText.length < char) {
        alert(message);
        document.getElementById(id).focus();
        return false;
    } else {
        return true;
    }
}

function validateDateFields(message) {
    let searchDate = "";
    for (var i = 1; i < arguments.length; i++) {
        searchDate = document.getElementById(arguments[i]).value;
        if (searchDate == null || searchDate.length == 0) {
            alert(message);
            document.getElementById(arguments[i]).focus();
            return false;
        }
    }
    return true;
}

function validateSearchFieldAndEstimateField(id, char, message, estimateDescriptionMsg, estimateTypeOfCivilWorkMsg, itemUnitMsg, numberMsg, regexStringMsg, regexNumberMsg) {
    let searchText = document.getElementById(id).value;
    if (searchText.length < char) {
        alert(message);
        document.getElementById(id).focus();
        return false;
    } else {
        if (validateEstimateField(estimateDescriptionMsg, estimateTypeOfCivilWorkMsg, itemUnitMsg, numberMsg, regexStringMsg, regexNumberMsg)) {
            return true;
        } else {
            return false;    
        }
    }
}

function validateSelectRadio(alertText, elementName) {
	let x = document.getElementsByName(elementName);
	for (var i = 0; i < x.length; i++) {
	  if (x[i].checked) {
		 return true;
	  }
	}
	alert(alertText);
	return false;
}

var output = [];
function showUploadFiles(event) {
    var files = event.target.files;
    for (var i = 0, f; f = files[i]; ++i) {
        output.push('<li><b>', f.name, '</b> (', f.type || 'n/a', ') - ', f.size, ' bytes</li>');
    }
    document.querySelector('#file_list').innerHTML = '<Ol>' + output.join('') + '</Ol>';
    document.querySelector('#removeAllFilesButton').innerHTML = 
    	'<button type="button" onclick="clearAllAttachments();"><img src="/images/trash.png" width="25" /></button>';
}
function clearAllAttachments(){
	output.length = 0;
	document.getElementById('files').value = null;
	document.querySelector('#file_list').innerHTML = '<Ol>' + output.join('') + '</Ol>';
	document.querySelector('#removeAllFilesButton').innerHTML = '';
}

function validateEstimateField(estimateDescriptionMsg, estimateTypeOfCivilWorkMsg, itemUnitMsg, numberMsg, regexStringMsg, regexNumberMsg) {
    let regexEstimate = /[~`!#$%^*+={\[}\]\|:;\"'<>]/;
    let regexNumber = /[^0-9,.]/;
    let selectBox = document.getElementsByName("selectBox");
    let estimateDescription = document.getElementById('estimateDescription').value;
    let filterEstimateDescription = estimateDescription.match(regexEstimate);
    let estimateTypeOfCivilWork = document.getElementById('estimateTypeOfCivilWork').value;
    let filterTypeOfCivilWork = estimateTypeOfCivilWork.match(regexEstimate);
    
    if (estimateDescription.length < 5 || estimateDescription.length > 255) {
        alert(estimateDescriptionMsg);
        document.getElementById('estimateDescription').focus();
        return false;
    } else if (filterEstimateDescription != null && filterEstimateDescription.length > 0) {
        alert(regexStringMsg);
        document.getElementById('estimateDescription').focus();
        return false;
    } else if (estimateTypeOfCivilWork.length < 3 || estimateTypeOfCivilWork.length > 100) {
        alert(estimateTypeOfCivilWorkMsg);
        document.getElementById('estimateTypeOfCivilWork').focus();
        return false;
    } else if (filterTypeOfCivilWork != null && filterTypeOfCivilWork.length > 0) {
        alert(regexStringMsg);
        document.getElementById('estimateTypeOfCivilWork').focus();
        return false;
    } else {
        for (var i = 0; i < selectBox.length; i++) {
            if(!validateEstimateItems(i, estimateDescriptionMsg, itemUnitMsg, numberMsg, regexEstimate, regexNumber, regexStringMsg, regexNumberMsg)){
                return false;
            }
        }
        return true;
    }
}

function validateEstimateItems(i, estimateDescriptionMsg, itemUnitMsg, numberMsg, regexEstimate, regexNumber, regexStringMsg, regexNumberMsg) {
    let itemDescription = document.getElementById("description" + i).value;
    let filterItemDescription = itemDescription.match(regexEstimate);

    let itemUnit = document.getElementById("unitOfMeasurement" + i).value;
    let filterItemUnit = itemUnit.match(regexEstimate);

    let itemQuantity = document.getElementById("quantity" + i).value;
    let filterItemQuantity = itemQuantity.match(regexNumber);

    let itemPrice = document.getElementById("price" + i).value;
    let filterItemPrice = itemPrice.match(regexNumber);

    if (itemDescription.length < 5 || itemDescription.length > 255) {
        alert(estimateDescriptionMsg);
        document.getElementById("description" + i).focus();
        return false;
    } else if (filterItemDescription != null && filterItemDescription.length > 0) {
        alert(regexStringMsg);
        document.getElementById("description" + i).focus();
        return false;
    } else if (itemUnit.length < 1 || itemUnit.length > 20) {
        alert(itemUnitMsg);
        document.getElementById("unitOfMeasurement" + i).focus();
        return false;
    } else if (filterItemUnit != null && filterItemUnit.length > 0) {
        alert(regexStringMsg);
        document.getElementById("unitOfMeasurement" + i).focus();
        return false;
    } else if (itemQuantity.length < 1 || itemQuantity.length > 11) {
        alert(numberMsg);
        document.getElementById("quantity" + i).focus();
        return false;
    } else if (filterItemQuantity != null && filterItemQuantity.length > 0) {
        alert(regexNumberMsg);
        document.getElementById("quantity" + i).focus();
        return false;
    } else if (itemPrice.length < 1 || itemPrice.length > 11) {
        alert(numberMsg);
        document.getElementById("price" + i).focus();
        return false;
    } else if (filterItemPrice != null && filterItemPrice.length > 0) {
        alert(regexNumberMsg);
        document.getElementById("price" + i).focus();
        return false;
    } else {
        return true;
    }
}

function validateSelectRadioItemEstimate(notCheck, estimateDescriptionMsg, estimateTypeOfCivilWorkMsg, itemUnitMsg, numberMsg, regexStringMsg, regexNumberMsg) {
    if (validateEstimateField(estimateDescriptionMsg, estimateTypeOfCivilWorkMsg, itemUnitMsg, numberMsg, regexStringMsg, regexNumberMsg)) {
        let x = document.getElementsByName("selectBox");
        for (var i = 0; i < x.length; i++) {
            if (x[i].checked) {
                return true;
            }
        }
        alert(notCheck);
        return false;
    }
    return false;
}

function validateSelectRadioItemEstimateRemove(notCheck, estimateDescriptionMsg, estimateTypeOfCivilWorkMsg, itemUnitMsg, numberMsg, regexStringMsg, regexNumberMsg) {
    let selectBox = document.getElementsByName("selectBox");
    let selected = false;
    for (var i = 0; i < selectBox.length; i++) {
        if (selectBox[i].checked) {
            selected = true;
        }
    }
    if (!selected) {
        alert(notCheck);
        return false;
    }
    let regexEstimate = /[~`!#$%^*+={\[}\]\|:;\"'<>]/;
    let regexNumber = /[^0-9,.]/;
    let estimateDescription = document.getElementById('estimateDescription').value;
    let filterEstimateDescription = estimateDescription.match(regexEstimate);
    let estimateTypeOfCivilWork = document.getElementById('estimateTypeOfCivilWork').value;
    let filterTypeOfCivilWork = estimateTypeOfCivilWork.match(regexEstimate);

    if (estimateDescription.length < 5 || estimateDescription.length > 255) {
        alert(estimateDescriptionMsg);
        document.getElementById('estimateDescription').focus();
        return false;
    } else if (filterEstimateDescription != null && filterEstimateDescription.length > 0) {
        alert(regexStringMsg);
        document.getElementById('estimateDescription').focus();
        return false;
    } else if (estimateTypeOfCivilWork.length < 3 || estimateTypeOfCivilWork.length > 100) {
        alert(estimateTypeOfCivilWorkMsg);
        document.getElementById('estimateTypeOfCivilWork').focus();
        return false;
    } else if (filterTypeOfCivilWork != null && filterTypeOfCivilWork.length > 0) {
        alert(regexStringMsg);
        document.getElementById('estimateTypeOfCivilWork').focus();
        return false;
    } else {
        for (var i = 0; i < selectBox.length; i++) {
            if (!selectBox[i].checked) {
                if (!validateEstimateItems(i, estimateDescriptionMsg, itemUnitMsg, numberMsg, regexEstimate, regexNumber, regexStringMsg, regexNumberMsg)) {
                    return false;
                }
            }
        }
    }
    return true;
}

function  calculateEstimate(){
    let price_tag = 0.0;
    let price_tags = document.getElementsByName("price_tag");
    let total = 0.0;
    let total_price = document.getElementById("total_price");
    for (var i = 0; i < price_tags.length; i++) {
        let itemQuantity = document.getElementById("quantity" + i).value;
        document.getElementById("quantity" + i).value = itemQuantity.replace(/,/g, '.');
        let itemPrice = document.getElementById("price" + i).value;
        document.getElementById("price" + i).value = itemPrice.replace(/,/g, '.');        
        price_tag = document.getElementById("quantity"+i).value * document.getElementById("price"+i).value;
        price_tags[i].value = price_tag.toFixed(2);
        total += price_tag;
    }
    total_price.value = total.toFixed(2);
}

