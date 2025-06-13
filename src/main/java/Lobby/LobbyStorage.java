package Lobby;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class LobbyStorage extends ListenerAdapter {
    // Static map holding all lobby sessions (lobbyId -> Lobby)
    private static final Map<Long, Lobby> lobbies = new ConcurrentHashMap<>();

    // Private constructor to prevent instantiation
    private LobbyStorage() {}

    // Method to add or update a Lobby in the map
    public static void addOrUpdateLobby(Long lobbyId, Lobby lobby) {
        lobbies.put(lobbyId, lobby);
        System.out.println("✅ Lobby added/updated with ID: " + lobbyId);
        System.out.println("Lobby Details:\n" + lobby);
    }

    // Method to get a lobby by its ID
    public static Lobby getLobby(Long lobbyId) {
        return lobbies.get(lobbyId);
    }

    // Method to remove a lobby by ID
    public static void removeLobby(String lobbyId) {
        Lobby removed = lobbies.remove(lobbyId);
        System.out.println("🗑️ Lobby removed with ID: " + lobbyId);
        if (removed != null) {
            System.out.println("Removed Lobby Details:\n" + removed);
        }
    }

    // Method to check if a lobby exists
    public static boolean containsLobby(Long lobbyId) {
        return lobbies.containsKey(lobbyId);
    }

    // Method to get all lobbies
    public static Map<Long, Lobby> getAllLobbies() {
        return lobbies;
    }

    // Method to load lobbies into memory (e.g. from a file or previous state)
    public static void loadLobbies(Map<Long, Lobby> loadedLobbies) {
        lobbies.clear();
        lobbies.putAll(loadedLobbies);
        System.out.println("🔁 Lobbies loaded into memory.");
    }

    // Method to print all stored lobbies
    public static void printAllLobbies() {
        if (lobbies.isEmpty()) {
            System.out.println("No lobbies currently stored.");
            return;
        }

        System.out.println("=== All Active Lobbies ===");
        lobbies.forEach((lobbyId, lobby) -> {
            System.out.println("Lobby ID: " + lobbyId);
            System.out.println(lobby);
            System.out.println("-------------------------");
        });
    }
//    public static Lobby getLobbyByCreatorId(String creatorId) {
//        long creatorIdLong;
//        try {
//            creatorIdLong = Long.parseLong(creatorId);
//        } catch (NumberFormatException e) {
//            System.out.println("Errore di parsing ID: " + creatorId);
//            return null;
//        }
//
//        System.out.println("Cerco la lobby per creatorId: " + creatorIdLong);
//
//        return lobbies.values().stream()
//                .filter(lobby -> lobby.getDiscordId() == creatorIdLong)
//                .findFirst()
//                .orElse(null);
//    }
//

}
