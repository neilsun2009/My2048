package my2048.com.my2048.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import my2048.com.my2048.R;
import my2048.com.my2048.db.My2048DB;
import my2048.com.my2048.model.My2048Data;
import my2048.com.my2048.utility.Hash;
import my2048.com.my2048.utility.LogUtil;
import my2048.com.my2048.utility.NetworkUtil;
import my2048.com.my2048.utility.TimeUtil;

import org.w3c.dom.Text;


public class GameActivity extends Activity implements View.OnClickListener {

    //timer

    Timer mTimer;
    TimerTask mTimerTask;
    Handler timerHandler;
    boolean isPause;

    TextView scoreScoreTxt;
    TextView scoreHighestTxt;
    TextView scoreTimeTxt;
    TextView scoreStepTxt;
    EditText submitName;
    Button btnPause;
    // Button btnMenu;
    ImageView[] oriImage = new ImageView[16];

    // ImageView oriImage01;

    LinearLayout mainArea;
    RelativeLayout canvas;
    RelativeLayout hoverLayout;
    Button btnStart;

    //coefficients to get the location of the image
    int[] imageBorder;
    int imageWidth;
    int imageHeight;

    //data etc
    My2048Data my2048Data;
    My2048DB my2048DB;
    boolean canResume;
    boolean isCountdown;
    int id;
    int maxScore;

    //direction
    final int LEFT = 0;
    final int RIGHT = 1;
    final int UP = 2;
    final int DOWN = 3;

    //animation
    final int animTime = 100;

    //0 for normal mode, 1 for countdown mode
    final int MODE_NORMAL = 0;
    final int MODE_COUNTDOWN = 1;
    int gameType = MODE_NORMAL;

    // TimeUtil backTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);
        my2048DB = My2048DB.getInstance(this);


        Bundle type =this.getIntent().getExtras();
        gameType = type.getInt("type");



        // main area
        canvas = (RelativeLayout)findViewById(R.id.canvas);
        mainArea = (LinearLayout)findViewById(R.id.main_area);
        scoreScoreTxt = (TextView)findViewById(R.id.score_score_txt);
        scoreHighestTxt = (TextView)findViewById(R.id.score_highest_txt);
        scoreStepTxt = (TextView)findViewById(R.id.score_step_txt);
        scoreTimeTxt = (TextView)findViewById(R.id.score_time_txt);
        btnPause = (Button) findViewById(R.id.game_btn_pause);
        // btnMenu = (Button) findViewById(R.id.game_btn_menu);
        for (int i = 0; i < 16; ++i) {
            oriImage[i] = (ImageView)findViewById(Hash.ORI_IMAGE_RESOURCE[i]);
        }

        // hover area before start
        btnStart = (Button)findViewById(R.id.btn_start);
        btnStart.setText("START");
        btnStart.setOnClickListener(this);
        // only normal mode shows pause button
        if (gameType == MODE_COUNTDOWN) {
            btnPause.setVisibility(View.GONE);
        } else {
            btnPause.setOnClickListener(this);
        }
        // btnMenu.setOnClickListener(this);
        hoverLayout = (RelativeLayout)findViewById(R.id.hover_layout);

        // main area slide events
        mainArea.setOnTouchListener(new View.OnTouchListener() {
            float[] prev = new float[2];

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        prev[0] = motionEvent.getX();
                        prev[1] = motionEvent.getY();
                        // LogUtil.d("Touch", "touchdown");
                        return true; // crucial
                    case MotionEvent.ACTION_UP:
                        float[] move = new float[2];
                        move[0] = prev[0] - motionEvent.getX();
                        move[1] = prev[1] - motionEvent.getY();
                        // LogUtil.d("Touch", "touchup");
                        if (move[0] > 50 && move[0] < 5000 && move[1] < 100 && move[1] > -100) {
                            // LogUtil.d("Touch", "left");
                            moveBlock(LEFT);
                        } else if (move[0] < -50 && move[0] > -5000 && move[1] < 100 && move[1] > -100) {
                            // LogUtil.d("Touch", "right");
                            moveBlock(RIGHT);
                        } else if (move[1] > 50 && move[1] < 5000 && move[0] < 100 && move[0] > -100) {
                            // LogUtil.d("Touch", "up");
                            moveBlock(UP);
                        } else if (move[1] < -50 && move[1] > -5000 && move[0] < 100 && move[0] > -100) {
                            // LogUtil.d("Touch", "down");
                            moveBlock(DOWN);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:

                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        //initialize

    }

    private void saveDataForPause() {
            if (maxScore == 0) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                maxScore = sharedPreferences.getInt("max_score", 0);
                if (my2048Data != null) {
                    if (maxScore < my2048Data.getScore()) {
                        maxScore = my2048Data.getScore();
                    }
                }
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean("can_resume", true);
            editor.putInt("max_score", maxScore);
            LogUtil.d("Init-pause", Integer.toString(maxScore));
            editor.commit();
            if (my2048Data == null) return;
            List<My2048Data> list = my2048DB.queryData(0, 0);
            if (list.isEmpty()) {
                my2048DB.insertData(my2048Data);
            } else {
                my2048DB.updateData(0, my2048Data);
                LogUtil.d("StartNew", "Update");
            }
            isPause = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameType == MODE_NORMAL) {
            saveDataForPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // hoverLayout.setVisibility(View.VISIBLE);
        // btnStart.setText("RESUME");
        isPause = false;
        /*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        maxScore = sharedPreferences.getInt("max_score", 0);
        scoreHighestTxt.setText(Integer.toString(maxScore));*/
    }

    @Override
    protected  void onStop() {
        super.onStop();
        stopTimer();

    }

    private void moveBlock(int direction) {
        checkGameOver();
        boolean noChange = true;
        int maxTimes = 0; // max moving times, to get appropriate new piece show-up time
        boolean[] canMerge = new boolean[16];
        for (int i = 0; i < 16; ++i) {
            canMerge[i] = true;
        }
        for (int i = 4; i < 16; ++i) {
            int now = Hash.MOVE_SEQUENCE[direction][i], tem = now+Hash.MOVE_SEQUENCE[direction][16];
            if (my2048Data.getNumbers()[now] == 0) {
                continue;
            }
            int start = now, end = tem, num = my2048Data.getNumbers()[start];
            boolean move = false, merge = false;
            int timeMax = Hash.MOVE_BOUNDRY[direction][now], time = 0;
            while ((my2048Data.getNumbers()[tem] == 0 || my2048Data.getNumbers()[tem] == my2048Data.getNumbers()[now])
                    && time < timeMax) {
                noChange = false;
                move = true;
                end = tem;
                ++time;
                if (time > maxTimes) {
                    maxTimes = time;
                }
                if (my2048Data.getNumbers()[tem] == 0) {
                    // LogUtil.d("Move", now + " " + tem);
                    my2048Data.setNumber(tem, my2048Data.getNumbers()[now]);
                    my2048Data.setNumber(now, 0);
                    now = tem;
                    tem = now+Hash.MOVE_SEQUENCE[direction][16];
                    if (time == timeMax) break;
                    if (!canMerge[tem]) break;
                } else {
                    my2048Data.setNumber(now, 0);
                    my2048Data.setNumber(tem, my2048Data.getNumbers()[tem]*2);
                    my2048Data.setScore(my2048Data.getScore()+my2048Data.getNumbers()[tem]);
                    canMerge[tem] = false;
                    merge = true;
                    break;
                }
            }
            if (move) {
                startTranslateAnimation(start, end, num, time, merge);
            }
        }
        if (!noChange) {
            my2048Data.setStep(my2048Data.getStep()+1);
            scoreStepTxt.setText(Integer.toString(my2048Data.getStep()));
            scoreScoreTxt.setText(Integer.toString(my2048Data.getScore()));
            if (my2048Data.getScore() > maxScore) {
                maxScore = my2048Data.getScore();
                scoreHighestTxt.setText(Integer.toString(maxScore));
            }
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0x789) {
                        setRandomNewPiece();

                        // checkGameOver();
                    }
                    if (msg.what == 0xabc) {
                        LogUtil.d("Over", "Ready to Check");
                        checkGameOver();
                    }
                }
            };
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(0x789);
                }
            }, 100 * maxTimes);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(0xabc);
                }
            }, 100 * maxTimes + 150 );
        }

    }

    private void startMergePieceAnimation(final int position, final int num) {
        final ImageView temView = setNewImageView(position, num);
        // ScaleAnimation anim = new ScaleAnimation(1.0f, 0.5F, 1.0f, 0.5F);
        // anim.setDuration(50);
        //Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate);

        //temView.startAnimation(anim);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(temView, "scaleX", 1.0f, 1.1f, 1.0f);
        anim1.setDuration(animTime);
        // anim1.setRepeatMode(Animation.REVERSE);

        ObjectAnimator anim2 = ObjectAnimator.ofFloat(temView, "scaleY", 1.0f, 1.1f, 1.0f);
        anim2.setDuration(animTime);
        // anim2.setRepeatMode(Animation.REVERSE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anim1).with(anim2);
        animatorSet.start();
        setNewPiece(position, num);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x456) {
                    canvas.removeView(temView);
                }
            }
        };
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x456);
            }
        }, animTime);
    }
    private void startNewPieceAnimation(final int position, final int num) {
        final ImageView temView = setNewImageView(position, num);
       // ScaleAnimation anim = new ScaleAnimation(1.0f, 0.5F, 1.0f, 0.5F);
       // anim.setDuration(50);
        //Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate);

        //temView.startAnimation(anim);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(temView, "scaleX", 0.5f, 1.0f);
        anim1.setDuration(animTime);
        // anim1.setRepeatMode(Animation.REVERSE);

        ObjectAnimator anim2 = ObjectAnimator.ofFloat(temView, "scaleY", 0.5f, 1.0f);
        anim2.setDuration(animTime);
        // anim2.setRepeatMode(Animation.REVERSE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anim1).with(anim2);
        animatorSet.start();

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x456) {
                    canvas.removeView(temView);
                    setNewPiece(position, num);
                }
            }
        };
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x456);
            }
        }, animTime);
    }

    private void startTranslateAnimation(final int prev, final int now, final int num, int time, final boolean merge) {
        final ImageView temView = setNewImageView(prev, num);

        /*TranslateAnimation anim = new TranslateAnimation(0, Hash.imageLocation[now][0]-Hash.imageLocation[prev][0],
                0, Hash.imageLocation[now][1]-Hash.imageLocation[prev][1]);*/
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(temView, "y", Hash.imageLocation[prev][1] - imageBorder[1], Hash.imageLocation[now][1] - imageBorder[1]);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(temView, "x", Hash.imageLocation[prev][0], Hash.imageLocation[now][0]);
        anim2.setDuration(animTime * time);
        anim1.setDuration(animTime * time);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(anim1).with(anim2);
        animatorSet.start();
        setNewPiece(prev, 0);
        // temView.startAnimation(anim);
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    canvas.removeView(temView);
                    setNewPiece(now, num);
                    if (merge) {
                        startMergePieceAnimation(now, num*2);
                    }
                }
            }
        };
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123);
            }
        }, animTime * time);
    }

    private void checkGameOver() {
        int numBlank = countNumBlank();

        if (numBlank != 0) {
            LogUtil.d("Over", " not into over");
            return;
        }
        for (int i = 0; i < 16; ++i) {
            if (i % 4 != 3) {
                int tem = i+1;
                if (my2048Data.getNumbers()[i] == my2048Data.getNumbers()[tem]) {
                    return;
                }
            }
            if (i < 12) {
                int tem = i+4;
                if (my2048Data.getNumbers()[i] == my2048Data.getNumbers()[tem]) {
                    return;
                }
            }
        }
        LogUtil.d("Over", "into over");
        gameOver();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start :
                // backTimer =new TimeUtil(0,10);
                Hash.imageLocation = new int[16][2];
                for (int i = 0; i < 16; ++i) {
                    oriImage[i].getLocationOnScreen(Hash.imageLocation[i]);
                }
                imageBorder = new int[2];
                hoverLayout.getLocationOnScreen(imageBorder);
                imageWidth = oriImage[0].getWidth();
                imageHeight = oriImage[0].getHeight();

                initMainArea();
                btnPause.setBackgroundResource(R.drawable.game_pause);
                hoverLayout.setVisibility(View.INVISIBLE);
                // handler for timer
                timerHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 0x666) {
                            if (gameType == MODE_NORMAL) {
                                my2048Data.tickTime(1);
                                scoreTimeTxt.setText(my2048Data.getTime().toString());
                            } else if(gameType == MODE_COUNTDOWN) {
                                // my2048Data.setTime(backTimer);
                                my2048Data.tickTime(-1);
                                scoreTimeTxt.setText(my2048Data.getTime().toString());
                                if(my2048Data.getTime().isNil()) {
                                    gameOver();
                                }
                            }
                        }
                    }
                };
                isPause = false;
                startTimer();
                break;
            case R.id.game_btn_pause :
                // if (GameType==1)
                saveDataForPause();
                hoverLayout.setVisibility(View.VISIBLE);
                stopTimer();
                btnPause.setBackgroundResource(R.drawable.game_resume);
                break;
//            case R.id.game_btn_menu :
//                // if(GameType==1)
//                saveDataForPause();
//                // Intent intent = new Intent(GameActivity.this, HomeActivity.class);
//                // startActivity(intent);
//                finish();
//               // onStop();
//                break;
            default:
                break;

        }
    }
    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    while (isPause) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!isPause) timerHandler.sendEmptyMessage(0x666);
                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, 0, 1000);

    }

    private void stopTimer(){

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }
    private void initMainArea() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        maxScore = sharedPreferences.getInt("max_score", 0);
        // LogUtil.d("Init", Integer.toString(maxScore));
        // scoreHighestTxt.setText(Integer.toString(maxScore));
        // id = sharedPreferences.getInt("id", 0);
        canResume = sharedPreferences.getBoolean("can_resume", false);
        // isCountdown = sharedPreferences.getBoolean("is_countdown", false);

        if (canResume && gameType == MODE_NORMAL) {
            List<My2048Data> list = my2048DB.queryData(0,0);
            my2048Data = list.get(0);
            LogUtil.d("StartNew",  Integer.toString(list.size()));
        } else {
            my2048Data = new My2048Data();
            if (gameType == MODE_COUNTDOWN) {
                my2048Data.setTime(new TimeUtil(0, 10));
                maxScore = sharedPreferences.getInt("max_score_countdown", 0);
            } else if (gameType == MODE_NORMAL) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean("can_resume", true);
                editor.commit();
            }
            LogUtil.d("StartNew", "Yes");
        }
        if (my2048Data.getScore() > maxScore) {
            maxScore = my2048Data.getScore();
        }
        scoreHighestTxt.setText(Integer.toString(maxScore));
        scoreScoreTxt.setText(Integer.toString(my2048Data.getScore()));
        scoreStepTxt.setText(Integer.toString(my2048Data.getStep()));
        scoreTimeTxt.setText(my2048Data.getTime().toString());


        for (int i = 0; i < 16; ++i) {
            // setNewImageView(i, my2048Data.getNumbers()[i]);
            if (my2048Data.getNumbers()[i] != 0) {
                startNewPieceAnimation(i, my2048Data.getNumbers()[i]);
            } else {
                setNewPiece(i, my2048Data.getNumbers()[i]);
            }
            // oriImage[i].setImageResource(Hash.BLOCK_RESOURCE[Hash.blockHash(my2048Data.getNumbers()[i])]);
        }
        // only when starting a new game, set a new piece
        if (my2048Data.getScore() == 0) {
            setRandomNewPiece();
        }

    }

    // generate new image view on canvas
    private ImageView setNewImageView(int position, int number) {
        ImageView temImage = new ImageView(this);
        temImage.setImageResource(Hash.BLOCK_RESOURCE[Hash.blockHash(number)]);
        temImage.setX(Hash.imageLocation[position][0] - imageBorder[0]);
        temImage.setY(Hash.imageLocation[position][1] - imageBorder[1]);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(imageWidth, imageHeight);
        canvas.addView(temImage, lp);
        return temImage;
    }
    private void setRandomNewPiece() {
        Random random = new Random();
        int numBlank = countNumBlank();
        if (numBlank == 0) {
            // gameOver();
            return;
        }
        int r = random.nextInt(numBlank);
        int p = 0;
        for (int i = 0; i < 16; ++i) {
            if (my2048Data.getNumbers()[i] != 0) continue;
            if (p == r) {
                startNewPieceAnimation(i, 2);
                // setNewPiece(i, 2);
                break;
            }
            ++p;
        }

    }
    private void gameOver() {



        hoverLayout.setVisibility(View.VISIBLE);
        stopTimer();
        if (gameType == MODE_NORMAL) {
            btnStart.setText("DEAD!");
            List<My2048Data> list = my2048DB.queryData(1,10);
            if (list.size() < 10) {
                my2048Data.setId(list.size()+1);
                my2048DB.insertData(my2048Data);
            } else {
                My2048Data tem = list.get(9);
                if (my2048Data.getScore() > tem.getScore()) {
                    my2048Data.setId(tem.getId());
                    my2048DB.updateData(tem.getId(), my2048Data);
                }
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putInt("max_score", maxScore);
            editor.putBoolean("can_resume", false);
            editor.commit();

        } else { // COUNTDOWN MODE
            btnStart.setText("TIME UP");
            List<My2048Data> list = my2048DB.queryData(11,20);
            if (list.size() < 10) {
                my2048Data.setId(list.size()+11);
                my2048DB.insertData(my2048Data);
            } else {
                My2048Data tem = list.get(9);
                if (my2048Data.getScore() > tem.getScore()) {
                    my2048Data.setId(tem.getId());
                    my2048DB.updateData(tem.getId(), my2048Data);
                }
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putInt("max_score_countdown", maxScore);
            // editor.putBoolean("can_resume", false);
            editor.commit();

        }
        // show dialog
        LinearLayout submit = (LinearLayout) getLayoutInflater().inflate(R.layout.submit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);

        builder.setTitle("你想上传你的分数吗？");
        builder.setView(submit);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // NOTHING
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        submitName = (EditText) alertDialog.findViewById(R.id.submit_name);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // upload score to server
                String username = submitName.getText().toString();
                LogUtil.d("submit", username);
                if (username.trim().equals("")) {
                    Toast.makeText(getLayoutInflater().getContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    // upload score to server
                    NetworkUtil.postRequest(gameType, my2048Data.getScore(), username, getLayoutInflater().getContext());
                    // NetworkUtil.getRequest(gameType, getLayoutInflater().getContext());
                    alertDialog.cancel();
                }
            }
        });

    }
    // change the original image blocks into new ones
    private void setNewPiece(int position, int number) {
        my2048Data.setNumber(position, number);
        oriImage[position].setImageResource(Hash.BLOCK_RESOURCE[Hash.blockHash(number)]);
    }

    private int countNumBlank() {
        int numBlank = 0;
        for (int i = 0; i < 16; ++i) {
            if (my2048Data.getNumbers()[i] == 0) {
                ++numBlank;
            }
        }
        return numBlank;
    }
}
