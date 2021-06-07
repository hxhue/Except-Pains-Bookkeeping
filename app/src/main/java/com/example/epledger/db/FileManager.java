package com.example.epledger.db;

//package com.example.backupdemo.manager;
//import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.github.mikephil.charting.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileManager {
    public static final String COMMAND_BACKUP = "backup";
    public static final String COMMAND_RESTORE = "restore";
    public static final String COMMAND_ISCONTACT = "contact";
    private Context mContext;

    public FileManager(Context context) {
        this.mContext = context;
    }
    public void reloadDb(String path) throws IOException, JSONException {
        File savefile=new File(path);
        //System.out.println(sdCardDir+"/SqliteDatabase.csv");
        //File fileParent = savefile.getParentFile();
        if(!savefile.exists()){
            savefile.createNewFile();
        }
        String datapath="/data/data/com.example.epledger/databases/test";
        File dbfile=new File(datapath);
        //System.out.println(sdCardDir+"/SqliteDatabase.csv");
        //File fileParent = savefile.getParentFile();
        if(!dbfile.exists()){
            dbfile.createNewFile();
        }
        fileCopy(dbfile,savefile);
    }

    public void writeDb() throws IOException, JSONException {
        String fullPath = Environment.getExternalStorageDirectory()+"/Database";
        String datapath="/data/data/com.example.epledger/databases/test";
        File savefile=new File(datapath);
        //System.out.println(sdCardDir+"/SqliteDatabase.csv");
        //File fileParent = savefile.getParentFile();
        if(!savefile.exists()){
            savefile.createNewFile();
        }
        File file=new File(fullPath);
        //System.out.println(sdCardDir+"/SqliteDatabase.csv");
        //File fileParent = savefile.getParentFile();
        if(!file.exists()){
            file.createNewFile();
        }
        fileCopy(file,savefile);
    }

    public void fileCopy(File backup, File dbFile) throws IOException {

        FileChannel inChannel = new FileInputStream(dbFile).getChannel();
        FileChannel outChannel = new FileOutputStream(backup).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }
}