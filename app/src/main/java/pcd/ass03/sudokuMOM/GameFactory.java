package pcd.ass03.sudokuMOM;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.sfuhrm.sudoku.*;
import pcd.ass03.sudoku.Game;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static pcd.ass03.sudokuMOM.Sudoku.EXCHANGE_NAME;

public final class GameFactory {
    public static Game joinGame(String gameId) throws IOException, TimeoutException {
        Riddle riddle = new GameMatrixFactory().newRiddle(GameSchemas.SCHEMA_9X9);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        Sudoku sudoku = new Sudoku(riddle, channel, gameId);

        final String queueName = channel.queueDeclare().getQueue();
        final String routingKey = ChannelNames.getAnnounceRoutingKey(gameId);
        AtomicBoolean requestSent = new AtomicBoolean(false);
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
        System.out.println("[" + sudoku.getNodeId() + "] Stato iniziale variabile " + requestSent);
        channel.basicConsume(queueName, (consumerTag, x) -> {
            String body = new String(x.getBody(), StandardCharsets.UTF_8);
            System.out.println("[" + sudoku.getNodeId() + "] Received announcement from " + body);
            if (!requestSent.get() && !body.isEmpty() && !body.equals(Integer.toString(sudoku.getNodeId()))) {
                channel.basicPublish(EXCHANGE_NAME, ChannelNames.getJoinsRoutingKey(gameId), null, body.getBytes());
                requestSent.compareAndSet(false, true);
                System.out.println("[" + sudoku.getNodeId() + "] Announcement sent to " + body);
            } else {
                System.out.println("[" + sudoku.getNodeId() + "] Ignoring " + body + " " + requestSent.get());
            }
        }, x -> {
        });
        
        System.out.println("[" + sudoku.getNodeId() + "] Asking for nodes on game " + gameId);
        channel.basicPublish(EXCHANGE_NAME, ChannelNames.getAnnounceRoutingKey(gameId), null, "".getBytes(StandardCharsets.UTF_8));
        return sudoku;
    }

    public static Game startGame() throws IOException, TimeoutException {
        GameMatrix matrix = Creator.createFull(GameSchemas.SCHEMA_9X9);
        Riddle riddle = Creator.createRiddle(matrix);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String gameId = UUID.randomUUID().toString();

        return new Sudoku(riddle, channel, gameId);
    }
}