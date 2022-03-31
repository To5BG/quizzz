package client.utils;

import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;

public class ImageUtils extends Task<Image> {

    byte[] bytes;
    Image javafxImg;

    public ImageUtils(byte[] bytes) {
        super();
        this.bytes = bytes;
    }

    @Override
    public Image call() {
        return this.javafxImg = new Image(new ByteArrayInputStream(bytes));
    }


}
