(function (global) {

    var map = document.querySelector("google-map"),
        stations = [];

    global.messageBus.register(function (event, data) {
        console.log(event, data);
        if (event === 'mapSelected') {
            map.resize();
        } else if (event === 'baseStations') {
            data.forEach(function (station) {
                stations[station.id] = station;
                addMapMarker(station);
            });
        } else if (event === 'baseStationAdded') {
            stations[data.id] = data;
            addMapMarker(data);
        } else if (event === 'baseStationUpdated') {
            stations[data.id] = data;
            map.clear();
            console.log("stations", stations);
            for (var id in stations) {
                // do something with key
                console.log(id);
                addMapMarker(stations[id]);
            }
        }
    });

    function addMapMarker(station) {
        var marker = document.createElement("google-map-marker");
        marker.setAttribute("id", station.id);
        marker.longitude = station.lng;
        marker.latitude = station.lat;
        marker.setAttribute('title', station.name);

        map.appendChild(marker);
        console.log(marker.longitude);
    }


}(this));