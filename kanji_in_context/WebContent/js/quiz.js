/**
 * 
 */

var MAX_SENTENCES = 4;

var sentences = new Array();
var buttonCount = 0;

function init() {
	// var quizBox = document.getElementById("quiz_box");
	// quizBox.innerHTML = "";
	addSentence();
}

function addSentence() {

	var sentenceRequest = new XMLHttpRequest();
	sentenceRequest.onreadystatechange = function() {

		if (sentenceRequest.readyState == 4) {
			if (sentenceRequest.status == 200) {

				// alert(sentenceRequest.responseText);
				var responseElement = sentenceRequest.responseXML.documentElement;
				var textNodes = responseElement.getElementsByTagName('text');

				if (textNodes.length == 0) {
					alert("ERROR: no sentence text\n"
							+ sentenceRequest.responseText);
					return;
				}

				var sentenceText = textNodes.item(0).firstChild.nodeValue;

				/*
				 * add new sentence
				 */
				var newSentence = document.createElement("div");
				newSentence.setAttribute("class", "sentence");
				newSentence.innerHTML = sentenceText;
				var newSize = sentences.push(newSentence);

				var quizBox = document.getElementById("quiz_box");
				quizBox.appendChild(newSentence);

				/*
				 * remove oldest sentence if array is too large
				 */
				if (newSize > MAX_SENTENCES) {
					var oldDiv = sentences.shift();
					quizBox.removeChild(oldDiv);
				}
			} else {
				document.getElementById("response_debug").innerHTML = "<pre>"
						+ sentenceRequest.responseText + "<pre>";
			}
		}

	}
	sentenceRequest.open("POST", "quiz", true);
	sentenceRequest.setRequestHeader("Content-type",
			"application/x-www-form-urlencoded");
	sentenceRequest.overrideMimeType("text/xml");
	sentenceRequest.send('action=get_sentence');

}

function checkQuestion(questionId, answer, isLastQuestion) {

	var questionInput = document.getElementById("question_input_" + questionId);
	var questionText = document.getElementById("question_text_" + questionId);

	// document.getElementById("response_debug").innerHTML += "<div>"
	// + "checking question #" + questionId + ", answer '" + answer
	// + "', question input: '" + questionInput.value + "'" + "</div>";

	if (questionInput.value == answer) {
		questionText.style.backgroundColor = "#4D924D";
		questionInput.disabled = "true";
	} else {
		questionText.style.backgroundColor = "#971919";
	}

	if (isLastQuestion == "true") {
		// alert("last question");
		addSentence();
	}
}
