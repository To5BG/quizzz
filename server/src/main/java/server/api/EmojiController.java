package server.api;

import commons.Emoji;
import commons.Player;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class EmojiController {
    private LeaderboardController leaderboard;

    public EmojiController(LeaderboardController leaderboard) {
        this.leaderboard = leaderboard;
    }

    /**
     * Send an emoji to the specified session
     * @param emoji The emoji to publish to the session
     * @return The emoji that is published to the session
     */
    @MessageMapping("/emoji/{sessionId}/send/{playerId}")
    @SendTo("/updates/emoji/{sessionId}")
    public Emoji sendEmoji(@DestinationVariable("playerId") long playerId, Emoji.EmojiType emoji) {
        ResponseEntity<Player> playerInfo = leaderboard.getPlayerById(playerId);
        if (playerInfo.getStatusCode() != HttpStatus.OK || playerInfo.getBody() == null) return null;
        String username = playerInfo.getBody().username;

        return new Emoji(username, emoji);
    }
}
