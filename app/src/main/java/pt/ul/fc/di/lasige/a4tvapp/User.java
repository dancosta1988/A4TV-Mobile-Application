package pt.ul.fc.di.lasige.a4tvapp;

class User {

    public String _user_id;
    public String _reading_mode;
    public String _focus_mode;
    public String _interaction_mode;
    public String _user_type;
    public String _gesture_mode;
    public String _speech_speed;
    public String _speech_pitch;
    public String _use_voice;

    public User(String user_id, String reading_mode, String focus_mode, String interaction_mode, String user_type, String gesture_mode, String speech_speed, String speech_pitch, String use_voice) {
        _user_id = user_id;
        _focus_mode = focus_mode;
        _reading_mode = reading_mode;
        _interaction_mode = interaction_mode;
        _user_type = user_type;
        _gesture_mode = gesture_mode;
        _speech_pitch = speech_pitch;
        _speech_speed = speech_speed;
        _use_voice = use_voice;
    }
}
