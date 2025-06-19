package Lobby;

import Stat.PlayerStatMongoDBManager;
import Stat.PlayerStats;
import Stat.PlayerStatsManager;
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
import java.util.HashMap;
import java.util.Map;

public class ButtonLobbyManager extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.startsWith("join_lobby_")) {
            Member joiner = event.getMember();
            Guild guild = event.getGuild();
            if (joiner == null || guild == null) return;

            long playerInfoRoleId = 1382385471300304946L;
            Role playerInfoRole = guild.getRoleById(playerInfoRoleId);
            if (playerInfoRole == null) {
                event.reply("❌ Configuration error: required role not found.").setEphemeral(true).queue();
                return;
            }

            if (!joiner.getRoles().contains(playerInfoRole)) {
                event.reply("❌ To use this system, you must create your **Player Ninja Card** first using `/add_info_card`.").setEphemeral(true).queue();
                return;
            }

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

            Lobby lobby = LobbyManager.getLobby(Long.parseLong(creatorId));
            if (lobby == null) return;

            if (lobby.isUserBlocked(joiner.getIdLong())) {
                event.reply("❌ You have been blocked by the lobby creator. You cannot join.").setEphemeral(true).queue();
                return;
            }

            // Get PlayerStatsManager and load stats
            PlayerStatsManager pm = PlayerStatsManager.getInstance();

            PlayerStats hostStats = pm.getPlayerStats(creator.getIdLong());
            if (hostStats == null) {
                hostStats = new PlayerStats();
                hostStats.setDiscordId(creator.getIdLong());
            }

            PlayerStats joinerStats = pm.getPlayerStats(joiner.getIdLong());
            if (joinerStats == null) {
                joinerStats = new PlayerStats();
                joinerStats.setDiscordId(joiner.getIdLong());
            }

            // PRIVATE LOBBY LOGIC
            if (lobby.isDirectLobby()) {
                if (lobby.getAllowedUserId() != joiner.getIdLong()) {
                    event.reply("❌ You are not allowed to join this private lobby.").setEphemeral(true).queue();
                    return;
                }

                // Update stats
                hostStats.incrementIgnoredRequestDirect();       // 👈 Host può ignorare
                joinerStats.incrementLobbiesJoinedDirect();       // 👈 Joiner ha tentato join direct

                pm.addOrUpdatePlayerStats(hostStats);
                pm.addOrUpdatePlayerStats(joinerStats);

                PlayerStatMongoDBManager.updatePlayerStats(hostStats);
                PlayerStatMongoDBManager.updatePlayerStats(joinerStats);

                // Add participant
                lobby.getPartecipants().add(joiner.getIdLong());

                TextChannel privateChannel = guild.getTextChannelById(lobby.getPrivateChannelId());
                if (privateChannel != null) {
                    privateChannel.getManager()
                            .putPermissionOverride(joiner, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .queue();

                    event.reply("✅ You have successfully joined the private lobby.").setEphemeral(true).queue();
                    privateChannel.sendMessage("✅ " + joiner.getAsMention() + " has joined the private lobby.").queue();
                } else {
                    event.reply("❌ Private channel not found.").setEphemeral(true).queue();
                }
                return;
            }

            // NORMAL LOBBY LOGIC
            hostStats.incrementIgnoredRequestGeneral();      // 👈 Host può ignorare
            joinerStats.incrementLobbiesJoinedGeneral();     // 👈 Joiner ha tentato join general

            pm.addOrUpdatePlayerStats(hostStats);
            pm.addOrUpdatePlayerStats(joinerStats);

            PlayerStatMongoDBManager.updatePlayerStats(hostStats);
            PlayerStatMongoDBManager.updatePlayerStats(joinerStats);

            System.out.println("✅ Lobby answered incremented for player: " + lobby.getDiscordId());

            event.reply("✅ Request sent to the lobby owner. Please wait for approval.").setEphemeral(true).queue();

            TextChannel privateChannel = guild.getTextChannelById(lobby.getPrivateChannelId());
            if (privateChannel != null) {
                privateChannel.sendMessage("🎮 The player **" + joiner.getEffectiveName() + "** wants to join your lobby, " + creator.getAsMention() + ".")
                        .addActionRow(
                                Button.success("accept_" + joiner.getId(), "✅ Accept"),
                                Button.danger("decline_" + joiner.getId(), "❌ Decline")
                        ).queue();
            }


    } else if (componentId.startsWith("decline_")) {
            String playerId = componentId.replace("decline_", "");
            User declinedUser = event.getJDA().getUserById(playerId);
            Member creator = event.getMember();
            Guild guild = event.getGuild();

            if (declinedUser == null || creator == null || guild == null) {
                event.reply("❌ Could not process decline.").setEphemeral(true).queue();
                return;
            }

            Lobby lobby = LobbyManager.getLobby(creator.getIdLong());
            if (lobby == null) {
                event.reply("❌ Could not find the related lobby.").setEphemeral(true).queue();
                return;
            }

            long threadPostId = lobby.getPostId();
            ThreadChannel thread = guild.getThreadChannelById(threadPostId);
            if (thread == null) {
                event.reply("❌ Could not find the forum thread for this lobby.").setEphemeral(true).queue();
                return;
            }

            // ===== STATS UPDATE START =====
            PlayerStatsManager statsManager = PlayerStatsManager.getInstance();

            // Statistiche dell'host (chi rifiuta)
            PlayerStats hostStats = statsManager.getPlayerStats(creator.getIdLong());
            if (hostStats == null) {
                hostStats = new PlayerStats();
                hostStats.setDiscordId(creator.getIdLong());
            }

            // Statistiche dell'utente rifiutato
            PlayerStats declinedStats = statsManager.getPlayerStats(declinedUser.getIdLong());
            if (declinedStats == null) {
                declinedStats = new PlayerStats();
                declinedStats.setDiscordId(declinedUser.getIdLong());
            }

            // Incrementa i contatori corretti in base al tipo di lobby
            if (lobby.isDirectLobby()) {
                declinedStats.incrementDeclinedUserDirect();        // Chi ha cliccato "decline" ha rifiutato
                hostStats.incrementWasDeclinedDirect();
            } else {
                hostStats.incrementDeclinedUserGeneral();
                declinedStats.incrementWasDeclinedGeneral();
            }

            // Aggiorna le statistiche nel database
            PlayerStatMongoDBManager.updatePlayerStats(hostStats);
            PlayerStatMongoDBManager.updatePlayerStats(declinedStats);
            // ===== STATS UPDATE END =====

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("▬▬▬▬ Join Request Declined ▬▬▬▬")
                    .setDescription("Player " + declinedUser.getAsMention() + " has been declined by " + creator.getAsMention() + ".")
                    .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                    .setColor(Color.decode("#1c0b2e"));

            event.deferEdit().queue(success -> {
                event.getMessage().delete().queue();
                thread.sendMessageEmbeds(embed.build()).queue();
            });

        } else if (componentId.startsWith("accept_")) {
            String playerId = componentId.replace("accept_", "");
            User acceptedUser = event.getJDA().getUserById(playerId);
            Guild guild = event.getGuild();
            Member creator = event.getMember();

            if (acceptedUser == null || guild == null || creator == null) {
                event.reply("❌ Missing information.").setEphemeral(true).queue();
                return;
            }

            // Lobby owner
            Lobby lobby = LobbyManager.getLobby(creator.getIdLong());
            if (lobby == null) {
                event.reply("❌ Lobby not found.").setEphemeral(true).queue();
                return;
            }

            long userIdLong = Long.parseLong(playerId);

            if (lobby.getPartecipants().contains(userIdLong)) {
                event.reply("⚠️ This player is already accepted in the lobby.").setEphemeral(true).queue();
                return;
            }

            // Stats update for host (creator) and accepted user
            PlayerStatsManager pm = PlayerStatsManager.getInstance();

            // Host stats
            PlayerStats hostStats = pm.getPlayerStats(creator.getIdLong());
            if (hostStats == null) {
                hostStats = new PlayerStats();
                hostStats.setDiscordId(creator.getIdLong());
                pm.addOrUpdatePlayerStats(hostStats);
            }

            // Accepted user stats
            PlayerStats acceptedStats = pm.getPlayerStats(userIdLong);
            if (acceptedStats == null) {
                acceptedStats = new PlayerStats();
                acceptedStats.setDiscordId(userIdLong);
                pm.addOrUpdatePlayerStats(acceptedStats);
            }

            // Increment correct stats based on lobby type
            if (lobby.isDirectLobby()) {
                acceptedStats.incrementWasAcceptedDirect();
                hostStats.decrementIgnoredRequestDirect();
            } else {
                hostStats.incrementHostAcceptedUserGeneral();
                hostStats.decrementIgnoredRequestGeneral();
                acceptedStats.incrementWasAcceptedGeneral();
            }

            PlayerStatMongoDBManager.updatePlayerStats(hostStats);
            PlayerStatMongoDBManager.updatePlayerStats(acceptedStats);

            // Add user to lobby participants
            lobby.getPartecipants().add(userIdLong);

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

            privateChannel.getManager()
                    .putPermissionOverride(acceptedMember, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                    .queue();

            event.deferEdit().queue(success -> {
                event.getMessage().delete().queue();
                privateChannel.sendMessage("✅ " + acceptedMember.getAsMention() + " has been accepted by " + creator.getAsMention() + ".").queue();
            });
        }

    }
}
