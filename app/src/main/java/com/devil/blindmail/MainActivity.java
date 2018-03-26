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

public class MainActivity extends AppCompatActivity{

    private TextToSpeech tts;
    private TextView status;
    private TextView To,Subject,Message;
    private int numberOfClicks;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);
        SessionManager sessionManager = new SessionManager(this);
        Log.d("email", sessionManager.getEmail());
        Log.d("pwd", sessionManager.getPasssword());
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    speak("Welcome to voice mail. Tell me the mail address to whom you want to send mail?");

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
        status = findViewById(R.id.status);
        To = findViewById(R.id.to);
        Subject  = findViewById(R.id.subject);
        Message = findViewById(R.id.message);
        numberOfClicks = 0;
        (findViewById(R.id.parent)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                speak("Would you like to log out?");
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

                try {
                    startActivityForResult(i, 200);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
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
            Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        tts.stop();
        tts.shutdown();
        String email = To.getText().toString().trim();
        String subject = Subject.getText().toString().trim();
        String message = Message.getText().toString().trim();
        SendMail sm = new SendMail(this, email, subject, message);
        sm.execute();

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
                            String to;
                            to= result.get(0).replaceAll("underscore","_");
                            to = to.replaceAll("\\s+","");
                            to = to.toLowerCase();
                            to = to.replace(" ","");
                            to = to + "@gmail.com";
                            To.setText(to);
                            status.setText("Subject?");
                            speak("What should be the subject?");
                            break;
                        case 2:
                            Subject.setText(result.get(0));
                            status.setText("Message?");
                            speak("Give me message");
                            break;
                        case 3:
                            Message.setText(result.get(0));
                            status.setText("Confirm?");
                            speak("Please Confirm the mail\n To : " + To.getText().toString() + "\nSubject : " + Subject.getText().toString() + "\nMessage : " + Message.getText().toString() + "\nSpeak Yes to confirm");
                            break;
                        case 4:
                            if(result.get(0).equals("yes"))
                            {
                                status.setText("Sending");
                                speak("Sending the mail");
                                sendEmail();
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
        else if(requestCode == 200){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if(result.get(0).equals("yes"))
                {
                    sessionManager.logoutUser();
                    startActivity(new Intent(this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                }
            }
        }
    }
}