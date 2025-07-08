package ClanManager;

import MongoDB.MongoDBManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class ClanManager extends ListenerAdapter {

    private static final String COLLECTION_NAME = "clans";

    // 🔧 Inserisce un nuovo clan nel database
    public static void insertClan(Clan clan) {
        MongoCollection<Document> collection = getClanCollection();
        Document doc = clanToDocument(clan);
        collection.insertOne(doc);
    }

    // 🔄 Aggiorna o crea (upsert) un clan
    public static void updateClan(Clan clan) {
        MongoCollection<Document> collection = getClanCollection();
        Document query = new Document("name", clan.getName());
        Document update = new Document("$set", clanToDocument(clan));
        collection.updateOne(query, update, new UpdateOptions().upsert(true));
    }

    // 🔍 Ottieni clan per nome
    public static Clan getClanByName(String name) {
        MongoCollection<Document> collection = getClanCollection();
        Document doc = collection.find(eq("name", name)).first();
        return doc != null ? documentToClan(doc) : null;
    }

    // 🔍 Ottieni il clan a cui un utente appartiene
    public static Clan getClanByUserId(String userId) {
        for (Clan clan : getAllClans()) {
            if (clan.getClanLeaderId().equals(userId) || clan.getMemberIds().contains(userId)) {
                return clan;
            }
        }
        return null;
    }

    // 📚 Tutti i clan in mappa (opzionale)
    public static Map<String, Clan> getAllClansAsMap() {
        Map<String, Clan> map = new HashMap<>();
        for (Clan clan : getAllClans()) {
            map.put(clan.getName(), clan);
        }
        return map;
    }

    // 📚 Tutti i clan in lista
    public static List<Clan> getAllClans() {
        MongoCollection<Document> collection = getClanCollection();
        List<Clan> clans = new ArrayList<>();
        for (Document doc : collection.find()) {
            clans.add(documentToClan(doc));
        }
        return clans;
    }

    // 🗑 Elimina clan per nome
    public static void deleteClan(String name) {
        MongoCollection<Document> collection = getClanCollection();
        collection.deleteOne(eq("name", name));
    }

    // 👤 Controlla se un userId è in un clan
    public static boolean isUserInAnyClan(String userId) {
        return getClanByUserId(userId) != null;
    }

    private static MongoCollection<Document> getClanCollection() {
        return MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
    }

    private static Document clanToDocument(Clan clan) {
        return new Document("name", clan.getName())
                .append("clanLeaderId", clan.getClanLeaderId())
                .append("members", clan.getMemberIds())
                .append("wins", clan.getWins())
                .append("losses", clan.getLosses())
                .append("creationDate", clan.getFormattedCreationDate());
    }

    private static Clan documentToClan(Document doc) {
        String name = doc.getString("name");
        String leaderId = doc.getString("clanLeaderId");
        List<String> members = doc.getList("members", String.class);
        int wins = doc.getInteger("wins", 0);
        int losses = doc.getInteger("losses", 0);
        String creationDate = doc.getString("creationDate");

        return new Clan(name, leaderId, members, wins, losses, creationDate);
    }
}
