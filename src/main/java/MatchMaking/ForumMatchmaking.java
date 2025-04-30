package MatchMaking;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;

import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.time.Instant;
import java.util.concurrent.TimeUnit;


import java.awt.*;
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
    public Long lobbyId;

    GuildReadyEvent  guildOnReadyEvent;
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
        // Verifica che l'interazione provenga dal menu giusto
        if (event.getComponentId().equals("select_platform")) {
            String selectedPlatform = (String) event.getValues().get(0); // "pc", "xbox", "psn"
            // add the user to the store with his choice
            platformSelections.put(event.getMember().getId(), selectedPlatform);
            // Elimina il messaggio effimero che contiene il menu
            event.getMessage().delete().queue();
            System.out.println(selectedPlatform);


            // Chiedi all'utente di scegliere il gioco
            EmbedBuilder gameEmbed = new EmbedBuilder();
            gameEmbed.setTitle("▬▬▬▬▬▬▬▬▬ Game Selection ▬▬▬▬▬▬▬▬▬");
            gameEmbed.setDescription("Now, please select the game you want to play.\n\n" +
                    "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            gameEmbed.setColor(Color.decode("#252428")); // Puoi usare qualsiasi colore

            // Menu per selezionare il gioco (ad esempio, titoli di giochi)
            StringSelectMenu gameMenu = StringSelectMenu.create("select_game")
                    .setPlaceholder("Choose your game...")
                    .addOption("Storm Connections", "Storm Connections")
                    .addOption("Storm Evo", "Storm Evo")
                    .addOption("Storm 4", "Storm4")
                    .addOption("Storm Trilogy", "Storm Trilogy")
                    .build();

            // Risposta effimera con il nuovo embed di selezione del gioco
            event.replyEmbeds(gameEmbed.build())
                    .addActionRow(gameMenu) // Aggiungi il menu per la selezione del gioco
                    .setEphemeral(true) // Risposta effimera
                    .queue();
        }

        if (event.getComponentId().equals("select_game")) {
            String selectedGame = (String) event.getValues().get(0); // select the game chosen
            gameSelections.put(event.getMember().getId(), selectedGame);
            event.getMessage().delete().queue();
            System.out.println(selectedGame);
            String userId = event.getUser().getId();

            String selectedPlatform = platformSelections.get(userId);
            String inputLabel = switch (selectedPlatform.toLowerCase()) {
                case "pc" -> "Enter your Steam Name Or Id";
                case "xbox" -> "Enter your Xbox Name Account";
                case "psn" -> "Enter your PSN Name Account";
                case "nintendo switch" -> "Enter your Nintendo Switch Name Account";
                default -> "Enter your Player ID";
            };

            TextInput playerIdInput = TextInput.create("player_id", inputLabel, TextInputStyle.SHORT)
                    .setPlaceholder("e.g. naruto123")
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("submit_player_id", "Enter Your ID")
                    .addActionRow(playerIdInput)
                    .build();

            event.replyModal(modal).queue(); // Questo apre il modale all’utente
        }
    }



        @Override
        public void onModalInteraction(ModalInteractionEvent event) {
            if (!event.getModalId().equals("submit_player_id")) return;

            String uid    = event.getUser().getId();
            String uname  = event.getUser().getName();
            String game   = gameSelections.get(uid);
            String pf     = platformSelections.get(uid);
            String pgName = event.getValue("player_id").getAsString();

            // Salva il nome in-game
            playerGameNameSelections.put(uid, pgName);

            // Crea il post nel Forum
            createForumPost(event.getGuild(), uid, uname, game, pf, pgName);

            String userId = event.getUser().getId();
            String playerGameName = event.getValue("player_id").getAsString();
            playerGameNameSelections.put(event.getMember().getId(), playerGameName);
            System.out.println("User " + userId + " submitted: " + playerGameName);
            System.out.println("📋 Current PlayerGameNameSelections Map:");
            playerGameNameSelections.forEach((k, v) ->
                    System.out.println(" > User ID: " + k + " -> PlayerGameName: " + v)
            );

            System.out.println("📋 Current lobbyOwners Map:");
            lobbyOwners.forEach((k, v) ->
                    System.out.println(" > User ID: " + k + " -> channelID: " + v)
            );

            sendLobbyRecap( userId,  event.getUser().getName(), gameSelections.get(userId), platformSelections.get(userId), playerGameName);

            // Rispondi all'utente
            event.reply("✅ Lobby successfully created on the Lobby forum channel").setEphemeral(true).queue();
        }



/*
            if (event.getModalId().equals("submit_player_id")) {
                String userId = event.getUser().getId();
                String playerGameName = event.getValue("player_id").getAsString();
                playerGameNameSelections.put(event.getMember().getId(),playerGameName);
                sendLobbyRecap( userId,  event.getUser().getName(), gameSelections.get(userId), platformSelections.get(userId), playerGameName);
                // Puoi salvarlo o loggarlo
                System.out.println("User " + userId + " submitted: " + playerGameName);

                // Stampa l'intera mappa
                System.out.println("📋 Current PlayerGameNameSelections Map:");
                playerGameNameSelections.forEach((k, v) ->
                        System.out.println(" > User ID: " + k + " -> PlayerGameName: " + v)
                );

                event.reply("✅ Your ID has been saved successfully!")
                        .setEphemeral(true)
                        .queue();
            }

 */

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


    private void createForumPost(Guild guild, String userId, String username, String game, String platform, String playerName)
    {
        // 1) Prendo il ForumChannel
        ForumChannel forum = guild.getForumChannelById(FORUM_CHANNEL_ID);
        if (forum == null) {
            System.err.println("Forum channel non trovato!");
            return;
        }

        // 2) Embed di recap
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬▬ Lobby Created ▬▬▬▬▬▬▬▬")
//                .setDescription(
//                        "**Creator:** <@" + userId + ">\n" +
//                                "**Game:** "      + game     + "\n" +
//                                "**Platform:** "+ platform + "\n" +
//                                "**In-game name:** `" + playerName + "`\n\n" +
//                                "Click On The Button to Join"
//
//                )
                .setDescription(
                        "**A new matchmaking lobby has been created!**\n\n" +
                                "**Lobby Info:**\n" +
                                "> **Creator:** <@" + userId + ">\n" +
                                "> **Game:** `" + game + "`\n" +
                                "> **Platform:** `" + platform + "`\n" +
                                "> **Player Name:** `" + playerName + "`\n\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"

                )
                .setColor(Color.decode("#252428"))
                .setTimestamp(Instant.now());

        // 3) Bottone “Unisciti”
        Button join = Button.success("join_lobby:" + userId, "🎮 Join");

        // 4) Costruisco il MessageCreateData
        MessageCreateData msgData = new MessageCreateBuilder()
                .setEmbeds(eb.build())
                .addActionRow(join)
                .build();

        // 5) Creo il post-forum (ThreadChannel) senza ulteriori code
        forum.createForumPost("Lobby Of " + username, msgData)
                .queue();
    }



    @Override
    public void onGuildReady(GuildReadyEvent event) {
        this.guildOnReadyEvent = event;
        List<CommandData> commands = new ArrayList<>();
        Long guildId = 856147888550969345L; // Replace with your server's ID
        Guild guild = event.getJDA().getGuildById(guildId);

        commands.add(Commands.slash("matchmaking", "Open a Matchmaking Request"));

        guild.updateCommands().addCommands(commands).queue();
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
}
