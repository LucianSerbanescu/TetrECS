package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.security.Key;

import static uk.ac.soton.comp1206.scene.MenuScene.muteSwitcher;

public class InstructionsScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Constructor
     * @param gameWindow
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    /**
     * Initialise the game
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(this::keyPressed);
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        //Create the root GamePane
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        //Create a StackPane to put the background
        StackPane instructionsSceneStack = new StackPane();
        instructionsSceneStack.setMaxWidth(gameWindow.getWidth());
        instructionsSceneStack.setMaxHeight(gameWindow.getHeight());
        instructionsSceneStack.getStyleClass().add("starWarsInstructionsSceneBackground");
        root.getChildren().add(instructionsSceneStack);

        //Create a borderPane to palce items
        BorderPane instructionsPane = new BorderPane();
        instructionsSceneStack.getChildren().add(instructionsPane);

        //Create a VBox to represent the top writing
        //There will be 2 elements
        VBox topSection = new VBox();
        //topSection.setSpacing(10);
        topSection.setAlignment(Pos.TOP_CENTER);
        instructionsPane.setTop(topSection);

        Text blankSpace = new Text(" ");
        topSection.getChildren().add(blankSpace);

        //Create the first writing text
        //Create the title
        Text introductionText = new Text("How to play");
        introductionText.getStyleClass().add("starWarsMandalorThemeInstruction");
        topSection.getChildren().add(introductionText);

        //Create the description
        Text subtitleIntroductionText = new Text("TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
        subtitleIntroductionText.getStyleClass().add("starWarsInstructions");
        TextFlow subtitleIntroductionTextFlow = new TextFlow(subtitleIntroductionText);
        subtitleIntroductionText.setTextAlignment(TextAlignment.CENTER);
        subtitleIntroductionTextFlow.setTextAlignment(TextAlignment.CENTER);
        topSection.getChildren().add(subtitleIntroductionTextFlow);

        //Create the image instructions
        ImageView imageInstructions = new ImageView(getClass().getResource("/images/Instructions.png").toExternalForm());
        imageInstructions.setFitWidth((double) gameWindow.getWidth() / 1.5D);
        imageInstructions.setPreserveRatio(true);
        instructionsPane.setCenter(imageInstructions);

        //Create the pieces display
        VBox pieceDisplayBox = new VBox();
        instructionsPane.setBottom(pieceDisplayBox);

        //Create the text
        Text pieceDisplayText = new Text("Game Pieces");
        pieceDisplayText.getStyleClass().add("starWarsInstructions");
        pieceDisplayBox.getChildren().add(pieceDisplayText);
        pieceDisplayBox.setSpacing(6);
        //pieceDisplayBox.setAlignment(Pos.CENTER);
        pieceDisplayText.setTextAlignment(TextAlignment.CENTER);

        //Create the actual pieces which are displayed
        GridPane gridToDisplayPieces = new GridPane();
        pieceDisplayBox.getChildren().add(gridToDisplayPieces);
        gridToDisplayPieces.setAlignment(Pos.CENTER);
        pieceDisplayBox.setAlignment(Pos.CENTER);
        gridToDisplayPieces.setVgap(10);
        gridToDisplayPieces.setHgap(10);

        //declare the first row and column in the boardGrid
        int counter = 0;
        //Set the pieces which game provides
        for (int i = 1; i <= 3; i++) {
            for(int j =  1; j <= 5; j++) {
                //create all the pieces each for every interation

                GamePiece gamePiece = GamePiece.createPiece(counter);
                counter ++;
                //create the grid where to place them
                PieceBoard pieceBoardGrid = new PieceBoard(3,3,gameWindow.getWidth()/15, gameWindow.getWidth()/15);
                //place the game piece on the grid
                pieceBoardGrid.setPiece(gamePiece);
                gridToDisplayPieces.add(pieceBoardGrid,i,j);
            }
        }
    }

    /**
     * Set the key events
     * @param e
     */
    private void keyPressed(KeyEvent e){
        boolean switcherValue;
        if (e.getCode().equals(KeyCode.M)){
            switcherValue = muteSwitcher % 2 == 0;
            muteSwitcher++;
            logger.info("Switcher value passed : " + switcherValue);
            Multimedia.shouldPlayMusic(switcherValue);
        } else if(e.getCode().equals(KeyCode.ESCAPE)){
            gameWindow.startMenu();
        }
    }

}
