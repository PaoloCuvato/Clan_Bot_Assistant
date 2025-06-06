package Lobby;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class LobbyStorage {
    // Static map holding all lobby sessions (lobbyId -> Lobby)
    private static final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();

    // Private constructor to prevent instantiation
    private LobbyStorage() {}

    // Method to add or update a Lobby in the map
    public static void addOrUpdateLobby(String lobbyId, Lobby lobby) {
        lobbies.put(lobbyId, lobby);
        System.out.println("✅ Lobby added/updated with ID: " + lobbyId);
        System.out.println("Lobby Details:\n" + lobby);
    }

    // Method to get a lobby by its ID
    public static Lobby getLobby(String lobbyId) {
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
    public static boolean containsLobby(String lobbyId) {
        return lobbies.containsKey(lobbyId);
    }

    // Method to get all lobbies
    public static Map<String, Lobby> getAllLobbies() {
        return lobbies;
    }

    // Method to load lobbies into memory (e.g. from a file or previous state)
    public static void loadLobbies(Map<String, Lobby> loadedLobbies) {
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
}
