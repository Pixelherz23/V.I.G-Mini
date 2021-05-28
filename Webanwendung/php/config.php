<?php

define('DBSERVER', 'localhost:3306');
define('DBUSERNAME', 'root');
define('DBPASSWORD', '');
define('DBNAME', '');


$db = mysqli_connect(DBSERVER,DBUSERNAME,DBPASSWORD,DBNAME);


if($db === false){
    
    die("Error: connection error.". mysqli_connect_error());

}


?>