package Stat;

import MongoDB.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class PlayerStatMongoDBManager {

    private static final String COLLECTION_NAME = "playerstats";

    public static void insertPlayerStats(PlayerStats stats) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Document doc = playerStatsToDocument(stats);
        collection.insertOne(doc);
    }

    public static void updatePlayerStats(PlayerStats stats) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Document query = new Document("discordId", stats.getDiscordId());
        Document update = new Document("$set", playerStatsToDocument(stats));
        collection.updateOne(query, update, new UpdateOptions().upsert(true));
    }

    public static PlayerStats getPlayerStatsById(long discordId) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Document doc = collection.find(eq("discordId", discordId)).first();
        return doc != null ? documentToPlayerStats(doc) : null;
    }

    public static Map<Long, PlayerStats> getAllPlayerStatsAsMap() {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Map<Long, PlayerStats> statsMap = new HashMap<>();
        for (Document doc : collection.find()) {
            PlayerStats stats = documentToPlayerStats(doc);
            statsMap.put(stats.getDiscordId(), stats);
        }
        return statsMap;
    }

    public static void deletePlayerStats(long discordId) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        collection.deleteOne(eq("discordId", discordId));
    }

    private static Document playerStatsToDocument(PlayerStats stats) {
        return new Document("discordId", stats.getDiscordId())
                .append("lobbiesCreatedGeneral", stats.getLobbiesCreatedGeneral())
                .append("lobbiesCreatedDirect", stats.getLobbiesCreatedDirect())
                .append("lobbiesJoinedGeneral", stats.getLobbiesJoinedGeneral())
                .append("lobbiesJoinedDirect", stats.getLobbiesJoinedDirect())
                .append("hostAcceptedUserGeneral", stats.getHostAcceptedUserGeneral())
                .append("wasAcceptedGeneral", stats.getWasAcceptedGeneral())
                .append("wasAcceptedDirect", stats.getWasAcceptedDirect())
                .append("declinedUserGeneral", stats.getDeclinedUserGeneral())
                .append("declinedUserDirect", stats.getDeclinedUserDirect())
                .append("wasDeclinedGeneral", stats.getWasDeclinedGeneral())
                .append("wasDeclinedDirect", stats.getWasDeclinedDirect())
                .append("ignoredRequestGeneral", stats.getIgnoredRequestGeneral())
                .append("ignoredRequestDirect", stats.getIgnoredRequestDirect())
                .append("lobbiesCompletedGeneral", stats.getLobbiesCompletedGeneral())
                .append("lobbiesCompletedDirect", stats.getLobbiesCompletedDirect())
                .append("lobbiesIncompleteGeneral", stats.getLobbiesIncompleteGeneral())
                .append("lobbiesIncompleteDirect", stats.getLobbiesIncompleteDirect())
                .append("lobbiesDisbandedGeneral", stats.getLobbiesDisbandedGeneral())
                .append("lobbiesDisbandedDirect", stats.getLobbiesDisbandedDirect())
                .append("score", stats.getScore());
    }

    private static PlayerStats documentToPlayerStats(Document doc) {
        PlayerStats stats = new PlayerStats();
        stats.setDiscordId(doc.getLong("discordId"));
        stats.setLobbiesCreatedGeneral(doc.getInteger("lobbiesCreatedGeneral", 0));
        stats.setLobbiesCreatedDirect(doc.getInteger("lobbiesCreatedDirect", 0));
        stats.setLobbiesJoinedGeneral(doc.getInteger("lobbiesJoinedGeneral", 0));
        stats.setLobbiesJoinedDirect(doc.getInteger("lobbiesJoinedDirect", 0));
        stats.setHostAcceptedUserGeneral(doc.getInteger("hostAcceptedUserGeneral", 0));
        stats.setWasAcceptedGeneral(doc.getInteger("wasAcceptedGeneral", 0));
        stats.setWasAcceptedDirect(doc.getInteger("wasAcceptedDirect", 0));
        stats.setDeclinedUserGeneral(doc.getInteger("declinedUserGeneral", 0));
        stats.setDeclinedUserDirect(doc.getInteger("declinedUserDirect", 0));
        stats.setWasDeclinedGeneral(doc.getInteger("wasDeclinedGeneral", 0));
        stats.setWasDeclinedDirect(doc.getInteger("wasDeclinedDirect", 0));
        stats.setIgnoredRequestGeneral(doc.getInteger("ignoredRequestGeneral", 0));
        stats.setIgnoredRequestDirect(doc.getInteger("ignoredRequestDirect", 0));
        stats.setLobbiesCompletedGeneral(doc.getInteger("lobbiesCompletedGeneral", 0));
        stats.setLobbiesCompletedDirect(doc.getInteger("lobbiesCompletedDirect", 0));
        stats.setLobbiesIncompleteGeneral(doc.getInteger("lobbiesIncompleteGeneral", 0));
        stats.setLobbiesIncompleteDirect(doc.getInteger("lobbiesIncompleteDirect", 0));
        stats.setLobbiesDisbandedGeneral(doc.getInteger("lobbiesDisbandedGeneral", 0));
        stats.setLobbiesDisbandedDirect(doc.getInteger("lobbiesDisbandedDirect", 0));
        stats.setScore(doc.getString("score"));
        return stats;
    }
}
