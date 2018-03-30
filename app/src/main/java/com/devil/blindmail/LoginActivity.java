package com.devil.blindmail;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;


public class LoginActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private TextView status;
    private TextView Email,Password;
    private int numberOfClicks;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if(sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }
        setContentView(R.layout.activity_login);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    speak("Welcome to voice mail. Tell me your mail address");

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

        status = findViewById(R.id.status);
        Email = findViewById(R.id.email);
        Password  = findViewById(R.id.password);
        numberOfClicks = 0;
    }

    private void speak(String text){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public void layoutClicked(View view)
    {
        numberOfClicks++;
        listen();
    }

    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(LoginActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    private void exitFromApp()
    {
        this.finishActivity(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if(result.get(0).equals("cancel"))
                {
                    speak("Cancelled!");
                    exitFromApp();
                }
                else {
                    switch (numberOfClicks) {
                        case 1:
                            String email;
                            email= result.get(0).replaceAll("underscore","_");
                            email = email.replaceAll("\\s+","");
                            email = email.trim();
                            email = email.toLowerCase();
                            email = email + "@gmail.com";
                            Email.setText(email);
                            status.setText("Password?");
                            speak("Tell your password");
                            break;
                        case 2:
                            Password.setText(result.get(0).toLowerCase().replace(" ",""));
                            status.setText("Confirm?");
                            speak("Please Confirm the mail ID\n Email : " + Email.getText().toString() + "\nPassword : " + Password.getText().toString() + "\nSpeak Yes to confirm");
                            break;
                        case 3:
                            if(result.get(0).equals("yes"))
                            {
                                status.setText("Logging In");
                                speak("Connecting to servers");
                                sessionManager = new SessionManager(this);
                                sessionManager.createLoginSession(Email.getText().toString(),Password.getText().toString());
                                startActivity(new Intent(this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                finish();
                            }else
                            {
                                status.setText("Restarting");
                                speak("Please Restart the app to reset");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        exitFromApp();
                                    }
                                }, 4000);
                            }
                    }
                }
            }
        }
    }
}