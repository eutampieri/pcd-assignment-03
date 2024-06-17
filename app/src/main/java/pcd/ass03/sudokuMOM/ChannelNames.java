package pcd.ass03.sudokuMOM;

public class ChannelNames {
    private static final String JOINS = "/joins";
    private static final String CLICKS = "/clicks";
    private static final String ANNOUNCE = "/announce";

    /**
     * Get base routing key for cell updates
     * @param id the game ID
     * @return the routing key
     */
    public static String getBaseRoutingKey(String id) {
        return id;
    }

    /**
     * Get base routing key for joins
     * @param id the game ID
     * @return the routing key
     */
    public static String getJoinsRoutingKey(String id) {
        return id + JOINS;
    }

    /**
     * Get base routing key for UI events
     * @param id the game ID
     * @return the routing key
     */
    public static String getClicksRoutingKey(String id) {
        return id + CLICKS;
    }

    /**
     * Get base routing key for node announcements
     * @param id the game ID
     * @return the routing key
     */
    public static String getAnnounceRoutingKey(String id) {
        return id + ANNOUNCE;
    }
}
