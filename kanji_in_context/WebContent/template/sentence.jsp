<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="sentence" class="kanjikyoushi.kinc.data.Sentence"
	scope="request" />
<sentence>
    <sentence_id>${ sentence.sentenceId }</sentence_id>
    <text><![CDATA[ ${ sentence.htmlText } ]]> </text>
    <c:forEach var="question" items="${ sentence.questions }">
		<question>
           <question_id>${ question.questionId }</question_id>
           <question_text>${ question.question }</question_text>
		   <answer_text>${ question.answer }</answer_text>
	    </question>
    </c:forEach>
</sentence>

