package PlayerInfo;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;
import java.util.Objects;

public class AddInfoCardCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "add_info_card" -> handleAddInfoCard(event);
            case "edit_ninja_card" -> handleEditInfoCard(event);
        }
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
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferReply(true).queue();
        event.getHook().editOriginalEmbeds(intro.build())
                .setActionRow(
                        StringSelectMenu.create("select_game")
                                .setPlaceholder("Choose your game")
                                .addOption("Storm Connections", "Storm Connections")
                                .addOption("Storm Evolution", "Storm Evolution")
                                .addOption("Storm 4", "Storm 4")
                                .addOption("Storm Revolution", "Storm Revolution")
                                .addOption("Storm Trilogy", "Storm Trilogy")
                                .build()
                ).queue();
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
                player.setGame(event.getValues().get(0));
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
                                .addOption("JP", "JP")
                                .addOption("SA", "SA")
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
        PlayerInfoStorage.printAllPlayers();

        event.reply("✅ Your profile has been successfully updated!").setEphemeral(true).queue();

        player.sendPlayerInfoLog(Objects.requireNonNull(event.getGuild()));
    }
}
