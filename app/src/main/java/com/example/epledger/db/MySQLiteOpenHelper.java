/*package com.example.myapplication2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

//import android.support.annotation.Nullable;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    //数据库名称。
    public static final String DATABASE_NAME = "test.db";

    //数据库版本号。
    public static int DATABASE_VERSION = 1;

    private static MySQLiteOpenHelper helper;

    //表名。
    public static final String TABLE_NAME = "exceldata";

    public static final String STUDENT_ID = "id";
    public static final String STUDENT_NAME = "name";
    public static final String STUDENT_GENDER = "gender";
    public static final String STUDENT_AGE = "age";

    //创建数据库表的SQL语句。
    private String sql_create_table = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + STUDENT_ID + " integer primary key autoincrement," + STUDENT_NAME + " varchar(60)," + STUDENT_GENDER + " varchar(1)," + STUDENT_AGE + " int)";

    public static MySQLiteOpenHelper getInstance(ImportDataFromExcel context) {
        if (helper == null) {
            helper = new MySQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        return helper;
    }

    public MySQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据库的表，如果不存在。
        db.execSQL(sql_create_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}*/
package com.example.epledger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private String sql_create_table = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +istemplate+" int DEFAULT 0, "+record_id+" INTEGER PRIMARY KEY AUTOINCREMENT,"+date1 + " date," + account + " int," + type_id + " int," + from_id + " int,"+ memo + " varchar(300), " + star + " int DEFAULT 0, "+ bitmap + " varchar(300),"+iconresid+" int" +")";
    private String sql_create_type = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME2 + " (" +type_id+" INTEGER PRIMARY KEY AUTOINCREMENT,"+type1 + " varchar(200), " +iconresid+" int"+")";
    private String sql_create_from = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME5 + " (" +from_id+" INTEGER PRIMARY KEY AUTOINCREMENT,"+from1 + " varchar(200)" +")";
    private String sql_create_id_type = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME3 + " (" +record_id+" int ,"+type_id + " int" +")";
    private String sql_create_id_from = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME6 + " (" +from_id+" int ,"+record_id + " int" +")";
    private String sql_create_event_item= "CREATE TABLE IF NOT EXISTS " + TABLE_NAME4 + " (name varchar(300), starting_date  date, cycle int, cycle_unit varchar(200), record_id int, iconresid int )";
    public static final String date1= "date";
    public static final String account= "account";
    public static final String type1 = "type";
    //public static final String type1 = "type1";
    public static final String from1= "from1";
    public static final String memo = "beizhu";
    public static final String type_id= "type_id";
    public static final String record_id= "record_id";
    public static final String from_id= "from_id";
    public static final String star= "star";
    public static final String bitmap= "bitmap";
    public static final String istemplate= "istemplate";
    public static final String iconresid= "iconresid";



    //数据库版本号
    private static Integer Version = 1;
    public static final String DATABASE_NAME = "test.db";

    //数据库版本号。
    public static int DATABASE_VERSION = 1;

    private static MySQLiteOpenHelper helper;

    //表名。
    public static final String TABLE_NAME = "exceldata";
    public static final String TABLE_NAME2 = "typename";
    public static final String TABLE_NAME3 = "id_type";
    public static final String TABLE_NAME4 = "EventItem";
    public static final String TABLE_NAME5 = "fromname";
    public static final String TABLE_NAME6 ="id_from";
    //在SQLiteOpenHelper的子类当中，必须有该构造函数
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                              int version) {
        //必须通过super调用父类当中的构造函数
        super(context, name, factory, version);
    }
    //参数说明
    //context:上下文对象
    //name:数据库名称
    //param:factory
    //version:当前数据库的版本，值必须是整数并且是递增的状态

    public MySQLiteOpenHelper(Context context,String name,int version)
    {
        this(context,name,null,version);
    }


    public MySQLiteOpenHelper(Context context,String name)
    {
        this(context, name, Version);
    }



    //当数据库创建的时候被调用
    @Override
    public void onCreate(SQLiteDatabase db) {

        System.out.println("创建数据库和表");
        //创建了数据库并创建一个叫records的表
        //SQLite数据创建支持的数据类型： 整型数据，字符串类型，日期类型，二进制的数据类型

        //execSQL用于执行SQL语句
        //完成数据库的创建
        db.execSQL(sql_create_table);
        db.execSQL(sql_create_type);
        db.execSQL(sql_create_id_type);
        db.execSQL(sql_create_event_item);
        db.execSQL(sql_create_from);
        db.execSQL(sql_create_id_from);
        //数据库实际上是没有被创建或者打开的，直到getWritableDatabase() 或者 getReadableDatabase() 方法中的一个被调用时才会进行创建或者打开


    }

    //数据库升级时调用
    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade（）方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("更新数据库版本为:"+newVersion);
    }


}


