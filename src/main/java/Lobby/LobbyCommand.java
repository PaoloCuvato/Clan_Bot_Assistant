package Lobby;

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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
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

        // Check if user option exists (if you need it)
        OptionMapping userOption = event.getOption("user");
        if (userOption == null) {
            event.reply("❌ You must specify a user for the direct lobby.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        User target = userOption.getAsUser();

        Lobby lobby = new Lobby();
        lobby.setDiscordId(discordId);
        lobby.setCreatedAt(LocalDateTime.now());
        lobby.setDirectLobby(true);
        // Maybe you want to block the target user here? Example:
        lobby.setAllowedUserId(target.getIdLong());
        lobbySessions.put(discordId, lobby);
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
        long discordId = event.getUser().getIdLong();
        Lobby lobby = lobbySessions.get(discordId);
        if (lobby == null) return;

        switch (event.getComponentId()) {
            case "lobby_type_select_lobby" -> {
                lobby.setLobbyType(event.getValues().get(0));
                promptGameSelection(event);
            }
            case "lobby_game_select_lobby" -> {
                lobby.setGame(event.getValues().get(0));
                promptPlatformSelection(event);
            }
            case "lobby_platform_select_lobby" -> {
                lobby.setPlatform(event.getValues().get(0));

                // Qui facciamo il salto:
                if (lobby.isDirectLobby()) {
                    promptConnectionTypeSelection(event);  // salto region e skill
                } else {
                    promptRegionSelection(event);
                }
            }

            case "lobby_region_select_lobby" -> {
                lobby.setRegion(event.getValues().get(0));
                promptSkillLevelSelection(event);
            }
            case "lobby_skill_select_lobby" -> {
                lobby.setSkillLevel(event.getValues().get(0));
                promptConnectionTypeSelection(event);
            }
            case "lobby_connection_select_lobby" -> {
                lobby.setConnectionType(event.getValues().get(0));
                promptLobbyDetailsModal(event);
            }
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
        if (!event.getModalId().equals("lobby_details_modal_lobby")) return;

        long discordId = event.getUser().getIdLong();
        Lobby lobby = lobbySessions.get(discordId);

        if (lobby == null) {
            event.reply("❌ Lobby session expired. Please start over.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Prendi i dati inseriti dal modal
        String playerName = event.getValue("lobby_playername").getAsString();
        String availability = event.getValue("lobby_availability").getAsString();
        String rules = event.getValue("lobby_rule").getAsString();

        // Aggiorna la lobby con i nuovi valori
        lobby.setPlayerName(playerName);
        lobby.setAvailability(availability);
        lobby.setRules(rules);
        lobby.setCreatedAt(LocalDateTime.now());
        System.out.println("lobby: \n" + lobby.toString());

        // Controllo: se esiste già in LobbyManager è un edit
        if (LobbyManager.getLobby(discordId) != null) {
            // E' un edit → aggiorniamo il messaggio embedded usando il metodo dedicato
            try {
                lobby.updateLobbyPost(event.getGuild());
                event.reply("✅ Lobby updated successfully!").setEphemeral(true).queue();

                // aggiorna log se vuoi
                lobby.sendLobbyLog(event.getGuild(), 1380683537501519963L);

                // aggiorna lobby nel manager
                LobbyManager.addLobby(discordId, lobby);

            } catch (Exception e) {
                event.reply("❌ Failed to update lobby embed.").setEphemeral(true).queue();
                e.printStackTrace();
            }

        } else {
            // E' una nuova creazione
            event.reply("✅ Lobby created successfully!").setEphemeral(true).queue();
            lobby.sendLobbyLog(event.getGuild(), 1380683537501519963L);
            lobby.sendLobbyAnnouncement(event.getGuild(), 1367186054045761616L);
            LobbyManager.addLobby(lobby.getDiscordId(), lobby);
            lobby.incrementCreated();
            System.out.println(" lobby created: " + lobby.getLobbiesCreated());
        }

        // in ogni caso rimuovo dalla sessione temporanea
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






/*
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("lobby_details_modal_lobby")) return;

        long discordId = event.getUser().getIdLong();
        Lobby lobby = lobbySessions.get(discordId);

        if (lobby == null) {
            event.reply("❌ Lobby session expired. Please start over.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String playerName = event.getValue("lobby_playername").getAsString();
        String availability = event.getValue("lobby_availability").getAsString();
        String rules = event.getValue("lobby_rule").getAsString();

        lobby.setPlayerName(playerName);
        lobby.setAvailability(availability);
        lobby.setRules(rules);
        lobby.setCreatedAt(LocalDateTime.now());
        System.out.println("lobby: \n"+lobby.toString());

        event.reply("✅ Lobby created successfully!").setEphemeral(true).queue();
        lobby.sendLobbyLog(event.getGuild(),1380683537501519963L);
        lobby.sendLobbyAnnouncement(event.getGuild(), 1367186054045761616L);
        LobbyManager.addLobby(lobby.getDiscordId(), lobby);
        lobby.incrementCreated();
        System.out.println(" lobby crated: "+ lobby.getLobbiesCreated());

        lobbySessions.remove(discordId);
    }



    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Guild not found.").setEphemeral(true).queue();
            return;
        }

        if (buttonId.startsWith("accept_direct_")) {
            String userIdStr = buttonId.replace("accept_direct_", "");
            long invitedUserId;
            try {
                invitedUserId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                event.reply("Invalid button ID format.").setEphemeral(true).queue();
                return;
            }

            Member clickingMember = event.getMember();
            if (clickingMember == null) {
                event.reply("Member info not available.").setEphemeral(true).queue();
                return;
            }

            // Controlla che chi clicca sia proprio l’utente invitato
            if (clickingMember.getIdLong() != invitedUserId) {
                event.reply("You are not authorized to accept this invitation.").setEphemeral(true).queue();
                return;
            }

            Lobby lobby = LobbyManager.getLobby(invitedUserId);
            if (lobby == null) {
                event.reply("Lobby no longer exists or expired.").setEphemeral(true).queue();
                return;
            }

            TextChannel privateChannel = guild.getTextChannelById(lobby.getPrivateChannelId());
            if (privateChannel == null) {
                event.reply("Lobby private channel not found.").setEphemeral(true).queue();
                return;
            }

            // Dai i permessi per entrare nel canale privato
            privateChannel.getPermissionContainer().upsertPermissionOverride(clickingMember)
                    .setAllowed(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                    .queue(success -> {
                        lobby.getPartecipants().add(invitedUserId);

                        event.editMessage("You accepted the invitation! You can now chat in the lobby.")
                                .setComponents() // rimuove i bottoni
                                .queue();

                        Member owner = guild.getMemberById(lobby.getDiscordId());
                        if (owner != null) {
                            privateChannel.sendMessage(owner.getAsMention() + ", " + clickingMember.getEffectiveName() + " joined your lobby!")
                                    .queue();
                        }
                    }, failure -> {
                        event.reply("Failed to update permissions.").setEphemeral(true).queue();
                    });

        } else if (buttonId.startsWith("decline_direct_")) {
            String userIdStr = buttonId.replace("decline_direct_", "");
            long invitedUserId;
            try {
                invitedUserId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                event.reply("Invalid button ID format.").setEphemeral(true).queue();
                return;
            }

            Member clickingMember = event.getMember();
            if (clickingMember == null) {
                event.reply("Member info not available.").setEphemeral(true).queue();
                return;
            }

            if (clickingMember.getIdLong() != invitedUserId) {
                event.reply("You are not authorized to decline this invitation.").setEphemeral(true).queue();
                return;
            }

            Lobby lobby = LobbyManager.getLobby(invitedUserId);
            if (lobby == null) {
                event.reply("Lobby no longer exists or expired.").setEphemeral(true).queue();
                return;
            }

            TextChannel privateChannel = guild.getTextChannelById(lobby.getPrivateChannelId());
            if (privateChannel == null) {
                event.reply("Lobby private channel not found.").setEphemeral(true).queue();
                return;
            }

            privateChannel.delete().queue(success -> {
                LobbyManager.removeLobby(lobby.getDiscordId());

                event.editMessage("You declined the invitation. The lobby was deleted.")
                        .setComponents()
                        .queue();

                Member owner = guild.getMemberById(lobby.getDiscordId());
                if (owner != null) {
                    owner.getUser().openPrivateChannel().queue(dm -> {
                        dm.sendMessage("Your lobby invitation to " + clickingMember.getEffectiveName() + " was declined, lobby deleted.")
                                .queue();
                    });
                }
            }, failure -> {
                event.reply("Failed to delete the lobby channel.").setEphemeral(true).queue();
            });
        }
    }

 */

}
