package my2048.com.my2048.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import my2048.com.my2048.R;

public class HomeActivity extends Activity implements View.OnClickListener {

    private Button mainPlayButton;
    private Button mainResumeButton;
    private Button mainClockButton;
    private Button mainRankButton;

    private boolean showResume;
    private boolean showClock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showResume = sharedPreferences.getBoolean("can_resume", false);

        mainPlayButton = (Button)findViewById(R.id.main_play_button);
        mainResumeButton = (Button)findViewById(R.id.main_resume_button);
        mainClockButton = (Button)findViewById(R.id.main_clock_button);
        mainRankButton = (Button)findViewById(R.id.main_rank_button);
        //initialize the buttons
        initButtons();
        mainPlayButton.setOnClickListener(this);
        if (showResume) {
            mainResumeButton.setOnClickListener(this);
        }
        mainRankButton.setOnClickListener(this);
        if (showClock) {
            mainClockButton.setOnClickListener(this);
        }
    }

    private void initButtons() {
        if (!showResume) {
            mainResumeButton.setAlpha((float) 0.5);
        }
        showClock = false;
        mainClockButton.setAlpha((float) 0.5);
    }

    @Override
    public void onClick(View view) {
        view.setAlpha((float) 0.8);
        switch (view.getId()) {
            case R.id.main_resume_button :
                Intent intent = new Intent(this, GameActivity.class);
                startActivity(intent);
                break;
            case R.id.main_play_button :
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean("can_resume", false);
                editor.commit();

                intent = new Intent(this, GameActivity.class);
                startActivity(intent);
                break;
            case R.id.main_clock_button : break;
            case R.id.main_rank_button : break;
            default : break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showResume = sharedPreferences.getBoolean("can_resume", false);
        if (showResume) {
            mainResumeButton.setAlpha(1.0f);
            mainResumeButton.setOnClickListener(this);
        } else {
            mainResumeButton.setAlpha(0.5f);
        }
        mainPlayButton.setAlpha(1.0f);
        mainRankButton.setAlpha(1.0f);
    }
}
