var game = require('../game/game.js').game,
    baseStations = [],
    baseStationRoom = 'baseStations',
    monitorRoom = 'monitors',
    droneEvent = 'drone',
    baseStationsEvent = 'baseStations',
    baseStationRemovedEvent = 'baseStationRemoved',
    baseStationAddedEvent = 'baseStationAdded',
    
    removeBaseStationOnDisconnect = function(req) {
        baseStations = baseStations.filter(function(element) {
            if (req.socket && element.socketId === req.socket.id) {
                req.io.manager.sockets.in(monitorRoom).emit(baseStationRemovedEvent, element);
                return false;
            }

            return true;
        });
    },
    
    enrichWithSocketId = function(req, data) {
        data.socketId = req.socket.id;
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
            req.socket.broadcast.emit(droneEvent, data);
            
            if(game.isRunning()) {
                game.checkGameState(req, data);
            }
        }
    },
    
    baseStation = function(req) {
        var baseStation = req.data || {},
            alreadyAddedBaseStation = function(element) {
                return element.id !== baseStation.id;
            };
        
        baseStation = enrichWithSocketId(req, baseStation);
        baseStations = baseStations.filter(alreadyAddedBaseStation);
        
        baseStations.push(baseStation);
        req.io.manager.sockets.in(monitorRoom).emit(baseStationAddedEvent, baseStation);
    },
    
    startCourse = function(req) {
        var randomSorter = function() {
            return (Math.round(Math.random())-0.5);
        };
        
        game.startCourse(req, baseStations.sort(randomSorter));
        
    };

exports.handlers = {
	ready: ready,
    disconnect: disconnect,
    drone: drone,
    baseStation: baseStation,
    startCourse: startCourse
};