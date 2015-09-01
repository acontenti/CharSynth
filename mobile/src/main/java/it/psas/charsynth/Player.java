package it.psas.charsynth;

/**
 * Created by Alessandro Contenti on 28/08/2015.
 * Copyright © 2015 Alessandro Contenti.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Player {
    public static final double TWOPI = Math.PI * 2;
	public static final double ROOT12OF2 = Math.pow(2, 1.0f / 12.0f);
	public static final String alphabet_short = " 0123456789abcdefghijklmnopqrstuvwxyz";
	public static final String alphabet_extended = " 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final float basefrequency_short = 110.0f;
	public static final float basefrequency_extended = 55.0f;
	public enum STATE {PLAY, PAUSE, STOP}
	public enum WAVE {SINE, TRIANGLE, SAWTOOTH, INV_SAWTOOTH, SQUARE}
    private int minbufferlength;
    private AudioTrack audioTrack;
    private ParseThread parseThread;
    private Context c;
    private EditText et;
    private InputMethodManager imm;
    private STATE playing = STATE.STOP;
    private int SAMPLERATE = 44100;
    private double frequency = 0f;
	private String alphabet = alphabet_short;
	private float basefrequency = basefrequency_short;
    private char[] score = new char[0];
    private float tempo = 120;
    private boolean running = true;
    private final Object lock = new Object();
    public PlayerWatcher playerWatcher;
    private float noteValue = 30;
    private boolean newlineaspause = false;
    private boolean loop = false;
	private boolean goupafterfinish = true;
	private WAVE wave = WAVE.SINE;


    Player(Context context, EditText editText, InputMethodManager inputMethodManager) {
        c = context;
        et = editText;
        imm = inputMethodManager;
        minbufferlength = AudioTrack.getMinBufferSize(SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minbufferlength,
                AudioTrack.MODE_STREAM);
        SoundThread soundThread = new SoundThread();
        soundThread.start();
        parseThread = new ParseThread();
        parseThread.start();
        updateSettings();
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

	public boolean isGoingUpAfterFinish() {
		return goupafterfinish;
	}

    public void updateSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        newlineaspause = sharedPref.getBoolean(c.getString(R.string.newlineaspause_checkbox_key), newlineaspause);
		noteValue = Float.parseFloat(sharedPref.getString(c.getString(R.string.basenote_list_key), "n" + noteValue).replaceAll("[^0-9.]", ""));
		goupafterfinish = sharedPref.getBoolean(c.getString(R.string.goupafterfinish_checkbox_key), goupafterfinish);
		boolean soe = sharedPref.getBoolean(c.getString(R.string.shortorextended_switch_key), false);
		basefrequency = soe ? basefrequency_short : basefrequency_extended;
		alphabet = soe ? alphabet_short : alphabet_extended;
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
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        score = et.getText().toString().toLowerCase().replaceAll("[^a-zA-Z0-9 \n]", "").toCharArray();
        parseThread.startThread();
    }

    public void stop() {
        playing = STATE.STOP;
        et.setEnabled(true);
        et.requestFocus();
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        parseThread.stopThread();
    }

    public void stopFromCallback() {
        playing = STATE.STOP;
        et.setEnabled(true);
        et.requestFocus();
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    public void onActivityStop() {
        running = false;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
    }

    public class ParseThread extends Thread {
        public boolean running = false;
        public int i = 0;

        ParseThread() {
            super();
        }

        @Override
        public void run() {
            while (Player.this.running) {
                synchronized (lock) {
                    while (running && i < score.length) {
                        playerWatcher.onTick(i);
                        if (score[i] == '\n') {
                            if (newlineaspause) {
                                score[i] = ' ';
                            }
                            else {
                                i++;
                                continue;
                            }
                        }
                        int pos = alphabet.indexOf(score[i]);
                        frequency = basefrequency * Math.pow(ROOT12OF2, pos);
                        try {
                            Thread.sleep((long) (1000.0f * noteValue / tempo));
                        } catch (InterruptedException e) {e.printStackTrace();}
                        i++;
                    }
                    if (loop) {
                        i = 0;
                        continue;
                    }
                    if (running) {
                        stopThread();
                    }
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }

        public void pauseThread() {
            running = false;
            frequency = 0;
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
            frequency = 0;
            if (playerWatcher != null) playerWatcher.onStop();
        }
    }

    public void setPlayerWatcher(PlayerWatcher watcher) {
        playerWatcher = watcher;
    }

    public interface PlayerWatcher {
        void onTick(int position);
        void onStop();
    }

    public class SoundThread extends Thread {
        private double phase = 0;
        @Override
        public void run() {
            int bufl = minbufferlength / 10;
            short[] audioBuffer = new short[bufl];
            audioTrack.play();
            while (running) {
                for (int i = 0; i < bufl; i++) {
                    phase += frequency * TWOPI / SAMPLERATE;
                    if (phase > TWOPI) phase -= TWOPI;
                    audioBuffer[i] = (short) (synthesize(phase) * 10000);
                }
                audioTrack.write(audioBuffer, 0, bufl);
            }
            audioTrack.stop();
            audioTrack.release();
        }

	}

	private double synthesize(double phase) {
		switch (wave) {
			case SINE:
				return Math.sin(phase);
			case SQUARE:
				return Math.signum(Math.sin(phase));
			default:
				return Math.sin(phase);
		}
	}
}
