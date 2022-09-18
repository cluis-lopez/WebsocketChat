function loadChatList() {
	fetch('chatlist.html').then(response => response.text())
		.then(text => {
			document.getElementById('mainPage').innerHTML = text;

			document.getElementById("searchButton").addEventListener("click", searchChats);
			document.getElementById("reload").addEventListener("click", reload);
			document.getElementById('groups').addEventListener('click', loadGroups);
			var selection = document.getElementById("selection");

			//Initialize the unnatended xhttp request to get LastChats and search users/groups
			var xhttp = new XMLHttpRequest();
			xhttp.open("GET", "/ChatServer/Userdata?id=" + window.credentials.id
				+ "&token=" + window.credentials.token + "&maxNumber=5&command=lastChats", true);
			xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			xhttp.setRequestHeader("Accept", "application/json");
			xhttp.onload = function() {
				if (this.readyState == 4 && this.status == 200) {
					var resp = JSON.parse(this.responseText);
					if (resp.code == "OK") {
						var chats = resp.chatList;
						recreateList(chats);
					} else {
						console.log(resp.code);
						location.href = "login.html";
					}
				}
			};

			xhttp.onerror = function(e) {
				console.log("Error Unresponsive Domain ");
			};

			xhttp.send();

			function reload() {
				xhttp.open("GET", "/ChatServer/Userdata?id=" + window.credentials.id
					+ "&token=" + window.credentials.token + "&maxNumber=5&command=lastChats", true);
				xhttp.send();
			}

			function searchChats() {
				var searchText = document.getElementById("searchBox").value;
				if (searchText == "")
					return;
				xhttp.open("GET", "/ChatServer/Userdata?id=" + window.credentials.id
					+ "&token=" + window.credentials.token + "&maxNumber=5&command=searchChats&searchChats=" + searchText, true);
				xhttp.send();

			};

			function loadGroups() {
				loadSelectGroupsPage();
			}

			function recreateList(elements) {
				var lista = document.getElementById("chatList");
				while (lista.firstChild) {
					lista.removeChild(lista.firstChild);
				}

				for (var s in elements) {
					if (elements[s].name == "")
						continue;
					var l = document.createElement("li");
					l.setAttribute("id", "element_" + elements[s].name);
					l.classList.add("list-group-item")
					l.classList.add("d-flex");
					l.classList.add("bd-highlight");
					l.classList.add("mb-3");
					var text = document.createElement("span");
					text.classList.add("me-auto");
					text.classList.add("p-2");
					text.classList.add("bd-highlight");
					text.innerHTML = elements[s].name;
					var isUser = document.createElement("span");
					isUser.classList.add("badge");
					isUser.classList.add("rounded-pill");
					isUser.classList.add("p-2");
					isUser.classList.add("bd-highlight");
					if (elements[s].isUser) {
						isUser.innerHTML = "User";
						isUser.classList.add("bg-primary");
					} else {
						isUser.innerHTML = "Group";
						isUser.classList.add("bg-info");
					}
					l.appendChild(text);
					l.appendChild(isUser);
					lista.appendChild(l);
					l.addEventListener("click", chatWith, false);
				}
			};

			function chatWith(evt) {
				console.log("Chat with " + evt.currentTarget.id);
				window.chatTo = evt.currentTarget.id.slice(8);
				window.typeChat = evt.target.parentNode.childNodes[1].innerText;
				console.log("chateando con: " + chatTo);
				loadChatRoom();
				//Load last messages from chat if any
			}
		});
}