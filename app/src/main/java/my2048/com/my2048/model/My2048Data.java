package my2048.com.my2048.model;

import my2048.com.my2048.utility.TimeUtil;

import java.sql.Time;
import java.util.Timer;

/**
 * Created by lenovo on 2016/3/13.
 */
public class My2048Data {
    private int id;
    private int[] numbers;
    private TimeUtil time;
    private int step;
    private String username;
    private int score;

    public My2048Data() {
        id = 0;
        numbers = new int[16];
        for (int i = 0; i< 16; ++i)
            numbers[i] = 0;
        step = 0;
        score = 0;
        username = "me";
        time = new TimeUtil();
    }
    public String getUsername() {return username;}
    public void setUsername(String name){username = name;}
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int[] getNumbers() {
        return numbers;
    }
    public void setNumbers(int[] numbers) {
        this.numbers = numbers;
    }

    public void setNumber(int position, int number) {
        this.numbers[position] = number;
    }

    public TimeUtil getTime() {
        return time;
    }
    public void setTime(TimeUtil time) {
        this.time = time;
    }

    public void tickTime(int f) {
        if (f == 1) {
            time.tickForward();
        }
        else if(f == -1) {
            time.tickBackward();
        }
    }

    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
    }

    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }

}
