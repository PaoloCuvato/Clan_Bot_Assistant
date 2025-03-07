package MatchMaking;

import  Enum.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lobby {
    public String info;
    public String rule;
    public TypeLobby typeOfLobby;
    public Date date;   // if the lobby is ranked or there is some event this is the date of that match or event


}

//        ThreadChannel tre = event.getGuild().getThreadChannelById(1347512936121241663L);
//        tre.getOwnerThreadMember().getUser().getId();