<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="kanjikyoushi.kinc.data.Sentence"%>
<%@ page import="kanjikyoushi.kinc.data.SentenceList"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="sentence_list"
	class="kanjikyoushi.kinc.data.SentenceList" scope="request" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Kanji In Context</title>
<style type="text/css">
div.sentence {
	padding: 2em;
	border: 2px solid black;
}

div.question {
	margin-left: 10px;
	font-style: italic;
	font-size: .8em;
}
</style>
</head>
<body>

<c:forEach var="sentence" items="${ sentence_list.sentences }">
	<div class="sentence">${ sentence.htmlText }
	<div><c:forEach var="question" items="${ sentence.questions }">
		<div class="question">${ question.question } : ${
		question.answer }</div>
	</c:forEach></div>
	</div>
</c:forEach>

</body>
</html>