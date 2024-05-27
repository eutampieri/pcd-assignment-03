package pcd.ass03.sudokuMOM;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.google.gson.*;
import de.sfuhrm.sudoku.Creator;
import de.sfuhrm.sudoku.GameMatrix;
import de.sfuhrm.sudoku.GameSchemas;
import de.sfuhrm.sudoku.Riddle;

public class SudokuServer {
 // Nome dell'exchange RabbitMQ
 private static final String EXCHANGE_NAME = "sudoku_exchange";


    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Dichiarazione dell'exchange di tipo topic
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName = channel.queueDeclare().getQueue();
        // Binding della coda con l'exchange per tutti i topic
        channel.queueBind(queueName, EXCHANGE_NAME, "#");


        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
// Qui definiamo un callback per gestire i messaggi in arrivo. Quando un messaggio viene ricevuto, viene convertito in una stringa e analizzato come JSON per estrarre l'azione richiesta.
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");

            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String action = jsonMessage.get("action").getAsString();
            String topic = jsonMessage.get("topic").getAsString(); // Estraiamo il topic dal messaggio

            switch (action) {
                case "newGrid":
                    handleNewGrid(channel, topic);
                    break;
                case "setNumber":
                    handleSetNumber(jsonMessage, channel, topic);
                    break;
                default:
                    System.out.println("Unknown action: " + action);
            }
        };

        channel.basicConsume(queueName, true, deliverCallback, t -> {});
    }

    private static void handleNewGrid(Channel channel, String topic) throws IOException {
        // Creazione di una nuova griglia Sudoku

        // TOPIC?
        GameMatrix matrix = Creator.createFull(GameSchemas.SCHEMA_9X9);
        Riddle riddle = Creator.createRiddle(matrix);

        // Invia la griglia al giocatore
        JsonObject response = new JsonObject();
        response.addProperty("status", "newGrid");
        response.add("board", riddleToJson(riddle));

        String responseMessage = response.toString();
        // Invia il contenuto di responseMessage al topic specificato
        channel.basicPublish(EXCHANGE_NAME, topic, null, responseMessage.getBytes("UTF-8"));
    }
// Estraiamo i dati dalla richiesta JSON e impostiamo il numero nella griglia Sudoku. Poi, inviamo una conferma al giocatore insieme alla griglia aggiornata.
    private static void handleSetNumber(JsonObject jsonMessage, Channel channel, String topic) throws IOException {
        // Estrarre i dati dalla richiesta
        int row = jsonMessage.get("row").getAsInt();
        int col = jsonMessage.get("col").getAsInt();
        byte value = jsonMessage.get("value").getAsByte();

        // Imposta il numero nella griglia Sudoku
        // Supponiamo che matrix e riddle siano variabili di istanza o globali

        // QUI DOBBIAMO FARE IN MODO CHE VENGA MODIFICATA LA GRID DEL TOPIC
        riddle.set(row, col, value);

        // Invia la conferma al giocatore
        JsonObject response = new JsonObject();
        //response.addProperty("status", "numberSet");
        response.add("board", riddleToJson(riddle));

        String responseMessage = response.toString();
        channel.basicPublish(EXCHANGE_NAME, topic, null, responseMessage.getBytes("UTF-8"));
    }
// Questo metodo converte una griglia Sudoku in un formato JSON.
    private static JsonArray riddleToJson(Riddle riddle) {
        JsonArray jsonBoard = new JsonArray();
        for (int i = 0; i < 9; i++) {
            JsonArray row = new JsonArray();
            for (int j = 0; j < 9; j++) {
                row.add(riddle.get(i, j));
            }
            jsonBoard.add(row);
        }
        return jsonBoard;
    }
}
