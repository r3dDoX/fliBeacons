(function (global) {
	
	var log = document.querySelector("#log"),
		stations = document.querySelector("#stations"),
		tabs = document.querySelector('paper-tabs'),
		monitor = document.querySelector(".container"),
		game = document.querySelector("#game-container"),
		createId = function (id) {
			return "a" + id.replace(" ", "-");
		},
		addDrone = function (drone) {
			var el = document.createElement("div"),
				buf = "<label>Drone</label>",
				id = createId(drone.id);
				
				
			buf += "<pre>" + JSON.stringify(drone, null, 2) + "</pre>";
			
			el.innerHTML = buf;
			el.classList.add("panel");
			log.appendChild(el);
			console.log(drone);
			var stationEl = document.getElementById(createId(drone.id));
			stationEl.addDrone(drone);
			
		},
		updateBaseStations = function (baseStations) {
			baseStations.forEach(function (station) {
				var id = createId(station.id);
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
			var id = createId(baseStation.id),
				el = document.querySelector("#" + id);
			if (el) {	
				el.parentNode.removeChild(el);
			}
		},
		socket = io.connect(),
		stationCreatedCount = 0;
		
	var listeners = []; 
	global.messageBus = {
		register: function (listener) {
			listeners.push(listener);
		},
		fire: function (event, data) {
			listeners.forEach(function (listener) {
				listener(event, data);
			});
		}
	};
		
	socket.on("drone", function (drone) {
		addDrone(drone);
		global.messageBus.fire("drone", drone);
	});
	
	socket.on("baseStations", function (stations) {
		updateBaseStations(stations);
		global.messageBus.fire("baseStations", stations);
	});
	socket.on("baseStationAdded", function (station) {
		updateBaseStations([station]);
		global.messageBus.fire("baseStationAdded", station);
	});
	socket.on("baseStationRemoved", function (station) {
		removeBaseStation(station);
		global.messageBus.fire("baseStationRemoved", station);
	});
	
	global.messageBus.register(function (event, data) {
		console.log("bus", event, data);
	});
		
	socket.emit('ready', {
		clientType: "monitor"
	});
	

	tabs.addEventListener('core-select', function() {
		console.log(tabs.selected);
		if (tabs.selected === 'monitor') {
			monitor.style.display = "block";
			game.style.display = "none";
		} else {
			monitor.style.display = "none";
			game.style.display = "block";
		}
	});
	
	
	// prototyping
	document.querySelector("#drone-near").addEventListener("click", function () {
		socket.emit("drone", {
			type: "entered",
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
			id: "station-" + stationCreatedCount,
			lat: 32.333232,
			lng: 13.31210
		});
	}, false);
} (this));