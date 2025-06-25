package PlayerInfo;

import Stat.PlayerStats;
import Stat.PlayerStatsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlashPlayerInfoManager extends ListenerAdapter {

    private final Map<Long, Long> messageIdToUserId = new ConcurrentHashMap<>();
    private final PlayerStatsManager statsManager = PlayerStatsManager.getInstance();

    private static SlashPlayerInfoManager instance;

    // Variabile per tenere traccia dell'ultimo userId per il menu dropdown
    private long lastQueriedUserId = -1;

    public static synchronized SlashPlayerInfoManager getInstance() {
        if (instance == null) {
            instance = new SlashPlayerInfoManager();
        }
        return instance;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (command.equals("my_ninjacard")) {
            long userId = event.getUser().getIdLong();
            PlayerInfo p = PlayerInfoStorage.getPlayerInfo(userId);
            PlayerStats stats = statsManager.getPlayerStats(userId);

            if (p == null || stats == null) {
                event.reply("❌ Player info or stats not found for you!").setEphemeral(true).queue();
                return;
            }

            lastQueriedUserId = userId;  // Salvo userId per il menu

            EmbedBuilder eb = buildDropdownEmbed();

            StringSelectMenu menu = StringSelectMenu.create("ninjacard:menu")
                    .setPlaceholder("Choose info to display")
                    .setMinValues(1)
                    .setMaxValues(1)
                    .addOption("Ninja Card", "ninjacard")
                    .addOption("General Match History", "general")
                    .addOption("Direct Match History", "direct")
                    .build();

            event.replyEmbeds(eb.build())
                    .addActionRow(menu)
                    .setEphemeral(true)
                    .queue();

        } else if (command.equals("search_ninjacard")) {
            OptionMapping targetOption = event.getOption("target");
            if (targetOption == null) {
                event.reply("❌ You must specify a user to search.").setEphemeral(true).queue();
                return;
            }

            User targetUser = targetOption.getAsUser();
            long targetUserId = targetUser.getIdLong();

            PlayerInfo p = PlayerInfoStorage.getPlayerInfo(targetUserId);
            PlayerStats stats = statsManager.getPlayerStats(targetUserId);

            if (p == null || stats == null) {
                event.reply("❌ This user has not created their Ninja Card yet.").setEphemeral(true).queue();
                return;
            }

            lastQueriedUserId = targetUserId;  // Salvo userId per il menu

            EmbedBuilder eb = buildDropdownEmbed();

            StringSelectMenu menu = StringSelectMenu.create("ninjacard:menu")
                    .setPlaceholder("Choose info to display")
                    .setMinValues(1)
                    .setMaxValues(1)
                    .addOption("Ninja Card", "ninjacard")
                    .addOption("General Match History", "general")
                    .addOption("Direct Match History", "direct")
                    .build();

            event.replyEmbeds(eb.build())
                    .addActionRow(menu)
                    .setEphemeral(true)
                    .queue();

        } else if (command.equals("send_player_info_file")) {
            event.deferReply().queue();

            java.io.File file = new java.io.File("playerinfolist.txt");

            if (!file.exists()) {
                event.getHook().sendMessage("❌ The file playerinfolist.txt was not found.").queue();
                return;
            }

            event.getHook().sendMessage("📄 Here is a text file containing all the users with the Player Info role:")
                    .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(file))
                    .queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("ninjacard:menu")) return;

        if (lastQueriedUserId == -1) {
            event.reply("❌ No user info found for this menu interaction. Please retry.").setEphemeral(true).queue();
            return;
        }

        PlayerInfo p = PlayerInfoStorage.getPlayerInfo(lastQueriedUserId);
        PlayerStats stats = statsManager.getPlayerStats(lastQueriedUserId);

        if (p == null || stats == null) {
            event.reply("❌ Player info or stats missing.").setEphemeral(true).queue();
            return;
        }

        String selected = event.getValues().get(0);

        EmbedBuilder embed;
        switch (selected) {
            case "ninjacard":
                embed = getNinjaCardEmbed(p);
                break;
            case "general":
                embed = getGeneralStatsEmbed(stats);
                break;
            case "direct":
                embed = getDirectStatsEmbed(stats);
                break;
            default:
                embed = new EmbedBuilder()
                        .setDescription("Invalid selection!")
                        .setColor(Color.RED);
        }

        // Cancella il messaggio del menu dropdown (ephemeral)
        event.getMessage().delete().queue();

        // Invia embed in DM all'utente che ha cliccato
        event.getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(embed.build()).queue();
        });
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;

        long messageId = event.getMessageIdLong();
        long userId = Long.parseLong(event.getUserId());

        if (!messageIdToUserId.containsKey(messageId)) return;

        long targetId = messageIdToUserId.get(messageId);

        PlayerInfo p = PlayerInfoStorage.getPlayerInfo(targetId);
        PlayerStats stats = statsManager.getPlayerStats(targetId);
        if (p == null || stats == null) return;

        String emoji = event.getReaction().getEmoji().getName();

        EmbedBuilder newEmbed;
        switch (emoji) {
            case "👤":
                newEmbed = getNinjaCardEmbed(p);
                break;
            case "🌍":
                newEmbed = getGeneralStatsEmbed(stats);
                break;
            case "🕹️":
                newEmbed = getDirectStatsEmbed(stats);
                break;
            default:
                return;
        }

        EmbedBuilder finalEmbed = newEmbed;
        event.retrieveMessage().queue(message -> {
            message.editMessageEmbeds(finalEmbed.build()).queue();
            event.getReaction().removeReaction(event.getUser()).queue();
        });
    }

    private EmbedBuilder buildDropdownEmbed() {
        return new EmbedBuilder()
                .setDescription(
                        "**Select one of the options below to view your stats:**\n" +
                                "> - Ninja Card\n" +
                                "> - General Match History\n" +
                                "> - Direct Match History"
                )
                .setColor(Color.decode("#1c0b2e"));
    }

    private EmbedBuilder getNinjaCardEmbed(PlayerInfo p) {
        return new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ Your Ninja Card ▬▬▬▬▬▬")
                .setColor(Color.decode("#1c0b2e"))
                .setDescription(
                        "**# Ninja Card Info:**\n" +
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
                        "**# General Stats:**\n" +
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
                        "**# Direct Stats:**\n" +
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
