package pt.ul.fc.di.lasige.a4tvapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;

import java.util.ArrayList;
import java.util.List;

public class A4TVMainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private final int CHECK_CODE = 0x1;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    private Speaker speaker;
    private A4TVMobileClient mobileClient;
    private A4TVAdaptationAndTutorialDialogs dialogs;
    private boolean firstTime = true;
    private String ipAdd = "192.168.1.7";
    private boolean useTTS = false;
    private int readingMode;
    private int interactMode;
    private int focusMode;
    private int gestureMode;
    private int userType;
    private float speechPitch;
    private float speechSpeed;
    public static TextView contentText;
    private Intent recognizerIntent;
    private SpeechRecognizer sr;
    private A4TVUserInterfaceEventManager userInterfaceEventManager;
    private int roll, yaw, pitch, oldPitch = 0;
    private float rollCorrection = 0, pitchCompensation = 0;
    private int normalizationPitch, normalizationYaw;
    private int counter = 0;
    private Pose _pose;
    private GestureDetector mGestureDetector;
    private View touch_view;
    private View.OnTouchListener touch;
    private Vibrator myVib;

    private static Button btnUP;
    private static Button btnDOWN;
    private static Button btnLEFT;
    private static Button btnRIGHT;
    private static Button btnOK;
    private static Button btnReadScreen;
    private static Button btnREPEAT;
    private static Button btnINTER;
    private static ToggleButton btnConnect;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
// If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            //showToast(getString(R.string.connected));
            if(useTTS)
                speaker.speak("Braçadeira conectada", TextToSpeech.QUEUE_FLUSH);
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            //showToast(getString(R.string.disconnected));
            if(useTTS)
                speaker.speak("Braçadeira desconectada", TextToSpeech.QUEUE_FLUSH);
        }

        // onPose() is called whenever the Myo detects that the person wearing it has changed their pose, for example,
        // making a fist, or not making a fist anymore.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            float relativeRoll = (roll - rollCorrection);
            _pose = pose;
            myo.notifyUserAction();
            if(myo.getArm() == Arm.LEFT && gestureMode == 0)
            {
                if(pose == Pose.WAVE_IN)
                    pose = Pose.WAVE_OUT;
                else if(pose == Pose.WAVE_OUT)
                    pose = Pose.WAVE_IN;
            }
            System.err.println(" Gesture mode " + gestureMode);
            switch (pose) {
                case WAVE_IN:
                    if (gestureMode == 0) {
                        System.err.println("Wave in roll:" + relativeRoll + "pitch compensation: " + pitchCompensation + " pitch: " + +pitch);
                        if ((relativeRoll - pitchCompensation) == 0) {
                            System.err.println("Swipe Left");
                            userInterfaceEventManager.addAction("left", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                            sendKeyboardInstruction(A4TVMobileClient.KEY_LEFT);
                        } else if ((relativeRoll - pitchCompensation) == 1) {
                            System.err.println("Swipe Down");
                            userInterfaceEventManager.addAction("down", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                            sendKeyboardInstruction(A4TVMobileClient.KEY_DOWN);
                        }
                    } else if (gestureMode == 1) {
                        userInterfaceEventManager.addAction("localize", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                        localize();
                    }
                    break;
                case WAVE_OUT:
                    if (gestureMode == 0) {
                        System.err.println("Wave out roll:" + relativeRoll + "pitch compensation: " + pitchCompensation + " pitch: " + +pitch);
                        if ((relativeRoll + pitchCompensation) == 0) {
                            System.err.println("Swipe Right");
                            userInterfaceEventManager.addAction("right", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                            sendKeyboardInstruction(A4TVMobileClient.KEY_RIGHT);
                        } else if ((relativeRoll + pitchCompensation) == 1) {
                            System.err.println("Swipe Up");
                            userInterfaceEventManager.addAction("up", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                            sendKeyboardInstruction(A4TVMobileClient.KEY_UP);
                        }
                    }else if (gestureMode == 1) {
                        userInterfaceEventManager.addAction("read_screen", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                        readScreen();
                    }
                    break;
                case DOUBLE_TAP:
                    System.err.println("Confirmation");
                    userInterfaceEventManager.addAction("ok", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                    sendKeyboardInstruction(A4TVMobileClient.KEY_OK);
                    break;
                case FIST:
                    if (gestureMode == 0) {
                        userInterfaceEventManager.addAction("localize", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                        localize();
                    } else if (gestureMode == 1) {
                        normalizationPitch = pitch;
                        normalizationYaw = yaw;
                    }
                    break;
                case FINGERS_SPREAD:
                    if (gestureMode == 0) {
                        if (recognizerIntent != null) {
                            userInterfaceEventManager.addAction("start_speech", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                            sr.startListening(recognizerIntent);
                        }
                    } else if (gestureMode == 1) {
                        if (recognizerIntent != null) {
                            userInterfaceEventManager.addAction("start_speech", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                            sr.startListening(recognizerIntent);
                        }

                    }
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {

            float _roll = (float) Math.atan2(2.0f * (rotation.w() * rotation.x() + rotation.y() * rotation.z()),
                    1.0f - 2.0f * (rotation.x() * rotation.x() + rotation.y() * rotation.y()));
            float _pitch = (float) Math.asin(Math.max(-1.0f, Math.min(1.0f, 2.0f * (rotation.w() * rotation.y() - rotation.z() * rotation.x()))));
            float _yaw = (float) Math.atan2(2.0f * (rotation.w() * rotation.z() + rotation.x() * rotation.y()),
                    1.0f - 2.0f * (rotation.y() * rotation.y() + rotation.z() * rotation.z()));


            roll = (int) ((_roll + (float) Math.PI) / (Math.PI * 2.0f) * 18);
            pitch = (int) ((_pitch + (float) Math.PI / 2.0f) / Math.PI * 18);
            yaw = (int) ((_yaw + (float) Math.PI) / (Math.PI * 2.0f) * 18);

            //System.err.println("X: "+rotation.x()+" Y: "+rotation.y()+" Z: "+rotation.z());
            //System.err.println("Roll: "+roll+" Pitch: "+pitch+" Yaw: "+yaw);
            if (gestureMode == 0) {
                if (Math.abs(oldPitch - pitch) > 0) {
                    oldPitch = pitch;
                    pitchCompensation = 0;


                    if (pitch <= 9)
                        pitchCompensation = 0;
                    else if (pitch <= 12)
                        pitchCompensation = 1;

                    rollCorrection = roll;
                    System.err.println("RollCorrection updated: " + rollCorrection + " pitch changed: " + pitch);
                }
            } else if (gestureMode == 1) {
                counter++;
                int diffPitch = (pitch - normalizationPitch);
                int diffYaw = (yaw - normalizationYaw);
                if (_pose == Pose.FIST && counter >= 50) {

                    if (diffPitch > 0) {
                        System.err.println("Swipe Up");
                        userInterfaceEventManager.addAction("up", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                        sendKeyboardInstruction(A4TVMobileClient.KEY_UP);
                    } else if (diffPitch < 0) {
                        System.err.println("Swipe Down");
                        userInterfaceEventManager.addAction("down", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                        sendKeyboardInstruction(A4TVMobileClient.KEY_DOWN);
                    } else if (diffYaw > 0) {
                        System.err.println("Swipe Left");
                        userInterfaceEventManager.addAction("left", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                        sendKeyboardInstruction(A4TVMobileClient.KEY_LEFT);
                    } else if (diffYaw < 0) {
                        System.err.println("Swipe Right");
                        userInterfaceEventManager.addAction("right", "-", "-", "-", "mid_air_gesture", readingMode + "." + focusMode, interactMode + "");
                        sendKeyboardInstruction(A4TVMobileClient.KEY_RIGHT);
                    }

                    counter = 0;


                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a4_tvmain);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content);
        contentText = (TextView)findViewById(R.id.contentText);

        //Remote Control Buttons
        btnUP = (Button)findViewById(R.id.btnUP);
        btnUP.setOnClickListener(this);
        btnUP.setOnTouchListener(this);
        btnDOWN = (Button)findViewById(R.id.btnDOWN);
        btnDOWN.setOnClickListener(this);
        btnDOWN.setOnTouchListener(this);
        btnLEFT = (Button)findViewById(R.id.btnLEFT);
        btnLEFT.setOnClickListener(this);
        btnLEFT.setOnTouchListener(this);
        btnRIGHT = (Button)findViewById(R.id.btnRIGHT);
        btnRIGHT.setOnClickListener(this);
        btnRIGHT.setOnTouchListener(this);
        btnOK = (Button)findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
        btnOK.setOnTouchListener(this);
        btnReadScreen = (Button)findViewById(R.id.btnReadScreen);
        btnReadScreen.setOnClickListener(this);
        btnReadScreen.setOnTouchListener(this);
        btnREPEAT = (Button)findViewById(R.id.btnREPEAT);
        btnREPEAT.setOnClickListener(this);
        btnREPEAT.setOnTouchListener(this);
        btnINTER = (Button)findViewById(R.id.btnSpeech);
        btnINTER.setOnClickListener(this);
        btnINTER.setOnTouchListener(this);

        hideButtons();

        //Connection button
        btnConnect = (ToggleButton)findViewById(R.id.btnConnect);
        btnConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    userInterfaceEventManager.addAction("connected to tv", "-", "-" , "-" ,"button", readingMode+"."+focusMode, interactMode+"");
                    // The toggle is enabled
                    connect();
                } else {
                    // The toggle is disabled
                    userInterfaceEventManager.addAction("disconnected from tv","-", "-" , "-" , "button", readingMode+"."+focusMode, interactMode+"");
                    disconnect();
                }
            }
        });
        btnConnect.setOnTouchListener(this);
        //Speech recognizer
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {

            sr = SpeechRecognizer.createSpeechRecognizer(this);
            SpeechRecognitionListener srListener = new SpeechRecognitionListener();
            sr.setRecognitionListener(srListener);

            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");

            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);



        }else{

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

        }

        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        //myo stuff
        Hub hub = Hub.getInstance();
        if(!hub.init(this, getPackageName())) {
            //sdasdsd
        }else {
            // Disable standard Myo locking policy. All poses will be delivered.
            hub.setLockingPolicy(Hub.LockingPolicy.NONE);

            // Next, register for DeviceListener callbacks.
            hub.addListener(mListener);

            // Finally, scan for Myo devices and connect to the first one found that is very near.
            hub.attachToAdjacentMyo();
        }

        //touch screen gestures
        // Create an object of our Custom Gesture Detector Class
        A4TVGestureListener customGestureDetector = new A4TVGestureListener(){
            public void onSwipeRight() {
                System.err.println("Swipe Right");
                userInterfaceEventManager.addAction("right","-", "-" , "-" , "screen_gesture", readingMode+"."+focusMode, interactMode+"");
                sendKeyboardInstruction(A4TVMobileClient.KEY_RIGHT);

            }

            public void onSwipeLeft() {
                System.err.println("Swipe Left");
                userInterfaceEventManager.addAction("left", "-", "-" , "-" ,"screen_gesture", readingMode+"."+focusMode, interactMode+"");
                sendKeyboardInstruction(A4TVMobileClient.KEY_LEFT);
            }

            public void onSwipeTop() {
                System.err.println("Swipe Up");
                userInterfaceEventManager.addAction("up","-", "-" , "-" , "screen_gesture", readingMode+"."+focusMode, interactMode+"");
                sendKeyboardInstruction(A4TVMobileClient.KEY_UP);

            }

            public void onSwipeBottom() {
                System.err.println("Swipe Down");
                userInterfaceEventManager.addAction("down","-", "-" , "-" , "screen_gesture", readingMode+"."+focusMode, interactMode+"");
                sendKeyboardInstruction(A4TVMobileClient.KEY_DOWN);
            }

            public  void onDoubletap(){
                System.err.println("Confirmation");
                userInterfaceEventManager.addAction("ok", "-", "-" , "-" ,"screen_gesture", readingMode+"."+focusMode, interactMode+"");
                sendKeyboardInstruction(A4TVMobileClient.KEY_OK);
            }

            public void holdingDown(){
                System.err.println("Holding down");
                if(recognizerIntent != null) {
                    userInterfaceEventManager.addAction("start speech", "-", "-" , "-" ,"screen_gesture", readingMode+"."+focusMode, interactMode+"");
                    sr.startListening(recognizerIntent);
                }
            }

            public void onSingleTap(){
                System.err.println("SingleTap");
                if(recognizerIntent != null) {
                    userInterfaceEventManager.addAction("localize", "-", "-" , "-" ,"screen_gesture", readingMode+"."+focusMode, interactMode+"");
                    localize();
                }
            }

            public void onScrollDown(){
                System.err.println("ScrollDown");
                /*
                if(recognizerIntent != null) {
                    userInterfaceEventManager.addAction("read_screen", "-", "-" , "-" ,"screen_gesture", readingMode+"."+focusMode, interactMode+"");
                    readScreen();
                }*/
            }
        };
        // Create a GestureDetector
        mGestureDetector = new GestureDetector(this, customGestureDetector);
        // Attach listeners that'll be called for double-tap and related gestures
        mGestureDetector.setOnDoubleTapListener(customGestureDetector);
        touch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }

        };
        touch_view = (View)findViewById(R.id.touch_view_tests); //before touchView
        touch_view.setOnTouchListener(touch);



        ToggleButton modeBtn = (ToggleButton)findViewById(R.id.modeBtn);
        modeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //NO LONGER NEEDED
            }
        });


        checkTTS();
        //start ui event manager
        userInterfaceEventManager = new A4TVUserInterfaceEventManager(this);
        //start dialog manager
        dialogs = new A4TVAdaptationAndTutorialDialogs(this);

        initiateServer();



        if(!userInterfaceEventManager.hasUserDoneTutorial()) {
            //run tutorial
            dialogs.startTutorial();
            userInterfaceEventManager.addAction("begin_tutorial", "-", "-" , "-" ,"none", readingMode + "." + focusMode, interactMode + "");
        }

        int count = userInterfaceEventManager.getIrrelevantActionsPattern();
        int count2 = userInterfaceEventManager.getLostAwarenessPattern();

        int count3 = userInterfaceEventManager.getReOccurencePattern("localize");
        int count4 = userInterfaceEventManager.getReOccurencePattern("read_screen");
        int count5 = userInterfaceEventManager.getReOccurencePattern("up");
        int count6 = userInterfaceEventManager.getReOccurencePattern("down");
        int count7 = userInterfaceEventManager.getReOccurencePattern("left");
        int count8 = userInterfaceEventManager.getReOccurencePattern("right");
        int count9 = userInterfaceEventManager.getReOccurencePattern("ok");

        int count10 = userInterfaceEventManager.getQuickVerticalnavigationPattern();
        int count11 = userInterfaceEventManager.getQuickHorizontalNavigationPattern();

        System.out.println("Irrelevant action patterns: " + count);
        System.out.println("LostAwareness patterns: " + count2);
        System.out.println("ReOccurence patterns " +
                            " Localize: " + count3 +
                            " Read Screen: " + count4 +
                            " Up: " + count5 +
                            " Down: " + count6 +
                            " Left: " + count7 +
                            " Right: " + count8 +
                            " OK: " + count9);

        System.out.println("VerticalMovements patterns: " + count10 + " HorizontalMovements patterns: " + count11 );

        /*if(count > 0)
            dialogs.createAdaptationDialog("Sugestão", "A aplicação detectou que já fez " + count + " acções irrelevantes. Deseja passar para o modo detalhado?", "reading_preference", "2").show();
        if(count2 > 0)
            dialogs.createAdaptationDialog("Sugestão", "A aplicação detectou que já se perdeu " + count2 + " vezes. Deseja passar para o modo detalhado?", "reading_preference", "2").show();
        */

        //showAllActionsOnConsole();

        //save actions to file Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        /*if(permissionWriteCheck == PackageManager.PERMISSION_GRANTED) {

            userInterfaceEventManager.storeAllActionsOnCSV(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/A4TV");
        }*/

        Thread probe = new Thread(new DiscoverSTBAddress(this));
        probe.start();
    }



    private void deleteStorage(){
        System.err.println( "Deleting storage ... ");
        userInterfaceEventManager.deleteStorage();

    }


    private void testStorage(){
        System.err.println( "Testing storage module... ");
        System.err.println( "Getting all stored actions count: " + userInterfaceEventManager.getAllActionsCount());
        System.err.println( "Getting specific action (e.g. right) count: " + userInterfaceEventManager.getActionCount("right"));
        System.err.println( "Getting action count from a specific level (e.g. 1.*): " + userInterfaceEventManager.getActionCountByLevel("1."));

        if(userInterfaceEventManager.isUserExperiencedWithVerbose() && readingMode == 1) {
            dialogs.createAdaptationDialog("Sugestão", "A aplicação detectou que o utilizador já tem alguma experiência, deseja passar para o modo conciso?", "reading_preference", "2").show();
        }

        if(userInterfaceEventManager.getIrrelevantActionsPattern() > 6 && readingMode == 2) {
            dialogs.createAdaptationDialog("Sugestão", "A aplicação detectou que está enviar comandos que não produzem efeito, deseja passar para o modo detalhado?", "reading_preference", "1").show();
        }

        System.err.println( "Fetching all actions' information");
        List<Action> actionsList = new ArrayList<Action>();
        actionsList = userInterfaceEventManager.getAllActions();
        for (Action a: actionsList) {
            System.err.println( "id: " + a._id + " desc: " + a._description + " interaction mode: " + a._interaction_mode + " level: " + a._current_level + " modality: " + a._modality + " date: " + a._date);
        }
        System.err.println( "---------------------------------------------------------------------------");
        System.err.println( "Fetching specific actions (e.g. ok)");
        actionsList = new ArrayList<Action>();
        actionsList = userInterfaceEventManager.getSpecificActions("ok");
        for (Action a: actionsList) {
            System.err.println( "id: " + a._id + " desc: " + a._description + " interaction mode: " + a._interaction_mode + " level: " + a._current_level + " modality: " + a._modality + " date: " + a._date);
        }
        System.err.println( "---------------------------------------------------------------------------");
        System.err.println( "Fetching actions by level (e.g. 2.2)");
        actionsList = new ArrayList<Action>();
        actionsList = userInterfaceEventManager.getActionsByLevel("2.2");
        for (Action a: actionsList) {
            System.err.println( "id: " + a._id + " desc: " + a._description + " interaction mode: " + a._interaction_mode + " level: " + a._current_level + " modality: " + a._modality + " date: " + a._date);
        }


    }



    private void readScreen(){
        if(mobileClient != null && mobileClient.isConnected()) {
            myVib.vibrate(200);
            mobileClient.stopSpeech();
            mobileClient.readScreen();
        }
    }

    private void localize(){
        if(mobileClient != null && mobileClient.isConnected()) {
            myVib.vibrate(200);
            mobileClient.stopSpeech();
            mobileClient.repeatLast();
        }
    }

    private void interrupt(){
        if(mobileClient != null && mobileClient.isConnected())
            myVib.vibrate(200);
            //mobileClient.stopSpeech();
            mobileClient.interruptSpeech();
    }

    private void sendKeyboardInstruction(int keycode){
        if(mobileClient != null && mobileClient.isConnected()) {
            myVib.vibrate(200);
            mobileClient.stopSpeech();

            if(mobileClient.getHasVideoPlayer()) {
                mobileClient.sendKeyboardInstructionToSTB(A4TVMobileClient.KEY_SPACE);
            }

            mobileClient.sendKeyboardInstructionToSTB(keycode);

        }

    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(A4TVMainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_a4_tvmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //startActivity(new Intent(this,Settings.class));
            Intent i = new Intent(this, Settings.class);
            startActivityForResult(i, 100);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnUP: sendKeyboardInstruction(A4TVMobileClient.KEY_UP);
                userInterfaceEventManager.addAction("up", "-", "-" , "-" ,"button", readingMode+"."+focusMode, interactMode+""); break;
            case R.id.btnDOWN: sendKeyboardInstruction(A4TVMobileClient.KEY_DOWN);
                userInterfaceEventManager.addAction("down", "-", "-" , "-" ,"button", readingMode+"."+focusMode, interactMode+""); break;
            case R.id.btnLEFT: sendKeyboardInstruction(A4TVMobileClient.KEY_LEFT);
                userInterfaceEventManager.addAction("left", "-", "-" , "-" ,"button", readingMode+"."+focusMode, interactMode+""); break;
            case R.id.btnRIGHT: sendKeyboardInstruction(A4TVMobileClient.KEY_RIGHT);
                userInterfaceEventManager.addAction("right", "-", "-" , "-" ,"button", readingMode+"."+focusMode, interactMode+""); break;
            case R.id.btnOK: sendKeyboardInstruction(A4TVMobileClient.KEY_OK);
                userInterfaceEventManager.addAction("ok", "-", "-" , "-" ,"button", readingMode+"."+focusMode, interactMode+""); break;
            case R.id.btnREPEAT:
                userInterfaceEventManager.addAction("localize", "-", "-" , "-" ,"button", readingMode+"."+focusMode, interactMode+"");
                localize(); break;
            case R.id.btnSpeech:
                interrupt();
                if(recognizerIntent != null) {
                    userInterfaceEventManager.addAction("start_speech", "-", "-" , "-" ,"button", readingMode+"."+focusMode, interactMode+"");
                    sr.startListening(recognizerIntent);
                }
                break;
            case R.id.btnReadScreen:
                userInterfaceEventManager.addAction("read_screen","-", "-" , "-" , "button", readingMode+"."+focusMode, interactMode+"");
                readScreen(); break;
                       //.... etc
        }
    }

    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    private void initiateServer(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPrefs.getBoolean("delete_storage", false)){
            deleteStorage();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean("delete_storage", false);
            editor.commit();

        }else {
            getUserPreferences();
        }


    }

    private void getUserPreferences(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        useTTS = sharedPrefs.getBoolean("TTS Preference Source", false);
        readingMode = Integer.parseInt(sharedPrefs.getString("reading_preference", "1"));
        interactMode = Integer.parseInt(sharedPrefs.getString("interact_preference", "1"));
        focusMode = Integer.parseInt(sharedPrefs.getString("focus_preference", "1"));
        userType = Integer.parseInt(sharedPrefs.getString("user_type", "1"));
        speechPitch = Float.parseFloat(sharedPrefs.getString("configure_pitch", "1.0"));
        speechSpeed = Float.parseFloat(sharedPrefs.getString("configure_speed", "1.0"));
        gestureMode = Integer.parseInt(sharedPrefs.getString("gesture_preference", "0"));
        ipAdd = sharedPrefs.getString("Set-Top Box IP address", ipAdd);
    }

    public static void showButtons(){
        btnUP.setVisibility(View.VISIBLE);
        //btnUP.requestLayout();
        btnDOWN.setVisibility(View.VISIBLE);
        //btnDOWN.requestLayout();
        btnLEFT.setVisibility(View.VISIBLE);
        //btnLEFT.requestLayout();
        btnRIGHT.setVisibility(View.VISIBLE);
        //btnRIGHT.requestLayout();
        btnOK.setVisibility(View.VISIBLE);
        //btnOK.requestLayout();
        btnReadScreen.setVisibility(View.VISIBLE);
        //btnReadScreen.requestLayout();
        btnREPEAT.setVisibility(View.VISIBLE);
        //btnREPEAT.requestLayout();
        btnINTER.setVisibility(View.VISIBLE);
        //btnINTER.requestLayout();
    }

    public static void hideButtons(){
        btnUP.setVisibility(View.GONE);
        btnUP.requestLayout();
        btnDOWN.setVisibility(View.GONE);
        btnDOWN.requestLayout();
        btnLEFT.setVisibility(View.GONE);
        btnLEFT.requestLayout();
        btnRIGHT.setVisibility(View.GONE);
        btnRIGHT.requestLayout();
        btnOK.setVisibility(View.GONE);
        btnOK.requestLayout();
        btnReadScreen.setVisibility(View.GONE);
        btnReadScreen.requestLayout();
        btnREPEAT.setVisibility(View.GONE);
        btnREPEAT.requestLayout();
        btnINTER.setVisibility(View.GONE);
        btnINTER.requestLayout();
    }

    private void connect(){
        myVib.vibrate(200);
        getUserPreferences();

        if(firstTime) {

            mobileClient = new A4TVMobileClient(ipAdd, 4444, useTTS, readingMode, this);
            updateMobileClientWithUserPreferences();

            if(useTTS) {
                mobileClient.setSpeaker(speaker);
            }
            firstTime = false;
            Thread cT = new Thread(mobileClient);
            cT.start();

        }else{
            if (!mobileClient.isConnected()) {
                updateMobileClientWithUserPreferences();
                Thread cT = new Thread(mobileClient);
                cT.start();

            }
        }

        if(mobileClient.isConnected()){
            showButtons();
        }
    }

    private void disconnect(){
        myVib.vibrate(200);
        userInterfaceEventManager.storeAllActionsOnCSV(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/A4TV");
        if(mobileClient.isConnected()) {
            mobileClient.shutDownConnection();

        }

        uncheckConnectionButton();


    }

    public static void uncheckConnectionButton(){
        if(btnConnect.isChecked())
            btnConnect.setChecked(false);

        hideButtons();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){

                if(useTTS) {
                    speaker = new Speaker(this);
                    speaker.allow(true);
                    speaker.setPitch(speechPitch);
                    speaker.setSpeed(speechSpeed);
                    useTTS = true;
                    //speaker.speak("Benvindo à A4TV. Por favor carregue no botão Ligar para começar.", TextToSpeech.QUEUE_FLUSH);
                }else {
                    //Toast.makeText(this, "Benvindo à A4TV. Por favor carregue no botão Ligar para começar.", Toast.LENGTH_LONG).show();
                }

            }else{

                /*Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);*/

            }
        }else if(resultCode == RESULT_OK) {
            System.err.println( "RESULT OK ");
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            if(sharedPrefs.getBoolean("delete_storage", false)){
                deleteStorage();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("delete_storage", false);
                editor.commit();

            }else {
                getUserPreferences();
            }

            updateMobileClientWithUserPreferences();

        }
    }

    private void updateMobileClientWithUserPreferences(){
        if(mobileClient != null) {
            mobileClient.setUseTTS(useTTS);
            mobileClient.setReadingMode(readingMode);
            mobileClient.setInteractMode(interactMode);
            mobileClient.setFocusMode(focusMode);
            mobileClient.setUserType(userType);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                userInterfaceEventManager.storeAllActionsOnCSV(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/A4TV");
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        userInterfaceEventManager.storeAllActionsOnCSV(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/A4TV");
        if(speaker != null)
            speaker.destroy();
        if(sr != null) {
            sr.stopListening();
            sr.destroy();
        }
        Hub.getInstance().removeListener(mListener);
        Hub.getInstance().shutdown();

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        System.err.println(  "TOUCH EVENT ON VIEW: " +view.getId() );
        switch (view.getId()) {
            case R.id.btnConnect:
            case R.id.btnUP:
            case R.id.btnDOWN:
            case R.id.btnLEFT:
            case R.id.btnRIGHT:
            case R.id.btnOK:
            case R.id.btnREPEAT:
            case R.id.btnSpeech:
            case R.id.btnReadScreen:touch.onTouch(touch_view, motionEvent);System.err.println(  "Returned false"); return false;
            default: System.err.println(  "Returned true"); return true;
            //.... etc

        }
        //return true;
    }

    class SpeechRecognitionListener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            System.err.println( "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            System.err.println(  "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            System.err.println(  "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            System.err.println(  "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            System.err.println(  "onEndofSpeech");
        }
        public void onError(int errorCode)
        {
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    System.err.println("Erro no audio.");
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    System.err.println("Erro no cliente.");
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    System.err.println("Erro de permissões.");
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    System.err.println("Erro de rede.");
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    System.err.println("Timedout");
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    System.err.println("No match");
                    if(mobileClient != null)
                        mobileClient.speakThis("Comando inexistente.");
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    System.err.println("Reconhecedor ocupado");
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    System.err.println("Erro no servidor");
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    System.err.println("Speech time out");
                    if(mobileClient != null)
                        mobileClient.speakThis("Não ouvi nenhum comando.");
                    break;
                default:
                    System.err.println("Não percebeu.");
                    if(mobileClient != null)
                        mobileClient.speakThis("Não percebi o comando.");
                    break;
            }

        }
        public void onResults(Bundle results) {
            String str = new String();
            System.err.println("onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            boolean shouldBreak = false;

            for (int i = 0; i < data.size(); i++) {
                System.err.println("result " + data.get(i));

                if (mobileClient != null) {
                    ArrayList<String> focusedDesc = mobileClient.getFocusedDescriptions();
                    for (int j = 0; j < focusedDesc.size(); j++) {
                        String recognized = ((String) data.get(i)).toLowerCase();
                        String focusedElem = focusedDesc.get(j).toLowerCase();
                        System.err.println("Is this command? " + recognized + " = " + focusedElem);
                        if (focusedElem.contains(recognized)) {
                            sendKeyboardInstruction(A4TVMobileClient.KEY_OK);
                            shouldBreak = true;
                            userInterfaceEventManager.addAction("ok", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                            break;
                        }
                    }

                }

                if (shouldBreak)
                    break;

                switch ((String) data.get(i)) {
                    case "ok":
                        sendKeyboardInstruction(A4TVMobileClient.KEY_OK);
                        shouldBreak = true;
                        userInterfaceEventManager.addAction("ok", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        break;
                    case "cima":
                        sendKeyboardInstruction(A4TVMobileClient.KEY_UP);
                        shouldBreak = true;
                        userInterfaceEventManager.addAction("up", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        break;
                    case "baixo":
                        sendKeyboardInstruction(A4TVMobileClient.KEY_DOWN);
                        shouldBreak = true;
                        userInterfaceEventManager.addAction("down", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        break;
                    case "esquerda":
                        sendKeyboardInstruction(A4TVMobileClient.KEY_LEFT);
                        shouldBreak = true;
                        userInterfaceEventManager.addAction("left", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        break;
                    case "direita":
                        sendKeyboardInstruction(A4TVMobileClient.KEY_RIGHT);
                        shouldBreak = true;
                        userInterfaceEventManager.addAction("right", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        break;
                    case "ligar":
                        connect();
                        shouldBreak = true;
                        userInterfaceEventManager.addAction("connect to tv", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        break;
                    case "desligar":
                        disconnect();
                        shouldBreak = true;
                        userInterfaceEventManager.addAction("disconnect from tv", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        break;
                    case "ler":
                        readScreen();
                        shouldBreak = true;
                        userInterfaceEventManager.addAction("read_screen", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        break;
                    case "localizar":
                        userInterfaceEventManager.addAction("localize", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        localize();
                        shouldBreak = true;
                        break;
                    case "parar":
                        userInterfaceEventManager.addAction("interrupt", "-", "-", "-", "speech", readingMode + "." + focusMode, interactMode + "");
                        interrupt();
                        shouldBreak = true;
                        break;
                    default:
                }

                if (shouldBreak)
                    break;

                if (!shouldBreak && mobileClient != null)
                    mobileClient.speakThis("Comando inexistente.");
            }
            //mText.setText("results: "+String.valueOf(data.size()));
        }
        public void onPartialResults(Bundle partialResults)
        {
            System.err.println( "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            System.err.println("onEvent " + eventType);
        }
    }

}

