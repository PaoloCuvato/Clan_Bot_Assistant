package PlayerInfo;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerInfoFileManager {

    private static final String FILE_NAME = "playerinfolist.txt";

    public static void savePlayerInfoList(Map<Long, PlayerInfo> playerInfoMap) {
        File file = new File(FILE_NAME);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<Long, PlayerInfo> entry : playerInfoMap.entrySet()) {
                long discordId = entry.getKey();
                String nickname = entry.getValue().getDiscordUsername();
                writer.write(discordId + "-" + nickname);
                writer.newLine();
            }
            System.out.println("✅ Player info list saved to " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("❌ Error saving player info list: " + e.getMessage());
        }
    }

    public static Map<Long, String> loadPlayerInfoList() {
        Map<Long, String> playerInfoMap = new HashMap<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("ℹ️ File " + FILE_NAME + " non trovato, salto il caricamento.");
            return playerInfoMap;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("-", 2);
                if (parts.length == 2) {
                    try {
                        long discordId = Long.parseLong(parts[0]);
                        String nickname = parts[1];
                        playerInfoMap.put(discordId, nickname);
                    } catch (NumberFormatException e) {
                        System.err.println("❌ Linea malformata: " + line);
                    }
                } else {
                    System.err.println("❌ Linea malformata: " + line);
                }
            }
            System.out.println("✅ Player info list loaded from " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("❌ Error loading player info list: " + e.getMessage());
        }

        return playerInfoMap;
    }
}
