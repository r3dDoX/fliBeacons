(function (global) {
	
	var log = document.querySelector("#log"),
		stations = document.querySelector("#stations"),
		tabs = document.querySelector('paper-tabs'),
		monitor = document.querySelector(".container"),
		game = document.querySelector("#game-container"),
		socket = io.connect(),
		stationCreatedCount = 0,
        listeners = []; 
    
    global.socket = socket;
    
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
		global.messageBus.fire("drone", drone);
	});
	socket.on("baseStations", function (stations) {
		global.messageBus.fire("baseStations", stations);
	});
	socket.on("baseStationAdded", function (station) {
		global.messageBus.fire("baseStationAdded", station);
	});
	socket.on("baseStationRemoved", function (station) {
		global.messageBus.fire("baseStationRemoved", station);
	});
	socket.on("updated", function (gameState) {
		global.messageBus.fire("updated", gameState);
	});
	socket.on("finished", function (gameState) {
		global.messageBus.fire("finished", gameState);
	});
		
	socket.emit('ready', {
		clientType: "monitor"
	});

	tabs.addEventListener('core-select', function() {
		if (tabs.selected === 'monitor') {
			monitor.style.display = "block";
			game.style.display = "none";
		} else {
			monitor.style.display = "none";
			game.style.display = "block";
            global.messageBus.fire("mapSelected");
		}
	});
	
	
	// prototyping
	document.querySelector("#drone-near").addEventListener("click", function () {
		socket.emit("drone", {
			type: "entered",
			proximity: "near",
			baseStationId: "station-1",
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
			baseStationId: "station-1",
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
			baseStationId: "station-1",
			distance: 2,
			beacon: {
				uuid: "dead-deaddead-dead",
				major: 2,
				minor: 3
			}
		});
	}, false);
	
	document.querySelector("#station").addEventListener("click", function () {
		var max = 0.01,
            min = -0.01;
        stationCreatedCount++;
		socket.emit("baseStation", {
			id: "id-" + stationCreatedCount,
			name: "Station " + stationCreatedCount,
			id: "station-" + stationCreatedCount,
			lat: 47.670162 + Math.random() * (max - min) + min,
			lng: 8.95015 + Math.random() * (max - min) + min
		});
	}, false);
} (this));