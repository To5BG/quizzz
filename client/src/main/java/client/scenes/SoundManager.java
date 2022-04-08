package client.scenes;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.nio.file.Path;

public class SoundManager {

    SoundProfile soundProfile;
    MediaPlayer mPlayer;

    public enum SoundProfile {
        NORMAL,
        WEIRD
    }

    public SoundManager() {
        this.soundProfile = SoundProfile.NORMAL;
        mPlayer = null;
    }

    /**
     * Toggles for field soundProfile
     */
    public void toggleProfile() {
        soundProfile = (soundProfile == SoundProfile.NORMAL) ? SoundProfile.WEIRD : SoundProfile.NORMAL;
    }

    /**
     * Method used to play any sound available in the database
     */
    public void playSound(String sound) {
        String location = "Sound/" + (soundProfile == SoundProfile.NORMAL ? "normal" : "weird") + sound + ".mp3";
        Media hit = new Media(Path.of(location).toUri().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }
}
