(function (global) {
	
	var q = document.querySelector.bind(document),
		log = q("#log"),
		stations = q("#stations"),
		tabs = q('paper-tabs'),
		monitor = q(".container"),
		game = q("#game-container"),
		socket = io.connect(),
		stationCreatedCount = 0,
        listeners = [],
        events = ['drone', 'baseStations', 'baseStationAdded', 'baseStationUpdated', 'baseStationRemoved', 'updated', 'started', 'finished', 'activate', 'gameState'];
    
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

    events.forEach(function (e) {
        socket.on(e, function(data) {
            console.log(data);
            global.messageBus.fire(e, data);
        });
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
            global.messageBus.fire('mapSelected');
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
        var max = 0.01,
            min = -0.01;
        stationCreatedCount ++;
		socket.emit("baseStation", {
			id: "id-" + stationCreatedCount,
			name: "Station " + stationCreatedCount,
            lat: 47.670162 + Math.random() * (max - min) + min,
            lng: 8.95015 + Math.random() * (max - min) + min
        });
	}, false);
} (this));