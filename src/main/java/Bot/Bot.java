package Bot;

import ClanManager.ClanStorage;
import Command.ManagerCommands;
import MatchMaking.MatchMakingCommand;
import MongoDB.MongoDBManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.EnumSet;

public class Bot {
    final String token = "MTMxMDE3MzY4NTI2ODc0NjI2Mg.GeWkHX._DXswbdkA_PzB62kd2aoVarg2f4tFqkSU3WaVc";

    public Bot() throws LoginException {
        // Inizializza MongoDB
        MongoDBManager.getDatabase();  // Questo assicura che MongoDB sia inizializzato quando parte il bot.

        DefaultShardManagerBuilder manager = DefaultShardManagerBuilder.createDefault(token);
        manager.setActivity(Activity.customStatus("Helping The Admin to Manage Clan Related Things"));
        manager.setAutoReconnect(true);
        manager.setStatus(OnlineStatus.ONLINE);
        manager.addEventListeners(
                                    new ManagerCommands(),
                                    new MatchMakingCommand()
        ); // Aggiungi i comandi
        manager.addEventListeners(new ClanStorage());


        // Abilita i permessi di intenti
        manager.enableIntents(EnumSet.allOf(GatewayIntent.class));
        manager.setMemberCachePolicy(MemberCachePolicy.ALL);

        // Abilita tutte le cache
        manager.enableCache(Arrays.asList(CacheFlag.values()));
        manager.build();

        System.out.println("The bot is running");
    }

    // Quando il bot si spegne, chiude la connessione MongoDB
    public void closeMongoConnection() {
        MongoDBManager.close();
    }
}