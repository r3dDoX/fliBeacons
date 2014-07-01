var baseStations = [],
    baseStationRoom = 'baseStations',
    monitorRoom = 'monitors',
    droneEvent = 'drone',
    baseStationUpdateEvent = 'baseStationUpdate',
    
    sendUpdatedBaseStations = function(sockets) {
        sockets.emit(baseStationUpdateEvent, baseStations);
    },
    
    removeBaseStationOnDisconnect = function(req) {
        var updated = false,
            removeBaseStation = function(element) {
                if (element.id !== req.socket.id) {
                    updated = true;
                    return updated;
                }

                return false;
            };
        
        baseStations = baseStations.filter(removeBaseStation);
        
        if(updated) {
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