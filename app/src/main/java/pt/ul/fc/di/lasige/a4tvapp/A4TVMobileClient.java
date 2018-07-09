package pt.ul.fc.di.lasige.a4tvapp;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class A4TVMobileClient extends Activity implements Runnable/*extends AsyncTask*/{

    static final int KEY_SPACE = 20;
    static final int KEY_UP = 38;
    static final int KEY_DOWN = 40;
    static final int KEY_LEFT = 37;
    static final int KEY_RIGHT = 39;
    static final int KEY_OK = 13;
    static final int KEY_PAUSE = 19;
    static final int KEY_0 = 48;
    static final int KEY_1 = 49;
    static final int KEY_2 = 50;
    static final int KEY_3 = 51;
    static final int KEY_4 = 52;
    static final int KEY_5 = 53;
    static final int KEY_6 = 54;
    static final int KEY_7 = 55;
    static final int KEY_8 = 56;
    static final int KEY_9 = 57;
    static final int KEY_RED = 403;
    static final int KEY_GREEN = 404;
    static final int KEY_YELLOW = 405;
    static final int KEY_BLUE = 406;
    static final int KEY_REWIND = 412;
    static final int KEY_STOP = 413;
    static final int KEY_PLAY = 415;
    static final int KEY_FAST_FWD = 417;
    static final int KEY_BACK = 461;


    //reading mode
    static final int BASIC = 1; //reads all elements
    static final int ADVANCED = 5; //reads all elements divided by blocks

    //interaction mode
    static final int STANDARD = 1; //all directions are permited for navigation
    static final int SEQUENTIAL = 2; //horizontal directions are for navigating within menus

    //focus mode
    static final int FOCUS_SIBLINGS = 1; //reads also the siblings as possible options
    static final int FOCUS_MAP = 2; //shows what you select for each directional key

    private String hostName;
    private int portNumber;
    private Socket kkSocket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;
    private Speaker speaker;
    private boolean connected = false;
    private UIMLParser uimlParser;
    private ArrayList<String> ttsQueue;
    private ArrayList<String> lastSentence;
    private boolean useTTS = false;
    private Context context;
    private int readingMode;
    private int focusMode;
    private int userType;
    private int interactionMode;
    private String orientation;
    private ArrayList<String> branchs;
    private boolean hasVideoPlayer = false;
    private MediaPlayer  mp;
    private A4TVUserInterfaceEventManager userInterfaceEventManager;
    private ArrayList<String> focusedDesc;
    private ArrayList<TVApplicationState> states;
    private int currentStateIndex = 0;


    String hardcodedMapIgModa[] = {"<uiml xmlns=\"http://docs.oasis-open.org/uiml/ns/uiml4.0\">" +
            "<peers>" +
            "<presentation base=\" HTML_4.01frameset_Harmonia_0.1 \"/>" +
            "</peers>" +
            "<interface>" +
            "<structure>" +
            "<part class=\"application\" id=\"a4tv_app\">" +
            "<part class=\"DIV\" id=\"rss_reader_view\">" +
            "<part class=\"DIV\" id=\"main_container\">" +
            "<part class=\"DIV\" id=\"channel-view\"/>" +
            "<part class=\"DIV\" id=\"feed-view\">" +
            "<part class=\"DIV\" id=\"active_item\">" +
            "<part class=\"DIV\" id=\"active_content\">" +
            "<part class=\"ARTICLE\" id=\"a4tv_ARTICLE_79\">" +
            "<part class=\"H2\" id=\"a4tv_H2_80\">" +
            "<part class=\"SPAN\" id=\"openItem\"/>" +
            "</part>" +
            "<part class=\"H6\" id=\"a4tv_H6_82\"/>" +
            "<part class=\"DIV\" id=\"a4tv_DIV_83\">" +
            "<part class=\"P\" id=\"a4tv_P_84\">" +
            "<part class=\"STRONG\" id=\"a4tv_STRONG_85\"/>" +
            "</part>" +
            "<part class=\"P\" id=\"selo-agencia\"/>" +
            "<part class=\"P\" id=\"a4tv_P_87\"/>" +
            "<part class=\"P\" id=\"a4tv_P_88\">" +
            "<part class=\"IMG\" id=\"a4tv_IMG_89\"/>" +
            "</part>" +
            "</part>" +
            "</part>" +
            "</part>" +
            "</part>" +
            "</part>" +
            "</part>" +
            "</part>" +
            "<part class=\"UL\" id=\"a4tv_UL_9\">" +
            "<part class=\"LI\" id=\"a4tv_LI_10\">" +
            "<part class=\"SPAN\" id=\"a4tv_SPAN_11\"/>" +
            "</part>" +
            "<part class=\"LI\" id=\"a4tv_LI_12\">" +
            "<part class=\"SPAN\" id=\"a4tv_SPAN_13\"/>" +
            "</part>" +
            "<part class=\"LI\" id=\"a4tv_LI_14\">" +
            "<part class=\"SPAN\" id=\"a4tv_SPAN_15\"/>" +
            "</part>" +
            "<part class=\"LI\" id=\"a4tv_LI_16\">" +
            "<part class=\"SPAN\" id=\"a4tv_SPAN_17\"/>" +
            "</part>" +
            "</part>" +
            "<part class=\"DIV\" id=\"item_list\">" +
            "<part class=\"DIV\" id=\"item_list_cnt\">" +
            "<part class=\"ARTICLE\" id=\"a4tv_ARTICLE_22\">" +
            "<part class=\"SECTION\" id=\"a4tv_SECTION_23\">" +
            "<part class=\"H5\" id=\"a4tv_H5_24\"/>" +
            "</part>" +
            "</part>" +
            "<part class=\"ARTICLE\" id=\"a4tv_ARTICLE_25\">" +
            "<part class=\"SECTION\" id=\"a4tv_SECTION_26\">" +
            "<part class=\"H5\" id=\"a4tv_H5_27\"/>" +
            "</part>" +
            "</part>" +
            "<part class=\"ARTICLE\" id=\"a4tv_ARTICLE_28\">" +
            "<part class=\"SECTION\" id=\"a4tv_SECTION_29\">" +
            "<part class=\"H5\" id=\"a4tv_H5_31\"/>" +
            "</part>" +
            "</part>" +
            "<part class=\"ARTICLE\" id=\"a4tv_ARTICLE_32\">" +
            "<part class=\"SECTION\" id=\"a4tv_SECTION_33\">" +
            "<part class=\"H5\" id=\"a4tv_H5_34\"/>" +
            "</part>" +
            "</part>" +
            "</part>" +
            "<part class=\"DIV\" id=\"item_list_indicator\"/>" +
            "<part class=\"DIV\" id=\"a4tv_DIV_75\"/>" +
            "<part class=\"DIV\" id=\"a4tv_DIV_76\"/>" +
            "</part>" +
            "<part class=\"DIV\" id=\"fps\"/>" +
            "</part>" +
            "</structure>" +
            "<style>" +
            "<property part-name=\"a4tv_LI_10\" name=\"target_down\">active_item</property>" +
            "<property part-name=\"a4tv_LI_10\" name=\"target_left\">a4tv_ARTICLE_22</property>" +
            "<property part-name=\"a4tv_LI_10\" name=\"target_right\">a4tv_LI_12</property>" +
            "<property part-name=\"a4tv_LI_12\" name=\"target_down\">active_item</property>" +
            "<property part-name=\"a4tv_LI_12\" name=\"target_left\">a4tv_LI_10</property>" +
            "<property part-name=\"a4tv_LI_12\" name=\"target_right\">a4tv_LI_14</property>" +
            "<property part-name=\"a4tv_LI_14\" name=\"target_down\">active_item</property>" +
            "<property part-name=\"a4tv_LI_14\" name=\"target_left\">a4tv_LI_12</property>" +
            "<property part-name=\"a4tv_LI_14\" name=\"target_right\">a4tv_LI_16</property>" +
            "<property part-name=\"a4tv_LI_16\" name=\"target_down\">active_item</property>" +
            "<property part-name=\"a4tv_LI_16\" name=\"target_left\">a4tv_LI_14</property>" +
            "<property part-name=\"active_item\" name=\"target_up\">a4tv_LI_12</property>" +
            "<property part-name=\"a4tv_ARTICLE_22\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_22\" name=\"target_down\">a4tv_ARTICLE_26</property>" +
            "<property part-name=\"a4tv_ARTICLE_22\" name=\"target_right\">active_item</property>" +
            "<property part-name=\"a4tv_ARTICLE_26\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_26\" name=\"target_up\">a4tv_ARTICLE_22</property>" +
            "<property part-name=\"a4tv_ARTICLE_26\" name=\"target_down\">a4tv_ARTICLE_30</property>" +
            "<property part-name=\"a4tv_ARTICLE_26\" name=\"target_right\">active_item</property>" +
            "<property part-name=\"a4tv_ARTICLE_30\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_30\" name=\"target_up\">a4tv_ARTICLE_26</property>" +
            "<property part-name=\"a4tv_ARTICLE_30\" name=\"target_down\">a4tv_ARTICLE_34</property>" +
            "<property part-name=\"a4tv_ARTICLE_30\" name=\"target_right\">active_item</property>" +
            "<property part-name=\"a4tv_ARTICLE_34\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_34\" name=\"target_up\">a4tv_ARTICLE_30</property>" +
            "<property part-name=\"a4tv_ARTICLE_34\" name=\"target_down\">a4tv_ARTICLE_38</property>" +
            "<property part-name=\"a4tv_ARTICLE_34\" name=\"target_right\">active_item</property>" +
            "<property part-name=\"a4tv_ARTICLE_38\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_38\" name=\"target_up\">a4tv_ARTICLE_34</property>" +
            "<property part-name=\"a4tv_ARTICLE_38\" name=\"target_down\">a4tv_ARTICLE_42</property>" +
            "<property part-name=\"a4tv_ARTICLE_38\" name=\"target_right\">active_item</property>" +
            "<property part-name=\"a4tv_ARTICLE_42\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_42\" name=\"target_up\">a4tv_ARTICLE_38</property>" +
            "<property part-name=\"a4tv_ARTICLE_42\" name=\"target_down\">a4tv_ARTICLE_46</property>" +
            "<property part-name=\"a4tv_ARTICLE_42\" name=\"target_right\">active_item</property>" +
            "<property part-name=\"a4tv_ARTICLE_46\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_46\" name=\"target_up\">a4tv_ARTICLE_42</property>" +
            "<property part-name=\"a4tv_ARTICLE_46\" name=\"target_down\">a4tv_ARTICLE_48</property>" +
            "<property part-name=\"a4tv_ARTICLE_46\" name=\"target_right\">active_item</property>" +
            "<property part-name=\"a4tv_ARTICLE_50\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_50\" name=\"target_up\">a4tv_ARTICLE_48</property>" +
            "<property part-name=\"a4tv_ARTICLE_50\" name=\"target_down\">a4tv_ARTICLE_54</property>" +
            "<property part-name=\"a4tv_ARTICLE_50\" name=\"target_right\">active_item</property>" +
            "<property part-name=\"a4tv_ARTICLE_54\" name=\"type\">ARTICLE</property>" +
            "<property part-name=\"a4tv_ARTICLE_54\" name=\"target_up\">a4tv_ARTICLE_50</property>" +
            "<property part-name=\"a4tv_ARTICLE_54\" name=\"target_down\">a4tv_ARTICLE_58</property>" +
            "<property part-name=\"a4tv_ARTICLE_54\" name=\"target_right\">active_item</property>" +
            "</style>" +
            "</interface>" +
            "</uiml>"};
    String hardcodedMapCocorico[] = {"<uiml xmlns=\"http://docs.oasis-open.org/uiml/ns/uiml4.0\">" +
            " <peers>" +
            "<presentation base=\" HTML_4.01frameset_Harmonia_0.1 \"/>" +
            " </peers>" +
            "<interface>" +
            "<structure>" +
            " <part class=\"application\" id=\"a4tv_app\">" +
            " <part class=\"DIV\" id=\"main\">" +
            " <part class=\"DIV\" id=\"select-view\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_2\"/>" +
            " <part class=\"DIV\" id=\"left-panel\">" +
            " <part class=\"DIV\" id=\"logo\"/>" +
            " <part class=\"DIV\" id=\"category-list-scroller\">" +
            " <part class=\"DIV\" id=\"category-list\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_7\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_8\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_9\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_10\"/>" +
            " </part>" +
            "</part>" +
            "</part>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_11\"/>" +
            " <part class=\"DIV\" id=\"info-panel\">" +
            " <part class=\"DIV\" id=\"info-panel-title\"/>" +
            " <part class=\"DIV\" id=\"info-panel-desc\"/>" +
            " </part>" +
            "<part class=\"DIV\" id=\"channel-panel\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_16\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_17\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_18\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_20\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_21\"/>" +
            " <part class=\"IMG\" id=\"a4tv_IMG_76\"/>" +
            " </part>" +
            "<part class=\"DIV\" id=\"a4tv_DIV_22\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_23\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_25\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_26\"/>" +
            " <part class=\"IMG\" id=\"a4tv_IMG_77\"/>" +
            " </part>" +
            "<part class=\"DIV\" id=\"a4tv_DIV_27\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_28\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_30\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_31\"/>" +
            " <part class=\"IMG\" id=\"a4tv_IMG_78\"/>" +
            " </part>" +
            "<part class=\"DIV\" id=\"a4tv_DIV_32\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_33\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_35\"/>" +
            " <part class=\"DIV\" id=\"a4tv_DIV_36\"/>" +
            " <part class=\"IMG\" id=\"a4tv_IMG_79\"/>" +
            " </part>" +
            "<part class=\"DIV\" id=\"a4tv_DIV_37\">" +
            " <part class=\"DIV\" id=\"a4tv_DIV_38\"/>" +
            " <part class=\"IMG\" id=\"a4tv_IMG_80\"/>" +
            " </part>" +
            "</part>" +
            "</part>" +
            "</part>" +
            " <part class=\"DIV\" id=\"notifications\"/>" +
            " <part class=\"VIDEO\" id=\"video\"/>" +
            " </part>" +
            "</part>" +
            "</structure>" +
            " <style>" +
            " <property part-name=\"a4tv_DIV_8\" name=\"target_up\">a4tv_DIV_11</property>" +
            " <property part-name=\"a4tv_DIV_8\" name=\"target_down\">a4tv_DIV_9</property>" +
            " <property part-name=\"a4tv_DIV_9\" name=\"target_up\">a4tv_DIV_8</property>" +
            " <property part-name=\"a4tv_DIV_9\" name=\"target_down\">a4tv_DIV_10</property>" +
            " <property part-name=\"a4tv_DIV_10\" name=\"target_up\">a4tv_DIV_9</property>" +
            " <property part-name=\"a4tv_DIV_10\" name=\"target_down\">a4tv_DIV_11</property>" +
            " <property part-name=\"a4tv_DIV_11\" name=\"target_up\">a4tv_DIV_10</property>" +
            " <property part-name=\"a4tv_DIV_11\" name=\"target_down\">a4tv_DIV_8</property>" +
            " <property part-name=\"a4tv_DIV_18\" name=\"target_right\">a4tv_DIV_24</property>" +
            " <property part-name=\"a4tv_DIV_22\" name=\"target_right\">a4tv_DIV_30</property>" +
            " <property part-name=\"a4tv_DIV_24\" name=\"target_right\">a4tv_DIV_30</property>" +
            " <property part-name=\"a4tv_DIV_24\" name=\"target_left\">a4tv_DIV_18</property>" +
            " <property part-name=\"a4tv_DIV_28\" name=\"target_right\">a4tv_DIV_36</property>" +
            " <property part-name=\"a4tv_DIV_30\" name=\"target_right\">a4tv_DIV_36</property>" +
            " <property part-name=\"a4tv_DIV_30\" name=\"target_left\">a4tv_DIV_24</property>" +
            " <property part-name=\"a4tv_DIV_34\" name=\"target_right\">a4tv_DIV_40</property>" +
            " <property part-name=\"a4tv_DIV_36\" name=\"target_right\">a4tv_DIV_40</property>" +
            " <property part-name=\"a4tv_DIV_36\" name=\"target_left\">a4tv_DIV_30</property>" +
            " <property part-name=\"a4tv_DIV_40\" name=\"target_right\">a4tv_DIV_46</property>" +
            " <property part-name=\"a4tv_DIV_42\" name=\"target_right\">a4tv_DIV_46</property>" +
            " <property part-name=\"a4tv_DIV_42\" name=\"target_left\">a4tv_DIV_36</property>" +
            " <property part-name=\"a4tv_DIV_46\" name=\"target_right\">a4tv_DIV_52</property>" +
            " <property part-name=\"a4tv_DIV_48\" name=\"target_right\">a4tv_DIV_52</property>" +
            " <property part-name=\"a4tv_DIV_48\" name=\"target_left\">a4tv_DIV_42</property>" +
            " <property part-name=\"a4tv_DIV_52\" name=\"target_right\">a4tv_DIV_58</property>" +
            " <property part-name=\"a4tv_DIV_54\" name=\"target_right\">a4tv_DIV_58</property>" +
            " <property part-name=\"a4tv_DIV_54\" name=\"target_left\">a4tv_DIV_48</property>" +
            " <property part-name=\"a4tv_DIV_58\" name=\"target_right\">a4tv_DIV_64</property>" +
            " <property part-name=\"a4tv_DIV_60\" name=\"target_right\">a4tv_DIV_64</property>" +
            " <property part-name=\"a4tv_DIV_60\" name=\"target_left\">a4tv_DIV_54</property>" +
            " <property part-name=\"a4tv_DIV_64\" name=\"target_right\">a4tv_DIV_70</property>" +
            " <property part-name=\"a4tv_DIV_66\" name=\"target_right\">a4tv_DIV_70</property>" +
            " <property part-name=\"a4tv_DIV_70\" name=\"target_right\">a4tv_DIV_76</property>" +
            " <property part-name=\"a4tv_DIV_72\" name=\"target_right\">a4tv_DIV_76</property>" +
            " <property part-name=\"a4tv_DIV_76\" name=\"target_right\">a4tv_DIV_82</property>" +
            " <property part-name=\"a4tv_DIV_78\" name=\"target_right\">a4tv_DIV_82</property>" +
            " <property part-name=\"a4tv_DIV_82\" name=\"target_right\">a4tv_DIV_88</property>" +
            " <property part-name=\"a4tv_DIV_84\" name=\"target_right\">a4tv_DIV_88</property>" +
            " <property part-name=\"a4tv_DIV_88\" name=\"target_right\">a4tv_DIV_94</property>" +
            " <property part-name=\"a4tv_DIV_90\" name=\"target_right\">a4tv_DIV_94</property>" +
            " <property part-name=\"a4tv_DIV_94\" name=\"target_right\">a4tv_DIV_100</property>" +
            " <property part-name=\"a4tv_DIV_96\" name=\"target_right\">a4tv_DIV_100</property>" +
            " <property part-name=\"a4tv_DIV_100\" name=\"target_right\">a4tv_DIV_106</property>" +
            " <property part-name=\"a4tv_DIV_102\" name=\"target_right\">a4tv_DIV_106</property>" +
            " <property part-name=\"a4tv_DIV_106\" name=\"target_right\">a4tv_DIV_112</property>" +
            " <property part-name=\"a4tv_DIV_108\" name=\"target_right\">a4tv_DIV_112</property>" +
            " <property part-name=\"a4tv_DIV_112\" name=\"target_right\">a4tv_DIV_118</property>" +
            " <property part-name=\"a4tv_DIV_114\" name=\"target_right\">a4tv_DIV_118</property>" +
            " <property part-name=\"a4tv_DIV_118\" name=\"target_right\">a4tv_DIV_124</property>" +
            " <property part-name=\"a4tv_DIV_120\" name=\"target_right\">a4tv_DIV_124</property>" +
            " <property part-name=\"a4tv_DIV_124\" name=\"target_right\">a4tv_DIV_128</property> " +
            " <property part-name=\"a4tv_DIV_126\" name=\"target_right\">a4tv_DIV_128</property> " +
            " <property part-name=\"full-screen-button-back\" name=\"target_right\">full-screen-button-pause</property> " +
            " <property part-name=\"full-screen-button-back\" name=\"target_left\">full-screen-button-next</property> " +
            " <property part-name=\"full-screen-button-back\" name=\"target_up\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-back\" name=\"target_down\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-pause\" name=\"target_left\">full-screen-button-back</property> " +
            " <property part-name=\"full-screen-button-pause\" name=\"target_right\">full-screen-button-show-info</property> " +
            " <property part-name=\"full-screen-button-pause\" name=\"target_up\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-pause\" name=\"target_down\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-show-info\" name=\"target_left\">full-screen-button-pause</property> " +
            " <property part-name=\"full-screen-button-show-info\" name=\"target_right\">full-screen-button-prev</property> " +
            " <property part-name=\"full-screen-button-show-info\" name=\"target_up\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-show-info\" name=\"target_down\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-prev\" name=\"target_left\">full-screen-button-show-info</property> " +
            " <property part-name=\"full-screen-button-prev\" name=\"target_right\">full-screen-category-index</property> " +
            " <property part-name=\"full-screen-button-prev\" name=\"target_up\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-prev\" name=\"target_down\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-category-index\" name=\"target_left\">full-screen-button-prev</property> " +
            " <property part-name=\"full-screen-category-index\" name=\"target_right\">full-screen-button-next</property> " +
            " <property part-name=\"full-screen-category-index\" name=\"target_up\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-category-index\" name=\"target_down\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-next\" name=\"target_left\">full-screen-category-index</property> " +
            " <property part-name=\"full-screen-button-next\" name=\"target_right\">full-screen-button-back</property> " +
            " <property part-name=\"full-screen-button-next\" name=\"target_up\">full-screen-progress-indicator</property> " +
            " <property part-name=\"full-screen-button-next\" name=\"target_down\">full-screen-progress-indicator</property> " +
            " </style>" +
            "</interface>" +
            "</uiml>",
            "<uiml xmlns=\"http://docs.oasis-open.org/uiml/ns/uiml4.0\">"  +
            "<peers>" +
            "<presentation base=\" HTML_4.01frameset_Harmonia_0.1 \"/>"  +
            " </peers>"  +
            "<interface>"  +
            "<structure>"  +
            " <part class=\"application\" id=\"a4tv_app\">"  +
            " <part class=\"DIV\" id=\"main\">"  +
            " <part class=\"DIV\" id=\"select-view\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_2\"/>"  +
            " <part class=\"DIV\" id=\"left-panel\">"  +
            " <part class=\"DIV\" id=\"logo\"/>"  +
            " <part class=\"DIV\" id=\"category-list-scroller\">"  +
            " <part class=\"DIV\" id=\"category-list\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_7\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_8\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_9\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_10\"/>"  +
            " </part>"  +
            "</part>"  +
            "</part>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_11\"/>"  +
            " <part class=\"DIV\" id=\"info-panel\">"  +
            " <part class=\"DIV\" id=\"info-panel-title\"/>"  +
            " <part class=\"DIV\" id=\"info-panel-desc\"/>"  +
            " </part>"  +
            "<part class=\"DIV\" id=\"channel-panel\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_16\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_17\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_18\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_20\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_21\"/>"  +
            " <part class=\"IMG\" id=\"a4tv_IMG_76\"/>"  +
            " </part>"  +
            "<part class=\"DIV\" id=\"a4tv_DIV_22\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_23\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_25\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_26\"/>"  +
            " <part class=\"IMG\" id=\"a4tv_IMG_77\"/>"  +
            " </part>"  +
            "<part class=\"DIV\" id=\"a4tv_DIV_27\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_28\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_30\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_31\"/>"  +
            " <part class=\"IMG\" id=\"a4tv_IMG_78\"/>"  +
            " </part>"  +
            "<part class=\"DIV\" id=\"a4tv_DIV_32\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_33\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_35\"/>"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_36\"/>"  +
            " <part class=\"IMG\" id=\"a4tv_IMG_79\"/>"  +
            " </part>"  +
            "<part class=\"DIV\" id=\"a4tv_DIV_37\">"  +
            " <part class=\"DIV\" id=\"a4tv_DIV_38\"/>"  +
            " <part class=\"IMG\" id=\"a4tv_IMG_80\"/>"  +
            " </part>"  +
            "</part>"  +
            "</part>"  +
            "</part>"  +
            " <part class=\"DIV\" id=\"notifications\"/>"  +
            " <part class=\"VIDEO\" id=\"video\"/>"  +
            " </part>"  +
            "</part>"  +
            "</structure>"  +
            " <style>"  +
            " <property part-name=\"a4tv_DIV_8\" name=\"target_up\">a4tv_DIV_11</property>"  +
            " <property part-name=\"a4tv_DIV_8\" name=\"target_down\">a4tv_DIV_7</property>"  +
            " <property part-name=\"a4tv_DIV_9\" name=\"target_up\">a4tv_DIV_8</property>"  +
            " <property part-name=\"a4tv_DIV_9\" name=\"target_down\">a4tv_DIV_10</property>"  +
            " <property part-name=\"a4tv_DIV_10\" name=\"target_up\">a4tv_DIV_9</property>"  +
            " <property part-name=\"a4tv_DIV_10\" name=\"target_down\">a4tv_DIV_11</property>"  +
            " <property part-name=\"a4tv_DIV_11\" name=\"target_up\">a4tv_DIV_10</property>"  +
            " <property part-name=\"a4tv_DIV_11\" name=\"target_down\">a4tv_DIV_8</property>"  +
            " <property part-name=\"a4tv_DIV_24\" name=\"target_right\">a4tv_DIV_28</property>" +
            " <property part-name=\"a4tv_DIV_28\" name=\"target_right\">a4tv_DIV_32</property>" +
            " </style>"  +
            "</interface>"  +
            "</uiml>"};


    public A4TVMobileClient(String hostName, int portNumber, boolean useTTS,int mode, Context context) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.useTTS = useTTS;
        this.context = context;
        uimlParser = new UIMLParser();
        readingMode = mode;
        branchs = new ArrayList();
        ttsQueue = new ArrayList<String>();
        focusedDesc = new ArrayList<String>();
        lastSentence = new ArrayList<String>();
        mp = MediaPlayer.create(context, R.raw.error_earcon);
        userInterfaceEventManager = new A4TVUserInterfaceEventManager(context);
        states = new ArrayList<TVApplicationState>();
    }


    public void setSpeaker(Speaker sp){
        speaker = sp;

    }

    public void setInteractMode(int mode){ interactionMode = mode;}

    public void setUseTTS(boolean val){
        useTTS = val;
    }

    public void setReadingMode(int rMode){
        readingMode = rMode;
    }

    public void setFocusMode(int fMode){
        focusMode = fMode;
    }

    public void setUserType(int uType){ userType = uType; }

    public boolean getHasVideoPlayer(){
        System.err.println("Has video player? " + hasVideoPlayer);

        return hasVideoPlayer;

    }

    public ArrayList<String> getFocusedDescriptions(){
        return focusedDesc;
    }

    public void toastThis(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(context , toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void writeOnText(final String msg)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                CopyOnWriteArrayList<String> copy = new CopyOnWriteArrayList<>(ttsQueue);//protect against ConcurrentModificationException

                A4TVMainActivity.contentText.setText(msg);

                for (String sentence: copy ) {
                    //System.err.println("Content label adding  " +  sentence);
                    A4TVMainActivity.contentText.append(sentence);

                }
               
                ttsQueue.clear();
            }
        });
    }

    public boolean isConnected(){
        return connected;
    }

    public boolean makeConnection(){

        try {
            kkSocket = new Socket(hostName, portNumber);
            out = new PrintWriter(kkSocket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(kkSocket.getInputStream(), "UTF-8"));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            if(useTTS)
                speaker.speak("A A4TV está ligada à sua Televisão.", TextToSpeech.QUEUE_FLUSH);
            else
                writeOnText("A A4TV está ligada à sua Televisão.");

            return true;
        } catch (UnknownHostException e) {
            System.err.println("Make Connection: Don't know about host " + hostName);


        } catch (IOException e) {
            System.err.println("Make Connection: Couldn't get I/O for the connection to " +
                    hostName);

        }
        if(useTTS)
            speaker.speak("A A4TV não conseguiu ligar-se à sua televisão.", TextToSpeech.QUEUE_FLUSH);
        else
            writeOnText("A A4TV não conseguiu ligar-se à sua televisão.");
        return false;
    }

    public void repeatLast(){
        readFocusedElements(true); //now repeat gives only the localization
        //sendToSpeaker();
    }

    public void stopSpeech(){
        interruptSpeech();//interrupt
        trd.interrupt();//stop thread
    }

    public void interruptSpeech(){
        if(useTTS)
            speaker.interruptSpeech();
    }

    /*@Override
    protected Object doInBackground(Object[] params) {*/

    @Override
    public void run() {

        boolean res = makeConnection();
        connected = res;
        if(connected) {
            try {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        A4TVMainActivity.showButtons();
                    }
                });
                String fromServer;
                String fromUser;
                connected = true;
                String lastMsg ="";

                while ((fromServer = in.readLine()) != null) {

                    if(lastMsg.compareTo(fromServer) == 0){
                        //lastMsg = fromServer;
                        stopSpeech();
                        System.err.println("Same msg!!!");
                        ArrayList<Action> actions = new ArrayList<Action>();
                        actions.addAll(states.get(currentStateIndex).getActions());
                        for (Action a : actions) {
                            userInterfaceEventManager.addAction("current_block_info", a._block_type, a._block_orientation, a._item_index, "-", a._current_level, a._interaction_mode);
                        }


                        if(useTTS) {
                            mp.start();
                        }



                    }else {
                        lastMsg = fromServer;
                        stopSpeech();
                        TVApplicationState newState = new TVApplicationState();
                        newState.setUIML(fromServer);
                        boolean hasFound = false;
                        int i = 0;
                        for(i = 0; i < states.size() ; i++){

                            if(states.get(i).getHash().compareTo(newState.getHash()) == 0){

                                hasFound = true;
                                break;
                            }
                        }

                        if(!hasFound) {
                            System.err.println("This state is new");
                            states.add(newState);
                            currentStateIndex = states.size()-1;
                        }else{
                            currentStateIndex = i;
                            System.err.println("This state already exists");
                        }

                        uimlParser.setUIML(states.get(currentStateIndex).getUIML());
                        readFocusedElements(false);
                        //sendToSpeaker();
                    }

                    if (fromServer.equals("Bye."))
                        break;

                }
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host " + hostName);

            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to " +
                        hostName);
            }
        }
        connected = false;
        runOnUiThread(new Runnable() {
            public void run()
            {
                A4TVMainActivity.uncheckConnectionButton();
            }
        });

        if(useTTS)
            speaker.speak("A A4TV desligou-se da sua televisão", TextToSpeech.QUEUE_FLUSH);
        else
            writeOnText("A A4TV desligou-se da sua televisão");

    }

    private String readElementsFromBlock(int blockId){

        ArrayList<String> elemId = uimlParser.getPartsWithClass("All");
        String blockString = "";
        String elemsBlockText = "";
        String orientation = uimlParser.getPropertyValueFromPartName("L" +blockId,"orientation");

        if(readingMode == A4TVMobileClient.BASIC)
            blockString = "Ínicio de Bloco. ";

        for (int temp = 0; temp < elemId.size(); temp++) {

            String bId =  uimlParser.getPropertyValueFromPartName(elemId.get(temp), "block");

            if(bId.compareTo("") != 0 && bId.compareTo("PAGE") != 0) {
                int id = Integer.parseInt(bId.split("L")[1]);
                if (blockId == id){
                    //get the description of the element
                    String text  = uimlParser.getDescription(elemId.get(temp));
                    //orientation = uimlParser.getPropertyValueFromPartName(elemId.get(temp), "orientation");
                    String isVisible = uimlParser.getPropertyValueFromPartName(elemId.get(temp), "visible");
                    if(text.compareTo("") != 0 && isVisible.compareTo("true") == 0 && !elemsBlockText.contains(text))
                        elemsBlockText += text + ". ";
                }
            }

        }

        if(elemsBlockText.trim().compareTo("") == 0) {
            blockString = "";
        }else {
            if(readingMode == A4TVMobileClient.BASIC) {
                blockString += orientation + ". " + elemsBlockText + " . Fim de Bloco. ";
            }else{
                blockString += elemsBlockText + " ";
            }
        }


        return blockString;
    }

    private void readFocusedElements(boolean userCommand){

        ttsQueue.clear();
        focusedDesc.clear();

        if(!uimlParser.isDocumentNull()) {

            if (currentStateIndex > states.size() && states.get(currentStateIndex).getLocalizeOutput().size() > 0) {
                System.err.println("State has a localize already.");
                ttsQueue.addAll(states.get(currentStateIndex).getLocalizeOutput());
                focusedDesc.addAll(states.get(currentStateIndex).getFocusedDescriptions());
                if(!userCommand) {
                    ArrayList<Action> actions = new ArrayList<Action>();
                    actions.addAll(states.get(currentStateIndex).getActions());
                    for (Action a : actions) {
                        userInterfaceEventManager.addAction("current_block_info", a._block_type, a._block_orientation, a._item_index, "-", a._current_level, a._interaction_mode);
                    }
                }

            } else {
                System.err.println("State does not have localize output.");
                ArrayList<String> elemId = uimlParser.getPartsWithClass("All");
                ArrayList<String> focused = new ArrayList<String>();
                hasVideoPlayer = false;
                orientation = "";

                for (int temp = 0; temp < elemId.size(); temp++) {

                    String isFocused = uimlParser.getPropertyValueFromPartName(elemId.get(temp), "focused");
                    String isVisible = uimlParser.getPropertyValueFromPartName(elemId.get(temp), "visible");
                    String hasVP = uimlParser.getPropertyValueFromPartName(elemId.get(temp), "type");

                    if (hasVP.compareTo("video/mp4") == 0) {
                        hasVideoPlayer = true;
                    }
                    //System.err.println("Focused: " + isFocused.compareTo("true"));
                    if (isFocused.compareTo("true") == 0 && isVisible.compareTo("true") == 0) {

                        //get the description of the focused element
                        String text = uimlParser.getDescription(elemId.get(temp));
                        String blockId = uimlParser.getPropertyValueFromPartName(elemId.get(temp), "block");
                        System.err.println("Focused element: " + elemId.get(temp));
                        System.err.println("Focused element text: " + text);
                        System.err.println("Associated block: " + blockId);
                        //get only the orientation of the first focused element if it exists more than one in the page
                        focused.add(elemId.get(temp));
                        ttsQueue.add(text + ". ");// group focused elements

                        //now get the description of the siblings of the focused element
                        int depthParent = Integer.parseInt(uimlParser.getPropertyValueFromPartName(elemId.get(temp), "depth")) - 1;//depth of parent
                        //String siblings = "";
                        ArrayList<String> siblings = new ArrayList<String>();
                        int count = 0;
                        int index = 0;
                        //find the elems with the same level as the parent
                        for (int i = 0; i < elemId.size(); i++) {
                            int depthElem = Integer.parseInt(uimlParser.getPropertyValueFromPartName(elemId.get(i), "depth"));

                            if (depthParent == depthElem) {
                                //get its children to find the siblings of the focused element
                                ArrayList<String> childs = uimlParser.getChildPartsWithId(elemId.get(i));
                                boolean areTheseSiblings = false;
                                for (String child : childs) {
                                    text = uimlParser.getDescription(child);//child description

                                    if (child == elemId.get(temp)) {
                                        areTheseSiblings = true;
                                        count++;
                                        index = count;
                                    } else {
                                    /*String orientationP = uimlParser.getPropertyValueFromPartName(child, "orientation");
                                    if(orientationP != "")
                                        orientation = orientationP;
                                    */

                                        count++;
                                        siblings.add(count + ". " + text + ". ");
                                    }
                                }
                                if (!areTheseSiblings) {
                                    siblings = new ArrayList<String>();
                                    count = 0;
                                    index = 0;
                                } else {
                                    break;
                                }

                            }
                        }


                        String contextS = index + " de " + count + ". ";

                        System.err.println("Getting info from block: " + blockId);
                        //get block where elem belongs
                        orientation = uimlParser.getPropertyValueFromPartName(blockId, "orientation");
                        String position = uimlParser.getPropertyValueFromPartName(blockId, "position");
                        System.err.println("Getting position: " + position);
                        String pos[] = position.split(";");

                        int posX = Integer.parseInt(pos[0]);
                        int posY = Integer.parseInt(pos[1]);
                        String dimensions = uimlParser.getPropertyValueFromPartName(blockId, "dimensions");
                        String block_type = uimlParser.getPropertyValueFromPartName(blockId, "type");
                        System.err.println("Getting Dimensions: " + dimensions);
                        int width = Integer.parseInt(dimensions.split(";")[0]);
                        int height = Integer.parseInt(dimensions.split(";")[1]);

                        //store info about the current focused element and block
                    /*String block_type = "-"; // types: menu, content, title

                    if(width >= 2*height || height >= 2*width){
                        //most likely a menu

                        if(hasVideoPlayer) //video control type
                            block_type = "control ";
                        else
                            block_type = "navigation "; //navigation type
                        if(count >= 2){ //probably a menu
                            block_type += "menu";
                        }else if(count == 1){
                            block_type += "single item menu";
                        }

                    }else if(count == 1){
                        //most likely content
                        if(ttsQueue.toString().length() > 80)
                            block_type = "content text";
                        else
                            block_type = "content title";

                    }else{
                        block_type = "unknown";
                    }
                    */

                        if(!userCommand) {
                            Action action = userInterfaceEventManager.addAction("current_block_info", block_type, orientation, index + "/" + count, "-", readingMode + "." + focusMode, interactionMode + "");
                            states.get(currentStateIndex).addAction(action);
                        }

                        if (readingMode == A4TVMobileClient.BASIC && focusMode == A4TVMobileClient.FOCUS_SIBLINGS) {

                            if (index > 0 && count > 1) {
                                if (orientation != "" && count > 1) {
                                    contextS += " orientação " + orientation + ". ";
                                }
                                ttsQueue.add(contextS);
                                ttsQueue.addAll(siblings);
                            }

                        } else {
                            if (index > 0 && count > 1) {
                                ttsQueue.add(contextS);
                            }
                        }
                    }
                }


                for (String id : focused) {
                    String text = uimlParser.getDescription(id);
                    focusedDesc.add(text);
                }

                if (readingMode == A4TVMobileClient.BASIC && focusMode == A4TVMobileClient.FOCUS_MAP) {
                    //map mode
                    UIMLParser uMap = new UIMLParser();

                    if (uimlParser.getPropertyValueFromPartName("a4tv_app", "title").compareTo("Universal RSS Reader") != 0) {
                        if (uimlParser.getPropertyValueFromPartName("a4tv_DIV_9", "focused").compareTo("true") != 0) {
                            uMap.setUIML(hardcodedMapCocorico[0]);
                            System.err.println("Using map 0");
                        } else {
                            uMap.setUIML(hardcodedMapCocorico[1]);
                            System.err.println("Using map 1");
                        }
                    } else {
                        uMap.setUIML(hardcodedMapIgModa[0]);
                    }

                    for (String id : focused) {

                        String idUp = uMap.getPropertyValueFromPartName(id, "target_up");
                        String navUp = uimlParser.getDescription(idUp);
                        String idDown = uMap.getPropertyValueFromPartName(id, "target_down");
                        String navDown = uimlParser.getDescription(idDown);
                        String idLeft = uMap.getPropertyValueFromPartName(id, "target_left");
                        String navLeft = uimlParser.getDescription(idLeft);
                        String idRight = uMap.getPropertyValueFromPartName(id, "target_right");
                        String navRight = uimlParser.getDescription(idRight);

                        if (navUp.compareTo("") != 0) {
                            if (navUp.length() >= 80)
                                navUp = "bloco de texto";
                            ttsQueue.add(" cima. " + navUp + ". ");
                        }
                        if (navDown.compareTo("") != 0) {
                            if (navDown.length() >= 80)
                                navDown = "bloco de texto";
                            ttsQueue.add(" baixo. " + navDown + ". ");
                        }
                        if (navLeft.compareTo("") != 0) {
                            if (navLeft.length() >= 80)
                                navLeft = "bloco de texto";
                            ttsQueue.add(" esquerda. " + navLeft + ". ");
                        }
                        if (navRight.compareTo("") != 0) {
                            if (navRight.length() >= 80)
                                navRight = "bloco de texto";
                            ttsQueue.add(" direita. " + navRight + ". ");
                        }

                    }

                }

                if (ttsQueue.size() > 0)
                    states.get(currentStateIndex).setLocalizeOutput(ttsQueue);
                else
                    ttsQueue.add("Sem informação.");
                if (currentStateIndex > states.size() && focusedDesc.size() > 0)
                    states.get(currentStateIndex).setFocusedDescriptions(focusedDesc);

            }
            if (ttsQueue.size() > 0)
                sendToSpeaker();
        }else{
            ttsQueue.add(" Não existe informação sobre a sua localização neste momento. ");
            sendToSpeaker();
        }
    }


    public void readScreen(){
        //System.err.println(screen);
        ttsQueue.clear();

        if(currentStateIndex < states.size() && states.get(currentStateIndex).getReadScreenOutput().size() > 0){
            ttsQueue.addAll(states.get(currentStateIndex).getReadScreenOutput());
        }else {

            if (!uimlParser.isDocumentNull()) {

                ArrayList<String> elemsId = uimlParser.getPartsWithClass("All");
                String value = "";
                ArrayList<Integer> blockIds = new ArrayList<Integer>();

                for (int temp = 0; temp < elemsId.size(); temp++) {

                    String bIds = uimlParser.getPropertyValueFromPartName(elemsId.get(temp), "block");
                    if (bIds.compareTo("") != 0 && bIds.compareTo("PAGE") != 0) {
                        int id = Integer.parseInt(bIds.split("L")[1]);
                        if (!blockIds.contains(id))
                            blockIds.add(id);
                    }

                }

                Collections.sort(blockIds);

                for (int i = 0; i < blockIds.size(); i++) {
                    //if(readingMode == A4TVMobileClient.BASIC)
                    System.out.println("Block: " + blockIds.get(i));

                    String b = readElementsFromBlock(blockIds.get(i));
                    if (b.compareTo("") != 0)
                        ttsQueue.add(b);

                }
                blockIds.clear();

                if (ttsQueue.size() > 0 && currentStateIndex > states.size()) {
                    states.get(currentStateIndex).setReadScreenOutput(ttsQueue);
                    //sendToSpeaker();
                }
            }else{
                ttsQueue.add("Não existe informação sobre a interface neste momento.");
                //sendToSpeaker();
            }
        }
        sendToSpeaker();
    }


    public Thread trd = new Thread() {

        private boolean inter = false;
        private CopyOnWriteArrayList<String> copy = new CopyOnWriteArrayList();

        public void run() {
            inter = false;
            int i = 0;
            copy = new CopyOnWriteArrayList<>(ttsQueue);//protect against ConcurrentModificationException
            lastSentence.clear(); //clears last sentence
            lastSentence.addAll(ttsQueue); // adds new sentence

            if(useTTS) {

                while (!trd.isInterrupted() && !inter && i < copy.size()) {
                    String sentence = copy.get(i);
                    if (useTTS) {
                        speaker.allow(true);

                        if (sentence.length() > 4000) {//max characters is 4000
                            String[] sentenceArray = sentence.split("\\.");
                            for (String sentenceS : sentenceArray) {
                                if (!trd.isInterrupted() && !inter) {
                                    speaker.speak(sentenceS + ".", TextToSpeech.QUEUE_FLUSH);
                                } else {
                                    break;
                                }
                            }
                        } else {
                            speaker.speak(sentence, TextToSpeech.QUEUE_FLUSH);

                        }
                    }

                    i++;
                }
                System.out.println("interrupted or finished");

            }else{
                    writeOnText("");
            }

            copy.clear();
            ttsQueue.clear();
        }

        public void interrupt(){
            if(useTTS)
                speaker.interruptSpeech();
            inter = true;
            ttsQueue.clear();
            copy.clear();
        }

    };

    public void speakThis(String sentence){
        ttsQueue.clear();
        ttsQueue.add(sentence);
        sendToSpeaker();
    }

    private void sendToSpeaker(){
                new Thread(new Runnable() {

                    public void run() {

                            trd.run();

                        }
                    }).start();
    }

    public void sendKeyboardInstructionToSTB(int cmd){

        if(out != null){

            System.out.println("Sending command for STB: " + cmd);
            out.println("keycode=" + cmd+ ";usertype=" + userType);
            out.flush();
        }
    }

    public void shutDownConnection(){
        try {
            kkSocket.close();
            out.close();
            in.close();
            stdIn.close();
            connected = false;
            if(useTTS)
                speaker.speak("A A4TV desligou-se da sua televisão", TextToSpeech.QUEUE_FLUSH);
            else
                writeOnText("A A4TV desligou-se da sua televisão");


        }catch(IOException e) {
            System.err.println("Couldn't shutdown connection");
        }

    }

}