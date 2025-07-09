package Config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Properties;

@Slf4j
public class Config {

    private final Properties props = new Properties();

    public Config() {
        loadProperties();
    }

    private void loadProperties() {
        try {
            File file = new File("config.properties");
            InputStream input;

            if (file.exists()) {
                log.info("Caricamento config.properties dal filesystem.");
                input = new FileInputStream(file);
            } else {
                log.info("Caricamento config.properties dalle risorse del JAR.");
                input = getClass().getClassLoader().getResourceAsStream("config.properties");
                if (input == null) {
                    throw new FileNotFoundException("config.properties non trovato né nel filesystem né nelle risorse!");
                }
            }

            props.load(input);
            log.info("config.properties caricato con successo.");

        } catch (IOException e) {
            log.error("Errore nel caricamento di config.properties", e);
        }
    }

    public String getToken() {
        return props.getProperty("bot.token");
    }

    public String getGuildId() {
        return props.getProperty("guild.id");
    }

    public String getMatchMakingCategory() {
        return props.getProperty("matchmaking.category");
    }

    public String getMatchmakinngGlobalChannel() {
        return props.getProperty("matchmaking.globalchannel");
    }

    public String getMatchmakingForumPost() {
        return props.getProperty("matchmaking.forum.post.channel");
    }

    public String getMatchmakingForumPost2() {
        return props.getProperty("matchmaking.forum.post.channel2");
    }

    public String getLobbyCategory() {
        return props.getProperty("category.lobby");
    }

    public String getCategoryLog() {
        return props.getProperty("category.log");
    }

    public String getNinjacardChannel() {
        return props.getProperty("ninjacard.channel.log");
    }

    public String getLobbyChannelLog() {
        return props.getProperty("lobby.channel.log");
    }

    public String getPlayerInfoRole() {
        return props.getProperty("role.playerinfo");
    }

    public String getClanleaderRole() {
        return props.getProperty("role.clanleader");
    }

    public String getClanMemberRole() {
        return props.getProperty("role.clanmember");
    }

    public String getRefereeRole() {
        return props.getProperty("role.referee");
    }

    public String getDesputesCategory() {
        return props.getProperty("disputes.category");
    }

    public String getDbConnection() {
        return props.getProperty("db.connection");
    }

    public String getDbName() {
        return props.getProperty("db.name");
    }

    public String getOptOutRole() {
        return props.getProperty("role.opt.out.pvp");
    }

}