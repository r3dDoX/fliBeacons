(function (global) {
	var stations = document.querySelector("#stations"),
		createId = function (id) {
			return "a" + id.replace(" ", "-");
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
		console.log(event, data);
		if (event === "baseStations") {
			updateBaseStations(data);
		} else if (event === "baseStationAdded") {
			updateBaseStations([data]);
		} else if (event === "baseStationRemoved") {
			removeBaseStation(data);
		} else if (event === "drone") {
			addDrone(data);
		} 
	});
	
}(this));