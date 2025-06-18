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

    public void addOrUpdatePlayerStats(PlayerStats stats) {
        playerStatsMap.put(stats.getDiscordId(), stats);
        log.info("PlayerStats added/updated for user {}", stats.getDiscordId());
    }

    public PlayerStats getPlayerStats(long discordId) {
        return playerStatsMap.get(discordId);
    }

    public void removePlayerStats(long discordId) {
        playerStatsMap.remove(discordId);
        log.info("Removed PlayerStats for user {}", discordId);
    }

    public void printAllStats() {
        playerStatsMap.values().forEach(PlayerStats::printStats);
    }
}
