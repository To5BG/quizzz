package client.scenes;


import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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
     * @param plugs - List of plugs corresponding to the buttons
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
