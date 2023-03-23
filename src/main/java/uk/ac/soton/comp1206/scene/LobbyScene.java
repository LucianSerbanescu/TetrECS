package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import static uk.ac.soton.comp1206.scene.MenuScene.muteSwitcher;

public class LobbyScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    private BorderPane mainPane;
    private Communicator communicator;
    private Timer timer = new Timer();

    private VBox gamesDisplayBox;
    private BorderPane borderPane;
    private ScrollPane messageBoxScrollPane;
    private AnchorPane bothButtons;
    private BorderPane borderPaneForChat;
    private boolean scrollToBottom;
    private boolean keepTheButton = false;
    private VBox messagesVBox = new VBox();
    private String playerName;
    private HBox allPlayersBox = new HBox();
    //private HBox newUsersBox= new HBox();
    private VBox topSection;
    //Use this to check if the game chat was created before
    private boolean chatCreated = false;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
    }

    /**
     * Initialise the game
     */
    @Override
    public void initialise() {
        communicator.addListener((message) -> Platform.runLater(() -> handleMessages(message)));
        scene.setOnKeyPressed(this::exit);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                communicator.send("LIST");
            }
        },2,2500);
        scene.addPostLayoutPulseListener(this::jumpToBottom);
    }

    /**
     * Build the UI
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //Create a stuck pane
        StackPane stackPane = new StackPane();
        mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        mainPane.setMaxHeight(gameWindow.getHeight());

        stackPane.setMaxWidth(gameWindow.getWidth());
        stackPane.setMaxHeight(gameWindow.getHeight());

        stackPane.getStyleClass().add("starWarsMultiplayer-background");
        stackPane.getChildren().add(mainPane);
        root.getChildren().add(stackPane);

        //Create a border pane
        borderPane = new BorderPane();
        stackPane.getChildren().add(borderPane);

        Text title = new Text("Multiplayer");
        title.getStyleClass().add("starWarsLobby");
        borderPane.setTop(title);
        title.setTextAlignment(TextAlignment.CENTER);
        BorderPane.setAlignment(title, Pos.CENTER);

        VBox leftSection = new VBox();
        borderPane.setLeft(leftSection);
        leftSection.setPadding(new Insets(5, 5, 5, 5));
        leftSection.setSpacing(70);

        VBox createGameBox = new VBox();
        leftSection.getChildren().add(createGameBox);
        createGameBox.setPadding(new Insets(5, 5, 5, 5));
        createGameBox.setSpacing(10);

        Text lobbyTitle = new Text("Create Game");
        lobbyTitle.getStyleClass().add("starWarsLobby");
        createGameBox.getChildren().add(lobbyTitle);

        //Create the Name of the chat insert field
        TextField insertGameName = new TextField();
        insertGameName.setPromptText("Create new game");
        createGameBox.getChildren().add(insertGameName);
        insertGameName.setOnKeyPressed((event) -> {
            //Check if you are allowed to create a game
            if(event.getCode() == KeyCode.ENTER) {
                if(!insertGameName.getText().equals("")){
                    logger.info("You pressed ENTER to create a game");
                    if(!chatCreated){
                        createChat(insertGameName.getText());
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR,"You are already in a chat");
                        error.showAndWait();
                    }
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR,"Name can't be null\nPlease insert a name in the box;");
                    error.showAndWait();
                }
            }
        } );

        Button buttonToCreateAGame = new Button("create game");
        buttonToCreateAGame.getStyleClass().add("menuItem");
        buttonToCreateAGame.setOnMouseClicked((event -> {
            if(!insertGameName.getText().equals("")){
                logger.info("You clicked the button to create a game");
                createChat(insertGameName.getText());
                if(!chatCreated){
                    createChat(insertGameName.getText());
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR,"You are already in a chat");
                    error.showAndWait();
                }
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR,"Name can't be null\nPlease give a name in the box");
                error.showAndWait();
            }

        }));
        createGameBox.getChildren().add(buttonToCreateAGame);

        gamesDisplayBox = new VBox();
        leftSection.getChildren().add(gamesDisplayBox);
        gamesDisplayBox.setPadding(new Insets(5, 5, 5, 5));
        gamesDisplayBox.setSpacing(10);

        Text gameDisplayTitle = new Text("Current Games : ");
        gameDisplayTitle.getStyleClass().add("starWarsLobby");
        gameDisplayTitle.setTextAlignment(TextAlignment.CENTER);
        gamesDisplayBox.getChildren().add(gameDisplayTitle);

    }

    /**
     * Handle all the messages came from the server
     * @param message
     */
    private void handleMessages(String message) {
        String[] communicatorMessage = message.split(" ",2);
        if(communicatorMessage[0].equals("CHANNELS")) {
            gamesDisplayBox.getChildren().clear();
            logger.info("Received channels");
            try {
                String[] everyChannel = communicatorMessage[1].split("\n");
                for(String line : everyChannel) {
                    Text lineText = new Text(line);
                    lineText.getStyleClass().add("starWarsLobbyCurrentGames");
                    gamesDisplayBox.getChildren().add(lineText);
                    lineText.setOnMouseClicked((event -> {
                        logger.info("name of the game you want to enter was clicked");
                        joinChannel(lineText.getText());
                    }));
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error("no channels");
            }
            setKeepTheButton();
        } else if(communicatorMessage[0].equals("ERROR")) {
            logger.error("Message error : " + communicatorMessage[1]);
            setKeepTheButton();
        } else if(communicatorMessage[0].equals("HOST")){
            logger.info("You are the host of this game" + "\n" + "Adding the start button");
            keepTheButton = true;
            setKeepTheButton();
        } else if(communicatorMessage[0].equals("MSG")){
            logger.error(" communicator message receive " + communicatorMessage[0] + "       " + communicatorMessage[1]);
            String[] playerMessage = communicatorMessage[1].split(":",2);
            logger.info("what split does " + playerMessage[0] + "       " + playerMessage[1]);
            String player = playerMessage[0];
            String messageReceivedFromPlayer = playerMessage[1];
            receiveMessage(player,messageReceivedFromPlayer);
        } else if(communicatorMessage[0].equals("START")) {
            startTheGame();
        } else if(communicatorMessage[0].equals("NICK")) {
            playerName = communicatorMessage[1];
            logger.info("playerName is " + playerName);
            Text playerNameTitle = new Text(playerName);
            playerNameTitle.getStyleClass().add("starWarsScoreSubTitle");
            //allPlayersBox.getChildren().add(playerNameTitle);

        } else if(communicatorMessage[0].equals("USERS")) {
            allPlayersBox.getChildren().clear();
            //allPlayersBox.getChildren().add(playerNameTitle);
            String[] allPlayers = communicatorMessage[1].split("\n");
            logger.info("all Players String is " + allPlayers[0]);
            for(String everyPlayer : allPlayers){
                var everyPlayerText = new Text(everyPlayer);
                logger.info("every Player text is " + everyPlayerText);
                everyPlayerText.getStyleClass().add("starWarsLobbyUsers");
                allPlayersBox.getChildren().add(everyPlayerText);
            }
            logger.info("All players box is " + allPlayersBox.getChildren());

        }
    }

    /**
     * Check if you are allowed to start the game, and if yes display the button
     */
    private void setKeepTheButton() {
        if(keepTheButton){
            var startGameButton = new Button("Start Game");
            startGameButton.setOnMouseClicked((event -> {
                logger.info("Requesting game start");
                communicator.send("START");
            }));
            bothButtons.getChildren().add(startGameButton);
            AnchorPane.setLeftAnchor(startGameButton, 0.0);
        }
    }

    /**
     * Join a channel
     * @param line
     */
    private void joinChannel(String line) {
        communicator.send("JOIN " + line);
        createChat(line);
    }

    /**
     * Create the Chat in the right
     * @param line
     */
    private void createChat(String line) {

        //Set true if the chat is created so it cant be created twice
        chatCreated = true;

        communicator.send("CREATE " + line);

        VBox rightSection = new VBox();
        borderPane.setRight(rightSection);

        Text titleForChat = new Text(line);
        titleForChat.getStyleClass().add("starWarsLobby");
        rightSection.getChildren().add(titleForChat);

        //set BorderPane dimensions
        borderPaneForChat = new BorderPane();
        rightSection.getChildren().add(borderPaneForChat);
        borderPaneForChat.setMinHeight(gameWindow.getHeight()/1.2);
        borderPaneForChat.setMinWidth(gameWindow.getWidth()/1.5);
        borderPaneForChat.setMaxHeight(gameWindow.getHeight()/1.2);
        borderPaneForChat.setMaxWidth(gameWindow.getWidth()/1.5);
        rightSection.setSpacing(1);
        borderPaneForChat.setPadding(new Insets(5, 5, 5, 5));
        borderPaneForChat.getStyleClass().add("starWarsGameBox");

        bothButtons = new AnchorPane();
        borderPaneForChat.setBottom(bothButtons);

        //Create a leave button
        var leaveGameButton = new Button("Leave Game");
        leaveGameButton.setOnMouseClicked(event -> {
            communicator.send("PART");
            rightSection.getChildren().clear();
            keepTheButton = false;
            chatCreated = false;
        });
        bothButtons.getChildren().add(leaveGameButton);
        AnchorPane.setRightAnchor(leaveGameButton, 0.0);


        BorderPane messageBoxBorderPane = new BorderPane();
        borderPaneForChat.setCenter(messageBoxBorderPane);

        //Initialise the scroll pane
        messageBoxScrollPane = new ScrollPane();
        messageBoxScrollPane.setFitToWidth(true);
        messageBoxScrollPane.getStyleClass().add("starWarsScroller");

        //Create the message
        var messageTextField = new TextField();
        messageTextField.setPromptText("Enter a message");
        messageTextField.getStyleClass().add("messageBox");
        //String message = messageTextField.getText();
        messageTextField.setOnKeyPressed(key -> {
            logger.info("The key enter is pressed");
            if(key.getCode().equals(KeyCode.ENTER)){
                if (!(messageTextField.getText().startsWith("/"))) {
                    communicator.send("MSG " + messageTextField.getText());
                } else {
                    String[] message1 = messageTextField.getText().split(" ", 2);
                    //String messageLowCase = message1[0].toLowerCase();
                    if (message1[0].equals("/nick")) {
                        communicator.send("NICK " + message1[1]);
                    }
                }
                messageTextField.clear();
            }
        });

        messageBoxBorderPane.setBottom(messageTextField);
        messageBoxBorderPane.setCenter(messageBoxScrollPane);

        //Put the players in the top
        topSection = new VBox();
        borderPaneForChat.setTop(topSection);
        topSection.getChildren().add(allPlayersBox);
        topSection.setSpacing(10);
        allPlayersBox.setSpacing(5);
        messageTextField.setPadding(new Insets(5,5,5,5));

        Text littleInstructions = new Text("typer /nick <NewName> to change your name");
        littleInstructions.getStyleClass().add("starWarsLobbyInstructions");
        topSection.getChildren().add(littleInstructions);
    }

    /**
     * Handle all the messages which are received
     * @param player
     * @param message
     */
    private void receiveMessage(String player, String message) {

        logger.info("Received message: {}", message);

        //Put the timer in chat message
        StringBuilder timeHourMessage = new StringBuilder();
        timeHourMessage.append("[" + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()) + "] " + player + ": ");
        timeHourMessage.append(message);

        Text messageText = new Text(timeHourMessage.toString());
        messageText.getStyleClass().add("starWarsMessages");

        if(!message.equals("")){
            messagesVBox.getChildren().add(messageText);
        }
        messageBoxScrollPane.setContent(messagesVBox);

        //Scroll to bottom
        if(messageBoxScrollPane.getVvalue() == 0.0f || messageBoxScrollPane.getVvalue() > 0.9f) {
            scrollToBottom = true;
        }

    }

    /**
     * Start the game
     */
    private void startTheGame(){
        logger.info("Start Multiplayer scene");
        timer.cancel();
        gameWindow.startMultiplayerGame();

    }

    /**
     * Jump to bottom in the scroller
     */
    private void jumpToBottom() {
        if(!scrollToBottom) return;
        messageBoxScrollPane.setVvalue(1.0f);
        scrollToBottom = false;
    }

    /**
     * Key events
     * @param keyEvent
     */
    private void exit(KeyEvent keyEvent) {
        boolean switcherValue;
        if (keyEvent.getCode().equals(KeyCode.M)){
            switcherValue = muteSwitcher % 2 == 0;
            muteSwitcher++;
            logger.info("Switcher value passed : " + switcherValue);
            Multimedia.shouldPlayMusic(switcherValue);
        } else if(keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            timer.cancel();
            gameWindow.startMenu();
            communicator.clearListeners();
            keepTheButton = false;
            communicator.send("PART");
        }
    }

}