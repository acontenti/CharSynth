package it.psas.charsynth;

/**
 * Created by Project s.a.s. on 28/08/2015.
 * Copyright © 1996, 2015 PROJECT s.a.s. All Rights Reserved.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class MainActivity extends AppCompatActivity {

    private Player player;
    private Menu menu;
    private EditText editText;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        editText = (EditText) findViewById(R.id.editText);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setSmoothScrollingEnabled(true);
        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.isEnabled()) {
                    editText.requestFocus();
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source instanceof SpannableStringBuilder) {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                    for (int i = end - 1; i >= start; i--) {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar) && !Character.isSpaceChar(currentChar) && currentChar != '\n') {
                            sourceAsSpannableBuilder.delete(i, i+1);
                        }
                    }
                    return source;
                } else {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++) {
                        char currentChar = source.charAt(i);
                        if (Character.isLetterOrDigit(currentChar) || Character.isSpaceChar(currentChar) || currentChar == '\n') {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        };
        editText.setFilters(new InputFilter[]{filter});
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.requestFocus();
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
        editText.setSelection(0);
        player = new Player(this, editText, imm);
        player.setPlayerWatcher(new Player.PlayerWatcher() {
            @Override
            public void onStop() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        player.stopFromCallback();
                        setPlayMenuToPauseStop();
                        editText.setSelection(0);
                    }
                });
            }
            @Override
            public void onTick(final int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editText.setSelection(position, position + 1);
                    }
                });
            }
        });
        DiscreteSeekBar tempo_bar = (DiscreteSeekBar) findViewById(R.id.seekBar_tempo);
        tempo_bar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean b) {
                player.setTempo((long) (60000.0f / (float) i));
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
        setPlayMenuToPauseStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.onActivityStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_play:
                if (player.isPlaying()) {
                    player.pause();
                    setPlayMenuToPauseStop();
                }
                else {
                    player.play();
                    item.setTitle(R.string.action_pause);
                    item.setIcon(R.drawable.ic_action_pause);
                }
                break;
            case R.id.action_stop:
                player.stop();
                setPlayMenuToPauseStop();
                break;
            case R.id.action_clear:
                if (editText.isEnabled()) editText.setText("");
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPlayMenuToPauseStop() {
        menu.findItem(R.id.action_play).setTitle(R.string.action_play);
        menu.findItem(R.id.action_play).setIcon(R.drawable.ic_action_play);
    }
}
