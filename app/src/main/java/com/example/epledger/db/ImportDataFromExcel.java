package com.example.epledger.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
//import android.support.v7.app.AppCompatActivity;
import com.example.epledger.R;
import com.example.epledger.model.Category;
import com.example.epledger.model.Record;
import com.example.epledger.model.Source;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;


import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


/**
 */
public class ImportDataFromExcel {
    public MySQLiteOpenHelper dbHelper;
    Context mContext;

    private SharedPreferences sharedPreferences;
    //@Override
    public ImportDataFromExcel(Context m) {
        mContext = m;
        initializeDatabase();
    }

    public void initializeDatabase() {
        Context c = mContext;
        dbHelper = new MySQLiteOpenHelper(c,"test");
        SQLiteDatabase db=dbHelper.getWritableDatabase();

        Category[] categoriesToAdd = new Category[] {
                new Category("Emergency", R.drawable.ic_fas_asterisk, 2),
                new Category("Study", R.drawable.ic_fas_pencil_alt,3),
                new Category("Food", R.drawable.ic_fas_utensils, 4),
                new Category("Shopping", R.drawable.ic_fas_shopping_cart, 5),
                new Category("Transportation", R.drawable.ic_fas_bus, 6),
                new Category("Digital", R.drawable.ic_fas_mobile_alt, 7),
                new Category("Coffee", R.drawable.ic_fas_coffee, 8),
                new Category("Present", R.drawable.ic_fas_gift, 9),
        };
        for (Category item: categoriesToAdd) {
            AddNewType(db, item.getName(), item.getIconResID());
        }

        Source[] sourcesToAdd = new Source[] {
                new Source("Alipay2", 1),
                new Source("Wechat", 2),
                new Source("Cash", 3),
        };
        for (Source item: sourcesToAdd) {
            AddNewFrom(db, item.getName());
        }

        db.close();
    }

    public void StoreRecord(SQLiteDatabase db, bill b)
    {
        /*ContentValues contentValues=getContentValues(b.date1,b.account,b.type1,b.from1,b.beizhu);
        db.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues);*/
    }
    public void AddNewType(SQLiteDatabase db,String type,int resID)
    {
        ContentValues contentValues = new ContentValues();
        int istype=SelectTypeId(type,db);
        if(istype==-1)
        {
            contentValues.put(MySQLiteOpenHelper.type1, type);
            contentValues.put(MySQLiteOpenHelper.iconresid, resID);
            db.insert(MySQLiteOpenHelper.TABLE_NAME2, null, contentValues);
        }

    }
    public void updateType(SQLiteDatabase db,String type,int resID,int type_id)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySQLiteOpenHelper.type1, type);
        contentValues.put(MySQLiteOpenHelper.iconresid, resID);
        db.update(MySQLiteOpenHelper.TABLE_NAME2, contentValues, "type_id=?",new String[]{Integer.toString(type_id)});
    }
    public void deleteType(SQLiteDatabase db,int type_id)
    {
        db.delete(MySQLiteOpenHelper.TABLE_NAME2, "type_id=?",new String[]{Integer.toString(type_id)});
    }
    public void deleteFrom(SQLiteDatabase db,int from_id)
    {
        db.delete(MySQLiteOpenHelper.TABLE_NAME5, "from_id=?",new String[]{Integer.toString(from_id)});
    }
    public void updateFrom(SQLiteDatabase db,String from,int from_id)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySQLiteOpenHelper.from1, from);
        //contentValues.put(MySQLiteOpenHelper.iconresid, resID);
        db.update(MySQLiteOpenHelper.TABLE_NAME5, contentValues, "from_id=?",new String[]{Integer.toString(from_id)});
    }

    public void AddNewFrom(SQLiteDatabase db,String from1)
    {
        ContentValues contentValues = new ContentValues();
        int isfrom=SelectFromId(from1,db);
        if(isfrom==-1)
        {
            contentValues.put(MySQLiteOpenHelper.from1, from1);
            db.insert(MySQLiteOpenHelper.TABLE_NAME5,  null, contentValues);
        }

    }
    public ArrayList<Record> FindTimeFrom(SQLiteDatabase sqLiteDatabase, String start, String end, List<String>s, List<String>c) throws ParseException {
        //SQLiteDatabase sqLiteDatabase=dbHelper.getReadableDatabase();
        ArrayList<Record> sum=new ArrayList<>();
        if(s.size()==0)
        {
            for(int j=0;j<c.size();j++)
            {
                ArrayList<Record> h=query_date(sqLiteDatabase,start,end,-1,SelectTypeId(c.get(j),sqLiteDatabase));
                if(h!=null) sum.addAll(h);
            }
            return sum;
        }
        for(int i=0;i<s.size();i++)
        {
            for(int j=0;j<c.size();j++)
            {
                ArrayList<Record> h=query_date(sqLiteDatabase,start,end,SelectFromId(s.get(i),sqLiteDatabase),SelectTypeId(c.get(j),sqLiteDatabase));
                if(h!=null) sum.addAll(h);
            }
        }
        sqLiteDatabase.close();
        return sum;
    }

    public void alia_base(String s) throws IOException, BiffException {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        SQLiteDatabase sqLiteDatabase=dbHelper.getWritableDatabase();
        //s=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+s;
        System.out.println(s);
        //InputStream is = mContext.getResources().getAssets().open(s);
        // 2
        FileInputStream fileInputStream = new FileInputStream(s);
        //一定要声明为GBK编码,因为默认编码为GBK
        Scanner in = new Scanner(fileInputStream, "GBK");
        //舍弃第一行
        in.nextLine();
        while (in.hasNextLine()) {
            String[] lines = in.nextLine().split(",");
            if(lines.length<16) continue;
            lines[0]=lines[0].replaceAll(" ","");
            if(lines[0].equals("交易号"))
            {
                continue;
            }
            System.out.println(lines[0]);
            System.out.println(lines[2]);
            double m=0;
            try {
                m= Double.parseDouble(lines[9]);
            }catch(NumberFormatException e)
            {
                e.printStackTrace();
                continue;
            }
            String date=lines[2].trim();
            date=date.substring(0,date.length()-3);
            date=date.replaceAll("-","/");
            System.out.println(date);
            String state=lines[10].trim();
            //System.out.println(state=="支出");
            if(state!=null)
            {
                if(state.equals("支出"))
                {
                    System.out.println(-m);
                    ContentValues contentValues1 = getContentValues(date,-m, -1,1,null,null,0);
                    sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);
                }
                else
                {
                    ContentValues contentValues1 = getContentValues(date,m,-1,1,null,null,0);
                    sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);

                }
            }
        }
        sqLiteDatabase.close();
    }
    public void weixin_base(String s) throws IOException {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        SQLiteDatabase sqLiteDatabase=dbHelper.getWritableDatabase();
        //s=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+s;
        System.out.println(s);
        //InputStream is = mContext.getResources().getAssets().open(s);
        // 2
        FileInputStream fileInputStream = new FileInputStream(s);
        //一定要声明为GBK编码,因为默认编码为GBK
        Scanner in = new Scanner(fileInputStream, "UTF-8");
        //舍弃第一行
        //in.nextLine();
        while (in.hasNextLine()) {
            String[] lines = in.nextLine().split(",");
            if(lines.length<11) continue;
            lines[0]=lines[0].trim();
            if(lines[0].equals("交易时间"))
            {
                continue;
            }
            String note=lines[10].trim();
            if(note.length()>2) note=note.substring(1,note.length()-1);
            if(note.equals("/")) note=null;
            System.out.println(lines[0]);
            System.out.println(lines[2]);
            double m=0;
            try {
                m= Double.parseDouble(lines[5].substring(1));
            }catch(NumberFormatException e)
            {
                e.printStackTrace();
                continue;
            }
            String date=lines[0].trim();
            date=date.substring(0,date.length()-3);
            date=date.replaceAll("-","/");
            System.out.println(date);
            date=simpleFormat.format(date);
            String state=lines[4].trim();
            //System.out.println(state=="支出");
            if(state!=null)
            {
                if(state.equals("支出"))
                {
                    System.out.println(-m);
                    ContentValues contentValues1 = getContentValues(date,-m, -1,2,note,null,0);
                    sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);
                }
                else
                {
                    ContentValues contentValues1 = getContentValues(date,m,-1,2,note,null,0);
                    sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);

                }
            }
        }
        sqLiteDatabase.close();
    }

    public void exportToCSV() throws IOException {

        int rowCount = 0;
        int colCount = 0;
        FileWriter fw;
        BufferedWriter bfw;
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME, null);
        rowCount=cursor.getCount();
        colCount = 7;
        //File sdCardDir = Environment.getExternalStorageDirectory();
        //File savefile = new File(sdCardDir+"/SqliteDatabase.csv");
        File savefile=new File("data/data/com.example.epledger/excel.csv");
        //System.out.println(sdCardDir+"/SqliteDatabase.csv");
        //File fileParent = savefile.getParentFile();
        if(!savefile.exists()){
            savefile.createNewFile();
        }

        //File saveFile = new File(sdCardDir, "SqliteDatabase.csv");
        //java.util.ArrayList<bill> b = query(dbHelper.getReadableDatabase());
            fw = new FileWriter(savefile);
            //bfw = new BufferedWriter(fw);
            bfw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savefile),"gbk"));
            //bfw.newLine();
            if (rowCount > 0){

                for (int i = 2; i < colCount; i++) {
                    if (i != colCount - 1)
                        bfw.write(cursor.getColumnName(i) + ',');
                    else
                        bfw.write(cursor.getColumnName(i));
                }

                bfw.newLine();

                for (int i = 0; i < rowCount; i++) {
                    cursor.moveToPosition(i);
                    Log.v("导出数据", "正在导出第" + (i + 1) + "条");
                    for (int j = 2; j < colCount; j++) {
                        String tmp=cursor.getString(j);
                        if(cursor.getString(j)==null) tmp="";
                        if (j != colCount - 1)
                            bfw.write(tmp + ',');
                        else
                            bfw.write(tmp);
                    }
                    bfw.newLine();
                }}

            bfw.flush();
// 释放缓存
            bfw.close();
// Toast.makeText(mContext, "导出完毕！", Toast.LENGTH_SHORT).show();
            Log.v("导出数据", "导出完毕！");
        }
    public void exportJson()
    {
        //SQLiteDatabase db=dbHelper.getWritableDatabase();
        //db.execSQL(".separator \",\"\n");

    }
    public ContentValues getContentValues(String date1, double account, int type_id, int from_id, String beizhu, String bitmap,int s) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySQLiteOpenHelper.date1, date1);
        contentValues.put(MySQLiteOpenHelper.account,account);
        if(type_id!=-1) contentValues.put(MySQLiteOpenHelper.type_id, type_id);
        contentValues.put(MySQLiteOpenHelper.from_id, from_id);
        contentValues.put(MySQLiteOpenHelper.memo, beizhu);
        contentValues.put(MySQLiteOpenHelper.bitmap, bitmap);
        contentValues.put(MySQLiteOpenHelper.star, s);
        return contentValues;
    }


    private java.util.ArrayList<Record> query_date(SQLiteDatabase db, String start, String end, int from, int type) throws ParseException {
        java.util.ArrayList<Record> bills =new ArrayList<>();
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        android.database.Cursor cursor;
        if(from!=-1)  cursor = db.rawQuery("SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME+" WHERE "+MySQLiteOpenHelper.from_id+"= ? AND "+MySQLiteOpenHelper.type_id+"= ? AND "+MySQLiteOpenHelper.date1+" <= ? AND "+MySQLiteOpenHelper.date1+" >= ? AND istemplate=0",new String[]{Integer.toString(from), Integer.toString(type),end,start}, null);
        else  cursor= db.rawQuery("SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME+" WHERE "+MySQLiteOpenHelper.type_id+"= ? AND "+MySQLiteOpenHelper.date1+" <= ? AND "+MySQLiteOpenHelper.date1+" >= ? AND istemplate=0",new String[]{Integer.toString(type),end,start}, null);
        if (cursor != null && cursor.getCount() > 0) {


            while (cursor.moveToNext()) {
                Record b = new Record();
                b.setId(cursor.getLong(cursor.getColumnIndex(MySQLiteOpenHelper.record_id)));
                Date tmp=simpleFormat.parse(cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.date1)));
                b.setDate(tmp);
                b.setMoney(cursor.getDouble(cursor.getColumnIndex(MySQLiteOpenHelper.account)));
                int type_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id));
                int from_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id));
//                b.setSource( get_id_from(from_id,db));
//                b.setCategory(get_id_type(type_id,db));

                // 2021-06-02 18:08:45
                // Now these fields are of type (Int?)
                b.setSourceID(from_id);
                b.setCategoryID(type_id);

                b.setNote(cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.memo)));
                int isstar=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.star));
                b.setStarred(isstar==1);
                b.setScreenshotPath(cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.bitmap)));
                bills.add(b);
            }

            cursor.close();
        }

        return bills;
    }

    //注意，没有关db，所以调用以后一定要手动关闭,根据类型的id查找其对应的字符串
    public String get_id_type(int id,SQLiteDatabase db)
    {
        String tmp=null;
        android.database.Cursor cursor = db.rawQuery("SELECT type FROM " + MySQLiteOpenHelper.TABLE_NAME2+" WHERE type_id=? ", new String[]{Integer.toString(id)},null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                tmp=cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.type1));
            }

            cursor.close();
        }
        return tmp;
    }

    public String get_id_from(int id,SQLiteDatabase db)
    {
        String tmp=null;
        android.database.Cursor cursor = db.rawQuery("SELECT from1 FROM " + MySQLiteOpenHelper.TABLE_NAME5+" WHERE from_id=? ", new String[]{Integer.toString(id)},null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                tmp=cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.from1));
            }

            cursor.close();
        }
        return tmp;
    }

    //与上述两函数功能相反，给定类型和from的字符串，返回它们的id
    public int SelectTypeId(String type,SQLiteDatabase db)
    {
        int res=-1;
        android.database.Cursor cursor = db.rawQuery("SELECT type_id FROM " + MySQLiteOpenHelper.TABLE_NAME2+" WHERE type=? ", new String[]{type},null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                res=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id));
            }

            cursor.close();
        }
        return res;
    }
    public int SelectFromId(String from1,SQLiteDatabase db)
    {
        int res=-1;
        android.database.Cursor cursor = db.rawQuery("SELECT from_id FROM " + MySQLiteOpenHelper.TABLE_NAME5+" WHERE from1=? ", new String[]{from1},null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                res=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id));
            }

            cursor.close();
        }
        return res;
    }
    public Long FindRecordID(int fromid,int typeid,double account,SQLiteDatabase db)
    {
        Long res= Long.valueOf(-1);
        String a=""+account;
        android.database.Cursor cursor = db.rawQuery("SELECT record_id FROM " + MySQLiteOpenHelper.TABLE_NAME+" WHERE from_id=? AND type_id=? AND account=?  ", new String[]{Integer.toString(fromid),Integer.toString(typeid),a},null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                res=cursor.getLong(cursor.getColumnIndex(MySQLiteOpenHelper.record_id));
            }

            cursor.close();
        }
        return res;
    }
    public  void DeleteID(Long rid,SQLiteDatabase db)
    {
        int m=db.delete(MySQLiteOpenHelper.TABLE_NAME,"record_id=?",new String[]{Long.toString(rid)});
        System.out.println("我被删除了");
        System.out.println(m);
    }
    public void Update(Long rid,ContentValues c,SQLiteDatabase db)
    {
        int m=db.update(MySQLiteOpenHelper.TABLE_NAME,c, "record_id=?",new String[]{Long.toString(rid)});
        System.out.println("我被更新了");
        System.out.println(m);
    }
    //查询SQLite数据库。读出所有数据内容。
    private java.util.ArrayList<bill> query(SQLiteDatabase db) {
        java.util.ArrayList<bill> bills = null;

        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME, null);
        if (cursor != null && cursor.getCount() > 0) {

            bills = new ArrayList<>();

            while (cursor.moveToNext()) {
                bill b = new bill();
                b.id=cursor.getLong(cursor.getColumnIndex(MySQLiteOpenHelper.record_id));
                b.date1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.date1));
                b.account = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.account));
                int type_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id));
                int from_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id));
                b.type1 = get_id_type(type_id,db);
                b.from1 = get_id_from(from_id,db);
                if(b.type1==null) continue;
                bills.add(b);
            }

            cursor.close();
        }

        db.close();

        return bills;
    }

    //数据容器，装载从数据库中读出的数据内容。
    public class bill {
        public Long id;
        public String date1;
        public int account;
        public String type1;
        public String from1;
        public String beizhu;
        public int star;
        public String bitmap;
        public int iconresid;
    }

    // 创建Excel标题行，第一行。
    private void createExcelHead(HSSFSheet mSheet) {
        HSSFRow headRow = mSheet.createRow(0);
        headRow.createCell(0).setCellValue(MySQLiteOpenHelper.record_id);
        headRow.createCell(1).setCellValue(MySQLiteOpenHelper.date1);
        headRow.createCell(2).setCellValue(MySQLiteOpenHelper.account);
        headRow.createCell(3).setCellValue(MySQLiteOpenHelper.type1);
        headRow.createCell(4).setCellValue(MySQLiteOpenHelper.from1);
        headRow.createCell(5).setCellValue(MySQLiteOpenHelper.memo);
        //headRow.createCell(6).setCellValue(MySQLiteOpenHelper.beizhu);
    }

    // 创建Excel的一行数据。
    private static void createCell(Long id2,String date1, int account,String type1, String from1, String beizhu,HSSFSheet sheet) {
        HSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
        dataRow.createCell(0).setCellValue(id2);
        dataRow.createCell(1).setCellValue(date1);
        dataRow.createCell(2).setCellValue(account);

        dataRow.createCell(3).setCellValue(type1);
        dataRow.createCell(4).setCellValue(from1);
        dataRow.createCell(5).setCellValue(beizhu);
    }
}
