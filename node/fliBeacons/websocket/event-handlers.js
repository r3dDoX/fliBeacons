var baseStations = [],
    baseStationRoom = 'baseStations',
    monitorRoom = 'monitors',
    droneEvent = 'drone',
    baseStationsEvent = 'baseStations',
    baseStationRemovedEvent = 'baseStationRemoved',
    baseStationAddedEvent = 'baseStationAdded',
    
    removeBaseStationOnDisconnect = function(req) {
        baseStations = baseStations.filter(function(element) {
            if (req.socket && element.id === req.socket.id) {
                req.io.manager.sockets.in(monitorRoom).emit(baseStationRemovedEvent, element);
                return false;
            }

            return true;
        });
    },
    
    enrichWithSocketId = function(req, data) {
        data.id = req.socket.id;
        return data;
    },
    
    ready = function(req) {
        var data = req.data || {};
        
        switch (data.clientType) {
            case 'monitor':
                req.socket.join(monitorRoom);
                req.socket.emit(baseStationsEvent, baseStations);
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
            data = enrichWithSocketId(req, data);
            req.socket.broadcast.emit(droneEvent, data);
        }
    },
    
    baseStation = function(req) {
        var baseStation = req.data || {};
        baseStation = enrichWithSocketId(req, baseStation);
        
        baseStations.push(baseStation);
        req.io.manager.sockets.in(monitorRoom).emit(baseStationAddedEvent, baseStation);
    };

exports.handlers = {
	ready: ready,
    disconnect: disconnect,
    drone: drone,
    baseStation: baseStation
};