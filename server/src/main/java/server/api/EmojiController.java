package server.api;

import commons.Emoji;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class EmojiController {
    /**
     * Send an emoji to the specified session
     * @param emoji The emoji to publish to the session
     * @return The emoji that is published to the session
     */
    @MessageMapping("/emoji/{sessionId}/send")
    @SendTo("/updates/emoji/{sessionId}")
    public Emoji sendEmoji(Emoji emoji) {
        return emoji;
    }
}
