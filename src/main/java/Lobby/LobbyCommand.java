package Lobby;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LobbyCommand extends ListenerAdapter {

    private final Map<Long, Lobby> lobbySessions = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        long discordId = event.getUser().getIdLong();

        if (event.getName().equals("freestyle")) {
            Lobby lobby = new Lobby();
            lobby.setDiscordId(discordId);
            lobby.setCreatedAt(LocalDateTime.now());

            lobbySessions.put(discordId, lobby);
            promptLobbyTypeStep(event);
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
                                lobby.incrementCompleted();
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
                promptRegionSelection(event);
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
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        // Ignora se è un bot
        if (event.getUser().isBot()) return;

        // Recuperiamo il canale come GuildChannel per poter accedere alla categoria
        GuildChannel guildChannel = event.getGuild().getGuildChannelById(event.getChannel().getId());

        // Verifica la categoria (sostituisci con il tuo ID categoria)
        Category category = guildChannel.getJDA().getCategoryById("1381025760231555077");
        if (category == null || !category.getId().equals("1381025760231555077")) {
            return;
        }

        // Controlla se il messaggio corrisponde a una lobby in attesa di completamento
        Lobby lobby = LobbyManager.getLobbyByCompletionMessageId(event.getMessageId());
        if (lobby != null) {
            event.retrieveMessage().queue(message -> {
                // Creiamo l'emoji ✅ come oggetto Emoji
                Emoji checkEmoji = Emoji.fromUnicode("✅");

                // Recuperiamo la reazione corrispondente
                MessageReaction reaction = message.getReaction(checkEmoji);
                int reactionCount = reaction != null ? reaction.getCount() : 0;

                // Se ci sono almeno 3 reazioni, completiamo e archiviamo la lobby
                if (reactionCount >= 3) {
                    ThreadChannel threadChannel = event.getGuild().getThreadChannelById(lobby.getPostId());
                    changeTagFromOpenedToClosed(threadChannel);
                    lobby.archivePost(event.getGuild());
                    threadChannel.sendMessage("✅ Lobby completed and archived.").queue();
                    LobbyManager.removeLobbyByCompletionMessageId(event.getMessageId());
                    lobby.incrementCompleted();
                    System.out.println(" lobby completed: "+ lobby.getLobbiesCompleted());

                }
            });
        }
    }


}
