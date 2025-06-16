package Lobby;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
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
import java.util.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
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
    private long EmbededMessageId;     // id of the embeded message with the join button
    private long announcementChannelId;  // nuovo campo

    // other stuff not on lobby stat
    private long PostId;
    private String completionMessageId;

    //check
    private boolean isCompleted = false;
    @Setter
    @Getter
    private boolean ownerEligibleToCreateNewLobby = false;
    private final Set<Long> partecipants = new HashSet<>();


    //stats
    private int lobbiesCreated = 0;
    private int lobbiesAnswered = 0;
    private int lobbiesCompleted = 0;

    private final Set<Long> blockedUsers = new HashSet<>();

    @Setter
    @Getter
    private boolean directLobby = false;

    @Setter
    @Getter
    private long allowedUserId;  // ID dell'utente autorizzato per lobby private



    public void incrementCreated() {
        lobbiesCreated++;
    }

    public void incrementAnswered() {
        lobbiesAnswered++;
    }

    public void incrementCompleted() {
        lobbiesCompleted++;
    }

    public void blockUser(long userId) {
        blockedUsers.add(userId);
    }

    public boolean isUserBlocked(long userId) {
        return blockedUsers.contains(userId);
    }

    public boolean isUserAllowed(long userId) {
        if (!this.isDirectLobby()) {
            return true; // se non è lobby privata, tutti possono entrare
        }
        return this.allowedUserId == userId;
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

        threadChannel.getManager().setArchived(true).setLocked(true).queue(
                success -> System.out.println("✅ Post archiviato e lockato con successo!"),
                error -> System.err.println("❌ Impossibile archiviare il post!")
        );
        incrementCompleted();
        System.out.println("✅ Lobby completed stat incremented for player: " + this.getDiscordId());
        LobbyManager.removeLobbyByCompletionMessageId(this.discordId);
        LobbyManager.removeLobby(this.getDiscordId());
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
            incrementCreated();
            System.out.println("✅ Lobby created incremented for player: " + this.getDiscordId());
            // Puoi salvare o usare messageId come vuoi
        });
    }

    public void sendLobbyAnnouncement(Guild guild, long postChannelId) {
        ForumChannel postChannel = guild.getForumChannelById(postChannelId);
        if (postChannel == null) {
            System.err.println("❌ Forum post channel not found!");
            return;
        }

        // Salvo l'ID del canale forum per poter aggiornare il messaggio in futuro
        this.announcementChannelId = postChannelId;

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

        EmbedBuilder publicEmbed = buildLobbyEmbed();

        Button joinButton = Button.success("join_lobby_" + discordId, "Join");

        postChannel.createForumPost(playerName + " Lobby", new MessageCreateBuilder()
                        .setEmbeds(publicEmbed.build())
                        .setActionRow(joinButton)
                        .build())
                .setTags(appliedTags)
                .queue(post -> {
                    // Recupera il thread creato
                    ThreadChannel threadChannel = post.getThreadChannel();

                    // Prendi fino a 100 messaggi per trovare il più vecchio (il primo del thread)
                    threadChannel.getHistory().retrievePast(100).queue(messages -> {
                        Message firstMessage = messages.stream()
                                .min(Comparator.comparing(Message::getTimeCreated))
                                .orElse(null);

                        if (firstMessage != null) {
                            this.setEmbededMessageId(firstMessage.getIdLong());
                        } else {
                            System.err.println("⚠️ Impossibile trovare il messaggio embedded nel thread");
                        }
                    });

                    System.out.println("📣 Forum lobby post created! Thread ID: " + threadChannel.getIdLong());
                    this.setPostId(threadChannel.getIdLong());

                    guild.createTextChannel(playerName.toLowerCase().replace(" ", "-") + "-lobby")
                            .setParent(guild.getCategoryById(1381025760231555077L)) // Categoria "lobby" corretta
                            .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                            .addPermissionOverride(guild.getMemberById(discordId),
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .queue(privateChannel -> {
                                this.privateChannelId = privateChannel.getIdLong();

                                // Primo messaggio classico
                                privateChannel.sendMessage("🔐 " + guild.getMember(UserSnowflake.fromId(discordId)).getUser().getAsMention() +
                                        ", this is your private lobby channel where you can accept or decline players.").queue();

                                // Secondo messaggio con il cancelletto e regole
                                StringBuilder secondMessage = new StringBuilder();
                                secondMessage.append("# ").append(game).append(" - ").append(platform).append("\n\n");

                                if (rules != null && !rules.isEmpty()) {
                                    secondMessage.append("**Rules:** ").append(rules).append("\n");
                                } else {
                                    secondMessage.append("**Rules:** None\n");
                                }

                                privateChannel.sendMessage(secondMessage.toString()).queue();

                                // Registra la lobby nel manager per accesso futuro
                                LobbyManager.addLobby(discordId, this);
                                this.getPartecipants().add(discordId);

                            });
                });
    }

    public EmbedBuilder buildLobbyEmbed() {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬ " + playerName + " Lobby ▬▬▬▬▬▬▬")
                .setColor(Color.decode("#1c0b2e"))
                .setDescription(
                        "**Player:** " + playerName + "\n" +
                                "**Game:** " + game + "\n" +
                                "**Platform:** " + platform + "\n" +
                                "**Region Target:** " + region + "\n" +
                                "**Wants to face:** " + skillLevel + " players\n\n" +
                                "Lobby is open - Click to join"
                )
                .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        return embed;
    }
    public void updateLobbyPost(Guild guild) {
        if (this.privateChannelId == 0 || this.PostId == 0) {
            System.err.println("❌ Cannot update post: missing privateChannelId or PostId");
            return;
        }

        ThreadChannel threadChannel = guild.getThreadChannelById(this.PostId);
        if (threadChannel == null) {
            System.err.println("❌ Thread channel not found for PostId: " + this.PostId);
            return;
        }

        threadChannel.retrieveMessageById(this.EmbededMessageId).queue(message -> {
            EmbedBuilder updatedEmbed = buildLobbyEmbed();

            Button joinButton = Button.success("join_lobby_" + discordId, "Join");

            message.editMessageEmbeds(updatedEmbed.build())
                    .setActionRow(joinButton)
                    .queue(
                            success -> System.out.println("✅ Forum post updated successfully"),
                            error -> System.err.println("❌ Failed to update forum post: " + error.getMessage())
                    );

        }, error -> {
            System.err.println("❌ Could not retrieve message to update: " + error.getMessage());
        });
    }

    public void printStats() {
        System.out.println("Discord ID: " + this.discordId);
        System.out.println("Lobbies Created: " + this.lobbiesCreated);
        System.out.println("Lobbies Answered: " + this.lobbiesAnswered);
        System.out.println("Lobbies Completed: " + this.lobbiesCompleted);
    }

    public void completeLobby() {
        if (!isCompleted) {
            isCompleted = true;
            incrementCompleted();

            // eventualmente fai altre cose
            System.out.println("✅ Lobby completed for player: " + this.discordId);
        }
    }


}
