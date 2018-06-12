package pt.ul.fc.di.lasige.a4tvapp;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.*;
import android.speech.tts.UtteranceProgressListener;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by Daniel Costa on 16/11/2015.
 */

public class Speaker implements OnInitListener {


    private TextToSpeech tts;

    private boolean stop = false;

    private boolean ready = false;

    private boolean allowed = false;

    private float pitch = 1.0f;

    private float speed = 1.0f;

    public Speaker(Context context){
        tts = new TextToSpeech(context, this);

    }

    public boolean isAllowed(){
        return allowed;
    }

    public void setPitch(float _pitch){ pitch = _pitch; }

    public void setSpeed(float _speed){ speed = _speed; }

    public void allow(boolean allowed){
        this.allowed = allowed;
    }

    public void onInit(int initStatus) {

        // check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if (tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                tts.setLanguage(Locale.US);

            tts.setPitch(pitch);
            tts.setSpeechRate(speed);

        }
        ready = true;
        System.out.println("Speaker Ready!");



    }

    /*public void repeat(){
        speak(lastSentence, TextToSpeech.QUEUE_FLUSH);
    }*/

    public boolean hasInitiated(){
        return ready;
    }

    public void speak(String text, int queueOp){

        // Speak only if the TTS is ready
        // and the user has allowed speech
        System.out.println("Ready: " + ready + " Allowed: " + allowed);

        if(ready && allowed) {
            allowed = false;
            Bundle bundle = new Bundle();
            bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_NOTIFICATION);
            CharSequence sq = text.subSequence(0, text.length());
            tts.stop();
            System.out.println("Sentence: " + text);
            int res = tts.speak(sq , queueOp, bundle, null);
            System.out.println("Speak result: " + res);
            allowed = true;

            while(tts.isSpeaking()){
                //DO NOTHING
            }
        }
    }

    public void stopSpeech(){
        interruptSpeech();
    }

    public void interruptSpeech(){
        tts.stop();
    }
    // Free up resources
    public void destroy(){
        tts.shutdown();
    }
}