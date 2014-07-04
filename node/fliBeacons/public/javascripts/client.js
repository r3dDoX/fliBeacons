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
        socket.on(e, function (data) {
            console.log(e, data);
            global.messageBus.fire(e, data);
        });
    });

    window.addEventListener('load', function () {
        socket.emit('ready', {
            clientType: "monitor"
        });
        console.log("ready emitted");
    });

    tabs.addEventListener('core-select', function () {
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
            image: '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',
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
        stationCreatedCount++;
        socket.emit("baseStation", {
            id: "id-" + stationCreatedCount,
            name: "Station " + stationCreatedCount,
            lat: 47.670162 + Math.random() * (max - min) + min,
            lng: 8.95015 + Math.random() * (max - min) + min
        });
    }, false);
}(this));