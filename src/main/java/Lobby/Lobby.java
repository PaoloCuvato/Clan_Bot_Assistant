package Lobby;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lobby extends ListenerAdapter {
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
    private long privateChannelId;  // ID del canale privato creato per questa lobby
    private long PostId;


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

    public void archivePost(Guild guild) {
        // Ottieni il thread dalla `ThreadChannel`
        ThreadChannel threadChannel = guild.getThreadChannels().stream()
                .filter(thread -> thread.getIdLong() == this.PostId)
                .findFirst()
                .orElse(null);

        if (threadChannel == null) {
            System.err.println("❌ Thread del forum non trovato!");
            return;
        }

        threadChannel.getManager().setArchived(true).queue(
                success -> System.out.println("✅ Post archiviato con successo!"),
                error -> System.err.println("❌ Impossibile archiviare il post!")
        );
    }

    public void deletePost(Guild guild) {
        // Ottieni il thread dalla `ThreadChannel`
        ThreadChannel threadChannel = guild.getThreadChannels().stream()
                .filter(thread -> thread.getIdLong() == this.PostId)
                .findFirst()
                .orElse(null);

        if (threadChannel == null) {
            System.err.println("❌ Thread del forum non trovato!");
            return;
        }

        threadChannel.delete().queue(
                success -> System.out.println("✅ Post eliminato con successo!"),
                error -> System.err.println("❌ Impossibile eliminare il post!")
        );
    }

    public void sendLobbyLog(Guild guild, long logChannelId) {
        TextChannel logChannel = guild.getTextChannelById(logChannelId);

        if (logChannel == null) {
            System.err.println("❌ Log channel not found!");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String creationTimeFormatted = createdAt != null ? createdAt.format(formatter) : "N/A";

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬ Lobby Created ▬▬▬▬▬▬▬")
                .setDescription(
                        "**A new lobby has been created!**\n" +
                                "** # Lobby: **" +
                                "Info About The lobby\n"+
                                " * **Discord ID:** " + discordId + "\n" +
                                " * **Player Name:** " + playerName + "\n" +
                                " * **Game:** " + game + "\n" +
                                " * **Platform:** " + platform + "\n" +
                                " * **Region:** " + region + "\n" +
                                " * **Skill Level:** " + skillLevel + "\n" +
                                " * **Connection:** " + connectionType + "\n" +
                                " * **Lobby Type:** " + lobbyType + "\n" +
                                " * **Availability:** " + availability + "\n" +
                                " * **Rules:** " + (rules != null && !rules.isEmpty() ? rules : "N/A") + "\n" +
                                " * **Created At:** " + creationTimeFormatted + "\n" +

                                "** # Lobby Stats:**" +
                                "interaction that the user have with the lobby's\n"+
                                " * **Created:** " + lobbiesCreated + "\n" +
                                " * **Answered:** " + lobbiesAnswered + "\n" +
                                " * **Completed:** " + lobbiesCompleted + "\n" +

                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                )
                .setColor(Color.decode("#1c0b2e"))
                .setTimestamp(Instant.now());

        // Invia l'embed e ottiene l'ID del messaggio
        logChannel.sendMessageEmbeds(eb.build()).queue(message -> {
            long messageId = message.getIdLong();
            System.out.println("✅ Log sent! Message ID: " + messageId);
            // Puoi salvare o usare messageId come vuoi
        });
    }

    public void sendLobbyAnnouncement(Guild guild, long postChannelId) {
        ForumChannel postChannel = guild.getForumChannelById(postChannelId);
        if (postChannel == null) {
            System.err.println("❌ Forum post channel not found!");
            return;
        }

        // Trova i tag disponibili e seleziona quelli corretti
        List<ForumTag> appliedTags = new ArrayList<>();
        for (ForumTag tag : postChannel.getAvailableTags()) {
            if (tag.getName().equalsIgnoreCase("Opened")) {
                appliedTags.add(tag);
            }
            if (tag.getName().equalsIgnoreCase(skillLevel)) { // es: "Beginner", "Intermediate", "Advanced"
                appliedTags.add(tag);
            }
        }

        EmbedBuilder publicEmbed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬ " + playerName + " Lobby ▬▬▬▬▬▬▬")
                .setColor(Color.decode("#1c0b2e"))
                .setDescription(
                        "** Player:** " + playerName + "\n" +
                                "** Game: ** " + game + "\n" +
                                "** Platform: ** " + platform + "\n" +
                                "** Region Target: ** " + region + "\n" +
                                "** Wants to face: ** " + skillLevel + " players\n\n" +
                                "Lobby is open - Click to join"
                )
                .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        Button joinButton = Button.success("join_lobby_" + discordId, "Join");

        postChannel.createForumPost(playerName + " Lobby", new MessageCreateBuilder()
                        .setEmbeds(publicEmbed.build())
                        .setActionRow(joinButton)
                        .build())
                .setTags(appliedTags)
                .queue(post -> {
                    System.out.println("📣 Forum lobby post created! Thread ID: " + post.getThreadChannel().getIdLong());
                    this.setPostId(post.getThreadChannel().getIdLong());

                    guild.createTextChannel(playerName.toLowerCase().replace(" ", "-") + "-lobby")
                          //  .setParent(postChannel.getParentCategory())
                            .setParent(guild.getCategoryById(1381025760231555077L)) // Categoria "lobby" corretta
                            .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                            .addPermissionOverride(guild.getMemberById(discordId),
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .queue(privateChannel -> {
                                this.privateChannelId = privateChannel.getIdLong();
                                privateChannel.sendMessage("🔐 " + guild.getMember(UserSnowflake.fromId(discordId)).getUser().getAsMention() + ", this is your private lobby channel where you can accept or decline players.").queue();

                                // Registra la lobby nel manager per accesso futuro
                                LobbyManager.addLobby(discordId, this);
                            });
                });
    }


}
