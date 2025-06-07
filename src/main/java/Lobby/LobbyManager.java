package Lobby;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager {
    private static final Map<Long, Lobby> lobbies = new ConcurrentHashMap<>();

    public static void addLobby(long creatorId, Lobby lobby) {
        lobbies.put(creatorId, lobby);
    }

    public static Lobby getLobby(long creatorId) {
        return lobbies.get(creatorId);
    }

    public static void removeLobby(long creatorId) {
        lobbies.remove(creatorId);
    }

    public static boolean hasLobby(long creatorId) {
        return lobbies.containsKey(creatorId);
    }
}
