package my2048.com.my2048.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import my2048.com.my2048.model.My2048Data;
import my2048.com.my2048.utility.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/3/13.
 */
public class My2048DB {
    public static final String DB_NAME = "my_2048";
    public static final int VERSION = 1;
    private static My2048DB my2048DB;
    private SQLiteDatabase db;

    private My2048DB(Context context) {
        My2048OpenHelper dbHelper = new My2048OpenHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    public synchronized static My2048DB getInstance(Context context) {
        if (my2048DB == null) {
            my2048DB = new My2048DB(context);
        }
        return my2048DB;
    }

    public void insertData(My2048Data my2048Data) {
        if (my2048Data != null) {
            ContentValues values = new ContentValues();
            values.put("score", my2048Data.getScore());
            values.put("step", my2048Data.getStep());
            values.put("id", my2048Data.getId());
            values.put("time", my2048Data.getTime().toString());
            StringBuilder stringBuilder = new StringBuilder();
            int[] numbers = my2048Data.getNumbers();
            stringBuilder.append(numbers[0]);
            for (int i = 0; i < 16; ++i) {
                stringBuilder.append(","+my2048Data.getNumbers()[i]);
            }
            values.put("numbers", stringBuilder.toString());
            db.insert("Data", null, values);
        }
    }

    //usage:
    //normal mode:1-10, countdown mode:11-20
    //0 for current (only in normal mode)
    public List<My2048Data> queryData(int lowerId, int upperId) {
        // db.delete("data", null, null);
        List<My2048Data> list = new ArrayList<My2048Data>();
        Cursor cursor = db.query("Data", null, "id >= ? AND id <= ?",
                new String[] {Integer.toString(lowerId), Integer.toString(upperId)}, null, null, "score desc");
        if (cursor.moveToFirst()) {
            do {
                My2048Data my2048Data = new My2048Data();
                my2048Data.setId(cursor.getInt(cursor.getColumnIndex("id")));
                my2048Data.setScore(cursor.getInt(cursor.getColumnIndex("score")));
                my2048Data.setStep(cursor.getInt(cursor.getColumnIndex("step")));
                my2048Data.setTime(new TimeUtil(cursor.getString(cursor.getColumnIndex("time"))));
                String numbers = cursor.getString(cursor.getColumnIndex("numbers"));
                String[] tem = numbers.split(",");
                int[] intTem = new int[16];
                for (int i = 0; i < 16; ++i) {
                    intTem[i] = Integer.valueOf(tem[i]);
                }
                my2048Data.setNumbers(intTem);
                list.add(my2048Data);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public void updateData(int id, My2048Data my2048Data) {
        ContentValues values = new ContentValues();
        values.put("score", my2048Data.getScore());
        values.put("step", my2048Data.getStep());
        values.put("time", my2048Data.getTime().toString());
        StringBuilder stringBuilder = new StringBuilder();
        int[] numbers = my2048Data.getNumbers();
        stringBuilder.append(numbers[0]);
        for (int i = 1; i < 16; ++i) {
            stringBuilder.append(","+my2048Data.getNumbers()[i]);
        }
        values.put("numbers", stringBuilder.toString());
        db.update("Data", values, "id = ?", new String[] {String.valueOf(id)});
    }
}
