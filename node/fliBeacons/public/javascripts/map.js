(function (global) {

    var map = document.querySelector("google-map");

    global.messageBus.register(function(event, data) {
        console.log(event, data);
        if(event === 'mapSelected') {
            map.resize();
        }
        if(event === 'baseStations') {
            data.forEach(function(station) {
                console.log(station);
                addMapMarker(station);
                map.latitude = station.lat;
                map.longitude = station.lng;
            });
        } else if(event === 'baseStationAdded') {
            addMapMarker(data);
        }
    });

    function addMapMarker(station) {
        var marker = document.createElement("google-map-marker");
        marker.longitude = station.lng;
        marker.latitude = station.lat;
        map.appendChild(marker);
    }


}(this));