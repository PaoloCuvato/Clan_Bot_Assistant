package Lobby;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.util.EnumSet;

public class ButtonLobbyManager extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.startsWith("join_lobby_")) {
            Member joiner = event.getMember();
            Guild guild = event.getGuild();
            if (joiner == null || guild == null) return;

            String creatorId = componentId.replace("join_lobby_", "");
            Member creator = guild.getMemberById(creatorId);
            if (creator == null) {
                event.reply("❌ Lobby creator not found.").setEphemeral(true).queue();
                return;
            }

            if (creatorId.equals(joiner.getId())) {
                event.reply("❌ You cannot join your own lobby.").setEphemeral(true).queue();
                return;
            }

            event.reply("✅ Request sent to the lobby owner. Please wait for approval.")
                    .setEphemeral(true).queue();

            Lobby lobby = LobbyManager.getLobby(Long.parseLong(creatorId));
            if (lobby == null) return;

            TextChannel privateChannel = guild.getTextChannelById(lobby.getPrivateChannelId());
            if (privateChannel != null) {
                privateChannel.sendMessage("🎮 The player **" + joiner.getEffectiveName() + "**, wants to join your lobby, " + creator.getAsMention() + ".")
                        .addActionRow(
                                Button.success("accept_" + joiner.getId(), "✅ Accept"),
                                Button.danger("decline_" + joiner.getId(), "❌ Decline")
                        )
                        .queue();
            }


// Parte declino modificata correttamente
        } else if (componentId.startsWith("decline_")) {
            String playerId = componentId.replace("decline_", "");
            User declinedUser = event.getJDA().getUserById(playerId);
            Member creator = event.getMember();
            Guild guild = event.getGuild();

            if (declinedUser == null || creator == null || guild == null) {
                event.reply("❌ Could not process decline.").setEphemeral(true).queue();
                return;
            }

            // Recupera la lobby del creator (o da chi gestisce il rifiuto)
            Lobby lobby = LobbyManager.getLobby(creator.getIdLong());
            if (lobby == null) {
                event.reply("❌ Could not find the related lobby.").setEphemeral(true).queue();
                return;
            }

            long threadPostId = lobby.getPostId(); // ✅ thread già esistente
            ThreadChannel thread = guild.getThreadChannelById(threadPostId);
            if (thread == null) {
                event.reply("❌ Could not find the forum thread for this lobby.").setEphemeral(true).queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("▬▬▬▬ Join Request Declined ▬▬▬▬")
                    .setDescription("Player " + declinedUser.getAsMention()
                            + " has been declined by " + creator.getAsMention() + ".")
                    .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                    .setColor(Color.decode("#1c0b2e"));

            event.deferEdit().queue(success -> {
                event.getMessage().delete().queue(); // elimina i bottoni
                thread.sendMessageEmbeds(embed.build()).queue(); // ✅ manda embed nel thread corretto
            });
        }

    }
}
