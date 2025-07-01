package Config;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
@NoArgsConstructor
public class Config {

    public String getToken(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("bot.token");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getGuildId(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("guild.id");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getMatchMakingCategory(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("matchmaking.category");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getMatchmakinngGlobalChannel(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("matchmaking.globalchannel");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getMatchmakingForumPost(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("matchmaking.forum.post.channel");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getMatchmakingForumPost2(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("matchmaking.forum.post.channel2");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getLobbyCategory(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("category.lobby");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getCategoryLog(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("category.log");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getNinjacardChannel(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("ninjacard.channel.log");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getLobbyChannelLog(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("lobby.channel.log");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getPlayerInfoRole(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("role.playerinfo");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getClanleaderRole(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("role.clanleader");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getClanMemberRole(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("role.clanmember");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    public String getRefereeRole(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("role.referee");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }
    // Db Part
    public String getDbConnection(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("db.connection");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }

    public String getDbName(){
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
            return props.getProperty("db.name");

        }catch (IOException e){
            e.printStackTrace();
            System.out.println(e.getCause() + "\n" + e.getMessage());
            return null;
        }
    }

}
