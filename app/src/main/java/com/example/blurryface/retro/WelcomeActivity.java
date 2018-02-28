package com.example.blurryface.retro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {
    public static int hiScore;
    SharedPreferences preferences;
    TextView highScoreText;
    int defaultHighScore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        //initialise our shared preference
        preferences = getSharedPreferences("retroScores",MODE_PRIVATE);
        hiScore = preferences.getInt("highscore",defaultHighScore);
        highScoreText = findViewById(R.id.highScoreText);
        //set the text to the high score scored
        highScoreText.setText(String.valueOf(hiScore));
    }
    public void onPlay(View view){
        //go to the Game and start playing
        Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void onQuit(View view){
        //quit
        System.exit(0);
    }
}
