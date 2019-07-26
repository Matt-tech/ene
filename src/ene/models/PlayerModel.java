package ene.models;

import ene.models.AbstractModel;
import ene.models.TrackModel;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Player class.
 */
public class PlayerModel extends AbstractModel implements LineListener {
    /**
     * Clip instance.
     */
    Clip clip;

    /**
     * Line event instance.
     */
    LineEvent lastEvent;

    /**
     * Sets the clip.
     * @param clip Clip instance.
     */
    protected void setClip(Clip clip) {
        this.clip = clip;
        this.changed();
    }

    /**
     * Returns the clip.
     * @return Clip instance.
     */
    protected Clip getClip() {
        return this.clip;
    }

    /**
     * Sets the last event.
     * @param event Line event instance.
     */
    protected void setLastEvent(LineEvent event) {
        this.lastEvent = event;
        this.changed();
    }

    /**
     * Returns the last event.
     * @return Line event instance.
     */
    public LineEvent getLastEvent() {
        return this.lastEvent;
    }

    /**
     * Load track.
     * @param track Track model instance.
     * @return Returns TRUE, if successful. Otherwise FALSE.
     */
    public boolean load(TrackModel track) {
        try {
            Clip clip = this.getClip();
            if (clip.isOpen()) clip.close();
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(track.getFilename()));
            clip = (Clip) AudioSystem.getLine(
                new DataLine.Info(
                    this.clip.getClass(),
                    audioStream.getFormat()
                )
            );
            clip.addLineListener(this);
            clip.open(audioStream);
            this.setClip(clip);
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    /**
     * Start playing.
     */
    public void start() {
        this.getClip().start();
    }

    /**
     * Stop playing.
     */
    public void stop() {
        this.getClip().stop();
    }

    @Override
    public void update(LineEvent event) {
        this.setLastEvent(event);
    }
}
