(function () {
	
	var log = document.querySelector("#log"),
		baseStations = [],
		addMessage = function (name, msg) {
			var el = document.createElement("div"),
				buf = "<label>" + name + "</label>";
				
			buf += "<pre>" + JSON.stringify(msg, null, 2) + "</pre>";
			
			el.innerHTML = buf;
			el.classList.add("panel");
			log.appendChild(el);
		},
		onBaseStationState = function (data) {
			
		};
	
	var socket = io.connect();
	socket.emit('ready', {
		clientType: "monitor"
	});

	socket.on("ready", function (data) {
		onBasestationState(data);
	});
	socket.on("close", function (data) {
		console.log("receveived 'close' event", data);
	});
	socket.on("drone", function (data) {
		addMessage("drone", data);
	});
	
	document.querySelector("#drone").addEventListener("click", function () {
		socket.emit("drone", {
			type: "entered",
			proximity: "near | far | immediate",
			distance: 2,
			beacon: {
				uuid: "dead-deaddead-dead",
				major: 2,
				minor: 3
			}
		});
	}, false);
} ());