package Lobby;

import Config.Config;
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
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private Config config = new Config();
    private  final String GUILD_ID = config.getGuildId();
    private static final String TICKET_CATEGORY_NAME = "Ninja disputes";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        long discordId = event.getUser().getIdLong();

        switch (event.getName()) {
            case "create_lobby" -> handleFreestyle(event);
            case "edit_lobby" -> handleEditLobby(event);
            case "create_private_lobby" -> handleDirect(event);
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

        if (event.getName().equalsIgnoreCase("leave_lobby")) {
            long userId = event.getUser().getIdLong();
            Lobby lobby = LobbyManager.getLobby(userId);

            if (lobby == null) {
                event.reply("You are not in a lobby.").setEphemeral(true).queue();
                return;
            }

            if (lobby.getPartecipants().size() > 1) {
                event.reply("You cannot leave a lobby with other participants.").setEphemeral(true).queue();
                return;
            }

            // Recupera le stats del player
            PlayerStats stats = PlayerStatsManager.getInstance().getPlayerStats(userId);
            if (stats == null) {
                stats = new PlayerStats();
                stats.setDiscordId(userId);
            }

            // Incrementa le stat in base al tipo di lobby
            if (lobby.isDirectLobby()) {
                stats.incrementLobbiesDisbandedDirect();
            } else {
                stats.incrementLobbiesDisbandedGeneral();
            }

            // Aggiorna in memoria e DB
            PlayerStatsManager.getInstance().addOrUpdatePlayerStats(stats);
            PlayerStatMongoDBManager.updatePlayerStats(stats);

            // Rimuovi la lobby
            LobbyManager.removeLobby(userId);

            // Invia messaggio di conferma
            event.reply("Lobby successfully disbanded. You will no longer have access to the private channel.")
                    .setEphemeral(true)
                    .queue(success -> {
                        Guild guild = event.getGuild();
                        Member member = event.getMember();

                        if (guild != null && member != null) {
                            if (!lobby.isDirectLobby()) {
                                ThreadChannel threadChannel = guild.getThreadChannels().stream()
                                        .filter(thread -> thread.getIdLong() == lobby.getPostId())
                                        .findFirst()
                                        .orElse(null);

                                if (threadChannel != null) {
                                    System.out.println("Thread trovato: " + threadChannel.getId());

                                    // Applica i tag nel thread (metodo esterno, non modificato)
                                    lobby.applyClosedTagsToThread(threadChannel);

                                    // Prepara embed
                                    EmbedBuilder embed = new EmbedBuilder()
                                            .setTitle("▬▬▬▬▬▬▬▬ Lobby Behavior ▬▬▬▬▬▬▬▬")
                                            .setDescription("This lobby has been disbanded by the host or participant.")
                                            .setFooter("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                                            .setColor(Color.decode("#1c0b2e"));

                                    // Invia embed con log di successo/errore
                                    threadChannel.sendMessageEmbeds(embed.build()).queue(
                                            successMsg -> System.out.println("✅ Embed inviato nel thread."),
                                            failure -> System.err.println("❌ Errore invio embed: " + failure.getMessage())
                                    );

                                    // Archivia e blocca il thread
                                    threadChannel.getManager()
                                            .setArchived(true)
                                            .setLocked(true)
                                            .queue(
                                                    successArchive -> System.out.println("✅ Thread archiviato e bloccato."),
                                                    errorArchive -> System.err.println("❌ Errore archiviazione/lock: " + errorArchive.getMessage())
                                            );

                                } else {
                                    System.err.println("❌ Thread channel non trovato.");
                                }
                            }

                            // Rimuovi permessi dal canale privato se esiste
                            TextChannel privateChannel = guild.getTextChannelById(lobby.getPrivateChannelId());
                            if (privateChannel != null) {
                                var override = privateChannel.getPermissionOverride(member);
                                if (override != null) {
                                    override.getManager()
                                            .deny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                                            .queue();
                                }
                            }
                        }
                    });
            // Rimuove il ruolo all'utente
            Guild guild = event.getGuild();
            Member member = event.getMember();
            if (guild != null && member != null) {
                Role role = guild.getRoleById(1391728467367694448L);
                if (role != null) {
                    guild.removeRoleFromMember(member, role).queue(
                            success -> System.out.println("[Info] Role removed successfully."),
                            error -> System.err.println("❌ Failed to remove role: " + error.getMessage())
                    );
                } else {
                    System.err.println("❌ Role not found.");
                }
            }
        }

        if (!event.getName().equals("close_lobby")) return;

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
                                //     "> * **Report Lobby Score:** with this option you can report  the score of the lobby\n" +
                                "> * **Incomplete lobby:** this option will mark your lobby as incompleted\n" +
                                "> * **Referee:** This option will contact an admin to do as referee or to report something.\n"

                        //                     "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
                )
                .setImage("https://64.media.tumblr.com/87273056002f62ae5f1b6417c001170f/f4a7711e15cf7a55-f5/s1280x1920/ec0568cc634542254ba8908d2de2861fda4d171f.gif")
                .setColor(Color.white);
//- Report Lobby Score (Score option only shows up when match format is set to casual 1v1 or group FT sets, Clan Battle)
        // 2) Costruisci il dropdown menu
        StringSelectMenu menu = StringSelectMenu.create("lobby:menu")
                .setPlaceholder("Choose an option for the lobby")
                .setMinValues(1)
                .setMaxValues(1)
                .addOption("Complete lobby", "completed")
                .addOption("Incompleted Lobby", "lobby_incompleted")
                //   .addOption("Score Lobby", "lobby_score")
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

        // Recupera o crea le statistiche dell'utente invitato
        PlayerStatsManager pm = PlayerStatsManager.getInstance();
        PlayerStats invitedStats = pm.getPlayerStats(toInviteId);
        if (invitedStats == null) {
            invitedStats = new PlayerStats();
            invitedStats.setDiscordId(toInviteId);
            pm.addOrUpdatePlayerStats(invitedStats);
        }

        // Incrementa ignoredRequestDirect
        invitedStats.incrementIgnoredRequestDirect();
        PlayerStatMongoDBManager.updatePlayerStats(invitedStats);

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
        promptPlatformSelection(event);
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
        System.out.println("direct property: "+lobby.isDirectLobby());
        lobby.setDiscordId(discordId);
        lobby.setCreatedAt(LocalDateTime.now());
        lobbySessions.put(discordId, lobby);

        // 2) Avvia il flow (es. promptLobbyTypeStep che mostrerà il Modal)
        promptPlatformSelection(event);
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
        promptPlatformSelection(event);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        System.out.println("[Info] Received select interaction!");
        System.out.println("[Info] Component ID: " + event.getComponentId());
        System.out.println("[Info] Selected values: " + event.getValues());

        if (event.getComponentId().equals("lobby:menu")) {
            String selected = event.getValues().get(0);
            System.out.println("Handling lobby:menu selection: " + selected);

            long discordId = event.getUser().getIdLong();
            Lobby lobby = LobbyManager.getLobby(discordId);

            if (lobby == null) {
                event.reply("❌ No active lobby found for this player.").setEphemeral(true).queue();
                return;
            }

            switch (selected) {
                case "completed" -> {
                    if (lobby.getPartecipants().size() <= 1) {
                        event.getMessage().delete().queue();
                        event.reply("❌ You cannot complete a lobby without participants.").setEphemeral(true).queue();
                        return;
                    }

                    ThreadChannel threadChannel = event.getGuild().getThreadChannels().stream()
                            .filter(thread -> thread.getIdLong() == lobby.getPostId())
                            .findFirst()
                            .orElse(null);

                    // Chiamata al metodo che imposta tutti i tag
                    lobby.applyClosedTagsToThread(threadChannel);
                    lobby.archivePost(event.getGuild());

                    event.reply("✅ Lobby marked as **completed**!").setEphemeral(true).queue(success -> {
                        event.getMessage().delete().queue();
                    });

                    // Rimuove il ruolo all'utente
                    Guild guild = event.getGuild();
                    Member member = event.getMember();
                    if (guild != null && member != null) {
                        Role role = guild.getRoleById(1391728467367694448L);
                        if (role != null) {
                            guild.removeRoleFromMember(member, role).queue(
                                    success -> System.out.println("[Info] Role removed successfully."),
                                    error -> System.err.println("❌ Failed to remove role: " + error.getMessage())
                            );
                        } else {
                            System.err.println("❌ Role not found.");
                        }
                    }
                }

                case "lobby_incompleted" -> {
                    // Recupera il thread associato alla lobby
                    ThreadChannel threadChannel = event.getGuild().getThreadChannels().stream()
                            .filter(thread -> thread.getIdLong() == lobby.getPostId())
                            .findFirst()
                            .orElse(null);

                    // Applica i tag (modifica o crea il metodo giusto in Lobby)
                    if (threadChannel != null) {
                        lobby.applyClosedTagsToThread(threadChannel);  // supponendo che esista o lo crei tu
                        lobby.incompleteLobby(event.getGuild());
                    }

                    // Segnala lobby incompleta

                    event.reply("⚠️ Lobby marked as **incomplete**.").setEphemeral(true).queue(success -> {
                        event.getMessage().delete().queue();
                    });

                    // Rimuove il ruolo all'utente (come prima)
                    Guild guild = event.getGuild();
                    Member member = event.getMember();
                    if (guild != null && member != null) {
                        Role role = guild.getRoleById(1391728467367694448L);
                        if (role != null) {
                            guild.removeRoleFromMember(member, role).queue(
                                    success -> System.out.println("[Info] Role removed successfully."),
                                    error -> System.err.println("❌ Failed to remove role: " + error.getMessage())
                            );
                        } else {
                            System.err.println("❌ Role not found.");
                        }
                    }
                }

                case "lobby_score" -> {
                    event.getMessage().editMessageComponents().queue();

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
                    if (!event.isAcknowledged()) {
                        event.deferReply(true).queue(v -> {
                            lobby.callRefereeInPrivateChannel(event.getGuild());
                            createTicket(event);
                        });
                    } else {
                        lobby.callRefereeInPrivateChannel(event.getGuild());
                        createTicket(event);
                    }
                }
                default -> event.reply("❌ Unknown option selected.").setEphemeral(true).queue();
            }
            return;
        }

        switch (event.getComponentId()) {
            case "lobby_platform_select_lobby" -> {
                Lobby lobby = lobbySessions.get(event.getUser().getIdLong());
                if (lobby == null) {
                    event.reply("❌ No active lobby found.").setEphemeral(true).queue();
                    return;
                }
                String selectedPlatform = event.getValues().get(0);
                System.out.println("[Info] Setting platform: " + selectedPlatform);
                lobby.setPlatform(selectedPlatform);

                if (lobby.isDirectLobby()) {
                    promptGameSelection(event,selectedPlatform);
                } else {
                    promptGameSelection(event, selectedPlatform);  // Mostra giochi in base alla piattaforma
                }
            }
            case "lobby_game_select_lobby" -> {
                Lobby lobby = lobbySessions.get(event.getUser().getIdLong());
                if (lobby == null) {
                    event.reply("❌ No active lobby found.").setEphemeral(true).queue();
                    return;
                }
                System.out.println("[Info] Setting game: " + event.getValues().get(0));
                lobby.setGame(event.getValues().get(0));
                if(lobby.isDirectLobby()) {
                    promptLobbyTypeStep(event); // Salta FPS se è Direct
                } else {
                    promptLobbyDurtationSelection(event);
                }
            }

            case "lobby_duration_select" -> {
                Lobby lobby = lobbySessions.get(event.getUser().getIdLong());
                if (lobby == null) {
                    event.reply("❌ No active lobby found.").setEphemeral(true).queue();
                    return;
                }

                String selectedValue = event.getValues().get(0);
                int durationMinutes = Integer.parseInt(selectedValue);
                System.out.println("[Info] Lobby duration set to " + durationMinutes + " minutes");

                lobby.setDurationMinutes(durationMinutes); // assicurati che questo metodo esista

                // Avvia il timer per marcare la lobby come incompleta allo scadere
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.schedule(() -> {
                    if (!lobby.isCompleted()) {
                        lobby.incompleteLobby(event.getGuild()); // assicurati che questo metodo esista
                        System.out.println("⏰ Lobby marked as incomplete after " + durationMinutes + " minutes.");
                    } else {
                        System.out.println("⏳ Timer ended, but lobby was already locked or completed. No action taken.");
                    }
                }, durationMinutes, TimeUnit.MINUTES);

                promptFpsSelection(event);


            }

            case "lobby_fps_select" -> {
                Lobby lobby = lobbySessions.get(event.getUser().getIdLong());
                if (lobby == null) {
                    event.reply("❌ No active lobby found.").setEphemeral(true).queue();
                    return;
                }
                System.out.println("[Info] Setting FPS: " + event.getValues().get(0));
                lobby.setFps(event.getValues().get(0));
                promptLobbyTypeStep(event);
                //  promptRegionSelection(event);
                // AGGIUNGI IL COSO DA CHIAMARE
            }

            case "lobby_type_select_lobby" -> {
                Lobby lobby = lobbySessions.get(event.getUser().getIdLong());
                if (lobby == null) {
                    event.reply("❌ No active lobby found.").setEphemeral(true).queue();
                    return;
                }
                System.out.println("[Info] Setting lobby type: " + event.getValues().get(0));
                lobby.setLobbyType(event.getValues().get(0));
                if (lobby.isDirectLobby()) {
                    promptConnectionTypeSelection(event); // Salta Region e Skill Level
                } else {
                    promptRegionSelection(event);
                }
            }

            case "lobby_region_select_lobby" -> {
                Lobby lobby = lobbySessions.get(event.getUser().getIdLong());
                if (lobby == null) {
                    event.reply("❌ No active lobby found.").setEphemeral(true).queue();
                    return;
                }
                System.out.println("[Info] Setting region: " + event.getValues().get(0));
                lobby.setRegion(event.getValues().get(0));
                promptSkillLevelSelection(event);
            }
            case "lobby_skill_select_lobby" -> {
                Lobby lobby = lobbySessions.get(event.getUser().getIdLong());
                if (lobby == null) {
                    event.reply("❌ No active lobby found.").setEphemeral(true).queue();
                    return;
                }
                System.out.println("[Info] Setting skill level: " + event.getValues().get(0));
                lobby.setSkillLevel(event.getValues().get(0));
                promptConnectionTypeSelection(event);
            }
            case "lobby_connection_select_lobby" -> {
                Lobby lobby = lobbySessions.get(event.getUser().getIdLong());
                if (lobby == null) {
                    event.reply("❌ No active lobby found.").setEphemeral(true).queue();
                    return;
                }

                System.out.println("[Info] Setting connection type: " + event.getValues().get(0));
                lobby.setConnectionType(event.getValues().get(0));

                // Assegna il ruolo
                Guild guild = event.getGuild();
                Member member = event.getMember();
                if (guild != null && member != null) {
                    Role role = guild.getRoleById(1391728467367694448L);
                    if (role != null) {
                        guild.addRoleToMember(member, role).queue(
                                success -> System.out.println("[Info] Role assigned successfully."),
                                error -> System.err.println("❌ Failed to assign role: " + error.getMessage())
                        );
                    } else {
                        System.err.println("❌ Role not found.");
                    }
                }

                promptLobbyDetailsModal(event);
            }
            default -> System.out.println("[Info] Unknown componentId: " + event.getComponentId());
        }
    }

    private void promptGameSelection(StringSelectInteractionEvent event, String platform) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ 🎮 Select Game ▬▬▬▬▬▬")
                .setDescription(" > Choose the game for your lobby." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("lobby_game_select_lobby");
        if (!platform.equalsIgnoreCase("RPCS3") &&  !platform.equalsIgnoreCase("PS3") &&  !platform.equalsIgnoreCase("Xbox 360")) {

            // Opzioni base per tutti ma non per rpcs3
            menuBuilder.addOption("NSUNS2", "NSUNS2");
            menuBuilder.addOption("NSUNSFB", "NSUNSFB");
            menuBuilder.addOption("NSUNSR", "NSUNSR");
            menuBuilder.addOption("NSUNSRTB", "NSUNSRTB");
            menuBuilder.addOption("NXBUNSC", "NXBUNSC");
        }
        // Aggiunte per PC
        if (platform.equalsIgnoreCase("PC")) {
            menuBuilder.addOption("NSUNSE", "NSUNSE");
        }

        if (platform.equalsIgnoreCase("RPCS3")) {
             menuBuilder.addOption("NSUNS", "NSUNS")
                        .addOption("NSUNS2", "NSUNS2")
                        .addOption("NSUNSG", "NSUNSG")
                        .addOption("NSUNS3", "NSUNS3")
                        .addOption("NSUNSFB", "NSUNSFB")
                        .addOption("NSUNSR", "NSUNSR");
        }

        if (platform.equalsIgnoreCase("PS3")) {
            menuBuilder.addOption("NSUNS2", "NSUNS2")
                       .addOption("NSUNSG", "NSUNSG")
                       .addOption("NSUNSFB", "NSUNSFB");
        }

        if (platform.equalsIgnoreCase("Xbox 360")) {
            menuBuilder.addOption("NSUNS2", "NSUNS2")
                       .addOption("NSUNSG", "NSUNSG")
                       .addOption("NSUNSFB", "NSUNSFB");
        }

        event.deferEdit().queue();
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(menuBuilder.build()))
                .queue();
    }


    private void promptFpsSelection(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ Select Your Target Fps ▬▬▬▬▬▬")
                .setDescription(" > Choose the fps that you are currently playing." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferEdit().queue();
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("lobby_fps_select")
                                .setMinValues(1)
                                .addOption("30 Fps", "30 Fps")
                                .addOption("60 Fps", "60 Fps")
                                .addOption("Any", "Any")
                                .build()
                ))
                .queue();
    }

    private void promptPlatformSelection(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬ 🕹️ Select Platform ▬▬▬▬▬▬")
                .setDescription(" > Choose your platform." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.replyEmbeds(embed.build())
                .addActionRow(
                        StringSelectMenu.create("lobby_platform_select_lobby")
                                .addOption("PC", "PC")
                                .addOption("RPCS3", "RPCS3")
                                .addOption("Xbox Series X", "Xbox Series X")
                                .addOption("Xbox Series S", "Xbox Series S")
                                .addOption("Xbox 360", "Xbox 360")
                                .addOption("PS5", "PS5")
                                .addOption("PS4", "PS4")
                                .addOption("PS3", "PS3")
                                .addOption("Nintendo Switch 1", "Nintendo Switch 1")
                                .addOption("Nintendo Switch 2", "Nintendo Switch 2")
                                .build()
                )
                .setEphemeral(true)
                .queue();
    }

        // VECCHIO
/*
        event.deferEdit().queue();
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        StringSelectMenu.create("lobby_platform_select_lobby")
                                .addOption("PC", "PC")
                                .addOption("Xbox Series X", "Xbox Series X")
                                .addOption("Xbox Series S", "Xbox Series S")
                                .addOption("PS5", "PS5")
                                .addOption("PS4", "PS4")
                                .addOption("Nintendo Switch 1", "Nintendo Switch 1")
                                .addOption("Nintendo Switch 2", "Nintendo Switch 2")

                                .build()
                ))
                .queue();

 */
        private void promptLobbyTypeStep(StringSelectInteractionEvent event) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("▬▬▬▬▬▬▬▬▬▬ Choose Lobby Type ▬▬▬▬▬▬▬▬▬")
                    .setDescription(" > Select the type of lobby you want to create. (Ranked and Endless will be implemented in the future)" +
                            "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                    .setColor(Color.white);

            event.deferEdit().queue();
            event.getHook().editOriginalEmbeds(embed.build())
                    .setComponents(
                            ActionRow.of(
                                    StringSelectMenu.create("lobby_type_select_lobby")
             //                               .addOption("Ranked", "Ranked")
                                            .addOption("Player Match", "Player Match")
                                            .addOption("Endless", "Endless")
                                            .addOption("Tournament", "Tournament")
                                            .addOption("Clan Battle", "Clan Battle")
                                            .build()
                            )
                    )
                    .queue();
        }

    private void promptLobbyDurtationSelection(StringSelectInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("▬▬▬▬▬▬▬▬▬ ⏰ Select Lobby Duration ▬▬▬▬▬▬▬▬▬")
                .setDescription(" > For how much time are you available to play on this lobby?" +
                        "\n (After that time, the lobby will be marked as incomplete if you don't close it first)." +
                        "\n\n▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .setColor(Color.white);

        event.deferEdit().queue(); // <- questa rimuove il "loading" del menu
        event.getHook().editOriginalEmbeds(embed.build())
                .setComponents(
                        ActionRow.of(
                                StringSelectMenu.create("lobby_duration_select")
                                        .addOption("30 minutes", "30")
                                        .addOption("45 minutes", "45")
                                        .addOption("60 minutes", "60")
                                        .addOption("75 minutes", "75")
                                        .addOption("90 minutes", "90")
                                        .addOption("120 minutes", "120")
                                        .build()
                        )
                )
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
                                .addOption("Europe", "Europe")
                                .addOption("North America", "North America")
                                .addOption("Canada", "Canada")
                                .addOption("Central America", "Central America")
                                .addOption("South America", "South America")
                                .addOption("East Asia", "East Asia")
                                .addOption("East Europe", "East Europe")
                                .addOption("Asia", "Asia")
                                .addOption("Middle East", "Middle East")
                                .addOption("Africa", "Africa")
                                .addOption("Oceania", "Oceania")
                                .addOption("Any", "Any")

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
                                .addOption("Beginner", "LF Beginner")
                                .addOption("Intermediate", "LF Intermediate")
                                .addOption("Advanced", "LF Advanced")
                                .addOption("Any", "LF Any")
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
                                .addOption("Any", "Any")
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
                        TextInput.create("lobby_rule", "Lobby Rules", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("Write the possible rules for the match, e.g. ban, character/stage selection, etc.")
                                .setRequired(true)
                                .build()
                )
                .build();

        event.replyModal(modal).queue();
    }

    public void changeTagFromOpenedToClosed(Lobby lobby, ThreadChannel threadChannel) {
        if (lobby == null) {
            System.err.println("❌ Lobby is null.");
            return;
        }

        if (threadChannel == null) {
            System.err.println("❌ ThreadChannel is null.");
            return;
        }

        String skillLevel = lobby.getSkillLevel();
        List<String> validSkillLevels = Arrays.asList("LF Beginner", "LF Intermediate", "LF Advanced", "LF Any");

        if (skillLevel == null || !validSkillLevels.contains(skillLevel)) {
            System.err.println("❌ Invalid or missing skill level in lobby: " + skillLevel);
            return;
        }

        ForumChannel forum = (ForumChannel) threadChannel.getParentChannel();
        if (forum == null) {
            System.err.println("❌ Thread is not in a forum channel.");
            return;
        }

        List<ForumTag> availableTags = forum.getAvailableTags();
        if (availableTags == null || availableTags.isEmpty()) {
            System.err.println("❌ No tags available in the forum.");
            return;
        }

        ForumTag closedTag = availableTags.stream()
                .filter(tag -> tag.getName().equalsIgnoreCase("Closed"))
                .findFirst()
                .orElse(null);

        if (closedTag == null) {
            System.err.println("❌ Tag 'Closed' not found.");
            return;
        }

        ForumTag skillLevelTag = availableTags.stream()
                .filter(tag -> tag.getName().equalsIgnoreCase(skillLevel))
                .findFirst()
                .orElse(null);

        if (skillLevelTag == null) {
            System.err.println("❌ Skill level tag '" + skillLevel + "' not found.");
            return;
        }

        threadChannel.getManager()
                .setAppliedTags(Arrays.asList(closedTag, skillLevelTag))
                .queue(
                        success -> System.out.println("✅ Tags changed to 'Closed' and '" + skillLevel + "', thread archived successfully."),
                        error -> System.err.println("❌ Failed to change tags or archive thread: " + error.getMessage())
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
        String rules        = event.getValue("lobby_rule").getAsString();

        // Aggiorna la lobby
        lobby.setPlayerName(playerName);
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
                lobby.sendLobbyLog(guild, Long.parseLong(config.getLobbyChannelLog()));
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
            long directLogChannelId = Long.parseLong(config.getLobbyChannelLog());
            long directCategoryId   = Long.parseLong(config.getLobbyCategory());

            // ✅ Nuovo metodo compatto
            lobby.sendDirectCreationLobbyLog(guild, directLogChannelId, directCategoryId);


            // ✅ Conferma ephemerale (opzionale: può stare nel metodo anche)
            event.getHook().sendMessage("✅ Private lobby channel creation in progress...").setEphemeral(true).queue();

        } else {
            // --- Generale (forum post + log) ---
            try {
                lobby.sendLobbyAnnouncement(guild, Long.parseLong(config.getMatchmakingForumPost()), () -> {
                    lobby.sendLobbyLog(guild, Long.parseLong(config.getLobbyChannelLog()));
                    LobbyManager.addLobby(discordId, lobby);
                    event.getHook().sendMessage("✅ Lobby created successfully!").setEphemeral(true).queue();
                });

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

/*
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
            Lobby l = LobbyManager.getLobby(event.getUser().getIdLong());
            String skill= l.getSkillLevel();
            changeTagFromOpenedToClosed(threadChannel,skill);  // aggiorna i tag forum
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


 */

    private void createTicket(StringSelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null || !guild.getId().equals(GUILD_ID)) return;

        User user = event.getUser();
        Member member = guild.getMember(user);
        if (member == null) return;

        Category category = guild.getCategoriesByName(TICKET_CATEGORY_NAME, true)
                .stream().findFirst()
                .orElseGet(() -> guild.createCategory(TICKET_CATEGORY_NAME).complete());

        String originalName = user.getName() + "-ticket";
        String channelName = originalName.toLowerCase().replace(" ", "-").replaceAll("-+", "-");

        boolean openTicketExists = category.getTextChannels().stream().anyMatch(c ->
                c.getName().equals(channelName) &&
                        c.getPermissionOverride(member) != null &&
                        !c.getPermissionOverride(member).getDenied().contains(Permission.VIEW_CHANNEL)
        );

        if (openTicketExists) {
            event.getHook().sendMessage("❗You already have an open ticket.").setEphemeral(true).queue();
            return;
        }

        category.createTextChannel(channelName)
                .addPermissionOverride(guild.getPublicRole(), 0L, Permission.VIEW_CHANNEL.getRawValue())
                .addPermissionOverride(member,
                        Permission.VIEW_CHANNEL.getRawValue()
                                | Permission.MESSAGE_SEND.getRawValue()
                                | Permission.MESSAGE_HISTORY.getRawValue(),
                        0L)
                .setTopic("Ticket Owner ID: " + user.getId())
                .queue(channel -> {
                    channel.sendMessage(user.getAsMention() + " — Ticket created: here you can talk with a referee to fix a problem or to report a problem **")
                            .addActionRow(Button.danger("ticket:close", "Close Ticket")).queue();

                    event.getHook().sendMessage("✅ Your ticket has been created: " + channel.getAsMention()).setEphemeral(true).queue();
                });
    }




}