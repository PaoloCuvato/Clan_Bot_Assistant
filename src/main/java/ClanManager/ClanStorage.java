package ClanManager;

import MongoDB.MongoDBManager;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClanStorage {
    // Getter per tutti i clan in memoria
    @Getter
    private static final Map<String, Clan> clans = new HashMap<>();

    // Aggiungi un clan alla memoria
    public static void addClan(Clan clan) {
        if (clan == null || clan.getName() == null || clan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Clan or clan name cannot be null or empty.");
        }
        clans.put(clan.getName(), clan);
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

    // Aggiorna il nome di un clan in memoria
    public static boolean updateClanName(String oldName, String newName) {
        if (!hasClan(oldName)) {
            return false; // Il clan con il vecchio nome non esiste
        }
        if (newName == null || newName.trim().isEmpty() || hasClan(newName)) {
            throw new IllegalArgumentException("New clan name is invalid or already exists.");
        }
        // Recupera il clan e rimuove la vecchia entry
        Clan clan = clans.get(oldName);
        clans.remove(oldName);

        // Aggiorna il nome del clan e reinseriscilo con il nuovo nome
        clan.updateClanName(newName);
        clans.put(newName, clan);
        return true;
    }

    // Rimuovi un clan dalla memoria
    public static boolean deleteClan(String clanName) {
        if (clans.containsKey(clanName)) {
            clans.remove(clanName);
            return true;
        }
        return false;
    }

    // ===================================
    // Metodi per interagire con il Database
    // ===================================

    // Aggiungi un clan nel database MongoDB
    public static void addClanToDatabase(Clan clan) {
        if (clan == null || clan.getName() == null || clan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Clan or clan name cannot be null or empty.");
        }
        MongoDBManager.insertClan(clan); // Inserisce nel database
    }

    // Recupera un clan da MongoDB
    public static Clan getClanFromDatabase(String name) {
        return MongoDBManager.getClanByName(name); // Recupera dal database
    }

    // Rimuovi un clan da MongoDB
    public static void removeClanFromDatabase(String name) {
        MongoDBManager.deleteClan(name); // Rimuove dal database
    }

    // Aggiorna un clan nel database MongoDB
    public static void updateClanInDatabase(Clan clan) {
        MongoDBManager.updateClan(clan); // Aggiorna nel database
    }

    // Controlla se un clan esiste nel database MongoDB
    public static boolean hasClanInDatabase(String name) {
        return MongoDBManager.getClanByName(name) != null;
    }

    // Trova il clan di appartenenza di un utente (dal database)
    public static Clan getClanByUserFromDatabase(User user) {
        MongoCollection<Document> collection = MongoDBManager.getDatabase().getCollection("clans");

        // Crea il filtro per trovare un clan che contiene l'utente
        FindIterable<Document> iterable = collection.find(Filters.eq("members", user.getId())); // Assuming "members" is an array of user IDs

        MongoCursor<Document> cursor = iterable.iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            // Deserializzare il documento BSON in un oggetto Clan
            Clan clan = documentToClan(doc);
            if (clan.getListClanMember().contains(user)) { // Verifica se l'utente è nel clan
                return clan;
            }
        }
        return null; // Se non trovato
    }

    // Metodo per deserializzare un documento MongoDB in un oggetto Clan
    private static Clan documentToClan(Document document) {
        // Supponiamo che il tuo oggetto Clan abbia un costruttore che accetta un documento o metodi di impostazione per i campi
        Clan clan = new Clan();

        // Popola i campi del clan dalla documentazione MongoDB
        clan.setName(document.getString("name"));
        clan.setListClanMember((ArrayList<User>) document.getList("members", User.class));  // Assumendo che "members" sia una lista di oggetti User
        // Popola altri campi del Clan se necessario

        return clan;
    }

    // Cambia il nome del clan nel database
    public static boolean updateClanNameInDatabase(String oldName, String newName) {
        if (!hasClanInDatabase(oldName)) {
            return false; // Il clan con il vecchio nome non esiste
        }
        if (newName == null || newName.trim().isEmpty() || hasClanInDatabase(newName)) {
            throw new IllegalArgumentException("New clan name is invalid or already exists.");
        }
        // Recupera il clan dal database e aggiorna il nome
        Clan clan = MongoDBManager.getClanByName(oldName);
        clan.updateClanName(newName);
        MongoDBManager.updateClan(clan);
        return true;
    }

    // Elimina un clan dal database
    public static boolean deleteClanFromDatabase(String clanName) {
        if (MongoDBManager.getClanByName(clanName) != null) {
            MongoDBManager.deleteClan(clanName);
            return true;
        }
        return false;
    }
}
