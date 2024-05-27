package pcd.ass03.sudokuMOM;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.sfuhrm.sudoku.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public final class GameFactory {
    public static Game joinGame(String gameId) throws IOException, TimeoutException {
        Riddle riddle = new GameMatrixFactory().newRiddle(GameSchemas.SCHEMA_9X9);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // TODO Send a random numner to gameId + "/join", wait for a response on gameId + "/" + randomFromBefore and set the node id to the response

        return new Sudoku(riddle, channel, gameId);
    }

    public static Game startGame() throws IOException, TimeoutException {
        GameMatrix matrix = Creator.createFull(GameSchemas.SCHEMA_9X9);
        Riddle riddle = Creator.createRiddle(matrix);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String gameId = "";

        return new Sudoku(riddle, channel, gameId);
    }
}