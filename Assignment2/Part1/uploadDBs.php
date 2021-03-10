<?php $filePath = "/Users/admin/AndroidStudioProjects/MyApplication/uploadedDBs/";

if(!file_exists($filePath)){
    mkdir($filePath, 0777, true);
}

$file = $filePath . basename($_FILES['fileUpload']['name']);

if (move_uploaded_file($_FILES['fileUpload']['tmp_name'], $file)) {
	echo "DB Uploaded to the Server";
} else {
    echo "DB Upload Failed";
}

?>
