<?php

 

$sid = $_POST['sid'];
$date = $_POST['date'];
$fileName = $_POST['outputFileName'];
$pythonFileName = $_POST['pythonFileName'];

$output = shell_exec('python ' . $pythonFileName . ' ' . $sid . ' ' . $date . ' ' . $fileName);

if (file_exists($fileName)) {
	
    header("Access-Control-Allow-Origin: *");
    header('Access-Control-Allow-Credentials: true');   
	header("Access-Control-Allow-Methods: GET, POST, OPTIONS"); 
    header('Content-Type: text/plain');
    header('Expires: 0');
    header('Cache-Control: must-revalidate');
    header('Pragma: public');
    readfile($fileName);
	unlink($fileName);
}

?>
