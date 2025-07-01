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

}
