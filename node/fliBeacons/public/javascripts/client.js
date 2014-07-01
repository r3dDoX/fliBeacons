(function () {
	
	var log = document.querySelector("#log"),
		stations = document.querySelector("#stations"),
		addDrone = function (drone) {
			var el = document.createElement("div"),
				buf = "<label>Drone</label>";
				
			buf += "<pre>" + JSON.stringify(drone, null, 2) + "</pre>";
			
			el.innerHTML = buf;
			el.classList.add("panel");
			log.appendChild(el);
			console.log(drone);
			var stationEl = document.getElementById(drone.stationUuid);
			stationEl.addDrone(drone);
			
		},
		updateBaseStations = function (baseStations) {
			baseStations.forEach(function (station) {
				var id = station.name.replace(" ", "-");
				if (!document.querySelector("#" + id)) {	
					var el = document.createElement("fli-base-station");
					el.name = station.name;
					el.lat= station.lat;
					el.lng= station.lng;
					el.setAttribute("id", id);
					stations.appendChild(el);
				}
			});
		},
		removeBaseStation = function (baseStation) {
			var id = station.name.replace(" ", "-"),
				el = document.querySelector("#" + id);
			if (el) {	
				el.parentNode.removeChild(el);
			}
		},
		socket = io.connect(),
		stationCreatedCount = 0;
		
	
	socket.on("ready", function (data) {
	});
	socket.on("close", function (data) {
		console.log("receveived 'close' event", data);
	});
	socket.on("drone", function (data) {
		addDrone(data);
	});
	
	socket.on("baseStations", function (stations) {
		updateBaseStations(stations);
	});
	socket.on("baseStationAdded", function (station) {
		updateBaseStations([station]);
	});
	socket.on("baseStationRemoved", function (station) {
		removeBaseStation(station);
	});
		
	socket.emit('ready', {
		clientType: "monitor"
	});
	
	
	document.querySelector("#drone-near").addEventListener("click", function () {
		socket.emit("drone", {
			type: "entered",
			stationUuid: "station-1",
			proximity: "near",
			distance: 2,
			beacon: {
				uuid: "dead-deaddead-dead",
				major: 2,
				minor: 3
			}
		});
	}, false);
	document.querySelector("#drone-immediate").addEventListener("click", function () {
		socket.emit("drone", {
			type: "entered",
			stationUuid: "station-1",
			proximity: "immediate",
			distance: 2,
			beacon: {
				uuid: "dead-deaddead-dead",
				major: 2,
				minor: 3
			}
		});
	}, false);
	document.querySelector("#drone-far").addEventListener("click", function () {
		socket.emit("drone", {
			type: "entered",
			stationUuid: "station-1",
			proximity: "far",
			distance: 2,
			beacon: {
				uuid: "dead-deaddead-dead",
				major: 2,
				minor: 3
			}
		});
	}, false);
	
	document.querySelector("#station").addEventListener("click", function () {
		stationCreatedCount++;
		socket.emit("baseStation", {
			name: "Station " + stationCreatedCount,
			uuid: "station-" + stationCreatedCount,
			lat: 32.333232,
			lng: 13.31210
		});
	}, false);
} ());