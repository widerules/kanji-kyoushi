String.prototype.htmlEntities = function() {
	return this.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g,
			'&gt;').replace(/'/g, '&#39').replace(/"/g, '&quot');
};

String.prototype.jsEscape = function() {
	return this.replace(/'/g, '\\\'').replace(/"/g, '\\"');
};
	