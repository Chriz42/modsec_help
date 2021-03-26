<!DOCTYPE html>
<html>
	<head>
		<title>How to WAF</title>
		<meta charset="UTF-8"/>
	</head>

	<body>
		<b> Passed the WAF</b>
		<br />
		<?php
			echo "<br />Request:<br />";
			print_r($_SERVER);
			echo "<br />";
			echo "<br />Args:<br />";
			print_r($_REQUEST);
		?>


	</body>
</html>
