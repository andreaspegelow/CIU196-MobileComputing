package com.ciu196.mobilecomputing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.Duration;


public class ConnectActivity extends AppCompatActivity {

    public enum guiMode{CONNECT, LISTEN, CANT_LISTEN, CANT_CONNECT, LISTENING, PLAYING };

    TextView pianoStatusTextView;
    TextView pianoDetailedTextView;
    TextView playerNameTextView;
    Button actionButton;
    Circle circle1;
    Circle circle2;
    Circle circle3;
    Circle circle4;
    View backgroundView;

    int currentBackgroundColor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_activity);

        BroadcastService broadcastService = new BroadcastService();

        //Connect UI Elements
        pianoStatusTextView = (TextView) findViewById(R.id.pianoStatusTextView);
        pianoDetailedTextView = (TextView) findViewById(R.id.pianodetailedTextView);
        playerNameTextView = (TextView) findViewById(R.id.playerNameTextView);
        actionButton = (Button) findViewById(R.id.actionButtion);
        circle1 = (Circle) findViewById(R.id.circle1);
        circle2 = (Circle) findViewById(R.id.circle2);
        circle3 = (Circle) findViewById(R.id.circle3);
        circle4 = (Circle) findViewById(R.id.circle4);
        backgroundView = (View) findViewById(R.id.backgroundLayout);
        currentBackgroundColor = getResources().getColor(R.color.backgroundGrayColor);
        pianoStatusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PitchAnimationActivity.class);
                startActivity(i);
            }
        });



        if (BroadcastService.isLive()) {
            if(BroadcastService.closeEnough()){
                switchGui(guiMode.LISTEN);
            }
            else {
                switchGui(guiMode.CANT_LISTEN);
            }
        }else{
            switchGui(guiMode.CONNECT);
        }

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchGui(guiMode.LISTENING);
            }
        });

    }

    private String formatDuration(Duration d) {

        String hours = d.getStandardHours() + "";
        String minutes = (d.getStandardMinutes() % 60 > 9 ? d.getStandardMinutes() % 60 + "" : "0" + d.getStandardMinutes() % 60 + "");
        String seconds = (d.getStandardSeconds() % 60 > 9 ? d.getStandardSeconds() % 60 + "" : "0" + d.getStandardSeconds() % 60 + "");


        return hours + ":" + minutes + ":" + seconds;
    }
    private void switchGui(guiMode m){

        if(m == guiMode.LISTEN){

            playerNameTextView.setText(BroadcastService.getPlayerName());
            pianoStatusTextView.setText("is playing");
            actionButton.setText("Start Listening");
            circle1.setColor(getResources().getColor(R.color.circle1BlueColor));
            circle2.setColor(getResources().getColor(R.color.circle2BlueColor));
            circle3.setColor(getResources().getColor(R.color.circle3BlueColor));
            circle4.setColor(getResources().getColor(R.color.circle4BlueColor));
            backgroundView.setBackgroundColor(getResources().getColor(R.color.backgroundGrayColor));

            try {
                pianoDetailedTextView.setText(formatDuration(BroadcastService.getCurrentSessionDuration()));
            } catch (NotLiveException e) {
                e.printStackTrace();
            }
        }
        else if(m == guiMode.CANT_CONNECT){
            playerNameTextView.setText(BroadcastService.getPlayerName());
            actionButton.setEnabled(false);
            actionButton.setText("Connect");
        }
        else if(m == guiMode.CONNECT){
            playerNameTextView.setText(BroadcastService.getPlayerName() + " is playing");
            actionButton.setText("Connect");

        }
        else {
            if (m == guiMode.LISTENING) {

                changeBackgroundColor(getResources().getColor(R.color.backgroundBlueColor));
                final Animation out = new AlphaAnimation(1.0f, 0.0f);
                out.setDuration(500);
                actionButton.startAnimation(out);
                out.setAnimationListener(new Animation.AnimationListener() {
                                             @Override
                                             public void onAnimationStart(Animation animation) {

                                             }

                                             @Override
                                             public void onAnimationEnd(Animation animation) {
                                                    actionButton.setVisibility(View.INVISIBLE);
                                             }

                                             @Override
                                             public void onAnimationRepeat(Animation animation) {

                                             }
                                         }
                );

                pianoStatusTextView.setText("Currently listening to");
                playerNameTextView.setText(BroadcastService.getPlayerName());


            } else if (m == guiMode.PLAYING) {
                pianoStatusTextView.setText(BroadcastService.getPlayerName() + " is playing");
                actionButton.setText("Connect");

            }
        }

    }

    private void changeBackgroundColor(int newColor){
        int colorFrom = currentBackgroundColor;
        int colorTo = newColor;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                backgroundView.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
        currentBackgroundColor = newColor;

    }
}