var drones = [{name: 'drone', uuid: 'b9407f30-f5f8-466e-aff9-25556b57fe6d', major: 51881, minor: 16836}, {name:'human', uuid: 'asdf', major: 123, minor: 456}],
    baseStations = [],
    activeStation = 0,
    gameState = { isRunning: false },
    activateEvent = 'activate',
    startedEvent = 'started',
    finishedEvent = 'finished',
    updatedEvent = 'updated',
    
    isRunning = function() {
        return gameState.isRunning;
    },
    
    initRanking = function() {
        return drones.map(function(element) {
            return {
                drone: element,
                points: 0
            };
        });
    },
    
    initHistory = function() {
        return ['Game started'];
    },
    
    addToHistory = function(object) {
        gameState.history.push(object);
    },
    
    initGameState = function() {
        gameState = {};
        gameState.isRunning = true;
        gameState.ranking = initRanking();
        gameState.history = initHistory();
        gameState.activeStation = baseStations[activeStation];
    },
    
    startCourse = function(req, baseStationOrder) {
        var sockets = req.io.manager.sockets;
        
        baseStations = baseStationOrder;
        activeStation = 0;
        initGameState();
        
        if (baseStations.length > 0) {
            sockets.emit(startedEvent, gameState);
            sockets.emit(activateEvent, baseStations[activeStation]);
        }
    },
    
    updateRanking = function(drone) {
        gameState.ranking.map(function(element) {
            if (element.drone.uuid === drone.beacon.uuid) {
                element.points += 1;
            }
            
            return element;
        }).sort(function(a, b) {
            return a.points > b.points;
        });
    },
    
    moveOn = function(req, drone) {
        var sockets = req.io.manager.sockets,
            activeBaseStation;
        
        updateRanking(drone);
        addToHistory({drone: drone, baseStation: baseStations[activeStation]});
        
        if (activeStation === baseStations.length - 1) {
            addToHistory('Game finished');
            gameState.isRunning = false;
            sockets.emit(finishedEvent, gameState);
        } else {
            activeBaseStation = baseStations[++activeStation];
            
            sockets.emit(activateEvent, activeBaseStation);
            gameState.activeStation = activeBaseStation;
            sockets.emit(updatedEvent, gameState);
        }
    },
    
    checkGameState = function(req, drone) {
        if (gameState.isRunning && drone.baseStationId === baseStations[activeStation].id && drone.proximity === 'immediate') {
            moveOn(req, drone);
        }
    },
    
    getGameState = function() {
        return gameState;
    };

exports.game = {
    startCourse: startCourse,
    checkGameState: checkGameState,
    isRunning: isRunning,
    getGameState: getGameState
};