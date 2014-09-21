<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript" src="js/string.js"></script>
<script type="text/javascript" src="js/quiz.js"></script>

<style type="text/css">
div.sentence {
	padding: 1.5em;
	border: 1px solid black;
}

span.question_text {
	background-color: #CD853F;
}

input.question_input {
	background: #F5DEB3;
	border: 1px solid #B78080;
}

input.question_input:FOCUS {
	border: 1px solid #590000;
}
</style>

</head>
<body>

quiz page

<div id="quiz_box"><input type="button" onclick="addSentence()"
	value="Add Sentence"></div>

<div id="response_debug"></div>

</body>
</html>