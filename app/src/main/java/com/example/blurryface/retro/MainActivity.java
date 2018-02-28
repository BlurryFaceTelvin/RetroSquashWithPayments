package com.example.blurryface.retro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.SoundPool;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Random;

import static com.example.blurryface.retro.WelcomeActivity.hiScore;

public class MainActivity extends AppCompatActivity {
    SquashCourtView squashCourtView;
    //Canvas to draw
    Canvas canvas;
    //variables for sound
    private SoundPool soundPool;
    int sample1,sample2,sample3,sample4=-1;
    //variable to hold/display number of pixels
    Display display;
    Point size;
    int screenWidth,screenHeight;
    //variables for our game objects
    //racket
    int racketWidth,racketHeight;
    Point racketPosition;
    //ball
    Point ballPosition;
    int ballWidth;
    //ball movement checks right,left,up,down
    boolean isBallRight,isBallLeft,isBallUp,isBallDown;
    //racket movement checks left,right
    boolean isRacketLeft,isRacketRight;
    //stats
    long lastFrameTime;
    int fps,score,lives,speed;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);
        //initialise sound

        soundPool = new SoundPool.Builder().setMaxStreams(10).build();
        sample1 = soundPool.load(this,R.raw.sample1,0);
        sample2 = soundPool.load(this,R.raw.sample2,0);
        sample3 = soundPool.load(this,R.raw.sample3,0);
        sample4 = soundPool.load(this,R.raw.sample4,0);

        //initialise the display
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        //game objects
        //racket
        racketPosition = new Point();
        racketHeight = 20;
        racketPosition.x = screenWidth/2;
        racketPosition.y = screenHeight-racketHeight;
        racketWidth = screenWidth/6;

        //ball
        ballPosition = new Point();
        ballWidth = screenWidth/35;
        ballPosition.x = screenWidth/2;
        ballPosition.y = 1+ ballWidth;

        //if player has made any payments make the necessary adjustments
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            String previousScore = getIntent().getStringExtra("initialscore");
            String boughtLives = getIntent().getStringExtra("extralives");
            score = Integer.parseInt(previousScore);
            lives = Integer.parseInt(boughtLives);
            speed = 6;
        }else {
            //initialise the Lives
            lives = 3;
            speed = 6;
            score = 0;
        }
        //initialise our shared preferences
        sharedPreferences = getSharedPreferences("retroScores",MODE_PRIVATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        squashCourtView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        squashCourtView.resume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //user taps on the right
                if(event.getX()>=screenWidth/2){
                    //move to the right
                    isRacketRight=true;
                    isRacketLeft= false;
                }else{
                    isRacketLeft=true;
                    isRacketRight=false;
                }
                break;
            case MotionEvent.ACTION_UP:
                isRacketRight=false;
                isRacketLeft=false;
                break;
        }
        return true;
    }



    //set up the view for our game
    public class SquashCourtView extends SurfaceView implements Runnable{
        SurfaceHolder ourHolder;
        Thread ourThread;
        // volatile ensures changes to the variable on all threads
        volatile boolean playingSquash;
        Paint paint;
        public SquashCourtView(Context context) {
            super(context);
            //initialise our variables
            ourHolder = getHolder();
            paint = new Paint();
            isBallDown=true;
            //send the ball moving in a random direction
            randomBallMovement();
        }
        public void randomBallMovement(){
            //send the ball moving in a random direction
            //random generator for 3 numbers 0-2
            Random generator = new Random();
            int randomDirection = generator.nextInt(3);
            switch (randomDirection){
                case 0:
                    //move to the center
                    isBallRight = false;
                    isBallLeft = false;
                    break;
                case 1:
                    //move to the left
                    isBallLeft = true;
                    isBallRight=false;
                    break;
                case 2:
                    //ball to the right
                    isBallRight=true;
                    isBallLeft=false;
                    break;
            }
        }
        @Override
        public void run() {
            //when playing draw our court, control frames speed and update the Court
            while (playingSquash){
                updateCourt();
                drawCourt();
                controlFPS();
            }
        }
        //detect ball collision with the wall and change the direction
        public void updateCourt(){
            //insure ball continues moving right
            if(isRacketRight){
                if(racketPosition.x +(racketWidth/2)<screenWidth)
                {
                    racketPosition.x+=20;
                }
            }
            //insure ball continues moving right
            if(isRacketLeft){
                if(racketPosition.x - (racketWidth/2)>0)
                    racketPosition.x -=20;
            }
            //if ball collides with the right wall change movement to left
            if(ballPosition.x+ballWidth>screenWidth){
                isBallLeft=true;
                isBallRight=false;
                soundPool.play(sample1,1,1,0,0,1);
            }
            //if ball collides with the left wall change movement to right
            if(ballPosition.x<0){
                isBallRight=true;
                isBallLeft = false;
                soundPool.play(sample1,1,1,0,0,1);
            }
            //when the ball goes past our racket
            if(ballPosition.y>(screenHeight-ballWidth)){

                lives--;
                if(racketWidth>ballWidth){
                    racketWidth--;
                }
                if(lives==0){
                    //game over
                    if(score>hiScore){
                        editor = sharedPreferences.edit();
                        hiScore=score;
                        editor.putInt("highscore",hiScore);
                        editor.apply();
                    }
                    //dialog to pay for more lives
                    Intent intent = new Intent(MainActivity.this,PaymentActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("score",String.valueOf(score));
                    startActivity(intent);


                }
                 //if your lives are more than 0 you get another chance
                //ball starts falling from top
                ballPosition.y = 1+ballWidth;
                //move in a random direction
                randomBallMovement();
            }
            //if we hit the top position of the screen move down
            if(ballPosition.y<=0){
                isBallDown=true;
                isBallUp=false;
                ballPosition.y=1;
                soundPool.play(sample2,1,1,0,0,1);
            }
            //depending on the direction we should be moving adjust x and y positions
            if(isBallDown){
                ballPosition.y +=6;
            }
            if(isBallUp){
                ballPosition.y -=10;
            }
            if(isBallLeft){
                ballPosition.x -=12;
            }
            if(isBallRight){
                ballPosition.x +=12;
            }
            //check if ball hits the racket
            //if ball is above or on the racket
            if(ballPosition.y+ballWidth>=(racketPosition.y-racketHeight/2)){
                int halfRacket = racketWidth/2;
                //if the ball hits the racket
                if((ballPosition.x+ballWidth)>(racketPosition.x - halfRacket)&&(ballPosition.x-ballWidth)<(racketPosition.x+halfRacket)){
                    soundPool.play(sample3,1,1,0,0,1);
                    //add code to reduce the racket width
                    if(racketWidth>ballWidth){
                        racketWidth--;
                    }
                    //increase the score and speed
                    score++;
                    speed+=2;
                    //move the racket up
                    isBallUp=true;
                    isBallDown=false;
                    //decide on how the ball is going to move
                    //if the ball is on the right side of racket
                    if(ballPosition.x>racketPosition.x){
                        isBallRight=true;
                        isBallLeft=false;
                    }else {
                        //if the ball is on the left side of racket
                        isBallLeft=true;
                        isBallRight=false;
                    }


                }
            }
        }
        public void controlFPS(){
            //make sure game runs smoothly
            long timeThisFrame = (System.currentTimeMillis()-lastFrameTime);
            long timeToSleep = 15- timeThisFrame;
            if(timeThisFrame>0){
                fps = (int)(100/timeThisFrame);
            }
            if(timeToSleep>0){
                try {
                    Thread.sleep(timeToSleep);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            lastFrameTime = System.currentTimeMillis();

        }
        public void drawCourt(){
            //check whether the view holder is valid
            //if not valid return
            if(!ourHolder.getSurface().isValid()){
                return;
            }
            //if valid
            //draw background
            canvas = ourHolder.lockCanvas();
            //black background
            canvas.drawColor(Color.RED);
            paint.setColor(Color.BLUE);
            //text size
            paint.setTextSize(45);
            canvas.drawText("Score "+ score + "    Lives "+ lives +"    FPS "+ fps,45,45,paint);
            //draw the racket
            int left = racketPosition.x-(racketWidth/2);
            int top = racketPosition.y -(racketHeight/2);
            int right = racketPosition.x +(racketWidth/2);
            int bottom = racketPosition.y +(racketHeight/2);
            canvas.drawRect(left,top,right,bottom,paint);
            //draw the ball
            canvas.drawCircle(ballPosition.x,ballPosition.y,ballWidth,paint);
            ourHolder.unlockCanvasAndPost(canvas);
        }
        //when the user exists the game without killing the activity
        public void pause(){
            playingSquash=false;
            try {
                ourThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //when user gets back to the game
        public void resume(){
            playingSquash=true;
            ourThread = new Thread(this);
            ourThread.start();
        }
    }
}
