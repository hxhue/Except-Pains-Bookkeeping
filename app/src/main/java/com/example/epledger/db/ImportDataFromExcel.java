package com.example.epledger.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;
import java.io.File;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import jxl.Sheet;
//import jxl.Workbook;

/**
 */
public class ImportDataFromExcel {
    public MySQLiteOpenHelper dbHelper;

    private SharedPreferences sharedPreferences;
    //@Override
    public ImportDataFromExcel() {
        //super.onCreate(savedInstanceState);
        Context c=MainApplication.getCustomApplicationContext();
        dbHelper  = new MySQLiteOpenHelper(c,"test");
        /*SQLiteDatabase sqLiteDatabase=dbHelper.getWritableDatabase();
        AddNewType(sqLiteDatabase,"吃饭");
        AddNewType(sqLiteDatabase,"娱乐");
        AddNewFrom(sqLiteDatabase,"支付宝");
        AddNewFrom(sqLiteDatabase,"微信");
        sqLiteDatabase.close();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues contentValues=getContentValues("2021/5/21",100,1,1,"没有备注");
        db.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues);
        contentValues=getContentValues("2021/5/22",100,2,1,"备注2");
        db.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues);
        db.close();
        ArrayList<Integer>a=new ArrayList<Integer>();
        a.add(1);
        a.add(2);
        List<bill>tmp=FindTimeFrom("2021/5/21","2021/5/22",a,a);
        for(int i=0;i<tmp.size();i++) System.out.println(tmp.get(i).id);*/
    }
    public void StoreRecord(SQLiteDatabase db,bill b)
    {
        /*ContentValues contentValues=getContentValues(b.date1,b.account,b.type1,b.from1,b.beizhu);
        db.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues);*/
    }
    public void AddNewType(SQLiteDatabase db,String type)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySQLiteOpenHelper.type1, type);
        db.insert(MySQLiteOpenHelper.TABLE_NAME2, null, contentValues);
    }
    public void AddNewFrom(SQLiteDatabase db,String from1)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySQLiteOpenHelper.from1, from1);
        db.insert(MySQLiteOpenHelper.TABLE_NAME5, null, contentValues);
    }
    public List<bill> FindTimeFrom(String start, String end, ArrayList<Integer> from, ArrayList<Integer>type)
    {

        SQLiteDatabase sqLiteDatabase=dbHelper.getReadableDatabase();
        List<bill> sum=new ArrayList<>();
        for(int i=0;i<from.size();i++)
        {
            for(int j=0;j<type.size();j++)
            {
                List<bill> h=query_date(sqLiteDatabase,start,end,from.get(i),type.get(j));
                if(h!=null) sum.addAll(h);
            }
        }
        sqLiteDatabase.close();
        return sum;
    }
    public void base_excel()
    {
        java.util.List<bill> b = query(dbHelper.getReadableDatabase());
        HSSFWorkbook mWorkbook = new HSSFWorkbook();
        HSSFSheet mSheet = mWorkbook.createSheet(MySQLiteOpenHelper.TABLE_NAME);
        createExcelHead(mSheet);
        SQLiteDatabase sqLiteDatabase=dbHelper.getReadableDatabase();
        for (bill t : b) {
            //System.out.println(student.id + "," + student.name + "," + student.gender + "," + student.age)
           String m=get_id_type(t.id,sqLiteDatabase);
            createCell(t.id,t.date1, t.account,m, t.from1, t.beizhu,mSheet);
        }

        File xlsFile = new File(Environment.getExternalStorageDirectory(), "excel.xls");
        try {
            if (!xlsFile.exists()) {
                xlsFile.createNewFile();
            }
            mWorkbook.write(xlsFile);// 或者以流的形式写入文件 mWorkbook.write(new FileOutputStream(xlsFile));
            mWorkbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sqLiteDatabase.close();
    }
    public void alia_base(String s) throws IOException {
        SQLiteDatabase sqLiteDatabase=dbHelper.getWritableDatabase();
        s=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+s;
        System.out.println(s);
        File xlsFile = new File(s);

        // 工作表
        Workbook workbook = WorkbookFactory.create(xlsFile);

        // 表个数。
        int numberOfSheets = workbook.getNumberOfSheets();

        // 遍历表。
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);

            // 行数。
            int rowNumbers = sheet.getLastRowNum() + 1;

            // Excel第一行。

            // 读数据。
            for (int row = 0; row < rowNumbers; row++) {
                Row r = sheet.getRow(row);
                if(r.getPhysicalNumberOfCells()!=16)
                    continue;
                int m=0;
                try {
                    m= Integer.parseInt(r.getCell(9).toString());
                }catch(NumberFormatException e)
                {
                    e.printStackTrace();
                }
                if(r.getCell(10)!=null)
                {
                    if(r.getCell(10).toString()=="支出")
                    {
                        ContentValues contentValues1 = getContentValues(r.getCell(3).toString(),-m, -1,SelectFromId(r.getCell(7).toString(),sqLiteDatabase),null);
                        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);
                    }
                    else
                    {
                        ContentValues contentValues1 = getContentValues(r.getCell(3).toString(),m, -1,SelectFromId(r.getCell(7).toString(),sqLiteDatabase),null);
                        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);

                    }
                }
                }

            sqLiteDatabase.close();


            }
        }
    public void weixin_base(String s) throws IOException {
        SQLiteDatabase sqLiteDatabase=dbHelper.getWritableDatabase();
        File xlsFile = new File(s);

        // 工作表
        Workbook workbook = WorkbookFactory.create(xlsFile);

        // 表个数。
        int numberOfSheets = workbook.getNumberOfSheets();

        // 遍历表。
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);

            // 行数。
            int rowNumbers = sheet.getLastRowNum() + 1;

            // Excel第一行。

            // 读数据。
            for (int row = 0; row < rowNumbers; row++) {
                Row r = sheet.getRow(row);
                if(r.getPhysicalNumberOfCells()!=11)
                    continue;
                int m=0;
                try {
                    m=Integer.valueOf( r.getCell(5).toString()).intValue();
                }catch(NumberFormatException e)
                {
                    e.printStackTrace();
                }
                if(r.getCell(4)!=null)
                {
                    if(r.getCell(4).toString()=="支出")
                    {
                        ContentValues contentValues1 = getContentValues(r.getCell(0).toString(),-m, -1,SelectFromId(r.getCell(2).toString(),sqLiteDatabase),null);
                        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);
                    }
                    else
                    {
                        ContentValues contentValues1 = getContentValues(r.getCell(0).toString(),-m, -1,SelectFromId(r.getCell(2).toString(),sqLiteDatabase),null);
                        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);

                    }
                }
            }

            sqLiteDatabase.close();


        }
    }



    private ContentValues getContentValues(String date1, int account, int type_id,int from_id,String beizhu) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySQLiteOpenHelper.date1, date1);
        contentValues.put(MySQLiteOpenHelper.account,account);
        if(type_id!=-1) contentValues.put(MySQLiteOpenHelper.type_id, type_id);
        contentValues.put(MySQLiteOpenHelper.from_id, from_id);
        contentValues.put(MySQLiteOpenHelper.beizhu, beizhu);
        return contentValues;
    }

    private java.util.List<bill> query_date(SQLiteDatabase db, String start, String end, int from, int type) {
        java.util.List<bill> bills = null;

        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME+" WHERE "+MySQLiteOpenHelper.from_id+"= ? AND "+MySQLiteOpenHelper.type_id+"= ? AND "+MySQLiteOpenHelper.date1+" <= ? AND "+MySQLiteOpenHelper.date1+" >= ? AND istemplate=0",new String[]{Integer.toString(from), Integer.toString(type),end,start}, null);
        if (cursor != null && cursor.getCount() > 0) {

            bills = new ArrayList<>();

            while (cursor.moveToNext()) {
                bill b = new bill();
                b.id=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.record_id));
                b.date1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.date1));
                b.account = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.account));
                int type_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id));
                int from_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id));
                b.type1 = get_id_type(type_id,db);
                b.from1 = get_id_from(from_id,db);

                bills.add(b);
            }

            cursor.close();
        }

        return bills;
    }

    //注意，没有关db，所以调用以后一定要手动关闭,根据类型的id查找其对应的字符串
    public String get_id_type(int id,SQLiteDatabase db)
    {
        String tmp="";
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
        String tmp="";
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
    //查询SQLite数据库。读出所有数据内容。
    private java.util.List<bill> query(SQLiteDatabase db) {
        java.util.List<bill> bills = null;

        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME, null);
        if (cursor != null && cursor.getCount() > 0) {

            bills = new ArrayList<>();

            while (cursor.moveToNext()) {
                bill b = new bill();
                b.id=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.record_id));
                b.date1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.date1));
                b.account = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.account));
                int type_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.type_id));
                int from_id = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.from_id));
                b.type1 = get_id_type(type_id,db);
                b.from1 = get_id_from(from_id,db);
                bills.add(b);
            }

            cursor.close();
        }

        db.close();

        return bills;
    }

    //数据容器，装载从数据库中读出的数据内容。
    public class bill {
        public int id;
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
        headRow.createCell(5).setCellValue(MySQLiteOpenHelper.beizhu);
        //headRow.createCell(6).setCellValue(MySQLiteOpenHelper.beizhu);
    }

    // 创建Excel的一行数据。
    private static void createCell(int id2,String date1, int account,String type1, String from1, String beizhu,HSSFSheet sheet) {
        HSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
        dataRow.createCell(0).setCellValue(id2);
        dataRow.createCell(1).setCellValue(date1);
        dataRow.createCell(2).setCellValue(account);

        dataRow.createCell(3).setCellValue(type1);
        dataRow.createCell(4).setCellValue(from1);
        dataRow.createCell(5).setCellValue(beizhu);
    }
}
