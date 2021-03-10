function downloadMatrix(urlToSend, sendStr, fileName) {
     var req = new XMLHttpRequest();
     req.open("POST", urlToSend, true);
	 req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
     req.responseType = "blob";
     req.onload = function (event) {
         var blob = req.response;
         var link=document.createElement('a');
         link.href=window.URL.createObjectURL(blob);
         link.download=fileName;
         link.click();
     };
	 
     req.send(sendStr);
}

function submitForm() {
	var fileName = "matrix.txt"
	var sid = document.getElementById("sid").value;
	var date = document.getElementById("date").value;
	var params = "pythonFileName=computeMatrix.py" + "&sid=" + sid + "&date=" + date + "&outputFileName=" + fileName;
	downloadMatrix('http://localhost:8080/process.php', params, fileName);
}
