package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    static int muteSwitcher = 3;
    static VBox muteBox;
    private Communicator communicator;

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
        this.communicator = gameWindow.getCommunicator();
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("starWarsMenu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Awful title
        var title = new Text ("T e t r E C S");
        var subTitle = new Text("- Star Wars Deluxe Edition -");
        title.getStyleClass().add("starWarsThemeTitle");
        subTitle.getStyleClass().add("starWarsThemeSubTitle");

        //Create a VBox where we are going to se the elements
        VBox menuTextBox = new VBox();
        menuTextBox.setAlignment(Pos.CENTER);

        //Add title to the box
        menuTextBox.getChildren().add(title);
        menuTextBox.getChildren().add(subTitle);

/*        if(communicator.getSinglePlayerGame()){
            Text singlePLayerText = new Text(" - Only single player is supported -  \n Connect to the VPN for Multiplayer");
            singlePLayerText.getStyleClass().add("starWarsSubTitleGrey");
            menuTextBox.getChildren().add(singlePLayerText);
        }*/

        //Transition for the Logo
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(2500),title);
        rotateTransition.setByAngle(18);
        rotateTransition.setCycleCount(700);
        rotateTransition.setAutoReverse(true);
        rotateTransition.play();

        //Creating the gamePlay options

        //Creating a button play

        VBox menuButtonsBox = new VBox();
        menuButtonsBox.setAlignment(Pos.CENTER);

        muteBox = new VBox();
        menuButtonsBox.setAlignment(Pos.CENTER);
        mainPane.setTop(muteBox);

        Text music = new Text("Music");
        music.getStyleClass().add("starWarsMusic");
        muteBox.getChildren().add(music);

        if(!(muteSwitcher % 2 == 0)) {
            Text onText = new Text("ON");
            onText.getStyleClass().add("starWarsMusic");
            muteBox.getChildren().add(onText);
        } else {
            Text onText = new Text("OFF");
            onText.getStyleClass().add("starWarsMusic");
            muteBox.getChildren().add(onText);
        }

        Button newGame = new Button("New Game");
        newGame.getStyleClass().add("starWarsMenuItem");

        Button multiplayerGame = new Button("Multiplayer");
        multiplayerGame.getStyleClass().add("starWarsMenuItem");

        Button howToPlay = new Button("How to play");
        howToPlay.getStyleClass().add("starWarsMenuItem");

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("starWarsMenuItem");

        //Add all buttons to the vbox
        menuButtonsBox.getChildren().addAll(newGame,multiplayerGame,howToPlay,exitButton);

        menuButtonsBox.setSpacing(10);

        //Set the menuBox which is made by 2 others VBoxed. One with text and one with bottons
        VBox menuBox = new VBox(70);

        menuBox.getChildren().addAll(menuTextBox,menuButtonsBox);
        menuBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(menuBox);

        //Bind the button action to the startGame method in the menu
        newGame.setOnAction(this::startGame);

        howToPlay.setOnAction(this::goOnInstructionsPage);

        exitButton.setOnMouseClicked((e) -> App.getInstance().shutdown());

        multiplayerGame.setOnMouseClicked(this::startMultiplayer);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        Multimedia.stop();
        //Multimedia.shouldPlayMusic(true);
        Multimedia.startPlayBackgroundMusic("clone_army_theme.mp3");
        scene.setOnKeyPressed(MenuScene::keyListener);
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    private void goOnInstructionsPage(ActionEvent event) {
        gameWindow.startInstructions();
    }

    private void startMultiplayer(MouseEvent mouseEvent) {
        gameWindow.startMultiplayerLobby();
    }

    //private void exitGame(ActionEvent event) { gameWindow.exitGame(); }

    //Exit game pressing key
    private static void keyListener(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            App.getInstance().shutdown();
        }
        boolean switcherValue;
        if (keyEvent.getCode().equals(KeyCode.M)){
            switcherValue = muteSwitcher % 2 == 0;
            muteSwitcher++;
            logger.info("Switcher value passed : " + switcherValue);

            if(!(muteSwitcher % 2 == 0)) {
                muteBox.getChildren().clear();
                Text music = new Text("Music");
                music.getStyleClass().add("starWarsMusic");
                muteBox.getChildren().add(music);
                Text onText = new Text("ON");
                onText.getStyleClass().add("starWarsMusic");
                muteBox.getChildren().add(onText);
            } else {
                muteBox.getChildren().clear();
                Text music = new Text("Music");
                music.getStyleClass().add("starWarsMusic");
                muteBox.getChildren().add(music);
                Text offText = new Text("OFF");
                offText.getStyleClass().add("starWarsMusic");
                muteBox.getChildren().add(offText);
            }

            Multimedia.shouldPlayMusic(switcherValue);
        }
    }

}
