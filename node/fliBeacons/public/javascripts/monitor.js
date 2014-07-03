(function (global) {
	var stations = document.querySelector("#stations"),
		createId = function (id) {
			return "a" + id.replace(" ", "-");
		},
		removeDrone = function (drone) {
			var id = createId(drone.baseStationId),
				stationEl = document.getElementById(id);
				
			stationEl.removeDrone(drone);
		},
		moveDrone = function (drone) {
			var id = createId(drone.baseStationId),
				stationEl = document.getElementById(id);
				
			stationEl.moveDrone(drone);
		},
		addDrone = function (drone) {
			var el = document.createElement("div"),
				buf = "<label>Drone</label>",
				id = createId(drone.baseStationId);
				
				
			buf += "<pre>" + JSON.stringify(drone, null, 2) + "</pre>";
			
			el.innerHTML = buf;
			el.classList.add("panel");
			log.appendChild(el);
			var stationEl = document.getElementById(id);
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
		};
		
		
	global.messageBus.register(function (event, data) {
		if (event === "baseStations") {
			updateBaseStations(data);
		} else if (event === "baseStationAdded") {
			updateBaseStations([data]);
		} else if (event === "baseStationRemoved") {
			removeBaseStation(data);
		} else if (event === "activate") { 
			var id = createId(data.id),
				baseStation = document.getElementById(id),
				allStations = document.getElementsByTagName("fli-base-station");
				
			for (var i = 0; i < allStations.length; i++) {
				allStations.item(i).classList.remove("active");
			}
			
			if (baseStation) {
				baseStation.classList.add("active");	
			}
		} else if (event === "drone") {
			if (data.type === "entered") {
				addDrone(data);
			} else if (data.type === "removed") {
				removeDrone(data);
			} else {
				moveDrone(data);
			}
		} 
	});
	
}(this));