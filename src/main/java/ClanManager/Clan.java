package ClanManager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Clan extends ListenerAdapter {
    private static final int MAX_MEMBERS = 16; // Maximum number of members in a clan
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM:dd:yyyy HH:mm:ss");

    private ArrayList<User> listClanMember = new ArrayList<>(MAX_MEMBERS);  // Lista dei membri come oggetti User
    private List<String> memberIds; // Memorizza gli ID degli utenti come String

    private String name;
    private User[] member;  // Singolo membro
    private String[] members; // Membri come array di stringhe (ID)

    private int wins = 0;
    private int losses = 0;
    private LocalDateTime creationDate;
    private String clanLeaderId;
    private GuildReadyEvent event;

    // Constructor con un singolo membro
    public Clan(String name, User member) {
        this.name = name;
        this.member = new User[]{member};
        this.creationDate = LocalDateTime.now();
        listClanMember.add(member);
        ClanStorage.addClan(this); // Aggiungi automaticamente il clan a ClanStorage
    }
    public Clan(String name, String leaderId, List<String> members, int wins, int losses, String creationDate) {
        this.name = name;
        this.clanLeaderId = leaderId;
        this.wins = wins;
        this.losses = losses;
        this.creationDate = LocalDateTime.parse(creationDate, DATE_FORMATTER);

        this.memberIds = new ArrayList<>();
        this.listClanMember = new ArrayList<>(MAX_MEMBERS);

        // Aggiungi il leader come primo membro
        if (leaderId != null && !leaderId.isEmpty()) {
            this.memberIds.add(leaderId);

            User leaderUser = ClanStorage.getUserFromId(leaderId);
            if (leaderUser != null) {
                this.listClanMember.add(leaderUser);
            }
        }

        // Aggiungi altri membri (escludi duplicati e il leader se già aggiunto)
        if (members != null) {
            for (String memberId : members) {
                if (memberId != null && !this.memberIds.contains(memberId)) {
                    this.memberIds.add(memberId);

                    User user = ClanStorage.getUserFromId(memberId);
                    if (user != null) {
                        this.listClanMember.add(user);
                    }
                }
            }
        }

        ClanStorage.addClan(this); // Facoltativo se vuoi inserirlo subito nello storage
    }


    // Constructor con un singolo membro e vittorie
    public Clan(String name, User member, int victories) {
        this.name = name;
        this.member = new User[]{member};
        this.wins = victories;
        this.creationDate = LocalDateTime.now();
        listClanMember.add(member);
        ClanStorage.addClan(this); // Aggiungi automaticamente il clan a ClanStorage
    }

    // Constructor con un singolo membro, vittorie e sconfitte
    public Clan(String name, User member, int victories, int losses) {
        this.name = name;
        this.member = new User[]{member};
        this.wins = victories;
        this.losses = losses;
        this.creationDate = LocalDateTime.now();
        listClanMember.add(member);
        ClanStorage.addClan(this); // Aggiungi automaticamente il clan a ClanStorage
    }

    // Constructor con array di membri e vittorie/sconfitte
    public Clan(String name, Object[] array, int wins, int losses) {
        this.name = name;
        this.wins = wins;
        this.losses = losses;
        this.creationDate = LocalDateTime.now();

        // Crea una lista per memorizzare gli ID dei membri come String
        this.members = new String[array.length];  // Inizializza un array di String per gli ID dei membri

        // Aggiungi gli ID degli utenti dall'array a members
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof String) {
                this.members[i] = (String) array[i];  // Memorizza l'ID dell'utente come stringa
                System.out.println(members[i]);
            }
        }

        // Aggiungi ogni membro al clan tramite gli ID
        for (String memberId : members) {
            if (memberId != null) {
                // Recupera l'oggetto User usando il metodo getUserFromId
                User user = (User) ClanStorage.getUserFromId(memberId);
                if (user != null) {
                    listClanMember.add(user);  // Aggiungi l'oggetto User al clan
                }
            }
        }

        ClanStorage.addClan(this); // Aggiungi automaticamente il clan a ClanStorage
    }

//    public Clan(String name, String leaderId, List<String> members, int wins, int losses, String creationDate) {
//        this.name = name;
//        this.clanLeaderId = leaderId;
//        this.memberIds = new ArrayList<>(members);
//
//        this.wins = wins;
//        this.losses = losses;
//        this.creationDate = LocalDateTime.parse(creationDate, DATE_FORMATTER);
//
//        this.listClanMember = new ArrayList<>(MAX_MEMBERS);
//    }

    public void loadMembersFromGuild(Guild guild) {
        listClanMember.clear();
        for (String memberId : memberIds) {
            Member member = guild.getMemberById(memberId);
            if (member != null) {
                listClanMember.add(member.getUser());
            }
        }
    }


    public boolean isLeader(String userId) {
        if (userId == null || clanLeaderId == null) {
            return false;
        }
        return userId.equals(clanLeaderId);
    }

    // Add a member – only if the executor is the clan leader
    public void addUser(User executor, User userToAdd, MessageChannel channel) {
        if (!isLeader(executor.getId())) {
            sendErrorEmbed(channel, "Only the clan leader can add members.");
            return;
        }
        if (listClanMember.contains(userToAdd)) {
            sendErrorEmbed(channel, "This user is already in the clan.");
            return;
        }
        if (listClanMember.size() >= MAX_MEMBERS) {
            sendErrorEmbed(channel, "The clan is full. Cannot add more members.");
            return;
        }
        listClanMember.add(userToAdd);
    }

    // Remove a member – only if the executor is the clan leader
    public void kickUser(User executor, User userToKick, MessageChannel channel) {
        if (!isLeader(executor.getId())) {
            sendErrorEmbed(channel, "Only the clan leader can remove members.");
            return;
        }
        if (!listClanMember.contains(userToKick)) {
            sendErrorEmbed(channel, "This user is not a member of the clan.");
            return;
        }
        listClanMember.remove(userToKick);
    }

    // Update clan name – only if the executor is the clan leader
    public void updateClanName(User executor, String newName, MessageChannel channel) {
        if (!isLeader(executor.getId())) {
            sendErrorEmbed(channel, "Only the clan leader can change the clan name.");
            return;
        }
        if (newName == null || newName.trim().isEmpty()) {
            sendErrorEmbed(channel, "The new name cannot be empty.");
            return;
        }
        this.name = newName.trim();
    }

    // Sends an error embed to the specified channel
    private void sendErrorEmbed(MessageChannel channel, String message) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("❌ Error");
        eb.setDescription(message);
        eb.setColor(0xFF5555); // Red color
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    // Restituisce la data di creazione formattata
    public String getFormattedCreationDate() {
        return this.creationDate.format(DATE_FORMATTER);
    }

    // Metodo per aggiornare il nome del clan


    // Metodo per aggiungere una vittoria
    public void addWin() {
        this.wins++;
    }

    // Metodo per aggiungere una sconfitta
    public void addLoss() {
        this.losses++;
    }

    // Metodo per rimuovere una vittoria
    public void removeWin() {
        if (this.wins > 0) this.wins--;
    }

    // Metodo per rimuovere una sconfitta
    public void removeLoss() {
        if (this.losses > 0) this.losses--;
    }

    // Restituisce il numero di membri del clan
    public int getMemberCount() {
        return this.listClanMember.size();
    }

    // Restituisce la lista dei membri come oggetti User
    public List<User> getMembersList() {
        return new ArrayList<>(listClanMember);
    }

    // Restituisce la lista degli ID dei membri
    public List<String> getMemberIdsList() {
        return new ArrayList<>(memberIds);
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

    public void updateClanName(String newName) {
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        super.onGuildReady(event);
        this.event=event;
    }
}