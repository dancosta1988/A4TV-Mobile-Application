package pt.ul.fc.di.lasige.a4tvapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by LaSIGE on 12/09/2017.
 */

public class StoreAndAnalyzeUserBehaviour extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "a4tv_user_actions";

    // Actions table name
    private static final String TABLE_ACTIONS = "actions";

    // Actions Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_MODALITY = "modality";
    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_INTERACTION_MODE = "interaction_mode";
    private static final String KEY_BLOCK_TYPE = "block_type";
    private static final String KEY_BLOCK_ORIENTATION = "block_orientation";
    private static final String KEY_ITEM_INDEX = "item_index";
    private static final String KEY_DATE = "DATE";

    public StoreAndAnalyzeUserBehaviour(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            try {
                SQLiteDatabase db = this.getWritableDatabase();
                onCreate(db);
            }catch(Exception e){

            }
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ACTIONS_TABLE = "CREATE TABLE " + TABLE_ACTIONS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_DESCRIPTION + " TEXT," + KEY_BLOCK_TYPE + " TEXT," + KEY_BLOCK_ORIENTATION + " TEXT," + KEY_ITEM_INDEX + " TEXT,"
                + KEY_MODALITY + " TEXT, " + KEY_CURRENT_LEVEL + " TEXT," + KEY_INTERACTION_MODE + " TEXT," + KEY_DATE + " DATE )";
        db.execSQL(CREATE_ACTIONS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIONS);

        // Create tables again
        onCreate(db);
    }

    public void deleteStorage(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIONS);
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new action
    void addAction(String description, String block_type, String block_orientation, String item_index, String modality, String current_level, String interaction_mode) {
        SQLiteDatabase db = this.getWritableDatabase();

        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("dMhms");
        String datetime = dateformat.format(date);

        String _id = UUID.randomUUID().toString();



        //int _id = (int)Integer.getInteger(datetime);

        ContentValues values = new ContentValues();
        values.put(KEY_ID, _id);
        values.put(KEY_DESCRIPTION, description);
        values.put(KEY_BLOCK_TYPE, block_type);
        values.put(KEY_BLOCK_ORIENTATION, block_orientation);
        values.put(KEY_ITEM_INDEX, item_index);
        values.put(KEY_MODALITY, modality);
        values.put(KEY_CURRENT_LEVEL, current_level);
        values.put(KEY_INTERACTION_MODE, interaction_mode);

        dateformat = new SimpleDateFormat("d-M-yyyy hh:mm:ss aa");
        datetime = dateformat.format(date);

        values.put(KEY_DATE, datetime);
        System.err.println("Storing action id: " + _id + " description:" + description + " using " + modality + " at " + datetime);
        System.err.println("Stored ui info - selected item: " + item_index +" from block with type:" + block_type + " orientation: " + block_orientation);
        // Inserting Row
        db.insert(TABLE_ACTIONS, null, values);
        db.close(); // Closing database connection
    }

    /*// Getting single contact
    Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                        KEY_NAME, KEY_PH_NO }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        // return contact
        return contact;
    }*/

    // Getting All Actions
    public List<Action> getAllActions() {
        List<Action> actionList = new ArrayList<Action>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Action action = new Action(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7),cursor.getString(8));
                // Adding contact to list
                actionList.add(action);
            } while (cursor.moveToNext());
        }

        // return contact list
        return actionList;
    }


    // Getting actions Count
    public int getAllActionsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ACTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count =cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting specific Actions (description)
    public List<Action> getSpecificActions(String desc) {
        List<Action> actionList = new ArrayList<Action>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + KEY_DESCRIPTION +" = '" + desc + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Action action = new Action(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7),cursor.getString(8));
                // Adding contact to list
                actionList.add(action);
            } while (cursor.moveToNext());
        }

        // return contact list
        return actionList;
    }

    // Getting a specific action Count
    public int getActionCount(String desc) {
        String countQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + KEY_DESCRIPTION +" = '" + desc + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count =cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting a specific level action Count
    public int getActionCountByLevel(String level) {
        String countQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + KEY_CURRENT_LEVEL +" LIKE '" + level + "%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count =cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting all Actions by level
    public List<Action> getActionsByLevel(String level) {
        List<Action> actionList = new ArrayList<Action>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + KEY_CURRENT_LEVEL +" LIKE '" + level + "%'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Action action = new Action(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7),cursor.getString(8));
                // Adding contact to list
                actionList.add(action);
            } while (cursor.moveToNext());
        }

        // return contact list
        return actionList;
    }

    public void showAllActionsOnConsole(){
        System.err.println( "Showing all actions' information on console");
        System.err.println( "---------------------------------------------------------------------------");
        List<Action> actionsList = new ArrayList<Action>();
        actionsList = getAllActions();
        for (Action a: actionsList) {
            System.err.println( "id: " + a._id + " desc: " + a._description + " interaction mode: " + a._interaction_mode + " level: " + a._current_level + " modality: " + a._modality + " date: " + a._date);
        }
        System.err.println( "---------------------------------------------------------------------------");
    }

    public void storeAllActionsOnCSV(String dir){
        System.err.println( "Saving all actions' information in a file");
        System.err.println( "Saving to: " + dir);
        System.err.println( "---------------------------------------------------------------------------");

        try {
            File root = new File(dir);

            if (!root.exists()) {
                if (root.mkdir()) ; //directory is created;
            }

            File outputFile = new File(root, "user_actions.csv");
           /* if (!outputFile.exists()) {
                if (outputFile.mkdir()) ; //directory is created;
            }*/

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false));

            List<Action> actionsList = new ArrayList<Action>();
            actionsList = getAllActions();
            for (Action a : actionsList) {
                //System.err.println( "id: " + a._id + " desc: " + a._description + " interaction mode: " + a._interaction_mode + " level: " + a._current_level + " modality: " + a._modality + " date: " + a._date);
                writer.write(a._id + "," + a._description + "," + a._block_type + "," + a._block_orientation + "," + a._interaction_mode + "," + a._current_level + "," + a._modality + "," + a._date);
                writer.newLine();
            }
            System.err.println( "Status: Successful");
            writer.close();

        }catch(IOException io){
            System.err.println( "Status: Failed to save actions. Report: ");
            System.err.println( io.getMessage());
        }

        System.err.println( "---------------------------------------------------------------------------");

    }

}
