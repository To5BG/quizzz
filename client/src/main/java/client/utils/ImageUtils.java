package client.utils;

import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;

public class ImageUtils extends Task<Image> {

    private QuestionUtils questionUtils;
    private String path;


    public ImageUtils(QuestionUtils questionUtils, String path) {
        super();
        this.path = path;
        this.questionUtils = questionUtils;
    }

    @Override
    public Image call() {
        return new Image(new ByteArrayInputStream(questionUtils.fetchImage(path)));
    }


}
