package PlayerInfo;

import MongoDB.MongoDBManager;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import MongoDB.*;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class PlayerInfoMongoDBManager {

    private static final String COLLECTION_NAME = "ninjacards";

    public static void insertPlayerInfo(PlayerInfo info) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Document doc = playerInfoToDocument(info);
        collection.insertOne(doc);
    }

    public static void updatePlayerInfo(PlayerInfo info) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Document query = new Document("discordId", info.getDiscordId());
        Document update = new Document("$set", playerInfoToDocument(info));
        collection.updateOne(query, update);
    }
    // lobby update 
    public static void incrementLobbyCounter(long discordId, int amount) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Document query = new Document("discordId", discordId);
        Document update = new Document("$inc", new Document("lobbyCounter", amount));
        collection.updateOne(query, update);
    }

    public static PlayerInfo getPlayerInfoById(long discordId) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Document doc = collection.find(eq("discordId", discordId)).first();
        if (doc != null) {
            return documentToPlayerInfo(doc);
        }
        return null;
    }

    public static List<PlayerInfo> getAllPlayerInfos() {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        List<PlayerInfo> players = new ArrayList<>();
        for (Document doc : collection.find()) {
            players.add(documentToPlayerInfo(doc));
        }
        return players;
    }

    // ✅ NUOVO METODO: restituisce una mappa con ID Discord → PlayerInfo
    public static Map<Long, PlayerInfo> getAllPlayerInfosAsMap() {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        Map<Long, PlayerInfo> playerMap = new HashMap<>();
        for (Document doc : collection.find()) {
            PlayerInfo info = documentToPlayerInfo(doc);
            playerMap.put(info.getDiscordId(), info);
        }
        return playerMap;
    }

    public static void deletePlayerInfo(long discordId) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        collection.deleteOne(eq("discordId", discordId));
    }

    private static Document playerInfoToDocument(PlayerInfo info) {
        return new Document("discordId", info.getDiscordId())
                .append("discordUsername", info.getDiscordUsername())
                .append("playerName", info.getPlayerName())
                // converto gli array in List
                .append("game", Arrays.asList(info.getGame()))
                .append("platforms", Arrays.asList(info.getPlatforms()))
                .append("connectionType", info.getConnectionType())
            //    .append("inGamePlayTime", info.getInGamePlayTime())
                .append("currentRegion", info.getCurrentRegion())
            //    .append("targetRegion", info.getTargetRegion())
                //      .availablePlayTime(doc.getString("availablePlayTime"))
                .append("mostPlayedGame", info.getMostPlayedGame())
                .append("skillLevel", info.getSkillLevel())
                .append("spokenLanguages", Arrays.asList(info.getSpokenLanguages()))
                .append("lobbyCounter", info.getLobbyCounter());
    }


    private static PlayerInfo documentToPlayerInfo(Document doc) {
        return PlayerInfo.builder()
                .discordId(doc.getLong("discordId"))
                .discordUsername(doc.getString("discordUsername"))
                .playerName(doc.getString("playerName"))
                .game(doc.getList("game", String.class).toArray(new String[0]))
                .platforms(doc.getList("platforms", String.class).toArray(new String[0]))
                .connectionType(doc.getString("connectionType"))
          //      .inGamePlayTime(doc.getString("inGamePlayTime"))
                .currentRegion(doc.getString("currentRegion"))
           //     .targetRegion(doc.getString("targetRegion"))
          //      .availablePlayTime(doc.getString("availablePlayTime"))
                .mostPlayedGame(doc.getString("mostPlayedGame"))
                .skillLevel(doc.getString("skillLevel"))
                .spokenLanguages(doc.getList("spokenLanguages", String.class).toArray(new String[0]))
                .lobbyCounter(doc.getInteger("lobbyCounter", 0))
                .build();
    }
}
