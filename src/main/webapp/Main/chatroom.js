function loadChatRoom() {
	fetch('chatroom.html').then(response => response.text())
		.then(text => {
			
			document.getElementById('mainPage').innerHTML = text;

			var chatTo = window.chatTo;
			var user = window.credentials;

			if (chatTo == null || chatTo == "" || chatTo == user.user)
				loadChatList();

			const weekdays = ["Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"];


			// Initialize UI variables and components
			document.getElementById("back").addEventListener("click", backToChatList);
			document.getElementById("send").addEventListener("click", wsSendMessage);
			var mainChat = document.getElementById("mainChat");
			var bottomBar = document.getElementById("bottomBar");
			var chatWith = document.getElementById("chatWith");
			chatWith.innerText = chatTo;
			mainChat.style.marginTop = document.getElementById("topnavbar").offsetHeight + 'px';
			mainChat.style.marginBottom = document.getElementById("bottomnavbar").offsetHeight + 'px';

			//Initialize the Websocket client
			var websocket = new WebSocket("ws://" + location.host + "/ChatServer/Server/{" + JSON.stringify(user) + "}");
			websocket.onopen = function() { wsOpen(); };
			websocket.onmessage = function(message) { wsGetMessage(message); };
			websocket.onclose = function(message) { wsCloseConnection(message); };
			websocket.onerror = function(message) { wsError(message); };

			function wsOpen() {
				console.log(user.user + " está ahora conectado");
				bottomBar.style.background = "lightgreen";
			}

			function wsCloseConnection(x) {
				bottomBar.style.background = "white";
				console.log("Closing connection " + x);
				websocket.close();
				loadChatList();
			}

			function backToChatList() {
				loadChatList();
			}

			function wsGetMessage(x) {
				var message = JSON.parse(x.data);
				console.log("Ha llegado el mensaje " + message);

				if (message.type == "TEXT") {
					var temp = new Date((message.createdAt.seconds * 1000) + (message.createdAt.nanos / 1000000))
					message.createdAt = temp.toISOString();

					var newmess = document.createElement("div");
					newmess.classList.add("d-flex");
					var card = document.createElement("div");
					card.classList.add("card");
					card.classList.add("w-75");
					card.classList.add("border-primary");
					var cardBody = document.createElement("div");
					cardBody.classList.add("card-body");
					cardBody.style.background = "LightSalmon";
					var cardHeader = document.createElement("h6");
					cardHeader.classList.add("card-header");
					cardHeader.innerHTML = "<strong>" + message.from + "</strong>";
					var cardText = document.createElement("p");
					cardText.classList.add("card-text");
					cardText.innerText = message.content;
					var cardFooter = document.createElement("div");
					cardFooter.classList.add("card-footer");
					var row = document.createElement("div");
					row.classList.add("row");
					row.classList.add("justify-content-between");
					var dateReceived = document.createElement("small");
					dateReceived.innerText = formattedTime(message.createdAt);
					dateReceived.classList.add("col-5")
					var status = document.createElement("small");
					status.classList.add("col-5");
					status.id = message.id;
					status.innerText = "status";
					row.appendChild(dateReceived);
					row.appendChild(status);
					cardFooter.appendChild(row);
					cardBody.appendChild(cardText);
					card.appendChild(cardHeader);
					card.appendChild(cardBody);
					card.appendChild(cardFooter);
					newmess.appendChild(card);
					mainChat.appendChild(newmess);

				} else if (message.type == "CONTROL") {
					if (message.code == "OK") {
						var el = document.getElementById(message.id);
						el.innerText = "Recibido";
						if (message.status == "User not connected")
							document.getElementById("chatWith").style.color = "red";
					} else {
						var newmess = document.createElement("div");
						newmess.classList.add("d-flex");
						newmess.id = message.id;
						var l2 = document.createElement("div");
						l2.classList.add("card");
						l2.classList.add("w-75");
						var l3 = document.createElement("div");
						l3.classList.add("card-body");
						l3.style.background = "Crimson";
						var l4 = document.createElement("h5");
						l4.classList.add("card-title");
						l4.innerText = "Error";
						var l5 = document.createElement("p");
						l5.classList.add("card-text");
						l5.innerText = message.code;
						var l6 = document.createElement("p");
						l6.classList.add("card-text");
						l6.innerText = message.status;
						l4.appendChild(l5);
						l4.appendChild(l6);
						l3.appendChild(l4);
						l2.appendChild(l3);
						newmess.appendChild(l2);
						mainChat.appendChild(newmess);
						onsole.log("Invalid message type received " + message);
					}
				}

				window.scrollTo({
					top: mainChat.scrollHeight,
					behavior: 'smooth'
				});
			}

			function wsSendMessage() {
				var message = {
					type: "TEXT",
					id: uuidv4(),
					from: user.user,
					to: chatTo,
					createdAt: new Date().toISOString(),
					content: document.getElementById("chatText").value
				};

				console.log("Enviando el mensaje: " + message);
				document.getElementById("chatText").value = "";

				var newmess = document.createElement("div");
				newmess.classList.add("d-flex");
				newmess.classList.add("flex-row-reverse"); //Solo para mensaje propio
				var card = document.createElement("div");
				card.classList.add("card");
				card.classList.add("w-75");
				card.classList.add("border-primary");
				var cardBody = document.createElement("div");
				cardBody.classList.add("card-body");
				cardBody.style.background = "CornflowerBlue";
				var cardHeader = document.createElement("h6");
				cardHeader.classList.add("card-header");
				cardHeader.innerHTML = "<strong>T&uacute;</strong>";
				var cardText = document.createElement("p");
				cardText.classList.add("card-text");
				cardText.innerText = message.content;
				var cardFooter = document.createElement("div");
				cardFooter.classList.add("card-footer");
				var row = document.createElement("div");
				row.classList.add("row");
				row.classList.add("justify-content-between");
				var dateReceived = document.createElement("small");
				dateReceived.innerText = formattedTime(message.createdAt);
				dateReceived.classList.add("col-5")
				var status = document.createElement("small");
				status.classList.add("col-5");
				status.id = message.id;
				status.innerText = "status";
				row.appendChild(dateReceived);
				row.appendChild(status);
				cardFooter.appendChild(row);
				cardBody.appendChild(cardText);
				card.appendChild(cardHeader);
				card.appendChild(cardBody);
				card.appendChild(cardFooter);
				newmess.appendChild(card);
				mainChat.appendChild(newmess);

				window.scrollTo({
					top: mainChat.scrollHeight,
					behavior: 'smooth'
				});

				websocket.send(JSON.stringify(message));
			}

			function wsError(message) {
				console.log("Error ... \n" + message);
			}

			function uuidv4() {
				return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
					(c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
				);
			}

			function formattedTime(x) {
				let now = new Date().getTime();
				let dev = Date.parse(x);
				let startOfToday = new Date();
				startOfToday.setHours(0, 0, 0, 0);
				let sot = startOfToday.getTime();
				let timeDiff = Math.round((now - dev) / 1000); //Diferencia en segundos
				// let seconds = Math.floor(timeDiff % 60);
				// let secondsAsString = seconds < 10 ? "0" + seconds : seconds;
				timeDiff = Math.floor(timeDiff / 60);
				let minutes = timeDiff % 60;
				// let minutesAsString = minutes < 10 ? "0" + minutes : minutes;
				timeDiff = Math.floor(timeDiff / 60);
				let hours = timeDiff % 24;
				// return "Hace "+hours+ (hours ==1 ? " hora" : " horas")+" y "+minutes+" minutos";
				timeDiff = Math.floor(timeDiff / 24);
				let days = timeDiff;
				if (dev > sot) {
					if (hours == 0)
						if (minutes < 5)
							return "Ahora";
						else
							return minutes + " m.";
					else
						return hours + " h." + minutes + " m.";
				} else if (days <= 6) {
					let d = new Date(dev)
					hours = d.getHours();
					minutes = d.getMinutes();
					return weekdays[d.getDay()] + " " + (hours < 10 ? "0" + hours : hours) +
						":" + (minutes < 10 ? "0" + minutes : minutes);
				} else {
					return new Date(dev).toLocaleDateString('es-ES',
						{ day: 'numeric', month: 'short', year: 'numeric', hour: 'numeric', minute: 'numeric' });
				}
			}
		});
}
