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
import java.util.HashMap;
import java.util.Map;

public class AddInfoCardCommand extends ListenerAdapter {

    private final Map<Long, PlayerInfo> sessions = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("add_info_card")) return;

        User user = event.getUser();
        long discordId = user.getIdLong();

        PlayerInfo player = new PlayerInfo();
        player.setDiscordId(discordId);
        player.setDiscordUsername(user.getName());
        player.setLobbyCounter(0);

        sessions.put(discordId, player);

        System.out.println("New player Discord ID: " + discordId);

        EmbedBuilder intro = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬ Create Your Player info Ninja Card ▬▬▬▬▬▬▬▬")
                .setDescription(" > This command helps you create your profile to join lobbies and participate in events.\n\n" +
                        "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
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
        PlayerInfo player = sessions.get(discordId);

        switch (event.getComponentId()) {
            case "select_game" -> {
                player.setGame(event.getValues().get(0));
                event.deferEdit().queue();
                askConnectionType(event);
            }
            case "select_connection" -> {
                player.setConnectionType(event.getValues().get(0));
                event.deferEdit().queue();
                askCurrentRegion(event);
            }
            case "select_region" -> {
                player.setCurrentRegion(event.getValues().get(0));
                event.deferEdit().queue();
                askTargetRegion(event);
            }
            case "select_target_region" -> {
                player.setTargetRegion(event.getValues().get(0));
                event.deferEdit().queue();
                askLanguages(event);
            }
            case "select_languages" -> {
                player.setSpokenLanguages(event.getValues().toArray(new String[0]));
                askFinalModal(event); // replyModal può essere chiamato qui e questa linea manda il modal alla fine dei vari menu
            }
        }
    }
    // vari embeded da chiamare
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
                                .addOption("Ethernet", "Ethernet")
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
                .setDescription(" > Which region do you want to face?" +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
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
   // modal finale emebeded
    private void askFinalModal(StringSelectInteractionEvent event) {
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
                .setPlaceholder("e.g. Weekend, monday, 6 pm etc etc")
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
        PlayerInfo player = sessions.get(discordId);

        player.setPlayerName(event.getValue("player_name").getAsString());

        String hoursInput = event.getValue("hours_played").getAsString();
        try {
            int hours = Integer.parseInt(hoursInput);
            player.setInGamePlayTime(hours + "");
        } catch (NumberFormatException e) {
            event.reply("❌ Player ninja card failed to create, please enter a valid number for hours played.").setEphemeral(true).queue();
            return;
        }

        player.setAvailablePlayTime(event.getValue("available_time").getAsString());

        System.out.println("✅ Player Profile Completed:");
        System.out.println("Discord ID: " + player.getDiscordId());
        System.out.println(player);

        event.reply("✅ Your player profile has been saved successfully!").setEphemeral(true).queue();
    }
}
