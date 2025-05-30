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
    }

    // Metodo per ottenere PlayerInfo dalla mappa
    public static PlayerInfo getPlayerInfo(long discordId) {
        return playerSessions.get(discordId);
    }

    // Metodo per rimuovere PlayerInfo dalla mappa
    public static void removePlayerInfo(long discordId) {
        playerSessions.remove(discordId);
    }

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