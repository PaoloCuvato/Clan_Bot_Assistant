package MatchMaking;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.entities.channel.attribute.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.attribute.*;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.*;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ForumMatchmaking extends ListenerAdapter {
    private final Map<String, String> platformSelections = new HashMap<>();
    private final Map<String, String> gameSelections = new HashMap<>();
    private final Map<String, String> playerGameNameSelections = new HashMap<>();
    private final Map<String, String> lobbyOwners = new HashMap<>();
    private final Map<String, String> connectionTypeSelections = new HashMap<>();
    public GuildReadyEvent  guildOnReadyEvent;
    public Long lobbyId;
    private static final String GUILD_ID = "1261880269573459990";
    private static final long FORUM_CHANNEL_ID = 1367186054045761616L;
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("matchmaking")) {

            // Primo embed - Info generale
            EmbedBuilder infoEmbed = new EmbedBuilder();
            infoEmbed.setTitle("▬▬▬▬▬▬▬▬▬ Matchmaking Info ▬▬▬▬▬▬▬▬▬");
            infoEmbed.setDescription(
                    "**Welcome to the Matchmaking System!**\n\n" +
                            " > This system helps players create and join game lobbies, and find fair matches.\n\n" +
                            " > Below you'll find more details and options.\n\n" +
                            "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
            );
            infoEmbed.setColor(Color.decode("#252428"));

            // Secondo embed - Selezione piattaforma (senza emoji per ora)
            EmbedBuilder platformEmbed = new EmbedBuilder();
            platformEmbed.setTitle("▬▬▬▬▬▬▬▬▬▬ Platform Selection ▬▬▬▬▬▬▬▬▬▬");
            platformEmbed.setDescription(" > What platform do you play on?\n\nPlease select your gaming platform from the dropdown menu below.\n\n" +
                    "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            platformEmbed.setColor(Color.decode("#252428"));

            // Dropdown menu (senza emoji personalizzate per semplificazione)
            StringSelectMenu platformMenu = StringSelectMenu.create("select_platform")
                    .setPlaceholder("Choose your platform...")
                    .addOption("PC", "Pc", "Play on PC")
                    .addOption("Xbox", "Xbox", "Play on Xbox")
                    .addOption("PSN", "Psn", "Play on PlayStation Network")
                    .addOption("Switch", "Nintendo Switch", "Play on Nintendo Switch")

                    .build();

            // Risposta effimera con entrambi gli embed
            event.replyEmbeds(infoEmbed.build()) // Risposta con il primo embed
                    .addEmbeds(platformEmbed.build()) // Aggiungi il secondo embed
                    .addActionRow(platformMenu) // Aggiungi il menu dropdown
                    .setEphemeral(true) // Risposta effimera
                    .queue();

        }
    }


    @Override
    public void onGenericSelectMenuInteraction(GenericSelectMenuInteractionEvent event) {
        if (event.getComponentId().equals("select_platform")) {
            String selectedPlatform = (String) event.getValues().get(0); // Ottieni la piattaforma selezionata
            platformSelections.put(event.getUser().getId(), selectedPlatform);

            // Elimina il messaggio del menu precedente
            event.getMessage().delete().queue();

            // Chiedi all'utente di selezionare il gioco
            EmbedBuilder gameEmbed = new EmbedBuilder();
            gameEmbed.setTitle("▬▬▬▬▬▬▬▬▬ Game Selection ▬▬▬▬▬▬▬▬▬");
            gameEmbed.setDescription("Now, please select the game you want to play.\n\n" +
                    "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            gameEmbed.setColor(Color.decode("#252428")); // Puoi usare qualsiasi colore

            StringSelectMenu gameMenu = StringSelectMenu.create("select_game")
                    .setPlaceholder("Choose your game...")
                    .addOption("Storm Connections", "Storm Connections")
                    .addOption("Storm Evo", "Storm Evo")
                    .addOption("Storm 4", "Storm 4")
                    .addOption("Storm Trilogy", "Storm Trilogy")
                    .build();

            event.replyEmbeds(gameEmbed.build())
                    .addActionRow(gameMenu)
                    .setEphemeral(true)
                    .queue();
        }

        if (event.getComponentId().equals("select_game")) {
            String selectedGame = (String) event.getValues().get(0); // Seleziona il gioco
            gameSelections.put(event.getUser().getId(), selectedGame);
            event.getMessage().delete().queue();

            // Chiedi il tipo di connessione
            EmbedBuilder connectionEmbed = new EmbedBuilder();
            connectionEmbed.setTitle("▬▬▬▬▬▬▬▬▬ Connection Type ▬▬▬▬▬▬▬▬▬");
            connectionEmbed.setDescription("Please select your internet connection type: \n\n" +
                    "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            connectionEmbed.setColor(Color.decode("#252428"));

            StringSelectMenu connectionMenu = StringSelectMenu.create("select_connection")
                    .setPlaceholder("Select your connection type...")
                    .addOption("Wi-Fi", "Wifi")
                    .addOption("Ethernet (Cable)", "Ethernet")
                    .build();

            event.replyEmbeds(connectionEmbed.build())
                    .addActionRow(connectionMenu)
                    .setEphemeral(true)
                    .queue();
        }

        if (event.getComponentId().equals("select_connection")) {
            String selectedConnection = (String) event.getValues().get(0); // Seleziona la connessione
            connectionTypeSelections.put(event.getUser().getId(), selectedConnection);
            event.getMessage().delete().queue();

            // Chiedi l'ID del giocatore tramite un modulo (Modal)
            TextInput playerIdInput = TextInput.create("player_id", "Enter your Player ID", TextInputStyle.SHORT)
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("submit_player_id", "Enter Your ID")
                    .addActionRow(playerIdInput)
                    .build();

            event.replyModal(modal).queue(); // Apre il modale all’utente
        }
    }



    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("submit_player_id")) return;

        String uid = event.getUser().getId();
        String uname = event.getUser().getName();
        String game = gameSelections.get(uid);
        String pf = platformSelections.get(uid);
        String playerGameName = event.getValue("player_id").getAsString();
        String connection = connectionTypeSelections.get(uid);

        // Salva il nome in-game
        playerGameNameSelections.put(uid, playerGameName);

        // Crea il post nel Forum
        createForumPost(event.getGuild(), uid, uname, game, pf,connection, playerGameName);

        //Control if every haspmap size is too big
        checkAndClearMaps();

        // Log di debug
        System.out.println("User " + uid + " submitted: " + playerGameName);
        System.out.println("📋 Current PlayerGameNameSelections Map:");
        playerGameNameSelections.forEach((k, v) ->
                System.out.println(" > User ID: " + k + " -> PlayerGameName: " + v)
        );

        System.out.println("📋 Current lobbyOwners Map:");
        lobbyOwners.forEach((k, v) ->
                System.out.println(" > User ID: " + k + " -> channelID: " + v)
        );

        // Invia un riepilogo privato all'utente
        sendLobbyRecap(uid, uname, game, pf, playerGameName);

        // Risposta all'utente
        event.reply("✅ Lobby successfully created on the Lobby forum channel").setEphemeral(true).queue();
    }



    public void createLobby(String userId, String playerGameName, Guild guild) {
        // Recupera o crea la categoria "Lobby"
        Category category = guild.getCategoriesByName("Lobby", true)
                .stream().findFirst()
                .orElseGet(() -> guild.createCategory("Lobby").complete());

        // Crea il nome del canale in modo che sia valido
        String channelName = guild.getMemberById(userId).getEffectiveName() + " -lobby"; // Puoi modificare il nome come desideri
        boolean lobbyExists = category.getTextChannels().stream().anyMatch(channel ->
                channel.getName().equals(channelName));

        if (lobbyExists) {
            // Se la lobby esiste già, puoi inviare un messaggio di errore
            return;
        }

        // Crea il canale privato
        category.createTextChannel(channelName)
                .addPermissionOverride(guild.getPublicRole(), 0L, Permission.VIEW_CHANNEL.getRawValue())
                .addPermissionOverride(guild.getMemberById(userId),
                        Permission.VIEW_CHANNEL.getRawValue()
                                | Permission.MESSAGE_SEND.getRawValue()
                                | Permission.MESSAGE_HISTORY.getRawValue(),
                        0L)
                .setTopic("Lobby Owner ID: " + userId)
                .queue(channel -> {
                    this.lobbyId = Long.valueOf(channel.getId());
                    channel.sendMessage(guild.getMemberById(userId).getAsMention() + " — Your private lobby has been created!").queue();

                });
    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String cid = event.getComponentId();
        if (!cid.startsWith("join_lobby:")) return;

        String hostId   = cid.split(":", 2)[1];
        String joinerId = event.getUser().getId();

        // Tagga entrambi nel thread
        event.getChannel().sendMessageFormat(
                "<@%s>, Joined on <@%s> on the lobby!",
                hostId, joinerId
        ).queue();

        // Ack privato
        event.reply("✅ You joined successfully on the lobby!").setEphemeral(true).queue();
    }


    private void createForumPost(Guild guild, String userId, String username, String game, String platform,String connection, String playerName) {
        // 1) Prendo il ForumChannel
        ForumChannel forum = guild.getForumChannelById(FORUM_CHANNEL_ID);
        if (forum == null) {
            System.err.println("Forum channel non trovato!");
            return;
        }

        // 2) Recupero i tag disponibili e trovo quelli giusti per gioco e piattaforma
        List<ForumTag> availableTags = forum.getAvailableTags();
        List<ForumTagSnowflake> tagIds = new ArrayList<>();

        // Aggiungi il tag del gioco
        for (ForumTag tag : availableTags) {
            if (tag.getName().equalsIgnoreCase(game)) {
                tagIds.add(ForumTagSnowflake.fromId(tag.getId())); // Tag del gioco trovato, aggiunto alla lista
            }
        }

        // Aggiungi il tag della piattaforma
        for (ForumTag tag : availableTags) {
            if (tag.getName().equalsIgnoreCase(platform)) {
                tagIds.add(ForumTagSnowflake.fromId(tag.getId())); // Tag della piattaforma trovato, aggiunto alla lista
            }
        }

        // Add The Tag Connection at the post
        for (ForumTag tag : availableTags){
            if(tag.getName().equalsIgnoreCase(connection)){
                tagIds.add(ForumTagSnowflake.fromId(tag.getId()));
            }
        }


        // 3) Embed di recap con le informazioni della lobby
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬▬ Lobby Created ▬▬▬▬▬▬▬▬")
                .setDescription(
                        "**A new matchmaking lobby has been created!**\n\n" +
                                "**Lobby Info:**\n" +
                                "> **Creator:** <@" + userId + ">\n" +
                                "> **Game:** `" + game + "`\n" +
                                "> **Platform:** `" + platform + "`\n" +
                                "> **Connection:** `" + connection + "`\n" +
                                "> **Player Name:** `" + playerName + "`\n\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                )
                .setColor(Color.decode("#252428"))
                .setTimestamp(Instant.now());

        // 4) Bottone “Unisciti”
        Button join = Button.success("join_lobby:" + userId, "🎮 Join");

        // 5) Costruisco il MessageCreateData
        MessageCreateData msgData = new MessageCreateBuilder()
                .setEmbeds(eb.build())
                .addActionRow(join)
                .build();

        // 6) Creo il post-forum (ThreadChannel) con i tag (gioco e piattaforma)
        forum.createForumPost("Lobby Of " + username, msgData)
                .setTags(tagIds)
                .queue();
    }

    public void deleteArchivedPosts(Guild guild) {
        ForumChannel forum = guild.getForumChannelById(FORUM_CHANNEL_ID);
        if (forum == null) {
            System.err.println("Forum channel not found!");
            return;
        }

        forum.retrieveArchivedPublicThreadChannels().queue(archivedThreads -> {
            if (archivedThreads.isEmpty()) {
                System.out.println("No archived threads found.");
                return;
            }

            for (ThreadChannel thread : archivedThreads) {
                thread.delete().queue(
                        success -> System.out.println("Deleted archived thread: " + thread.getName()),
                        error -> System.err.println("Failed to delete thread: " + thread.getName())
                );
            }
        });
    }
    public void checkAndClearMaps(){
        if(platformSelections.size() >= 100 ){
            platformSelections.clear();
            System.out.println("PlatformSelection Map got cleaned up");
        }
        if(gameSelections.size() >= 100){
            gameSelections.clear();
            System.out.println("GameSelection Map got cleaned up");
        }
        if(playerGameNameSelections.size()  >= 100){
            platformSelections.clear();
            System.out.println("PlayerGameNameSelections Map got cleaned up");
        }
        if (lobbyOwners.size() >= 100){
            lobbyOwners.clear();
            System.out.println("LobbyOwners Map got cleaned up");
        }
        if (connectionTypeSelections.size() >= 100){
            connectionTypeSelections.clear();
            System.out.println("ConnectionTypeSelection Map got cleaned up");
        }
    }

    public void sendLobbyRecap(String userId, String username, String game, String platform, String playerName) {

        // ID del canale di log (sostituisci con il tuo)
        long logChannelId = 1366842485791526994L; // <-- Sostituisci con il tuo vero ID

        TextChannel logChannel = guildOnReadyEvent.getGuild().getTextChannelById(logChannelId);
        if (logChannel == null) {
            System.err.println("Log channel not found!");
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss");
        String creationTime = LocalDateTime.now().format(formatter);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬▬ Lobby Created ▬▬▬▬▬▬▬▬ ")
                .setDescription(
                        "**A new matchmaking lobby has been created!**\n\n" +
                                "**User Info:**\n" +
                                "> **Discord ID:** `" + userId + "`\n" +
                                "> **Username:** `" + username + "`\n\n" +
                                "**Lobby Info:**\n" +
                                "> **Game:** `" + game + "`\n" +
                                "> **Platform:** `" + platform + "`\n" +
                                "> **Player Name:** `" + playerName + "`\n\n" +
                                "**Created At:** `" + creationTime + "` \n\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"

                )
                .setColor(Color.WHITE);
        //.setImage("https://cdn.discordapp.com/attachments/1350633984740032582/1362835603489554652/Shunsui_Gif_2.gif?ex=6803d710&is=68028590&hm=0ff797ef08a8b6d066295b3f45e59bcd53b7ae8288f5d85381fa6573e553d5df&");

        logChannel.sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        this.guildOnReadyEvent = event;
        List<CommandData> commands = new ArrayList<>();
        Long guildId = 856147888550969345L; // Replace with your server's ID
        Guild guild = event.getJDA().getGuildById(guildId);

        commands.add(Commands.slash("matchmaking", "Open a Matchmaking Request"));

        guild.updateCommands().addCommands(commands).queue();

        // 🔁 Scheduler che ogni 6 ore elimina i post archiviati
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            deleteArchivedPosts(guild);
        }, 0, 24, TimeUnit.HOURS); // ⏰ Ogni 24 ore
    }
}
