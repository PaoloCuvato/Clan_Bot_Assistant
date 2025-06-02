package PlayerInfo;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

public class SlashPlayerInfoManager extends ListenerAdapter {
    private static final Map<Long, PlayerInfo> playerSessions= PlayerInfoStorage.getAllSessions();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        long userId = event.getMember().getIdLong();

        if (command.equals("my_ninjacard")) {
            PlayerInfo p = playerSessions.get(userId);

            if (p == null) {
                event.reply("❌ No Ninja Card data found for you. Use `/add_info_card` to create one or contact an admin if the issue persists.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("▬▬▬▬▬▬ Ninja Card ▬▬▬▬▬▬")
                    .setDescription(
                                    "** # Ninja Card Info:** " +
                                    "These are all the stat about you\n" +
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
                                    "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                    )
                    .setColor(Color.decode("#1c0b2e"))
                    .setTimestamp(Instant.now());

            event.replyEmbeds(eb.build())
                    .setEphemeral(true)
                    .queue();
        }
              // search part
        if (command.equals("search_ninjacard")) {
            OptionMapping targetOption = event.getOption("target");
            if (targetOption == null) {
                event.reply("❌ You must specify a user to search.").setEphemeral(true).queue();
                return;
            }

            User targetUser = targetOption.getAsUser();
            long targetUserId = targetUser.getIdLong();

            PlayerInfo p = PlayerInfoStorage.getPlayerInfo(targetUserId);

            if (p == null) {
                event.reply("❌ This user has not created their Ninja Card yet.").setEphemeral(true).queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("▬▬▬▬▬▬ Ninja Card ▬▬▬▬▬▬")
                    .setDescription(
                            "** # Ninja Card Info:** " +
                                    "These are all the stat about you\n" +
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
                                    "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                    )
                    .setColor(Color.decode("#1c0b2e"))
                    .setTimestamp(Instant.now());

            event.replyEmbeds(eb.build())
                    .setEphemeral(true)
                    .queue();
        }

    }

}
