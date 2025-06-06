package Lobby;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lobby {
    private long discordId;             // Discord user ID
    private String playerName;
    private String game;
    private String platform;
    private String region;
    private String skillLevel;          // "beginner", "intermediate", "advanced"
    private String connectionType;      // "ethernet" or "wifi"
    private String lobbyType;           // "ranked" or "casual"
    private String availability;        // Es: "Mon 18-20, Wed 21-23"
    private String rules;               // Regole opzionali
    private LocalDateTime createdAt;

    private int lobbiesCreated = 0;
    private int lobbiesAnswered = 0;
    private int lobbiesCompleted = 0;

    public void incrementCreated() {
        lobbiesCreated++;
    }

    public void incrementAnswered() {
        lobbiesAnswered++;
    }

    public void incrementCompleted() {
        lobbiesCompleted++;
    }
}
