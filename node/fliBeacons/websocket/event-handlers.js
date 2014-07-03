var game = require('../game/game.js').game,
    baseStations = [],
    baseStationRoom = 'baseStations',
    monitorRoom = 'monitors',
    droneEvent = 'drone',
    baseStationsEvent = 'baseStations',
    baseStationRemovedEvent = 'baseStationRemoved',
    baseStationAddedEvent = 'baseStationAdded',
    baseStationUpdatedEvent = 'baseStationUpdated',
    gameStateEvent = 'gameState',
    
    removeBaseStationOnDisconnect = function(req) {
        baseStations = baseStations.filter(function(element) {
            if (element.socketId === req) {
                global.sockets.in(monitorRoom).emit(baseStationRemovedEvent, element);
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
        console.log(data);
        
        if (data) {
            req.socket.broadcast.emit(droneEvent, data);
            
            if(game.isRunning()) {
                game.checkGameState(req, data);
            }
        }
    },
    
    baseStation = function(req) {

        console.log(req.data);
        var baseStation = req.data || {},
            baseStationEvent = baseStationAddedEvent,
            alreadyAddedBaseStation = function(element) {
                if (element.id === baseStation.id) {
                    baseStationEvent = baseStationUpdatedEvent;
                    return false;
                }
                return true;
            };
        
        baseStation = enrichWithSocketId(req, baseStation);
        baseStations = baseStations.filter(alreadyAddedBaseStation);
        
        baseStations.push(baseStation);
        console.log(baseStationEvent, baseStation);
        req.io.manager.sockets.in(monitorRoom).emit(baseStationEvent, baseStation);
    },
    
    startCourse = function(req) {
        var randomSorter = function() {
            return (Math.round(Math.random())-0.5);
        };
        
        game.startCourse(req, baseStations.sort(randomSorter));
        
    },
    
    gameState = function(req) {
        req.socket.emit(gameStateEvent, game.getGameState());
    },
    
    getBaseStations = function(req) {
        req.socket.emit(baseStationsEvent, baseStations);
    };

exports.handlers = {
	ready: ready,
    disconnect: disconnect,
    drone: drone,
    baseStation: baseStation,
    startCourse: startCourse,
    gameState: gameState,
    baseStations: getBaseStations
};