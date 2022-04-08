package client.scenes;

import com.google.inject.Inject;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class WebViewCtrl extends SceneCtrl implements Initializable {

    private final MainCtrl mainCtrl;
    private final SoundManager soundManager;

    @FXML
    private WebView webView;

    @FXML
    private Button backButton;

    @FXML
    private AnchorPane pane;

    private WebEngine engine;
    private String page;

    @Inject
    public WebViewCtrl(MainCtrl mainCtrl, SoundManager soundManager) {
        this.mainCtrl = mainCtrl;
        this.soundManager = soundManager;
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(URL location, ResourceBundle resources) {
        webView.prefWidthProperty().bind(pane.widthProperty());
        webView.prefHeightProperty().bind(pane.heightProperty());

        pane.widthProperty().addListener((ObservableValue<? extends Number>
                                                  observable, Number oldValue, Number newValue) -> {
            backButton.setLayoutX(newValue.doubleValue() / 2 - (backButton.widthProperty().getValue() / 2));
        });

        engine = webView.getEngine();
        loadPage();
    }

    /**
     * Loads the edit activities page.
     */
    public void loadPage() {
        engine.load(this.page);
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() {
        // ENSURE STABLE SHUTDOWN
    }

    /**
     * {@inheritDoc}
     */
    public void back() {
        soundManager.playSound("Button");
        shutdown();
        mainCtrl.showSplash();
    }

    /**
     * Setter for the page url.
     *
     * @param url The url to be set.
     */
    public void setPage(String url) {
        this.page = url;
    }
}
