package client.scenes;

import client.utils.ServerUtils;
import commons.*;
import jakarta.ws.rs.BadRequestException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;


import java.util.*;

public abstract class GameCtrl implements Initializable {

    protected final static int GAME_ROUNDS = 20;
    protected final static int GAME_ROUND_TIME = 10;
    protected final static int MIDGAME_BREAK_TIME = 6;
    protected final static int TIMER_UPDATE_INTERVAL_MS = 50;
    protected final static int GAME_ROUND_DELAY = 2;

    @FXML
    protected StackPane answerArea;

    @FXML
    protected Label questionPrompt;

    @FXML
    protected ImageView imagePanel;

    @FXML
    protected Label pointsLabel;

    @FXML
    protected Label questionCount;

    @FXML
    protected Label countdown;

    @FXML
    protected ProgressBar timeProgress;

    @FXML
    protected Button submitButton;

    @FXML
    protected Button removeOneButton;

    @FXML
    protected Button decreaseTimeButton;

    @FXML
    protected Button doublePointsButton;

    @FXML
    protected TableView<Player> leaderboard;

    @FXML
    protected TableColumn<Player, Integer> colRank;

    @FXML
    protected TableColumn<Player, String> colUserName;

    @FXML
    protected TableColumn<Player, Integer> colPoints;

    protected ServerUtils server;
    protected MainCtrl mainCtrl;

    protected List<RadioButton> multiChoiceAnswers;
    protected TextField estimationAnswer;
    protected long sessionId;
    protected long playerId;
    protected Question currentQuestion;
    protected int points = 0;
    protected int bestSingleScore = 0;
    protected int bestMultiScore = 0;
    protected int rounds = 0;
    protected double difficultyFactor = 1;
    protected double timeFactor;
    protected Thread timerThread;
    protected Evaluation evaluation;

    protected boolean doublePointsJoker;
    protected boolean doublePointsActive;
    protected boolean decreaseTimeJoker;
    protected boolean removeOneJoker;


    public GameCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.multiChoiceAnswers = new ArrayList<RadioButton>();

        doublePointsJoker = true;
        doublePointsActive = false;
        decreaseTimeJoker = true;
        removeOneJoker = true;

        // Set to defaults
        this.sessionId = 0L;
        this.playerId = 0L;
    }

    /**
     * Setter for sessionId.
     *
     * @param sessionId the id of the sessions
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Setter for playerId.
     *
     * @param playerId the id of the player
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    /**
     * Setter for bestScore in single mode.
     */
    public void setBestSingleScore() {
        this.bestSingleScore = server.getPlayerById(playerId).bestSingleScore;
    }

    /**
     * Setter for bestScore in multiplayer mode.
     */
    public void setBestMultiScore() {
        this.bestMultiScore = server.getPlayerById(playerId).bestMultiScore;
    }

    /**
     * Load general question information
     *
     * @param q the question to be rendered
     */
    protected void renderGeneralInformation(Question q) {
        this.questionPrompt.setText(q.prompt);
        switch (q.type) {
            case RANGE_GUESS:
            case EQUIVALENCE:
            case MULTIPLE_CHOICE:
                try {
                    Image image = new Image("assets/" + q.imagePath);
                    imagePanel.setImage(image);
                    break;
                }
                catch (Exception e) {
                    break;
                }
        }

    }

    /**
     * Displays the count of the current question
     */
    public void renderQuestionCount() {
        questionCount.setText(String.format("Question: %d", rounds + 1));
    }

    /**
     * Switch method to render answer options for a given question
     *
     * @param q Question from which to take the possible answers
     */
    protected void renderAnswerFields(Question q) {
        switch (q.type) {
            case MULTIPLE_CHOICE:
            case COMPARISON:
            case EQUIVALENCE:
                renderMultipleChoiceQuestion(q);
                if (removeOneJoker) {
                    disableButton(removeOneButton, false);
                }
                break;
            case RANGE_GUESS:
                renderEstimationQuestion();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported question type when rendering answers");
        }
    }


    private void renderEstimationQuestion() {
        this.countdown.setText("");
        this.estimationAnswer = new TextField();
        answerArea.getChildren().clear();
        answerArea.getChildren().add(estimationAnswer);
    }

    /**
     * Render question of the multiple choice question variant
     *
     * @param q the question to be rendered
     */
    protected void renderMultipleChoiceQuestion(Question q) {
        double yPosition = 0.0;
        this.countdown.setText("Options:");
        multiChoiceAnswers.clear();
        answerArea.getChildren().clear();
        for (String opt : q.answerOptions) {
            RadioButton choice = new RadioButton(opt);
            choice.setTranslateY(yPosition);
            yPosition += 30;
            multiChoiceAnswers.add(choice);
            answerArea.getChildren().add(choice);
        }
    }

    /**
     * Change the image displayed upon hovering the activity answer options
     */
    public void imageHover() {
        Question q = this.currentQuestion;
        switch (this.currentQuestion.type) {
            case COMPARISON:
                try {
                    for (int i = 0; i < multiChoiceAnswers.size(); i++) {
                        Image image = new Image("assets/" + q.activityPath.get(i));
                        multiChoiceAnswers.get(i).setOnMouseEntered(e ->
                                imagePanel.setImage(image));
                    }
                    break;
                }
                catch (IllegalArgumentException e) {
                    break;
                }
            case EQUIVALENCE:
                try {
                    for (int i = 0; i < multiChoiceAnswers.size(); i++) {
                        Image image = new Image("assets/" + q.activityPath.get(i));
                        multiChoiceAnswers.get(i).setOnMouseEntered(e ->
                                imagePanel.setImage(image));
                        multiChoiceAnswers.get(i).setOnMouseExited(e ->
                                imagePanel.setImage(new Image("assets/" + q.imagePath)));
                    }
                    break;
                }
                catch (IllegalArgumentException e) {
                    break;
                }
        }
    }

    /**
     * Switch method to render correct answers based on the question type
     */
    protected void renderCorrectAnswer() {
        switch (this.evaluation.type) {
            case MULTIPLE_CHOICE:
            case COMPARISON:
            case EQUIVALENCE:
                renderMultipleChoiceAnswers(this.evaluation.correctAnswers);
                break;
            case RANGE_GUESS:
                renderEstimationAnswers(this.evaluation.correctAnswers);
                break;
            default:
                throw new UnsupportedOperationException("Currently only multiple choice answers can be rendered");
        }
    }

    private void renderEstimationAnswers(List<Integer> correctAnswers) {
        int givenAnswer = 0;
        int actualAnswer = correctAnswers.get(0);
        try {
            givenAnswer = Integer.parseInt(estimationAnswer.getText());
        } catch (NumberFormatException ex) {
            givenAnswer = actualAnswer;
        }

        int diff = givenAnswer - actualAnswer;
        String correctAnswer = "Correct Answer: " + actualAnswer;

        if (diff > 0) {
            correctAnswer += " (+" + diff + ")";
        } else if (diff < 0) {
            correctAnswer += " (" + diff + ")";
        }

        Label resultText = new Label(correctAnswer);
        answerArea.getChildren().clear();
        answerArea.getChildren().add(resultText);
    }

    /**
     * Render answers of the multiple choice question variant
     *
     * @param correctIndices Indexes of the correct answer(s)
     */
    protected void renderMultipleChoiceAnswers(List<Integer> correctIndices) {
        for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
            if (correctIndices.contains(i)) {
                multiChoiceAnswers.get(i).setStyle("-fx-background-color: green");
            } else {
                multiChoiceAnswers.get(i).setDisable(true);
            }
        }
    }

    /**
     * Starts reading time countdown and updates label accordingly to inform the user.
     */
    public void countdown() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            int counter = 5;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (counter < 0) {
                        cancel();
                        loadAnswer();
                    } else {
                        countdown.setText("The answer option will appear in " + counter + " seconds.");
                        counter--;
                    }
                });
            }
        }, 0, 1000);
    }

    /**
     * Loads a question and starts reading time.
     */
    public void loadQuestion() {
        disableButton(removeOneButton, true);
        disableButton(doublePointsButton, true);
        disableButton(decreaseTimeButton, true);
        disableButton(submitButton, true);
        this.answerArea.getChildren().clear();

        Question q = this.server.fetchOneQuestion(this.sessionId);
        this.currentQuestion = q;
        renderGeneralInformation(q);
        renderQuestionCount();
        countdown();
    }

    /**
     * Loads the answers of the current question and updates the timer after reading time is over
     */
    public void loadAnswer() {
        Question q = this.currentQuestion;
        renderAnswerFields(q);

        if(removeOneJoker) {
            disableButton(removeOneButton, q.type == Question.QuestionType.RANGE_GUESS);
        }

        disableButton(submitButton, false);

        Task roundTimer = new Task() {
            @Override
            public Object call() {
                double refreshCounter = 0;
                long gameRoundMs = GAME_ROUND_TIME * 1000;
                double timeElapsed = 0;
                while (timeElapsed < gameRoundMs) {
                    //the speed on which the timer updates, with default speed 1
                    double booster = getTimeJokers() + 1;
                    updateProgress(gameRoundMs - timeElapsed, gameRoundMs);
                    refreshCounter += booster;
                    try {
                        Thread.sleep(TIMER_UPDATE_INTERVAL_MS);
                        timeElapsed = refreshCounter * TIMER_UPDATE_INTERVAL_MS;
                        timeFactor = timeProgress.getProgress();
                    } catch (InterruptedException e) {
                        updateProgress(0, 1);
                        return null;
                    }
                }
                updateProgress(0, 1);
                Platform.runLater(() -> submitAnswer(true));
                return null;
            }
        };
        timeProgress.progressProperty().bind(roundTimer.progressProperty());
        this.timerThread = new Thread(roundTimer);
        this.timerThread.start();
        imageHover();
    }

    /**
     * Removes player from session, along with the singleplayer session. Also called if controller is closed forcibly
     */
    public void shutdown() {
        if (this.timerThread != null && this.timerThread.isAlive()) this.timerThread.interrupt();
        if (sessionId != 0) {
            updateScore(playerId, 0, false);
            server.addPlayerAnswer(sessionId, playerId, new Answer(Question.QuestionType.MULTIPLE_CHOICE));
            server.removePlayer(sessionId, playerId);
            setPlayerId(0);
        }
        if (server.getPlayers(sessionId).size() == 0) {
            server.removeSession(sessionId);
            setSessionId(0);
        }
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        shutdown();
        this.questionPrompt.setText("[Question]");
        this.answerArea.getChildren().clear();
        this.pointsLabel.setText("Points: 0");
        this.multiChoiceAnswers.clear();
        this.points = 0;
        this.rounds = 0;
        this.currentQuestion = null;
        this.imagePanel.setImage(null);

        //re-enable jokers
        doublePointsJoker = true;
        doublePointsActive = false;
        decreaseTimeJoker = true;
        removeOneJoker = true;

        disableButton(submitButton, true);
        mainCtrl.showSplash();
    }

    /**
     * Switch method that maps keyboard key presses to functions.
     *
     * @param e KeyEvent to be switched
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> back();
        }
    }

    /**
     * Updates the point counter in client side, and then updates database entry
     */
    public void updatePoints() {
        int temppoints;
        switch (this.evaluation.type) {
            case MULTIPLE_CHOICE:
            case COMPARISON:
            case EQUIVALENCE:
                temppoints = (int) ((80 * this.evaluation.points * timeFactor) +
                        (20 * this.evaluation.points));
                break;
            case RANGE_GUESS:
                int givenAnswer;
                int actualAnswer = this.evaluation.correctAnswers.get(0);
                try {
                    givenAnswer = Integer.parseInt(estimationAnswer.getText());
                } catch (NumberFormatException ex) {
                    givenAnswer = 0;
                }
                int diff = Math.abs(givenAnswer - actualAnswer);
                if(diff == 0) {
                    temppoints = (int) (60 * this.evaluation.points * timeFactor) + 40;
                }
                else {
                    if(diff > actualAnswer) diff = actualAnswer;
                    temppoints = (int) (90 - 90*((double) diff*difficultyFactor*timeFactor/actualAnswer) +
                            ((diff < actualAnswer) ? 10 - 10*((double) diff*difficultyFactor/actualAnswer) : 0));
                    if (temppoints <= 0) temppoints = 0;
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported question type when parsing answer");
        }

        if (doublePointsActive) {
            temppoints = temppoints * 2;
            switchStatusOfDoublePoints();
        }
        points += temppoints;
        renderPoints();
        updateScore(playerId, points, false);
    }

    /**
     * Called when player points are rendered or updated
     */
    public void renderPoints() {
        pointsLabel.setText(String.format("Points: %d", this.points));
    }

    /**
     * Submit button click event handler
     */
    public void submitAnswerButton() {
        submitAnswer(false);
    }

    /**
     * Submit an answer to the server
     */
    public void submitAnswer(boolean initiatedByTimer) {
        Answer ans = new Answer(currentQuestion.type);

        switch (currentQuestion.type) {
            case MULTIPLE_CHOICE:
            case COMPARISON:
            case EQUIVALENCE:
                for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
                    if (multiChoiceAnswers.get(i).isSelected()) {
                        ans.addAnswer(i);
                    }
                }
                break;
            case RANGE_GUESS:
                // TODO disallow non-numeric answer
                try {
                    ans.addAnswer(Integer.parseInt(estimationAnswer.getText()));
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid answer yo");
                    if (!initiatedByTimer) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Invalid answer");
                        alert.setHeaderText("Invalid answer");
                        alert.setContentText("You should only enter an integer number");
                        alert.show();
                        return;
                    } else {
                        ans.addAnswer(0);
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported question type when parsing answer");
        }

        if (this.timerThread != null && this.timerThread.isAlive()) this.timerThread.interrupt();
        disableButton(submitButton, true);
        disableButton(removeOneButton, true);

        this.evaluation = server.submitAnswer(sessionId, ans);

        server.toggleReady(sessionId, true);

        var session = server.getSession(sessionId);
        if (session.playersReady == session.players.size()) {
            server.updateStatus(session, GameSession.SessionStatus.PAUSED);
        }
    }

    /**
     * Gets the user's answer, starts the evaluation and loads a new question or ends the game.
     */
    public void startEvaluation(int scoreEvaluation) {

        if (this.evaluation == null) return;

        disableButton(removeOneButton, true);
        disableButton(decreaseTimeButton, true);
        disableButton(doublePointsButton, true);

        switch (rounds / 4){
            case 0 -> difficultyFactor = 1;
            case 1 -> difficultyFactor = 2;
            case 2 -> difficultyFactor = 3;
            case 3 -> difficultyFactor = 4;
            case 4 -> difficultyFactor = 5;
            default -> difficultyFactor = 1;
        }

        updatePoints();
        renderCorrectAnswer();

        this.evaluation = null;

        // TODO disable button while waiting
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    rounds++;
                    resetTimeJokers();
                    if (rounds == GAME_ROUNDS) {
                        // TODO display leaderboard things here
                        if (points > scoreEvaluation) updateScore(playerId, points, true);
                        back();
                    } else if (rounds == GAME_ROUNDS / 2 &&
                            server.getSession(sessionId).sessionType == GameSession.SessionType.MULTIPLAYER) {
                        multiChoiceAnswers.clear();
                        answerArea.getChildren().clear();
                        imagePanel.setImage(null);
                        displayMidGameScreen();
                    } else {
                        try {
                            GameSession session = server.toggleReady(sessionId, false);
                            if (session.playersReady == 0) {
                                server.updateStatus(session, GameSession.SessionStatus.ONGOING);
                            }
                            imagePanel.setImage(null);
                            loadQuestion();
                        } catch (BadRequestException e) {
                            System.out.println("takingover");
                        }
                    }
                });
            }
        }, GAME_ROUND_DELAY * 1000);
    }

    /**
     * Display mid-game leaderboard
     */
    public void displayMidGameScreen() {
        var players = server.getPlayers(sessionId);
        var data = FXCollections.observableList(players);
        leaderboard.setItems(data);
        answerArea.setOpacity(0);
        submitButton.setOpacity(0);
        removeOneButton.setOpacity(0);
        doublePointsButton.setOpacity(0);
        decreaseTimeButton.setOpacity(0);
        questionPrompt.setOpacity(0);
        leaderboard.setOpacity(1);

        Task roundTimer = new Task() {
            @Override
            public Object call() {
                long refreshCounter = 0;
                long gameRoundMs = MIDGAME_BREAK_TIME * 1000;
                while (refreshCounter * TIMER_UPDATE_INTERVAL_MS < gameRoundMs) {
                    updateProgress(gameRoundMs - refreshCounter * TIMER_UPDATE_INTERVAL_MS, gameRoundMs);
                    ++refreshCounter;
                    try {
                        Thread.sleep(TIMER_UPDATE_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        updateProgress(0, 1);
                        return null;
                    }
                }
                updateProgress(0, 1);
                return null;
            }
        };
        timeProgress.progressProperty().bind(roundTimer.progressProperty());
        this.timerThread = new Thread(roundTimer);
        this.timerThread.start();

        GameSession session = server.toggleReady(sessionId, false);
        if (session.playersReady == 0) {
            server.updateStatus(session, GameSession.SessionStatus.ONGOING);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    leaderboard.setOpacity(0);
                    answerArea.setOpacity(1);
                    questionPrompt.setOpacity(1);
                    submitButton.setOpacity(1);
                    removeOneButton.setOpacity(1);
                    doublePointsButton.setOpacity(1);
                    decreaseTimeButton.setOpacity(1);
                    loadQuestion();
                });
            }
        }, MIDGAME_BREAK_TIME * 1000);
    }

    /**
     * Disable button so the player can not interact with it
     *
     * @param button  - Button to be disabled
     * @param disable - boolean value whether the button should be disabled or enabled
     */
    public void disableButton(Button button, boolean disable) {
        if (button == null) return;
        if (disable) button.setOpacity(0.5);
        if (!disable) button.setOpacity(1);
        button.setDisable(disable);
    }

    /**
     * Remove One Answer Joker
     * When this joker is used it removes one incorrect answer from the answers list for the player that used it
     */
    public void removeOneAnswer() {
        removeOneJoker = false;
        disableButton(removeOneButton, true);

        switch (currentQuestion.type) {
            case COMPARISON:
            case EQUIVALENCE:
            case MULTIPLE_CHOICE:
                List<Integer> incorrectAnswers = new ArrayList<>();
                List<Integer> correctAnswers = server.getCorrectAnswers(sessionId);
                for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
                    if (!correctAnswers.contains(i)) {
                        incorrectAnswers.add(i);
                    }
                }
                int randomIndex = new Random().nextInt(incorrectAnswers.size());
                RadioButton button = multiChoiceAnswers.get(incorrectAnswers.get(randomIndex));
                if (button.isSelected()) {
                    button.setSelected(false);
                }
                button.setDisable(true);
                break;
            default:
                disableButton(removeOneButton, false);
        }
    }


    /**
     * Get number of time Jokers for the current session
     *
     * @return int representing number of time jokers
     */
    public double getTimeJokers() {
        return server.getSession(sessionId).getTimeJokers();
    }

    /**
     * Reset the number of time Jokers for the current session to default value
     */
    public void resetTimeJokers() {
        if(getTimeJokers() != 0) {
            server.updateTimeJokers(sessionId, 0);
        }
    }
    /**
     * Decrease Time Joker
     * When this joker is used, the timer speeds up
     * This joker becomes Increase Time Joker in Singleplayer
     */
    public void decreaseTime() {
        decreaseTimeJoker = false;
        disableButton(decreaseTimeButton, true);
        server.updateTimeJokers(sessionId, (int) getTimeJokers() + 1);
    }

    /**
     * Double Points Joker
     * When this joker is used, it doubles the points gained for the question when it was used.
     */
    public void doublePoints() {
        doublePointsJoker = false;
        disableButton(doublePointsButton, true);
        switchStatusOfDoublePoints();
    }

    /**
     * Switch the doublePointsJoker status from true to false and from false to true
     */
    private void switchStatusOfDoublePoints() {
        doublePointsActive = !doublePointsActive;
    }

    /**
     * Generic event handler for clicking on an emoji
     * @param ev The event information
     */
    public void emojiEventHandler(Event ev) {
        Node source = (Node) ev.getSource();
        String nodeId = source.getId();
        Emoji.EmojiType type;
        switch (nodeId) {
            case "emojiFunny":
                type = Emoji.EmojiType.FUNNY;
                break;
            case "emojiSad":
                type = Emoji.EmojiType.SAD;
                break;
            case "emojiAngry":
                type = Emoji.EmojiType.ANGRY;
                break;
            default:
                return;
        }
        server.sendEmoji(sessionId, playerId, type);
    }

    /**
     * the method to updateScore
     *
     * @param playerId    the id of the player
     * @param points      the points of the player
     * @param isBestScore the flag of the best score of the player
     */
    public abstract void updateScore(long playerId, int points, boolean isBestScore);
}
