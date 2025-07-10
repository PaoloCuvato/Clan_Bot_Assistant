package Command;

import ClanManager.Clan;
import ClanManager.ClanService;
import ClanManager.ClanStorage;
import Config.Config;
import MongoDB.MongoDBManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.dv8tion.jda.api.entities.*;

import static ClanManager.ClanStorage.*;

public class ManagerCommands extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        // Handling the /info command
        if (command.equals("info")) {
            if (event.getChannel() instanceof TextChannel) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("▬▬▬▬▬▬▬▬▬▬ Info ▬▬▬▬▬▬▬▬▬▬▬");
                builder.setDescription(" > **The purpose of this bot is to manage and simplify the administration of Storm Clan Related things.**" +
                        "\n\n > **The bot is created by Azrael.**\n\n"
                        + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                builder.setImage("https://media1.tenor.com/m/4XzoCqoNqjQAAAAd/uzumaki-clan.gif");
                builder.setColor(Color.decode("#1C638C"));
                event.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
        }

        // Handling the /clear command
        if (command.equals("clear") && (Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR))) {
            if (event.getChannel() instanceof TextChannel) {
                Integer numberOfMessagesToDelete = event.getOption("message").getAsInt();
                TextChannel channel = (TextChannel) event.getChannel();
                channel.getHistory().retrievePast(numberOfMessagesToDelete).queue(messages -> {
                    channel.purgeMessages(messages);
                    event.reply("**Chat cleared**.").setEphemeral(true).queue();
                });
            } else {
                event.reply("**This command can only be used in text channels.**").queue();
            }
        }

        // Handling the /commands command
        if (command.equals("commands")) {
            if (event.getChannel() instanceof TextChannel) {
                TextChannel channel = (TextChannel) event.getChannel();
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("▬▬▬▬▬▬▬▬▬ All Commands ▬▬▬▬▬▬▬▬▬");

                builder.setDescription("### __Commands only `@everyone` can use__" +
                                       "\n - __*/add_info_card*__ - This command allows you to create a personalized player card for server matchmaking."+
                                       "\n - __*/search_ninjacard*__ - This command allows you to search up any users registered ninja info card in this server."+
                                       "\n - __*/clan_member_list*__ - This command allows you to search up members of a registered clan in this server."+
                                       "\n - __*/commands*__ - This command sends a view only message detailing every perk that can be used with the server bot."+
                                       "\n - __*/list_all_clan*__ - This command will send you a list of every registered clan in this server.\n"+

                                        "### __Commands only `@Ninja Assembly/RN` can use__"+
                                        "\n - __*/freestyle*__ - This command allows you to create public in server PVP lobbies for https://discord.com/channels/420393601176961025/1389588609295712357 & https://discord.com/channels/420393601176961025/1391434101059354804."+
                                        "\n - __*/direct*__ - This command allows you to create private in server PVP lobbies."+
                                        "\n - __*/my_ninjacard*__ - This command sends a drop down allowing you to look at your ninja info card and lobby stats of the server."+
                                        "\n - __*/retire*__ - This command is for players that want to retire their solo or clan journey in this server.\n"+

                                        "### __Commands only `@Ninja Hideout` can use__"+
                                        "\n - __*/add_user_lobby*__ - This allows the host of a lobby to invite registered players of this server to their direct lobby."+
                                        "\n - __*/leave_lobby*__ - This command allows the host of a lobby to leave before someone joined."+
                                        "\n - __*/results*__ - This command allows the host of a lobby to mark the results between all participants.\n"+

                                        "### __Commands only `@Ninja Proctor` can use__"+
                                        "\n - __*/edit_wins*__ - This command allows the lobby referee to edit the win score of a player."+
                                        "\n - __*/edit_losses*__ - This command allows the lobby referee to edit the lose score of a player."+
                                        "\n - __*/results*__ - This command allows the lobby referee to mark the results between participants.\n"+

                                        "### __Commands only `@Clan Leader` can use__"+
                                        "\n - __*/register_clan*__ - This command allows a player to create a guild for this server."+
                                        "\n - __*/add_clan_member*__ - This command allows a user to add members of this server to their registered clan."+
                                        "\n - __*/edit_clan_name*__ - This command allows a user to edit the name of their registered clan."+
                                        "\n - __*/kick_clan_member*__ - This command allows a user to kick a player out of their registered clan."+
                                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

                builder.setImage("https://media1.tenor.com/m/R8CSlK2ys1AAAAAd/sasuke-scroll.gif");
                builder.setColor(Color.white);
                event.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
        }

        // Handling the clan command

        if (command.equals("register_clan")) {
            if (event.getMember() == null) {
                event.reply("You don't have the required permissions to register a clan.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            long userId = event.getUser().getIdLong();

            // Creazione istanza di ClanService
            ClanService clanService = ClanService.getInstance();

            // Controlla se è già leader di un clan
            if (clanService.isClanLeaderInAnyClan(userId)) {
                event.reply("❌ You are already leading a clan and cannot create another one.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String clanName = event.getOption("name").getAsString();

            // Controlla se il nome è già preso
            if (clanService.getClanMap().containsKey(clanName)) {
                event.reply("❌ A clan with that name already exists. Please choose a different name.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            Clan clan = new Clan(
                    clanName,
                    String.valueOf(userId),
                    new ArrayList<>(),
                    0,
                    0,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM:dd:yyyy HH:mm:ss"))
            );

            clanService.addOrUpdateClan(clan);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("▬▬▬▬ Clan Registered Successfully! ▬▬▬▬")
                    .setColor(Color.GREEN)
                    .setDescription("**The following clan has been registered successfully!**")
                    .addField("**Clan Name**", clan.getName(), true)
                    .addField("**Created By**", event.getUser().getAsTag(), true)
                    .addField("**Creation Date**", clan.getFormattedCreationDate(), false)
                    .addField("**Victories**", String.valueOf(clan.getWins()), true)
                    .addField("**Losses**", String.valueOf(clan.getLosses()), true)
                    .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

            event.replyEmbeds(embed.build()).setEphemeral(true).queue(success -> {
                Guild guild = event.getGuild();
                Member member = event.getMember();

                if (guild != null && member != null) {
                    Role clanLeaderRole = guild.getRoleById(735017246786715709L);
                    if (clanLeaderRole != null) {
                        guild.addRoleToMember(member, clanLeaderRole).queue(
                                v -> System.out.println("[Info] Clan Leader role assigned to " + member.getEffectiveName()),
                                err -> System.err.println("❌ Failed to assign Clan Leader role: " + err.getMessage())
                        );
                    } else {
                        System.err.println("❌ Clan Leader role not found.");
                    }

                    TextChannel logChannel = guild.getTextChannelById(1391769296975429742L);
                    if (logChannel != null) {
                        logChannel.sendMessageEmbeds(embed.build()).queue();
                    } else {
                        System.err.println("❌ Log channel not found.");
                    }
                }
            });

        } else if (command.equals("add_clan_member")) {
            User userToAdd = event.getOption("user").getAsUser();
            String leaderId = event.getUser().getId();

            ClanService clanService = ClanService.getInstance();
            Clan leaderClan = clanService.getClanByLeader(Long.parseLong(leaderId));

            if (leaderClan == null) {
                event.reply("❌ You are not leading any clan, so you cannot add members.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String clanName = leaderClan.getName();

            // DB check: is user already in a clan?
            Clan targetUserClan = clanService.getClanByMember(userToAdd.getIdLong());
            if (targetUserClan != null) {
                event.reply("❌ That user is already part of a clan: `" + targetUserClan.getName() + "`.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // Role check: does user already have the clan member role?
            Guild guild = event.getGuild();
            if (guild != null) {
                Member member = guild.getMember(userToAdd);
                if (member != null) {
                    Role clanMemberRole = guild.getRoleById(1258861925438062724L); // ID ruolo Clan Member
                    if (clanMemberRole != null && member.getRoles().contains(clanMemberRole)) {
                        event.reply("❌ That user already has the `Clan Member` role and might already be in a clan.")
                                .setEphemeral(true)
                                .queue();
                        return;
                    }
                }
            }

            try {
                leaderClan.addUser(event.getUser(), userToAdd, event.getChannel());

                boolean dbUpdate = MongoDBManager.addUserToClan(clanName, userToAdd.getId());
                if (!dbUpdate) {
                    event.reply("⚠️ User could not be added to the database.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("▬▬▬ User Registered Successfully to a Clan! ▬▬▬▬")
                        .setColor(Color.decode("#859BC6"))
                        .setDescription("**The player has been added to the clan.**")
                        .addField("**Player Id:**", userToAdd.getId(), true)
                        .addField("**Player Name:**", userToAdd.getAsTag(), true)
                        .addField("**Clan Name:**", clanName, false)
                        .addField("**Victories:**", String.valueOf(leaderClan.getWins()), true)
                        .addField("**Losses:**", String.valueOf(leaderClan.getLosses()), true)
                        .addField("**Clan Creation Date:**", leaderClan.getFormattedCreationDate(), false)
                        .setImage("https://media1.tenor.com/m/hmS-_I4TaGAAAAAd/dyar-and.gif")
                        .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

                event.replyEmbeds(embed.build()).setEphemeral(true).queue(success -> {
                    if (guild != null) {
                        Member member = guild.getMember(userToAdd);
                        if (member != null) {
                            Role clanMemberRole = guild.getRoleById(1258861925438062724L);
                            if (clanMemberRole != null) {
                                guild.addRoleToMember(member, clanMemberRole).queue();
                            }
                        }

                        TextChannel logChannel = guild.getTextChannelById(1391769296975429742L);
                        if (logChannel != null) {
                            logChannel.sendMessageEmbeds(embed.build()).queue();
                        }
                    }
                });
            } catch (IllegalStateException e) {
                event.reply("Cannot add user to the clan: " + e.getMessage())
                        .setEphemeral(true)
                        .queue();
            }


        } else if (command.equals("kick_clan_member")) {
            String clanName = event.getOption("clan_name").getAsString();
            User userToKick = event.getOption("user").getAsUser();

            String leaderId = event.getUser().getId();
            Clan leaderClan = ClanService.getInstance().getClanByLeader(Long.parseLong(leaderId));

            if (leaderClan == null) {
                event.reply("❌ You are not leading any clan, so you cannot remove members.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (!leaderClan.getName().equals(clanName)) {
                event.reply("❌ You can only remove members from your own clan: `" + leaderClan.getName() + "`.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            try {
                leaderClan.kickUser(event.getUser(), userToKick, event.getChannel());

                boolean dbUpdate = MongoDBManager.removeUserFromClan(clanName, userToKick.getId());
                if (!dbUpdate) {
                    event.reply("⚠️ User could not be removed from the database.")
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("▬▬▬▬ User Kicked Successfully from the Clan! ▬▬▬▬")
                        .setColor(Color.RED)
                        .setDescription("**Your operation was successful!\nThe player has been kicked out of the clan.**")
                        .addField("**Player Id:**", userToKick.getId(), true)
                        .addField("**Player Name:**", userToKick.getAsTag(), true)
                        .addField("**Clan Name:**", leaderClan.getName(), false)
                        .addField("**Clan Creation Date:**", leaderClan.getFormattedCreationDate(), true)
                        .setImage("https://media1.tenor.com/m/kUaBa_GPTfAAAAAC/obito-death.gif")
                        .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

                event.replyEmbeds(embed.build()).setEphemeral(true).queue(success -> {
                    Guild guild = event.getGuild();
                    if (guild != null) {
                        Member member = guild.getMember(userToKick);
                        if (member != null) {
                            Role clanMemberRole = guild.getRoleById(1258861925438062724L);
                            if (clanMemberRole != null) {
                                guild.removeRoleFromMember(member, clanMemberRole).queue(
                                        v -> System.out.println("[Info] Clan Member role removed from " + member.getEffectiveName()),
                                        err -> System.err.println("❌ Failed to remove Clan Member role: " + err.getMessage())
                                );
                            } else {
                                System.err.println("❌ Clan Member role not found.");
                            }
                        } else {
                            System.err.println("❌ Member not found in the guild.");
                        }

                        TextChannel logChannel = guild.getTextChannelById(1391769296975429742L);
                        if (logChannel != null) {
                            logChannel.sendMessageEmbeds(embed.build()).queue();
                        }
                    }
                });
            } catch (IllegalStateException e) {
                event.reply("Cannot kick user from the clan: " + e.getMessage())
                        .setEphemeral(true)
                        .queue();
            }

        }
        // OLD STUFF

        if (event.getName().equals("edit_clan_name")) {
            String oldName = event.getOption("old_name").getAsString();
            String newName = event.getOption("new_name").getAsString();

            // 1. Verifica se il clan esiste
            if (!ClanStorage.hasClan(oldName)) {
                event.reply("⚠️ Error: Clan **" + oldName + "** does not exist.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // 2. Verifica se il nuovo nome è già utilizzato
            if (ClanStorage.hasClan(newName)) {
                event.reply("⚠️ Error: A clan with the name **" + newName + "** already exists.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // 3. Ottieni ID dell'utente
            String userId = event.getUser().getId();
            long leaderId;

            try {
                leaderId = Long.parseLong(userId);
            } catch (NumberFormatException e) {
                event.reply("❌ Invalid user ID format. Please contact an administrator.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // 4. Verifica se l'utente è effettivamente il leader del clan
            Clan clan = ClanService.getInstance().getClanByLeader(leaderId);
            if (clan == null || !clan.getName().equalsIgnoreCase(oldName)) {
                event.reply("❌ You can only change the name of your own clan.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // 5. Prova a rinominare il clan
            boolean success = ClanStorage.updateClanName(oldName, newName);
            if (success) {

                // (Facoltativo) Aggiorna anche in MongoDB
                // MongoDBManager.updateClanName(oldName, newName);

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("▬▬▬▬▬ Clan Name Updated Successfully! ▬▬▬▬")
                        .setColor(Color.decode("#20B2AA"))
                        .setDescription(String.format(
                                        "\n\n > 🔁 **Old Name:** `%s`" +
                                        "\n > ✨ **New Name:** `%s`" +
                                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", oldName, newName
                        ))
                        .setImage("https://media1.tenor.com/m/lhsiMCdib-IAAAAd/itachi-uchiha-forehead-protector.gif")
                        .setFooter("Clan System • Edit Name");

                // Risposta privata al leader
                event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();

                // Log nel canale di log
                Guild guild = event.getGuild();
                if (guild != null) {
                    TextChannel logChannel = guild.getTextChannelById(1391769296975429742L);
                    if (logChannel != null) {
                        logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                    } else {
                        System.err.println("⚠️ Log channel not found.");
                    }
                }
            } else {
                event.reply("⚠️ Error: Unable to update the clan name. Please try again.")
                        .setEphemeral(true)
                        .queue();
            }
        }


        if (event.getName().equals("edit_wins")) {
            event.deferReply(true).queue(); // <-- Risponde subito per evitare timeout

            String clanName = event.getOption("clan_name").getAsString();
            int newWins = event.getOption("wins").getAsInt();

            ClanService clanService = ClanService.getInstance();

            Clan targetClan = clanService.getClanMap().values().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(clanName))
                    .findFirst()
                    .orElse(null);

            if (targetClan == null) {
                event.getHook().sendMessage("❌ Clan `" + clanName + "` does not exist.").setEphemeral(true).queue();
                return;
            }

            targetClan.setWins(newWins);
            clanService.addOrUpdateClan(targetClan);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("▬▬▬▬▬ Victories Updated Successfully ▬▬▬▬▬");
            embed.setColor(Color.decode("#1CAFEC"));
            embed.setDescription("**The victories of the clan have been updated successfully!**");
            embed.addField("**Clan Name:**", targetClan.getName(), true);
            embed.addField("**New Wins:**", String.valueOf(targetClan.getWins()), true);
            embed.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
        }




        if (event.getName().equals("edit_losses")) {
            String clanName = event.getOption("clan_name").getAsString();
            int newLosses = event.getOption("losses").getAsInt();

            ClanService clanService = ClanService.getInstance();

            Clan targetClan = clanService.getClanMap().values().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(clanName))
                    .findFirst()
                    .orElse(null);

            if (targetClan == null) {
                event.reply("❌ Clan `" + clanName + "` does not exist.").setEphemeral(true).queue();
                return;
            }

            // Aggiorna sconfitte
            targetClan.setLosses(newLosses);
            clanService.addOrUpdateClan(targetClan);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("▬▬▬▬▬ Losses Updated Successfully ▬▬▬▬▬");
            embed.setColor(Color.decode("#776644"));
            embed.setDescription("**The losses of the clan have been updated successfully!**");
            embed.addField("**Clan Name:**", targetClan.getName(), true);
            embed.addField("**New Losses:**", String.valueOf(targetClan.getLosses()), true);
            embed.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }


        if (event.getName().equals("info_user")) {
            Member member = event.getOption("user").getAsMember();
            if (member == null) {
                event.reply("User not found.").setEphemeral(true).queue(); // Controllo extra per evitare null pointer
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("**User Information**");
            embedBuilder.setColor(Color.decode("#1CAFEC"));
            embedBuilder.setThumbnail(member.getUser().getEffectiveAvatarUrl());
            embedBuilder.addField("**Username:**", member.getUser().getName(), false);
            embedBuilder.addField("**Tag**:", member.getUser().getAsTag(), false);
            embedBuilder.addField("**User ID**:", member.getUser().getId(), false);
            embedBuilder.addField("**Creation Date**", member.getUser().getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE), true);
            embedBuilder.addField("**Join Date**", member.getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE), true);

            // Aggiunta della sezione clan
            Clan userClan = ClanStorage.getClanByUser(member.getUser());
            if (userClan != null) {
                embedBuilder.addField("**Clan of Belonging**", userClan.getName(), false);
            } else {
                // Messaggio effimero se il giocatore non ha un clan
                event.reply("The player does not belong to any clan.").setEphemeral(true).queue();
                return; // Termina l'esecuzione del metodo
            }

            // Sezione ruoli
            StringBuilder roles = new StringBuilder();
            for (Role role : member.getRoles()) {
                roles.append(" > * ").append(role.getName()).append("\n");
            }
            if (roles.length() == 0) {
                roles.append("No roles assigned.");
            }
            embedBuilder.addField("Roles", roles.toString(), false);

            // Rispondi con l'embed
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }


        if (event.getName().equals("clan_stat")) {
            String clanName = event.getOption("clan_name").getAsString();
            System.out.println("Received clan name: " + clanName);

            // Recupera il clan dal nome
            Clan clan = ClanStorage.getClan(clanName);
            if (clan == null) {
                event.reply("The clan **" + clanName + "** does not exist.").setEphemeral(true).queue();
                return;
            }

            // Ottieni la lista dei membri del clan
            ArrayList<User> clanMembers = (ArrayList<User>) clan.getListClanMember();
            System.out.println("Number of members: " + clanMembers.size());

            // Crea l'embed
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("▬▬▬▬▬▬▬▬ Clan Info ▬▬▬▬▬▬▬▬"); // Titolo con il nome del clan
            embedBuilder.setColor(Color.green);

            // Aggiungi le informazioni principali
            embedBuilder.addField("**Clan Name**", clan.getName(), false);
            embedBuilder.addField("**Wins**", String.valueOf(clan.getWins()), true);
            embedBuilder.addField("**Losses**", String.valueOf(clan.getLosses()), true);
            embedBuilder.addField("**Creation Date**", clan.getFormattedCreationDate(), false);

            // Costruzione della lista membri
            StringBuilder memberList = new StringBuilder();
            memberList.append("**List of Members:**\n"); // Linea introduttiva

            // Verifica se i membri non sono null o vuoti
            if (clanMembers != null && !clanMembers.isEmpty()) {
                System.out.println("Clan has members, proceeding with list...");

                for (User member : clanMembers) {
                    System.out.println("Processing member: " + member.getName());  // Stampa il nome completo con tag

                    // Prendi solo la parte del nome (senza il tag)
                    String memberName = member.getEffectiveName();

                    System.out.println("Extracted member name: " + memberName);  // Stampa solo il nome senza tag

                    memberList.append("  > * ").append(memberName)  // Usa solo il nome, non il tag
                            .append(" - ").append(member.getAsMention()).append("\n");
                }
            } else {
                System.out.println("No members found in this clan.");
                memberList.append("No members in this clan.");
            }

            // Verifica se la lista dei membri è stata correttamente formattata
            System.out.println("Member list generated: \n" + memberList.toString());

            // Imposta la descrizione e il conteggio
            embedBuilder.setDescription(memberList.toString());
            embedBuilder.appendDescription("\n**Total members in this clan: ** " + clanMembers.size());

            // Imposta il footer con la chiusura
            embedBuilder.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

            // Rispondi con l'embed
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue(
                    success -> System.out.println("Embed sent successfully"),
                    failure -> System.out.println("Error sending embed: " + failure.getMessage())
            );
        }

        if (event.getName().equals("clan_member_list")) {
            String clanName = event.getOption("clan_name").getAsString();
            System.out.println("Received clan name: " + clanName);

            // Recupera il clan dal nome
            Clan clan = ClanStorage.getClan(clanName);
            if (clan == null) {
                event.reply("The clan **" + clanName + "** does not exist.").setEphemeral(true).queue();
                return;
            }

            // Ottieni la lista dei membri del clan
            ArrayList<User> clanMembers = (ArrayList<User>) clan.getListClanMember();
            System.out.println("Number of members: " + clanMembers.size());

            // Crea l'embed
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("▬▬▬▬▬▬▬▬▬▬ " + clan.getName() + " ▬▬▬▬▬▬▬▬▬▬▬"); // Titolo con il nome del clan
            embedBuilder.setColor(Color.green);

            // Costruzione della lista membri
            StringBuilder memberList = new StringBuilder();
            memberList.append("**List of Members:**\n"); // Linea introduttiva

            // Verifica se i membri non sono null o vuoti
            if (clanMembers != null && !clanMembers.isEmpty()) {
                System.out.println("Clan has members, proceeding with list...");

                for (User member : clanMembers) {
                    System.out.println("Processing member: " + member.getName());  // Stampa il nome completo con tag
                    // Prendi solo la parte del nome (senza il tag)
                    String memberName = member.getEffectiveName();
                    System.out.println("Extracted member name: " + memberName);  // Stampa solo il nome senza tag

                    memberList.append("  > * ").append(memberName)  // Usa solo il nome, non il tag
                            .append(" - ").append(member.getAsMention()).append("\n");
                }
            } else {
                System.out.println("No members found in this clan.");
                memberList.append("No members in this clan.");
            }

            // Verifica se la lista dei membri è stata correttamente formattata
            System.out.println("Member list generated: \n" + memberList.toString());

            // Imposta la descrizione e il conteggio
            embedBuilder.setDescription(memberList.toString());
            embedBuilder.appendDescription("\n**Total members in this clan: ** " + clanMembers.size()+ "\n\n ▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            embedBuilder.setImage("https://media1.tenor.com/m/SbU1R7BA53gAAAAC/naruto-data-naruto.gif");

            // Rispondi con l'embed
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue(
                    success -> System.out.println("Embed sent successfully"),
                    failure -> System.out.println("Error sending embed: " + failure.getMessage())
            );
        }
// gif https://media.tenor.com/SbU1R7BA53gAAAAM/naruto-data-naruto.gif
        if (event.getName().equals("delete_clan")) {
            String clanName = event.getOption("clan_name").getAsString();

            // Controlla se il clan esiste
            if (ClanStorage.hasClan(clanName)) {
                // Rimuovi il clan dalla memoria
                ClanStorage.removeClan(clanName);

                // Rimuovi il clan dal database
                boolean isDeletedFromDB = ClanStorage.deleteClanFromDatabase(clanName);

                if (isDeletedFromDB) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("▬▬▬▬▬▬ Clan Deleted Successfully ▬▬▬▬▬▬▬");
                    embedBuilder.setDescription(
                            " > **The clan `" + clanName + "` has been successfully deleted from both memory and database.**\n\n" +
                                    "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    embedBuilder.setColor(Color.decode("#B90000")); // Colore rosso per indicare la cancellazione
                    embedBuilder.setImage("https://media1.tenor.com/m/qvsXglbsb7oAAAAd/shinra-tensei.gif");
                    event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                    System.out.println("Clan: "+ clanName +" Successfully deleted from the DB");

                } else {
                    event.reply("Failed to delete the clan from the database. Please try again.").setEphemeral(true).queue();
                }
            } else {
                event.reply("The clan **" + clanName + "** does not exist.").setEphemeral(true).queue();
            }
        }

        if (event.getName().equals("list_all_clan")) {
            // Recupera tutti i clan
            Map<String, Clan> allClans = ClanStorage.getClans();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("**List of All Clans**"); // Titolo dell'embed
            embedBuilder.setColor(Color.decode("#FFD151")); // Colore dell'embed

            // Costruzione della lista dei clan
            StringBuilder clanList = new StringBuilder();
            clanList.append("**Registered Clans:**\n"); // Linea introduttiva
            System.out.println("Number of clans: " + allClans.size());

            for (String clanName : allClans.keySet()) {
                Clan clan = allClans.get(clanName);
                clanList.append("  > * ").append(clanName)
                        .append(" - Members: ").append(clan.getMemberCount()).append("\n");
                System.out.println("Clan found: " + clanName);
            }

            // Controllo se ci sono clan
            if (allClans.isEmpty()) {
                clanList.append("No clans are currently registered.");
            }

            // Imposta descrizione ed eventuale conteggio
            embedBuilder.setDescription(clanList.toString());
            embedBuilder.appendDescription("\n**Total clans registered: ** " + allClans.size());

            // Rispondi con l'embed
            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }

    }
    //test3

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Un esempio di comando per recuperare un clan dal database
        if (event.getMessage().getContentRaw().equals("!getClanData")) {
            String clanName = "NewClan";  // Sostituisci con il nome reale del clan
            Clan clan = MongoDBManager.getClanByName(clanName);

            if (clan != null) {
                event.getChannel().sendMessage("Clan found: " + clan.getName()).queue();
            } else {
                event.getChannel().sendMessage("Clan not found.").queue();
            }
        }

        // Altri comandi per inserire, aggiornare o eliminare clan

        if (event.getMessage().getContentRaw().equals("!addClan")) {
            Clan newClan = new Clan();
            newClan.setName("NewClan");
            newClan.setWins(0);
            newClan.setLosses(0);
            MongoDBManager.insertClan(newClan);
            event.getChannel().sendMessage("Clan added successfully!").queue();
        }

    }

    @Override
    public void onReady(ReadyEvent event) {
        Config config = new Config();
        Long guildId = Long.valueOf(config.getGuildId());
        Guild guild = event.getJDA().getGuildById(guildId);

        // delete all the global command
        List<Command> commandglobals = event.getJDA().retrieveCommands().complete();
        for (Command cmd : commandglobals) {
            cmd.delete().queue();
            System.out.println("Global command deleted: " + cmd.getName());
        }

        // dispaly all the current global command
        List<Command> commandglobals2 = event.getJDA().retrieveCommands().complete();
        for (Command cmd : commandglobals2) {
            System.out.println("Global command : " + cmd.getName());

        }

        List<CommandData> commands = new ArrayList<>();

        commands.add(Commands.slash("commands", "Info about all the bot's commands"));

        commands.add(Commands.slash("register_clan", "Create a new clan in the bot")
                .addOptions(
                        new OptionData(OptionType.STRING, "name", "The name of the clan", true)
                        //    new OptionData(OptionType.USER, "user", "The user to put on the clan", true)
                        //    new OptionData(OptionType.INTEGER, "victories", "Clan wins (optional)", false).setMinValue(0L),
                        //    new OptionData(OptionType.INTEGER, "losses", "Clan losses (optional)", false).setMinValue(0L)
                ));

        commands.add(Commands.slash("add_clan_member", "Add a user to a specific clan")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The user to add to the clan", true)
                ));

        commands.add(Commands.slash("kick_clan_member", "Remove a user from a specific clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.USER, "user", "The user to remove", true)
                ));

        commands.add(Commands.slash("edit_wins", "Edit the number of victories for a clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The clan's name", true),
                        new OptionData(OptionType.INTEGER, "wins", "New number of wins", true).setMinValue(0L)
                ));

        commands.add(Commands.slash("edit_losses", "Edit the number of losses for a clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The clan's name", true),
                        new OptionData(OptionType.INTEGER, "losses", "New number of losses", true).setMinValue(0L)
                ));


        commands.add(Commands.slash("edit_clan_name", "Edit or create a clan name")
                .addOptions(
                        new OptionData(OptionType.STRING, "old_name", "Current clan name", true),
                        new OptionData(OptionType.STRING, "new_name", "New clan name", true)
                ));

        commands.add(Commands.slash("clan_member_list", "List all members in a specific clan")
                .addOptions(new OptionData(OptionType.STRING, "clan_name", "The clan's name", true)));

        commands.add(Commands.slash("list_all_clan", "List all clans registered on the bot"));

        commands.add(Commands.slash("add_info_card", "Create the player info card"));

        commands.add(Commands.slash("my_ninjacard", "Show your Ninja Card"));

       // commands.add(Commands.slash("edit_ninja_card", "Edit your Ninja Card"));

        commands.add(Commands.slash("search_ninjacard", "View another user's Ninja Card")
                .addOptions(new OptionData(OptionType.USER, "target", "The user", true)));

        commands.add(Commands.slash("freestyle", "Send lobby creation embed"));

        commands.add(Commands.slash("edit_lobby", "Edit the lobby embed"));

        commands.add(Commands.slash("direct", "Send private lobby"));
        //     commands.add(Commands.slash("complete_lobby", "Archive and complete the lobby"));
        commands.add(Commands.slash("leave_lobby", "Leave the current lobby"));

        commands.add(Commands.slash("send_player_info_file", "Send a .txt with all players with Player Info role"));

        commands.add(Commands.slash("results", "Send an embed about the lobby"));

        commands.add(Commands.slash("add_user_lobby", "Add a user to your lobby")
                .addOptions(new OptionData(OptionType.USER, "player", "The user to add", true)));

        // Aggiorna tutti i comandi in una singola chiamata pulita
        guild.updateCommands().addCommands(commands).queue();
    }
}

/*
        commands.add(Commands.slash("info", "Info about the bot"));
        commands.add(Commands.slash("cancel", "Cancel the lobby you created or joined"));

        commands.add(Commands.slash("clan_stat", "View clan stats")
                .addOptions(new OptionData(OptionType.STRING, "clan_name", "The clan's name", true)));

        commands.add(Commands.slash("info_user", "Info about a specific player")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The user", true)
                ));
                        commands.add(Commands.slash("delete_clan", "Delete an existing clan")
                .addOptions(new OptionData(OptionType.STRING, "clan_name", "The clan's name", true)));
        commands.add(Commands.slash("block_user", "Block a user from your lobby")
                      .addOptions(new OptionData(OptionType.USER, "user", "The user to block", true)));

         commands.add(Commands.slash("lobby_stats", "Show lobby stats"));
 */