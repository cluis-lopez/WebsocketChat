'use strict';
window.addEventListener(
	'load',
	function() {
		var credentials = localStorage.getItem("credentials");
		if (credentials == null)
			location.href = "../login.html";
		window.credentials = JSON.parse(credentials);
		window.chatTo = '';
		window.typeChat = '';

		loadChatList();
	});
