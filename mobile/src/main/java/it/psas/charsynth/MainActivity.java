package it.psas.charsynth;

/**
 * Created by Project s.a.s. on 28/08/2015.
 * Copyright © 1996, 2015 PROJECT s.a.s. All Rights Reserved.
 */
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {

    private Player player;
    private Menu menu;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setSmoothScrollingEnabled(true);
        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.isEnabled()) editText.requestFocus();
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
        editText.requestFocus();
        player = new Player(this, editText);
        player.setOnStopListener(new Player.OnStopListener() {
            @Override
            public void onStop() {
                runOnUiThread(new Runnable() {
                    public void run(){
                        menu.findItem(R.id.action_play).setTitle(R.string.action_play);
                        menu.findItem(R.id.action_play).setIcon(R.drawable.ic_action_play);
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
        menu.findItem(R.id.action_play).setTitle(R.string.action_play);
        menu.findItem(R.id.action_play).setIcon(R.drawable.ic_action_play);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.onStop();
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
                    item.setTitle(R.string.action_play);
                    item.setIcon(R.drawable.ic_action_play);
                }
                else {
                    player.play();
                    item.setTitle(R.string.action_pause);
                    item.setIcon(R.drawable.ic_action_pause);
                }
                break;
            case R.id.action_stop:
                player.stop();
                menu.findItem(R.id.action_play).setTitle(R.string.action_play);
                menu.findItem(R.id.action_play).setIcon(R.drawable.ic_action_play);
                break;
            case R.id.action_clear:
                if (editText.isEnabled()) editText.setText("");
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }
}
