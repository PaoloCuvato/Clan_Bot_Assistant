package Stat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Data
public class PlayerStats extends ListenerAdapter {

    private long discordId;

    // Lobbies Created
    private int lobbiesCreatedGeneral = 0;
    private int lobbiesCreatedDirect = 0;

    // Lobbies Joined
    private int lobbiesJoinedGeneral = 0;
    private int lobbiesJoinedDirect = 0;

    // Times host accepted a user (only for General)
    private int hostAcceptedUserGeneral = 0;

    // Times player was accepted into a lobby (General + Direct)
    private int wasAcceptedGeneral = 0;
    private int wasAcceptedDirect = 0;

    // Times player declined a user (General + Direct)
    private int declinedUserGeneral = 0;
    private int declinedUserDirect = 0;

    // Times player was declined (General + Direct)
    private int wasDeclinedGeneral = 0;
    private int wasDeclinedDirect = 0;

    // Ignored Requests (General + Direct)
    private int ignoredRequestGeneral = 0;
    private int ignoredRequestDirect = 0;

    // Lobbies Completed
    private int lobbiesCompletedGeneral = 0;
    private int lobbiesCompletedDirect = 0;

    // Lobbies Incomplete
    private int lobbiesIncompleteGeneral = 0;
    private int lobbiesIncompleteDirect = 0;

    // Lobbies Disbanded
    private int lobbiesDisbandedGeneral = 0;
    private int lobbiesDisbandedDirect = 0;

    // Score
    private String score;

    // ===================== Increment Methods ======================

    public void incrementLobbiesCreatedGeneral() {
        lobbiesCreatedGeneral++;
        log.info("Lobbies Created (General) incremented to {}", lobbiesCreatedGeneral);
    }

    public void incrementLobbiesCreatedDirect() {
        lobbiesCreatedDirect++;
        log.info("Lobbies Created (Direct) incremented to {}", lobbiesCreatedDirect);
    }

    public void incrementLobbiesJoinedGeneral() {
        lobbiesJoinedGeneral++;
        log.info("Lobbies Joined (General) incremented to {}", lobbiesJoinedGeneral);
    }

    public void incrementLobbiesJoinedDirect() {
        lobbiesJoinedDirect++;
        log.info("Lobbies Joined (Direct) incremented to {}", lobbiesJoinedDirect);
    }

    public void incrementHostAcceptedUserGeneral() {
        hostAcceptedUserGeneral++;
        log.info("Host Accepted User (General) incremented to {}", hostAcceptedUserGeneral);
    }

    public void incrementWasAcceptedGeneral() {
        wasAcceptedGeneral++;
        log.info("Was Accepted (General) incremented to {}", wasAcceptedGeneral);
    }

    public void incrementWasAcceptedDirect() {
        wasAcceptedDirect++;
        log.info("Was Accepted (Direct) incremented to {}", wasAcceptedDirect);
    }

    public void incrementDeclinedUserGeneral() {
        declinedUserGeneral++;
        log.info("Declined User (General) incremented to {}", declinedUserGeneral);
    }

    public void incrementDeclinedUserDirect() {
        declinedUserDirect++;
        log.info("Declined User (Direct) incremented to {}", declinedUserDirect);
    }

    public void incrementWasDeclinedGeneral() {
        wasDeclinedGeneral++;
        log.info("Was Declined (General) incremented to {}", wasDeclinedGeneral);
    }

    public void incrementWasDeclinedDirect() {
        wasDeclinedDirect++;
        log.info("Was Declined (Direct) incremented to {}", wasDeclinedDirect);
    }

    public void incrementIgnoredRequestGeneral() {
        ignoredRequestGeneral++;
        log.info("Ignored Request (General) incremented to {}", ignoredRequestGeneral);
    }

    public void incrementIgnoredRequestDirect() {
        ignoredRequestDirect++;
        log.info("Ignored Request (Direct) incremented to {}", ignoredRequestDirect);
    }

    public void incrementLobbiesCompletedGeneral() {
        lobbiesCompletedGeneral++;
        log.info("Lobbies Completed (General) incremented to {}", lobbiesCompletedGeneral);
    }

    public void incrementLobbiesCompletedDirect() {
        lobbiesCompletedDirect++;
        log.info("Lobbies Completed (Direct) incremented to {}", lobbiesCompletedDirect);
    }

    public void incrementLobbiesIncompleteGeneral() {
        lobbiesIncompleteGeneral++;
        log.info("Lobbies Incomplete (General) incremented to {}", lobbiesIncompleteGeneral);
    }

    public void incrementLobbiesIncompleteDirect() {
        lobbiesIncompleteDirect++;
        log.info("Lobbies Incomplete (Direct) incremented to {}", lobbiesIncompleteDirect);
    }

    public void incrementLobbiesDisbandedGeneral() {
        lobbiesDisbandedGeneral++;
        log.info("Lobbies Disbanded (General) incremented to {}", lobbiesDisbandedGeneral);
    }

    public void incrementLobbiesDisbandedDirect() {
        lobbiesDisbandedDirect++;
        log.info("Lobbies Disbanded (Direct) incremented to {}", lobbiesDisbandedDirect);
    }

    public void updateScore(String newScore) {
        this.score = newScore;
        log.info("Score updated to {}", score);
    }

    public void printStats() {
        log.info("PlayerStats: {}", this);
    }
}
