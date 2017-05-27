package my2048.com.my2048.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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

public class LocalRankActivity extends AppCompatActivity {

    public List<My2048Data> my2048DataList;
    private List<Map<String,Object> > dataList = new ArrayList<Map<String,Object> >();

    private Button netRankButton;
    private Button timeModelButton;
    private Button normalModelButton;
    private ListView listView;
    private My2048DB my2048DB;
    private SimpleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_rank);

        netRankButton = (Button) findViewById(R.id.netRankButton);
        timeModelButton = (Button) findViewById(R.id.timeModelButton);
        normalModelButton = (Button) findViewById(R.id.normalModelButton);
        listView = (ListView) findViewById(R.id.rankList);
        my2048DB = My2048DB.getInstance(this);
        my2048DataList = my2048DB.queryData(1,10);
        getData();
        netRankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LocalRankActivity.this, NetRankActivity.class);
                LocalRankActivity.this.startActivity(intent);
                finish();
            }
        });

        normalModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                my2048DataList = my2048DB.queryData(1,10);
                getData();
                adapter.notifyDataSetChanged();
                timeModelButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
                normalModelButton.setBackgroundColor(Color.parseColor("#E1F5FE"));
            }
        });

        timeModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                my2048DataList = my2048DB.queryData(11,20);
                getData();
                adapter.notifyDataSetChanged();
                timeModelButton.setBackgroundColor(Color.parseColor("#e1f5fe"));
                normalModelButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        });
        adapter  = new SimpleAdapter(this,
                dataList,
                R.layout.listtemp,
                new String[]{"rank","name","points"},
                new int[]{R.id.rank,R.id.name,R.id.points}
        );
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void getData() {
        dataList.clear();
        for (int i = 0; i < my2048DataList.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("rank", i+1);
            map.put("name", my2048DataList.get(i).getUsername());
            map.put("points", my2048DataList.get(i).getScore());
            dataList.add(map);
        }
        Log.v("debug",String.valueOf(dataList.size()));
        return ;
    }
}
