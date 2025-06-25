package PlayerInfo;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import java.time.Instant;


import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerInfo extends ListenerAdapter {

    private long discordId; // Discord User ID (retrieved using JDA 5)

    private String discordUsername; // Discord username (e.g. MyCoolName#0001)

    private String playerName; // Player's in-game name

    private String[] game  = new String[0];; // Game name (Storm 3,4,evo)

    private String[] platforms  = new String[0];; // Gaming platform (e.g. PC, Xbox, PS5)

    private String connectionType; // e.g. "WiFi" or "Wired"

    private String[] spokenLanguages = new String[0]; ; // Languages spoken (e.g. ["English", "French"])

    private int lobbyCounter; // Number of lobbies the player has joined

    private String currentRegion; // Region they live in (e.g. EU, NA)

    private String  mostPlayedGame;

    private String  skillLevel;

    //  private String inGamePlayTime; // the number of hour someone have on the game

    //   private String targetRegion; // Region they want to play in (e.g. NA, JP)

    //   private String availablePlayTime; // e.g. "Evenings", "Weekends", or time range
    public void sendPlayerInfoLog(Guild guild) {
        long logChannelId = 1377959042663714846L; // Sostituisci con l'ID corretto del tuo canale
        TextChannel logChannel = guild.getTextChannelById(logChannelId);

        if (logChannel == null) {
            System.err.println("❌ Log channel not found!");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String creationTime = LocalDateTime.now().format(formatter);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ Ninja Card Created ▬▬▬▬▬▬ ")
                .setDescription(
                        "**A new Ninja Card has been created!**\n" +
                                "** # User Info:** " +
                                "These are the stat about the user\n"+
                                " * **Username:** " + this.discordUsername + "\n" +
                                " * **Discord ID:** " + this.discordId + "\n" +
                                "** # Player Info:** " +
                                " These are all the stat about the player\n" +
                                " * **Platform:** " + String.join(",",this.platforms) + "\n" +
                                " * **Game:** " + String.join(",",this.game) + "\n" +
                                " * **Player Name:** " + this.playerName + "\n" +
                                " * **Connection:** " + this.connectionType + "\n" +
                      //          " * **Hours Played:** " + this.inGamePlayTime + "\n" +
                                " * **Current Region:** " + this.currentRegion + "\n" +
                      //          " * **Target Region:** " + this.targetRegion + "\n" +
                                " * **Languages:** " + String.join(", ", this.spokenLanguages) + "\n" +
                      //          " * **Availability:** " + this.availablePlayTime + "\n" +
                                " * **Lobbies Joined:** " + this.lobbyCounter + "\n" +
                                " * **Created At:** " + creationTime + "\n\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                )
                .setColor(Color.decode( "#1c0b2e"))

                .setTimestamp(Instant.now()); // 👈 footer automatico con orario


        logChannel.sendMessageEmbeds(eb.build()).queue();
    }

    public void sendPlayerInfo(Guild guild) {
        long logChannelId = 1377959042663714846L; // Sostituisci con l'ID corretto del tuo canale
        TextChannel logChannel = guild.getTextChannelById(logChannelId);

        if (logChannel == null) {
            System.err.println("❌ Log channel not found!");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String creationTime = LocalDateTime.now().format(formatter);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ Ninja Card Created ▬▬▬▬▬▬ ")
                .setDescription(
                        "**A new Ninja Card has been created!**\n\n" +
                                "**User Info:**\n" +
                                " * **Discord ID:** " + this.discordId + "\n" +
                                " * **Username:** " + this.discordUsername + "\n\n" +
                                "** # Player Info:**\n" +
                                " * **Game:** " + this.game + "\n" +
                                " * **Platform:** " + String.join(",",this.platforms) + "\n" +
                                " * **Player Name:** " + this.playerName + "\n" +
                                " * **Connection:** " + this.connectionType + "\n" +
                           //     " * **Hours Played:**" + this.inGamePlayTime + "\n" +
                                " * **Current Region:** " + this.currentRegion + "\n" +
                           //     " * **Target Region:** " + this.targetRegion + "\n" +
                                " * **Languages:** " + String.join(", ", this.spokenLanguages) + "\n" +
                          //      " * **Availability:** " + this.availablePlayTime + "\n" +
                                " *  **Lobbies Joined:** " + this.lobbyCounter + "\n\n" +
                                "**Created At:** " + creationTime + "\n\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                )
                .setColor(Color.WHITE)

                .setTimestamp(Instant.now()); // 👈 footer automatico con orario


        logChannel.sendMessageEmbeds(eb.build()).queue();
    }
}
