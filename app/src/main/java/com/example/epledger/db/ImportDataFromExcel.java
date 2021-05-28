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


    }
    public List<bill> find_date_from(String start,String end,String from,String type)
    {

        SQLiteDatabase sqLiteDatabase=dbHelper.getReadableDatabase();
        List<bill> h = query_date(dbHelper.getReadableDatabase(),start,end,from,type);
        return h;
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
            ArrayList<String>m=get_id_type(t.id,sqLiteDatabase);
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
                        ContentValues contentValues1 = getContentValues(r.getCell(3).toString(),-m, null,r.getCell(7).toString(),null);
                        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);
                    }
                    else
                    {
                        ContentValues contentValues1 = getContentValues(r.getCell(3).toString(),m, null,r.getCell(7).toString(),null);
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
                        ContentValues contentValues1 = getContentValues(r.getCell(0).toString(),-m, null,r.getCell(2).toString(),null);
                        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);
                    }
                    else
                    {
                        ContentValues contentValues1 = getContentValues(r.getCell(0).toString(),-m, null,r.getCell(2).toString(),null);
                        sqLiteDatabase.insert(MySQLiteOpenHelper.TABLE_NAME, null, contentValues1);

                    }
                }
            }

            sqLiteDatabase.close();


        }
    }



    private ContentValues getContentValues(String date1, int account, String type1,String from1,String beizhu) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MySQLiteOpenHelper.date1, date1);
        contentValues.put(MySQLiteOpenHelper.account,account);
        contentValues.put(MySQLiteOpenHelper.type1, type1);
        contentValues.put(MySQLiteOpenHelper.from1, from1);
        contentValues.put(MySQLiteOpenHelper.beizhu, beizhu);
        return contentValues;
    }

    private java.util.List<bill> query_date(SQLiteDatabase db, String start, String end, String from, String type) {
        java.util.List<bill> bills = null;

        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME+" WHERE "+MySQLiteOpenHelper.from1+"= ? AND "+MySQLiteOpenHelper.type1+"= ? AND "+MySQLiteOpenHelper.date1+" <= ? AND "+MySQLiteOpenHelper.date1+" >= ?",new String[]{from, type,end,start}, null);
        if (cursor != null && cursor.getCount() > 0) {

            bills = new ArrayList<>();

            while (cursor.moveToNext()) {
                bill b = new bill();
                b.id=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.id2));
                b.date1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.date1));
                b.account = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.account));
                b.type1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.type1));;
                b.from1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.from1));

                bills.add(b);
            }

            cursor.close();
        }

        db.close();

        return bills;
    }

    //注意，没有关db，所以调用以后一定要手动关闭
    public ArrayList<String> get_id_type(int id,SQLiteDatabase db)
    {
        ArrayList<String>tmp=new ArrayList<>();
        android.database.Cursor cursor = db.rawQuery("SELECT typename.type1 FROM " + MySQLiteOpenHelper.TABLE_NAME2+","+ MySQLiteOpenHelper.TABLE_NAME3+" WHERE id_type.id=? AND typename.type_id=id_type.type_id ", new String[]{Integer.toString(id)},null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                tmp.add(cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.type1)));
            }

            cursor.close();
        }
        return tmp;
    }
    //查询SQLite数据库。读出所有数据内容。
    private java.util.List<bill> query(SQLiteDatabase db) {
        java.util.List<bill> bills = null;

        android.database.Cursor cursor = db.rawQuery("SELECT * FROM " + MySQLiteOpenHelper.TABLE_NAME, null);
        if (cursor != null && cursor.getCount() > 0) {

            bills = new ArrayList<>();

            while (cursor.moveToNext()) {
                bill b = new bill();
                b.id=cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.id2));
                b.date1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.date1));
                b.account = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.account));
                b.type1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.type1));;
                b.from1 = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.from1));

                bills.add(b);
            }

            cursor.close();
        }

        db.close();

        return bills;
    }

    //数据容器，装载从数据库中读出的数据内容。
    private class bill {
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
        headRow.createCell(0).setCellValue(MySQLiteOpenHelper.id2);
        headRow.createCell(1).setCellValue(MySQLiteOpenHelper.date1);
        headRow.createCell(2).setCellValue(MySQLiteOpenHelper.account);
        headRow.createCell(3).setCellValue(MySQLiteOpenHelper.type1);
        headRow.createCell(4).setCellValue(MySQLiteOpenHelper.from1);
        headRow.createCell(5).setCellValue(MySQLiteOpenHelper.beizhu);
        //headRow.createCell(6).setCellValue(MySQLiteOpenHelper.beizhu);
    }

    // 创建Excel的一行数据。
    private static void createCell(int id2,String date1, int account,ArrayList<String>type1, String from1, String beizhu,HSSFSheet sheet) {
        HSSFRow dataRow = sheet.createRow(sheet.getLastRowNum() + 1);
        dataRow.createCell(0).setCellValue(id2);
        dataRow.createCell(1).setCellValue(date1);
        dataRow.createCell(2).setCellValue(account);
        String tmp="";
        for(int i=0;i<type1.size();i++) tmp+=(type1.get(i)+" ");
        dataRow.createCell(3).setCellValue(tmp);
        dataRow.createCell(4).setCellValue(from1);
        dataRow.createCell(5).setCellValue(beizhu);
    }
}
