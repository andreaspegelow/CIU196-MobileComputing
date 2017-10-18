package com.ciu196.mobilecomputing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.Duration;

import java.util.Random;

import static android.support.design.widget.FloatingActionButton.*;
import static com.ciu196.mobilecomputing.ViewAnimationService.addAnimator;
import static com.ciu196.mobilecomputing.ViewAnimationService.addFadeInAnimation;
import static com.ciu196.mobilecomputing.ViewAnimationService.addFadeOutAnimation;
import static com.ciu196.mobilecomputing.ViewAnimationService.addFadeWithScaleAnimation;
import static com.ciu196.mobilecomputing.ViewAnimationService.addInstantOperation;
import static com.ciu196.mobilecomputing.ViewAnimationService.getColorTransitionAnimator;
import static com.ciu196.mobilecomputing.ViewAnimationService.getTranslateToCenterInParentViewAnimator;
import static com.ciu196.mobilecomputing.ViewAnimationService.getTranslationAnimator;
import static com.ciu196.mobilecomputing.ViewAnimationService.getUniformScaleAnimator;
import static com.ciu196.mobilecomputing.ViewAnimationService.startAllAnimation;


public class ConnectActivity extends AppCompatActivity {

    public enum guiMode {CONNECT, START_TO_LISTEN, CANT_LISTEN, CANT_CONNECT, LISTENING, PLAYING};

    public enum circleColor {BLUE, GRAY, RED};

    final int STATUS_FADE_DURATION = 475;
    final int NAME_FADE_DURATION = 400;
    final int ACTION_BUTTON_FADE_DURATION = 550;
    final int DURATION_TEXT_FADE_DURATION = 600;

    RelativeLayout rel;
    TextView pianoStatusTextView;
    TextView listenersTextView;
    TextView playerNameTextView;
    TextView durationText;
    ImageView earImage;
    Button actionButton;
    Circle circle1;
    Circle circle2;
    Circle circle3;
    Circle circle4;
    View backgroundView;
    View listenerLayout;
    View errorView;
    TextView errorText;
    FloatingActionButton fab, fab1, fab2, fab3;

    guiMode currentGuiMode = guiMode.START_TO_LISTEN;
    int currentBackgroundColor = 0;
    int currentCircleColors[] = {0, 0, 0, 0};

    String resultName;


    boolean testFlag = false;
    boolean isShowingReactions = false;
    float density;
    LinearLayout.LayoutParams miniFabLp, normalFabLp;

    View.OnClickListener reactionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Toast toast = new Toast(ConnectActivity.this);
            ImageView imageView = new ImageView(ConnectActivity.this);

            int id = view.getId();
            switch (id){
                case R.id.fab1:
                    imageView.setImageResource(R.drawable.ic_thumb_up_white_24dp);
                    imageView.setBackgroundColor(getColor(R.color.actionBlueColor));
                    break;
                case R.id.fab2:
                    imageView.setImageResource(R.drawable.ic_tag_faces_white_24dp);
                    imageView.setBackgroundColor(getColor(R.color.fabColor));
                    break;
                case R.id.fab3:
                    imageView.setImageResource(R.drawable.ic_favorite_white_24dp);
                    imageView.setBackgroundColor(getColor(R.color.myLocationRed));
                    break;
                default:
                    break;
            }
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT));
            imageView.setPadding(10,10,10,10);
            rel.addView(imageView);
            animateReaction(imageView);
        }
    };

    public void animateReaction(final ImageView view){
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        Random random = new Random();
        int x = 150 + random.nextInt(width-300);
        int y = height - random.nextInt(height / 4) - 100;
        view.setX(x);
        view.setY(y);
        view.setVisibility(VISIBLE);
        view.setElevation(4);

        int duration = 3000;
        AnimatorSet animation = new AnimatorSet();
        animation
                .play(ViewAnimationService.getFadeAnimator(view, duration, 1, 0))
                .with(ViewAnimationService.getTranslationAnimator(view, duration, ViewAnimationService.Axis.Y, -200))
                .with(ViewAnimationService.getElevationTransitionAnimator(view, duration, 32f))
                .with(ViewAnimationService.getWiggleAnimator(view, 1000, -15f, 15f))
                .with(ViewAnimationService.getUniformScaleAnimator(view, duration, 2.5f));

        
        //ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha",  1f, 0f);
        //fadeOut.setDuration(2000);

        //final AnimatorSet mAnimationSet = new AnimatorSet();

        //mAnimationSet.play(fadeIn).after(fadeOut);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rel.removeView(view);
            }
        });
        animation.setInterpolator(new PowerInterpolator(2.5f));
        animation.start();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_activity);
        //TESTING todo REMOVE
        BroadcastService.getInstance(this);

        //Connect UI Elements
        rel = (RelativeLayout) (RelativeLayout) findViewById(R.id.backgroundLayout);
        pianoStatusTextView = (TextView) findViewById(R.id.pianoStatusTextView);
        listenersTextView = (TextView) findViewById(R.id.listenersTextView);
        playerNameTextView = (TextView) findViewById(R.id.playerNameTextView);
        durationText = (TextView) findViewById(R.id.durationText);
        actionButton = (Button) findViewById(R.id.actionButtion);
        listenerLayout = (View) findViewById(R.id.listenersLayout);
        earImage = (ImageView) findViewById(R.id.earImage);
        errorView = (View) findViewById(R.id.errorView);
        errorText = (TextView) findViewById(R.id.errorText);

        density = getResources().getDisplayMetrics().density;
        int miniFabWidth = (int) (36 * getResources().getSystem().getDisplayMetrics().density);
        int normalFabWidth = (int) (56 * getResources().getSystem().getDisplayMetrics().density);
        int layout_padding16 = (int) (16 * getResources().getSystem().getDisplayMetrics().density);
        int layout_padding36 = (int) (36 * getResources().getSystem().getDisplayMetrics().density);
        int layout_padding8 = (int) (8 * getResources().getSystem().getDisplayMetrics().density);
        miniFabLp = new LinearLayout.LayoutParams(miniFabWidth, miniFabWidth, Gravity.CENTER_HORIZONTAL);
        miniFabLp.gravity = Gravity.CENTER_HORIZONTAL;
        miniFabLp.setMargins(layout_padding16,layout_padding16,layout_padding16,layout_padding8); //todo
        normalFabLp = new LinearLayout.LayoutParams(normalFabWidth, normalFabWidth, Gravity.CENTER_HORIZONTAL);
        normalFabLp.gravity = Gravity.CENTER_HORIZONTAL;

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);

        fab1.setOnClickListener(reactionListener);
        fab2.setOnClickListener(reactionListener);
        fab3.setOnClickListener(reactionListener);


        circle1 = (Circle) findViewById(R.id.circle1);
        circle2 = (Circle) findViewById(R.id.circle2);
        circle3 = (Circle) findViewById(R.id.circle3);
        circle4 = (Circle) findViewById(R.id.circle4);
        backgroundView = (View) findViewById(R.id.backgroundLayout);
        currentBackgroundColor = getResources().getColor(R.color.backgroundCreamColor);
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
            if (BroadcastService.closeEnough()) {
                switchGui(guiMode.CONNECT);
            } else {
                switchGui(guiMode.CANT_CONNECT);
            }
        }

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentGuiMode == guiMode.START_TO_LISTEN){
                    switchGui(guiMode.LISTENING);
                } else if (currentGuiMode == guiMode.LISTENING){
                    switchGui(guiMode.START_TO_LISTEN);
                }else if (currentGuiMode == guiMode.CONNECT){


                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Enter desired name");

                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.name_dialog, null);
                    builder.setView(dialogView);


                    builder.setPositiveButton("CONNECT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resultName = ((EditText)dialogView.findViewById(R.id.name)).getText().toString();
                            if(BroadcastService.startNewBroadcast(resultName))
                                switchGui(guiMode.PLAYING);
                            else
                                Toast.makeText(v.getContext(),"Cannot connect", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();



                }else if (currentGuiMode == guiMode.PLAYING){
                        switchGui(guiMode.CONNECT);
                }


            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentGuiMode == guiMode.START_TO_LISTEN){

                    Intent i = new Intent(ConnectActivity.this, VolumeRangeActivity.class);
                    startActivity(i);
                }else if (currentGuiMode == guiMode.LISTENING){

                    if(isShowingReactions){
                        //hide reaction alternatives
                        addFadeWithScaleAnimation(fab, 400, 0, 1, 0); //hide
                        fab.setSize(SIZE_NORMAL);
                        fab.setLayoutParams(normalFabLp);

                                fab.setImageResource(R.drawable.ic_tag_faces_white_24dp);
                        AnimatorSet animation = new AnimatorSet();
                        animation
                                .play(ViewAnimationService.getColorTransitionAnimator(fab, 100, R.color.disabledGrey, R.color.fabColor))
                                .with(ViewAnimationService.getFadeWithScaleAnimator(fab, 400, 1, 0, 1));
                        animation.start(); //show

                        //fab.show();

                        fab1.setClickable(false);
                        fab2.setClickable(false);
                        fab3.setClickable(false);
                        fab1.hide();
                        fab2.hide();
                        fab3.hide();

                        isShowingReactions = false;
                    } else {
                        //show reaction alternatives
                        addFadeWithScaleAnimation(fab, 400, 0, 1, 0); //hide
                        fab.setSize(SIZE_MINI);
                        fab.setLayoutParams(miniFabLp);
                        fab.setImageResource(R.drawable.ic_close_black_24dp);
                        ViewAnimationService.getColorTransitionAnimator(fab, 100, R.color.fabColor, R.color.disabledGrey).start();

                        AnimatorSet animation = new AnimatorSet();
                        animation
                                .play(ViewAnimationService.getColorTransitionAnimator(fab, 100, R.color.fabColor, R.color.disabledGrey))
                                .with(ViewAnimationService.getFadeWithScaleAnimator(fab, 400, 1, 0, 1));
                        animation.start(); //show


                        fab1.setClickable(true);
                        fab2.setClickable(true);
                        fab3.setClickable(true);
                        fab1.show();
                        fab2.show();
                        fab3.show();

                        fab1.setSize(SIZE_NORMAL);
                        fab2.setSize(SIZE_NORMAL);
                        fab3.setSize(SIZE_NORMAL);



                        isShowingReactions = true;
                    }


                }
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

            addInstantOperation(actionButton, () -> actionButton.setBackground(getDrawable(R.drawable.rounded_button_blue)));
            pianoStatusTextView.setTextColor(getColor(R.color.grayTextColor));
            playerNameTextView.setTextColor(getColor(R.color.actionBlueColor));
            playerNameTextView.setText(BroadcastService.getPlayerName());
            addInstantOperation(pianoStatusTextView, () -> pianoStatusTextView.setText("is playing"));
            actionButton.setText("Start Listening");
            listenersTextView.setText(BroadcastService.getNumberOfListeners()+"");
            earImage.setImageResource(R.drawable.ic_hearing_black_24dp);
            setCircleColor(circleColor.GRAY);
            changeBackgroundColor(getResources().getColor(R.color.backgroundCreamColor));
            addFadeInAnimation(playerNameTextView, 500);
            addFadeInAnimation(pianoStatusTextView, 550);
            addFadeInAnimation(earImage, 400);
            addFadeInAnimation(listenerLayout, 600);
            addFadeInAnimation(actionButton, 1000);


            addInstantOperation(fab,
                    () -> fab.setImageResource(R.drawable.ic_map_white_24dp)
            );
            addFadeWithScaleAnimation(fab, 400, 1, 0, 1);


            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            fab1.hide();
            fab2.hide();
            fab3.hide();

            final Handler handler = new Handler();

            handler.postDelayed(new Runnable(){
                public void run(){
                    try {

                        durationText.setText(formatDuration(BroadcastService.getCurrentSessionDuration()));
                    } catch (NotLiveException e) {
                        e.printStackTrace();
                    }

                    handler.postDelayed(this, 1000);
                }
            }, 0);

        } else if (m == guiMode.LISTENING) {
            currentGuiMode = guiMode.LISTENING;

            changeBackgroundColor(getResources().getColor(R.color.backgroundBlueColor));
            actionButton.setBackground(getResources().getDrawable(R.drawable.rounded_button_blue));
            setCircleColor(circleColor.BLUE);
            pianoStatusTextView.setTextColor(getResources().getColor(R.color.grayTextColor));
            playerNameTextView.setTextColor(getResources().getColor(R.color.actionBlueColor));
            //addInstantOperation(pianoStatusTextView, () -> pianoStatusTextView.setText("Currently listening to"));
            playerNameTextView.setText(BroadcastService.getPlayerName());
            actionButton.setText("Stop listening");
            earImage.setImageResource(R.drawable.ic_hearing_white_24dp);

            fab.setSize(SIZE_NORMAL);
            fab.setLayoutParams(normalFabLp);

            addInstantOperation(fab,
                    () -> fab.setImageResource(R.drawable.ic_tag_faces_white_24dp)
            );
            addFadeWithScaleAnimation(fab, 400, 1, 0, 1);



            addFadeInAnimation(earImage, 400);
            //TODO: The previous setText call doesn't update the internal position of the textView, which translateToCenterInParentView uses. E.I it dosn't work. Will have to be fixed.
            addAnimator(pianoStatusTextView, getTranslateToCenterInParentViewAnimator(pianoStatusTextView, 500, ViewAnimationService.Axis.X));
            addAnimator(playerNameTextView, getTranslateToCenterInParentViewAnimator(playerNameTextView, 500, ViewAnimationService.Axis.X));

            addAnimator(playerNameTextView, getTranslationAnimator(playerNameTextView, 500, ViewAnimationService.Axis.Y, 120));
            addAnimator(listenerLayout, getTranslationAnimator(listenerLayout, 500, ViewAnimationService.Axis.Y, 130));

            addAnimator(playerNameTextView, getUniformScaleAnimator(playerNameTextView, 500, 1.3f));

            addFadeInAnimation(playerNameTextView, NAME_FADE_DURATION);
            addFadeInAnimation(pianoStatusTextView, STATUS_FADE_DURATION);
            addFadeInAnimation(actionButton, ACTION_BUTTON_FADE_DURATION);

        } else if (m == guiMode.CANT_CONNECT) {
            addFadeInAnimation(playerNameTextView, NAME_FADE_DURATION);
            addFadeInAnimation(pianoStatusTextView, STATUS_FADE_DURATION);
            addFadeInAnimation(actionButton, ACTION_BUTTON_FADE_DURATION);
            currentGuiMode = guiMode.CANT_CONNECT;
            setCircleColor(circleColor.GRAY);
            playerNameTextView.setText(BroadcastService.getPlayerName());
            actionButton.setEnabled(false);
            actionButton.setText("Connect to piano");

        } else if (m == guiMode.CONNECT) {
            currentGuiMode = guiMode.CONNECT;

            changeBackgroundColor(getColor(R.color.backgroundGrayColor));
            setCircleColor(circleColor.GRAY);

            durationText.setVisibility(View.INVISIBLE);

            addInstantOperation(playerNameTextView, () -> playerNameTextView.setTextColor(getColor(R.color.actionRedColor)));
            addInstantOperation(playerNameTextView, () -> playerNameTextView.setText(BroadcastService.getPlayerName()));

            addInstantOperation(pianoStatusTextView, () ->  pianoStatusTextView.setTextColor(getColor(R.color.whiteColor)));
            addInstantOperation(pianoStatusTextView, () ->   pianoStatusTextView.setText("is playing"));

            addInstantOperation(actionButton, () ->   actionButton.setText("Connect to piano"));
            addInstantOperation(actionButton, () ->   actionButton.setBackground(getDrawable(R.drawable.rounded_button_red)));

            addInstantOperation(listenersTextView, () ->   listenersTextView.setText(BroadcastService.getNumberOfListeners()+""));

            addInstantOperation(earImage, () ->   earImage.setImageResource(R.drawable.ic_hearing_white_24dp));


            addFadeInAnimation(playerNameTextView, NAME_FADE_DURATION);
            addFadeInAnimation(pianoStatusTextView, STATUS_FADE_DURATION);
            addFadeInAnimation(earImage, 550);
            addFadeInAnimation(actionButton, ACTION_BUTTON_FADE_DURATION);


        }  else if (m == guiMode.PLAYING) {
            currentGuiMode = guiMode.PLAYING;
            changeBackgroundColor(getColor(R.color.backgroundRedColor));
            setCircleColor(circleColor.RED);
            playerNameTextView.setTextColor(getColor(R.color.actionRedColor));
            playerNameTextView.setText("You");
            pianoStatusTextView.setTextColor(getColor(R.color.whiteColor));
            pianoStatusTextView.setText("are playing");
            actionButton.setText("Stop playing");
            listenersTextView.setText(BroadcastService.getNumberOfListeners()+"");
            earImage.setImageResource(R.drawable.ic_hearing_white_24dp);
            actionButton.setBackground(getDrawable(R.drawable.rounded_button_red));

            addFadeInAnimation(playerNameTextView, NAME_FADE_DURATION);
            addFadeInAnimation(pianoStatusTextView, STATUS_FADE_DURATION);
            addFadeInAnimation(earImage, 550);
            addFadeInAnimation(actionButton, ACTION_BUTTON_FADE_DURATION);
            addFadeInAnimation(durationText, DURATION_TEXT_FADE_DURATION);

        }
        startAllAnimation();
    }


    private void teardownCurrentGui() {

        if(currentGuiMode == guiMode.START_TO_LISTEN){

            addFadeOutAnimation(playerNameTextView, 200);
            addFadeOutAnimation(pianoStatusTextView, 350);
            addFadeOutAnimation(actionButton, 700);
            addFadeOutAnimation(earImage, 400);
            addFadeWithScaleAnimation(fab, 400, 0, 1, 0);
        } else if(currentGuiMode == guiMode.LISTENING){
            addFadeOutAnimation(playerNameTextView, 200);
            addFadeOutAnimation(pianoStatusTextView, 350);
            addFadeOutAnimation(actionButton, 700);
            addFadeOutAnimation(earImage, 400);
            addFadeWithScaleAnimation(fab, 400, 0, 1, 0);
            isShowingReactions = false;
            addAnimator(playerNameTextView, getTranslationAnimator(playerNameTextView, 500, ViewAnimationService.Axis.Y, -120));
            addAnimator(listenerLayout, getTranslationAnimator(listenerLayout, 500, ViewAnimationService.Axis.Y, -130));
        }
        else if(currentGuiMode == guiMode.CANT_CONNECT){



        }
        else if(currentGuiMode == guiMode.CONNECT){


        }
        else if(currentGuiMode == guiMode.PLAYING){


        }

    }


    private void changeBackgroundColor(int newColor) {
        addAnimator(backgroundView, getColorTransitionAnimator(backgroundView, 750, currentBackgroundColor, newColor));
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

        }else if (c == circleColor.GRAY) {
            colorTo1 = getResources().getColor(R.color.circle1GrayColor);
            colorTo2 = getResources().getColor(R.color.circle2GrayColor);
            colorTo3 = getResources().getColor(R.color.circle3GrayColor);
            colorTo4 = getResources().getColor(R.color.circle4GrayColor);

        }

        addAnimator(circle1, getColorTransitionAnimator(circle1, 250, colorFrom1, colorTo1));
        addAnimator(circle2, getColorTransitionAnimator(circle2, 450, colorFrom2, colorTo2));
        addAnimator(circle3, getColorTransitionAnimator(circle3, 600, colorFrom3, colorTo3));
        addAnimator(circle4, getColorTransitionAnimator(circle4, 700, colorFrom4, colorTo4));

        currentCircleColors[0] = colorTo1;
        currentCircleColors[1] = colorTo2;
        currentCircleColors[2] = colorTo3;
        currentCircleColors[3] = colorTo4;


    }


}