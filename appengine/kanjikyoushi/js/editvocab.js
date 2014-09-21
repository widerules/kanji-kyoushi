function doEdit(key, type, value) {
    new_value = prompt("enter new value", value);
    if (new_value == null || new_value == value) { return; } 

	xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = processChangeRequest;
    xmlhttp.open('POST', '/vocab', true);
	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xmlhttp.overrideMimeType("text/xml");
	xmlhttp.send('key=' + encodeURI(key) + '&do=edit&type=' + encodeURI(type) 
	    + '&old_value=' + encodeURI(value) + '&new_value=' + encodeURI(new_value));
}

function doAdd(key, type) {
    new_value = prompt("enter new " + type);
    if (new_value == null) { return; } 

	xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = processChangeRequest;
    xmlhttp.open('POST', '/vocab', true);
	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xmlhttp.overrideMimeType("text/xml");
	xmlhttp.send('key=' + encodeURI(key) + '&do=add&type=' + encodeURI(type) 
	    + '&new_value=' + encodeURI(new_value));
}

function doDelete(key, type, value) {
    // confirm deletion
    var answer = confirm("delete " + type + " '" + value + "'?");
    if (!answer) {
        return;
    }

	xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = processChangeRequest;
    xmlhttp.open('POST', '/vocab', true);
	xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xmlhttp.overrideMimeType("text/xml");
	xmlhttp.send('key=' + encodeURI(key) + '&do=delete&type=' + encodeURI(type) 
	    + '&old_value=' + encodeURI(value));
}

function processChangeRequest() {
    if (xmlhttp.readyState == 4) {
	    if (xmlhttp.status == 200) {
	        // responseText = xmlhttp.responseText;
	        // alert(responseText);

	        response = xmlhttp.responseXML.documentElement;

            // get response data
            try {
                messages = response.getElementsByTagName('message');
                if (messages.length > 0) {
                    alert(messages[0].firstChild.data);
                }
	        } catch (e) {}
	        
	        response_word = response.getElementsByTagName('word')[0].firstChild.data.htmlEntities();
	        response_key = response.getElementsByTagName('key')[0].firstChild.data.htmlEntities();
            response_kanji_index = response.getElementsByTagName('kanji_index')[0].firstChild.data.htmlEntities();
            response_active = response.getElementsByTagName('active')[0].firstChild.data.htmlEntities();
            response_modified = response.getElementsByTagName('modified')[0].firstChild.data.htmlEntities();
            response_readings = response.getElementsByTagName("reading")
            response_meanings = response.getElementsByTagName("meaning")
            
            // get page elements
            page_word = document.getElementById('word');
            page_kanji_index = document.getElementById('kanji_index');
            page_active = document.getElementById('active');
            page_modified = document.getElementById('modified');
            page_readings = document.getElementById('readings');
            page_meanings = document.getElementById('meanings');

            // update word
            page_word.innerHTML = response_word;
            
            // update word info
            page_kanji_index.innerHTML = "kanji index: " + response_kanji_index;
            page_active.innerHTML = "active: " + response_active;
            page_modified.innerHTML = "modified: " + localDateString(response_modified);
            
            // update readings
            reading_text = "<div class=\"type_label\">読み方</div>";
            for (i = 0; i < response_readings.length; i++) {
                reading_string = response_readings[i].firstChild.data;
                    reading_text = reading_text 
                        + "<div class=\"word_data\">" 
                            + reading_string.htmlEntities()
                            + "<a class=\"change edit\" href=\"#\" "
                                + "onclick=\"doEdit('" + response_key + "', 'reading','" + reading_string.jsEscape() + "')\">edit</a>\n"
                            + "<a class=\"change delete\" href=\"#\" "
                                + "onclick=\"doDelete('" + response_key + "', 'reading', '" + reading_string.jsEscape() + "')\">delete</a>"
                        + "</div>";
            }
            reading_text = reading_text 
                + "<div class=\"word_data\">"
                    + "<a class=\"change add\" href=\"#\" "
                        + "onclick=\"doAdd('" + response_key + "', 'reading')\">add reading</a>"
                + "</div></div>";
            page_readings.innerHTML = reading_text;
            
            // update meanings
            meaning_text = "<div class=\"type_label\">意味</div>";
            for (i = 0; i < response_meanings.length; i++) {
                meaning_string = response_meanings[i].firstChild.data;
                meaning_text = meaning_text 
                    + "<div class=\"word_data\">" 
                        + meaning_string.htmlEntities()
                        + "<a class=\"change edit\" href=\"#\" "
                            + "onclick=\"doEdit('" + response_key + "', 'meaning','" + meaning_string.jsEscape() + "')\">edit</a>\n"
                        + "<a class=\"change delete\" href=\"#\" "
                            + "onclick=\"doDelete('" + response_key + "', 'meaning', '" + meaning_string.jsEscape() + "')\">delete</a>"
                    + "</div>";
            }
            meaning_text = meaning_text 
                + "<div class=\"word_data\">"
                    + "<a class=\"change add\" href=\"#\" "
                        + "onclick=\"doAdd('" + response_key + "', 'meaning')\">add meaning</a>"
                + "</div></div>";
            page_meanings.innerHTML = meaning_text;
        }
    }
}

function localDateString(gmt_date_string) {
    utc_time_milli = Date.parse(gmt_date_string);
    local_offset_min = new Date().getTimezoneOffset();
    local_time_milli = utc_time_milli - (local_offset_min * 60000)
    local_date = new Date(local_time_milli);
    return local_date.toLocaleString();
}

function initPage() {
    document.getElementById('modified').innerHTML = 'modified: ' 
        + localDateString('{{ word.modified|date:"m/d/Y H:i:s" }}');
}
