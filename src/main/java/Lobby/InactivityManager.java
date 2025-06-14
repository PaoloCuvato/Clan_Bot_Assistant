package Lobby;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

public class InactivityManager extends ListenerAdapter {

    private final Map<Long, Instant> lastActivityMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

   private final long inactivityThresholdMillis = 3 * 60 * 60 * 1000; // 3 ore
   // private final long inactivityThresholdMillis = 6 * 60 * 1000; // 6 minuti

    private final long categoryId;
    private final JDA jda;

    public InactivityManager(JDA jda, long categoryId) {
        this.jda = jda;
        this.categoryId = categoryId;

        initializeActivityMap();
        scheduler.scheduleAtFixedRate(this::checkInactiveChannels, 10, 10, TimeUnit.MINUTES);
    }

    // Inizializza la mappa di attività scorrendo i canali già presenti nella categoria
    private void initializeActivityMap() {
        for (Guild guild : jda.getGuilds()) {
            Category category = guild.getCategoryById(categoryId);
            if (category == null) continue;

            category.getChannels().forEach(channel -> {
                if (channel instanceof GuildMessageChannel messageChannel) {
                    lastActivityMap.put(messageChannel.getIdLong(), Instant.now());
                }
            });

            System.out.println("InactivityManager avviato su " + lastActivityMap.size() + " canali nella categoria " + category.getName());
        }
    }

    // Quando arriva un messaggio, aggiorna l'attività del canale se è nella categoria
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;

        if (event.getChannel() instanceof GuildMessageChannel channel
                && channel instanceof StandardGuildChannel standardChannel) {
            if (standardChannel.getParentCategoryIdLong() == categoryId) {
                lastActivityMap.put(channel.getIdLong(), Instant.now());
                System.out.println("Attività aggiornata per il canale: " + channel.getName());
            }
        }
    }

    // Quando viene creato un nuovo canale, lo registra nella mappa se è sotto la categoria
    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        GuildChannel channel = (GuildChannel) event.getChannel();

        if (channel instanceof GuildMessageChannel messageChannel
                && channel instanceof StandardGuildChannel standardChannel) {

            if (standardChannel.getParentCategoryIdLong() == categoryId) {
                lastActivityMap.put(messageChannel.getIdLong(), Instant.now());
                System.out.println("Nuovo canale registrato automaticamente: " + messageChannel.getName());
            }
        }
    }

    // Controlla periodicamente i canali inattivi
    private void checkInactiveChannels() {
        Instant now = Instant.now();

        for (Map.Entry<Long, Instant> entry : lastActivityMap.entrySet()) {
            long channelId = entry.getKey();
            Instant lastActivity = entry.getValue();

            long diff = now.toEpochMilli() - lastActivity.toEpochMilli();

            if (diff >= inactivityThresholdMillis) {
                deleteChannel(channelId);
            }
        }
    }

    // Elimina il canale se viene considerato inattivo
    private void deleteChannel(long channelId) {
        for (Guild guild : jda.getGuilds()) {
            GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, channelId);
            if (channel != null) {
                channel.delete().queue(
                        success -> System.out.println("Cancellato canale inattivo: " + channel.getName()),
                        failure -> System.err.println("Errore durante eliminazione canale: " + failure.getMessage())
                );
                lastActivityMap.remove(channelId);
            }
        }
    }
}
