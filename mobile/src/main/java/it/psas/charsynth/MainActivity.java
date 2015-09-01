package it.psas.charsynth;

/**
 * Created by Alessandro Contenti on 28/08/2015.
 * Copyright © 2015 Alessandro Contenti.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
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
        scrollView.setOnClickListener(openKeyboardOnClickListener);
        findViewById(R.id.scrollView_linearLayout).setOnClickListener(openKeyboardOnClickListener);
        editText.setFilters(new InputFilter[]{Utils.editTextInputFilter});
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openKeyboard();
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
        player = new Player(this, editText, imm);
        player.setPlayerWatcher(new Player.PlayerWatcher() {
            @Override
            public void onStop() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        player.stopFromCallback();
                        setPlayMenuToPauseStop();
						if (player.isGoingUpAfterFinish()) {
							editText.setSelection(0);
						}
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
                player.setTempo((float) i);
            }
            @Override public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {}
            @Override public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {}
        });
        chackLaunchFromSendIntent(getIntent());
        editText.setSelection(0);
    }

    private void chackLaunchFromSendIntent(Intent recieverIntent) {
        if (recieverIntent != null && recieverIntent.getAction().equals(Intent.ACTION_SEND) && recieverIntent.getType().equals("text/plain")) {
            String sharedText = recieverIntent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null && sharedText.length() > 0) {
                editText.setText(sharedText);
            }
            else {
                new AlertDialog.Builder(MainActivity.this).setTitle("Sorry!").setMessage("Could not read data").setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialog, int which) {}}).show();
            }
        }
    }

    View.OnClickListener openKeyboardOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (editText.isEnabled()) {
                openKeyboard();
            }
        }
    };

    private void openKeyboard() {
        editText.requestFocus();
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.updateSettings();
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
            case R.id.action_loop:
                item.setChecked(!item.isChecked());
                player.setLoop(item.isChecked());
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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
