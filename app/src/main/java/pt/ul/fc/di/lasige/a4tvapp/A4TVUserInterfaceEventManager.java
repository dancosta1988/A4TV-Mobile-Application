package pt.ul.fc.di.lasige.a4tvapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
    private static final String ACTION_ID = "action_id";
    private static final String ACTION_USER_ID = "user_id";
    private static final String ACTION_DESCRIPTION = "description";
    private static final String ACTION_MODALITY = "modality";
    private static final String ACTION_CURRENT_LEVEL = "current_level";
    private static final String ACTION_INTERACTION_MODE = "interaction_mode";
    private static final String ACTION_BLOCK_TYPE = "block_type";
    private static final String ACTION_BLOCK_ORIENTATION = "block_orientation";
    private static final String ACTION_ITEM_INDEX = "item_index";
    private static final String ACTION_ITEM_NAME = "item_name";
    private static final String ACTION_DATE = "DATE";
    
    //user options table name
    private static final String TABLE_USER_OPTIONS = "user_options";
    
    //User options Columns names
    public static final String USER_ID = "user_id";
    public static final String USER_READING_MODE = "reading_mode";
    public static final String USER_FOCUS_MODE = "focus_mode";
    public static final String USER_INTERACTION_MODE = "interaction_mode";
    public static final String USER_USER_TYPE = "user_type";
    public static final String USER_SPEECH_SPEED = "speech_speed";
    public static final String USER_SPEECH_PITCH = "speech_pitch";
    public static final String USER_GESTURE_MODE = "gesture_mode";
    public static final String USER_USE_APP_VOICE = "use_voice";
    public static final String USER_LAST_LOGGED = "last_logged";


    //Thresholds
    static  int IRRELEVANT_ACTIONS_THRESHOLD = 6;
    static  int SHORT_IRRELEVANT_ACTIONS_THRESHOLD = 3;
    static  int REOCCURENCE_LOCALIZE_ACTIONS_THRESHOLD = 6;
    static  int REOCCURENCE_READ_SCREEN_ACTIONS_THRESHOLD = 6;
    static  int REOCCURENCE_SPEECH_ACTIONS_THRESHOLD = 6;
    static  int SHORT_REOCCURENCE_LOCALIZE_ACTIONS_THRESHOLD = 3;
    static  int SHORT_REOCCURENCE_READ_SCREEN_ACTIONS_THRESHOLD = 3;
    static  int SHORT_REOCCURENCE_SPEECH_ACTIONS_THRESHOLD = 3;
    static  int QUICK_SCROLL_ACTIONS_THRESHOLD = 6;
    static  int LOST_AWARENESS_ACTIONS_THRESHOLD = 6;
    static  int LONG_CHECK_ACTIONS_THRESHOLD = 6685; //Average 191 actions per hour (7 days x 5 hours = 6685)
    static  int SHORT_CHECK_ACTIONS_THRESHOLD = 120;
    static int CONSEC_COUNT = 1;


    private int NumberOfErrors = 0;
    private String lastAction = "";
    private String lastIndex = "";
    private int startSpeechInRow = 1;
    private int irrelevantActionsInRow = 0;
    private int actionsToCheck;
    private String currentUserID;


    public A4TVUserInterfaceEventManager(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            try {
                SQLiteDatabase db = this.getWritableDatabase();
                onCreate(db);
            }catch(Exception e){

            }

    }

    public void setCurrentUserID(String ACTION_USER_ID){
        currentUserID = ACTION_USER_ID;
    }
    //------------------------ Storing User Interface Events -----------------//

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ACTIONS_TABLE = "CREATE TABLE " + TABLE_ACTIONS + "("
                + ACTION_ID + " TEXT PRIMARY KEY," + ACTION_USER_ID + " TEXT,"+ ACTION_DESCRIPTION + " TEXT," + ACTION_BLOCK_TYPE + " TEXT," + ACTION_BLOCK_ORIENTATION + " TEXT," + ACTION_ITEM_INDEX + " TEXT,"
                + ACTION_ITEM_NAME + " TEXT,"+ ACTION_MODALITY + " TEXT, " + ACTION_CURRENT_LEVEL + " TEXT," + ACTION_INTERACTION_MODE + " TEXT," + ACTION_DATE + " DATE )";
        db.execSQL(CREATE_ACTIONS_TABLE);

        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER_OPTIONS + "("
                + USER_ID + " TEXT PRIMARY KEY," + USER_READING_MODE + " TEXT,"+ USER_FOCUS_MODE + " TEXT," + USER_INTERACTION_MODE + " TEXT," + USER_USER_TYPE + " TEXT," + USER_GESTURE_MODE + " TEXT,"
                + USER_SPEECH_SPEED + " TEXT, " + USER_SPEECH_PITCH + " TEXT, " + USER_USE_APP_VOICE + " TEXT, " + USER_LAST_LOGGED + " TEXT )";
        db.execSQL(CREATE_USER_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_OPTIONS);
        // Create tables again
        onCreate(db);
    }

    public void deleteStorage(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_OPTIONS);
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new user
    public void addUser(String user_id){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_ID, user_id);
        values.put(USER_READING_MODE, "1");
        values.put(USER_FOCUS_MODE, "1");
        values.put(USER_INTERACTION_MODE, "1");
        values.put(USER_USER_TYPE, "1");
        values.put(USER_GESTURE_MODE, "0");
        values.put(USER_SPEECH_SPEED, "1.0");
        values.put(USER_SPEECH_PITCH, "1.0");
        values.put(USER_USE_APP_VOICE, "true");
        values.put(USER_LAST_LOGGED, "true");

        // Inserting Row
        db.insert(TABLE_USER_OPTIONS, null, values);
        db.close(); // Closing database connection

    }

    public void updateUserOption( String option, String value){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(option, value);

        // Inserting Row
        db.update(TABLE_USER_OPTIONS, values, USER_ID + "='" + currentUserID + "'", null );
        db.close(); // Closing database connection

    }

    public void updateUserOptions(String reading_mode, String focus_mode, String interaction_mode, String user_type, String gesture_mode, String speech_speed, String speech_pitch, String use_voice ){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_READING_MODE, reading_mode);
        values.put(USER_FOCUS_MODE, focus_mode);
        values.put(USER_INTERACTION_MODE, interaction_mode);
        values.put(USER_USER_TYPE, user_type);
        values.put(USER_GESTURE_MODE, gesture_mode);
        values.put(USER_SPEECH_SPEED, speech_speed);
        values.put(USER_SPEECH_PITCH, speech_pitch);
        values.put(USER_USE_APP_VOICE, use_voice);



        // Inserting Row
        int result = db.update(TABLE_USER_OPTIONS, values, USER_ID + "='" + currentUserID + "'", null );
        System.err.println( "Updating User: " + currentUserID + " use voice: " + use_voice + " result " + result);
        db.close(); // Closing database connection

    }

    public User getUserLastlogged(){
        String selectQuery = "SELECT  * FROM " + TABLE_USER_OPTIONS + " WHERE " + USER_LAST_LOGGED + " = 'true'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        User user = null;
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            user = new User(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7), cursor.getString(8));
            System.err.println( "Getting User: " + currentUserID + " use voice: " + cursor.getString(8) + " getting boolean " + Boolean.valueOf(cursor.getString(8)));
        }

        // return contact list
        return user;
    }

    public User getUserOptions(){
        String selectQuery = "SELECT  * FROM " + TABLE_USER_OPTIONS + " WHERE " + USER_ID + " = '" + currentUserID + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        User user = null;
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

                user = new User(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7), cursor.getString(8));
            System.err.println( "Getting User: " + currentUserID + " use voice: " + cursor.getString(8) + " getting boolean " + Boolean.valueOf(cursor.getString(8)));
        }

        // return contact list
        return user;
    }

    // Adding new action
    public Action addAction(String description, String block_type, String block_orientation, String item_index, String item_name, String modality, String current_level, String interaction_mode) {
        SQLiteDatabase db = this.getWritableDatabase();

        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("dMhms");
        String datetime = dateformat.format(date);

        String _id = UUID.randomUUID().toString();



        //int _id = (int)Integer.getInteger(datetime);

        ContentValues values = new ContentValues();
        values.put(ACTION_ID, _id);
        values.put(ACTION_USER_ID, currentUserID);
        values.put(ACTION_DESCRIPTION, description);
        values.put(ACTION_BLOCK_TYPE, block_type);
        values.put(ACTION_BLOCK_ORIENTATION, block_orientation);
        values.put(ACTION_ITEM_INDEX, item_index);
        values.put(ACTION_ITEM_NAME, item_name);
        values.put(ACTION_MODALITY, modality);
        values.put(ACTION_CURRENT_LEVEL, current_level);
        values.put(ACTION_INTERACTION_MODE, interaction_mode);

        dateformat = new SimpleDateFormat("d-M-yyyy hh:mm:ss aa");
        datetime = dateformat.format(date);

        values.put(ACTION_DATE, datetime);
        //System.err.println("Storing action id: " + _id + " description:" + description + " using " + modality + " at " + datetime);
        //System.err.println("Stored ui info - selected item: " + item_index +" from block with type:" + block_type + " orientation: " + block_orientation);


        // Inserting Row
        db.insert(TABLE_ACTIONS, null, values);
        db.close(); // Closing database connection

        if(description.compareTo(lastAction) == 0 && description.compareTo(Action.START_SPEECH) == 0){
            startSpeechInRow++;
        }else{
            startSpeechInRow = 1;
        }

        lastAction = description;
        return new Action(_id, currentUserID, description, block_type, block_orientation, item_index, item_name, modality, current_level, interaction_mode, datetime);
    }

    /*// Getting single contact
    Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { ACTION_ID,
                        ACTION_NAME, ACTION_PH_NO }, ACTION_ID + "=?",
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
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + ACTION_USER_ID + " = '" + currentUserID + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Action action = new Action(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10));
                // Adding contact to list
                actionList.add(action);
            } while (cursor.moveToNext());
        }

        // return contact list
        return actionList;
    }

    public void removeNulls(String user){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ACTION_USER_ID, user);

        // Inserting Row
        int result = db.update(TABLE_ACTIONS, values, ACTION_USER_ID +" IS NULL", null );
        System.err.println( "Updating User: " + user);
        db.close(); // Closing database connection
    }


    // Getting actions Count
    public int getAllActionsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_ACTIONS  + " WHERE " + ACTION_USER_ID + " = '" + currentUserID + "'";
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
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + ACTION_DESCRIPTION +" = '" + desc + "'" + " AND " + ACTION_USER_ID + " = '" + currentUserID + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Action action = new Action(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10));
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
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + ACTION_DESCRIPTION +" != '" + desc + "' AND " + ACTION_USER_ID + " = '" + currentUserID + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Action action = new Action(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10));
                // Adding contact to list
                actionList.add(action);
            } while (cursor.moveToNext());
        }

        // return contact list
        return actionList;
    }

    // Getting a specific action Count
    public int getActionCount(String desc) {
        String countQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + ACTION_DESCRIPTION +" = '" + desc + "'" + " AND " + ACTION_USER_ID + " = '" + currentUserID + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count =cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting a specific level action Count
    public int getActionCountByLevel(String level) {
        String countQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + ACTION_CURRENT_LEVEL +" LIKE '" + level + "%'" + " AND " + ACTION_USER_ID + " = '" + currentUserID + "'";
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
        String selectQuery = "SELECT  * FROM " + TABLE_ACTIONS + " WHERE " + ACTION_CURRENT_LEVEL +" LIKE '" + level + "%'" + " AND " + ACTION_USER_ID + " = '" + currentUserID + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Action action = new Action(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4), cursor.getString(5), cursor.getString(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10));
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
            System.err.println( "id: " + a._id + " user id: " + a._user_id +" desc: " + a._description + " interaction mode: " + a._interaction_mode + " level: " + a._current_level + " modality: " + a._modality + " date: " + a._date);
        }
        System.err.println( "---------------------------------------------------------------------------");
    }

    public void storeAllActionsOnCSV(String dir){
        System.err.println( "Saving to: " + dir);

        try {
            File root = new File(dir);

            if (!root.exists()) {
                if (root.mkdir()) ; //directory is created;
            }

            File outputFile = new File(root, currentUserID+"_user_actions.csv");
           /* if (!outputFile.exists()) {
                if (outputFile.mkdir()) ; //directory is created;
            }*/

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false));

            List<Action> actionsList = new ArrayList<Action>();
            actionsList = getAllActions();
            for (Action a : actionsList) {
                //System.err.println( "id: " + a._id + " desc: " + a._description + " interaction mode: " + a._interaction_mode + " level: " + a._current_level + " modality: " + a._modality + " date: " + a._date);
                writer.write(a._id + "," + a._description + "," + a._block_type + "," + a._block_orientation + "," + a._item_index + "," + a._item_name + "," + a._interaction_mode + "," + a._current_level + "," + a._modality + "," + a._date);
                writer.newLine();
            }
            System.err.println( "Status: Successful");
            writer.close();

        }catch(IOException io){
            System.err.println( "Status: Failed to save actions. Report: ");
            System.err.println( io.getMessage());
        }

    }

    public void readConfigFile(String dir, String fileName){

        try {
            //read the file containing the configuration
            File initialFile = new File(dir + fileName);
            InputStream targetStream = new FileInputStream(initialFile);

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(targetStream, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, null, "config");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the property tag
                if (name.equals("property")) {
                    String nameP = parser.getAttributeValue(null, "name");
                    String valueP = "";
                    if (parser.next() == XmlPullParser.TEXT) {
                        valueP = parser.getText();
                        parser.nextTag();
                    }
                    if(valueP != ""){
                        System.err.println( "Property: " + nameP + " = " + valueP);
                        if(nameP.compareTo("IRRELEVANT_ACTIONS_THRESHOLD") == 0)
                            IRRELEVANT_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("SHORT_IRRELEVANT_ACTIONS_THRESHOLD") == 0)
                            SHORT_IRRELEVANT_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("REOCCURENCE_LOCALIZE_ACTIONS_THRESHOLD") == 0)
                            REOCCURENCE_LOCALIZE_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("REOCCURENCE_READ_SCREEN_ACTIONS_THRESHOLD") == 0)
                            REOCCURENCE_READ_SCREEN_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("REOCCURENCE_SPEECH_ACTIONS_THRESHOLD") == 0)
                            REOCCURENCE_SPEECH_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("SHORT_REOCCURENCE_LOCALIZE_ACTIONS_THRESHOLD") == 0)
                            SHORT_REOCCURENCE_LOCALIZE_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("SHORT_REOCCURENCE_READ_SCREEN_ACTIONS_THRESHOLD") == 0)
                            SHORT_REOCCURENCE_READ_SCREEN_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("SHORT_REOCCURENCE_SPEECH_ACTIONS_THRESHOLD") == 0)
                            SHORT_REOCCURENCE_SPEECH_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("QUICK_SCROLL_ACTIONS_THRESHOLD") == 0)
                            QUICK_SCROLL_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("LOST_AWARENESS_ACTIONS_THRESHOLD") == 0)
                            LOST_AWARENESS_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("LONG_CHECK_ACTIONS_THRESHOLD") == 0)
                            LONG_CHECK_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                        else if(nameP.compareTo("SHORT_CHECK_ACTIONS_THRESHOLD") == 0)
                            SHORT_CHECK_ACTIONS_THRESHOLD = Integer.parseInt(valueP);
                    }
                }
            }

        }catch(Exception e){
            System.err.println( "Status: Failed to read config file. Report: ");
            System.err.println( e.getMessage());
            e.printStackTrace();
        }

    }


    //------------------------ Analyze User Interface Events -----------------//

    public boolean hasUserDoneTutorial(){
        return getActionCount(Action.BEGIN_TUTORIAL) > 0;
    }

    public boolean isUserExperiencedWithVerbose() {
        int nOfActionsInVerbose = getActionCountByLevel("1.");
        return ( nOfActionsInVerbose > 0 && nOfActionsInVerbose > 150 && NumberOfErrors / nOfActionsInVerbose < 10);
    }

    public boolean isTimeToCheckEvents(){

        Long currentMili = new Date().getTime();

        List<Action> actions = getAllActions();
        boolean needCheck = false;
        int i = actions.size()-1;
        int count = 0;

        try {
            String time1 = actions.get(actions.size() - 1).getDate();
            SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("d-M-yyyy hh:mm:ss aa");
            sdf1.parse(time1);
            int current_day = sdf1.getCalendar().get(Calendar.DAY_OF_MONTH);


            if(actions.size() > 0) {
                while ( i > 0 && actions.get(i).getDescription().compareTo(Action.CHECK_USER_EVENTS) != 0)
                {
                    i--;
                    count++;
                }

                String time2 = actions.get(i).getDate();
                SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("d-M-yyyy hh:mm:ss aa");
                sdf2.parse(time2);
                int last_check_day = sdf2.getCalendar().get(Calendar.DAY_OF_MONTH);
                actionsToCheck = count;
                if(actionsToCheck >= LONG_CHECK_ACTIONS_THRESHOLD && last_check_day != current_day)
                    needCheck = true;
            }
            System.err.println("Checking time for pattern analysis. Number of actions: " + count + ". Needs check: " + needCheck);
            return needCheck;

        }catch(ParseException e){
            System.out.println("Error parsing date: " + e.getMessage());
            return false;
        }
    }

    //User is always using the talkback modality
    public boolean isUserAlwaysUsingTalkBAck(){
        float count = 0;


        List<Action> actions = getActionsExcept(Action.CURRENT_BLOCK_INFO);
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
    public boolean findIrrelevantActionsPattern(boolean longTerm) {
        boolean found = false;
        if(longTerm) {
            found = getIrrelanvActionsPattern();
        }else{
            found = (irrelevantActionsInRow >= SHORT_IRRELEVANT_ACTIONS_THRESHOLD);
        }
        return found;
    }

    public void increaseIrrelevantActions(){
            irrelevantActionsInRow++;
    }

    public void resetIrrelevantActions(){
        irrelevantActionsInRow = 0;
    }

    private boolean getIrrelanvActionsPattern(){
        int count = 0;
        String[] skip = new String[]{"full-screen-button-next", "full-screen-button-pause", "full-screen-button-prev", "full-screen-progress-indicator"};
        List<Action> actions = getSpecificActions(Action.CURRENT_BLOCK_INFO);
        if(actions.size() > actionsToCheck)
            actions = actions.subList(actions.size()-actionsToCheck-1,actions.size()-1);
        int i = 0;
        for (i = 0; i < actions.size()-4;) {

                String index_1_1 = actions.get(i).getItemName();
                String index_2_1 = actions.get(i + 1).getItemName();
                String index_1_2 = actions.get(i + 2).getItemName();
                String index_2_2 = actions.get(i + 3).getItemName();
                if ((index_1_1.compareTo(index_1_2) == 0 && index_2_1.compareTo(index_2_2) == 0)
                        && (index_1_1.compareTo(index_2_1) != 0 || index_1_2.compareTo(index_2_2) != 0 )) {
                    count++;
                    i+=4;
                    System.out.println("Irrelavant action found: 1. " + index_1_1 + " 2. " + index_1_2 + " | 1. " + index_2_1+" 2. " + index_2_2);
                }else if(index_1_1.compareTo(index_2_1) == 0 &&
                        (index_1_1.compareTo(skip[0]) != 0 && index_1_1.compareTo(skip[1]) != 0 &&
                                index_1_1.compareTo(skip[2]) != 0 && index_1_1.compareTo(skip[3]) != 0)){
                    count++;
                    i++;
                    System.out.println("Irrelavant action found: 1. " + index_1_1 + " 2. " + index_2_1);
                }else{
                    i++;
                }
        }
        System.out.println("Irrelavant actions count: " + count + " from total: " + i);

        return count >= ((IRRELEVANT_ACTIONS_THRESHOLD * actionsToCheck) / LONG_CHECK_ACTIONS_THRESHOLD);
    }

    public boolean findReOccurencePattern(String elementaryAction, boolean longTerm) {
        boolean found = false;
        if(longTerm) {
            switch (elementaryAction) {
                case Action.LOCALIZE:
                    found = getReOccurencePattern(elementaryAction, actionsToCheck) >= ((REOCCURENCE_LOCALIZE_ACTIONS_THRESHOLD * actionsToCheck) / LONG_CHECK_ACTIONS_THRESHOLD);
                    break;
                case Action.READ_SCREEN:
                    found = getReOccurencePattern(elementaryAction, actionsToCheck) >= ((REOCCURENCE_READ_SCREEN_ACTIONS_THRESHOLD * actionsToCheck) / LONG_CHECK_ACTIONS_THRESHOLD);
                    break;
                case Action.START_SPEECH:
                    found = getReOccurencePattern(elementaryAction, actionsToCheck) >= ((REOCCURENCE_SPEECH_ACTIONS_THRESHOLD * actionsToCheck) / LONG_CHECK_ACTIONS_THRESHOLD);
                    break;
            }
        }else{
            switch (elementaryAction) {
                case Action.LOCALIZE:

                    break;
                case Action.READ_SCREEN:

                    break;
                case Action.START_SPEECH:
                    System.err.println("startSpeechInRow " + startSpeechInRow);
                    found = (startSpeechInRow >= SHORT_REOCCURENCE_SPEECH_ACTIONS_THRESHOLD);
                    break;
            }
        }
        return found;
    }

    //Action Re-occurrence, when the user performs an elementary action repeatedly
    private int getReOccurencePattern(String elementaryAction, int lastNumberofActions){
        int count = 0;
        int countConsec = 0;
        //Long currentMili = new Date().getTime();
        List<Action> actions = getActionsExcept(Action.CURRENT_BLOCK_INFO);
        if(actions.size() > lastNumberofActions)
            actions = actions.subList(actions.size()-lastNumberofActions-1,actions.size()-1);
        //System.out.println(actions.size());
        for (int i = 0; i < actions.size() - 1; i++) {

                    //System.out.println(i + " " + actions.get(i).getDescription());
                    if (actions.get(i).getDescription().compareTo(elementaryAction) == 0 &&
                            (actions.get(i + 1).getDescription().compareTo(elementaryAction) == 0
                                    || actions.get(i + 2).getDescription().compareTo(elementaryAction) == 0)) {

                        countConsec++;


                        if (countConsec == CONSEC_COUNT) // higher consec doesnt need to count
                            count++;


                    } else {
                        countConsec = 0;
                    }

        }

        System.out.println("Reoccurences for " + elementaryAction + " count: " + count);
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

    public boolean findQuickVerticalnavigationPattern(){
        return (getQuickVerticalnavigationPattern() + getQuickHorizontalNavigationPattern()) >= ((QUICK_SCROLL_ACTIONS_THRESHOLD * actionsToCheck) / LONG_CHECK_ACTIONS_THRESHOLD);
    }

    //Quick Up/Down Scroll pattern means the user is searching and skimming for information
    private int getQuickVerticalnavigationPattern(){
        int count = 0;
        int countConsec = 0;

        List<Action> actions = getActionsExcept(Action.CURRENT_BLOCK_INFO);
        if(actions.size() > actionsToCheck)
            actions = actions.subList(actions.size()-actionsToCheck-1,actions.size()-1);
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
                                || (actions.get(i + 1).getDescription().compareTo(Action.START_SPEECH) == 0 && (actions.get(i + 2).getDescription().compareTo("up") == 0
                                || actions.get(i + 2).getDescription().compareTo("down") == 0))) &&
                        ((actions.get(i).getModality().compareTo("button") == 0 && diff <= 4000) ||
                                (actions.get(i).getModality().compareTo("speech") == 0 && diff <= 6000) ||
                                (actions.get(i).getModality().compareTo("screen_gesture") == 0 && diff <= 2000) ||
                                (actions.get(i).getModality().compareTo("mid_air_gesture") == 0 && diff <= 2000))) {

                    countConsec++;

                    if (countConsec == CONSEC_COUNT) // higher consec doesnt need to count
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
    private int getQuickHorizontalNavigationPattern(){
        int count = 0;
        int countConsec = 0;

        List<Action> actions = getActionsExcept(Action.CURRENT_BLOCK_INFO);

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
                                || (actions.get(i + 1).getDescription().compareTo(Action.START_SPEECH) == 0 && (actions.get(i + 2).getDescription().compareTo("left") == 0
                                || actions.get(i + 2).getDescription().compareTo("right") == 0))) &&
                        ((actions.get(i).getModality().compareTo("button") == 0 && diff <= 4000) ||
                                (actions.get(i).getModality().compareTo("speech") == 0 && diff <= 6000) ||
                                (actions.get(i).getModality().compareTo("screen_gesture") == 0 && diff <= 2000) ||
                                (actions.get(i).getModality().compareTo("mid_air_gesture") == 0 && diff <= 2000))) {

                    countConsec++;

                    if (countConsec == CONSEC_COUNT) // higher consec doesnt need to count
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

            if(actions.get(i).getDescription().compareTo(Action.CURRENT_BLOCK_INFO) == 0 && actions.get(i+1).getDescription().compareTo(Action.CURRENT_BLOCK_INFO) != 0){

                if(actions.get(i+1).getDescription().compareTo(Action.LOCALIZE) == 0 || actions.get(i+2).getDescription().compareTo(Action.LOCALIZE) == 0){

                    countConsec++;

                    if(countConsec == CONSEC_COUNT) // higher consec doesnt need to count
                        count++;

                }else{
                    countConsec = 0;
                }
            }
        }


        return count;
    }

}
