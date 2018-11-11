package pt.ul.fc.di.lasige.a4tvapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by LaSIGE on 12/09/2017.
 */

public class Action {

    //public variables
    public String _id;
    public String _user_id;
    public String _description;
    public String _block_type;
    public String _block_orientation;
    public String _item_index;
    public String _modality;
    public String _current_level;
    public String _interaction_mode;
    public String _date;

    // constructor
    public Action(String _id, String _user_id, String _description, String _block_type, String _block_orientation, String _item_index, String _modality, String _current_level, String _interaction_mode, String _date){

            this._id = _id;
            this._user_id = _user_id;
            this._description = _description;
            this._block_orientation = _block_orientation;
            this._block_type = _block_type;
            this._item_index = _item_index;
            this._modality = _modality;
            this._current_level = _current_level;
            this._interaction_mode = _interaction_mode;
            this._date = _date;

    }

    // constructor
    public Action(String _description, String _user_id, String _block_type, String _block_orientation, String _item_index, String _modality, String _current_level, String _interaction_mode){

        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dMyyyyhhmmss");
        String datetime = dateformat.format(c.getTime());

        this._id =  UUID.randomUUID().toString();
        this._user_id = _user_id;
        this._description = _description;
        this._block_orientation = _block_orientation;
        this._block_type = _block_type;
        this._item_index = _item_index;
        this._modality = _modality;
        this._current_level = _current_level;
        this._interaction_mode = _interaction_mode;


        dateformat = new SimpleDateFormat("d-M-yyyy hh:mm:ss aa");
        datetime = dateformat.format(c.getTime());
        this._date = datetime;
    }

    // getting ID
    public String getID(){
        return this._id;
    }

    // getting user ID
    public String getUserID(){
        return this._user_id;
    }

    // getting description
    public String getDescription(){
        return this._description;
    }

    // getting modality
    public String getModality(){
        return this._modality;
    }

    // getting level
    public String getUserLevel(){
        return this._current_level;
    }

    // getting interaction mode
    public String getInteractionMode(){
        return this._interaction_mode;
    }

    // getting date
    public String getDate(){
        return this._date;
    }

    //getting block type
    public  String getBlockType(){ return this._block_type; }

    //getting block orientation
    public  String getBlockOrientation(){ return this._block_orientation; }

    //getting item index
    public  String getItemIndex(){ return this._item_index; }

}


