package com.ciu196.mobilecomputing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.Duration;

import static com.ciu196.mobilecomputing.ViewAnimationService.fadeInAnimation;
import static com.ciu196.mobilecomputing.ViewAnimationService.fadeOutAnimation;
import static com.ciu196.mobilecomputing.ViewAnimationService.translateAnimation;
import static com.ciu196.mobilecomputing.ViewAnimationService.translateToCenterInParentViewAnimation;
import static com.ciu196.mobilecomputing.ViewAnimationService.uniformScaleAnimation;
import static java.lang.Thread.sleep;


public class ConnectActivity extends AppCompatActivity {

    public enum guiMode {CONNECT, START_TO_LISTEN, CANT_LISTEN, CANT_CONNECT, LISTENING, PLAYING};

    public enum circleColor {BLUE, RED};

    public enum Axis {X, Y};

    TextView pianoStatusTextView;
    TextView pianoDetailedTextView;
    TextView playerNameTextView;
    Button actionButton;
    Circle circle1;
    Circle circle2;
    Circle circle3;
    Circle circle4;
    View backgroundView;

    guiMode currentGuiMode = guiMode.START_TO_LISTEN;
    int currentBackgroundColor = 0;
    int currentCircleColors[] = {0, 0, 0, 0};
    circleColor currentCircleColor = circleColor.BLUE;

    boolean testFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_activity);

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
        currentCircleColors[0] = circle1.getColor();
        currentCircleColors[1] = circle2.getColor();
        currentCircleColors[2] = circle3.getColor();
        currentCircleColors[3] = circle4.getColor();




        if (BroadcastService.isLive()) {
            if (BroadcastService.closeEnough()) {
                switchGui(guiMode.START_TO_LISTEN);
            } else {
                switchGui(guiMode.CANT_LISTEN);
            }
        } else {
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

    private void switchGui(guiMode m) {

        teardownCurrentGui();

        if (m == guiMode.START_TO_LISTEN) {
            currentGuiMode = guiMode.START_TO_LISTEN;

            playerNameTextView.setText(BroadcastService.getPlayerName());
            pianoStatusTextView.setText("is playing");
            actionButton.setText("Start Listening");
            setCircleColor(circleColor.BLUE);
            backgroundView.setBackgroundColor(getResources().getColor(R.color.backgroundGrayColor));
            fadeInAnimation(playerNameTextView, 500);
            fadeInAnimation(pianoStatusTextView, 550);
            fadeInAnimation(pianoDetailedTextView, 600);
            fadeInAnimation(actionButton, 1000);

            final Handler handler = new Handler();

            handler.postDelayed(new Runnable(){
                public void run(){
                    try {

                        pianoDetailedTextView.setText(formatDuration(BroadcastService.getCurrentSessionDuration()));
                    } catch (NotLiveException e) {
                        e.printStackTrace();
                    }

                    handler.postDelayed(this, 1000);
                }
            }, 0);

        } else if (m == guiMode.LISTENING) {
            currentGuiMode = guiMode.LISTENING;

            changeBackgroundColor(getResources().getColor(R.color.backgroundBlueColor));
            setCircleColor(circleColor.BLUE);

            pianoStatusTextView.setText("Currently listening to");
            playerNameTextView.setText(BroadcastService.getPlayerName());


            //TODO: The previous setText call dosn't update the internal position of the textView, which translateToCenterInParentView uses. E.I it dosn't work. Will have to be fixed.
            translateToCenterInParentViewAnimation(pianoStatusTextView, 500, Axis.X);
            translateToCenterInParentViewAnimation(playerNameTextView, 500, Axis.X);

            translateAnimation(playerNameTextView, 500, Axis.Y, 120);
            translateAnimation(pianoDetailedTextView, 500, Axis.Y, 130);

            uniformScaleAnimation(playerNameTextView, 500, 1.3f);

            fadeInAnimation(playerNameTextView, 500);
            fadeInAnimation(pianoStatusTextView, 550);
            fadeInAnimation(pianoDetailedTextView, 600);



        } else if (m == guiMode.CANT_CONNECT) {
            currentGuiMode = guiMode.CANT_CONNECT;

            playerNameTextView.setText(BroadcastService.getPlayerName());
            actionButton.setEnabled(false);
            actionButton.setText("Connect");

        } else if (m == guiMode.CONNECT) {
            currentGuiMode = guiMode.CONNECT;

            playerNameTextView.setText(BroadcastService.getPlayerName() + " is playing");
            actionButton.setText("Connect");

        }  else if (m == guiMode.PLAYING) {
            currentGuiMode = guiMode.PLAYING;

            pianoStatusTextView.setText(BroadcastService.getPlayerName() + " is playing");
            setCircleColor(circleColor.RED);
            changeBackgroundColor(getResources().getColor(R.color.backgroundRedColor));

        }
    }


    private void teardownCurrentGui() {
        if(currentGuiMode == guiMode.START_TO_LISTEN){
            fadeOutAnimation(playerNameTextView, 200);
            fadeOutAnimation(pianoStatusTextView, 350);
            fadeOutAnimation(pianoDetailedTextView, 400);
            fadeOutAnimation(actionButton, 500);

        } else if(currentGuiMode == guiMode.LISTENING){


        }
        else if(currentGuiMode == guiMode.CANT_CONNECT){


        }
        else if(currentGuiMode == guiMode.CONNECT){


        }
        else if(currentGuiMode == guiMode.PLAYING){


        }

    }

    private void changeBackgroundColor(int newColor) {
        colorTransitionAnimation(backgroundView, 250, currentBackgroundColor, newColor);
        currentBackgroundColor = newColor;

    }

    private void setCircleColor(circleColor c) {
        int colorFrom1 = currentCircleColors[0];
        int colorFrom2 = currentCircleColors[1];
        int colorFrom3 = currentCircleColors[2];
        int colorFrom4 = currentCircleColors[3];
        int colorTo1 = 0;
        int colorTo2 = 0;
        int colorTo3 = 0;
        int colorTo4 = 0;

        if (c == circleColor.BLUE) {
            colorTo1 = getResources().getColor(R.color.circle1BlueColor);
            colorTo2 = getResources().getColor(R.color.circle2BlueColor);
            colorTo3 = getResources().getColor(R.color.circle3BlueColor);
            colorTo4 = getResources().getColor(R.color.circle4BlueColor);
        } else if (c == circleColor.RED) {
            colorTo1 = getResources().getColor(R.color.circle1RedColor);
            colorTo2 = getResources().getColor(R.color.circle2RedColor);
            colorTo3 = getResources().getColor(R.color.circle3RedColor);
            colorTo4 = getResources().getColor(R.color.circle4RedColor);

        }

        colorTransitionAnimation(circle1, 250, colorFrom1, colorTo1);
        colorTransitionAnimation(circle2, 450, colorFrom2, colorTo2);
        colorTransitionAnimation(circle3, 600, colorFrom3, colorTo3);
        colorTransitionAnimation(circle4, 700, colorFrom4, colorTo4);

        currentCircleColors[0] = colorTo1;
        currentCircleColors[1] = colorTo2;
        currentCircleColors[2] = colorTo3;
        currentCircleColors[3] = colorTo4;


    }

    private void colorTransitionAnimation(final View v, int duration, int colorFrom, int colorTo) {
        ValueAnimator circle1ColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        circle1ColorAnimation.setDuration(duration);
        circle1ColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if (v instanceof Circle)
                    ((Circle) v).setColor((int) animator.getAnimatedValue());
                else
                    v.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        circle1ColorAnimation.start();

    }
}