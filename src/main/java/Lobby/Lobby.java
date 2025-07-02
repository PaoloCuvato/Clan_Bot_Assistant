package Lobby;

import Config.Config;
import Stat.PlayerStatMongoDBManager;
import Stat.PlayerStats;
import Stat.PlayerStatsManager;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Data

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
    private int maxPartecipants = 2;
    private String Fps;  // nuovo campo

    // other stuff not on lobby stat
    private long PostId;
    private String completionMessageId;

    //check
    private boolean isCompleted = false;
    private boolean ownerEligibleToCreateNewLobby = false;
    private boolean directLobby = false; // if the lobby is direct or not
    private final Set<Long> partecipants = new HashSet<>();

    //stats
    private final Set<Long> blockedUsers = new HashSet<>();
    private long allowedUserId;  // ID dell'utente autorizzato per lobby privat
    private Config c = new Config();

    // this  method will set up the max people on the lobby
    public void checkMaxpartecipants() {

        if (this.lobbyType.equals("Player Match")) {
            this.maxPartecipants = 2;

        } else if (this.lobbyType.equals("Ranked")) {
            this.maxPartecipants = 2;

        } else if (this.lobbyType.equals("Endless")) {
            this.maxPartecipants = 8;

        } else if (this.lobbyType.equals("Tournaments")) {
            this.maxPartecipants = 8;

        }
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
    public void incompleteLobby(Guild guild) {
        if (this.isCompleted) {
            System.out.println("⚠️ Cannot mark an already completed lobby as incomplete.");
            return;
        }

        // Mark lobby as incomplete
        this.isCompleted = false;

        PlayerStatsManager pm = PlayerStatsManager.getInstance();

        // Update creator stats
        PlayerStats creatorStats = pm.getPlayerStats(this.discordId);
        if (creatorStats == null) {
            creatorStats = new PlayerStats();
            creatorStats.setDiscordId(this.discordId);
            pm.addOrUpdatePlayerStats(creatorStats);
        }

        if (this.directLobby) {
            // SOLO STATS DIRECT
            creatorStats.incrementLobbiesIncompleteDirect();
        } else {
            // STATS GENERAL
            creatorStats.incrementLobbiesIncompleteGeneral();

            // ARCHIVIA THREAD FORUM
            ThreadChannel threadChannel = guild.getThreadChannels().stream()
                    .filter(thread -> thread.getIdLong() == this.PostId)
                    .findFirst()
                    .orElse(null);

            if (threadChannel != null) {
                threadChannel.getManager().setArchived(true).setLocked(true).queue(
                        success -> System.out.println("✅ Post archived and locked (incomplete lobby)."),
                        error -> System.err.println("❌ Unable to archive the post.")
                );
            } else {
                System.err.println("❌ Forum thread not found.");
            }
        }
        PlayerStatMongoDBManager.updatePlayerStats(creatorStats);

        // Remove lobby for creator
        LobbyManager.removeLobby(this.discordId);

        // Update and remove lobby for each participant (excluding creator)
        for (Long participantId : partecipants) {
            if (!participantId.equals(this.discordId)) {
                PlayerStats participantStats = pm.getPlayerStats(participantId);
                if (participantStats == null) {
                    participantStats = new PlayerStats();
                    participantStats.setDiscordId(participantId);
                    pm.addOrUpdatePlayerStats(participantStats);
                }

                if (this.directLobby) {
                    participantStats.incrementLobbiesIncompleteDirect();
                } else {
                    participantStats.incrementLobbiesIncompleteGeneral();
                }
                PlayerStatMongoDBManager.updatePlayerStats(participantStats);

                LobbyManager.removeLobby(participantId);
            }
        }

        partecipants.clear();

        System.out.println("✅ Lobby marked as incomplete and removed for all players in lobby: " + this.discordId);
    }

    public void archivePost(Guild guild) {
        PlayerStatsManager pm = PlayerStatsManager.getInstance();

        // Stats host
        PlayerStats hostStats = pm.getPlayerStats(discordId);
        if (hostStats == null) {
            hostStats = new PlayerStats();
            hostStats.setDiscordId(discordId);
            pm.addOrUpdatePlayerStats(hostStats);
        }

        if (this.directLobby) {
            // STATISTICHE DIRECT
            hostStats.incrementLobbiesCompletedDirect();

            GuildChannel channel = guild.getGuildChannelById(this.privateChannelId);
            if (channel != null && channel instanceof TextChannel textChannel) {
                for (Long participantId : new ArrayList<>(partecipants)) { // copia lista per evitare ConcurrentModification
                    if (!participantId.equals(this.discordId)) {
                        Member member = guild.getMemberById(participantId);
                        if (member != null) {
                            // Rimuovi permessi
                            textChannel.getPermissionOverride(member).getManager()
                                    .deny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                                    .queue(
                                            success -> System.out.println("✅ Rimosso permessi a " + member.getEffectiveName()),
                                            error -> System.err.println("❌ Impossibile rimuovere permessi a " + participantId)
                                    );

                            // Rimuovi stats
                            PlayerStats participantStats = pm.getPlayerStats(participantId);
                            if (participantStats == null) {
                                participantStats = new PlayerStats();
                                participantStats.setDiscordId(participantId);
                                pm.addOrUpdatePlayerStats(participantStats);
                            }
                            participantStats.incrementLobbiesCompletedDirect();
                            PlayerStatMongoDBManager.updatePlayerStats(participantStats);

                            // Rimuovi dalla lobby
                            LobbyManager.removeLobby(participantId);
                            partecipants.remove(participantId);
                        }
                    }
                }
            } else {
                System.err.println("❌ Canale privato non trovato o non è un TextChannel.");
            }

            // Rimuovi lobby host
            LobbyManager.removeLobby(this.discordId);
            PlayerStatMongoDBManager.updatePlayerStats(hostStats);

        } else {
            // ARCHIVIA THREAD
            ThreadChannel threadChannel = guild.getThreadChannels().stream()
                    .filter(thread -> thread.getIdLong() == this.PostId)
                    .findFirst()
                    .orElse(null);

            if (threadChannel != null) {
                threadChannel.getManager().setArchived(true).setLocked(true).queue(
                        success -> System.out.println("✅ Post archiviato e bloccato."),
                        error -> System.err.println("❌ Impossibile archiviare il post.")
                );
            } else {
                System.err.println("❌ Forum thread non trovato.");
            }

            // STATISTICHE GENERAL
            hostStats.incrementLobbiesCompletedGeneral();

            for (Long participantId : partecipants) {
                if (!participantId.equals(discordId)) {
                    PlayerStats participantStats = pm.getPlayerStats(participantId);
                    if (participantStats == null) {
                        participantStats = new PlayerStats();
                        participantStats.setDiscordId(participantId);
                        pm.addOrUpdatePlayerStats(participantStats);
                    }
                    participantStats.incrementLobbiesCompletedGeneral();
                    PlayerStatMongoDBManager.updatePlayerStats(participantStats);
                    LobbyManager.removeLobby(participantId);
                }
            }

            LobbyManager.removeLobbyByCompletionMessageId(this.discordId);
            LobbyManager.removeLobby(this.discordId);
            PlayerStatMongoDBManager.updatePlayerStats(hostStats);
        }
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
                                " * **Post URL:** " + "<#"+PostId+">"+ " | " + PostId + "\n" +
                                " * **Created At:** " + creationTimeFormatted + "\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                )
                .setColor(Color.decode("#1c0b2e"))
                .setTimestamp(Instant.now());

        // Invia l'embed e ottiene l'ID del messaggio
        logChannel.sendMessageEmbeds(eb.build()).queue(message -> {
            long messageId = message.getIdLong();
            System.out.println("✅ Log sent! Message ID: " + messageId);
            //  incrementCreated();
            System.out.println("✅ Lobby created incremented for player: " + this.getDiscordId());
            // Puoi salvare o usare messageId come vuoi
        });
    }

    public void sendLobbyAnnouncement(Guild guild, long postChannelId, Runnable onComplete) {
        ForumChannel postChannel = guild.getForumChannelById(postChannelId);
        if (postChannel == null) {
            System.err.println("❌ Forum post channel not found!");
            return;
        }

        this.announcementChannelId = postChannelId;

        List<ForumTag> appliedTags = new ArrayList<>();
        for (ForumTag tag : postChannel.getAvailableTags()) {
            if (tag.getName().equalsIgnoreCase("Open")) {
                appliedTags.add(tag);
            }
            if (tag.getName().equalsIgnoreCase(skillLevel)) {
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
                    ThreadChannel threadChannel = post.getThreadChannel();
                    this.setPostId(threadChannel.getIdLong());

                    // Recupera fino a 100 messaggi per trovare il primo
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

                    guild.createTextChannel(playerName.toLowerCase().replace(" ", "-") + "-lobby")
                           // .setParent(guild.getCategoryById(1381025760231555077L))
                             .setParent(guild.getCategoryById(c.getLobbyCategory()))

                            .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                            .addPermissionOverride(guild.getMemberById(discordId),
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .queue(privateChannel -> {
                                this.privateChannelId = privateChannel.getIdLong();

                                privateChannel.sendMessage("🔐 " + guild.getMember(UserSnowflake.fromId(discordId)).getUser().getAsMention() +
                                        ", this is your private lobby channel where you can accept or decline players.").queue();

                                StringBuilder secondMessage = new StringBuilder();
                                secondMessage.append("### ").append(platform).append(" - ").append(game).append("\n\n");

                                if (rules != null && !rules.isEmpty()) {
                                    secondMessage.append("**Rules:** ").append(rules).append("\n");
                                } else {
                                    secondMessage.append("**Rules:** None\n");
                                }

                                privateChannel.sendMessage(secondMessage.toString()).queue();

                                LobbyManager.addLobby(discordId, this);
                                this.getPartecipants().add(discordId);

                                // ✅ Callback finale eseguita SOLO dopo che tutto è stato creato
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            });
                });
    }

    public void sendDirectCreationLobbyLog(Guild guild, long logChannelId, long categoryId) {
        TextChannel logChannel = guild.getTextChannelById(logChannelId);
        if (logChannel == null) {
            System.err.println("❌ Log channel not found with ID: " + logChannelId);
            return;
        }

        Category category = guild.getCategoryById(categoryId);
        if (category == null) {
            System.err.println("❌ Category not found with ID: " + categoryId);
            return;
        }

        Member creator = guild.getMemberById(discordId);
        if (creator == null) {
            System.err.println("❌ Creator member not found in guild");
            return;
        }

        // (1) Creazione del canale privato
        guild.createTextChannel("private-lobby-" + this.playerName, category)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride(creator, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                .queue(privateChannel -> {
                    this.privateChannelId = privateChannel.getIdLong();

                    // (2) Messaggio di benvenuto
                    privateChannel.sendMessageFormat(
                            "🔐 %s, this is your private lobby channel where you can accept or decline players.\nUse `/add @user` to invite someone.",
                            creator.getAsMention()
                    ).queue();

                    // (3) Info lobby
                    String info = String.format("%s - %s\nRules: %s\nAvailability: %s",
                            this.game,
                            this.platform,
                            (this.rules == null || this.rules.isEmpty()) ? "None" : this.rules,
                            (this.availability == null || this.availability.isEmpty()) ? "N/A" : this.availability
                    );
                    privateChannel.sendMessage(info).queue();

                    // (4) Embed di log con il tuo template
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String creationTimeFormatted = createdAt != null ? createdAt.format(formatter) : "N/A";

                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("▬▬▬▬▬▬▬ Lobby Created ▬▬▬▬▬▬▬")
                            .setDescription(
                                    "**A new lobby has been created!**\n" +
                                            "** # Lobby: ** Info About The lobby\n" +
                                            " * **Discord ID:** "     + discordId   + "\n" +
                                            " * **Player Name:** "     + playerName  + "\n" +
                                            " * **Game:** "            + game        + "\n" +
                                            " * **Platform:** "        + platform    + "\n" +
                                            " * **Region:** "          + region      + "\n" +
                                            " * **Skill Level:** "     + skillLevel  + "\n" +
                                            " * **Connection:** "      + connectionType + "\n" +
                                            " * **Lobby Type:** "      + lobbyType   + "\n" +
                                            " * **Availability:** "    + availability + "\n" +
                                            " * **Rules:** "           + (rules != null && !rules.isEmpty() ? rules : "N/A") + "\n" +
                                            " * **Created At:** "      + creationTimeFormatted + "\n\n" +
                                            "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                            )
                            .setColor(Color.decode("#1c0b2e"))
                            .setTimestamp(Instant.now());

                    logChannel.sendMessageEmbeds(eb.build()).queue(message -> {
                        long messageId = message.getIdLong();
                        System.out.println("✅ Log sent! Message ID: " + messageId);
                        // Statistica “create”
                        System.out.println("✅ Lobby created incremented for player: " + discordId);
                    });

                    // (5) Registra la lobby e i partecipanti
                    LobbyManager.addLobby(discordId, this);
                    this.getPartecipants().add(discordId);

                    // (6) Conferma ephemerale all’utente se necessario
                    // (Se sei in un context di InteractionHook, invoca event.getHook().sendMessage(…))
                }, failure -> {
                    System.err.println("❌ Failed to create private channel.");
                    failure.printStackTrace();
                });
    }


    public EmbedBuilder buildLobbyEmbed() {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬▬▬▬ " + lobbyType + " Created ▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.decode("#1c0b2e"))
                .setDescription(
                      //  "# "+"<@"+this.getDiscordId()+">"+" | " +this.getDiscordId() + "\n"+
                        "### <@"+this.getDiscordId()+">"+" is available for the next "+ availability +" on " + platform + "\n"+
                                "* **In Game Name:** " + playerName + "\n" +
                                "* **Game Target:** " + game + "\n" +
                             //   "**Platform:** " + platform + "\n" +
                                "* **FPS Target:** " + Fps + "\n" +
                                "* **Skill Target:** " + skillLevel + " players\n" +
                                "* **Region Target:** " + region + "\n" +
                                "* **Connection Method Target: **" + connectionType + "\n" +
                                "* **Target Rules:** " + rules + "\n\n" +
                             //   "**Availability:** " + availability + "\n" +
                                "*** Lobby is open - Click to join ***"+
                                "\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"

                )
                .setTimestamp(Instant.now());
        return embed;
    }


    /*
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
     */
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



    public void completeLobby() {
        if (!isCompleted) {
            isCompleted = true;
            // incrementCompleted();

            // eventualmente fai altre cose
            System.out.println("✅ Lobby completed for player: " + this.discordId);
        }
    }

    public void callRefereeInPrivateChannel(Guild guild) {
        String adminRoleId =c.getRefereeRole();
       // String adminRoleId = "1015310375094337656";
        String message = "<@&" + adminRoleId + "> You have been requested to manage/oversee a dispute between the participants of this lobby.";

        TextChannel privateChannel = guild.getTextChannelById(this.privateChannelId);
        if (privateChannel == null) {
            System.err.println("❌ Private lobby channel not found (ID: " + this.privateChannelId + ")");
            return;
        }

        privateChannel.sendMessage(message).queue(
                success -> System.out.println("✅ Referee alert sent to private channel."),
                error -> System.err.println("❌ Failed to send referee alert: " + error.getMessage())
        );
    }


    public void incompleteAndCleanupLobby(Guild guild) {

        // Se la lobby è già completata, non si può marcare incompleta
        if (this.isCompleted) {
            System.out.println("⚠️ Cannot mark an already completed lobby as incomplete.");
            return;
        }

        // Marca la lobby come incompleta e aggiorna le statistiche
        this.incompleteLobby(guild);

        // Elimina il canale privato se esiste
        TextChannel privateChannel = guild.getTextChannelById(this.privateChannelId);
        if (privateChannel != null) {
            privateChannel.delete().queue(
                    success -> System.out.println("✅ Private channel deleted."),
                    error -> System.err.println("❌ Failed to delete private channel: " + error.getMessage())
            );
        } else {
            System.out.println("⚠️ Private channel not found or already deleted.");
        }
    }

}
