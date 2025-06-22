package Lobby;

import Stat.PlayerStatMongoDBManager;
import Stat.PlayerStats;
import Stat.PlayerStatsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class LobbyCommand extends ListenerAdapter {

    private final Map<Long, Lobby> lobbySessions = new HashMap<>();
    private final long categoryId = 1381025760231555077L; // Categoria per le lobby

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        long discordId = event.getUser().getIdLong();

        switch (event.getName()) {
            case "freestyle" -> handleFreestyle(event);
            case "edit_lobby" -> handleEditLobby(event);
            case "direct" -> handleDirect(event);
            case "add_user_lobby" -> handleAddUserLobby(event, discordId);
            case "kick_user_lobby" -> handleKickUserLobby(event, discordId);

            // altri comandi...
        }


        if (event.getName().equals("delete_lobby")) {
            System.out.println("on delete part");
            // Puoi aggiungere logica per cancellare la lobby dal manager o database
            Lobby lobby = LobbyManager.getLobby(discordId);
            if (lobby != null) {
                lobby.deletePost(event.getGuild());

                event.reply(event.getUser().getAsMention() + " ✅ Lobby deleted successfully.")
                        .setEphemeral(true)
                        .queue();

                LobbyManager.removeLobby(discordId); // Se hai un metodo del genere
            } else {
                event.reply("❌ No active lobby found to delete.")
                        .setEphemeral(true)
                        .queue();
            }
        }

        if (!event.getName().equals("results")) return;

        // Ricava membro e utente che hanno invocato il comando
        Member member = event.getMember();
        User user = event.getUser();

        // 1) Costruisci l'embed
        EmbedBuilder eb = new EmbedBuilder()
                //  .setTitle("▬▬▬▬▬▬▬▬▬▬▬ Report ▬▬▬▬▬▬▬▬▬▬▬")
                .setDescription(
                        "**Welcome to our lobby manage page.**\n" +
                                "> Use this command to finalize the lobby. You can view a summary of what happened, report results, provide feedback, or notify an admin if needed.It’s the last step to close out the session properly\n\n" +
                                "**Please choose one of the options below for your lobby:**\n" +
                                "> * **Complete lobby:** this option will mark and complete the current lobby\n" +
                                "> * **Report Lobby Score:** with this option you can report  the score of the lobby\n" +
                                "> * **Incomplete lobby:** this option will mark your lobby as incompleted\n" +
                                "> * **Referee:** This option will contact an admin to do as referee or to report something.\n"

                        //                     "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                )
                .setImage("https://64.media.tumblr.com/87273056002f62ae5f1b6417c001170f/f4a7711e15cf7a55-f5/s1280x1920/ec0568cc634542254ba8908d2de2861fda4d171f.gif")
                .setColor(Color.white);
//- Report Lobby Score (Score option only shows up when match format is set to casual 1v1 or group FT sets, Clan Battle)
        // 2) Costruisci il dropdown menu
        StringSelectMenu menu = StringSelectMenu.create("result:menu")
                .setPlaceholder("Choose an option for the lobby")
                .setMinValues(1)
                .setMaxValues(1)
                .addOption("Complete lobby", "completed")
                .addOption("Incompleted Lobby", "lobby_incompleted")
                .addOption("Score Lobby", "lobby_score")
                .addOption("Referee", "report_to_referee")
                .build();


        // 3) Invia embed + menu
        event.replyEmbeds(eb.build())
                .addActionRow(menu)
                .setEphemeral(true)
                .queue();

        if (event.getName().equals("complete_lobby")) {
            System.out.println("on complete part");
            Lobby lobby = LobbyManager.getLobby(discordId);

            if (lobby != null) {
                event.reply("✅ All participants need to react to this message to complete the lobby and archive.")
                        .setEphemeral(false)
                        .queue(messageHook -> {
                            messageHook.retrieveOriginal().queue(message -> {
                                // Crea l'oggetto Emoji per la spunta
                                Emoji checkEmoji = Emoji.fromUnicode("✅");

                                // Aggiunge la reazione ✅
                                message.addReaction(checkEmoji).queue();

                                // Salva l'ID del messaggio nella lobby per monitorare le reazioni
                                lobby.setCompletionMessageId(message.getId());

                                // Salva anche la lobby nel manager per il completamento
                                LobbyManager.saveLobbyCompletionMessage(lobby);

                                // NON incrementiamo qui i completed. Lo faremo solo a completamento avvenuto
                            });
                        });
            } else {
                event.reply("❌ No active lobby found to complete.")
                        .setEphemeral(true)
                        .queue();
            }
        }

        if (event.getName().equals("cancel")) {
            long userId = event.getUser().getIdLong();

            // Verifica se l'utente ha una lobby attiva
            if (!LobbyManager.hasLobby(userId)) {
                event.reply("❌ You don't have an active lobby to cancel.").setEphemeral(true).queue();
                return;
            }

            Lobby ownLobby = LobbyManager.getLobby(userId);
            System.out.println("[DEBUG] Lobby partecipants size: " + ownLobby.getPartecipants().size());
            // Controlla se la lobby ha altri partecipanti oltre all'owner
            if (ownLobby.getPartecipants().size() > 1) {
                event.reply("❌ You can't cancel a lobby that already has participants. Please complete it instead.")
                        .setEphemeral(true).queue();
                return;
            }

            // Cancella il post nel forum (thread)
            ownLobby.deletePost(event.getGuild());

            // Aggiorna statistiche di abbandono (disband)
            PlayerStats stats = PlayerStatsManager.getInstance().getPlayerStats(userId);
            if (stats == null) {
                stats = new PlayerStats();
                stats.setDiscordId(userId);
                PlayerStatsManager.getInstance().addOrUpdatePlayerStats(stats);
            }

            if (ownLobby.isDirectLobby()) {
                stats.incrementLobbiesDisbandedDirect();
            } else {
                stats.incrementLobbiesDisbandedGeneral();
            }

            PlayerStatMongoDBManager.updatePlayerStats(stats);

            // Rimuove la lobby dal manager
            LobbyManager.removeLobby(userId);
            LobbyManager.removeLobbyByCompletionMessageId(userId);

            // Risposta finale
            event.reply("🗑️ Your lobby has been successfully cancelled.").setEphemeral(true).queue();
        }


        if (event.getName().equals("block_user")) {
            long ownerId = event.getUser().getIdLong();
            Lobby lobby = LobbyManager.getLobby(ownerId); // Use your method to fetch the user's lobby

            if (lobby == null) {
                event.reply("❌ You don't have an active lobby.").setEphemeral(true).queue();
                return;
            }

            User target = event.getOption("user").getAsUser();
            long targetId = target.getIdLong();

            lobby.blockUser(targetId);

            event.reply("✅ " + target.getAsMention() + " has been blocked from your lobby.")
                    .setEphemeral(true).queue();
        }

        if (!event.getName().equals("direct")) return;

    }
    private void handleKickUserLobby(SlashCommandInteractionEvent event, long ownerId) {
        User toKick = event.getOption("player").getAsUser();
        long toKickId = toKick.getIdLong();

        Lobby lobby = LobbyManager.getLobby(ownerId);
        if (lobby == null || !lobby.isDirectLobby() || lobby.getDiscordId() != ownerId) {
            event.reply("❌ No direct lobby.").setEphemeral(true).queue();
            return;
        }
        if (!lobby.getPartecipants().contains(toKickId)) {
            event.reply("❌ User not in lobby.").setEphemeral(true).queue();
            return;
        }

        TextChannel priv = event.getGuild().getTextChannelById(lobby.getPrivateChannelId());
        Member member = event.getGuild().getMember(toKick);
        if (priv == null || member == null) {
            event.reply("❌ Channel or member missing.").setEphemeral(true).queue();
            return;
        }

        priv.getManager()
                .removePermissionOverride(member)
                .queue();

        lobby.getPartecipants().remove(toKickId);
        event.reply("✅ Kicked " + toKick.getAsMention()).setEphemeral(true).queue();
        priv.sendMessage(toKick.getAsMention() + " has been kicked from the lobby.").queue();
    }

    private void handleAddUserLobby(SlashCommandInteractionEvent event, long ownerId) {
        User toInvite = event.getOption("player").getAsUser();
        long toInviteId = toInvite.getIdLong();

        Lobby lobby = LobbyManager.getLobby(ownerId);
        if (lobby == null || !lobby.isDirectLobby() || lobby.getDiscordId() != ownerId) {
            event.reply("❌ You don’t have an active direct lobby.").setEphemeral(true).queue();
            return;
        }

        lobby.checkMaxpartecipants();
        if (lobby.getPartecipants().size() >= lobby.getMaxPartecipants()) {
            event.reply("❌ Your lobby is full (max " + lobby.getMaxPartecipants() + ").")
                    .setEphemeral(true).queue();
            return;
        }
        if (lobby.getPartecipants().contains(toInviteId) || lobby.isUserBlocked(toInviteId)) {
            event.reply("❌ User already in or blocked.").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();
        TextChannel priv = guild.getTextChannelById(lobby.getPrivateChannelId());
        Member member = guild.getMember(toInvite);
        if (priv == null || member == null) {
            event.reply("❌ Cannot find channel or member.").setEphemeral(true).queue();
            return;
        }

        // 1) Concedi VIEW ma nega esplicitamente SEND (read-only)
        priv.getManager()
                .putPermissionOverride(
                        member,
                        /* allow */ EnumSet.of(Permission.VIEW_CHANNEL),
                        /* deny  */ EnumSet.of(Permission.MESSAGE_SEND)
                )
                .queue(v -> {
                    // 2) Messaggio con bottoni Yes/No
                    Button yes = Button.success("lobby_add_yes:" + ownerId + ":" + toInviteId, "Yes");
                    Button no  = Button.danger ("lobby_add_no:"  + ownerId + ":" + toInviteId, "No");

                    priv.sendMessage(toInvite.getAsMention() + ", do you want to join this lobby?")
                            .setActionRow(yes, no)
                            .queue();

                    event.reply("✅ Invitation sent to " + toInvite.getAsMention())
                            .setEphemeral(true).queue();
                }, failure -> {
                    event.reply("❌ Failed to set view permission.").setEphemeral(true).queue();
                });
    }



    private void handleFreestyle(SlashCommandInteractionEvent event) {
        long discordId = event.getUser().getIdLong();
        Lobby existingLobby = LobbyManager.getLobby(discordId);

        if (existingLobby != null && !existingLobby.isCompleted()) {
            if (!existingLobby.isOwnerEligibleToCreateNewLobby()) {
                event.reply("❌ You already have an active lobby. Please complete it or wait for another participant to react before creating a new one.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        Lobby lobby = new Lobby();
        // player stat updated when a general lobby is created
        PlayerStats stats = new PlayerStats();
        stats.incrementLobbiesCreatedGeneral();
        Map<Long, PlayerStats> playerStatsMap = new HashMap<>();
        playerStatsMap.put(discordId,stats);
        PlayerStatMongoDBManager.updatePlayerStats(stats);

        lobby.setDiscordId(discordId);
        lobby.setCreatedAt(LocalDateTime.now());
        lobbySessions.put(discordId, lobby);
        promptLobbyTypeStep(event);
    }

    private void handleDirect(SlashCommandInteractionEvent event) {
        long discordId = event.getUser().getIdLong();
        Lobby existingLobby = LobbyManager.getLobby(discordId);

        if (existingLobby != null && !existingLobby.isCompleted()) {
            if (!existingLobby.isOwnerEligibleToCreateNewLobby()) {
                event.reply("❌ You already have an active lobby. Please complete it first.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }

        // 1) Crea solo in sessione temporanea, **senza** aggiungere al Manager
        Lobby lobby = new Lobby();
        lobby.setDirectLobby(true);
        lobby.setDiscordId(discordId);
        lobby.setCreatedAt(LocalDateTime.now());
        lobbySessions.put(discordId, lobby);

        // 2) Avvia il flow (es. promptLobbyTypeStep che mostrerà il Modal)
        promptLobbyTypeStep(event);
    }







    private void handleEditLobby(SlashCommandInteractionEvent event) {
        long discordId = event.getUser().getIdLong();
        Lobby lobby = LobbyManager.getLobby(discordId);

        if (lobby == null) {
            event.reply("❌ You don't have an active lobby to edit.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        lobbySessions.put(discordId, lobby);
        promptLobbyTypeStep(event);
    }

    private void promptLobbyTypeStep(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬▬▬▬ Choose Lobby Type ▬▬▬▬▬▬▬▬▬")
                .setDescription(" > Select the type of lobby you want to create.(Ranked and Endless will be implemented on the future)" +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferReply(true).queue(hook -> {
            hook.editOriginalEmbeds(embed.build())
                    .setComponents(
                            ActionRow.of(
                                    StringSelectMenu.create("lobby_type_select_lobby")
                                            .addOption("Ranked", "Ranked")
                                            .addOption("Player Match", "Player Match")
                                            .addOption("Endless", "Endless")
                                            .build()
                            )
                    )
                    .queue();
        });
    }
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        System.out.println("[Info] Received select interaction!");
        System.out.println("[Info] Component ID: " + event.getComponentId());
        System.out.println("[Info] Selected values: " + event.getValues());

        // Report referee viene gestito prima della lookup per utente
        if (event.getComponentId().equals("result:menu")) {
            String selected = event.getValues().get(0);
            System.out.println("Handling result:menu selection: " + selected);
            // Gestione via utente per le altre selezioni
            long discordId = event.getUser().getIdLong();
            Lobby lobby = LobbyManager.getLobby(discordId);

            if (lobby == null) {
                System.err.println("❌ No lobby associated with this player ID: " + discordId);
                event.reply("❌ No active lobby found for this player.").setEphemeral(true).queue();
                return;
            }

            switch (selected) {
                //to do completed and incompleted i need to do player stats
                case "completed" ->{
                    lobby.archivePost(event.getGuild());
                    event.reply("✅ Lobby marked as **completed**!").setEphemeral(true).queue();
                }
                case "lobby_incompleted" ->{
                    lobby.incompleteLobby();
                    event.reply("⚠️ Lobby marked as **incomplete**.").setEphemeral(true).queue();
                }
                case "lobby_score" -> {
                    event.getMessage().editMessageComponents().queue();  // ok, è un'azione separata

                    Modal modal = Modal.create("lobby_score_modal", "Enter Your Lobby Score")
                            .addActionRow(
                                    TextInput.create("score_input", "Score", TextInputStyle.SHORT)
                                            .setPlaceholder("e.g. 2-1, W-L")
                                            .setRequired(true)
                                            .build()
                            )
                            .build();

                    event.replyModal(modal).queue();
                }



                case "report_to_referee" -> {
                    lobby.callRefereeInPrivateChannel(event.getGuild());
                    event.reply("🛡️ A referee has been notified.").setEphemeral(true).queue();
                }
                default -> event.reply("❌ Unknown option selected.").setEphemeral(true).queue();
            }
            return;
        }

        // Gestione via utente per le altre selezioni
        long discordId = event.getUser().getIdLong();
        Lobby lobby = lobbySessions.get(discordId);
        if (lobby == null) {
            System.out.println("No lobby found for user ID: " + discordId);
            event.reply("❌ No active lobby found.").setEphemeral(true).queue();
            return;
        }

        switch (event.getComponentId()) {
            case "lobby_type_select_lobby" -> {
                System.out.println("[Info] Setting lobby type: " + event.getValues().get(0));
                lobby.setLobbyType(event.getValues().get(0));
                promptGameSelection(event);
            }
            case "lobby_game_select_lobby" -> {
                System.out.println("[Info] Setting game: " + event.getValues().get(0));
                lobby.setGame(event.getValues().get(0));
                promptPlatformSelection(event);
            }
            case "lobby_platform_select_lobby" -> {
                System.out.println("[Info] Setting platform: " + event.getValues().get(0));
                lobby.setPlatform(event.getValues().get(0));
                if (lobby.isDirectLobby()) {
                    promptConnectionTypeSelection(event);
                } else {
                    promptRegionSelection(event);
                }
            }
            case "lobby_region_select_lobby" -> {
                System.out.println("[Info] Setting region: " + event.getValues().get(0));
                lobby.setRegion(event.getValues().get(0));
                promptSkillLevelSelection(event);
            }
            case "lobby_skill_select_lobby" -> {
                System.out.println("[Info] Setting skill level: " + event.getValues().get(0));
                lobby.setSkillLevel(event.getValues().get(0));
                promptConnectionTypeSelection(event);
            }
            case "lobby_connection_select_lobby" -> {
                System.out.println("[Info] Setting connection type: " + event.getValues().get(0));
                lobby.setConnectionType(event.getValues().get(0));
                promptLobbyDetailsModal(event);
            }
            default -> System.out.println("[Info] Unknown componentId: " + event.getComponentId());
        }
    }



    private void promptGameSelection(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ 🎮 Select Game ▬▬▬▬▬▬")
                .setDescription(" > Choose the game for your lobby." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferEdit().queue();
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("lobby_game_select_lobby")
                                .addOption("Storm Connections", "Storm Connections")
                                .addOption("Storm Evolution", "Storm Evolution")
                                .addOption("Storm 4", "Storm 4")
                                .addOption("Storm Revolution", "Storm Revolution")
                                .addOption("Storm Trilogy", "Storm Trilogy")
                                .build()
                ))
                .queue();
    }

    private void promptPlatformSelection(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ 🕹️ Select Platform ▬▬▬▬▬▬")
                .setDescription(" > Choose your platform." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferEdit().queue();
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("lobby_platform_select_lobby")
                                .addOption("PC", "PC")
                                .addOption("Xbox", "Xbox")
                                .addOption("PlayStation", "PlayStation")
                                .addOption("Switch", "Switch")
                                .build()
                ))
                .queue();
    }

    private void promptRegionSelection(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬ 🌍 Select Region ▬▬▬▬▬▬▬")
                .setDescription(" > Choose the region that you want to be matched against." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferEdit().queue();
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("lobby_region_select_lobby")
                                .addOption("NA", "NA")
                                .addOption("EU", "EU")
                                .addOption("SA", "SA")
                                .addOption("JP", "JP")
                                .addOption("Asia", "Asia")
                                .build()
                ))
                .queue();
    }

    private void promptSkillLevelSelection(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬ 🎯 Select Skill Level ▬▬▬▬")
                .setDescription(" > Choose The skill level that you want to fight." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferEdit().queue();
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("lobby_skill_select_lobby")
                                .addOption("Beginner", "Beginner")
                                .addOption("Intermediate", "Intermediate")
                                .addOption("Advanced", "Advanced")
                                .build()
                ))
                .queue();
    }

    private void promptConnectionTypeSelection(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬ 📡 Select Connection Type ▬▬▬▬▬")
                .setDescription(" > Choose your connection type." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferEdit().queue();
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("lobby_connection_select_lobby")
                                .addOption("WiFi", "WiFi")
                                .addOption("Ethernet", "Ethernet")
                                .build()
                ))
                .queue();
    }

    private void promptLobbyDetailsModal(StringSelectInteractionEvent event) {
        // 🔴 Rimuove i componenti (es. dropdown) dal messaggio originale
        event.getMessage().editMessageComponents().queue();

        Modal modal = Modal.create("lobby_details_modal_lobby", "Complete Lobby Setup")
                .addActionRow(
                        TextInput.create("lobby_playername", "Your Nickname", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("e.g. User1234")
                                .setRequired(true)
                                .build()
                )
                .addActionRow(
                        TextInput.create("lobby_availability", "Availability", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Describe your availability, e.g. Mondays, Weekends...")
                                .setRequired(true)
                                .build()
                )
                .addActionRow(
                        TextInput.create("lobby_rule", "Lobby Rules", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Write the possible rules for the match, e.g. ban, character/stage selection, etc.")
                                .setRequired(true)
                                .build()
                )
                .build();

        event.replyModal(modal).queue();
    }

    public void changeTagFromOpenedToClosed(ThreadChannel threadChannel) {
        if (threadChannel == null) {
            System.err.println("❌ ThreadChannel is null.");
            return;
        }

        ForumChannel forum = (ForumChannel) threadChannel.getParentChannel();
        if (forum == null) {
            System.err.println("❌ Thread is not in a forum channel.");
            return;
        }

        // Trova il tag "closed"
        ForumTag closedTag = forum.getAvailableTags().stream()
                .filter(tag -> tag.getName().equalsIgnoreCase("Closed"))
                .findFirst()
                .orElse(null);

        if (closedTag == null) {
            System.err.println("❌ Tag 'closed' not found.");
            return;
        }

        // Cambia i tag applicati: qui rimuoviamo tutti e mettiamo solo "closed"
        threadChannel.getManager()
                .setAppliedTags(Collections.singleton(closedTag))
                .queue(
                        success -> System.out.println("✅ Tag changed to 'closed' successfully."),
                        error -> System.err.println("❌ Failed to change tag: " + error.getMessage())
                );
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();
        long discordId = event.getUser().getIdLong();

        // === 1) Modal punteggio ===
        if (modalId.equals("lobby_score_modal")) {
            String score = event.getValue("score_input").getAsString();
            PlayerStatsManager manager = PlayerStatsManager.getInstance();
            PlayerStats stats = manager.getPlayerStats(discordId);
            if (stats == null) {
                stats = new PlayerStats();
                stats.setDiscordId(discordId);
            }
            stats.setScore(score);
            manager.addOrUpdatePlayerStats(stats);
            event.reply("✅ Score saved successfully: `" + score + "`")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // === 2) Modal dettagli lobby ===
        if (!modalId.equals("lobby_details_modal_lobby")) {
            return;
        }

        Lobby lobby = lobbySessions.get(discordId);
        if (lobby == null) {
            event.reply("❌ Lobby session expired. Please start over.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Leggi i campi
        String playerName   = event.getValue("lobby_playername").getAsString();
        String availability = event.getValue("lobby_availability").getAsString();
        String rules        = event.getValue("lobby_rule").getAsString();

        // Aggiorna la lobby
        lobby.setPlayerName(playerName);
        lobby.setAvailability(availability);
        lobby.setRules(rules);
        lobby.setCreatedAt(LocalDateTime.now());

        Guild guild = event.getGuild();
        Member member = guild.getMember(event.getUser());
        if (member == null) {
            event.reply("❌ Could not find your member in this guild.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // === 3) Edit vs Nuova ===
        boolean isEdit = LobbyManager.getLobby(discordId) != null;
        if (isEdit) {
            try {
                lobby.updateLobbyPost(guild);
                event.reply("✅ Lobby updated successfully!").setEphemeral(true).queue();
                lobby.sendLobbyLog(guild, 1380683537501519963L);
                LobbyManager.addLobby(discordId, lobby);
            } catch (Exception e) {
                event.reply("❌ Failed to update lobby embed.").setEphemeral(true).queue();
                e.printStackTrace();
            }
            return;
        }

        // === 4) Nuova lobby ===
        event.deferReply(true).queue(); // ephemerale risposta

        if (lobby.isDirectLobby()) {
            // --- Direct (privata) ---
            long directLogChannelId = 1380683537501519963L;
            long directCategoryId   = 1381025760231555077L;

            // ✅ Nuovo metodo compatto
            lobby.sendDirectCreationLobbyLog(guild, directLogChannelId, directCategoryId);

            // ✅ Conferma ephemerale (opzionale: può stare nel metodo anche)
            event.getHook().sendMessage("✅ Private lobby channel creation in progress...").setEphemeral(true).queue();

        } else {
            // --- Generale (forum post + log) ---
            try {
                lobby.sendLobbyAnnouncement(guild, 1367186054045761616L);
                LobbyManager.addLobby(discordId, lobby);
                event.getHook().sendMessage("✅ Lobby created successfully!").setEphemeral(true).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.getHook().sendMessage("❌ Failed to create lobby post.").setEphemeral(true).queue();
            }
        }

        // === 5) Aggiorna statistiche ===
        PlayerStatsManager statsManager = PlayerStatsManager.getInstance();
        PlayerStats hostStats = statsManager.getPlayerStats(discordId);
        if (hostStats == null) {
            hostStats = new PlayerStats();
            hostStats.setDiscordId(discordId);
        }
        if (lobby.isDirectLobby()) {
            hostStats.incrementLobbiesCreatedDirect();
        } else {
            hostStats.incrementLobbiesCreatedGeneral();
        }
        statsManager.addOrUpdatePlayerStats(hostStats);
        PlayerStatMongoDBManager.updatePlayerStats(hostStats);

        // === 6) Rimuovi sessione temporanea ===
        lobbySessions.remove(discordId);
    }






    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        // Ignora i bot
        if (event.getUser().isBot()) return;

        // Verifica che siamo nella categoria lobby
        GuildChannel guildChannel = event.getGuild().getGuildChannelById(event.getChannel().getId());
        if (guildChannel == null) return;

        Category category = guildChannel.getJDA().getCategoryById("1381025760231555077");
        if (category == null || !category.getId().equals("1381025760231555077")) return;

        // Recupera la lobby tramite il completionMessageId
        Lobby lobby = LobbyManager.getLobbyByCompletionMessageId(event.getMessageIdLong());
        if (lobby == null) return;

        // Controlliamo se a reagire è il creator della lobby
        if (event.getUser().getIdLong() != lobby.getDiscordId()) {
            System.out.println("❌ Solo il creatore della lobby può completarla.");
            return;
        }

        // Verifica che abbia reagito col ✅
        if (!event.getEmoji().getName().equals("✅")) {
            return;
        }

        // Protezione: evitiamo che venga completata due volte
        if (lobby.isCompleted()) {
            System.out.println("⚠️ La lobby è già completata.");
            return;
        }

        // Archiviazione thread forum
        ThreadChannel threadChannel = event.getGuild().getThreadChannelById(lobby.getPostId());
        if (threadChannel != null) {
            changeTagFromOpenedToClosed(threadChannel);  // aggiorna i tag forum
            lobby.archivePost(event.getGuild());
            threadChannel.sendMessage("✅ Lobby completed and archived.").queue();
        } else {
            System.err.println("❌ ThreadChannel not found for PostId: " + lobby.getPostId());
        }

        // Aggiorna lo stato
        lobby.completeLobby();

        // Rimuove la lobby dalle mappe
        LobbyManager.removeLobbyByCompletionMessageId(event.getMessageIdLong());
        LobbyManager.removeLobby(lobby.getDiscordId());

        // Rimuove i permessi al creator nel canale privato
        TextChannel privateChannel = event.getGuild().getTextChannelById(lobby.getPrivateChannelId());
        if (privateChannel != null) {
            privateChannel.getPermissionOverride(event.getGuild().getMemberById(lobby.getDiscordId()))
                    .delete().queue(
                            success -> System.out.println("✅ Permessi rimossi dal canale privato."),
                            error -> System.err.println("❌ Errore rimuovendo permessi: " + error.getMessage())
                    );
        }

        System.out.println("✅ Lobby completata e pulita correttamente.");
    }






}
