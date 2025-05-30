package Log;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceStreamEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Logger extends ListenerAdapter {
    @Override
    public void onUserUpdateAvatar(@NotNull UserUpdateAvatarEvent event) {
        User user = event.getUser();
        String newAvatarUrl = event.getNewAvatarUrl();       // Il nuovo avatar
        String timeFormatted = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("▬▬ User Avatar Updated ▬▬")
                .setColor(Color.decode("#ff6347"))
                .setDescription(
                        "**" + user.getAsMention() + "** has updated his avatar!\n\n" +
                                "> **Time:** `" + timeFormatted + "`\n" +
                                "> **User ID:** `" + event.getUser().getId() + "`"
                )
                .setImage(newAvatarUrl)
                .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");


        // Ottieni il canale tramite l'ID (sostituisci con l'ID del canale desiderato)
        TextChannel logChannel = event.getJDA().getTextChannelById(1370349729182908566L);

        if (logChannel != null) {
            logChannel.sendMessageEmbeds(embed.build()).queue();
        } else {
            System.out.println("Il canale non esiste o non è accessibile.");
        }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        Guild guild = event.getGuild();
        User user = event.getUser();

        // Get the log channel (replace with your actual channel ID)
        TextChannel logChannel = guild.getTextChannelById(1370403981255377058L);

        if (logChannel != null) {
            // Retrieve audit logs for "kick" action
            guild.retrieveAuditLogs()
                    .type(ActionType.KICK)
                    .limit(5) // Checking the last 5 kick events
                    .queue(logs -> {
                        boolean wasKicked = false;

                        // Check if the user was kicked
                        for (AuditLogEntry entry : logs) {
                            if (entry.getTargetId().equals(user.getId())) {
                                // Check if the kick happened recently (within 5 seconds)
                                OffsetDateTime time = entry.getTimeCreated();
                                OffsetDateTime now = OffsetDateTime.now();
                                if (time.plusSeconds(5).isAfter(now)) {
                                    wasKicked = true;
                                    break; // Exit loop once we find the kick
                                }
                            }
                        }

                        // Create embed for kick or left event
                        MessageEmbed embed;
                        if (wasKicked) {
                            // If the user was kicked
                            embed = new EmbedBuilder()
                                    .setTitle("Player Kicked")
                                    .setDescription(user.getAsTag() + " has been kicked from the server.")
                                    .setColor(Color.RED)
                                    .setTimestamp(OffsetDateTime.now())
                                    .setFooter(user.getName(), user.getAvatarUrl()) // Foto profilo nel footer
                                    .build();
                        } else {
                            // If the user left voluntarily
                            embed = new EmbedBuilder()
                                    .setTitle("Member Banned from the Server")
                                    .setDescription(user.getAsTag() + " has get banned.")
                                    .setColor(Color.red)
                                    .setTimestamp(OffsetDateTime.now())
                                    .setFooter(user.getName(), user.getAvatarUrl()) // Foto profilo nel footer
                                    .build();
                        }

                        // Send embed to the log channel
                        logChannel.sendMessageEmbeds(embed).queue();
                    });
        } else {
            System.out.println("Log channel not found!");
        }
    }

    @Override
    public void onGuildVoiceStream(@NotNull GuildVoiceStreamEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        boolean isStreaming = event.getVoiceState().isStream();

        TextChannel logChannel = guild.getTextChannelById(1370410340331945994L); // Replace with your log channel ID

        if (logChannel != null) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(isStreaming ? Color.green : Color.red)
                    .setTitle("VC Stream Status Updated")
                    .setDescription("> **" + member.getAsMention() + "** " +
                            (isStreaming ? "started streaming!" : "stopped streaming."))
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter(member.getNickname(), member.getAvatarUrl());

            logChannel.sendMessageEmbeds(embed.build()).queue();
        }
    }

}
