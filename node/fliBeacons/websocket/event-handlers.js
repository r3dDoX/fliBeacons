var baseStations = [],
    baseStationRoom = 'baseStations',
    monitorRoom = 'monitors',
    droneEvent = 'drone',
    baseStationUpdateEvent = 'baseStationUpdate',
    
    sendUpdatedBaseStations = function(sockets) {
        sockets.emit(baseStationUpdateEvent, baseStations);
    },
    
    removeBaseStationOnDisconnect = function(req) {
        var length = baseStations.length;
        
        baseStations = baseStations.filter(function(element) {
            return element.id !== req.socket.id;
        });
        
        if(length !== baseStations.length) {
            sendUpdatedBaseStations(req.io.manager.sockets.in(monitorRoom));
        }
    },
    
    ready = function(req) {
        var data = req.data || {};
        
        switch (data.clientType) {
            case 'monitor':
                req.socket.join(monitorRoom);
                sendUpdatedBaseStations(req.socket);
                break;
            case 'baseStation':
                req.socket.join(baseStationRoom);
                break;
        }
    }, 
    
    disconnect = function(req) {
        removeBaseStationOnDisconnect(req.socket.id);
    },
    
    drone = function(req) {
        var data = req.data;
        
        if (data) {
            req.socket.broadcast.emit(droneEvent, data);
        }
    },
    
    baseStation = function(req) {
        var baseStation = req.data || {};
        baseStation.id = req.socket.id;
        
        baseStations.push(baseStation);
        sendUpdatedBaseStations(req.io.manager.sockets.in(monitorRoom));
    };

exports.handlers = {
	ready: ready,
    disconnect: disconnect,
    drone: drone,
    baseStation: baseStation
};