package ene.views;

import javax.swing.plaf.metal.MetalSliderUI;
import ene.controllers.PlayerController;
import ene.interfaces.Controller;
import ene.interfaces.Localization;
import ene.interfaces.Model;
import ene.models.PlayerModel;
import ene.views.AbstractView;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Player view class.
 */
public class PlayerView extends AbstractView <JPanel, PlayerModel> implements Localization {
    /**
     * Player controller instance.
     */
    protected PlayerController playerController;

    /**
     * Play/Stop button.
     */
    protected JButton playButton;

    /**
     * Progress slider.
     */
    protected JSlider progressSlider;

    /**
     * Progress label.
     */
    protected JLabel progressLabel;

    /**
     * Progress refresh timer.
     */
    Timer progressTimer;

    /**
     * Track time format instance.
     */
    SimpleDateFormat trackTimeFormat;

    /**
     * Sets the player controller instance.
     * @param playerController Player controller instance.
     */
    protected void setPlayerController(Controller playerController) {
        this.playerController = (PlayerController)playerController;
    }

    /**
     * Returns the player controller instance.
     * @return Player controller instance.
     */
    protected PlayerController getPlayerController() {
        return this.playerController;
    }

    /**
     * Constructor.
     * @param model Player model instance.
     * @param controller Player controller instance.
     */
    public PlayerView(Model model, Controller playerController) {
        model.addView(this);
        this.setModel(model);
        this.setPlayerController(playerController);
        this.setLayoutPosition(BorderLayout.SOUTH);
        this.initialize();
    }

    /**
     * Initializing.
     */
    protected void initialize() {
        // Panel
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setCoreComponent(panel);
        // Play button.
        this.playButton = new JButton(getString("PLAY_BUTTON_START"));
        this.playButton.setFocusPainted(false);
        this.playButton.addActionListener(event -> {
            debugInfoAbout(event);
            this.getPlayerController().togglePlayback();
        });
        panel.add(this.playButton, BorderLayout.WEST);
        // Progress slider.
        JSlider slider = this.progressSlider = new JSlider(0, 100, 0);
        this.progressSlider.setUI(new MetalSliderUI() {
            @Override
            protected void scrollDueToClickInTrack(int direction) {
                slider.setValue(this.valueForXPosition(slider.getMousePosition().x));
            }
        });
        this.progressSlider.addChangeListener(event -> {
            this.getPlayerController().changeTrackPosition(progressSlider.getValue());
        });
        panel.add(this.progressSlider, BorderLayout.CENTER);
        // Progress label.
        this.progressLabel = new JLabel("--:-- / --:--");
        this.progressLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.trackTimeFormat = new SimpleDateFormat("mm:ss");
        this.trackTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        panel.add(this.progressLabel, BorderLayout.EAST);
        // Progress refresh timer.
        this.progressTimer = new Timer(100, event -> {
            this.updateProgress();
        });
        // Prepare player controls.
        this.disablePlayerControls();
    }

    /**
     * Enables player controls.
     */
    protected void enablePlayerControls() {
        this.playButton.setEnabled(true);
        this.progressSlider.setEnabled(true);
        this.progressLabel.setEnabled(true);
    }

    /**
     * Disables player controls.
     */
    protected void disablePlayerControls() {
        this.playButton.setEnabled(false);
        this.progressSlider.setEnabled(false);
        this.progressLabel.setEnabled(false);
    }

    /**
     * Updates the progress slider and label.
     */
    protected void updateProgress() {
        int trackLength = this.getModel().getTrackLength();
        int trackPosition = this.getModel().getTrackPosition();
        this.progressSlider.setMaximum(trackLength);
        this.progressSlider.setValue(trackPosition);
        this.progressLabel.setText(
            this.formatTimeToString(trackPosition) +
            " / " +
            this.formatTimeToString(trackLength)
        );
    }

    /**
     * Resets the track progress.
     */
    protected void resetProgress() {
        this.getPlayerController().changeTrackPosition(0);
        this.updateProgress();
    }

    /**
     * Returns a string representation of the time.
     * @param timeInSeconds Time in seconds.
     * @return String representation of the time.
     */
    protected String formatTimeToString(int timeInSeconds) {
        return this.trackTimeFormat.format(new Date(timeInSeconds*1000));
    }

    @Override
    public void update() {
        Type lastEventType = this.getModel().getLastEvent().getType();
        // Track opened.
        if (lastEventType == Type.OPEN) {
            this.resetProgress();
            this.enablePlayerControls();
        // Track closed.
        } else if (lastEventType == Type.CLOSE) {
            this.resetProgress();
            this.progressLabel.setText("--:-- / --:--");
            this.disablePlayerControls();
        // Track started.
        } else if (lastEventType == Type.START) {
            this.progressTimer.start();
            this.playButton.setText(getString("PLAY_BUTTON_PAUSE"));
        // Track paused.
        } else if (lastEventType == Type.STOP) {
            PlayerModel playerModel = this.getModel();
            if (playerModel.getTrackPosition() == playerModel.getTrackLength()) this.resetProgress();
            this.progressTimer.stop();
            this.playButton.setText(getString("PLAY_BUTTON_START"));
        }
    }
}
