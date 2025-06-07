package Lobby;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

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
                event.reply("❌ You cannot join your own lobby.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            event.reply("✅ Request sent to the lobby owner. Please wait for approval.")
                    .setEphemeral(true)
                    .queue();

            Lobby lobby = LobbyManager.getLobby(Long.parseLong(creatorId));
            if (lobby == null) return;

            TextChannel privateChannel = guild.getTextChannelById(lobby.getPrivateChannelId());
            if (privateChannel != null) {
                privateChannel.sendMessage("🎮 Player **" + joiner.getEffectiveName() + "** wants to join your lobby.")
                        .addActionRow(
                                Button.success("accept_" + joiner.getId(), "✅ Accept"),
                                Button.danger("decline_" + joiner.getId(), "❌ Decline")
                        )
                        .queue();
            }

        } else if (componentId.startsWith("accept_")) {
            String playerId = componentId.replace("accept_", "");
            User acceptedUser = event.getJDA().getUserById(playerId);
            Guild guild = event.getGuild();
            Member creator = event.getMember();
            if (acceptedUser == null || guild == null || creator == null) {
                event.reply("❌ Missing information.").setEphemeral(true).queue();
                return;
            }

            Lobby lobby = LobbyManager.getLobby(creator.getIdLong());
            if (lobby == null) {
                event.reply("❌ Lobby not found.").setEphemeral(true).queue();
                return;
            }

            TextChannel privateChannel = guild.getTextChannelById(lobby.getPrivateChannelId());
            if (privateChannel == null) {
                event.reply("❌ Private channel not found.").setEphemeral(true).queue();
                return;
            }

            Member acceptedMember = guild.getMemberById(playerId);
            if (acceptedMember == null) {
                event.reply("❌ Player not found.").setEphemeral(true).queue();
                return;
            }

            // Aggiungi i permessi
            privateChannel.getManager().putPermissionOverride(acceptedMember,
                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null).queue();

            // Disattiva i bottoni nel messaggio
            List<Button> disabledButtons = event.getMessage().getButtons().stream()
                    .map(Button::asDisabled)
                    .collect(Collectors.toList());

            event.editMessage("✅ " + acceptedMember.getAsMention() + " has been accepted by " + creator.getAsMention() + ".")
                    .setComponents(ActionRow.of(disabledButtons))
                    .queue();

            privateChannel.sendMessage("🎉 " + acceptedMember.getAsMention() + " has joined the lobby. Approved by " + creator.getAsMention() + "!").queue();

        } else if (componentId.startsWith("decline_")) {
            String playerId = componentId.replace("decline_", "");
            User declinedUser = event.getJDA().getUserById(playerId);
            Member creator = event.getMember();
            if (declinedUser == null || creator == null) {
                event.reply("❌ Could not process decline.").setEphemeral(true).queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🚫 Join Request Declined")
                    .setDescription("Player " + declinedUser.getAsMention() + " has been declined by " + creator.getAsMention() + ".")
                    .setColor(Color.RED);

            // Disattiva i bottoni nel messaggio
            List<Button> disabledButtons = event.getMessage().getButtons().stream()
                    .map(Button::asDisabled)
                    .collect(Collectors.toList());

            event.editMessage("❌ Request declined.")
                    .setComponents(ActionRow.of(disabledButtons))
                    .queue();

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
