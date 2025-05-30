package PlayerInfo;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfo extends ListenerAdapter {

    private long discordId; // Discord User ID (retrieved using JDA 5)

    private String discordUsername; // Discord username (e.g. MyCoolName#0001)

    private String playerName; // Player's in-game name

    private String game; // Game name (Storm 3,4,evo)

    private String platform; // Gaming platform (e.g. PC, Xbox, PS5)

    private String connectionType; // e.g. "WiFi" or "Wired"

    private String inGamePlayTime; // the number of hour someone have on the game

    private String currentRegion; // Region they live in (e.g. EU, NA)

    private String targetRegion; // Region they want to play in (e.g. NA, JP)

    private String availablePlayTime; // e.g. "Evenings", "Weekends", or time range

    private String[] spokenLanguages; // Languages spoken (e.g. ["English", "French"])

    private int lobbyCounter; // Number of lobbies the player has joined

}
