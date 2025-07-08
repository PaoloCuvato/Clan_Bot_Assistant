package Bot;

import ClanManager.ClanManager;
import ClanManager.ClanService;
import ClanManager.ClanStorage;
import Command.ManagerCommands;
import Lobby.Lobby;
import MatchMaking.ForumMatchmaking;
import MatchMaking.MatchMakingCommand;
import MongoDB.MongoDBManager;
import PlayerInfo.AddInfoCardCommand;
import PlayerInfo.SlashPlayerInfoManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import javax.security.auth.login.LoginException;
import java.util.Arrays;
import Lobby.*;
import java.util.EnumSet;
import Config.*;

public class Bot extends ListenerAdapter {
    Config config = new Config();
    String token = config.getToken();


    public Bot() throws LoginException {
        // Inizializza MongoDB
        MongoDBManager.getDatabase();  // Questo assicura che MongoDB sia inizializzato quando parte il bot.
        long categoryId = Long.parseLong(config.getLobbyCategory()); // metti l'ID della tua categoria dei canali temporanei


        DefaultShardManagerBuilder manager = DefaultShardManagerBuilder.createDefault(token);
        //manager.setActivity(Activity.customStatus("Helping The Admin to Manage Clan Related Things"));
        manager.setAutoReconnect(true);
        manager.setStatus(OnlineStatus.ONLINE);
        manager.addEventListeners(new ManagerCommands(), new ForumMatchmaking(), new AddInfoCardCommand(),new SlashPlayerInfoManager()); // Aggiungi i comandi
        manager.addEventListeners(new ClanStorage(), new LobbyCommand(), new ButtonLobbyManager(), new Lobby(), new ClanService(),new ClanManager());
        manager.addEventListeners(new InactivityInitializer(categoryId));

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