var baseStations = [],
    baseStationRoom = 'baseStations',
    monitorRoom = 'monitors',
    droneEvent = 'drone',
    baseStationUpdateEvent = 'baseStationUpdate',
    
    sendBaseStations = function(req) {
        req.socket.emit(baseStationUpdateEvent, baseStations);
    },
    
    sendUpdatedBaseStations = function(req) {
        req.io.manager.sockets.in(monitorRoom).emit(baseStationUpdateEvent, baseStations);
    },
    
    ready = function(req) {
        var data = req.data || {};
        
        switch (data.clientType) {
            case 'monitor':
                req.socket.join(monitorRoom);
                sendBaseStations(req);
                break;
            case 'baseStation':
                baseStations.push({ id: req.socket.id });
                req.socket.join(baseStationRoom);
                break;
        }
    }, 
    
    disconnect = function(req) {
        var removeId = function(element) {
            return element.id !== req.socket.id;
        };
        
        baseStations = baseStations.filter(removeId);
    },
    
    drone = function(req) {
        var data = req.data;
        
        if (data) {
            req.socket.broadcast.emit(droneEvent, data);
        }
    },
    
    baseStation = function(req) {
        baseStations[req.socket.id].baseStation = req.data;
        sendUpdatedBaseStations(req);
    };

exports.handlers = {
	ready: ready,
    disconnect: disconnect,
    drone: drone,
    baseStation: baseStation
};