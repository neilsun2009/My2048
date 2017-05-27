package my2048.com.my2048.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import my2048.com.my2048.R;
import my2048.com.my2048.activity.NetRankActivity;
import my2048.com.my2048.model.My2048Data;

/**
 * Created by Administrator on 2017/5/19.
 */

public class NetworkUtil {
    static private Context context;
    static private final int SUBMIT_SUCCEED = 0x111;
    static private final int SUBMIT_FAIL = 0x222;
    static private final int GET_SUCCEED = 0x333;
    static private final int GET_FAIL = 0x444;
    static private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUBMIT_SUCCEED:
                    Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
                    break;
                case SUBMIT_FAIL:
                    Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show();
                    break;
                case GET_SUCCEED:
                    List<My2048Data> list = new ArrayList<My2048Data>();
                    try {
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        JSONArray resultJsonArray = jsonObject.getJSONArray("scores");
                        for (int i = 0; i < resultJsonArray.length(); i++) {
                            My2048Data data = new My2048Data();
                            data.setUsername(resultJsonArray.getJSONObject(i).getString("username"));
                            data.setScore(resultJsonArray.getJSONObject(i).getInt("score"));
                            list.add(data);
                        }
                        NetRankActivity.my2048DataList = list;
                        NetRankActivity.dataList.clear();
                        for (int i = 0; i < NetRankActivity.my2048DataList.size(); i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("rank", i+1);
                            map.put("name", NetRankActivity.my2048DataList.get(i).getUsername());
                            map.put("points", NetRankActivity.my2048DataList.get(i).getScore());
                            NetRankActivity.dataList.add(map);
                        }
                        NetRankActivity.adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                    }
                    break;
                case GET_FAIL:
                    Toast.makeText(context, "获取排行榜数据失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    static final String SERVER = "http://zhangwx.cn/score";
    static private boolean checkInternet() {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            boolean network = networkInfo.isAvailable();
            if (!network) {
                Toast.makeText(context, "当前没有可用网络", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                //Toast.makeText(MainActivity.this, "当前有可用网络！", Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            Toast.makeText(context, "当前没有可用网络", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    static public void postRequest(final int mode, final int score, final String username, final Context mContext) {
        context = mContext;
        if (checkInternet()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        // build jSON
                        JSONObject json = new JSONObject();
                        json.put("mode", mode);
                        json.put("username", username);
                        json.put("score", score);
                        URL url = new URL(SERVER);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        connection.setDoOutput(true);
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        // DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                        // out.writeBytes(json.toString());
                        OutputStream os = connection.getOutputStream();
                        os.write(json.toString().getBytes());
                        os.flush();
                        LogUtil.d("network", "success " + json.toString());
                        connection.connect();
                        if (connection.getResponseCode() != 200) {
                            throw new RuntimeException("Failed : HTTP error code : "
                                    + connection.getResponseCode());
                        }
                        Message message = new Message();
                        message.what = SUBMIT_SUCCEED;
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.d("network", "fail " + e.toString());
                        Message message = new Message();
                        message.what = SUBMIT_FAIL;
                        handler.sendMessage(message);
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }

                }
            }).start();
        }

    }

    static public void getRequest(final int mode, final Context mContext) {
        context = mContext;
        if (checkInternet()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(SERVER + "?mode=" + mode);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        // DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                        // out.writeBytes(json.toString());
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        LogUtil.d("network", "success " + response.toString());
                        connection.connect();
                        if (connection.getResponseCode() != 200) {
                            throw new RuntimeException("Failed : HTTP error code : "
                                    + connection.getResponseCode());
                        }
                        Message message = new Message();
                        message.what = GET_SUCCEED;
                        message.obj = response.toString();
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.d("network", "fail " + e.toString());
                        Message message = new Message();
                        message.what = GET_FAIL;
                        handler.sendMessage(message);
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }

                }
            }).start();
        }

    }
}
