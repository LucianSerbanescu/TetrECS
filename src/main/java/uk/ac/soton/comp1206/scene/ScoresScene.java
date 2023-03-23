package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import static uk.ac.soton.comp1206.scene.MenuScene.muteSwitcher;

import java.io.*;
import java.util.*;

public class ScoresScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);
    private final Game game;
    private final Communicator communicator;

    private ObservableList<Pair<String,Integer>> localScoresObservableList;
    private ObservableList<Pair<String,Integer>> serverScoresObservableList;
    private ObservableList<Pair<String,Integer>> multiplayerScoresObservableList;

    //bind the scores and hold the current list of scores in Scene
    private SimpleListProperty localScores;
    private SimpleListProperty remoteScores;
    private SimpleListProperty multiplayerScores;

    //Array of scoreLines
    private ArrayList<Pair<String,Integer>> arrayOfPairsForLocalScore;
    private ArrayList<Pair<String,Integer>> arrayOfPairsForServerScore;
    private ArrayList<Pair<String,Integer>> arrayOfPairsForMultiplayerScore;

    //used in UI showScore
    private ScoresList localScoresList;
    private ScoresList serverScoresList;
    private ScoresList multiplayerScoresList;

    //One line of score. put it here so it could be passed between the methods
    private String[] scoreLine;

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private boolean stopWriting = true;

    int solveTheBug = 0;
    private String yourName;
    private Integer gameScore;
    boolean multiplayerGame;

    VBox multiplayerScoreBox = new VBox();

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow,Game game, boolean isMultiplayer) {
        super(gameWindow);
        this.game = game;
        communicator = gameWindow.getCommunicator();
        multiplayerGame = isMultiplayer;

        logger.info("Creating Score Scene for SinglePLayer");

        localScoresObservableList = FXCollections.observableArrayList();
        serverScoresObservableList = FXCollections.observableArrayList();
        multiplayerScoresObservableList = FXCollections.observableArrayList();
        //The wrapper
        //Do i need this for online scores?
        //What is its role?
        localScores = new SimpleListProperty<Pair<String,Integer>>(localScoresObservableList);
        remoteScores = new SimpleListProperty<Pair<String,Integer>>(serverScoresObservableList);
        multiplayerScores = new SimpleListProperty<Pair<String,Integer>>(multiplayerScoresObservableList);

        localScoresList = new ScoresList();
        serverScoresList = new ScoresList();
        multiplayerScoresList = new ScoresList();

        arrayOfPairsForLocalScore = new ArrayList<>();
        arrayOfPairsForServerScore = new ArrayList<>();
        arrayOfPairsForMultiplayerScore = new ArrayList<>();

        gameScore = game.getScore();

    }

    @Override
    public void initialise() {
        logger.info("Initialising Score");
        if(Multimedia.shouldPlayMusic){
            Multimedia.startPlayBackgroundMusic("Duel_Of_The_Fates.mp3");
        }
        //subscribe to the communicator, then it changes do smth
        communicator.addListener((message) -> Platform.runLater(() -> {
            this.loadOnlineScores(message);
        }));

        communicator.send("HISCORES");
        communicator.send("SCORES");

    }

    @Override
    public void build() {
        logger.info("Building " + getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        root.setMaxWidth(gameWindow.getWidth());
        root.setMaxHeight(gameWindow.getHeight());

        BorderPane borderPane = new BorderPane();
        borderPane.setMaxWidth(gameWindow.getWidth());
        borderPane.setMaxHeight(gameWindow.getHeight());
        root.getChildren().add(borderPane);
        root.setAlignment(Pos.CENTER);
        borderPane.getStyleClass().add("starWarsScore-background");

        GridPane topSection = new GridPane();
        borderPane.setTop(topSection);

        Text scoreListText = new Text("Score List");
        scoreListText.setTextAlignment(TextAlignment.CENTER);
        scoreListText.getStyleClass().add("starWarsScoreTitle");
        topSection.setAlignment(Pos.CENTER);
        topSection.setVgap(100);
        topSection.setHgap(20);
        topSection.add(scoreListText,0,0);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5, 20, 20, 20));
        gridPane.setMaxWidth(gameWindow.getWidth());
        gridPane.setMaxHeight(gameWindow.getHeight());
        gridPane.setVgap(20);
        gridPane.setHgap(20);
        borderPane.setCenter(gridPane);
        gridPane.setAlignment(Pos.TOP_CENTER);

        VBox writeYourNameBox = new VBox();
        writeYourNameBox.setAlignment(Pos.CENTER);
        writeYourNameBox.setSpacing(20);

        Text writeYourNameText = new Text("Enter your name");
        writeYourNameText.getStyleClass().add("starWarsMandalorThemeHeading");
        writeYourNameBox.getChildren().add(writeYourNameText);

        var yourNameField = new TextField();
        yourNameField.setPromptText("Enter your name");
        yourNameField.setMaxWidth(gameWindow.getWidth() / 2);
        yourNameField.requestFocus();
        writeYourNameBox.getChildren().add(yourNameField);

        Button buttonReturnYourName = new Button("Enter");
        buttonReturnYourName.getStyleClass().add("starWarsMenuItem");
        writeYourNameBox.getChildren().add(buttonReturnYourName);


        gridPane.add(writeYourNameBox,0,0);

        VBox localScoreBox = new VBox();
        localScoreBox.setAlignment(Pos.CENTER);

        VBox serverScoreBox = new VBox();
        serverScoreBox.setAlignment(Pos.CENTER);

        if(multiplayerGame) {
            Text multiplayerScoreText = new Text("Multiplayer Score");
            multiplayerScoreText.getStyleClass().add("starWarsScoreSubTitle");
            multiplayerScoreBox.getChildren().add(multiplayerScoreText);

            multiplayerScoreBox.getChildren().add(multiplayerScoresList);
        }

        if(multiplayerGame){
            multiplayerScoreBox.getChildren().add(game.getPlayersVBox());
        }

        multiplayerScoreBox.setAlignment(Pos.CENTER);

        buttonReturnYourName.setOnMouseClicked((event) -> {
            yourName = yourNameField.getText();
            writeOnlineScore(yourName);
            revealScores(gridPane,localScoreBox,serverScoreBox,yourNameField,multiplayerScoreBox);
        });

        yourNameField.setOnKeyPressed((event) -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                yourName = yourNameField.getText();
                writeOnlineScore(yourName);
                revealScores(gridPane,localScoreBox,serverScoreBox,yourNameField,multiplayerScoreBox);
            }
        });

        localScoresList.getListProperty().bind(localScores);
        serverScoresList.getListProperty().bind(remoteScores);
        multiplayerScoresList.getListProperty().bind(multiplayerScores);

        if(!multiplayerGame){
            Text localScoreText = new Text("Local Score");
            localScoreText.getStyleClass().add("starWarsScoreSubTitle");
            localScoreBox.getChildren().add(localScoreText);

            localScoreBox.getChildren().add(localScoresList);
        }

        Text onlineScoreText = new Text("Server Score");
        onlineScoreText.getStyleClass().add("starWarsScoreSubTitle");
        serverScoreBox.getChildren().add(onlineScoreText);

        serverScoreBox.getChildren().add(serverScoresList);

        GridPane botSection = new GridPane();
        borderPane.setBottom(botSection);
        botSection.setAlignment(Pos.CENTER);
        botSection.setVgap(50);

        Button returnButton = new Button("Menu");
        returnButton.getStyleClass().add("starWarsMenuItem");
        returnButton.setOnMouseClicked(this::returnMenu);
        returnButton.setAlignment(Pos.CENTER);
        botSection.add(returnButton,0,0);
    }

    /**
     * Display what you scores means
     * @param gridPane
     * @param localScoreBox
     * @param serverScoreBox
     * @param yourNameField
     * @param multiplayerScoreBox
     */
    private void revealScores(GridPane gridPane,VBox localScoreBox,VBox serverScoreBox,TextField yourNameField,VBox multiplayerScoreBox) {
        gridPane.getChildren().remove(0);
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(30);
        Text text1 = new Text();
        text1.setText(yourName + "'s Score : " + gameScore);
        text1.getStyleClass().add("starWarsDisplayScoreBig");
        vBox.getChildren().add(text1);
        gridPane.getChildren().add(vBox);
        gridPane.setAlignment(Pos.TOP_CENTER);

        if(!communicator.getSinglePlayerGame()){
            if(!multiplayerGame){
                if(gameScore > arrayOfPairsForServerScore.get(arrayOfPairsForServerScore.size() - 1).getValue()) {
                    Text text2 = new Text("Congratulations you beat a online high score");
                    text2.getStyleClass().add("starWarsDisplayScoreMedium");
                    vBox.getChildren().add(text2);
                } else {
                    Text text3 = new Text("Sadly you didn't beat any online high score");
                    text3.getStyleClass().add("starWarsDisplayScoreMedium");
                    vBox.getChildren().add(text3);
                }
            } else {
                Text text = new Text("This score was also saved in your local list scores");
                text.getStyleClass().add("starWarsDisplayScoreMedium");
                vBox.getChildren().add(text);
            }
        }

        Button button = new Button("Continue");
        button.getStyleClass().add("starWarsMenuItem");
        vBox.getChildren().add(button);
        loadScores();
        writeScores();

        button.setOnMouseClicked(event -> revealScoresList(gridPane,localScoreBox,serverScoreBox,yourNameField,multiplayerScoreBox));
        button.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                revealScoresList(gridPane,localScoreBox,serverScoreBox,yourNameField,multiplayerScoreBox);
            }
        });
    }

    /**]
     * Reveal scores
     * @param gridPane
     * @param localScoreBox
     * @param serverScoreBox
     * @param yourNameField
     * @param multiplayerScoreBox
     */
    private void revealScoresList(GridPane gridPane,VBox localScoreBox,VBox serverScoreBox,TextField yourNameField, VBox multiplayerScoreBox) {
        gridPane.getChildren().remove(0);
        gridPane.add(localScoreBox,0,0);
        gridPane.add(multiplayerScoreBox,0,0);
        if(!communicator.getSinglePlayerGame()){
            gridPane.add(serverScoreBox,1,0);
        }
        if(!multiplayerGame){
            localScoresList.reveal();
        }
        if(!communicator.getSinglePlayerGame()){
            serverScoresList.reveal();
        }
        if(multiplayerGame){
            multiplayerScoresList.reveal();
        }

        logger.info("Your user name is " + yourName);
    }

    //Read from file
    public void loadScores() {
        try {
            bufferedReader = new BufferedReader(new FileReader("scores.txt"));
            //logger.info("it read the file");
        } catch (IOException e) {
            System.err.println("no such file in the directory");
            stopWriting = false;
        }

        //Read from file and add to the arrayList
        if(fileIsReady()){
            while(fileIsReady()) {
                scoreLine = getLine().split(":");
                logger.info("scanner is reading : " + scoreLine[1]);
                int scoreInteger = Integer.parseInt(scoreLine[1]);
                logger.info("Integer score is : " + scoreInteger);
                Pair<String,Integer> scorePair = new Pair<String,Integer>(scoreLine[0],scoreInteger);
                arrayOfPairsForLocalScore.add(scorePair);

            }
            //Also add your Score
            Pair<String,Integer> yourScore = new Pair<String,Integer>(yourName + " ",game.getScore());
            arrayOfPairsForLocalScore.add(yourScore);
            //Sort the arrayList
            for(int i = 0; i < arrayOfPairsForLocalScore.size(); i++) {
                for(int j = 0; j < arrayOfPairsForLocalScore.size(); j++) {
                    if(arrayOfPairsForLocalScore.get(i).getValue() > arrayOfPairsForLocalScore.get(j).getValue()){
                        Collections.swap(arrayOfPairsForLocalScore, i, j);
                    }
                }
            }
            //Add arrayList to the localList
            localScores.addAll(arrayOfPairsForLocalScore);
        } else {
            for(int i = 10000; i > 0; i = i - 1000){
                Pair<String,Integer> scorePair = new Pair<String,Integer>("Lucian",i);
                localScores.add(scorePair);
            }
        }
        for (Object o : localScores) {
            logger.info("Array is sortet in this way : " + o.toString());
        }

    }

    /**
     * Put the score online
     * @param message
     */
    public void loadOnlineScores(String message) {
        logger.info("arrayOfPairsForServerScore.size() = " + arrayOfPairsForServerScore.size());
        //logger.info("Receiving the following message in loadOnlineScore method " + message);
        String[] allTheHiScores = message.split(" ");
        //delete the extra column
        //allTheHiScores[1] = allTheHiScores[1].split(":")[1];
        if(allTheHiScores[0].equals("HISCORES")){
            arrayOfPairsForServerScore.clear();
            remoteScores.clear();
            logger.info("splitMessageWithScore [1] is :" + allTheHiScores[1]);
            for (int i = 0; i < 10; i++){
                String[] everyLineOfScore = allTheHiScores[1].split("\n");
                //logger.info("everyLineOfScopre [1]" + everyLineOfScore[1]);
                logger.info("every line is " + everyLineOfScore[i]);
                String[] eachElementOfScore = everyLineOfScore[i].split(":");
                logger.info("every element is key + value :  " + eachElementOfScore[0] + " && " + eachElementOfScore[1]);
                Pair<String,Integer> pair = new Pair<>(eachElementOfScore[0],Integer.parseInt(eachElementOfScore[1]));
                arrayOfPairsForServerScore.add(pair);
                //logger.info(("Server score in the for loop") + serverScores);
            }
            remoteScores.addAll(arrayOfPairsForServerScore);
            //logger.info(("Server score ") + arrayOfPairsForServerScore);
            logger.info("Array of remote list " + remoteScores);
        } else {
            logger.error("Failed to get the scores from online ");
        }
    }

    //Get one line
    public String getLine() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    //Check if you can read from file
    public boolean fileIsReady() {
        try {
            return bufferedReader.ready();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Write the scores in the score TXT
     */
    public void writeScores() {
        if(stopWriting) {
            try {
                bufferedWriter = new BufferedWriter(new FileWriter("scores.txt"));
            } catch (IOException e) {
                System.err.println("no such file in the directory");
            }
            try {
                logger.info("writes");
                for (Object pair : localScores) {
                    Pair<String, Integer> pair1 = (Pair<String, Integer>) pair;
                    bufferedWriter.write(pair1.getKey() + ":" + pair1.getValue() + "\n");
                    logger.info("has written : " + pair1.getKey());
                }
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * White the scores online
     * @param yourName
     */
    public void writeOnlineScore(String yourName) {
        //logger.info("array of pairs for server score length : " + arrayOfPairsForServerScore.size());
        //logger.info("It gets here in the write online score method" + arrayOfPairsForServerScore.get(arrayOfPairsForServerScore.size()-1).getKey() + "  " +arrayOfPairsForServerScore.get(arrayOfPairsForServerScore.size()-1).getValue());
        if(!communicator.getSinglePlayerGame()) {
            if(gameScore > arrayOfPairsForServerScore.get(arrayOfPairsForServerScore.size() - 1).getValue()) {
                communicator.send("HISCORE " + yourName + ":" + gameScore);
                communicator.send(("HISCORES UNIQUE"));
            }
        }

    }

    private void returnMenu(MouseEvent mouseEvent) {
        gameWindow.startMenu();
    }

}


