package server.api;

import commons.Emoji;
import commons.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class EmojiControllerTest {
    private EmojiController sut;
    private TestPlayerRepository repo;
    private LeaderboardController leaderboard;
    private long playerId;

    @BeforeEach
    public void setup() {
        repo = new TestPlayerRepository();
        leaderboard = new LeaderboardController(repo);
        ResponseEntity<Player> resp = leaderboard.addPlayerForcibly(new Player("test", 0));
        assertNotNull(resp.getBody());
        playerId = resp.getBody().id;
        sut = new EmojiController(leaderboard);
    }

    @Test
    public void testSendEmoji() {
        Emoji result = sut.sendEmoji(playerId, Emoji.EmojiType.FUNNY);
        assertNotNull(result);
        assertEquals("test", result.username);
        assertEquals(Emoji.EmojiType.FUNNY, result.emoji);

        // try with invalid player id
        result = sut.sendEmoji(42L, Emoji.EmojiType.SAD);
        assertNull(result);
    }
}
