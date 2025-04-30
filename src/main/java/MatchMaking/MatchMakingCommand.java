package MatchMaking;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
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

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class MatchMakingCommand extends ListenerAdapter {

    private final Map<String, String> platformSelections = new HashMap<>();
    private final Map<String, String> gameSelections = new HashMap<>();
    private final Map<String, String> playerGameNameSelections = new HashMap<>();
    private final Map<String, String> lobbyOwners = new HashMap<>();
    private final Map<String, String> lobbyHostsByMessageId = new HashMap<>();

    GuildReadyEvent  guildOnReadyEvent;

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
        if (event.getModalId().equals("submit_player_id")) {
            String userId = event.getUser().getId();
            String playerGameName = event.getValue("player_id").getAsString();
            playerGameNameSelections.put(event.getMember().getId(), playerGameName);
            lobbyOwners.put(userId, userId); // l'host è se stesso, lo salvi come chiave e valore

            sendLobbyRecap(userId, event.getUser().getName(), gameSelections.get(userId), platformSelections.get(userId), playerGameName);

            System.out.println("User " + userId + " submitted: " + playerGameName);
            System.out.println("📋 Current PlayerGameNameSelections Map:");
            playerGameNameSelections.forEach((k, v) ->
                    System.out.println(" > User ID: " + k + " -> PlayerGameName: " + v)
            );

            // Embed creation
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎮 Lobby created from " + playerGameName)
                    .setDescription("A player has started a new lobby.\nClick the button below to join!")
                    .setColor(Color.decode("#252428"));

            // Button creation
            Button joinButton = Button.secondary("join_lobby", "🎮 Join Lobby");

            event.replyEmbeds(embed.build())
                    .addActionRow(joinButton)
                    .setEphemeral(false)
                    .queue();
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
        }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("join_lobby")) return;

        Guild guild      = event.getGuild();
        Member clicker   = event.getMember();
        String clickerId = clicker.getId();
        // Recupera l'host dal messageId salvato in onModalInteraction
        String hostId    = lobbyHostsByMessageId.get(event.getMessage().getId());

        // 1) Elimina subito il messaggio col bottone
        event.getMessage().delete().queue();

        // 2) Crea un thread pubblico "lobby"
        event.getMessage()
                .createThreadChannel("lobby")
                .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_24_HOURS)
                .queue(thread -> {
                    IPermissionContainer perms = thread.getPermissionContainer();

                    // 3) Nega VIEW_CHANNEL a tutti (@everyone)
                    perms.upsertPermissionOverride(guild.getPublicRole())
                            .setDenied(Permission.VIEW_CHANNEL)
                            .queue();

                    // 4) Concede VIEW + SEND al clicker
                    perms.upsertPermissionOverride(clicker)
                            .setAllowed(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                            .queue();

                    // 5) Concede VIEW + SEND anche all’host
                    if (hostId != null && !hostId.equals(clickerId)) {
                        guild.retrieveMemberById(hostId).queue(hostMember ->
                                        perms.upsertPermissionOverride(hostMember)
                                                .setAllowed(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                                                .queue(),
                                err -> System.err.println("Host non trovato: " + err.getMessage())
                        );
                    }

                    // 6) Messaggio di benvenuto nel thread
                    thread.sendMessage("🎮 <@" + clickerId + "> joined the lobby!").queue();
                });

        // 7) Conferma effimera al clicker
        event.reply("✅ Lobby thread created and you're in!").setEphemeral(true).queue();
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

    public void sendLobbyRecap( String userId, String username, String game, String platform, String playerName) {

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
                                "**Created At:** `" + creationTime + "` \n\n"+
                        "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"

                )
                .setColor(Color.WHITE);
                //.setImage("https://cdn.discordapp.com/attachments/1350633984740032582/1362835603489554652/Shunsui_Gif_2.gif?ex=6803d710&is=68028590&hm=0ff797ef08a8b6d066295b3f45e59bcd53b7ae8288f5d85381fa6573e553d5df&");

        logChannel.sendMessageEmbeds(eb.build()).queue();
    }

}


