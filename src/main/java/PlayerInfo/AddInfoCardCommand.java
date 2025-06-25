package PlayerInfo;

import Stat.PlayerStats;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import Stat.*;

import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.Objects;

public class AddInfoCardCommand extends ListenerAdapter {

    public User target;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "add_info_card" -> handleAddInfoCard(event);
            case "edit_ninja_card" -> handleEditInfoCard(event);
            case "search_ninjacard" -> handleSearchNinjaCard(event);
        }

        if (event.getName().equals("send_player_info_file")) {
            event.deferReply().queue();

            File file = new File("playerinfolist.txt");

            if (!file.exists()) {
                event.getHook().sendMessage("❌ The file playerinfolist.txt was not found.").queue();
                return;
            }

            event.getHook().sendMessage("📄 Here is a text file containing all the users with the Player Info role:")
                    .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(file))
                    .queue();
        }

        if (!event.getName().equals("my_ninjacard")) return;

        // Ricava membro e utente che hanno invocato il comando
        Member member = event.getMember();
        User user = event.getUser();

        // 1) Costruisci l'embed
        EmbedBuilder eb = new EmbedBuilder()
                .setDescription(
                        "**Welcome to our Show stats page.**\n" +
                                "> Use this command to display all the info about you. You can view a summary of your stat, like your player info and stats about your games\n\n" +
                                "**Please choose one of the options below to see:**\n" +
                                "> * **Ninja Card Info:** This option will show you all the data relative to your ninja card\n" +
                                "> * **General Stats:** This option will show you all the data relative to your Global Lobby stats\n" +
                                "> * **Direct Stats:** This option will show you all the data relative to your Private Lobby stats\n"
                )
                .setImage("https://media.discordapp.net/attachments/1163923648034373773/1387401919567368263/Meme.gif?ex=685d363a&is=685be4ba&hm=545ca9c6eb7aa7412b16d580f4fa65cf808cd530296d8c694113dae5d16f7fa4&=")
                .setColor(Color.white);
        // 2) Costruisci il dropdown menu
        StringSelectMenu menu = StringSelectMenu.create("result:menu")
                .setPlaceholder("Choose an option to see")
                .setMinValues(1)
                .setMaxValues(1)
                .addOption("Ninja Card Info", "ninja_card_info")
                .addOption("General Stats", "general_stats")
                //   .addOption("Score Lobby", "lobby_score")
                .addOption("Direct Stats", "direct_stats")
                .build();

        // 3) Invia embed + menu
        event.replyEmbeds(eb.build())
                .addActionRow(menu)
                .setEphemeral(true)
                .queue();
    }

    private void handleSearchNinjaCard(SlashCommandInteractionEvent event) {

        OptionMapping targetOption = event.getOption("target");
        this.target= targetOption.getAsUser();

        EmbedBuilder eb = new EmbedBuilder()
                .setDescription(
                        "**Stats for player:** " + targetOption.getAsUser().getName() + "\n" +
                                "> Use this command to display all the info about a specific user. You can view a summary of their player info and stats about their games.\n" +
                                "**Please choose one of the options below to see:**\n" +
                                "> * **Ninja Card Info:** This option will show you all the data related to the player's ninja card.\n" +
                                "> * **General Stats:** This option will show you all the data related to the player's Global Lobby stats.\n" +
                                "> * **Direct Stats:** This option will show you all the data related to the player's Private Lobby stats.\n"
                )
                .setImage("https://media.discordapp.net/attachments/1163923648034373773/1387396028650750114/JJ.gif?ex=685d30bd&is=685bdf3d&hm=0619f4f76510179973924bb2aa175ef88d6f34482ac57723a9e3c4bb8dae8e5b&=")
                .setColor(Color.white);

        String componentId = "search_result:menu:";

        StringSelectMenu menu = StringSelectMenu.create(componentId)
                .setPlaceholder("Choose an option to see")
                .setMinValues(1)
                .setMaxValues(1)
                .addOption("Ninja Card Info", "ninja_card_info")
                .addOption("General Stats", "general_stats")
                .addOption("Direct Stats", "direct_stats")
                .build();

        event.replyEmbeds(eb.build())
                .addActionRow(menu)
                .setEphemeral(true)
                .queue();
    }



    private void handleAddInfoCard(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        long discordId = user.getIdLong();

        PlayerInfo player = new PlayerInfo();
        player.setDiscordId(discordId);
        player.setDiscordUsername(user.getName());
        player.setLobbyCounter(0);

        PlayerInfoStorage.addOrUpdatePlayerInfo(discordId, player);
        System.out.println("New player Discord ID: " + discordId);

        sendIntroEmbed(event, "Create Your Ninja Info Card", "This command helps you set up your player profile to join lobbies and participate in events.");
    }

    private void handleEditInfoCard(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        long discordId = user.getIdLong();

        PlayerInfo player = PlayerInfoStorage.getPlayerInfo(discordId);
        if (player == null) {
            event.reply("❌ No profile found. Use `/add_info_card` to create one.").setEphemeral(true).queue();
            return;
        }

        sendIntroEmbed(event, "Edit Your Ninja Info Card", "This command lets you update your existing player profile.");
    }

    private void sendIntroEmbed(SlashCommandInteractionEvent event, String title, String description) {
        EmbedBuilder intro = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬ " + title + " ▬▬▬▬▬▬▬▬")
                .setDescription(" > " + description +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferReply(true).queue(hook -> {
            hook.editOriginalEmbeds(intro.build())
                    .setActionRow(
                            StringSelectMenu.create("select_game")
                                    .setPlaceholder("Choose your games")
                                    .setMinValues(1)
                                    .setMaxValues(5)
                                    .addOption("Storm Connections", "Storm Connections")
                                    .addOption("Storm Evolution", "Storm Evolution")
                                    .addOption("Storm 4", "Storm 4")
                                    .addOption("Storm Revolution", "Storm Revolution")
                                    .addOption("Storm Trilogy", "Storm Trilogy")
                                    .build()
                    ).queue();
        });
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long discordId = event.getUser().getIdLong();
        PlayerInfo player = PlayerInfoStorage.getPlayerInfo(discordId);

        if (player == null) {
            event.reply("❌ Player profile not found. Please use /add_info_card to create one.")
                    .setEphemeral(true).queue();
            return;
        }

        switch (event.getComponentId()) {
            case "select_game" -> {
                player.setGame(event.getValues().toArray(new String[0]));
                PlayerInfoStorage.addOrUpdatePlayerInfo(discordId, player);
                event.deferEdit().queue();
                askPlatforms(event); // 👈 questa è la versione corretta
            }

            case "select_platforms" -> {
                player.setPlatforms(event.getValues().toArray(new String[0])); // Aggiungi metodo `setPlatforms` in PlayerInfo
                PlayerInfoStorage.addOrUpdatePlayerInfo(discordId, player);
                event.deferEdit().queue();
                askConnectionType(event);
            }
            case "select_connection" -> {
                player.setConnectionType(event.getValues().get(0));
                PlayerInfoStorage.addOrUpdatePlayerInfo(discordId, player);
                event.deferEdit().queue();
                askCurrentRegion(event);
            }
            case "select_region" -> {
                player.setCurrentRegion(event.getValues().get(0));
                PlayerInfoStorage.addOrUpdatePlayerInfo(discordId, player);
                event.deferEdit().queue();
                askTargetRegion(event);
            }
            case "select_target_region" -> {
                player.setTargetRegion(event.getValues().get(0));
                PlayerInfoStorage.addOrUpdatePlayerInfo(discordId, player);
                event.deferEdit().queue();
                askLanguages(event);
            }
            case "select_languages" -> {
                player.setSpokenLanguages(event.getValues().toArray(new String[0]));
                PlayerInfoStorage.addOrUpdatePlayerInfo(discordId, player);
                askFinalModal(event);
            }
            case "result:menu" -> {
                String selected = event.getValues().get(0);
                if (player == null) {
                    event.reply("❌ Player profile not found. Please use `/add_info_card` to create one.")
                            .setEphemeral(true).queue();
                    return;
                }

                switch (selected) {
                    case "ninja_card_info" -> {
                        event.deferEdit().queue(); // Rimuove i componenti e "pulisce" il messaggio
                        event.getHook().editOriginalEmbeds(getNinjaCardEmbed(player).build())
                                .setComponents() // Rimuove i componenti (menu) se non vuoi lasciarli
                                .queue();
                    }
                    case "general_stats" -> {
                        PlayerStats stats = PlayerStatsManager.getInstance().getPlayerStats(discordId);

                        if (stats == null) {
                            event.reply("❌ Player statistics not found. Please play some matches first.")
                                    .setEphemeral(true).queue();
                            return;
                        }

                        event.deferEdit().queue();
                        event.getHook().editOriginalEmbeds(getGeneralStatsEmbed(stats).build())
                                .setComponents()
                                .queue();
                    }

                    case "direct_stats" -> {
                        PlayerStats stats = PlayerStatsManager.getInstance().getPlayerStats(discordId);

                        if (stats == null) {
                            event.reply("❌ Player statistics not found. Please play some matches first.")
                                    .setEphemeral(true).queue();
                            return;
                        }

                        event.deferEdit().queue();
                        event.getHook().editOriginalEmbeds(getDirectStatsEmbed(stats).build())
                                .setComponents()
                                .queue();
                    }

                }

            }
            case "search_result:menu:" -> {
                String selected = event.getValues().get(0);
                if (this.target == null) {
                    event.reply("❌ The user that you send is null")
                            .setEphemeral(true).queue();
                    return;
                }

                switch (selected) {
                    case "ninja_card_info" -> {
                        PlayerInfo p2 = PlayerInfoStorage.getPlayerInfo(target.getIdLong());
                        event.deferEdit().queue(); // Rimuove i componenti e "pulisce" il messaggio
                        event.getHook().editOriginalEmbeds(getNinjaCardEmbed(p2).build())
                                .setComponents() // Rimuove i componenti (menu) se non vuoi lasciarli
                                .queue();
                    }
                    case "general_stats" -> {
                        PlayerStats stats = PlayerStatsManager.getInstance().getPlayerStats(target.getIdLong());

                        if (stats == null) {
                            event.reply("❌ Player statistics not found. Please play some matches first.")
                                    .setEphemeral(true).queue();
                            return;
                        }

                        event.deferEdit().queue();
                        event.getHook().editOriginalEmbeds(getGeneralStatsEmbed(stats).build())
                                .setComponents()
                                .queue();
                    }

                    case "direct_stats" -> {
                        PlayerStats stats = PlayerStatsManager.getInstance().getPlayerStats(target.getIdLong());

                        if (stats == null) {
                            event.reply("❌ Player statistics not found. Please play some matches first.")
                                    .setEphemeral(true).queue();
                            return;
                        }

                        event.deferEdit().queue();
                        event.getHook().editOriginalEmbeds(getDirectStatsEmbed(stats).build())
                                .setComponents()
                                .queue();
                    }

                }

            }

        }
    }

    private void askConnectionType(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬ Connection Type ▬▬▬▬▬▬▬")
                .setDescription(" > Select the type of connection you use to play online." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.getHook().editOriginalEmbeds(embed.build())
                .setActionRow(
                        StringSelectMenu.create("select_connection")
                                .addOption("WiFi", "WiFi")
                                .addOption("Wired", "Wired")
                                .build()
                ).queue();
    }

    private void askPlatforms(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬▬▬ 🎮 Platform ▬▬▬▬▬▬▬▬▬")
                .setDescription(" > Select all platforms you play on (multiple selections allowed)." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.getHook().editOriginalEmbeds(embed.build())
                .setActionRow(
                        StringSelectMenu.create("select_platforms")
                                .setPlaceholder("Choose your platforms")
                                .setMaxValues(4)
                                .addOption("PlayStation (PSN)", "PSN")
                                .addOption("PC (Steam/Epic)", "PC")
                                .addOption("Xbox", "Xbox")
                                .addOption("Nintendo Switch", "Switch")
                                .build()
                ).queue();
    }


    private void askCurrentRegion(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬ 🌍 Your Region ▬▬▬▬▬")
                .setDescription(" > Where are you currently located?" +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.getHook().editOriginalEmbeds(embed.build())
                .setActionRow(
                        StringSelectMenu.create("select_region")
                                .addOption("EU", "EU")
                                .addOption("NA", "NA")
                                .addOption("SA", "SA")
                                .addOption("JP", "JP")
                                .build()
                ).queue();
    }

    private void askTargetRegion(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬ 🎯 Target Region ▬▬▬▬▬")
                .setDescription(" > Which region do you want to play with?\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.getHook().editOriginalEmbeds(embed.build())
                .setActionRow(
                        StringSelectMenu.create("select_target_region")
                                .addOption("EU", "EU")
                                .addOption("NA", "NA")
                                .addOption("JP", "JP")
                                .addOption("SA", "SA")
                                .build()
                ).queue();
    }

    private void askLanguages(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ 🗣️ Spoken Languages ▬▬▬▬▬▬▬")
                .setDescription(" > Select the languages you speak (you can choose multiple)." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.getHook().editOriginalEmbeds(embed.build())
                .setActionRow(
                        StringSelectMenu.create("select_languages")
                                .setPlaceholder("Choose languages")
                                .setMaxValues(6)
                                .addOption("English", "English")
                                .addOption("French", "French")
                                .addOption("Spanish", "Spanish")
                                .addOption("Italian", "Italian")
                                .addOption("Arabic", "Arabian")
                                .addOption("Japanese", "Japanese")
                                .build()
                ).queue();
    }

    private void askFinalModal(StringSelectInteractionEvent event) {
        event.getHook().editOriginalComponents().queue(); // Clear select menus

        TextInput playerName = TextInput.create("player_name", "In-Game Name", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("e.g. User1234")
                .build();

        TextInput hoursPlayed = TextInput.create("hours_played", "Hours Played (number)", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("e.g. 120")
                .build();

        TextInput availableTime = TextInput.create("available_time", "When Do You Play?", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setPlaceholder("e.g. Weekends, Monday evenings, etc.")
                .build();

        Modal modal = Modal.create("final_modal", "Final Profile Details")
                .addActionRow(playerName)
                .addActionRow(hoursPlayed)
                .addActionRow(availableTime)
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("final_modal")) return;

        long discordId = event.getUser().getIdLong();
        PlayerInfo player = PlayerInfoStorage.getPlayerInfo(discordId);

        if (player == null) {
            event.reply("❌ Player profile not found. Please use /add_info_card to create one.")
                    .setEphemeral(true).queue();
            return;
        }

        player.setPlayerName(event.getValue("player_name").getAsString());

        String hoursInput = event.getValue("hours_played").getAsString();
        try {
            int hours = Integer.parseInt(hoursInput);
            player.setInGamePlayTime(String.valueOf(hours));
        } catch (NumberFormatException e) {
            event.reply("❌ Please enter a valid number for hours played.")
                    .setEphemeral(true).queue();
            return;
        }

        player.setAvailablePlayTime(event.getValue("available_time").getAsString());

        PlayerInfoStorage.addOrUpdatePlayerInfo(discordId, player);
        // Qui salvi la lista aggiornata su file
        PlayerInfoFileManager.savePlayerInfoList(PlayerInfoStorage.getAllSessions());
        PlayerInfoStorage.printAllPlayers();

        event.reply("✅ Your profile has been successfully updated!").setEphemeral(true).queue();

        player.sendPlayerInfoLog(Objects.requireNonNull(event.getGuild()));

        // 👉 Here you assign the player info role
        long roleId = 1382385471300304946L; // ID del ruolo
        event.getGuild().retrieveMemberById(discordId).queue(member -> {
            event.getGuild().addRoleToMember(member, event.getGuild().getRoleById(roleId)).queue(
                    success -> System.out.println("✅ Ruolo Player Info assegnato a " + member.getEffectiveName()),
                    error -> System.err.println("❌ Impossibile assegnare il ruolo Player Info: " + error.getMessage())
            );
        }, error -> {
            System.err.println("❌ Utente non trovato: " + error.getMessage());
        });
    }
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        Map<Long, PlayerInfo> playerInfoMap = PlayerInfoMongoDBManager.getAllPlayerInfosAsMap();

        // Carica in memoria
        PlayerInfoStorage.loadSessions(playerInfoMap);

        // Stampa tutta la mappa
        System.out.println("✅ NinjaCards loaded from MongoDB:");
        for (Map.Entry<Long, PlayerInfo> entry : playerInfoMap.entrySet()) {
            System.out.println("🔹 DiscordID: " + entry.getKey());
            System.out.println("    " + entry.getValue()); // Assicurati che PlayerInfo abbia un buon toString()
        }
    }


    private EmbedBuilder getNinjaCardEmbed(PlayerInfo p) {
        return new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ Your Ninja Card ▬▬▬▬▬▬")
                .setColor(Color.decode("#1c0b2e"))
                .setDescription(
                        "**# Ninja Card Info:**" +
                                "These are all the stat about your player ninja card\n" +
                                " * **Platform:** " + String.join(", ", p.getPlatforms()) + "\n" +
                                " * **Game:** " + String.join(", ", p.getGame()) + "\n" +
                                " * **Player Name:** " + p.getPlayerName() + "\n" +
                                " * **Connection:** " + p.getConnectionType() + "\n" +
                                " * **My Region:** " + p.getCurrentRegion() + "\n" +
                                " * **Target Region:** " + p.getTargetRegion() + "\n" +
                                " * **Languages:** " + String.join(", ", p.getSpokenLanguages()) + "\n" +
                                " * **Availability:** " + p.getAvailablePlayTime() + "\n" +
                                " * **Hours Played:** " + p.getInGamePlayTime() + "\n" +
                                " * **Lobbies Joined:** " + p.getLobbyCounter() + "\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                );
    }

    private EmbedBuilder getGeneralStatsEmbed(PlayerStats stats) {
        return new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ Your General Stats ▬▬▬▬▬▬")
                .setColor(Color.decode("#1c0b2e"))
                .setDescription(
                        "**# General Stats:**" +
                                "These are all the stat about your public lobby history\n" +
                                " * **Lobbies Created: **" + stats.getLobbiesCreatedGeneral() + "\n" +
                                " * **Lobbies Joined: **" + stats.getLobbiesJoinedGeneral() + "\n" +
                                " * **Host Accepted Users: **" + stats.getHostAcceptedUserGeneral() + "\n" +
                                " * **Was Accepted: **" + stats.getWasAcceptedGeneral() + "\n" +
                                " * **Declined Users: **" + stats.getDeclinedUserGeneral() + "\n" +
                                " * **Was Declined: **" + stats.getWasDeclinedGeneral() + "\n" +
                                " * **Ignored Requests: **" + stats.getIgnoredRequestGeneral() + "\n" +
                                " * **Lobbies Completed: **" + stats.getLobbiesCompletedGeneral() + "\n" +
                                " * **Lobbies Incomplete: **" + stats.getLobbiesIncompleteGeneral() + "\n" +
                                " * **Lobbies Disbanded: **" + stats.getLobbiesDisbandedGeneral() + "\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                );
    }

    private EmbedBuilder getDirectStatsEmbed(PlayerStats stats) {
        return new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ Your Direct Stats ▬▬▬▬▬▬")
                .setColor(Color.decode("#1c0b2e"))
                .setDescription(
                        "**# Direct Stats:**" +
                                "These are all the stat about your private lobby history\n" +
                                " * **Lobbies Created: **" + stats.getLobbiesCreatedDirect() + "\n" +
                                " * **Lobbies Joined: **" + stats.getLobbiesJoinedDirect() + "\n" +
                                " * **Was Accepted: **" + stats.getWasAcceptedDirect() + "\n" +
                                " * **Declined Users: **" + stats.getDeclinedUserDirect() + "\n" +
                                " * **Was Declined: **" + stats.getWasDeclinedDirect() + "\n" +
                                " * **Ignored Requests: **" + stats.getIgnoredRequestDirect() + "\n" +
                                " * **Lobbies Completed: **" + stats.getLobbiesCompletedDirect() + "\n" +
                                " * **Lobbies Incomplete: **" + stats.getLobbiesIncompleteDirect() + "\n" +
                                " * **Lobbies Disbanded: **" + stats.getLobbiesDisbandedDirect() + "\n" +
                                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                );
    }

}