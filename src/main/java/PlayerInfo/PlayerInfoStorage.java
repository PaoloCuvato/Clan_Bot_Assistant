package PlayerInfo;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
public class PlayerInfoStorage {
    // La mappa statica che tiene tutte le sessioni PlayerInfo (discordId -> PlayerInfo)
    private static final Map<Long, PlayerInfo> playerSessions = new ConcurrentHashMap<>();

    // Privato per evitare istanze multiple
    private PlayerInfoStorage() {}

    // Metodo per aggiungere o aggiornare un PlayerInfo nella mappa
    public static void addOrUpdatePlayerInfo(long discordId, PlayerInfo playerInfo) {
        playerSessions.put(discordId, playerInfo);

        if (PlayerInfoMongoDBManager.getPlayerInfoById(discordId) == null) {
            PlayerInfoMongoDBManager.insertPlayerInfo(playerInfo);
            System.out.println("🟢 PlayerInfo creato e salvato su MongoDB per: " + discordId);
        } else {
            PlayerInfoMongoDBManager.updatePlayerInfo(playerInfo);
            System.out.println("🔵 PlayerInfo aggiornato su MongoDB per: " + discordId);
        }

        // Stampa dettagli player info in console
        System.out.println("Dettagli PlayerInfo:\n" + playerInfo);
    }

    public static void removePlayerInfo(long discordId) {
        PlayerInfo removed = playerSessions.remove(discordId);
        PlayerInfoMongoDBManager.deletePlayerInfo(discordId);

        System.out.println("🗑️ PlayerInfo rimosso da memoria e MongoDB per: " + discordId);
        if (removed != null) {
            System.out.println("Dettagli PlayerInfo rimossi:\n" + removed);
        }
    }



    // Metodo per ottenere PlayerInfo dalla mappa
    public static PlayerInfo getPlayerInfo(long discordId) {
        return playerSessions.get(discordId);
    }

    // Metodo per rimuovere PlayerInfo dalla mappa
    /*
    public static void removePlayerInfo(long discordId) {
        playerSessions.remove(discordId);
    }


     */
    // Metodo per controllare se esiste PlayerInfo per un discordId
    public static boolean containsPlayerInfo(long discordId) {
        return playerSessions.containsKey(discordId);
    }

    // Metodo per ottenere la mappa completa (es. per salvarla su DB)
    public static Map<Long, PlayerInfo> getAllSessions() {
        return playerSessions;
    }

    // Metodo per caricare tutta la mappa (es. da DB all’avvio)
    public static void loadSessions(Map<Long, PlayerInfo> loadedSessions) {
        playerSessions.clear();
        playerSessions.putAll(loadedSessions);
    }
    // Nuovo metodo per stampare tutto il contenuto della mappa
    public static void printAllPlayers() {
        if (playerSessions.isEmpty()) {
            System.out.println("No player info stored.");
            return;
        }

        System.out.println("=== All Player Info ===");
        playerSessions.forEach((discordId, playerInfo) -> {
            System.out.println("Discord ID: " + discordId);
            System.out.println(playerInfo);
            System.out.println("-----------------------");
        });
    }
}



/*

IMPORTANTE
// Aggiungere un player
PlayerInfoStorage.addPlayerInfo(playerInfo);

// Ottenere un player
PlayerInfoStorage p = PlayerInfoStorage.getPlayerInfo(discordId);

// Ottenere tutti i players
Map<Long, PlayerInfoStorage> allPlayers = SessionStorage.getAllPlayers();

 */