package Lobby;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InactivityInitializer extends ListenerAdapter {

    private final long categoryId;

    public InactivityInitializer(long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        JDA jda = event.getJDA();
        Guild guild = event.getGuild();

        // Registra il listener InactivityManager solo una volta (ad esempio, qui per la prima guild)
        InactivityManager inactivityManager = new InactivityManager(jda, categoryId);
        jda.addEventListener(inactivityManager);

        System.out.println("✅ InactivityManager registrato per la guild: " + guild.getName());
    }
}
