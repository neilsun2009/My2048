package my2048.com.my2048.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lenovo on 2016/3/13.
 */
public class My2048OpenHelper extends SQLiteOpenHelper {

    public static final String CREATE_DATA = "create table Data ("
            + "id integer primary key,"
            + "numbers text,"
            + "time text,"
            + "score int,"
            + "step int)";

    public My2048OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
