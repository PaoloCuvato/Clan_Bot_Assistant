package ClanManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-arguments constructor
@AllArgsConstructor // Generates a constructor with all fields
public class ClanMember extends ListenerAdapter {
    private String name;  // User's name
    private int wins = 0;     // Number of wins
    private int losses = 0;   // Number of losses
    private String clan;  // Clan membership (optional)

    // Method to add a win
    public void addWin() {
        this.wins++;
    }

    // Method to add a loss
    public void addLoss() {
        this.losses++;
    }
    public void reduceWin() {
        this.wins--;
    }

    // Method to add a loss
    public void reduceLoss() {
        this.losses--;
    }
}