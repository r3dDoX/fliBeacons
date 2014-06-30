var sockets = {},
    ready = function(req) {
        sockets[req.socket.id] = {};
    }, 
    
    disconnect = function(req) {
        delete sockets[req.socket.id];
    },
    
    drone = function(req) {
        var data = req.data;
        
        if (data) {
            req.io.manager.sockets.emit('drone', data);
        }
    },
    
    baseStation = function(req) {
        sockets[req.socket.id].baseStation = req.data;
    };

exports.handlers = {
	ready: ready,
    disconnect: disconnect,
    drone: drone,
    baseStation: baseStation
};