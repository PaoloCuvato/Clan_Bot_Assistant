package ClanManager;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Clan extends ListenerAdapter {
    private static final int MAX_MEMBERS = 50; // Maximum number of members in a clan
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM:dd:yyyy HH:mm:ss");

    private ArrayList<User> listClanMember = new ArrayList<>(MAX_MEMBERS);  // Lista dei membri come oggetti User
    private List<String> memberIds; // Memorizza gli ID degli utenti come String

    private String name;
    private User[] member;  // Singolo membro
    private String[] members; // Membri come array di stringhe (ID)

    private int wins = 0;
    private int losses = 0;
    private LocalDateTime creationDate;

    // Constructor con un singolo membro
    public Clan(String name, User member) {
        this.name = name;
        this.member = new User[]{member};
        this.creationDate = LocalDateTime.now();
        listClanMember.add(member);
        ClanStorage.addClan(this); // Aggiungi automaticamente il clan a ClanStorage
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

    // Metodo per aggiungere un membro
    public void addUser(User user) {
        if (listClanMember.contains(user)) {
            throw new IllegalStateException("User is already in the clan.");
        }
        if (listClanMember.size() >= MAX_MEMBERS) {
            throw new IllegalStateException("Clan is full. Cannot add more members.");
        }
        listClanMember.add(user);
    }

    // Metodo per rimuovere un membro
    public boolean kickUser(User user) {
        if (!listClanMember.contains(user)) {
            throw new IllegalStateException("User is not in the clan.");
        }
        listClanMember.remove(user);
        return true;
    }

    // Restituisce la data di creazione formattata
    public String getFormattedCreationDate() {
        return this.creationDate.format(DATE_FORMATTER);
    }

    // Metodo per aggiornare il nome del clan
    public boolean updateClanName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }
        this.name = newName.trim();
        return true;
    }

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
}
