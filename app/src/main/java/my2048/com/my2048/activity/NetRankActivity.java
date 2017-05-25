package my2048.com.my2048.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my2048.com.my2048.R;
import my2048.com.my2048.db.My2048DB;
import my2048.com.my2048.model.My2048Data;
import my2048.com.my2048.utility.NetworkUtil;

public class NetRankActivity extends AppCompatActivity {

    public static List<My2048Data> my2048DataList;
    public static List<Map<String,Object> > dataList = new ArrayList<Map<String,Object> >();
    public static SimpleAdapter adapter;

    private Button localRankButton;
    private Button timeModelButton;
    private Button normalModelButton;
    private ListView listView;
    private My2048DB my2048DB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_rank);
        my2048DataList = new ArrayList<My2048Data>();
        localRankButton = (Button) findViewById(R.id.localRankButton);
        timeModelButton = (Button) findViewById(R.id.timeModelButton);
        normalModelButton = (Button) findViewById(R.id.normalModelButton);
        listView = (ListView) findViewById(R.id.rankList);
        my2048DB = My2048DB.getInstance(this);
        NetworkUtil.getRequest(0, getLayoutInflater().getContext());
        localRankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(NetRankActivity.this, LocalRankActivity.class);
                NetRankActivity.this.startActivity(intent);
                finish();
            }
        });

        normalModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkUtil.getRequest(0, getLayoutInflater().getContext());
                timeModelButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
                normalModelButton.setBackgroundColor(Color.parseColor("#E1F5FE"));
            }
        });

        timeModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkUtil.getRequest(1, getLayoutInflater().getContext());
                timeModelButton.setBackgroundColor(Color.parseColor("#e1f5fe"));
                normalModelButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        });
        adapter = new SimpleAdapter(this, dataList, R.layout.listtemp,
                new String[]{"rank","name","points"},
                new int[]{R.id.rank,R.id.name,R.id.points});
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
