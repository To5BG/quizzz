package client.utils;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class ImageUtils extends Task<Image> {

    BufferedImage bufferedImage;
    Image javafxImg;

    public ImageUtils(BufferedImage img) {
        super();
        this.bufferedImage = img;
    }

    @Override
    public Image call() {
        return this.javafxImg = SwingFXUtils.toFXImage(bufferedImage, null);
    }


}
