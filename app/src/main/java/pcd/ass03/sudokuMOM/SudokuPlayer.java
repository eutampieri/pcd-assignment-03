package pcd.ass03.sudokuMOM;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.google.gson.*;

public class SudokuPlayer {

    private static final String EXCHANGE_NAME = "sudoku_exchange";

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private SudokuGUI gui;
    private String queueName;

    public SudokuPlayer(SudokuGUI gui) throws IOException, TimeoutException {
        this.gui = gui;
        factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
        // Dichiarazione dell'exchange di tipo topic
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        queueName = channel.queueDeclare().getQueue();
        // Binding della coda con l'exchange per tutti i topic
        channel.queueBind(queueName, EXCHANGE_NAME, "#");

        // QUI STO RICEVENDO LE GRID MANDATE DAL SERVER (QUINDI O LA NUOVA GRID O QUELLA AGGIORNATA)
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            //System.out.println(" [x] Received '" + message + "'");

            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String topic = delivery.getEnvelope().getRoutingKey();
            JsonArray board = jsonMessage.getAsJsonArray("board");

            gui.updateGrid(topic, board);
        };

        channel.basicConsume(queueName, true, deliverCallback, t -> {});
    }

    public void newGrid(String topic) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "newGrid");
        request.addProperty("topic", topic);
        sendRequest(request.toString(), topic);
        // Binding della coda per ricevere i messaggi di questo topic
        // Il motivo principale per cui queueBind viene utilizzato nel metodo newGrid ma non nel metodo setGrid è che il binding della coda al topic è necessario solo una volta, quando viene creata una nuova griglia.
        channel.queueBind(queueName, EXCHANGE_NAME, topic);
    }

    public void setGrid(int row, int col, byte value, String topic) throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "setNumber");
        request.addProperty("row", row);
        request.addProperty("col", col);
        request.addProperty("value", value);
        request.addProperty("topic", topic);
        sendRequest(request.toString(), topic);
    }

    private void sendRequest(String request, String topic) throws IOException {
        channel.basicPublish(EXCHANGE_NAME, topic, null, request.getBytes("UTF-8")); // Invio la richiesta al topic specificato
        System.out.println(" [x] Sent '" + request + "' to topic '" + topic + "'");
    }

    public void close() throws IOException, TimeoutException {
        connection.close();
        channel.close();
    }
}
