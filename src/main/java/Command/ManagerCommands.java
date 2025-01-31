package Command;

import ClanManager.Clan;
import ClanManager.ClanStorage;
import ClanManager.ClanStorageWithJson;
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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManagerCommands extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        // Handling the /info command
        if (command.equals("info") && (Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR))) {
            if (event.getChannel() instanceof TextChannel) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("▬▬▬▬▬▬▬▬▬▬ Info ▬▬▬▬▬▬▬▬▬▬▬");
                builder.setDescription(" > **The purpose of this bot is to manage and simplify the administration of Storm Clan Related things.**" +
                        "\n\n > **The bot is created by Azrael.**\n\n"
                        + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                builder.setImage("https://media1.tenor.com/m/4XzoCqoNqjQAAAAd/uzumaki-clan.gif");
                builder.setColor(Color.decode("#1C638C"));
                event.replyEmbeds(builder.build()).queue();
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
        if (command.equals("commands") && (Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR))) {
            if (event.getChannel() instanceof TextChannel) {
                TextChannel channel = (TextChannel) event.getChannel();
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("▬▬▬▬▬▬▬▬▬ All Commands ▬▬▬▬▬▬▬▬▬");
                builder.setDescription(
                        "\n\n > This is the complete List of the commands for this bot:" +
                                "\n\n > * **/info** -> __This command will show you info about the bot__" +
                                "\n\n > * **/clear** -> __This command will clear the chat in a specific channel__" +
                                "\n\n > * **/commands** -> __This command will show you all the commands of the bot__" +
                                "\n\n > * **/register_clan** -> __This command will register a new clan in the bot__" +
                                "\n\n > * **/edit_clan_name** -> __This command will edit the name of a specific clan__" +
                                "\n\n > * **/info_clan** -> __This command will give you all info about a specific clan__" +
                                "\n\n > * **/ft_request** -> __This command will make a friendly challenge between 2 random clans__" +
                                "\n\n > * **/clan_win** -> __This command will update the victory count for a specific clan__" +
                                "\n\n > * **/clan_loses** -> __This command will update the losses for a specific clan__" +
                                "\n\n > * **/clan_list** -> __This command will give you a list of all player in a clan__" +
                                "\n\n > * **/info_user** -> __This command will provide info about a specific user__" +
                                "\n\n > * **/add_user** -> __This command will add a user to a specific clan__" +
                                "\n\n > * **/kick_user** -> __This command will kick a user from a specific clan__" +
                                "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                builder.setImage("https://media1.tenor.com/m/R8CSlK2ys1AAAAAd/sasuke-scroll.gif");
                builder.setColor(Color.white);
                event.replyEmbeds(builder.build()).queue();
            }
        }



// Handling the /register_clan command

        if (command.equals("register_clan")) {
            if (event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                // Estrai le opzioni dal comando
                String clanName = event.getOption("name").getAsString();
                User user = (User) event.getOption("user").getAsUser(); // Utente che crea il clan
                int victories = event.getOption("victories").getAsInt();  // Vittorie
                int losses = event.getOption("losses").getAsInt();       // Perdite
                TextChannel channel = (TextChannel) event.getChannel();   // Canale in cui inviare il messaggio

                // Crea il nuovo clan
                Clan clan = new Clan(clanName, user, victories, losses);

                // Inserisci il clan nel database MongoDB
                MongoDBManager.insertClan(clan);

                // Crea l'embed per il riepilogo
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("▬▬▬▬ Clan Registered Successfully! ▬▬▬▬");
                builder.setColor(Color.GREEN);
                builder.setDescription("**The following clan has been registered successfully!**");
                builder.addField("**Clan Name**", clan.getName(), true);
                builder.addField("**Created By**", user.getAsTag(), true);
                builder.addField("**Creation Date**", clan.getFormattedCreationDate(), false);
                builder.addField("**Victories**", String.valueOf(clan.getWins()), true);
                builder.addField("**Losses**", String.valueOf(clan.getLosses()), true);
                builder.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

                // Invio dell'embed come risposta
                event.replyEmbeds(builder.build()).queue();
            } else {
                // Risposta nel caso in cui il membro non abbia i permessi necessari
                event.reply("You don't have the required permissions to register a clan.").setEphemeral(true).queue();
            }
        }

        if (event.getName().equals("add_user")) {
            String clanName = event.getOption("clan_name").getAsString();
            User user = event.getOption("user").getAsUser();

            // Search for the clan in storage
            Clan clan = ClanStorage.getClan(clanName);
            if (clan == null) {
                event.reply("Clan `" + clanName + "` does not exist!").setEphemeral(true).queue();
                return;
            }

            // Add the user to the clan
            try {
                clan.addUser(user);
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(" Player Added Successfully to the clan");
                embed.setTitle("▬▬▬ User Registered Successfully to a Clan! ▬▬▬");
                embed.setColor(Color.decode("#859BC6"));
                embed.setDescription("**Your operation was successful!\nThe player has been added to the clan.**");
                embed.addField("**Player Id: **", user.getId(), true);
                embed.addField("**Player Name: **", user.getEffectiveName(), true);
                embed.addField("**Clan Name: **", clan.getName(), false);
                embed.addField("**Victories: **", String.valueOf(clan.getWins()), true);
                embed.addField("**Losses: **", String.valueOf(clan.getLosses()), true);
                embed.addField("**Clan Creation Date: **", clan.getFormattedCreationDate(), false);
                embed.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                //     embed.setFooter(user.getName() +"  "+user.getEffectiveAvatarUrl());
                event.replyEmbeds(embed.build()).queue();
            } catch (IllegalStateException e) {
                event.reply("Cannot add user to the clan: " + e.getMessage()).setEphemeral(true).queue();
            }
        }
        if (event.getName().equals("kick_user")) {
            String clanName = event.getOption("clan_name").getAsString();
            User user = event.getOption("user").getAsUser();

            Clan clan = ClanStorage.getClan(clanName);
            if (clan == null) {
                event.reply("Clan `" + clanName + "` does not exist!").setEphemeral(true).queue();
                return;
            }

            try {
                clan.kickUser(user);
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("▬▬▬ User Kicked Successfully of the Clan! ▬▬▬");
                embed.setColor(Color.red);
                embed.setDescription("**Your operation was successful!\nThe player has been kicked out of the clan.**");
                embed.addField("**Player Id: **", user.getId(), true);
                embed.addField("**Player Name: **", user.getEffectiveName(), true);
                embed.addField("**Clan Name: **", clan.getName(), false);
                embed.addField("**Clan Creation Date: **", clan.getFormattedCreationDate(), true);
                embed.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                //     embed.setFooter(user.getName() +"  "+user.getEffectiveAvatarUrl());
                event.replyEmbeds(embed.build()).queue();
            } catch (IllegalStateException e) {
                event.reply("Cannot kick user of the clan: " + e.getMessage()).setEphemeral(true).queue();
            }
        }

        // Parte aggiunta nel metodo onSlashCommandInteraction
        if (event.getName().equals("edit_wins")) {
            String clanName = event.getOption("clan_name").getAsString();
            int newWins = event.getOption("wins").getAsInt();

            // Trova il clan nel ClanStorage
            Clan clan = ClanStorage.getClan(clanName);
            if (clan == null) {
                event.reply("Clan `" + clanName + "` does not exist!").setEphemeral(true).queue();
                return;
            }

            // Aggiorna le vittorie usando il setter
            clan.setWins(newWins);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("▬▬▬▬▬  Victories Updated Successfully ▬▬▬▬▬ ");
            embed.setColor(Color.decode("#1CAFEC"));
            embed.setDescription("**The victories of the clan have been updated successfully!**");
            embed.addField("**Clan Name:**", clan.getName(), true);
            embed.addField("**New Wins:**", String.valueOf(clan.getWins()), true);
            embed.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            event.replyEmbeds(embed.build()).queue();
        }

        if (event.getName().equals("edit_losses")) {
            String clanName = event.getOption("clan_name").getAsString();
            int newLosses = event.getOption("losses").getAsInt();

            // Trova il clan nel ClanStorage
            Clan clan = ClanStorage.getClan(clanName);
            if (clan == null) {
                event.reply("Clan `" + clanName + "` does not exist!").setEphemeral(true).queue();
                return;
            }

            // Aggiorna le sconfitte usando il setter
            clan.setLosses(newLosses);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("▬▬▬▬▬  Losses Updated Successfully ▬▬▬▬▬");
            embed.setColor(Color.decode("#776644"));
            embed.setDescription("**The losses of the clan have been updated successfully!**");
            embed.addField("**Clan Name:**", clan.getName(), true);
            embed.addField("**New Losses:**", String.valueOf(clan.getLosses()), true);
            embed.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            event.replyEmbeds(embed.build()).queue();
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
            event.replyEmbeds(embedBuilder.build()).queue();
        }

        if (event.getName().equals("edit_clan_name")) {
            String oldName = event.getOption("old_name").getAsString();
            String newName = event.getOption("new_name").getAsString();

            // Controllo se esiste già un clan con il vecchio nome
            if (ClanStorage.hasClan(oldName)) {
                // Modifica del nome del clan esistente
                if (ClanStorage.updateClanName(oldName, newName)) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("▬▬▬▬▬ Clan Name Updated Successfully! ▬▬▬▬");
                    embedBuilder.setDescription(
                                    "\n\n > * **Old Name** ->   "+ oldName +
                                    "\n\n > * **New Name** ->   "+ newName +
                                    "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    embedBuilder.setImage("https://media1.tenor.com/m/lhsiMCdib-IAAAAd/itachi-uchiha-forehead-protector.gif");
                    embedBuilder.setColor(Color.decode("#20B2AA"));
                    event.replyEmbeds(embedBuilder.build()).queue();

                } else {
                    event.reply("Failed to update the clan name. Please try again.").setEphemeral(true).queue();
                }

            }
        }


        if (event.getName().equals("clan_member_list")) {
            String clanName = Objects.requireNonNull(event.getOption("clan_name")).getAsString();

            // Recupera il clan dal nome
            Clan clan = ClanStorage.getClan(clanName);
            if (clan == null) {
                event.reply("The clan **" + clanName + "** does not exist.").setEphemeral(true).queue();
                return;
            }

            // Ottieni la lista dei membri del clan
            ArrayList<User> clanMembers = clan.getListClanMember();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("**Clan:** " + clanName); // Titolo con il nome del clan
            embedBuilder.setColor(Color.decode("#FFD151")); // Colore dell'embed

            // Costruzione della lista membri
            StringBuilder memberList = new StringBuilder();
            memberList.append("**List of Members:**\n"); // Linea introduttiva

            for (User member : clanMembers) {
                memberList.append("  > * ").append(member.getName())
                        .append(" - ").append(member.getAsMention()).append("\n");
            }

            // Controllo se ci sono membri
            if (clanMembers.isEmpty()) {
                memberList.append("No members in this clan.");
            }

            // Imposta descrizione ed eventuale conteggio
            embedBuilder.setDescription(memberList.toString());
            embedBuilder.appendDescription("\n**Total members in this clan: ** " + clanMembers.size());

            // Rispondi con l'embed
            event.replyEmbeds(embedBuilder.build()).queue();
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

            for (String clanName : allClans.keySet()) {
                Clan clan = allClans.get(clanName);
                clanList.append("  > * ").append(clanName)
                        .append(" - Members: ").append(clan.getMemberCount()).append("\n");
            }

            // Controllo se ci sono clan
            if (allClans.isEmpty()) {
                clanList.append("No clans are currently registered.");
            }

            // Imposta descrizione ed eventuale conteggio
            embedBuilder.setDescription(clanList.toString());
            embedBuilder.appendDescription("\n**Total clans registered: ** " + allClans.size());

            // Rispondi con l'embed
            event.replyEmbeds(embedBuilder.build()).queue();
        }


        if (event.getName().equals("clan_stat")) {
            //  String clanName = Objects.requireNonNull(event.getOption("clan_name")).getAsString();
            String clanName = event.getOption("clan_name").getAsString();

            // Recupera il clan dal nome
            Clan clan = ClanStorage.getClan(clanName);
            if (clan == null) {
                event.reply("The clan **" + clanName + "** does not exist.").setEphemeral(true).queue();
                return;
            }

            // Ottieni la lista dei membri del clan
            ArrayList<User> clanMembers = clan.getListClanMember();

            // Crea l'embed
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("▬▬▬▬▬▬▬▬ Clan Info ▬▬▬▬▬▬▬▬"); // Titolo con il nome del clan
            embedBuilder.setColor(Color.red);

            // Aggiungi le informazioni principali/
            embedBuilder.addField("**Clan Name**", clan.getName(), false);
            embedBuilder.addField("**Wins**", String.valueOf(clan.getWins()), true);
            embedBuilder.addField("**Losses**", String.valueOf(clan.getLosses()), true);
            embedBuilder.addField("**Creation Date**", clan.getFormattedCreationDate(), false);

            // Costruzione della lista membri
            StringBuilder memberList = new StringBuilder();
            memberList.append("**List of Members:**\n"); // Linea introduttiva

            for (User member : clanMembers) {
                memberList.append("  > * ").append(member.getName())
                        .append(" - ").append(member.getAsMention()).append("\n");
            }

            // Controllo se ci sono membri
            if (clanMembers.isEmpty()) {
                memberList.append("No members in this clan.");
            }

            // Imposta descrizione ed eventuale conteggio
            embedBuilder.setDescription(memberList.toString());
            embedBuilder.appendDescription("\n**Total members in this clan: ** " + clanMembers.size());
            embedBuilder.setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

            // Rispondi con l'embed
            event.replyEmbeds(embedBuilder.build()).queue();
        }
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
                    event.replyEmbeds(embedBuilder.build()).queue();
                    System.out.println("Clan: "+ clanName +" Successfully deleted from the DB");

                } else {
                    event.reply("Failed to delete the clan from the database. Please try again.").setEphemeral(true).queue();
                }
            } else {
                event.reply("The clan **" + clanName + "** does not exist.").setEphemeral(true).queue();
            }
        }


        if (event.getName().equals("ft_request")) {
            String clanName = event.getOption("clan_name").getAsString();

            // Controlla se il clan esiste
            if (ClanStorage.hasClan(clanName)) {
                // Crea l'embed per la richiesta FT
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("▬▬▬▬▬▬▬▬▬▬ FT Request ▬▬▬▬▬▬▬▬▬");
                embedBuilder.setDescription(
                        " > **Clan:** `" + clanName + "`\n" +
                                " > **Status:** Open to all\n\n" +
                                "This FT request is open to all clans. If interested, reply to this message or contact the clan directly." +
                                "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                );
                embedBuilder.setColor(Color.ORANGE); // Colore distintivo per la richiesta
                embedBuilder.setImage("https://media1.tenor.com/m/12s59XBmULkAAAAd/obito-uchiha-vs-kakashi-hatake-naruto-shippuden.gif");

                // Rispondi con l'embed
                event.replyEmbeds(embedBuilder.build()).queue();
            } else {
                // Risposta se il clan non esiste
                event.reply("Impossible to create the request. The clan **" + clanName + "** does not exist.")
                        .setEphemeral(true) // Messaggio visibile solo all'utente
                        .queue();
            }
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

        // Altri comandi per aggiornare o eliminare clan
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        // List of commands to register for the guild
        List<CommandData> commands = new ArrayList<>();

        // Register /register_clan command with required and optional options
        commands.add(Commands.slash("register_clan", "Create a new clan in the bot")
                .addOptions(
                        new OptionData(OptionType.STRING, "name", "The name of the clan", true),
                        new OptionData(OptionType.USER, "user", "The name of the user to put on the clan", true),
                        new OptionData(OptionType.INTEGER, "victories", "How much wins Have the clan,this is not required", false).setMinValue(0L),
                        new OptionData(OptionType.INTEGER, "losses", "How much loses Have the clan,this is not required", false).setMinValue(0L)

                ));

        commands.add(Commands.slash("add_user", "Add a user to a specific clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.USER, "user", "The name of the user to put on the clan", true)));

        commands.add(Commands.slash("kick_user", "Remove a user from a specific clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.USER, "user", "The user to remove from the clan", true)));

        commands.add(Commands.slash("edit_wins", "Edit the number of victories for a clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.INTEGER, "wins", "The new number of wins", true).setMinValue(0L)
                ));

        commands.add(Commands.slash("edit_losses", "Edit the number of losses for a clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.INTEGER, "losses", "The new number of losses", true).setMinValue(0L)
                ));
        commands.add(Commands.slash("info_user", "Info about a specific Player")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The name of the user to put on the clan", true)));

        commands.add(Commands.slash("edit_clan_name", "Edit or create a clan name")
                .addOptions(
                        new OptionData(OptionType.STRING, "old_name", "The current name of the clan", true),
                        new OptionData(OptionType.STRING, "new_name", "The new name for the clan", true)
                ));

        commands.add(Commands.slash("clan_member_list", "Get the list of all members in a specific clan")
                .addOption(OptionType.STRING, "clan_name", "The name of the clan", true)
        );


        commands.add(Commands.slash("clan_stat", "Get the list of all members in a specific clan")
                .addOption(OptionType.STRING, "clan_name", "The name of the clan", true)
        );

        commands.add(Commands.slash("delete_clan", "Delete an existing clan")
                .addOption(OptionType.STRING, "clan_name", "The name of the clan to delete", true));

        commands.add(Commands.slash("delete", "Delete an existing clan")
                .addOption(OptionType.STRING, "clan_name", "The name of the clan to delete", true));

        commands.add(Commands.slash("ft_request", "Send a Clan Battle request")
                .addOption(OptionType.STRING, "clan_name", "The name of your clan", true));


        commands.add(Commands.slash("list_all_clan", "Get the list of all Clan Registrated on the Bot"));



        // Register the commands to the guild
        event.getJDA().updateCommands().addCommands(commands).queue();
    }

    @Override
    public void onReady(ReadyEvent event) {
        // Update the commands for the bot
        Long guildId = 856147888550969345L; // Replace with your server's ID
        Guild guild = event.getJDA().getGuildById(guildId);
        guild.upsertCommand("info", "Info about the bot").queue();
        guild.upsertCommand("commands", "Info about all the bot's commands").queue();
        guild.upsertCommand("register_clan", "Create a new clan in the bot")
                .addOptions(
                        new OptionData(OptionType.STRING, "name", "The name of the clan", true),
                        new OptionData(OptionType.USER, "user", "The name of the user to put on the clan", true),
                        new OptionData(OptionType.INTEGER, "victories", "How much wins Have the clan,this is not required", false).setMinValue(0L),
                        new OptionData(OptionType.INTEGER, "losses", "How much loses Have the clan,this is not required", false).setMinValue(0L)

                );
        guild.upsertCommand("add_user", "Add a user to a specific clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.USER, "user", "The name of the user to put on the clan", true));

        guild.upsertCommand(Commands.slash("kick_user", "Remove a user from a specific clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.USER, "user", "The user to remove from the clan", true)));

        guild.upsertCommand(Commands.slash("edit_wins", "Edit the number of victories for a clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.INTEGER, "wins", "The new number of wins", true).setMinValue(0L)
                ));

        guild.upsertCommand(Commands.slash("edit_losses", "Edit the number of losses for a clan")
                .addOptions(
                        new OptionData(OptionType.STRING, "clan_name", "The name of the clan", true),
                        new OptionData(OptionType.INTEGER, "losses", "The new number of losses", true).setMinValue(0L)
                ));

        guild.upsertCommand("info_user", "Info about a specific Player")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "The name of the user to put on the clan", true));

        guild.upsertCommand("edit_clan_name", "Edit or create a clan name")
                .addOptions(
                        new OptionData(OptionType.STRING, "old_name", "The current name of the clan", true),
                        new OptionData(OptionType.STRING, "new_name", "The new name for the clan", true)
                );

        guild.upsertCommand("clan_member_list", "Get the list of all members in a specific clan")
                    .addOption(OptionType.STRING, "clan_name", "The name of the clan", true)
                    .queue();

        guild.upsertCommand("clan_stat", "Get the list of all members in a specific clan")
                .addOption(OptionType.STRING, "clan_name", "The name of the clan", true)
                .queue();

        guild.upsertCommand("delete_clan", "Delete an existing clan")
                .addOption(OptionType.STRING, "clan_name", "The name of the clan to delete", true).queue();

        guild.upsertCommand("delete", "Delete an existing clan")
                .addOption(OptionType.STRING, "clan_name", "The name of the clan to delete", true).queue();

        guild.upsertCommand("ft_request", "Send a Clan Battle request")
                .addOption(OptionType.STRING, "clan_name", "The name of your clan", true).queue();

        guild.upsertCommand("list_all_clan", "Get the list of all Clan Registrated on the Bot");

    }

}
