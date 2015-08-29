package it.psas.charsynth;

/**
 * Created by Project s.a.s. on 28/08/2015.
 * Copyright © 1996, 2015 PROJECT s.a.s. All Rights Reserved.
 */
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.widget.EditText;

public class Player {
    private static double TWOPI = Math.PI * 2;
    private int minbufferlength;
    private AudioTrack audioTrack;
    private ParseThread parseThread;
    private Context c;
    private EditText et;
    private STATE playing = STATE.STOP;
    private int fs = 44100;
    private double f = 0f;
    private String alphabet = " 0123456789abcdefghijklmnopqrstuvwxyz";
    private char[] score = new char[0];
    private long pause = 500;
    private boolean running = true;
    private final Object lock = new Object();
    public OnStopListener onstoplistener;
    public enum STATE {PLAY, PAUSE, STOP}

    Player(Context context, EditText editText) {
        c = context;
        et = editText;

        minbufferlength = AudioTrack.getMinBufferSize(fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minbufferlength,
                AudioTrack.MODE_STREAM);

        Thread soundThread = new Thread() {
            private double phase = 0;

            public void run() {

                int bufl = minbufferlength / 10;

                short[] audioBuffer = new short[bufl];

                audioTrack.play();

                while (running) {
                    for (int i = 0; i < bufl; i++) {
                        phase += f * TWOPI / fs;
                        if (phase > TWOPI) phase -= TWOPI;
                        audioBuffer[i] = (short) (Math.sin(phase) * 10000);
                    }
                    audioTrack.write(audioBuffer, 0, bufl);
                }
                audioTrack.stop();
                audioTrack.release();
            }
        };
        soundThread.start();
        parseThread = new ParseThread();
        parseThread.start();
    }

    public boolean isPlaying() {
        return playing == STATE.PLAY;
    }

    public void pause() {
        playing = STATE.PAUSE;
        parseThread.pauseThread();
    }

    public void play() {
        playing = STATE.PLAY;
        et.setEnabled(false);
        score = et.getText().toString().toLowerCase().replaceAll("[^a-z0-9 ]", "").toCharArray();
        parseThread.startThread();
    }

    public void stop() {
        playing = STATE.STOP;
        et.setEnabled(true);
        et.requestFocus();
        parseThread.stopThread();
    }

    public void onStop() {
        running = false;
    }

    public class ParseThread extends Thread {
        public boolean running = false;
        public int i = 0;
        private Handler handler = new Handler();

        ParseThread() {
            super();
        }

        @Override
        public void run() {
            while (Player.this.running) {
                synchronized (lock) {
                    while (running && i < score.length) {
                        int pos = alphabet.indexOf(score[i]);
                        f = pos * 50;
                        try {
                            Thread.sleep(pause);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        i++;
                    }
                    if (running) stopThread();
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }

        public void pauseThread() {
            running = false;
            f = 0;
        }
        public void startThread() {
            synchronized (lock) {
                running = true;
                lock.notifyAll();
            }
        }
        public void stopThread() {
            running = false;
            i = 0;
            f = 0;
            if (onstoplistener != null) onstoplistener.onStop();
        }
    }

    public void setOnStopListener(OnStopListener onstoplistener) {
        this.onstoplistener = onstoplistener;
    }

    public interface OnStopListener {
        void onStop();
    }
}
