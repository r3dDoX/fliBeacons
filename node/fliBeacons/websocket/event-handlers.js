var clients = [],
    clientRoom = 'clients',
    monitorRoom = 'monitors',
    
    sendMonitorsUpdatedBaseStations = function(req) {
        req.io.manager.sockets.in(monitorRoom).emit('baseStationChange', clients);
    },
    
    ready = function(req) {
        var data = req.data || {};
        
        switch (data.clientType) {
            case 'monitor':
                req.socket.join(monitorRoom);
                break;
            case 'baseStation':
                clients.push({ id: req.socket.id });
                req.socket.join(clientRoom);
                sendMonitorsUpdatedBaseStations(req);
                break;
        }
    }, 
    
    disconnect = function(req) {
        var removeId = function(element) {
            return element.id !== req.socket.id;
        };
        
        clients = clients.filter(removeId);
    },
    
    drone = function(req) {
        var data = req.data;
        
        if (data) {
            req.socket.broadcast.emit('drone', data);
        }
    },
    
    baseStation = function(req) {
        clients[req.socket.id].baseStation = req.data;
    };

exports.handlers = {
	ready: ready,
    disconnect: disconnect,
    drone: drone,
    baseStation: baseStation
};