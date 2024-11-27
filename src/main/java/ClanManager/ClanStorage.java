package ClanManager;

import lombok.Getter;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class ClanStorage {
    // Getter per tutti i clan
    @Getter
    private static final Map<String, Clan> clans = new HashMap<>();

    // Aggiungi un clan alla memoria
    public static void addClan(Clan clan) {
        if (clan == null || clan.getName() == null || clan.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Clan or clan name cannot be null or empty.");
        }
        clans.put(clan.getName(), clan);
    }

    // Recupera un clan per nome
    public static Clan getClan(String name) {
        return clans.get(name);
    }

    // Controlla se un clan esiste
    public static boolean hasClan(String name) {
        return clans.containsKey(name);
    }

    // Trova il clan di appartenenza di un utente
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

    // Aggiorna il nome di un clan
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

    public static boolean deleteClan(String clanName) {
        if (clans.containsKey(clanName)) {
            clans.remove(clanName);
            return true;
        }
        return false;
    }

}
