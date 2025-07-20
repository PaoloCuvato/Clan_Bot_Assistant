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
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.Permission;

import java.io.InputStream;
import java.util.*;


import java.awt.*;
import java.io.InputStream;
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
    private int durationMinutes= 180;

    // other stuff not on lobby stat
    private long PostId;
    private String completionMessageId;

    //check
    private boolean isCompleted = false;
    private boolean ownerEligibleToCreateNewLobby = false;
    private boolean directLobby = false; // if the lobby is direct or not
    private final Set<Long> partecipants = new HashSet<>();

    //maps
    private static final Map<String, String> PLATFORM_TAG_IDS = Map.of(
            "PS5", "1389940855149695077",
            "PS4", "1389940855149695077",
            "PS3", "1389940855149695077",

            "Xbox Series X", "1389940871989956618",
            "Xbox Series S", "1389940871989956618",
            "Xbox 360", "1389940871989956618",

            "PC", "1389940833939095562",
            "RPCS3", "1389997424440901762",

            "Nintendo Switch 1", "1389940892961341460",
            "Nintendo Switch 2", "1389940892961341460"

    );
    private static final Map<String, Long> PLATFORM_EMOJI_IDS = Map.of(
            "PS5", 123456789012345678L,
            "PS4", 123456789012345678L,
            "PS3", 123456789012345678L,

            "Xbox Series X", 223456789012345678L,
            "Xbox Series S", 223456789012345678L,
            "Xbox 360", 223456789012345678L,

            "PC", 1316691292930965576L,
            "RPCS3", 423456789012345678L,

            "Nintendo Switch 1", 523456789012345678L,
            "Nintendo Switch 2", 523456789012345678L
    );


    private static final Map<String, String> GAME_TAG_IDS = Map.of(
            "NSUNS", "1389612741211324567",
            "NSUNS2", "1389612772303831091",
            "NSUNSG", "1389612790511046797",

            "NSUNS3", "1389612818701221928",
            "NSUNSFB", "1389612857313853440",
            "NSUNSR", "1389612876217716828",
            "NSUNS4", "1389612902415204383",

            "NSUNSRTB", "1389613031729795213",
            "NXBUNSC", "1390055655053856971",
            "NSUNSE", "1389613099081928845"

    );

    private static final Map<String, String> PLATFORM_TAG_IDS_CLAN = Map.of(
            "PS5", "1392110160675078225",
            "PS4", "1392110160675078225",
            "PS3", "1392110160675078225",

            "Xbox Series X", "1392110172444430346",
            "Xbox Series S", "1392110172444430346",
            "Xbox 360", "1392110172444430346",

            "PC", "1392110116139958386",
            "RPCS3", "1392110274596835369",

            "Nintendo Switch 1", "1392110240555864136",
            "Nintendo Switch 2", "1392110240555864136"

    );

    private static final Map<String, String> GAME_TAG_IDS_CLAN = Map.of(
            "NSUNS", "1392109606381293678",
            "NSUNS2", "1392109686228258961",
            "NSUNSG", "1392109726485053541",

            "NSUNS3", "1392109775335981076",
            "NSUNSFB", "1392109814795997235",
            "NSUNSR", "1392109866663022693",
            "NSUNS4", "1392109910707277896",

            "NSUNSRTB", "1392109955917549608",
            "NXBUNSC", "1392110324492009552",
            "NSUNSE", "1392110052596387871"

    );

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

        } else if (this.lobbyType.equals("Tournament")) {
            this.maxPartecipants = 8;

        }else if (this.lobbyType.equals("Clan Battle")) {
            this.maxPartecipants = 8;
        }
    }

    public static String getPlatformTagId(String platformName) {
        return PLATFORM_TAG_IDS.get(platformName);
    }

    public void applyPlatformTag(ThreadChannel threadChannel, String platformName) {
        if (threadChannel == null) {
            System.err.println("❌ ThreadChannel is null.");
            return;
        }

        String platformTagId = PLATFORM_TAG_IDS.get(platformName);
        if (platformTagId == null) {
            System.err.println("❌ Invalid platform name specified: " + platformName);
            return;
        }

        ForumChannel forum = (ForumChannel) threadChannel.getParentChannel();
        if (forum == null) {
            System.err.println("❌ Thread is not in a forum channel.");
            return;
        }

        ForumTag platformTag = forum.getAvailableTags().stream()
                .filter(tag -> tag.getId().equals(platformTagId))
                .findFirst()
                .orElse(null);

        if (platformTag == null) {
            System.err.println("❌ Platform tag '" + platformName + "' not found in forum.");
            return;
        }

        threadChannel.getManager()
                .setAppliedTags(Collections.singletonList(platformTag))
                .queue(
                        success -> System.out.println("✅ Applied platform tag '" + platformName + "' successfully."),
                        error -> System.err.println("❌ Failed to apply platform tag: " + error.getMessage())
                );
    }

    public void applyGameTag(ThreadChannel threadChannel, String gameKey) {
        if (threadChannel == null) {
            System.err.println("❌ ThreadChannel is null.");
            return;
        }

        String tagId = GAME_TAG_IDS.get(gameKey);
        if (tagId == null) {
            System.err.println("❌ Game key '" + gameKey + "' not recognized.");
            return;
        }

        ForumChannel forum = (ForumChannel) threadChannel.getParentChannel();
        if (forum == null) {
            System.err.println("❌ Thread is not in a forum channel.");
            return;
        }

        ForumTag gameTag = forum.getAvailableTags().stream()
                .filter(tag -> tag.getId().equals(tagId))
                .findFirst()
                .orElse(null);

        if (gameTag == null) {
            System.err.println("❌ Game tag with ID '" + tagId + "' not found.");
            return;
        }

        threadChannel.getManager()
                .setAppliedTags(Collections.singletonList(gameTag))
                .queue(
                        success -> System.out.println("✅ Game tag '" + gameKey + "' applied successfully."),
                        error -> System.err.println("❌ Failed to apply game tag: " + error.getMessage())
                );
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
            creatorStats.incrementLobbiesIncompleteDirect();
        } else {
            creatorStats.incrementLobbiesIncompleteGeneral();

            // ARCHIVIA THREAD FORUM
            ThreadChannel threadChannel = guild.getThreadChannelById(this.PostId);

            if (threadChannel != null) {
                // Invia embed nel thread
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📌 Lobby Status: Incomplete")
                        .setDescription("This lobby has been automatically marked as **incomplete** due to inactivity.")
                        .setColor(Color.decode("#5c5470")) // viola spento
                        .setFooter("Forum post archived and locked");

                threadChannel.sendMessageEmbeds(embed.build()).queue();

                // 🔖 Aggiunta tag con ID 1389612412470038649
                ForumTagSnowflake tagToAdd = ForumTagSnowflake.fromId(1389612412470038649L);
                List<ForumTagSnowflake> currentTags = new ArrayList<>(threadChannel.getAppliedTags());

                if (!currentTags.contains(tagToAdd)) {
                    currentTags.add(tagToAdd);
                }

                threadChannel.getManager()
                        .setAppliedTags(currentTags)
                        .queue(
                                success -> System.out.println("🏷 Tag 'Incompleta' aggiunto mantenendo i precedenti."),
                                error -> System.err.println("❌ Errore nell'aggiornare i tag.")
                        );

                // Archivio e blocco il thread
                if (threadChannel.isArchived()) {
                    threadChannel.getManager()
                            .setArchived(false)
                            .queue(unarchived -> {
                                threadChannel.getManager()
                                        .setArchived(true)
                                        .setLocked(true)
                                        .queue(
                                                success -> System.out.println("✅ Thread unarchived, then archived and locked."),
                                                error -> System.err.println("❌ Failed to archive/lock thread.")
                                        );
                            }, err -> System.err.println("❌ Failed to unarchive thread."));
                } else {
                    threadChannel.getManager()
                            .setArchived(true)
                            .setLocked(true)
                            .queue(
                                    success -> System.out.println("✅ Thread archived and locked."),
                                    error -> System.err.println("❌ Failed to archive/lock thread.")
                            );
                }
            } else {
                System.err.println("❌ Forum thread not found for ID: " + this.PostId);
            }
        }

        PlayerStatMongoDBManager.updatePlayerStats(creatorStats);

        // Rimuove la lobby per il creatore
        LobbyManager.removeLobby(this.discordId);

        // Aggiorna e rimuove la lobby per ogni partecipante (escluso creatore)
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
                for (Long participantId : new ArrayList<>(partecipants)) {
                    if (!participantId.equals(this.discordId)) {
                        Member member = guild.getMemberById(participantId);
                        if (member != null) {
                            if (textChannel.getPermissionOverride(member) != null) {
                                textChannel.getPermissionOverride(member).getManager()
                                        .deny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                                        .queue(
                                                success -> System.out.println("✅ Rimosso permessi a " + member.getEffectiveName()),
                                                error -> System.err.println("❌ Impossibile rimuovere permessi a " + participantId)
                                        );
                            }
                            PlayerStats participantStats = pm.getPlayerStats(participantId);
                            if (participantStats == null) {
                                participantStats = new PlayerStats();
                                participantStats.setDiscordId(participantId);
                                pm.addOrUpdatePlayerStats(participantStats);
                            }
                            participantStats.incrementLobbiesCompletedDirect();
                            PlayerStatMongoDBManager.updatePlayerStats(participantStats);
                            LobbyManager.removeLobby(participantId);
                            partecipants.remove(participantId);
                        }
                    }
                }
            } else {
                System.err.println("❌ Canale privato non trovato o non è un TextChannel.");
            }

            LobbyManager.removeLobby(this.discordId);
            PlayerStatMongoDBManager.updatePlayerStats(hostStats);

        } else {
            // ARCHIVIA THREAD
            ThreadChannel threadChannel = guild.getThreadChannels().stream()
                    .filter(thread -> thread.getIdLong() == this.PostId)
                    .findFirst()
                    .orElse(null);

            if (threadChannel != null) {

                // Applica i tag
                applyClosedTagsToThread(threadChannel);

                // Invia embed nel thread
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("▬▬▬▬▬▬▬▬ Lobby Behavior ▬▬▬▬▬▬▬▬")
                        .setDescription("This lobby has been marked as completed")
                        .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                        .setColor(Color.decode("#1c0b2e"));

                threadChannel.sendMessageEmbeds(embed.build()).queue();

                // Archivia e blocca
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
                                " * **Availability:** " + durationMinutes + " Min \n" +
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

    public void applyClosedTagsToThread(ThreadChannel threadChannel) {
        if (threadChannel == null) {
            System.err.println("❌ ThreadChannel is null.");
            return;
        }

        ForumChannel forum = (ForumChannel) threadChannel.getParentChannel();
        if (forum == null) {
            System.err.println("❌ Thread is not in a forum channel.");
            return;
        }

        List<ForumTag> appliedTags = new ArrayList<>();

        // Tag "Closed" - cambia ID se è Clan Battle
        String closedTagId = this.lobbyType != null && this.lobbyType.equalsIgnoreCase("Clan Battle")
                ? "1392109187240300604"
                : "1389612412470038649";

        ForumTag closedTag = getTagById(forum, closedTagId);
        if (closedTag != null) {
            appliedTags.add(closedTag);
        } else {
            System.err.println("❌ Tag 'Closed' non trovato.");
            return;
        }

        // Seleziona la mappa corretta (normale o Clan)
        Map<String, String> platformTagMap = this.lobbyType != null && this.lobbyType.equalsIgnoreCase("Clan Battle")
                ? PLATFORM_TAG_IDS_CLAN
                : PLATFORM_TAG_IDS;

        Map<String, String> gameTagMap = this.lobbyType != null && this.lobbyType.equalsIgnoreCase("Clan Battle")
                ? GAME_TAG_IDS_CLAN
                : GAME_TAG_IDS;

        // Tag piattaforma
        String platformTagId = platformTagMap.get(this.platform);
        ForumTag platformTag = getTagById(forum, platformTagId);
        if (platformTag != null) {
            appliedTags.add(platformTag);
        }

        // Tag gioco
        String gameTagId = gameTagMap.get(this.game.toUpperCase());
        ForumTag gameTag = getTagById(forum, gameTagId);
        if (gameTag != null) {
            appliedTags.add(gameTag);
        }

        // Tag skillLevel
        ForumTag skillLevelTag = getTagByName(forum, this.skillLevel);
        if (skillLevelTag != null) {
            appliedTags.add(skillLevelTag);
        }

        // Applica i tag al thread
        threadChannel.getManager().setAppliedTags(appliedTags).queue(
                success -> System.out.println("✅ Tag chiusura applicati al thread."),
                error -> System.err.println("❌ Errore durante l'applicazione dei tag al thread: " + error.getMessage())
        );
    }



    public void sendLobbyAnnouncement(Guild guild, long postChannelId, Runnable onComplete) {
        boolean isClanBattle = this.lobbyType != null && this.lobbyType.equalsIgnoreCase("Clan Battle");

        long finalChannelId = isClanBattle ? 1391434101059354804L : postChannelId;

        ForumChannel postChannel = guild.getForumChannelById(finalChannelId);

        if (postChannel == null) {
            System.err.println("❌ Forum post channel not found!");
            return;
        }

        this.announcementChannelId = finalChannelId;
        System.out.println("📌 Annuncio lobby in canale forum ID: " + finalChannelId + " (Tipo: " + lobbyType + ")");

        List<ForumTag> appliedTags = new ArrayList<>();

        // Tag "Open"
        ForumTag openTag = getTagByName(postChannel, "Open");
        if (openTag != null) appliedTags.add(openTag);

        // Tag skillLevel
        ForumTag skillLevelTag = getTagByName(postChannel, skillLevel);
        if (skillLevelTag != null) appliedTags.add(skillLevelTag);

        // Seleziona la mappa tag corretta (Clan vs Normale)
        Map<String, String> platformTagMap = isClanBattle ? PLATFORM_TAG_IDS_CLAN : PLATFORM_TAG_IDS;
        Map<String, String> gameTagMap = isClanBattle ? GAME_TAG_IDS_CLAN : GAME_TAG_IDS;

        // Tag Platform tramite ID
        String platformTagId = platformTagMap.get(platform);
        ForumTag platformTag = getTagById(postChannel, platformTagId);
        if (platformTag != null) {
            appliedTags.add(platformTag);
        } else {
            System.err.println("⚠️ Platform tag '" + platform + "' non trovato tra i tag disponibili nel forum.");
        }

        // Tag Game tramite ID
        String gameTagId = gameTagMap.get(game.toUpperCase());
        ForumTag gameTag = getTagById(postChannel, gameTagId);
        if (gameTag != null) {
            appliedTags.add(gameTag);
        } else {
            System.err.println("⚠️ Game tag '" + game + "' non trovato tra i tag disponibili nel forum.");
        }

        // Costruzione embed
        EmbedBuilder publicEmbed = buildLobbyEmbed();
        Button joinButton = Button.success("join_lobby_" + discordId, "Join");

        MessageCreateBuilder messageBuilder = new MessageCreateBuilder()
                .setEmbeds(publicEmbed.build())
                .setActionRow(joinButton);

        // Immagine gioco
        String imageName = game.toUpperCase() + ".png";
        System.out.println("Cerco immagine: images/" + imageName);
        InputStream imageStream = getClass().getClassLoader().getResourceAsStream("images/" + imageName);

        if (imageStream != null) {
            FileUpload imageUpload = FileUpload.fromData(imageStream, imageName);
            System.out.println("Immagine caricata: " + imageUpload.getName());
            messageBuilder.addFiles(imageUpload);
        } else {
            System.err.println("⚠️ Immagine per il gioco '" + game + "' non trovata nelle risorse (cercato: images/" + imageName + ")");
        }

        // Creazione forum post
        postChannel.createForumPost(playerName + " Lobby", messageBuilder.build())
                .setTags(appliedTags)
                .queue(post -> {
                    ThreadChannel threadChannel = post.getThreadChannel();
                    this.setPostId(threadChannel.getIdLong());

                    threadChannel.getHistory().retrievePast(1).queue(messages -> {
                        if (!messages.isEmpty()) {
                            Message firstMessage = messages.get(0);
                            this.setEmbededMessageId(firstMessage.getIdLong());
                            firstMessage.pin().queue();

                            // Emoji piattaforma
                            Long platformEmojiId = PLATFORM_EMOJI_IDS.get(platform);
                            if (platformEmojiId != null) {
                                RichCustomEmoji platformEmoji = guild.getEmojiById(platformEmojiId);
                                if (platformEmoji != null) {
                                    firstMessage.addReaction(platformEmoji).queue();
                                    System.out.println("✅ Reaction piattaforma aggiunta: " + platformEmoji.getName());
                                } else {
                                    System.err.println("⚠️ Emoji piattaforma ID " + platformEmojiId + " non trovata nella guild.");
                                }
                            } else {
                                System.err.println("⚠️ Nessuna emoji configurata per la piattaforma: " + platform);
                            }

                            // Emoji gioco
                            Map<String, Long> gameEmojiMap = Map.of(
                                    "NSUNS", 1390706988975132785L,
                                    "NSUNS2", 1390714684885434459L,
                                    "NSUNSG", 1317938657872838656L,
                                    "NSUNS3", 1317938959472398479L,
                                    "NSUNSFB", 1390717671766495294L,
                                    "NSUNSR", 1317940351608160317L,
                                    "NSUNS4", 1317940595758600245L,
                                    "NSUNSRTB", 1390716381024096318L,
                                    "NXBUNSC", 1317943597450133514L,
                                    "NSUNSE", 1390719912208633936L
                            );

                            String key = game.toUpperCase();
                            Long emojiId = gameEmojiMap.get(key);
                            if (emojiId != null) {
                                RichCustomEmoji emoji = guild.getEmojiById(emojiId);
                                if (emoji != null) {
                                    firstMessage.addReaction(emoji).queue();
                                    System.out.println("✅ Reaction gioco aggiunta: " + emoji.getName());
                                } else {
                                    System.err.println("⚠️ Emoji gioco ID " + emojiId + " non trovata nella guild.");
                                }
                            } else {
                                System.err.println("⚠️ Nessuna emoji configurata per il gioco: " + game);
                            }

                        } else {
                            System.err.println("⚠️ Nessun messaggio trovato nel thread.");
                        }
                    });

                    System.out.println("📣 Forum lobby post created! Thread ID: " + threadChannel.getIdLong());

                    // Creazione canale privato
                    guild.createTextChannel(playerName.toLowerCase().replace(" ", "-") + "-lobby")
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

                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            });
                });
    }


    private ForumTag getTagByName(ForumChannel forum, String tagName) {
        if (tagName == null) return null;
        return forum.getAvailableTags().stream()
                .filter(tag -> tag.getName().equalsIgnoreCase(tagName))
                .findFirst()
                .orElse(null);
    }
    private ForumTag getTagById(ForumChannel forum, String tagId) {
        if (tagId == null) return null;
        return forum.getAvailableTags().stream()
                .filter(tag -> tag.getId().equals(tagId))
                .findFirst()
                .orElse(null);
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
                                            " * **Availability:** "    + durationMinutes + " Min \n" +
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
                        "### <@"+this.getDiscordId()+">"+" is available for the next "+ durationMinutes +" Min on " + platform + "\n"+
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
