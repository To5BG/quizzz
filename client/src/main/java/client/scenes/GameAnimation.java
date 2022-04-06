package client.scenes;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

public class GameAnimation {

    private final static String BATTERY_GREEN = "#84ba84";

    /**
     * Empty Constructor
     */
    public GameAnimation() {
    }


    /**
     * Make the battery style animation when hovering enabled for the list of buttons
     *
     * @param buttonList - List of buttons
     */
    public void startBatteryAnimation(List<Button> buttonList) {
        startBatteryAnimation(buttonList, null);
    }

    /**
     * Make the battery style animation when hovering enabled for the list of buttons and animate the plugs
     *
     * @param buttonList - List of buttons
     * @param plugs      - List of plugs corresponding to the buttons
     */
    public void startBatteryAnimation(List<Button> buttonList, List<ImageView> plugs) {
        for (int index = 0; index < 3; index++) {
            int num = index;
            buttonList.get(index).setOnMouseEntered(ev -> {
                if (plugs != null) {
                    plugInAnimation(plugs.get(num)).play();
                }
                Animation animation = fillBatteryAnimation(buttonList.get(num));
                animation.play();
            });

            buttonList.get(index).setOnMouseExited(ev -> {
                if (plugs != null) {
                    plugOutAnimation(plugs.get(num)).play();
                }
                Animation animation = drainBatteryAnimation(buttonList.get(num));
                animation.play();
            });
        }
    }

    /**
     * Make a randomized emoji stream when emojis are used
     *
     * @param emoji Emoji to be displayed
     */
    public void startEmojiAnimation(ImageView emoji, String username, Pane area) {
        area.getChildren().add(emoji);
        FadeTransition fadeTransition = emojiFadeTransition(emoji);
        TranslateTransition translateTransition = emojiTranslateTransition(emoji);

        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(
                fadeTransition,
                translateTransition
        );
        pt.play();
        pt.setOnFinished(e -> area.getChildren().remove(emoji));
    }

    /**
     * Returns a new FadeTransition for emojis
     *
     * @param emoji Emoji to fade
     * @return FadeTransition object
     */
    private FadeTransition emojiFadeTransition(ImageView emoji) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(emoji);
        fadeTransition.setDuration(Duration.millis(1000));
        fadeTransition.setInterpolator(Interpolator.EASE_IN);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        return fadeTransition;
    }

    /**
     * Returns a new TranslateTransition for emojis
     *
     * @param emoji Emoji to translate
     * @return TranslateTransition object
     */
    private TranslateTransition emojiTranslateTransition(ImageView emoji) {
        TranslateTransition translateTransition = new TranslateTransition();
        translateTransition.setNode(emoji);
        translateTransition.setDuration(Duration.millis(1000));
        translateTransition.setInterpolator(Interpolator.EASE_OUT);
        translateTransition.setFromX(0);
        translateTransition.setFromY(40);
        translateTransition.setToY(80 * (1 - Math.random()));
        translateTransition.setToX(150);
        return translateTransition;
    }

    private Timeline plugInAnimation(ImageView plug) {
        return new Timeline(new KeyFrame(Duration.millis(500),
                new KeyValue(plug.translateXProperty(), 16)));
    }

    private Timeline plugOutAnimation(ImageView plug) {
        return new Timeline(new KeyFrame(Duration.millis(500),
                new KeyValue(plug.translateXProperty(), 0)));
    }

    /**
     * Make the background green and set the opacity steadily higher
     *
     * @param button - button for which this animation applies
     * @return the animation
     */
    private Animation fillBatteryAnimation(Button button) {
        return new Transition() {
            {
                setCycleDuration(Duration.millis(500));
                setInterpolator(Interpolator.EASE_IN);
            }

            @Override
            protected void interpolate(double frac) {
                Color vColor = Color.web(BATTERY_GREEN);
                Color c = new Color(vColor.getRed(), vColor.getGreen(), vColor.getBlue(), frac);
                button.setBackground(new Background(new BackgroundFill(c, new CornerRadii(12), Insets.EMPTY)));
            }
        };
    }

    /**
     * The reverse of fillBatteryAnimation
     *
     * @param button - button for which this animation applies
     * @return the animation
     */
    private Animation drainBatteryAnimation(Button button) {
        return new Transition() {
            {
                setCycleDuration(Duration.millis(500));
                setInterpolator(Interpolator.EASE_IN);
            }

            @Override
            protected void interpolate(double frac) {
                Color vColor = Color.web(BATTERY_GREEN);
                Color c = new Color(vColor.getRed(), vColor.getGreen(), vColor.getBlue(), 1 - frac);
                button.setBackground(new Background(new BackgroundFill(c, new CornerRadii(12), Insets.EMPTY)));
            }
        };
    }
}
