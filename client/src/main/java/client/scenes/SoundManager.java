package client.scenes;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {

    SoundProfile soundProfile;
    MediaPlayer mainPlayer;

    public enum SoundProfile {
        NORMAL,
        WEIRD
    }

    public SoundManager() {
        this.soundProfile = SoundProfile.NORMAL;
        mainPlayer = null;
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
        String location = "/Sounds/" + (soundProfile == SoundProfile.NORMAL ? "normal" : "weird") + sound + ".mp3";
        Media hit = new Media(getClass().getResource(location).toString());
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        if (sound.equals("Button")) mediaPlayer.setVolume(0.25);
        //if (sound.equals("Welcome")) mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        if (sound.equals("Welcome") || sound.equals("Waiting") || sound.contains("InGame")) mainPlayer = mediaPlayer;
        mediaPlayer.play();
    }

    /**
     * Stop currently playing music
     */
    public void halt() {
        if (mainPlayer != null) mainPlayer.stop();
    }
}
