package pt.ul.fc.di.lasige.a4tvapp;


import android.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class TVApplicationState {
    private String hash;
    private String uiml;
    private ArrayList<String> focusedDescriptions;
    private ArrayList<String> localizeOutput;
    private ArrayList<String> readScreenOutput;

    public TVApplicationState(){
        localizeOutput = new ArrayList<String>();
        readScreenOutput = new ArrayList<String>();
        focusedDescriptions = new ArrayList<String>();
    }

    //getters
    public String getHash(){
        return hash;
    }

    public String getUIML(){
        return uiml;
    }

    public ArrayList<String> getLocalizeOutput(){
        return localizeOutput;
    }

    public ArrayList<String> getReadScreenOutput(){
        return readScreenOutput;
    }

    public ArrayList<String> getFocusedDescriptions(){
        return focusedDescriptions;
    }

    //setters
    public void setUIML(String newUIML)  {
        uiml = newUIML;

        byte[] data = new byte[0];
        try {
            data = uiml.getBytes("UTF-8");
            hash = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            System.err.println("Error creating hash: " + e.fillInStackTrace());
            e.printStackTrace();
        }



    }


    public void setLocalizeOutput(ArrayList<String> newOutput){
        localizeOutput.clear();
        localizeOutput.addAll(newOutput);
    }

    public void setReadScreenOutput(ArrayList<String> newOutput){
        readScreenOutput.clear();
        readScreenOutput.addAll(newOutput);
    }

    public void setFocusedDescriptions(ArrayList<String> newOutput){
        focusedDescriptions.clear();
        focusedDescriptions.addAll(newOutput);
    }


}
