function loadSelectGroupsPage() {
	fetch('groups/select.html').then(response => response.text())
		.then(text => {
			document.getElementById('mainPage').innerHTML = text;
			document.getElementById('gotoCreate').addEventListener('click', loadCreatePage);
			document.getElementById('back').addEventListener('click', returnChatList);

			var navtab = document.getElementById("nav-tab");
			var tabTrigger = new bootstrap.Tab(navtab);
			navtab.addEventListener("click", function(evt) {
				evt.preventDefault();
				tabTrigger.show();
			});
			var xhttp = new XMLHttpRequest();
			xhttp.open("GET", "/ChatServer/groupMgmt?id=" + window.credentials.id + "&token=" + window.credentials.token, true);
			xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			xhttp.setRequestHeader("Accept", "application/json");
			xhttp.onload = function() {
				if (this.readyState == 4 && this.status == 200) {
					var resp = JSON.parse(this.responseText);
					if (resp.code == "OK") {
						console.log(resp);
						recreateList(resp.ownerOf, "ownerOf");
						recreateList(resp.memberOf, "memberOf")
					} else {
						console.log(resp.code);
						document.getElementById("warningtext").innerHTML = resp.code;
						warningModal.show();
					}
				}
			};
			xhttp.send();
		});

	function returnChatList() {
		loadChatList();
	}

	function recreateList(elements, lista) {
		var lista = document.getElementById(lista);
		while (lista.firstChild) {
			lista.removeChild(lista.firstChild);
		}

		for (var s in elements) {
			if (elements[s] == "")
				continue;
			var l = document.createElement("li");
			l.setAttribute("id", "element_" + elements[s]);
			l.classList.add("list-group-item");
			l.appendChild(document.createTextNode(elements[s]));
			lista.appendChild(l);
			l.addEventListener("click", loadAdminGroup, false);
		}
	}
}

function loadAdminGroup(evt) {
	var group = evt.target.id.slice(8);
	loadGroup(group);
}

function loadGroup(group) {
	fetch('groups/admin.html').then(response => response.text())
		.then(text => {
			document.getElementById('mainPage').innerHTML = text;
			document.getElementById('title').innerHTML = group;
			document.getElementById('add').addEventListener('click', addUser);
			document.getElementById('remove').addEventListener('click', removeUser);
			document.getElementById('back').addEventListener('click', loadSelectGroupsPage);
			document.getElementById('searchButton').addEventListener('click', searchUsers);
			populateUsers(group);
		});
}

function populateUsers(group) {
	var lista = document.getElementById('userList');
	const successModal = new bootstrap.Modal('#success');
	const successModalEl = document.getElementById('success');
	const warningModal = new bootstrap.Modal('#warning');
	const warningModalEl = document.getElementById('warning');
	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", "/ChatServer/userGroupMgmt?id=" + window.credentials.id + "&token=" + window.credentials.token + "&group=" + encodeURIComponent(group), true);
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xhttp.setRequestHeader("Accept", "application/json");
	xhttp.onload = function() {
		if (this.readyState == 4 && this.status == 200) {
			var resp = JSON.parse(this.responseText);
			if (resp.code == "OK") {
				console.log(resp);
				document.getElementById('owner').value = resp.owner;
				while (lista.firstChild) {
					lista.removeChild(lista.firstChild);
				}

				for (var s in resp.users) {
					if (resp.users[s] == "")
						continue;
					var l = document.createElement("option");
					//l.classList.add("list-group-item");
					l.appendChild(document.createTextNode(resp.users[s]));
					lista.appendChild(l);
				}
			} else {
				console.log(resp.code);
				document.getElementById("warningtext").innerHTML = resp.code;
				warningModal.show();
			}
		}
	};
	xhttp.send();
}

function searchUsers() {
	var searchText = document.getElementById("searchBox").value;
	if (searchText == "")
		return;
	const warningModal = new bootstrap.Modal('#warning');
	const warningModalEl = document.getElementById('warning');
	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", "/ChatServer/Userdata?id=" + window.credentials.id
		+ "&token=" + window.credentials.token + "&maxNumber=5&command=searchUsers&searchUsers=" + encodeURIComponent(searchText), true);
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xhttp.setRequestHeader("Accept", "application/json");
	xhttp.onload = function() {
		if (this.readyState == 4 && this.status == 200) {
			var resp = JSON.parse(this.responseText);
			if (resp.code == "OK") {
				console.log(resp);
				var lista = document.getElementById('totalUserList');
				while (lista.firstChild) {
					lista.removeChild(lista.firstChild);
				}

				for (var s in resp.users) {
					if (resp.users[s] == "")
						continue;
					var l = document.createElement("option");
					l.setAttribute('value', resp.users[s]);
					//l.appendChild(document.createTextNode(resp.users[s]));
					lista.appendChild(l);
				}
			} else {
				console.log(resp.code);
				document.getElementById("warningtext").innerHTML = resp.code;
				warningModal.show();
			}
		}
	};
	xhttp.send();
}

function addUser(evt) {
	const successModal = new bootstrap.Modal('#success');
	const successModalEl = document.getElementById('success');
	const warningModal = new bootstrap.Modal('#warning');
	const warningModalEl = document.getElementById('warning');
	var data = "id=" + window.credentials.id
		+ "&token=" + window.credentials.token
		+ "&group=" + encodeURIComponent(document.getElementById('title').innerHTML)
		+ "&user=" + encodeURIComponent(document.getElementById('searchBox').value);
	console.log("añadiendo usuario: " + data);
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "/ChatServer/userGroupMgmt", true);
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xhttp.setRequestHeader("Accept", "application/json");
	xhttp.onload = function() {
		if (this.readyState == 4 && this.status == 200) {
			var resp = JSON.parse(this.responseText);
			if (resp.code == "OK") {
				document.getElementById("successtext").innerHTML = "El usuario " + resp.user + " se ha a&ntilde;adido con &eacute;xito al grupo " + resp.group;
				successModal.show();
				loadGroup(document.getElementById('title').innerHTML);
			} else {
				console.log(resp.code);
				document.getElementById("warningtext").innerHTML = resp.code;
				warningModal.show();
			}
		}
	};
	xhttp.send(data);
}

function removeUser(evt) {
	var user = document.getElementById('userList').value;
	if (user !== null && typeof (user) !== undefined && user != "") {
		const successModal = new bootstrap.Modal('#success');
		const successModalEl = document.getElementById('success');
		const warningModal = new bootstrap.Modal('#warning');
		const warningModalEl = document.getElementById('warning');
		var data = "id=" + window.credentials.id
			+ "&token=" + window.credentials.token
			+ "&group=" + encodeURIComponent(document.getElementById('title').innerHTML)
			+ "&user=" + encodeURIComponent(document.getElementById('userList').value);

		var xhttp = new XMLHttpRequest();
		xhttp.open("DELETE", "/ChatServer/userGroupMgmt?" + data, true);
		xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhttp.setRequestHeader("Accept", "application/json");
		xhttp.onload = function() {
			if (this.readyState == 4 && this.status == 200) {
				var resp = JSON.parse(this.responseText);
				if (resp.code == "OK") {
					document.getElementById("successtext").innerHTML = "El usuario " + resp.user + " se ha eliminado del grupo " + resp.group;
					successModal.show();
					loadGroup(document.getElementById('title').innerHTML);
				} else {
					console.log(resp.code);
					document.getElementById("warningtext").innerHTML = resp.code;
					warningModal.show();
				}
			}
		};
		xhttp.send();
	}
}

function loadCreatePage() {
	fetch('groups/create.html').then(response => response.text())
		.then(text => {
			document.getElementById('mainPage').innerHTML = text;
			document.getElementById('owner').value = window.credentials.user;
			document.getElementById('submit').addEventListener('click', creaGrupo);
			document.getElementById('back').addEventListener('click', loadSelectGroupsPage);
		});
}

function creaGrupo() {
	const successModal = new bootstrap.Modal('#success');
	const successModalEl = document.getElementById('success');
	const warningModal = new bootstrap.Modal('#warning');
	const warningModalEl = document.getElementById('warning');
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", "/ChatServer/groupMgmt", true);
	xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xhttp.setRequestHeader("Accept", "application/json");
	xhttp.onload = function() {
		if (this.readyState == 4 && this.status == 200) {
			var resp = JSON.parse(this.responseText);
			if (resp.code == "OK") {
				document.getElementById("successtext").innerHTML = "El grupo " + resp.group + " se ha creado con &eacute;xito";
				successModal.show();
			} else {
				console.log(resp.code);
				document.getElementById("warningtext").innerHTML = resp.code;
				warningModal.show();
			}
		}
	}
	xhttp.send('id=' + window.credentials.id + '&token=' + window.credentials.token + '&group=' + document.getElementById('name').value);
}