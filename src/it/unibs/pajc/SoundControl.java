package it.unibs.pajc;

import javax.sound.sampled.*;
import java.io.File;

public enum SoundControl {
    CUEBALL_HIT("cueballhit.wav"),
    BALL_POTTED("ballpotted.wav"),
    BALL_COLLISION("ballhit.wav");

    String fileName;
    Clip clip;

    private SoundControl(String fname) {
        this.fileName = fname;
        clip = loadSoundTrack(fname);
    }

    private Clip loadSoundTrack(String fname) {
        try {

            File audioFile = new File("resources/" + fname);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();

            clip.open(audioStream);

            return clip;

        } catch(Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public void play() {

        if (clip == null)
            return;
        new Thread(() -> {
            clip.setFramePosition(0);
            clip.start();

        }).start();
    }

}
