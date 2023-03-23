package uk.ac.soton.comp1206.multimedia;

import javafx.beans.property.BooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.constant.ClassDesc;

public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Media.class);
    private static MediaPlayer musicPlayer;
    private static MediaPlayer audioPlayer;
    public static boolean shouldPlayMusic = true;
    private static String lastMusic;

    //Method for playing any audio

    /**
     * Play Sounds
     * @param audioFileName
     */
    public static void playAudio(String audioFileName) {
        if(shouldPlayMusic) {
            logger.info("Start playAudio method");
            //Initialise the files to be played
            String audio = Multimedia.class.getResource("/sounds/" + audioFileName).toExternalForm();
            Media mediaAudio = new Media(audio);
            //Actually playing the files
            audioPlayer = new MediaPlayer(mediaAudio);

            //check if the files can be accesed
            try {
                //Try to play the music
                logger.info("Trying to play audio");
                audioPlayer.play();
                logger.info("Audio played");
            } catch (Exception exception) {
                //Can't play music
                logger.info("Can't play audio");
                exception.printStackTrace();
            }
        }

    }

    /**
     * PLay music
     * @param startMusic
     */
    public static void startPlayBackgroundMusic(String startMusic) {
        if(shouldPlayMusic){
            lastMusic = startMusic;
            logger.info("Last music is : " + lastMusic);
            String startMusicPath = Multimedia.class.getResource("/music/" + lastMusic).toExternalForm();
            Media media = new Media(startMusicPath);
            musicPlayer = new MediaPlayer(media);
            try {
                musicPlayer.play();
                logger.info("Start to play background music from the start music method, and the music is {}", startMusic);
                //Here I was helped by helpDesk
                musicPlayer.setOnEndOfMedia(() -> {
                    //recursively play music
                    startPlayBackgroundMusic(startMusic);
                });
            } catch (Exception exception) {
                exception.printStackTrace();
                logger.error("Can't play music background");
            }
        }
    }

    /**
     * stop the music
     */
    public static void stop() {
        if (audioPlayer != null){
            audioPlayer.stop();
        }
        if (musicPlayer != null){
            musicPlayer.stop();
        }
    }

    /**
     * Check if the music should play
     * @param shouldPlayMusic
     */
    public static void shouldPlayMusic(boolean shouldPlayMusic) {
        Multimedia.shouldPlayMusic = shouldPlayMusic;
        checkIfMusicShouldBePlayer();
    }

    /**
     * Check if music should play
     */
    public static void checkIfMusicShouldBePlayer() {
        if(!Multimedia.shouldPlayMusic) {
            stop();
        } else {
            if(lastMusic != null){
                startPlayBackgroundMusic(lastMusic);
            }
        }
    }

}
