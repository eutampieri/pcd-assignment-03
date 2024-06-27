package pcd.ass03.sudokuRMI;

import de.sfuhrm.sudoku.*;
import pcd.ass03.sudoku.GameUpdate;
import pcd.ass03.sudoku.Pair;
import pcd.ass03.sudoku.ValueType;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class GameManagerImpl implements GameManager {
    private final Map<String, Riddle> games = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, List<GameEventsListener>> clients = Collections.synchronizedMap(new HashMap<>());

    protected GameManagerImpl() throws RemoteException {
        super();
    }

    @Override
    public String createGame() {
        String gameId = UUID.randomUUID().toString();
        GameMatrix matrix = Creator.createFull(GameSchemas.SCHEMA_9X9);
        Riddle riddle = Creator.createRiddle(matrix);
        games.put(gameId, riddle);
        clients.put(gameId, new ArrayList<>());
        return gameId;
    }

    @Override
    public void joinGame(String listenerId, String gameId) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost");
        GameEventsListener listener =  (GameEventsListener) registry.lookup(listenerId);
        clients.get(gameId).add(listener);
        Riddle riddle = games.get(gameId);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                ValueType status = riddle.getWritable(i, j) ? ValueType.USER : ValueType.GIVEN;
                listener.notifyGameUpdate(new GameUpdate(i, j, riddle.get(i, j), status));
            }
        }
    }

    @Override
    public void notifyClick(String gameId, Pair<Integer, Integer> position) throws RemoteException {
        this.clients.get(gameId).forEach(listener -> {
            try {
                listener.notifyClick(position);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void notifyUpdate(String gameId, GameUpdate update) throws RemoteException {
        this.games.get(gameId).set(update.getX(), update.getY(), (byte)update.getValue());
        this.clients.get(gameId).forEach(listener -> {
            try {
                listener.notifyGameUpdate(update);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static void main(String[] args) throws RemoteException {
        GameManager gameManager = new GameManagerImpl();
        GameManager gameManagerStub = (GameManager) UnicastRemoteObject.exportObject(gameManager, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("gameManager", gameManagerStub);
    }
}
