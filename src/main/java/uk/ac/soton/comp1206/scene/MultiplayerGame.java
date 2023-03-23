package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MultiplayerGame extends Game {

    private final Communicator communicator;
    private final Queue<GamePiece> pieceQueue= new LinkedList();
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    protected boolean firstTime = false;
    public VBox multiplayerPlayersScore = new VBox();
    //protected ScheduledExecutorService executor;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(Communicator communicator, int cols, int rows) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.addListener(message -> Platform.runLater(() -> receive(message)));
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    @Override
    public void initialiseGame() {
        logger.info("Initialising game");
        nextRound = executor.schedule(super :: restartGameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        restartGameLoop();
        score.set(0);
        level.set(0);
        lives.set(3);
        multiplier.set(1);
        for (int i = 0; i < 5; ++i) communicator.send("PIECE");
    }

    /**
     * This method will assign the received information to other methods as specified
     *
     * @param message from communicator
     */
    public void receive(String message) {
        logger.info("Received message: {}", message);
        String[] messageReceived = message.split(" ", 2);
        if (messageReceived[0].equals("PIECE") && messageReceived.length > 1) {
            receivePiece(Integer.parseInt(messageReceived[1]));
        }
        else if (messageReceived[0].equals("SCORES") && messageReceived.length > 1) {
            receiveScores(messageReceived[1]);
        }
    }

    /**
     * Receive the piece from the sever
     * @param piece
     */
    public void receivePiece(int piece) {
        //Set random direction
        GamePiece newPiece = GamePiece.createPiece(piece, 1);
        logger.info("Received piece: {}", newPiece);
        pieceQueue.add(newPiece);
        logger.info("Pieces received: {}", pieceQueue);
        if (!firstTime && pieceQueue.size() > 2) {
            logger.info("First time done");
            firstTime = true;
            followingPiece = spawnPiece();
            nextPiece();
            communicator.send("SCORES");
        }
    }

    /**
     * spawn a piece from the server
     * @return
     */
    @Override
    public GamePiece spawnPiece() {
        communicator.send("PIECE");
        return pieceQueue.poll();
    }

    /**
     * Update the score
     * @param numberOfLines
     * @param numberOfBlocks
     */
    public void score(int numberOfLines, int numberOfBlocks) {
        if(numberOfLines != 0){
            score.set(score.intValue() + (numberOfLines * numberOfBlocks * 10 * multiplier.intValue()));
            communicator.send("SCORE " + score.getValue());
        }
    }

    /**
     * For processing incoming score
     *
     * @param scores from communicator
     */
    public void receiveScores(String scores) {
        multiplayerPlayersScore.getChildren().clear();
        logger.info("Receive scores: {}", scores);
        String[] allScoreList = scores.split("\n");
        for (String oneScoreList : allScoreList) {
            String[] splitOneScore = oneScoreList.split(":");
            logger.info("Received oneScoreList: {} = {}", splitOneScore[0], Integer.parseInt(splitOneScore[1]));
            StringBuilder oneLinePLayers = new StringBuilder();
            oneLinePLayers.append(splitOneScore[0]);
            oneLinePLayers.append(" : ");
            oneLinePLayers.append(splitOneScore[1]);
            Text onePlayer = new Text(oneLinePLayers.toString());
            onePlayer.getStyleClass().add("starWarsVersus");
            multiplayerPlayersScore.getChildren().add(onePlayer);
            if(splitOneScore[2].equals("DEAD")) {
                onePlayer.getStyleClass().add("starWarsVersusCrossed");
            }

        }
    }

    public VBox getPlayersVBox(){
        return multiplayerPlayersScore;
    }

}
