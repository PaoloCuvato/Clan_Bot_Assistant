package MatchMaking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import Enum.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String id;
    public String name;
    private Game game ;
    private Platform platform;
    private String typeOfPlayer;
    private Region region;
    private Connections connection;
    public String bio;
    public String clan;
    public Boolean clanLeader = false;
    public int wins = 0;
    public int losses = 0;
}
