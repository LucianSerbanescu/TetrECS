package uk.ac.soton.comp1206.component;


import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.ScoresScene;

public class ScoresList extends VBox{

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    protected SimpleListProperty<Pair<String, Integer>> scoreList = new SimpleListProperty();

    /**
     * Put the alignment
     */
    public ScoresList() {
        this.setAlignment(Pos.CENTER);
    }

    /**
     * Reveal the List
     */
    public void reveal() {

        logger.info("Start the reveal method ");

        VBox vBox = new VBox();

        for(Object o : scoreList){
            logger.info("scoreList in ScoresListClass is : " + o.toString());
        }
        for(int i = 0; (i < 10 && i < scoreList.size()); i++) {
            Pair<String, Integer> scorePair = scoreList.get(i);
            Text playerScore = new Text(scorePair.getKey() + " : " + scorePair.getValue());
            playerScore.getStyleClass().add("starWarsDisplayScore");
            vBox.getChildren().add(playerScore);
        }
        vBox.setAlignment(Pos.CENTER);
        this.getChildren().add(vBox);
        logger.info("The reveal list is " + scoreList);
    }

    /**
     * Return score List
     * @return
     */
    public ListProperty<Pair<String, Integer>> getListProperty() {
        return scoreList;
    }

}
