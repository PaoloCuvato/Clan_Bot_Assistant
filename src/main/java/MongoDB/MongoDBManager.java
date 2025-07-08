package MongoDB;

import ClanManager.Clan;
import Config.Config;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public class MongoDBManager {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static MongoDatabase getDatabase() {
        Config config= new Config();
        if (database == null) {
            String uri = config.getDbConnection();
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase(config.getDbName());
        }
        return database;
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    // Inserisci un clan nel database
    public static void insertClan(Clan clan) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Serializza la lista dei membri
        List<String> memberIDs = clan.getListClanMember().stream()
                .map(user -> user.getId())  // Usa il tag dell'utente (MODIFICATO DA GET AS TAG A ID)
                .collect(Collectors.toList());

        // Creazione del documento
        Document clanDocument = new Document("name", clan.getName())
                .append("clanLeaderId", clan.getClanLeaderId())
                .append("wins", clan.getWins())
                .append("losses", clan.getLosses())
                .append("creationDate", clan.getFormattedCreationDate())
                .append("members", memberIDs);

        // Log documento da inserire
        System.out.println("Document to insert: " + clanDocument.toJson());

        // Log delle collezioni nel database per verifica
        MongoDatabase db = getDatabase();
        for (String collectionName : db.listCollectionNames()) {
            System.out.println("Collection: " + collectionName);
        }

        try {
            collection.insertOne(clanDocument);
            System.out.println("Clan inserted successfully");
        } catch (Exception e) {
            System.err.println("Error during insertion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean updateClanNameInDatabase(String oldName, String newName) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Check if the clan exists in the database
        Document query = new Document("name", oldName);
        Document update = new Document("$set", new Document("name", newName));

        try {
            if (collection.updateOne(query, update).getModifiedCount() > 0) {
                System.out.println("✅ Clan name updated in MongoDB: " + oldName + " ➝ " + newName);
                return true;
            } else {
                System.out.println("❌ Error: Clan " + oldName + " was not found in the database.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating clan name in the database: " + e.getMessage());
            return false;
        }
    }

    public static boolean addUserToClan(String clanName, String userID) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Aggiungi l'ID all'array "members" solo se non è già presente
        Document query = new Document("name", clanName);
        Document update = new Document("$addToSet", new Document("members", userID));

        try {
            if (collection.updateOne(query, update).getModifiedCount() > 0) {
                System.out.println("✅ User " + userID + " added to clan " + clanName);
                return true;
            } else {
                System.out.println("⚠️ Clan not found or user already in the clan.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error adding user to clan: " + e.getMessage());
            return false;
        }
    }

    public static boolean removeUserFromClan(String clanName, String userID) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Rimuove l'ID dall'array "members"
        Document query = new Document("name", clanName);
        Document update = new Document("$pull", new Document("members", userID));

        try {
            if (collection.updateOne(query, update).getModifiedCount() > 0) {
                System.out.println("✅ User " + userID + " removed from clan " + clanName);
                return true;
            } else {
                System.out.println("⚠️ Clan not found or user not in the clan.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error removing user from clan: " + e.getMessage());
            return false;
        }
    }



    // Recupera un clan per nome
    public static Clan getClanByName(String name) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");
        Document document = collection.find(eq("name", name)).first();

        if (document != null) {
            Clan clan = new Clan();
            clan.setName(document.getString("name"));
            clan.setWins(document.getInteger("wins"));
            clan.setLosses(document.getInteger("losses"));
            // Puoi recuperare anche la lista dei membri e altri dettagli
            return clan;
        }
        return null; // Se non trovato
    }

    // Aggiorna un clan
    public static void updateClan(Clan clan) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");
        Document query = new Document("name", clan.getName());
        Document update = new Document("$set", new Document("wins", clan.getWins())
                .append("losses", clan.getLosses())
                .append("creationDate", clan.getFormattedCreationDate()));
        collection.updateOne(query, update);
    }

    // Rimuovi un clan
    public static void deleteClan(String name) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");
        collection.deleteOne(eq("name", name));
    }
}
