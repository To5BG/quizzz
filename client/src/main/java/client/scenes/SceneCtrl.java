package client.scenes;

public abstract class SceneCtrl {

    /**
     * Clean-up method called to disband any server connections and update the database.
     * Also called when receiving an external request to close a client session.
     */
    public abstract void shutdown();

    /**
     * Clean-up sub method called when an FXML scene needs to be changed.
     */
    public abstract void back();
}