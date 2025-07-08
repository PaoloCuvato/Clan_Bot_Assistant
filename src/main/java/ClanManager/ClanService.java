package ClanManager;

import MongoDB.MongoDBManager;
import com.mongodb.client.MongoCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ClanService extends ListenerAdapter {

    // In memoria: leaderId -> Clan
    private Map<Long, Clan> clanMap = new HashMap<>();

    // --- Singleton implementation ---
    private static ClanService instance;

    public static synchronized ClanService getInstance() {
        if (instance == null) {
            instance = new ClanService();
            instance.loadAllClansFromDB();
            log.info("ClanService instance created and clans loaded");
        }
        return instance;
    }
    // --- End Singleton ---

    private static final String COLLECTION_NAME = "clans";

    private void loadAllClansFromDB() {
        MongoCollection<Document> coll = MongoDBManager.getDatabase().getCollection(COLLECTION_NAME);
        int loadedCount = 0;

        for (Document doc : coll.find()) {
            try {
                Clan clan = documentToClan(doc);

                String leaderIdStr = clan.getClanLeaderId();
                if (leaderIdStr == null || leaderIdStr.isEmpty()) {
                    log.warn("Skipping clan document with missing or empty leaderId: {}", doc.toJson());
                    continue;
                }

                long leaderId = Long.parseLong(leaderIdStr);
                clanMap.put(leaderId, clan);
                loadedCount++;

            } catch (NumberFormatException e) {
                log.error("Invalid leader ID format in document: {}", doc.toJson(), e);
            } catch (Exception e) {
                log.error("Error loading clan from document: {}", doc.toJson(), e);
            }
        }

        log.info("✅ Loaded {} clans into memory", loadedCount);
    }

    public boolean addOrUpdateClan(Clan clan) {
        long leaderId = Long.parseLong(clan.getClanLeaderId());

        // Se è una nuova creazione, non permettere duplicati
        if (!clanMap.containsKey(leaderId) && isClanLeaderInAnyClan(leaderId)) {
            log.warn("Leader {} already has a clan, cannot create another", leaderId);
            return false;
        }

        // Aggiungi o aggiorna
        clanMap.put(leaderId, clan);
        upsertClanInDB(clan);
        log.info("Clan '{}' added/updated for leader {}", clan.getName(), leaderId);
        return true;
    }

    public void removeClanByLeader(long leaderId) {
        Clan clan = clanMap.remove(leaderId);
        if (clan != null) {
            MongoCollection<Document> coll = MongoDBManager.getDatabase()
                    .getCollection(COLLECTION_NAME);
            coll.deleteOne(eq("name", clan.getName()));
            log.info("Removed clan '{}' of leader {}", clan.getName(), leaderId);
        }
    }

    public Clan getClanByLeader(long leaderId) {
        return clanMap.get(leaderId);
    }

    public Clan getClanByMember(long memberId) {
        for (Clan clan : clanMap.values()) {
            if (clan.getClanLeaderId().equals(String.valueOf(memberId))
                    || clan.getMemberIds().contains(String.valueOf(memberId))) {
                return clan;
            }
        }
        return null;
    }


    public boolean isClanLeaderInAnyClan(long leaderId) {
        return clanMap.containsKey(leaderId);
    }


    public boolean isMemberInAnyClan(long memberId) {
        return getClanByMember(memberId) != null;
    }


    private void upsertClanInDB(Clan clan) {
        MongoCollection<Document> coll = MongoDBManager.getDatabase()
                .getCollection(COLLECTION_NAME);
        Document doc = clanToDocument(clan);
        coll.updateOne(eq("name", clan.getName()),
                new Document("$set", doc),
                new com.mongodb.client.model.UpdateOptions().upsert(true));
    }

    private static Document clanToDocument(Clan clan) {
        System.out.println("DEBUG leaderId: " + clan.getClanLeaderId()); // Verifica qui
        return new Document("name", clan.getName())
                .append("clanLeaderId", clan.getClanLeaderId())
                .append("members", clan.getMemberIds())
                .append("wins", clan.getWins())
                .append("losses", clan.getLosses())
                .append("creationDate", clan.getFormattedCreationDate());
    }


    private Clan documentToClan(Document doc) {
        String name = doc.getString("name");
        String leaderId = doc.getString("clanLeaderId");
        List<String> members = doc.getList("members", String.class);
        int wins = doc.getInteger("wins", 0);
        int losses = doc.getInteger("losses", 0);
        String creationDate = doc.getString("creationDate");
        return new Clan(name, leaderId, members, wins, losses, creationDate);
    }
}
