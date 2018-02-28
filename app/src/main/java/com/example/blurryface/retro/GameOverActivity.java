package com.example.blurryface.retro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.example.blurryface.retro.WelcomeActivity.hiScore;

public class GameOverActivity extends AppCompatActivity {
    TextView score,highScore;
    int high;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        score = findViewById(R.id.yourScoreText);
        highScore = findViewById(R.id.yourHighScoretext);
        Bundle extras = getIntent().getExtras();
        sharedPreferences = getSharedPreferences("retroScores",MODE_PRIVATE);
        if(extras!=null){
            String playerscore = getIntent().getStringExtra("score");
            score.setText(playerscore);
        }
        high = sharedPreferences.getInt("highscore",hiScore);
        highScore.setText(String.valueOf(high));

    }
    public void onReplay(View view){
        Intent intent = new Intent(GameOverActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void onQuit(View view){
        System.exit(0);
    }
}
