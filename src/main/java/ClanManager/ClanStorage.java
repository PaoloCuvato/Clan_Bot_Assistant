package ClanManager;

import MongoDB.MongoDBManager;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static MongoDB.MongoDBManager.getDatabase;

public class ClanStorage extends ListenerAdapter {
    static Guild guild;
    // Getter per tutti i clan in memoria
    static GuildReadyEvent event;
    @Getter
    private static final Map<String, Clan> clans = new HashMap<>();
    private static final Map<String, Clan> Leaderclans = new HashMap<>();

    private static final Map<String, Clan> userClanMap = new HashMap<>();


    // Aggiungi un clan alla memoria
    public static void addClan(Clan clan) {
        if (clan == null || clan.getName() == null || clan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Clan or clan name cannot be null or empty.");
        }
        clans.put(clan.getName(), clan);
        System.out.println("[Info] Clan added to the map: " + clan.getName());  // Log per confermare l'aggiunta

    }

    // Recupera un clan dalla memoria
    public static Clan getClan(String name) {
        return clans.get(name);
    }

    // Controlla se un clan esiste in memoria
    public static boolean hasClan(String name) {
        return clans.containsKey(name);
    }

    // Trova il clan di appartenenza di un utente (in memoria)
    public static Clan getClanByUser(User user) {
        for (Clan clan : clans.values()) { // Itera su tutti i clan
            if (clan.getListClanMember().contains(user)) { // Verifica se l'utente è nella lista membri
                return clan;
            }
        }
        return null; // Restituisci null se non è trovato
    }

    // Rimuovi un clan dalla memoria
    public static void removeClan(String name) {
        clans.remove(name);
    }

    public static boolean updateClanName(String oldName, String newName) {
        if (!hasClan(oldName)) {
            System.out.println("[Error] ❌ Error: Clan " + oldName + " does not exist in memory.");
            return false; // Clan not found in memory
        }
        if (hasClan(newName)) {
            System.out.println("[Error] ❌ Error: A clan with the name " + newName + " already exists.");
            return false; // The new name is already in use
        }

        // Retrieve the clan and remove the old name
        Clan clan = clans.get(oldName);
        clans.remove(oldName);

        // Update the clan name
        clan.updateClanName(newName);
        clans.put(newName, clan); // Save the clan with the new name

        // Update the database
        boolean dbUpdated = MongoDBManager.updateClanNameInDatabase(oldName, newName);

        if (!dbUpdated) {
            System.out.println("[⚠️ Warning]: Database update failed!");
            return false;
        }

        System.out.println("[Info] ✅ Clan name updated successfully: " + oldName + " ➝ " + newName);
        return true;
    }


    // ===================================
    // Metodi per interagire con il Database
    // ===================================
    public static void addClanToDatabase(Clan clan) {
        if (clan == null || clan.getName() == null || clan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Clan or clan name cannot be null or empty.");
        }
        MongoDBManager.insertClan(clan); // Inserisce nel database
    }

    // Elimina un clan dal database
    public static boolean deleteClanFromDatabase(String clanName) {
        if (MongoDBManager.getClanByName(clanName) != null) {
            MongoDBManager.deleteClan(clanName);
            return true;
        }
        return false;
    }

    public static boolean updateClanWinsInDatabase(String clanName, int newWins) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Creiamo la query per trovare il clan
        Document query = new Document("name", clanName);

        // Creiamo l'update per modificare solo le vittorie
        Document update = new Document("$set", new Document("wins", newWins));

        try {
            if (collection.updateOne(query, update).getModifiedCount() > 0) {
                System.out.println("[Info] ✅ Clan wins updated in MongoDB: " + clanName + " ➝ Wins: " + newWins);
                return true;
            } else {
                System.out.println("❌ Error: Clan " + clanName + " not found in the database.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating clan wins in the database: " + e.getMessage());
            return false;
        }
    }
    public static boolean updateClanLossesInDatabase(String clanName, int newLosses) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Creiamo la query per trovare il clan
        Document query = new Document("name", clanName);

        // Creiamo l'update per modificare solo le sconfitte
        Document update = new Document("$set", new Document("losses", newLosses));

        try {
            if (collection.updateOne(query, update).getModifiedCount() > 0) {
                System.out.println("[Info] ✅ Clan losses updated in MongoDB: " + clanName + " ➝ Losses: " + newLosses);
                return true;
            } else {
                System.out.println("[❌ Error] Clan " + clanName + " not found in the database.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("[❌ Error] updating clan losses in the database: " + e.getMessage());
            return false;
        }
    }
    static User getUserFromId(String userId)  {
        // Recupera il membro tramite l'ID
        Member member = event.getGuild().getMemberById(userId);  // Recupera il membro dalla guild tramite ID

        if (member != null) {
            return member.getUser();  // Restituisce l'oggetto User dal Member
        }
        return null;  // Restituisce null se il membro non viene trovato
    }

    private static Clan documentToClan(Document document) {
        try {
            String name = document.getString("name");
            int wins = document.getInteger("wins", 0);
            int losses = document.getInteger("losses", 0);
            String creationDateStr = document.getString("creationDate");
            String clanLeaderId = document.getString("clanLeaderId");
            List<String> memberIds = document.getList("members", String.class);

            System.out.println("[Info] Loaded document: " + document.toJson());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM:dd:yyyy HH:mm:ss");
            LocalDateTime creationDate = LocalDateTime.parse(creationDateStr, formatter);

            // Initialize clan
            Clan clan = new Clan(name, memberIds.toArray(), wins, losses);
            clan.setCreationDate(creationDate);
            clan.setClanLeaderId(clanLeaderId);

            // Clear existing list just in case
            clan.getListClanMember().clear();

            if (memberIds != null) {
                memberIds.stream()
                        .filter(id -> id != null && !id.isEmpty())
                        .distinct()
                        .forEach(id -> {
                            User user = getUserFromId(id);
                            if (user != null) {
                                clan.getListClanMember().add(user);
                                System.out.println("[Info] User added to clan: " + user.getName());
                            } else {
                                System.out.println("[Info] User not found for ID: " + id);
                            }
                        });
            }

            return clan;

        } catch (Exception e) {
            System.out.println("[Error] Error converting document to clan: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void loadAllClansFromDatabase() {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Check if the collection exists
        if (collection == null) {
            System.out.println("[Error] The 'clans' collection was not found in the database.");
            return;
        }

        // Log how many documents are present
        long count = collection.countDocuments();
        System.out.println("[Info] Total documents in 'clans' collection: " + count);

        FindIterable<Document> documents = collection.find();

        // Check if there are any documents to load
        if (!documents.iterator().hasNext()) {
            System.out.println("[Error] No clan documents found in the collection.");
            return;
        }

        // Loop through each document and convert it into a Clan object
        for (Document document : documents) {
            Clan clan = documentToClan(document);
            if (clan != null) {
                addClan(clan);
                System.out.println("[Info] Loaded clan: " + clan.getName());
            } else {
                System.out.println("[Error] Failed to load a clan from document: " + document.toJson());
            }
        }

        System.out.println("[Info] Finished loading all clans from the database.");
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        this.event= event;
        this.guild = event.getGuild();
        System.out.println("[Info] Guild is ready, loading clans...");

        ClanStorage.loadAllClansFromDatabase();
        System.out.println("[Info] All clans loaded.");
    }

}
