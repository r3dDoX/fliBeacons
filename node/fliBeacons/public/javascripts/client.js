(function (global) {
	
	var q = q.bind(document),
		log = q("#log"),
		stations = q("#stations"),
		tabs = q('paper-tabs'),
		monitor = q(".container"),
		game = q("#game-container"),
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
		console.log(drone);
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
	socket.on("activate", function (baseStation) {
		global.messageBus.fire("activate", baseStation);
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
		}
	});
	
	
	// prototyping
	q("#drone-dialog").addEventListener("click", function () {
		q("#drone").toggle();
	}, false);
	
	q("#send-drone").addEventListener("click", function () {
		console.log(q("#type [checked]").getAttribute("name"));
		socket.emit("drone", {
			type: q("#type [checked]").getAttribute("name"),
			proximity: q("#proximity [checked]").getAttribute("name"),
			baseStationId: q("#baseStationId").value,
			distance: q("#distance").value,
			beacon: {
				uuid: q("#beaconUuid").value,
				major: q("#beaconMajor").value,
				minor: q("#beaconMinor").value
			}
		});
	});
	
	q("#station").addEventListener("click", function () {
		stationCreatedCount++;
		socket.emit("baseStation", {
			id: "id-" + stationCreatedCount,
			name: "Station-" + stationCreatedCount,
			id: "Station-" + stationCreatedCount,
			lat: 32.333232,
			lng: 13.31210
		});
	}, false);
} (this));