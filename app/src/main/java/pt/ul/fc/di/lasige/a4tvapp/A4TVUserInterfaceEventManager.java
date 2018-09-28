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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by LaSIGE on 12/09/2017.
 */

public class A4TVUserInterfaceEventManager extends SQLiteOpenHelper {

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

    private int NumberOfErrors = 0;


    public A4TVUserInterfaceEventManager(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            try {
                SQLiteDatabase db = this.getWritableDatabase();
                onCreate(db);
            }catch(Exception e){

            }

    }

    //------------------------ Storing User Interface Events -----------------//

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
    public Action addAction(String description, String block_type, String block_orientation, String item_index, String modality, String current_level, String interaction_mode) {
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

        return new Action(_id, description, block_type, block_orientation, item_index, modality, current_level, interaction_mode, datetime);
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

    // Getting all Actions except (description)
    public List<Action> getActionsExcept(String desc) {
        List<Action> actionList = new ArrayList<Action>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + KEY_DESCRIPTION +" != '" + desc + "'";

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
                writer.write(a._id + "," + a._description + "," + a._block_type + "," + a._block_orientation + "," + a._item_index + "," + a._interaction_mode + "," + a._current_level + "," + a._modality + "," + a._date);
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


    //------------------------ Analyze User Interface Events -----------------//

    public boolean hasUserDoneTutorial(){
        return getActionCount("begin_tutorial") > 0;
    }

    public boolean isUserExperiencedWithVerbose() {
        int nOfActionsInVerbose = getActionCountByLevel("1.");
        return ( nOfActionsInVerbose > 0 && nOfActionsInVerbose > 150 && NumberOfErrors / nOfActionsInVerbose < 10);
    }

    //User is always using the talkback modality
    public boolean isUserAlwaysUsingTalkBAck(){
        float count = 0;


        List<Action> actions = getActionsExcept("current_block_info");
        for (int i = 0; i < actions.size(); i++) {

            if(actions.get(i).getModality().compareTo("button") == 0 )
                count++;

        }

        return (count/actions.size() >= 0.8);
    }

    //Direction Shift, usually happens when a user stops progressing along a branch of a task tree
    public int getDirectionShiftPattern(){
        return 0;
    }

    //Action Cancellation, occurs when a user backtracks immediately after taking an action
    public int getActionCancelationPattern(){
        return 0;
    }

    //Irrelevant Actions, when the user performs irrelevant actions during a task
    //Actions that result in the same index will be considered irrelevant
    public int getIrrelevantActionsPattern(){
        int count = 0;

        List<Action> actions = getSpecificActions("current_block_info");
        for (int i = 0; i < actions.size()-4; i+=2) {

            String index_1_1 = actions.get(i).getItemIndex();
            String index_2_1 = actions.get(i+1).getItemIndex();
            String index_1_2 = actions.get(i+2).getItemIndex();
            String index_2_2 = actions.get(i+3).getItemIndex();
            if(index_1_1.compareTo(index_1_2) == 0 && index_2_1.compareTo(index_2_2) == 0){
                count++;
            }
        }


        return count;
    }

    //Action Re-occurrence, when the user performs an elementary action repeatedly
    public int getReOccurencePattern(String elementaryAction){
        int count = 0;
        int countConsec = 0;

        List<Action> actions = getActionsExcept("current_block_info");
        //System.out.println(actions.size());
        for (int i = 0; i < actions.size() - 1; i++) {

            //System.out.println(actions.get(i).getDescription());
            if (actions.get(i).getDescription().compareTo(elementaryAction) == 0 &&
                    (actions.get(i + 1).getDescription().compareTo(elementaryAction) == 0
                            || actions.get(i + 1).getDescription().compareTo(elementaryAction) == 0)) {

                countConsec++;


                if (countConsec == 3) // higher consec doesnt need to count
                    count++;


            } else {
                countConsec = 0;
            }

        }


        return count;
    }

    //Upstairs Pattern can indicate that the user is not following the route that the designer of the website intended
    public int getUpstairsPattern(){
        return 0;
    }

    //Fingers pattern arises when the user navigates to other pages in a website but returns after short periods of time
    public int getFingersPattern(){
        return 0;
    }

    //Vertical/Horizontal Mouse Movement, this pattern happens when a user is unable to proceed with the execution of a
    // task and starts to visually explore the interface for other options, usually this is reflected in the motion of the mouse pointer
    public int getExplorePattern(){
        return 0;
    }

    //Quick Up/Down Scroll pattern means the user is searching and skimming for information
    public int getQuickVerticalnavigationPattern(){
        int count = 0;
        int countConsec = 0;

        List<Action> actions = getActionsExcept("current_block_info");

        for (int i = 0; i < actions.size() - 1; i++) {

            String time1 = actions.get(i).getDate();
            SimpleDateFormat sdf1 = new java.text.SimpleDateFormat ("d-M-yyyy hh:mm:ss aa");
            String time2 = actions.get(i+1).getDate();
            SimpleDateFormat sdf2 = new java.text.SimpleDateFormat ("d-M-yyyy hh:mm:ss aa");
            try {
                sdf1.parse (time1);
                sdf2.parse (time2);
                long diff = sdf2.getCalendar().getTimeInMillis() - sdf1.getCalendar().getTimeInMillis();

                if ((actions.get(i).getDescription().compareTo("up") == 0 || actions.get(i).getDescription().compareTo("down") == 0) &&
                        ((actions.get(i + 1).getDescription().compareTo("up") == 0 || actions.get(i + 1).getDescription().compareTo("down") == 0)
                                || (actions.get(i + 1).getDescription().compareTo("start_speech") == 0 && (actions.get(i + 2).getDescription().compareTo("up") == 0
                                || actions.get(i + 2).getDescription().compareTo("down") == 0))) &&
                        ((actions.get(i).getModality().compareTo("button") == 0 && diff <= 4000) ||
                                (actions.get(i).getModality().compareTo("speech") == 0 && diff <= 6000) ||
                                (actions.get(i).getModality().compareTo("screen_gesture") == 0 && diff <= 2000) ||
                                (actions.get(i).getModality().compareTo("mid_air_gesture") == 0 && diff <= 2000))) {

                    countConsec++;

                    if (countConsec == 3) // higher consec doesnt need to count
                        count++;

                } else {
                    countConsec = 0;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        return count;
    }

    //Quick Left/Right Scroll pattern means the user is searching and skimming for information
    public int getQuickHorizontalNavigationPattern(){
        int count = 0;
        int countConsec = 0;

        List<Action> actions = getActionsExcept("current_block_info");

        for (int i = 0; i < actions.size() - 1; i++) {

            String time1 = actions.get(i).getDate();
            SimpleDateFormat sdf1 = new java.text.SimpleDateFormat ("d-M-yyyy hh:mm:ss aa");
            String time2 = actions.get(i+1).getDate();
            SimpleDateFormat sdf2 = new java.text.SimpleDateFormat ("d-M-yyyy hh:mm:ss aa");

            try {
                sdf1.parse (time1);
                sdf2.parse (time2);
                long diff = sdf2.getCalendar().getTimeInMillis() - sdf1.getCalendar().getTimeInMillis();
                /*System.out.println("Date1 =" + sdf1.getCalendar().getTime());
                System.out.println("Date2 =" + sdf2.getCalendar().getTime());
                System.out.println("Modality: "+ actions.get(i).getModality() + " Diff =" + diff);*/
                if ((actions.get(i).getDescription().compareTo("left") == 0 || actions.get(i).getDescription().compareTo("right") == 0) &&
                        ((actions.get(i + 1).getDescription().compareTo("left") == 0 || actions.get(i + 1).getDescription().compareTo("right") == 0)
                                || (actions.get(i + 1).getDescription().compareTo("start_speech") == 0 && (actions.get(i + 2).getDescription().compareTo("left") == 0
                                || actions.get(i + 2).getDescription().compareTo("right") == 0))) &&
                        ((actions.get(i).getModality().compareTo("button") == 0 && diff <= 4000) ||
                                (actions.get(i).getModality().compareTo("speech") == 0 && diff <= 6000) ||
                                (actions.get(i).getModality().compareTo("screen_gesture") == 0 && diff <= 2000) ||
                                (actions.get(i).getModality().compareTo("mid_air_gesture") == 0 && diff <= 2000))) {

                    countConsec++;

                    if (countConsec == 3) // higher consec doesnt need to count
                        count++;

                } else {
                    countConsec = 0;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        return count;
    }

    //Page Hoping happens when a user spends short periods of time in a page as he/she is backtracking for a familiar page
    public int getPageHopingsPattern(){
        return 0;
    }

    //Hub and Spoke navigation pattern occurs when the user keeps returning to a familiar page after accessing an unfamiliar one,
    // not accessing more than two pages away.
    public int getHubAndSpokePattern(){
        return 0;
    }

    //User is always using the localize feature after each action
    public int getLostAwarenessPattern(){
        int count = 0;
        int countConsec = 0;

        List<Action> actions = getAllActions();
        for (int i = 0; i < actions.size()-2; i++) {

            if(actions.get(i).getDescription().compareTo("current_block_info") == 0 && actions.get(i+1).getDescription().compareTo("current_block_info") != 0){

                if(actions.get(i+1).getDescription().compareTo("localize") == 0 || actions.get(i+2).getDescription().compareTo("localize") == 0){

                    countConsec++;

                    if(countConsec == 3) // higher consec doesnt need to count
                        count++;

                }else{
                    countConsec = 0;
                }
            }
        }


        return count;
    }

}
