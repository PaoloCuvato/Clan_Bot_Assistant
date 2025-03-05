package ClanManager;

import MongoDB.MongoDBManager;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    // Aggiungi un clan alla memoria
    public static void addClan(Clan clan) {
        if (clan == null || clan.getName() == null || clan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Clan or clan name cannot be null or empty.");
        }
        clans.put(clan.getName(), clan);
        System.out.println("Clan added to the map: " + clan.getName());  // Log per confermare l'aggiunta

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
//    public static boolean updateClanName(String oldName, String newName) {
//        if (!hasClan(oldName)) {
//            return false; // Il clan con il vecchio nome non esiste
//        }
//        if (newName == null || newName.trim().isEmpty() || hasClan(newName)) {
//            throw new IllegalArgumentException("New clan name is invalid or already exists.");
//        }
//        // Recupera il clan e rimuove la vecchia entry
//        Clan clan = clans.get(oldName);
//        clans.remove(oldName);
//
//        // Aggiorna il nome del clan e reinseriscilo con il nuovo nome
//        clan.updateClanName(newName);
//        clans.put(newName, clan);
//        return true;
//    }
    public static boolean updateClanName(String oldName, String newName) {
        if (!hasClan(oldName)) {
            System.out.println("❌ Error: Clan " + oldName + " does not exist in memory.");
            return false; // Clan not found in memory
        }
        if (hasClan(newName)) {
            System.out.println("❌ Error: A clan with the name " + newName + " already exists.");
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
            System.out.println("⚠️ Warning: Database update failed!");
            return false;
        }

        System.out.println("✅ Clan name updated successfully: " + oldName + " ➝ " + newName);
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
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

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
//    private static Clan documentToClan(Document document) {
//        // Supponiamo che il tuo oggetto Clan abbia un costruttore che accetta un documento o metodi di impostazione per i campi
//        Clan clan = new Clan();
//
//        // Popola i campi del clan dalla documentazione MongoDB
//        clan.setName(document.getString("name"));
//        clan.setListClanMember((ArrayList<User>) document.getList("members", User.class));  // Assumendo che "members" sia una lista di oggetti User
//        // Popola altri campi del Clan se necessario
//
//        return clan;
//    }

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




    public static boolean updateClanWinsInDatabase(String clanName, int newWins) {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Creiamo la query per trovare il clan
        Document query = new Document("name", clanName);

        // Creiamo l'update per modificare solo le vittorie
        Document update = new Document("$set", new Document("wins", newWins));

        try {
            if (collection.updateOne(query, update).getModifiedCount() > 0) {
                System.out.println("✅ Clan wins updated in MongoDB: " + clanName + " ➝ Wins: " + newWins);
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
                System.out.println("✅ Clan losses updated in MongoDB: " + clanName + " ➝ Losses: " + newLosses);
                return true;
            } else {
                System.out.println("❌ Error: Clan " + clanName + " not found in the database.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating clan losses in the database: " + e.getMessage());
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
            List<String> members = document.getList("members", String.class);  // Membri come ID stringa

            // Log del documento caricato per il debug
            System.out.println("Document loaded: " + document.toJson());

            // Converte la data di creazione in un oggetto LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM:dd:yyyy HH:mm:ss");
            LocalDateTime creationDate = LocalDateTime.parse(creationDateStr, formatter);

            // Crea il clan
            Clan clan = new Clan(name, members.toArray(), wins, losses);
            System.out.println("Clan created: " + clan);

            // Rimuove eventuali membri nulli o vuoti e elimina i duplicati
            if (members != null) {
                members = members.stream()
                        .filter(member -> member != null && !member.isEmpty())  // Filtro per eliminare nulli e vuoti
                        .distinct()  // Rimuove i duplicati
                        .collect(Collectors.toList());
            }

            // Aggiungi i membri al clan (convertendo gli ID in oggetti User)
            for (String memberId : members) {
                // Recupera il membro dalla guild tramite l'ID
                User user = getUserFromId(memberId);  // Usa il metodo che hai definito per recuperare il member
                if (user != null) {
                    System.out.println("Recuperato utente per l'ID: " + memberId);

                    // Controlla se l'utente è già nel clan
                    if (!clan.getMembersList().contains(user)) {  // Assicurati che il metodo getMembersList() restituisca i membri del clan
                        // Aggiungi l'utente al clan
                        clan.addUser(user);
                    } else {
                        System.out.println("L'utente con ID: " + memberId + " è già presente nel clan.");
                    }
                } else {
                    System.out.println("Utente non trovato per l'ID: " + memberId);
                }
            }

            return clan;
        } catch (Exception e) {
            System.out.println("Errore durante la conversione del documento in clan: " + e.getMessage());
        }
        return null;  // Restituisce null se qualcosa va storto
    }



//    // Metodo per convertire un documento MongoDB in un oggetto Clan
//    private static Clan documentToClan(Document document) {
//        try {
//            String name = document.getString("name");
//            int wins = document.getInteger("wins", 0);
//            int losses = document.getInteger("losses", 0);
//            String creationDateStr = document.getString("creationDate");
//            List<String> members = document.getList("members", String.class);
//
//            System.out.println("Document loaded: " + document.toJson());  // Log per visualizzare il documento
//
//            // Converte la data di creazione in un oggetto LocalDateTime
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM:dd:yyyy HH:mm:ss");
//            LocalDateTime creationDate = LocalDateTime.parse(creationDateStr, formatter);
//
//            // Crea il clan
//            Clan clan = new Clan(name, null, wins, losses);  // Usa un membro fittizio per il momento
//
//            // Aggiungi i membri al clan
//            for (String memberTag : members) {
//                User user = getUserFromName(memberTag);  // Recupera l'utente tramite il tag
//                if (user != null) {
//                    clan.addUser(user);
//                }
//            }
//
//            return clan;
//        } catch (Exception e) {
//            System.out.println("Errore durante la conversione del documento in clan: " + e.getMessage());
//        }
//        return null;  // Restituisce null se qualcosa va storto
//    }



    // Metodo per ottenere un utente da Discord basandosi sul nome
    public static User getUserFromName(String userName) {
        if (guild == null) {
            System.err.println("Guild is not initialized! Cannot find the user.");
            return null;
        }

        for (Member member : guild.getMembers()) {
            User user = member.getUser();
            if (user.getName().equalsIgnoreCase(userName)) {
                return user;  // Return the found user
            }
        }


        System.out.println("User not found in guild: " + userName);  // Log se l'utente non viene trovato
        return null;
    }

    public static User getUserFromTag(String tag) {
        if (guild == null) {
            System.err.println("Guild is not initialized! Cannot find the user.");
            return null;
        }

        // Scorre i membri della guild per trovare l'utente con il tag
        for (Member member : guild.getMembers()) {
            User user = member.getUser();
            if (user.getAsTag().equals(tag)) {  // Confronta il tag completo (username#1234)
                return user;  // Restituisce l'utente trovato
            }
        }

        // Se l'utente non è stato trovato
        System.out.println("User with tag " + tag + " not found.");
        return null;
    }




    public static void loadAllClansFromDatabase() {
        MongoCollection<Document> collection = getDatabase().getCollection("clans");

        // Verifica la connessione al database
        if (collection == null) {
            System.out.println("Errore: la collezione 'clans' non è stata trovata nel database.");
            return;
        }

        // Log per verificare il numero di documenti
        long count = collection.countDocuments();
        System.out.println("Totale documenti nella collezione clans: " + count);

        FindIterable<Document> documents = collection.find();

        // Verifica se ci sono documenti da caricare
        if (!documents.iterator().hasNext()) {
            System.out.println("Nessun documento trovato nella collezione 'clans'.");
        }

        for (Document document : documents) {
            Clan clan = documentToClan(document);
            if (clan != null) {
                addClan(clan);
                System.out.println("Clan caricato: " + clan.getName());  // Log quando il clan viene caricato
            }
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        this.event= event;
        this.guild = event.getGuild();
        System.out.println("Guild is ready, loading clans...");

        ClanStorage.loadAllClansFromDatabase();
        System.out.println("All clans loaded.");
    }

}
