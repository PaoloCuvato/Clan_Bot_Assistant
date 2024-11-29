package ClanManager;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Data
@NoArgsConstructor
public class Clan extends ListenerAdapter {
    private static final int MAX_MEMBERS = 50; // Maximum number of members in a clan
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM:dd:yyyy HH:mm:ss");

    private ArrayList<User> listClanMember = new ArrayList<>(MAX_MEMBERS);
    private String name;
    private User[] members;
    private int wins = 0;
    private int losses = 0;
    private LocalDateTime creationDate;

    // Constructor with a single member
    public Clan(String name, User member) {
        this.name = name;
        this.members = new User[]{member};
        this.creationDate = LocalDateTime.now();
        listClanMember.add(member);
        ClanStorage.addClan(this); // Automatically add the clan to ClanStorage
    }

    // Constructor with a single member and wins
    public Clan(String name, User member, int victories) {
        this.name = name;
        this.members = new User[]{member};
        this.wins = victories;
        this.creationDate = LocalDateTime.now();
        listClanMember.add(member);
        ClanStorage.addClan(this); // Automatically add the clan to ClanStorage
    }

    // Constructor with a single member, wins, and losses
    public Clan(String name, User member, int victories, int losses) {
        this.name = name;
        this.members = new User[]{member};
        this.wins = victories;
        this.losses = losses;
        this.creationDate = LocalDateTime.now();
        listClanMember.add(member);
        ClanStorage.addClan(this); // Automatically add the clan to ClanStorage
    }

    public void addUser(User user) {
        if (listClanMember.contains(user)) {
            throw new IllegalStateException("User is already in the clan.");
        }
        if (listClanMember.size() >= MAX_MEMBERS) {
            throw new IllegalStateException("Clan is full. Cannot add more members.");
        }
        listClanMember.add(user);
    }

    public boolean kickUser(User user) {
        if (!listClanMember.contains(user)) {
            throw new IllegalStateException("User is not in the clan.");
        }
        listClanMember.remove(user);
        return true;
    }

    public String getFormattedCreationDate() {
        return this.creationDate.format(DATE_FORMATTER);
    }

    public boolean updateClanName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }
        this.name = newName.trim();
        return true;
    }

    public void addWin() {
        this.wins++;
    }

    public void addLoss() {
        this.losses++;
    }

    public void removeWin() {
        if (this.wins > 0) this.wins--;
    }

    public void removeLoss() {
        if (this.losses > 0) this.losses--;
    }

    public int getMemberCount() {
        return this.listClanMember.size();
    }

    @Override
    public String toString() {
        return "Clan{" +
                "name='" + name + '\'' +
                ", wins=" + wins +
                ", losses=" + losses +
                ", creationDate=" + getFormattedCreationDate() +
                '}';
    }
}
