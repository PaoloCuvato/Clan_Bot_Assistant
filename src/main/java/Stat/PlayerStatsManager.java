package Stat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PlayerStatsManager extends ListenerAdapter {

    // Map containing Discord user IDs and their corresponding PlayerStats
    private Map<Long, PlayerStats> playerStatsMap = new HashMap<>();

    // --- Singleton implementation ---
    private static PlayerStatsManager instance;

    public static synchronized PlayerStatsManager getInstance() {
        if (instance == null) {
            instance = new PlayerStatsManager();
            log.info("PlayerStatsManager instance created");
        }
        return instance;
    }
    // --- End Singleton ---

    public void addOrUpdatePlayerStats(PlayerStats stats) {
        playerStatsMap.put(stats.getDiscordId(), stats);
        log.info("PlayerStats added/updated for user {}", stats.getDiscordId());
    }

    public PlayerStats getPlayerStats(long discordId) {
        PlayerStats stats = playerStatsMap.get(discordId);

        if (stats == null) {
            stats = PlayerStatMongoDBManager.getPlayerStatsById(discordId);

            if (stats != null) {
                playerStatsMap.put(discordId, stats);
            }
        }

        return stats;
    }


    public void removePlayerStats(long discordId) {
        playerStatsMap.remove(discordId);
        log.info("Removed PlayerStats for user {}", discordId);
    }

    public void printAllStats() {
        playerStatsMap.values().forEach(PlayerStats::printStats);
    }
}
