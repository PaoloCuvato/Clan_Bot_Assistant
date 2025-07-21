package Lobby;

import Config.Config;
import Stat.PlayerStatMongoDBManager;
import Stat.PlayerStats;
import Stat.PlayerStatsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.EnumSet;

public class ButtonLobbyManager extends ListenerAdapter {
    Config config = new Config();
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        if (componentId.startsWith("join_lobby_")) {
            Member joiner = event.getMember();
            Guild guild = event.getGuild();
            if (joiner == null || guild == null) return;
            long playerInfoRoleId = Long.parseLong(config.getPlayerInfoRole());
            // long playerInfoRoleId = 1382385471300304946L;
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

            if (lobby.isDirectLobby()) {
                if (lobby.getAllowedUserId() != joiner.getIdLong()) {
                    event.reply("❌ You are not allowed to join this private lobby.").setEphemeral(true).queue();
                    return;
                }
                // hostStats.incrementIgnoredRequestDirect();
                joinerStats.incrementLobbiesJoinedDirect();
                joinerStats.incrementIgnoredRequestDirect();

                pm.addOrUpdatePlayerStats(hostStats);
                pm.addOrUpdatePlayerStats(joinerStats);
                PlayerStatMongoDBManager.updatePlayerStats(hostStats);
                PlayerStatMongoDBManager.updatePlayerStats(joinerStats);

                lobby.getPartecipants().add(joiner.getIdLong());

                GuildChannel rawChannel = guild.getGuildChannelById(lobby.getPrivateChannelId());

                if (rawChannel instanceof TextChannel privateChannel) {
                    privateChannel.getManager()
                            .putPermissionOverride(joiner, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .queue();

                    event.reply("✅ You have successfully joined the private lobby.").setEphemeral(true).queue();
                    privateChannel.sendMessage("✅ " + joiner.getAsMention() + " has joined the private lobby.").queue();

                } else if (rawChannel instanceof ThreadChannel threadChannel) {
                    threadChannel.addThreadMember(joiner.getUser()).queue(
                            success -> {
                                event.reply("✅ You have successfully joined the private lobby.").setEphemeral(true).queue();
                                threadChannel.sendMessage("✅ " + joiner.getAsMention() + " has joined the private lobby.").queue();
                            },
                            error -> {
                                event.reply("❌ Could not add you to the thread.").setEphemeral(true).queue();
                                System.err.println("❌ Error adding member to thread: " + error.getMessage());
                            }
                    );

                } else {
                    event.reply("❌ Private channel not found or unsupported type.").setEphemeral(true).queue();
                }
            }


            hostStats.incrementIgnoredRequestGeneral();
            joinerStats.incrementLobbiesJoinedGeneral();

            pm.addOrUpdatePlayerStats(hostStats);
            pm.addOrUpdatePlayerStats(joinerStats);
            PlayerStatMongoDBManager.updatePlayerStats(hostStats);
            PlayerStatMongoDBManager.updatePlayerStats(joinerStats);

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

            PlayerStatsManager statsManager = PlayerStatsManager.getInstance();
            PlayerStats hostStats = statsManager.getPlayerStats(creator.getIdLong());
            if (hostStats == null) {
                hostStats = new PlayerStats();
                hostStats.setDiscordId(creator.getIdLong());
            }

            PlayerStats declinedStats = statsManager.getPlayerStats(declinedUser.getIdLong());
            if (declinedStats == null) {
                declinedStats = new PlayerStats();
                declinedStats.setDiscordId(declinedUser.getIdLong());
            }

            if (lobby.isDirectLobby()) {
                declinedStats.incrementDeclinedUserDirect();
                hostStats.incrementWasDeclinedDirect();
            } else {
                hostStats.incrementDeclinedUserGeneral();
                declinedStats.incrementWasDeclinedGeneral();
            }

            PlayerStatMongoDBManager.updatePlayerStats(hostStats);
            PlayerStatMongoDBManager.updatePlayerStats(declinedStats);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("▬▬▬▬▬▬▬▬ Lobby Behavior ▬▬▬▬▬▬▬▬")
                    .setDescription("Player " + declinedUser.getAsMention() + " has been declined by " + creator.getAsMention() + ".")
                    .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
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

            PlayerStatsManager pm = PlayerStatsManager.getInstance();
            PlayerStats hostStats = pm.getPlayerStats(creator.getIdLong());
            if (hostStats == null) {
                hostStats = new PlayerStats();
                hostStats.setDiscordId(creator.getIdLong());
                pm.addOrUpdatePlayerStats(hostStats);
            }

            PlayerStats acceptedStats = pm.getPlayerStats(userIdLong);
            if (acceptedStats == null) {
                acceptedStats = new PlayerStats();
                acceptedStats.setDiscordId(userIdLong);
                pm.addOrUpdatePlayerStats(acceptedStats);
            }

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

            lobby.getPartecipants().add(userIdLong);

            GuildChannel rawChannel = guild.getGuildChannelById(lobby.getPrivateChannelId());
            Member acceptedMember = guild.getMemberById(playerId);
            if (rawChannel == null || acceptedMember == null) {
                event.reply("❌ Private channel or player not found.").setEphemeral(true).queue();
                return;
            }

            long threadPostId = lobby.getPostId();
            ThreadChannel thread = guild.getThreadChannelById(threadPostId);
            if (thread == null) {
                event.reply("❌ Could not find the forum thread for this lobby.").setEphemeral(true).queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("▬▬▬▬▬▬▬▬ Lobby Behavior ▬▬▬▬▬▬▬▬")
                    .setDescription("Player " + acceptedUser.getAsMention() + " has been accepted by " + creator.getAsMention() + ".")
                    .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                    .setColor(Color.decode("#1c0b2e"));

            // Gestione canale (TextChannel o ThreadChannel)
            event.deferEdit().queue(success -> {
                event.getMessage().delete().queue();

                if (rawChannel instanceof TextChannel privateChannel) {
                    privateChannel.getManager()
                            .putPermissionOverride(acceptedMember, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .queue();

                    privateChannel.sendMessage("✅ " + acceptedMember.getAsMention() + " has been accepted by " + creator.getAsMention() + ".").queue();
                } else if (rawChannel instanceof ThreadChannel threadChannel) {
                    threadChannel.addThreadMember(acceptedUser).queue();
                    threadChannel.sendMessage("✅ " + acceptedMember.getAsMention() + " has been accepted by " + creator.getAsMention() + ".").queue();
                }

                thread.sendMessageEmbeds(embed.build()).queue();
            });
        }


        // ✅ LOGICA AGGIUNTA PER YES / NO
        if (componentId.startsWith("lobby_add_yes:") || componentId.startsWith("lobby_add_no:")) {
            String[] parts = componentId.split(":");
            boolean accepted = parts[0].endsWith("_yes");
            long ownerId = Long.parseLong(parts[1]);
            long userId = Long.parseLong(parts[2]);

            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("❌ Guild not found.").setEphemeral(true).queue();
                return;
            }

            Lobby lobby = LobbyManager.getLobby(ownerId);
            if (lobby == null) {
                event.reply("❌ Lobby not found.").setEphemeral(true).queue();
                return;
            }

            GuildChannel priv = guild.getGuildChannelById(lobby.getPrivateChannelId());
            Member invited = guild.getMemberById(userId);
            if (priv == null || invited == null) {
                event.reply("❌ Channel or member missing.").setEphemeral(true).queue();
                return;
            }

            PlayerStatsManager pm = PlayerStatsManager.getInstance();

            // Stats creatore
            PlayerStats creatorStats = pm.getPlayerStats(ownerId);
            if (creatorStats == null) {
                creatorStats = new PlayerStats();
                creatorStats.setDiscordId(ownerId);
                pm.addOrUpdatePlayerStats(creatorStats);
            }

            // Stats invitato
            PlayerStats invitedStats = pm.getPlayerStats(userId);
            if (invitedStats == null) {
                invitedStats = new PlayerStats();
                invitedStats.setDiscordId(userId);
                pm.addOrUpdatePlayerStats(invitedStats);
            }

            if (accepted) {
                // ✅ Incrementa accepted per creatore e joined per invitato
                creatorStats.incrementWasAcceptedDirect();
                invitedStats.incrementLobbiesJoinedDirect();
                invitedStats.decrementIgnoredRequestDirect();

                PlayerStatMongoDBManager.updatePlayerStats(creatorStats);
                PlayerStatMongoDBManager.updatePlayerStats(invitedStats);

                if (priv instanceof TextChannel textChannel) {
                    textChannel.getManager()
                            .putPermissionOverride(invited,
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND),
                                    EnumSet.noneOf(Permission.class))
                            .queue(v -> {
                                lobby.getPartecipants().add(userId);
                                event.reply("✅ You have joined!").setEphemeral(true).queue();
                                textChannel.sendMessage(invited.getAsMention() + " has joined the lobby.").queue();
                            }, failure -> event.reply("❌ Failed to grant permissions.").setEphemeral(true).queue());
                } else if (priv instanceof ThreadChannel threadChannel) {
                    threadChannel.addThreadMember(invited.getUser()).queue(v -> {
                        lobby.getPartecipants().add(userId);
                        event.reply("✅ You have joined!").setEphemeral(true).queue();
                        threadChannel.sendMessage(invited.getAsMention() + " has joined the lobby.").queue();
                    }, failure -> event.reply("❌ Failed to add to thread.").setEphemeral(true).queue());
                } else {
                    event.reply("❌ Unsupported channel type.").setEphemeral(true).queue();
                }

            } else {
                // ✅ Incrementa declined per creatore
                creatorStats.incrementWasDeclinedDirect();
                invitedStats.incrementDeclinedUserDirect();
                invitedStats.decrementIgnoredRequestDirect();

                PlayerStatMongoDBManager.updatePlayerStats(creatorStats);
                PlayerStatMongoDBManager.updatePlayerStats(invitedStats);

                if (priv instanceof TextChannel textChannel) {
                    textChannel.getManager()
                            .putPermissionOverride(invited,
                                    EnumSet.noneOf(Permission.class),
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND))
                            .queue(v -> {
                                lobby.blockUser(userId);
                                event.reply("❌ You declined the invitation.").setEphemeral(true).queue();
                                textChannel.sendMessage(invited.getAsMention() + " has declined the invitation.").queue();
                            }, failure -> event.reply("❌ Failed to update permissions.").setEphemeral(true).queue());
                } else if (priv instanceof ThreadChannel threadChannel) {
                    // Non si possono togliere permessi in un ThreadChannel, quindi solo blocco e messaggio
                    lobby.blockUser(userId);
                    event.reply("❌ You declined the invitation.").setEphemeral(true).queue();
                    threadChannel.sendMessage(invited.getAsMention() + " has declined the invitation.").queue();
                } else {
                    event.reply("❌ Unsupported channel type.").setEphemeral(true).queue();
                }
            }
            return;
        }



        Guild guild = event.getGuild();
        if (guild == null) return;

        GuildChannel genericChannel = (GuildChannel) event.getChannel();

        TextChannel channel = null;
        if (genericChannel instanceof TextChannel) {
            channel = (TextChannel) genericChannel;
        } else if (genericChannel instanceof ThreadChannel thread) {
            var parent = thread.getParentChannel();
            if (parent instanceof TextChannel parentTextChannel) {
                channel = parentTextChannel;
            }
        }

        if (channel == null) {
            event.reply("❌ Impossibile determinare il canale di testo o il suo genitore.")
                    .setEphemeral(true).queue();
            return;
        }

        Category category = channel.getParentCategory();
        if (category == null || (!category.getId().equals(config.getDesputesCategory()) && !category.getName().equalsIgnoreCase("Ninja Disputes"))) {
            // Canale non nella categoria giusta, esci senza fare nulla
            return;
        }

        String topic = channel.getTopic();
        if (topic == null || !topic.contains("Ticket Owner ID:")) {
            event.deferReply(true).queue();
            event.getHook().sendMessage("❌ Ticket owner ID non trovato nel topic.").queue();
            return;
        }

        String userId = topic.replaceAll(".*Ticket Owner ID:\\s*", "").trim();
        Member ticketOwner = guild.getMemberById(userId);
        if (ticketOwner == null) {
            event.reply("❌ Impossibile trovare il proprietario del ticket.")
                    .setEphemeral(true).queue();
            return;
        }

        switch (componentId) {
            case "ticket:close":
                channel.getPermissionOverride(ticketOwner).getManager()
                        .deny(Permission.VIEW_CHANNEL).queue();

                channel.getManager().setName("closed-" + channel.getName()).queue();

                event.reply("🔒 Ticket chiuso.")
                        .addActionRow(Button.success("ticket:reopen", "Riapri Ticket")).queue();
                break;

            case "ticket:reopen":
                channel.getPermissionOverride(ticketOwner).getManager()
                        .grant(Permission.VIEW_CHANNEL).queue();
                event.reply("🔓 Ticket riaperto.").queue();
                break;

            default:
                // Gestione altri bottoni o dropdown se serve
                break;
        }
    }
}