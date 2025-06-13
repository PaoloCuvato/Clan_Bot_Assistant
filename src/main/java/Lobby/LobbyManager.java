package Lobby;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager {

    private static final Map<Long, Lobby> lobbies = new ConcurrentHashMap<>();

    // Nuova mappa per tracciare le lobby per completionMessageId
    private static final Map<String, Lobby> completionMessageLobbies = new ConcurrentHashMap<>();

    // Aggiunge una lobby
    public static void addLobby(long creatorId, Lobby lobby) {
        lobbies.put(creatorId, lobby);
    }

    // Recupera una lobby tramite creatorId
    public static Lobby getLobby(long creatorId) {
        return lobbies.get(creatorId);
    }

    // Rimuove una lobby tramite creatorId
    public static void removeLobby(long creatorId) {
        lobbies.remove(creatorId);
    }

    // Controlla se esiste una lobby per un creatorId
    public static boolean hasLobby(long creatorId) {
        return lobbies.containsKey(creatorId);
    }


    // Salva la lobby associata a un messaggio di completamento
    public static void saveLobbyCompletionMessage(Lobby lobby) {
        if (lobby.getCompletionMessageId() != null) {
            completionMessageLobbies.put(lobby.getCompletionMessageId(), lobby);
        }
    }

    // Recupera la lobby tramite messageId del messaggio di completamento
    public static Lobby getLobbyByCompletionMessageId(String messageId) {
        return completionMessageLobbies.get(messageId);
    }

    // Rimuove la lobby tramite messageId del messaggio di completamento
    public static void removeLobbyByCompletionMessageId(String messageId) {
        completionMessageLobbies.remove(messageId);
    }
}
