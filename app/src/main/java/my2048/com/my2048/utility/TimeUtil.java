package my2048.com.my2048.utility;

import java.util.InputMismatchException;

/**
 * Created by lenovo on 2016/3/13.
 */
public class TimeUtil {
    private int minute;
    private int second;
    public TimeUtil() {
        minute = 0;
        second = 0;
    }
    public TimeUtil(int minute, int second) {
        this.minute = minute;
        this.second = second;
    }
    public TimeUtil(String s) {
        LogUtil.d("DB", s);
        String[] tem = s.split(":");
        LogUtil.d("DB", tem[0] + ' ' + tem[1]);
        minute = Integer.parseInt(tem[0]);
        second = Integer.parseInt(tem[1]);
    }
    public void tickForward() {
        ++second;
        if (second == 60) {
            minute++;
            second = 0;
        }
    }
    public boolean tickBackward() {
        --second;
        if (second == -1) {
            --minute;
            second = 0;
            if (minute == -1) {
                return false;
            }
        }
        return true;
    }
    public String toString(){
        String sec = String.format("%2d", second).replace(" ", "0");
        return minute + ":" + sec;
    }
}
