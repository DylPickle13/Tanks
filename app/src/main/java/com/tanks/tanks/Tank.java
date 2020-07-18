package com.tanks.tanks;

//A heck of a lot of import statements!

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

/**
 * Created by Dylan on 2018-02-09.
 */
public class Tank extends AppCompatActivity {

    //A heck of a lot more variables! Believe me, they all do something!
    static int shotPower1, shotPower2, shotAngle1, shotAngle2, windFactor, tank1x, tank2x, slopx1, slopx2, expFrame, player_won, i, turns, empCount;
    static float[] shot1 = new float[2], shot2 = new float[2];
    static int[] hp = new int[3], weaponUse = new int[3], empWait = new int[3], shield = new int[3], tree = new int[5];
    static int[][] ammo = new int[3][5];
    static float tank1y, tank2y, slopy1, slopy2, slope, tank1_angleValue, tank2_angleValue;
    static boolean sound_check = true, player_turn = false, wind_check = true, isMenuVisible, gamePaused = false;
    static boolean bulletInAnim = false, expInAnim = false, whichExp, hitTank, busInAnim = false, canMove, endAnim;
    static boolean supplyDropped, notCrash, namesSet = false, rememberTurn, playerEMPD, resetEMP1, resetEMP2, fired;
    BulletTrail whiteTrail;
    Terrain terrain;
    MediaPlayer explosionSound, fireSound, emp, shieldSound;
    TextView player_name, wind_factor, wind_text, power_value, angle_value, menu_shader, start_gameShader, hp_value, damageNum, turn, oppHealth;
    TextView empAmmo, shieldAmmo, shellAmmo, bulletAmmo, flashScreen, currentWeaponAmmo, shield_value, currentWeaponText;
    ImageView wind_direction, tank1, tank2, tank1_cannon, tank2_cannon, bullet, explosion, tankExplosion, battleBus, banner, supplyDrop;
    ImageView empSym, shieldSym, bulletSym, shellSym, currentWeapon, capShield1, capShield2, blueShell;
    ImageView[] trees = new ImageView[5];
    SeekBar shot_power, shot_angle;
    ProgressBar bulletInMotion, hp_bar, shield_bar;
    Button menu_exit, menu_resume, start_game, move_left, move_right, menu, fire, yesPlayAgain, noPlayAgain, interact, weapons, weaponBack;
    ObjectAnimator move_tank1X, move_tank1Y, tank1_angle, move_tank2X, move_tank2Y, tank2_angle, expX, expY, moveShieldX, moveShieldY;
    ObjectAnimator move_tank_cannon1X, move_tank_cannon1Y, tank1__cannon_angle, move_tank_cannon2X, move_tank_cannon2Y, tank2__cannon_angle;
    AnimatorSet tank_animate, tank_angle_animate, shoot_bullet, moveExp, moveNum, moveShield, blueShellPath;
    ObjectAnimator projX, projY, projAngle, fadeIn, fadeOut, moveNumX, moveNumY, moveShellX, moveShellY, riseShell, shellGroundPath, shootShellDown;
    String player1name, player2name;
    View decorView;
    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    private SeekBar.OnSeekBarChangeListener windPower = new SeekBar.OnSeekBarChangeListener() {
        //This event listener listens for the player to interact with the power bar
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //When the progress bar is changed, it changes the number above it to the correct value
            if (!isMenuVisible) {
                power_value.setText("Power: " + progress);
            }
        }

        //Unnecessary methods
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
    };
    private SeekBar.OnSeekBarChangeListener windAngle = new SeekBar.OnSeekBarChangeListener() {
        //This event listener listens for the player to interact with the Angle bar
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //When the progress bar is changed, it changes the number above it to the correct value, and also sets the angle of the tank's cannon
            if (!isMenuVisible) {
                angle_value.setText("Angle: " + progress);
                tank_angle_animate = new AnimatorSet();
                if (!player_turn) {
                    tank1__cannon_angle = ObjectAnimator.ofFloat(tank1_cannon, "rotation", tank1_angleValue + progress - 90);
                    tank1__cannon_angle.setDuration(0);
                    tank_angle_animate.play(tank1__cannon_angle);
                    shotAngle1 = progress;
                } else if (player_turn) {
                    tank2__cannon_angle = ObjectAnimator.ofFloat(tank2_cannon, "rotation", tank2_angleValue + progress - 90);
                    tank2__cannon_angle.setDuration(0);
                    tank_angle_animate.play(tank2__cannon_angle);
                    shotAngle2 = progress;
                }
                tank_angle_animate.start();
            }
        }

        //Unnecessary methods
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    //This method runs when the game boots up, it creates objects of classes, sets the current content view, and creates audio objects for use later on
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        whiteTrail = new BulletTrail(this);
        terrain = new Terrain(this);
        setContentView(R.layout.activity_main_menu);
        namesSet = false;
        fireSound = MediaPlayer.create(this, R.raw.tankfire);
        explosionSound = MediaPlayer.create(this, R.raw.explosion);
        emp = MediaPlayer.create(this, R.raw.emp_sound);
        shieldSound = MediaPlayer.create(this, R.raw.shield_sound);
    }

    //This method runs when you have exited out of the app, and you resume it again, not a reboot, just resuming the game
    protected void onResume() {
        super.onResume();

        //Resumes screen settings
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        decorView.setSystemUiVisibility(uiOptions);

        //Pauses bullets/projectiles if not in app
        gamePaused = false;
        if (bulletInAnim) {
            bulletLoop();
        }
        if (expInAnim) {
            bulletExplode();
        }
    }

    //This method runs when you exit out of the app but do not kill it
    protected void onPause() {
        super.onPause();
        gamePaused = true;
    }

    //Shows the settings menu, and sets their switches to the correct value
    public void showSettings(View view) {
        setContentView(R.layout.settings);
        ((Switch) findViewById(R.id.switch1)).setChecked(sound_check);
        ((Switch) findViewById(R.id.switch3)).setChecked(player_turn);
        ((Switch) findViewById(R.id.switch2)).setChecked(wind_check);
    }

    //This method runs when you hit the exit button in the settings menu, it gets the switch values and goes back to the title screen
    public void backHome(View view) {
        sound_check = ((Switch) findViewById(R.id.switch1)).isChecked();
        player_turn = ((Switch) findViewById(R.id.switch3)).isChecked();
        wind_check = ((Switch) findViewById(R.id.switch2)).isChecked();
        rememberTurn = player_turn;
        setContentView(R.layout.activity_main_menu);
    }

    //This method runs when you hit the Play Game button in the main menu
    public void playGame(View view) {
        setContentView(R.layout.game_setup);
        rememberTurn = player_turn;
    }

    //This method runs when you hit the back button in the game setup menu, goes back to the main menu
    public void cancel(View view) {
        setContentView(R.layout.activity_main_menu);
    }

    //This method runs when you hit the Play button in the game setup menu
    public void startGame(View view) {

        //Sets the names of the players if they have not already been set from a previous game
        if (!namesSet) {
            player1name = "Player 1";
            player2name = "Player 2";
            EditText p1 = findViewById(R.id.editText);
            EditText p2 = findViewById(R.id.editText2);
            player1name = p1.getText().toString();
            player2name = p2.getText().toString();
            if (player1name.length() == 0) {
                player1name = "No-name1";
            } else if (player1name.length() > 8) {
                player1name = player1name.substring(0, 8);
            }
            if (player2name.length() == 0) {
                player2name = "No-name2";
            } else if (player2name.length() > 8) {
                player2name = player2name.substring(0, 8);
            }
            player_turn = rememberTurn;
        }
        namesSet = true;

        //Generates terrain, selects random background, adds the background, then the terrain on top, then the bullet trail graphics, then the buttons and menus
        Terrain.generatePath();
        setContentView(R.layout.game_layout);
        View game = findViewById(R.id.game_layout);
//        int backGround = (int) (Math.random() * 3 + 1);
//        if (backGround == 1) {
//            setContentView(R.layout.back_ground1);
//        } else if (backGround == 2) {
//            setContentView(R.layout.back_ground2);
//        } else if (backGround == 3) {
//            setContentView(R.layout.back_ground3);
//        }
        setContentView(R.layout.back_ground4);
        addContentView(terrain, new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
        addContentView(whiteTrail, new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
        addContentView(game, new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
        decorView.setSystemUiVisibility(uiOptions);
        findIds();
        resetValues();
        plantTrees();
        menuVisibility(false);
    }

    //This method sets the values to what they should be upon starting a new game
    public void resetValues() {
        BulletTrail.clearArrays();
        notCrash = false;
        canMove = false;
        hitTank = false;
        busInAnim = false;
        playerEMPD = false;
        resetEMP1 = false;
        resetEMP2 = false;
        for (int i = 1; i <= 2; i++) {
            hp[i] = 300;
            shield[i] = 0;
            weaponUse[i] = 3;
        }
        for (int i = 0; i <= 4; i++) {
            tree[i] = (int) (Math.random() * 1920 + 1);
        }
        turns = 0;
        empCount = 0;
        for (int i = 1; i <= 2; i++) {
            empWait[i] = 0;
        }
        endAnim = false;
        for (int i = 1; i <= 2; i++) {
            for (int ii = 1; ii <= 4; ii++) {
                ammo[i][ii] = 2;
            }
        }
        shot_power.setProgress(50);
        shotPower1 = 50;
        shotPower2 = 50;
        shotAngle1 = 180;
        shotAngle2 = 0;
    }

    //This method declares all the view objects I'm using in the game screen
    public void findIds() {
        trees[0] = findViewById(R.id.imageView15);
        trees[1] = findViewById(R.id.imageView25);
        trees[2] = findViewById(R.id.imageView24);
        trees[3] = findViewById(R.id.imageView23);
        trees[4] = findViewById(R.id.imageView21);
        currentWeaponText = findViewById(R.id.textView5);
        currentWeaponText.setVisibility(View.INVISIBLE);
        angle_value = findViewById(R.id.textView21);
        angle_value.setVisibility(View.INVISIBLE);
        power_value = findViewById(R.id.textView22);
        power_value.setVisibility(View.INVISIBLE);
        blueShell = findViewById(R.id.imageView22);
        blueShell.setVisibility(View.INVISIBLE);
        shield_bar = findViewById(R.id.progressBar3);
        shield_bar.setVisibility(View.INVISIBLE);
        shield_value = findViewById(R.id.textView18);
        shield_value.setVisibility(View.INVISIBLE);
        capShield1 = findViewById(R.id.imageView19);
        capShield1.setVisibility(View.INVISIBLE);
        capShield2 = findViewById(R.id.imageView20);
        capShield2.setVisibility(View.INVISIBLE);
        currentWeaponAmmo = findViewById(R.id.textView17);
        currentWeaponAmmo.setVisibility(View.INVISIBLE);
        flashScreen = findViewById(R.id.textView16);
        flashScreen.setVisibility(View.INVISIBLE);
        menu_exit = findViewById(R.id.button8);
        menu_resume = findViewById(R.id.button9);
        menu_shader = findViewById(R.id.textView23);
        hp_value = findViewById(R.id.textView20);
        hp_value.setVisibility(View.INVISIBLE);
        hp_bar = findViewById(R.id.progressBar);
        hp_bar.setVisibility(View.INVISIBLE);
        start_gameShader = findViewById(R.id.textView27);
        start_gameShader.setVisibility(View.VISIBLE);
        start_game = findViewById(R.id.button10);
        start_game.setVisibility(View.VISIBLE);
        bullet = findViewById(R.id.imageView17);
        bullet.setVisibility(View.INVISIBLE);
        move_left = findViewById(R.id.button5);
        move_left.setVisibility(View.INVISIBLE);
        move_right = findViewById(R.id.button6);
        move_right.setVisibility(View.INVISIBLE);
        fire = findViewById(R.id.button3);
        fire.setVisibility(View.INVISIBLE);
        menu = findViewById(R.id.button7);
        menu.setVisibility(View.INVISIBLE);
        bulletInMotion = findViewById(R.id.progressBar2);
        bulletInMotion.setVisibility(View.INVISIBLE);
        explosion = findViewById(R.id.imageView10);
        explosion.setVisibility(View.INVISIBLE);
        tankExplosion = findViewById(R.id.imageView);
        damageNum = findViewById(R.id.textView2);
        damageNum.setVisibility(View.INVISIBLE);
        battleBus = findViewById(R.id.imageView9);
        battleBus.setVisibility(View.INVISIBLE);
        banner = findViewById(R.id.imageView11);
        banner.setVisibility(View.INVISIBLE);
        turn = findViewById(R.id.textView6);
        player_name = findViewById(R.id.textView10);
        player_name.setVisibility(View.INVISIBLE);
        yesPlayAgain = findViewById(R.id.button);
        yesPlayAgain.setVisibility(View.INVISIBLE);
        noPlayAgain = findViewById(R.id.button2);
        noPlayAgain.setVisibility(View.INVISIBLE);
        supplyDrop = findViewById(R.id.imageView8);
        oppHealth = findViewById(R.id.textView4);
        interact = findViewById(R.id.button4);
        interact.setVisibility(View.INVISIBLE);
        weapons = findViewById(R.id.button11);
        weapons.setVisibility(View.INVISIBLE);
        empSym = findViewById(R.id.imageView16);
        empSym.setVisibility(View.INVISIBLE);
        shieldSym = findViewById(R.id.imageView14);
        shieldSym.setVisibility(View.INVISIBLE);
        bulletSym = findViewById(R.id.imageView13);
        bulletSym.setVisibility(View.INVISIBLE);
        shellSym = findViewById(R.id.imageView12);
        shellSym.setVisibility(View.INVISIBLE);
        currentWeapon = findViewById(R.id.imageView18);
        currentWeapon.setVisibility(View.INVISIBLE);
        weaponBack = findViewById(R.id.button13);
        weaponBack.setVisibility(View.INVISIBLE);
        empAmmo = findViewById(R.id.textView7);
        empAmmo.setVisibility(View.INVISIBLE);
        shieldAmmo = findViewById(R.id.textView8);
        shieldAmmo.setVisibility(View.INVISIBLE);
        shellAmmo = findViewById(R.id.textView15);
        shellAmmo.setVisibility(View.INVISIBLE);
        bulletAmmo = findViewById(R.id.textView9);
        bulletAmmo.setVisibility(View.INVISIBLE);
        tank1 = findViewById(R.id.imageView5);
        tank2 = findViewById(R.id.imageView4);
        tank1_cannon = findViewById(R.id.imageView6);
        tank2_cannon = findViewById(R.id.imageView7);
        shot_power = findViewById(R.id.seekBar3);
        shot_power.setVisibility(View.INVISIBLE);
        shot_angle = findViewById(R.id.seekBar2);
        shot_angle.setVisibility(View.INVISIBLE);
        isMenuVisible = true;
    }

    //This method runs when the Start Game button is pressed, sets certain views visible or invisible, moves tanks to their start location
    public void initGame2(View view) {
        currentWeaponText.setVisibility(View.VISIBLE);
        angle_value.setVisibility(View.VISIBLE);
        power_value.setVisibility(View.VISIBLE);
        shot_power.setVisibility(View.VISIBLE);
        shot_angle.setVisibility(View.VISIBLE);
        currentWeaponAmmo.setVisibility(View.VISIBLE);
        currentWeapon.setVisibility(View.VISIBLE);
        tank1.setVisibility(View.VISIBLE);
        tank2.setVisibility(View.VISIBLE);
        tank1_cannon.setVisibility(View.VISIBLE);
        tank2_cannon.setVisibility(View.VISIBLE);
        start_gameShader.setVisibility(View.INVISIBLE);
        start_game.setVisibility(View.INVISIBLE);
        hp_bar.setVisibility(View.VISIBLE);
        hp_value.setVisibility(View.VISIBLE);
        shield_value.setVisibility(View.VISIBLE);
        shield_bar.setVisibility(View.VISIBLE);
        move_right.setVisibility(View.VISIBLE);
        move_left.setVisibility(View.VISIBLE);
        fire.setVisibility(View.VISIBLE);
        menu.setVisibility(View.VISIBLE);
        weapons.setVisibility(View.VISIBLE);
        player_name.setVisibility(View.VISIBLE);
        tank1x = Terrain.c + (int) (Math.random() * 300 - 150);
        tank2x = Terrain.b + (int) (Math.random() * 300 - 150);
        isMenuVisible = false;
        moveTank(0);
        player_turn = !player_turn;
        moveTank(0);
        player_turn = !player_turn;
        playerTurn();
    }

    //This method moves all of the trees to their respective locations on the terrain. Requires being an environmentalist
    public void plantTrees() {
        ObjectAnimator[] moveTreeX = new ObjectAnimator[5], moveTreeY = new ObjectAnimator[5];
        for (int i = 0; i <= 4; i++) {
            moveTreeX[i] = ObjectAnimator.ofFloat(trees[i], "translationX", tree[i] - 50);
            moveTreeY[i] = ObjectAnimator.ofFloat(trees[i], "translationY", (Terrain.y[tree[i]] * -1) + 10);
        }
        AnimatorSet moveAllTrees = new AnimatorSet();
        moveAllTrees.play(moveTreeX[0]).with(moveTreeX[1]).with(moveTreeX[2]).with(moveTreeX[3]).with(moveTreeX[4]).with(moveTreeY[0]).with(moveTreeY[1]).with(moveTreeY[2]).with(moveTreeY[3]).with(moveTreeY[4]);
        moveAllTrees.setDuration(0);
        moveAllTrees.start();
    }

    //This method runs at the start of each player's turn, sets important values
    public void playerTurn() {

        //Sets the names of the current player's turn in the top left
        if (!player_turn) {
            player_name.setText(player1name);
            turn.setText(player1name + "'s Turn");
        } else if (player_turn) {
            player_name.setText(player2name);
            turn.setText(player2name + "'s Turn");
        }

        //Makes the player turn prompt appear for 1 second, then turn invisible again
        turn.setVisibility(View.VISIBLE);
        Handler fadeTurn = new Handler();
        fadeTurn.postDelayed(new Runnable() {
            @Override
            public void run() {
                turn.setVisibility(View.INVISIBLE);
            }
        }, 1000);

        //Checks emp effects and whether next turn is still the same player's turn
        canMove = true;
        empCheck();

        //Sets the current weapon to the designated one, changes the image to that one, sets the fire button text to "use" if it's an emp or shield
        fire.setText("FIRE");
        if (!player_turn) {
            if (weaponUse[1] == 3) {
                currentWeapon.setImageResource(R.drawable.bullet);
                currentWeaponAmmo.setText("∞");
            } else if (weaponUse[1] == 1) {
                currentWeapon.setImageResource(R.drawable.emp);
                currentWeaponAmmo.setText(String.format("%d", ammo[1][1]));
                fire.setText("USE");
            } else if (weaponUse[1] == 2) {
                currentWeapon.setImageResource(R.drawable.shield);
                currentWeaponAmmo.setText(String.format("%d", ammo[1][2]));
                fire.setText("USE");
            } else if (weaponUse[1] == 4) {
                currentWeaponAmmo.setText(String.format("%d", ammo[1][3]));
            } else if (weaponUse[1] == 5) {
                currentWeapon.setImageResource(R.drawable.blueshell);
                currentWeaponAmmo.setText(String.format("%d", ammo[1][4]));
            }
        } else if (player_turn) {
            if (weaponUse[2] == 3) {
                currentWeapon.setImageResource(R.drawable.bullet);
                currentWeaponAmmo.setText("∞");
            } else if (weaponUse[2] == 1) {
                currentWeapon.setImageResource(R.drawable.emp);
                currentWeaponAmmo.setText(String.format("%d", ammo[2][1]));
                fire.setText("USE");
            } else if (weaponUse[2] == 2) {
                currentWeapon.setImageResource(R.drawable.shield);
                currentWeaponAmmo.setText(String.format("%d", ammo[2][2]));
                fire.setText("USE");
            } else if (weaponUse[2] == 4) {
                currentWeaponAmmo.setText(String.format("%d", ammo[2][3]));
            } else if (weaponUse[2] == 5) {
                currentWeapon.setImageResource(R.drawable.blueshell);
                currentWeaponAmmo.setText(String.format("%d", ammo[2][4]));
            }
        }

        //Check if shield value is less than 0, if so, remove shield graphic
        if (shield[1] <= 0) {
            capShield1.setVisibility(View.INVISIBLE);
        }
        if (shield[2] <= 0) {
            capShield2.setVisibility(View.INVISIBLE);
        }

        //Creates a random wind value from -20 to 20 if wind is enabled
        if (wind_check) {
            windFactor = (int) (Math.random() * 41 - 20);
            windFactor = windFactor - (int) (Math.random() + 0.5);
            wind_factor = findViewById(R.id.textView14);
            wind_direction = findViewById(R.id.imageView3);
            wind_factor.setText(String.format("%d", Math.abs(windFactor)));     //Set wind number to its correct value

            //Set the wind direction image
            if (windFactor < 0) {
                wind_direction.setImageResource(R.drawable.leftpointer);
            } else if (windFactor > 0) {
                wind_direction.setImageResource(R.drawable.rightpointer);
            } else {
                wind_direction.setImageResource(R.drawable.nowind);
            }
        } else {
            windFactor = 0;
            wind_direction = findViewById(R.id.imageView3);
            wind_direction.setVisibility(View.INVISIBLE);
            wind_factor = findViewById(R.id.textView14);
            wind_factor.setVisibility(View.INVISIBLE);
            wind_text = findViewById(R.id.textView13);
            wind_text.setVisibility(View.INVISIBLE);
        }

        //Turn on progress bar listeners
        shot_power.setOnSeekBarChangeListener(windPower);
        shot_angle.setOnSeekBarChangeListener(windAngle);

        //Move bullet to the current tank while invisible to prepare for the next shot
        if (!player_turn) {
            shot_power.setProgress(shotPower1);
            shot_angle.setProgress(shotAngle1);
            projX = ObjectAnimator.ofFloat(bullet, "translationX", tank1x + 50 - 70);
            projY = ObjectAnimator.ofFloat(bullet, "translationY", tank1x + 50 - 70);
        } else if (player_turn) {
            shot_power.setProgress(shotPower2);
            shot_angle.setProgress(shotAngle2);
            projX = ObjectAnimator.ofFloat(bullet, "translationX", tank2x + 50 - 70);
            projY = ObjectAnimator.ofFloat(bullet, "translationY", tank2x + 50 - 70);
        }
        projX.setDuration(0);
        projY.setDuration(0);
        shoot_bullet = new AnimatorSet();
        shoot_bullet.play(projX).with(projY);
        shoot_bullet.start();

        //Set health and shield to their correct values
        if (!player_turn) {
            hp_value.setText("HP: " + hp[1]);
            hp_bar.setProgress(hp[1]);
            oppHealth.setText(player2name + "'s Health: " + hp[2]);
            shield_value.setText("SHIELD: " + shield[1]);
            shield_bar.setProgress(shield[1]);
        } else if (player_turn) {
            hp_value.setText("HP: " + hp[2]);
            hp_bar.setProgress(hp[2]);
            oppHealth.setText(player1name + "'s Health: " + hp[1]);
            shield_value.setText("SHIELD: " + shield[2]);
            shield_bar.setProgress(shield[2]);
        }

        //Set move buttons visible because you get to move once per turn
        move_left.setVisibility(View.VISIBLE);
        move_right.setVisibility(View.VISIBLE);

        //Check whether the supply drop is in range of the current tank
        checkDropDistance();
    }

    //This method checks all emp values, for example, if the emp is in effect, count one turn. Also checks the emp cooldown so you can;t use them consecutively
    public void empCheck() {
        if (empCount == 1) {
            playerEMPD = false;
        }
        if (playerEMPD) {
            empCount++;
        } else {
            empCount = 0;
        }
        if (empWait[1] == 5) {
            resetEMP1 = false;
        }
        if (empWait[2] == 5) {
            resetEMP2 = false;
        }
        if (resetEMP1) {
            empWait[1]++;
        } else {
            empWait[1] = 0;
        }
        if (resetEMP2) {
            empWait[2]++;
        } else {
            empWait[2] = 0;
        }
    }

    //This method runs when the Move Right button is pressed
    public void tankMoveRight(View view) {
        i = 0;

        //Makes the buttons invisible so that you can't touch it again
        move_left.setVisibility(View.INVISIBLE);
        move_right.setVisibility(View.INVISIBLE);
        fire.setVisibility(View.INVISIBLE);
        moveTankRightDelay();
    }

    //This method runs when the Move Left button is pressed
    public void tankMoveLeft(View view) {
        i = 0;

        //Makes the buttons invisible so that you can't touch it again
        move_left.setVisibility(View.INVISIBLE);
        move_right.setVisibility(View.INVISIBLE);
        fire.setVisibility(View.INVISIBLE);
        moveTankLeftDelay();
    }

    //This method is only there to run the moveTank() method 4 times with a delay in between
    public void moveTankRightDelay() {

        //Runs the code inside run() after 0.2 seconds 4 times
        Handler moveTank = new Handler();
        moveTank.postDelayed(new Runnable() {
            @Override
            public void run() {
                i++;
                if (!isMenuVisible) {
                    //If the tank is between the screen limits and the other tank is not in the way
                    if (tank1x <= 1841 && !player_turn && (tank1.getRight() + tank1x) < tank2x) {
                        tank1x = tank1x + 20;   //Actually increment the value
                    }
                    if (tank2x <= 1841 && player_turn) {
                        tank2x = tank2x + 20;   //Actually increment the value
                    }
                    moveTank(200);      //Move the tank to its new location
                }
                if (i <= 4) {
                    moveTankRightDelay();   //Run the method again in order to repeat it 4 times
                } else {
                    fire.setVisibility(View.VISIBLE);
                }
            }
        }, 200);
    }

    //Runs the code inside run() after 0.2 seconds 4 times
    public void moveTankLeftDelay() {

        //Runs the code inside run() after 0.2 seconds 4 times
        Handler moveTank = new Handler();
        moveTank.postDelayed(new Runnable() {
            @Override
            public void run() {
                i++;
                if (!isMenuVisible) {
                    //If the tank is between the screen limits and the other tank is not in the way
                    if (tank1x >= 61 && !player_turn) {
                        tank1x = tank1x - 20;       //Actually increment the value
                    }
                    if (tank2x >= 61 && player_turn && tank2x > (tank1.getRight() + tank1x)) {
                        tank2x = tank2x - 20;       //Actually increment the value
                    }
                    moveTank(200);      //Move the tank to its new location
                }
                if (i <= 4) {
                    moveTankLeftDelay();        //Run the method again in order to repeat it 4 times
                } else {
                    fire.setVisibility(View.VISIBLE);
                }
            }
        }, 200);
    }

    //This method physically move the tank to its updated location
    public void moveTank(int duration) {
        if (!player_turn) {
            tank1y = (Terrain.y[tank1x] + 5) * -1;      //Update the y value

            //Move the tank to its coordinates
            move_tank1X = ObjectAnimator.ofFloat(tank1, "translationX", tank1x - 50);
            move_tank1Y = ObjectAnimator.ofFloat(tank1, "translationY", tank1y);

            //Move the tank's cannon to its coordinates
            move_tank_cannon1X = ObjectAnimator.ofFloat(tank1_cannon, "translationX", tank1x - 50);
            move_tank_cannon1Y = ObjectAnimator.ofFloat(tank1_cannon, "translationY", tank1y);

            //Move the shield icon to its coordinates
            moveShieldX = ObjectAnimator.ofFloat(capShield1, "translationX", tank1x - 50);
            moveShieldY = ObjectAnimator.ofFloat(capShield1, "translationY", tank1y + 10);

            //Calculate the slope of the terrain below the tank so that the tank can rotate on it
            slopx1 = (tank1.getLeft() + tank1x) - 50;
            slopx2 = (tank1.getRight() + tank1x) - 50;
            if (tank1.getLeft() + tank1x - 50 > 0) {
                slopy1 = (Terrain.y[tank1.getLeft() + tank1x - 50]);
            }
            if (tank1.getRight() + tank1x - 50 < 1920) {
                slopy2 = (Terrain.y[tank1.getRight() + tank1x - 50]);
            }
            slope = (slopy2 - slopy1) / (slopx2 - slopx1);
            tank1_angleValue = (int) Math.toDegrees(Math.atan(slope)) * -1;

            //Adjust the tank's cannon to the proper angle based on its rotation on the ground already
            tank1_angle = ObjectAnimator.ofFloat(tank1, "rotation", tank1_angleValue);
            tank1__cannon_angle = ObjectAnimator.ofFloat(tank1_cannon, "rotation", tank1_angleValue + (shotAngle1 - 90));
        } else if (player_turn) {
            tank2y = (Terrain.y[tank2x] + 5) * -1;      //Update the y value

            //Move the tank to its coordinates
            move_tank2X = ObjectAnimator.ofFloat(tank2, "translationX", tank2x - 50);
            move_tank2Y = ObjectAnimator.ofFloat(tank2, "translationY", tank2y);

            //Move the tank's cannon to its coordinates
            move_tank_cannon2X = ObjectAnimator.ofFloat(tank2_cannon, "translationX", tank2x - 50);
            move_tank_cannon2Y = ObjectAnimator.ofFloat(tank2_cannon, "translationY", tank2y);

            //Move the shield icon to its coordinates
            moveShieldX = ObjectAnimator.ofFloat(capShield2, "translationX", tank2x - 50);
            moveShieldY = ObjectAnimator.ofFloat(capShield2, "translationY", tank2y + 10);

            //Calculate the slope of the terrain below the tank so that the tank can rotate on it
            slopx1 = (tank2.getLeft() + tank2x) - 50;
            slopx2 = (tank2.getRight() + tank2x) - 50;
            if (tank2.getLeft() + tank2x - 50 > 0) {
                slopy1 = (Terrain.y[tank2.getLeft() + tank2x - 50]);
            }
            if (tank2.getRight() + tank2x - 50 < 1920) {
                slopy2 = (Terrain.y[tank2.getRight() + tank2x - 50]);
            }
            slope = (slopy2 - slopy1) / (slopx2 - slopx1);
            tank2_angleValue = (int) Math.toDegrees(Math.atan(slope)) * -1;

            //Adjust the tank's cannon to the proper angle based on its rotation on the ground already
            tank2_angle = ObjectAnimator.ofFloat(tank2, "rotation", tank2_angleValue);
            tank2__cannon_angle = ObjectAnimator.ofFloat(tank2_cannon, "rotation", tank2_angleValue + (shotAngle2 - 90));
        }

        //Actually start the animation of moving the tank
        tank_animate = new AnimatorSet();
        if (!player_turn) {
            tank_animate.play(move_tank1X).with(move_tank1Y).with(tank1_angle).with(move_tank_cannon1X).with(move_tank_cannon1Y).with(tank1__cannon_angle).with(moveShieldX).with(moveShieldY);
        } else if (player_turn) {
            tank_animate.play(move_tank2X).with(move_tank2Y).with(tank2_angle).with(move_tank_cannon2X).with(move_tank_cannon2Y).with(tank2__cannon_angle).with(moveShieldX).with(moveShieldY);
        }
        tank_animate.setDuration(duration);
        tank_animate.start();
        canMove = false;
        checkDropDistance();       //Check whether the supply drop is now in range of the tank
    }

    //This method runs when the fire button is touched. It dictates what to do depending on the weapon you have selected, also keeps track of the turns
    public void fire(View view) {
        if (!isMenuVisible) {
            if (!player_turn) {
                if (weaponUse[1] == 3) {        //Fire bullet

                    //Get the power and angle values from the progress bar so that the BulletCalculations class can use it
                    shotPower1 = shot_power.getProgress();
                    shotAngle1 = shot_angle.getProgress();
                    shot1[0] = shotPower1;
                    if (shot1[0] == 0) {
                        shot1[0]++;
                    }
                    shot1[1] = shotAngle1 + tank1_angleValue;
                    fired = true;
                    fireBullet();
                } else if (weaponUse[1] == 1 && ammo[1][1] != 0 && !resetEMP1) {    //Use EMP
                    fired = true;
                    useEMP();
                    ammo[1][1]--;
                } else if (weaponUse[1] == 2 && ammo[1][2] != 0 && shield[1] == 0) {       //Use Shield
                    fired = true;
                    useShield();
                    ammo[1][2]--;
                } else if (weaponUse[1] == 5 && ammo[1][4] != 0) {      //Fire Blue Shell
                    fired = true;
                    useBlueShell();
                    ammo[1][4]--;
                } else {
                    turn.setText("No Ammo");        //If there is no ammo of the current weapon
                    if (weaponUse[1] == 1 && resetEMP1 && ammo[1][1] > 0) {     //If the EMP is still recharging
                        turn.setText("EMP Recharging");
                    }
                    if (weaponUse[1] == 2 && shield[1] > 0 && ammo[1][2] > 0) {     //If the shield is still active
                        turn.setText("Shield Is Active Already");
                    }

                    //Display the message for 1.5 seconds
                    turn.setVisibility(View.VISIBLE);
                    Handler errorMessage = new Handler();
                    errorMessage.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            turn.setVisibility(View.INVISIBLE);
                        }
                    }, 1500);
                }
            } else if (player_turn) {
                if (weaponUse[2] == 3) {    //Fire bullet

                    //Get the power and angle values from the progress bar so that the BulletCalculations class can use it
                    shotPower2 = shot_power.getProgress();
                    shotAngle2 = shot_angle.getProgress();
                    shot2[0] = shotPower2;
                    if (shot2[0] == 0) {
                        shot2[0]++;
                    }
                    shot2[1] = shotAngle2 + tank2_angleValue;
                    fired = true;
                    fireBullet();
                } else if (weaponUse[2] == 1 && ammo[2][1] != 0 && !resetEMP2) {    //Use EMP
                    fired = true;
                    useEMP();
                    ammo[2][1]--;
                } else if (weaponUse[2] == 2 && ammo[2][2] != 0 && shield[2] == 0) {    //Use Shield
                    fired = true;
                    useShield();
                    ammo[2][2]--;
                } else if (weaponUse[2] == 5 && ammo[2][4] != 0) {      //Fire Blue Shell
                    fired = true;
                    useBlueShell();
                    ammo[2][4]--;
                } else {
                    turn.setText("No Ammo");        //If there is no ammo of the current weapon
                    if (weaponUse[2] == 1 && resetEMP2 && ammo[2][1] > 0) {     //If the EMP is still recharging
                        turn.setText("EMP Recharging");
                    }
                    if (weaponUse[2] == 2 && shield[2] > 0 && ammo[2][2] > 0) {     //If the shield is still active
                        turn.setText("Shield Is Active Already");
                    }

                    //Display the message for 1.5 seconds
                    turn.setVisibility(View.VISIBLE);
                    Handler errorMessage = new Handler();
                    errorMessage.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            turn.setVisibility(View.INVISIBLE);
                        }
                    }, 1500);
                }
            }

            //Keeps track of turns
            if (fired) {
                turns++;
                fired = false;
            }
        }
    }

    //This method plays specific sounds when called depending on the parameter/information given to it
    public void playSounds(int whichSound) {
        if (sound_check) {      //Checks to see if sound has been turned on or not
            if (whichSound == 1) {
                fireSound.start();      //This method .start() plays the actual sound
            } else if (whichSound == 2) {
                explosionSound.start();
            } else if (whichSound == 3) {
                emp.start();
            } else if (whichSound == 4) {
                shieldSound.start();
            }
        }
    }

    //This method is called when the player wants to fire a regular bullet, it sets up part of the process
    public void fireBullet() {
        playSounds(1);      //Play the bullet fire sound
        bullet.setVisibility(View.VISIBLE);
        bulletInMotion.setVisibility(View.VISIBLE);
        BulletCalculations.bulletSetup();       //Sets all the initial bullet values in a separate class

        //Animates the bullet to the tank's position
        projX = ObjectAnimator.ofFloat(bullet, "translationX", BulletCalculations.x);
        projY = ObjectAnimator.ofFloat(bullet, "translationY", BulletCalculations.y);
        projAngle = ObjectAnimator.ofFloat(bullet, "rotation", BulletCalculations.bulletAngle);
        shoot_bullet = new AnimatorSet();
        shoot_bullet.setDuration(0);
        shoot_bullet.play(projX).with(projY).with(projAngle);
        shoot_bullet.start();

        //Starts the main bullet loop
        bulletLoop();
        hideButtons(true);
        bulletInAnim = true;
    }

    //This method runs when the player wants to use an EMP
    public void useEMP() {
        playSounds(3);      //Plays the EMP sound
        hideButtons(true);

        //Sets the EMP initial values, sets emp to true so the player can have an extra turn
        playerEMPD = true;
        if (!player_turn) {
            resetEMP1 = true;
        } else if (player_turn) {
            resetEMP2 = true;
        }

        //Animates a flash screen. Turns white in 0.25 seconds then fades in 2.5 seconds
        bulletInMotion.setVisibility(View.VISIBLE);
        flashScreen.setVisibility(View.VISIBLE);
        ObjectAnimator hidden = ObjectAnimator.ofFloat(flashScreen, "alpha", 0);
        hidden.setDuration(0);
        hidden.start();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(flashScreen, "alpha", 1);
        fadeIn.setDuration(250);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(flashScreen, "alpha", 0);
        fadeOut.setDuration(2500);
        AnimatorSet flash = new AnimatorSet();
        flash.play(fadeOut).after(fadeIn).after(hidden);
        flash.start();
        Handler resetShader = new Handler();
        resetShader.postDelayed(new Runnable() {
            @Override
            public void run() {
                bulletInMotion.setVisibility(View.INVISIBLE);
                flashScreen.setVisibility(View.INVISIBLE);
                switchTurn();
                hideButtons(false);
            }
        }, 2500);
    }

    //This method runs when the player uses a shield, the shield adds an extra 100 health
    public void useShield() {
        playSounds(4);      //Play Shield sound

        //Sets the shield value to 100, and makes the shield icon visible on top of the tank
        if (!player_turn) {
            shield[1] = 100;
            moveShieldX = ObjectAnimator.ofFloat(capShield1, "translationX", tank1x - 50);
            moveShieldY = ObjectAnimator.ofFloat(capShield1, "translationY", tank1y + 10);
            capShield1.setVisibility(View.VISIBLE);
        } else if (player_turn) {
            shield[2] = 100;
            moveShieldX = ObjectAnimator.ofFloat(capShield2, "translationX", tank2x - 50);
            moveShieldY = ObjectAnimator.ofFloat(capShield2, "translationY", tank2y + 10);
            capShield2.setVisibility(View.VISIBLE);
        }
        moveShield = new AnimatorSet();
        moveShield.play(moveShieldX).with(moveShieldY);
        moveShield.setDuration(0);
        moveShield.start();
        shield_bar.setProgress(100);
        shield_value.setText("SHIELD: 100");
        switchTurn();       //Switches the turn
    }

    //This method runs when the player uses a blue shell
    public void useBlueShell() {
        playSounds(1);      //Play the Blue Shell sound (same as the bullet sound)
        Path shellPath = new Path();        //Creates an empty path
        shellPath.reset();      //Clears the path from any previous ones
        hideButtons(true);
        blueShell.setVisibility(View.VISIBLE);
        BulletCalculations.magnitude = 35;      //Sets the damage to 35
        BulletCalculations.subtractDamage(!player_turn);        //Subtracts the damage from the opposite player

        if (!player_turn) {
            //Makes the shell face the right way
            blueShell.setScaleX(-1);

            //Makes the sell rise above the ground
            moveShellX = ObjectAnimator.ofFloat(blueShell, "translationX", tank1x - 50);
            moveShellY = ObjectAnimator.ofFloat(blueShell, "translationY", tank1y - 10);
            riseShell = ObjectAnimator.ofFloat(blueShell, "translationY", tank1y - 290);

            //Defines the path the shell will take to the opposing player
            shellPath.moveTo(tank1x - 50, tank1y - 290);
            for (int i = tank1x - 50; i <= tank2x - 50; i++) {
                shellPath.lineTo(i, Terrain.y[i] * -1 - 290);
            }

            //Animation along this new path
            shellGroundPath = ObjectAnimator.ofFloat(blueShell, "translationX", "translationY", shellPath);
            shootShellDown = ObjectAnimator.ofFloat(blueShell, "translationY", tank2y - 10);

            //Moves the explosion animation to where the shell lands
            expX = ObjectAnimator.ofFloat(explosion, "translationX", tank2x - 50);
            expY = ObjectAnimator.ofFloat(explosion, "translationY", tank2y - 10);
        } else if (player_turn) {
            //Makes the shell face the right way
            blueShell.setScaleX(1);

            //Makes the sell rise above the ground
            moveShellX = ObjectAnimator.ofFloat(blueShell, "translationX", tank2x - 50);
            moveShellY = ObjectAnimator.ofFloat(blueShell, "translationY", tank2y - 10);
            riseShell = ObjectAnimator.ofFloat(blueShell, "translationY", tank2y - 290);

            //Defines the path the shell will take to the opposing player
            shellPath.moveTo(tank2x - 50, tank2y - 290);
            for (int i = tank2x - 50; i >= tank1x - 50; i--) {
                shellPath.lineTo(i, Terrain.y[i] * -1 - 290);
            }

            //Animation along this new path
            shellGroundPath = ObjectAnimator.ofFloat(blueShell, "translationX", "translationY", shellPath);
            shootShellDown = ObjectAnimator.ofFloat(blueShell, "translationY", tank1y - 10);

            //Moves the explosion animation to where the shell lands
            expX = ObjectAnimator.ofFloat(explosion, "translationX", tank1x - 50);
            expY = ObjectAnimator.ofFloat(explosion, "translationY", tank1y - 10);
        }

        //Animates the shell to the tank location
        blueShellPath = new AnimatorSet();
        blueShellPath.play(moveShellX).with(moveShellY);
        blueShellPath.setDuration(0);
        blueShellPath.start();

        //Makes the shell rise up in the air
        blueShellPath = new AnimatorSet();
        blueShellPath.play(riseShell);
        blueShellPath.setDuration(2500);
        blueShellPath.start();

        //Makes the shell move along the path after a certain amount of time
        Handler moveShell = new Handler();
        moveShell.postDelayed(new Runnable() {
            @Override
            public void run() {
                blueShellPath = new AnimatorSet();
                blueShellPath.play(shellGroundPath);
                blueShellPath.setDuration(5000);
                blueShellPath.start();
            }
        }, 2500);

        //Makes the shell shoot down onto the opposing tank after a certain amount of time
        Handler shootShell = new Handler();
        shootShell.postDelayed(new Runnable() {
            @Override
            public void run() {
                blueShellPath = new AnimatorSet();
                blueShellPath.play(shootShellDown);
                blueShellPath.setDuration(500);
                blueShellPath.start();
            }
        }, 7500);

        //Makes the shell disappear, sets up the explosion animation and checks win conditions after a certain amount of time
        Handler endShellAnim = new Handler();
        endShellAnim.postDelayed(new Runnable() {
            @Override
            public void run() {
                playSounds(2);
                explosion.setVisibility(View.VISIBLE);
                expFrame = 1;
                expInAnim = true;
                moveExp = new AnimatorSet();
                moveExp.setDuration(0);
                moveExp.play(expX).with(expY);
                moveExp.start();
                bulletExplode();
                numberDamage();
                hideButtons(false);
                blueShell.setVisibility(View.INVISIBLE);
                BulletCalculations.playerWin();
                if (player_won != 0) {
                    eventTankWin();
                } else {
                    switchTurn();
                }
            }
        }, 8000);
    }

    //This method animates the supply drop dropping from the battle bus to the ground
    public void dropAnim() {

        //Animates the bus the position where it will drop the supply drop, takes 7.5 seconds
        battleBus.setVisibility(View.VISIBLE);
        ObjectAnimator moveBusBack = ObjectAnimator.ofFloat(battleBus, "translationX", -302);
        moveBusBack.setDuration(0);
        ObjectAnimator moveBus = ObjectAnimator.ofFloat(battleBus, "translationX", SupplyDrop.supplyPosition + 200);
        moveBus.setDuration(7500);
        AnimatorSet setBusPath = new AnimatorSet();
        setBusPath.play(moveBus).after(moveBusBack);
        setBusPath.start();

        //Runs the code after the blue shell has risen above the ground
        Handler finishAnim = new Handler();
        finishAnim.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Moves the battle bus off screen
                ObjectAnimator moveBus = ObjectAnimator.ofFloat(battleBus, "translationX", 2500);
                moveBus.setDuration(7500);

                //Moves the supply drop to its x-coordinate instantly
                ObjectAnimator moveDrop = ObjectAnimator.ofFloat(supplyDrop, "translationX", SupplyDrop.supplyPosition - 84);
                moveDrop.setDuration(0);

                //Make drop invisible if the Winner Winner Chicken Dinner Animation is playing so it doesn't drop
                if (!endAnim) {
                    supplyDrop.setVisibility(View.VISIBLE);
                }

                //Make drop go down to the terrain
                ObjectAnimator descendDrop = ObjectAnimator.ofFloat(supplyDrop, "translationY", 1080 - 498 - Math.abs(Terrain.y[SupplyDrop.supplyPosition]) - 77 + 15);
                descendDrop.setDuration(5000);

                //Actually animate all of the above
                AnimatorSet busExitDrop = new AnimatorSet();
                busExitDrop.play(moveBus).with(moveDrop).with(descendDrop);
                busExitDrop.start();

                //Check if there are any tanks near when the drop gets to the ground
                Handler ground = new Handler();
                ground.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        supplyDropped = false;
                        checkDropDistance();
                    }
                }, 5000);
            }
        }, 7500);
    }

    //This method runs when the "interact" button is pressed, it's what happens when you pick up a supply drop
    public void pickUpDrop(View view) {
        //Makes the supply drop disappear and the interact button with it
        supplyDrop.setVisibility(View.INVISIBLE);
        interact.setVisibility(View.INVISIBLE);

        //Moves the drop back to its original y-coordinate
        ObjectAnimator ascendDrop = ObjectAnimator.ofFloat(supplyDrop, "translationY", (Math.abs(Terrain.y[SupplyDrop.supplyPosition]) - 77 + 15) * -1);
        ascendDrop.setDuration(0);

        //Moves the battle bus back to its original x-coordinate off-screen
        ObjectAnimator moveBusBack = ObjectAnimator.ofFloat(battleBus, "translationX", -302);
        moveBusBack.setDuration(0);

        //Actually animates the above animations
        AnimatorSet resetDrop = new AnimatorSet();
        resetDrop.play(ascendDrop).with(moveBusBack);
        resetDrop.start();

        //Shows which item the tank has picked up from the supply drop, also increments the ammo, and shows a prompt message
        busInAnim = false;
        if (!player_turn) {
            if (SupplyDrop.item == 1) {
                ammo[1][1] += 1;
                turn.setText(player1name + " picked up an EMP");
                currentWeaponAmmo.setText(String.format("%d", ammo[1][1]));
            } else if (SupplyDrop.item == 2) {
                ammo[1][2] += 1;
                turn.setText(player1name + " picked up a Shield");
                currentWeaponAmmo.setText(String.format("%d", ammo[1][2]));
            } else if (SupplyDrop.item == 3) {
                ammo[1][3] += 2;
                turn.setText(player1name + " picked up 2 Volcano Bombs");
                currentWeaponAmmo.setText(String.format("%d", ammo[1][3]));
            } else if (SupplyDrop.item == 4) {
                ammo[1][4] += 3;
                turn.setText(player1name + " picked up 3 Blue Shells");
                currentWeaponAmmo.setText(String.format("%d", ammo[1][4]));
            }
        } else if (player_turn) {
            if (SupplyDrop.item == 1) {
                ammo[2][1] += 1;
                turn.setText(player2name + " picked up an EMP");
                currentWeaponAmmo.setText(String.format("%d", ammo[2][1]));
            } else if (SupplyDrop.item == 2) {
                ammo[2][2] += 1;
                turn.setText(player2name + " picked up a Shield");
                currentWeaponAmmo.setText(String.format("%d", ammo[2][2]));
            } else if (SupplyDrop.item == 3) {
                ammo[2][3] += 2;
                turn.setText(player2name + " picked up 2 Volcano Bombs");
                currentWeaponAmmo.setText(String.format("%d", ammo[2][3]));
            } else if (SupplyDrop.item == 4) {
                ammo[2][4] += 3;
                turn.setText(player2name + " picked up 3 Blue Shells");
                currentWeaponAmmo.setText(String.format("%d", ammo[2][4]));
            }
        }

        //If the current weapon is the regular bullet, keep the ammo count as infinite
        if (weaponUse[1] == 3 && !player_turn) {
            currentWeaponAmmo.setText("∞");
        } else if (weaponUse[2] == 3 && player_turn) {
            currentWeaponAmmo.setText("∞");
        }

        //Makes the "Picked up [weapon]" prompt appear for 1.5 seconds, then disappear
        turn.setVisibility(View.VISIBLE);
        Handler pickUpPrompt = new Handler();
        pickUpPrompt.postDelayed(new Runnable() {
            @Override
            public void run() {
                turn.setVisibility(View.INVISIBLE);
            }
        }, 1500);
    }

    //This method checks if the supply drop is close enough to either of the tanks to pick up
    public void checkDropDistance() {
        if (busInAnim && !supplyDropped && !endAnim && !bulletInAnim) {     //Boolean variables to avoid complications with other events
            if (Math.abs(SupplyDrop.supplyPosition - tank1x) <= 60) {       //If tank is less than 60 pixels
                if (!player_turn) {
                    interact.setVisibility(View.VISIBLE);
                }
            } else if (Math.abs(tank2x - SupplyDrop.supplyPosition) <= 60) {        //If tank is less than 60 pixels
                if (player_turn) {
                    interact.setVisibility(View.VISIBLE);
                }
            } else {
                interact.setVisibility(View.INVISIBLE);
            }
        }
    }

    //This method runs when the emp icon is clicked in the weapons menu
    public void setEmp(View view) {

        //Set the current weapon and set the ammo count to the correct value, set the current weapon icon
        if (!player_turn) {
            weaponUse[1] = 1;
            currentWeaponAmmo.setText(String.format("%d", ammo[1][1]));
        } else if (player_turn) {
            weaponUse[2] = 1;
            currentWeaponAmmo.setText(String.format("%d", ammo[2][1]));
        }
        currentWeapon.setImageResource(R.drawable.emp);

        //Sets the background color of the icon to green
        setWeaponsTransparent();
        empSym.setBackgroundColor(Color.GREEN);
    }

    //This method runs when the shield icon is clicked in the weapons menu
    public void setShield(View view) {

        //Set the current weapon and set the ammo count to the correct value, set the current weapon icon
        if (!player_turn) {
            weaponUse[1] = 2;
            currentWeaponAmmo.setText(String.format("%d", ammo[1][2]));
        } else if (player_turn) {
            weaponUse[2] = 2;
            currentWeaponAmmo.setText(String.format("%d", ammo[2][2]));
        }
        currentWeapon.setImageResource(R.drawable.shield);

        //Sets the background color of the icon to green
        setWeaponsTransparent();
        shieldSym.setBackgroundColor(Color.GREEN);
    }

    //This method runs when the bullet icon is clicked in the weapons menu
    public void setBullet(View view) {

        //Set the current weapon and set the ammo count to the correct value, set the current weapon icon
        if (!player_turn) {
            weaponUse[1] = 3;
        } else if (player_turn) {
            weaponUse[2] = 3;
        }
        currentWeaponAmmo.setText("∞");
        currentWeapon.setImageResource(R.drawable.bullet);

        //Sets the background color of the icon to green
        setWeaponsTransparent();
        bulletSym.setBackgroundColor(Color.GREEN);
    }

    //This method runs when the shell icon is clicked in the weapons menu
    public void setShell(View view) {

        //Set the current weapon and set the ammo count to the correct value, set the current weapon icon
        if (!player_turn) {
            weaponUse[1] = 5;
            currentWeaponAmmo.setText(String.format("%d", ammo[1][4]));
        } else if (player_turn) {
            weaponUse[2] = 5;
            currentWeaponAmmo.setText(String.format("%d", ammo[2][4]));
        }
        currentWeapon.setImageResource(R.drawable.blueshell);

        //Sets the background color of the icon to green
        setWeaponsTransparent();
        shellSym.setBackgroundColor(Color.GREEN);
    }

    //This method sets all of the weapon icon backgrounds inside the weapons menu to transparent
    public void setWeaponsTransparent() {
        empSym.setBackgroundColor(Color.TRANSPARENT);
        shieldSym.setBackgroundColor(Color.TRANSPARENT);
        bulletSym.setBackgroundColor(Color.TRANSPARENT);
        shellSym.setBackgroundColor(Color.TRANSPARENT);
    }

    //This method animates the firing of the bullet, updates values, draws, checks win conditions, triggers supply drops and explosion animations
    public void bulletLoop() {
        if (!gamePaused && !notCrash) {
            Handler bulletDelay = new Handler();
            bulletDelay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BulletCalculations.bulletUpdate();      //Update bullet values
                    bulletRender();     //Animate bullet
                    if (!BulletCalculations.collision) {
                        bulletLoop();       //Run the loop again if there is no collision
                    } else {
                        playSounds(2);      //Play bullet fire sound
                        if (player_won != 0) {      //Checks if a player's health is < 0
                            whichExp = false;
                            if (hitTank) {
                                numberDamage();     //If the bullet hits a tank, display the amount of damage
                            }
                            eventTankWin();     //Play the ending animation because someone has won the game
                        } else {
                            if (hitTank) {
                                numberDamage();     //If the bullet hits a tank, display the amount of damage
                            }
                            if (!busInAnim && turns % 3 == 0 && !endAnim) {     //Drop a supply drop 1/2 of the time every 3 turns
                                SupplyDrop.checkTurn();     //Checks conditions for the drop
                                if (SupplyDrop.drop) {
                                    busInAnim = true;
                                    supplyDropped = true;
                                    dropAnim();     //Conditions check out, drop it!
                                }
                            }
                            hitTank = false;
                            explosionSetup();       //Sets up explosion
                            whichExp = true;
                            bulletExplode();        //Animate explosion
                            endBulletAnim();
                            switchTurn();       //Switch player turn if !EMP
                            hideButtons(false);
                        }
                    }
                }
            }, 20);
        }
    }

    //This method switches the current turn if the EMP is inactive, then runs the playerTurn() nethod
    public void switchTurn() {
        if (!playerEMPD) {
            player_turn = !player_turn;
        }
        playerTurn();
    }

    //This method displays the amount of damage done to a tank when it is hit
    public void numberDamage() {
        damageNum.setText(String.format("%d", BulletCalculations.magnitude));       //Set the number to be displayed

        //If damage is less than 100, make the color blue, else yellow
        if (BulletCalculations.magnitude >= 100) {
            damageNum.setTextColor(Color.YELLOW);
        } else {
            damageNum.setTextColor(Color.CYAN);
        }

        //Move the location of the number just above the explosion
        damageNum.setVisibility(View.VISIBLE);
        if (!player_turn && weaponUse[1] == 5) {
            moveNumX = ObjectAnimator.ofFloat(damageNum, "translationX", tank2x - 100);
            moveNumY = ObjectAnimator.ofFloat(damageNum, "translationY", tank2y - 80);
        } else if (player_turn && weaponUse[2] == 5) {
            moveNumX = ObjectAnimator.ofFloat(damageNum, "translationX", tank1x - 100);
            moveNumY = ObjectAnimator.ofFloat(damageNum, "translationY", tank1y - 80);
        } else {
            moveNumX = ObjectAnimator.ofFloat(damageNum, "translationX", BulletCalculations.x - 100);
            moveNumY = ObjectAnimator.ofFloat(damageNum, "translationY", BulletCalculations.y - 80);
        }
        moveNum = new AnimatorSet();
        moveNum.play(moveNumX).with(moveNumY);
        moveNum.setDuration(0);
        moveNum.start();

        //Make it so that the number appears, then fades out
        fadeIn = ObjectAnimator.ofFloat(damageNum, "alpha", 1.0f);
        fadeOut = ObjectAnimator.ofFloat(damageNum, "alpha", 0);
        fadeIn.setDuration(1000);
        fadeOut.setDuration(1500);
        fadeIn.start();

        //If the shield becomes < 0 then get rid of the icon
        if (shield[1] == 0) {
            capShield1.setVisibility(View.INVISIBLE);
        } else if (shield[2] == 0) {
            capShield2.setVisibility(View.INVISIBLE);
        }

        //Fade out the number after 1.5 seconds
        Handler fadeDelay = new Handler();
        fadeDelay.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOut.start();
            }
        }, 1500);
    }

    //This method runs after the bullet collides with something, it just makes the bullet invisible and hides the progress circle
    public void endBulletAnim() {
        bullet.setVisibility(View.INVISIBLE);
        bulletInMotion.setVisibility(View.INVISIBLE);
        bulletInAnim = false;
    }

    //This method runs before the explosion animation happens to set all the necessary values
    public void explosionSetup() {
        explosion.setVisibility(View.VISIBLE);
        expFrame = 1;
        expInAnim = true;

        //Moves the explosion to where the bullet collided
        expX = ObjectAnimator.ofFloat(explosion, "translationX", BulletCalculations.x - 40);
        expY = ObjectAnimator.ofFloat(explosion, "translationY", BulletCalculations.y + 20);
        moveExp = new AnimatorSet();
        moveExp.setDuration(0);
        moveExp.play(expX).with(expY);
        moveExp.start();
    }

    //This method animates the explosion, it runs itself over and over
    public void bulletExplode() {
        if (!gamePaused && !notCrash) {
            Handler explosionDelay = new Handler();
            explosionDelay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    explosionPics();       //Show an explosion frame
                    expFrame++;
                    if (expFrame <= 26) {
                        bulletExplode();    //Repeat loop as long as the explosion
                    } else {
                        //When explosions are done, set the explosion to be invisible
                        explosion.setVisibility(View.INVISIBLE);
                        expInAnim = false;
                    }
                }
            }, 100);
        }
    }

    //This method just holds all of the different explosion sprites for use one at a time
    public void explosionPics() {
        if (expFrame == 1) {
            explosion.setImageResource(R.drawable.exp1);
        } else if (expFrame == 2) {
            explosion.setImageResource(R.drawable.exp2);
        } else if (expFrame == 3) {
            explosion.setImageResource(R.drawable.exp3);
        } else if (expFrame == 4) {
            explosion.setImageResource(R.drawable.exp4);
        } else if (expFrame == 5) {
            explosion.setImageResource(R.drawable.exp5);
        } else if (expFrame == 6) {
            explosion.setImageResource(R.drawable.exp6);
        } else if (expFrame == 7) {
            explosion.setImageResource(R.drawable.exp7);
        } else if (expFrame == 8) {
            explosion.setImageResource(R.drawable.exp8);
        } else if (expFrame == 9) {
            explosion.setImageResource(R.drawable.exp9);
        } else if (expFrame == 10) {
            explosion.setImageResource(R.drawable.exp10);
        } else if (expFrame == 11) {
            explosion.setImageResource(R.drawable.exp11);
        } else if (expFrame == 12) {
            explosion.setImageResource(R.drawable.exp12);
        } else if (expFrame == 13) {
            explosion.setImageResource(R.drawable.exp13);
        } else if (expFrame == 14) {
            explosion.setImageResource(R.drawable.exp14);
        } else if (expFrame == 15) {
            explosion.setImageResource(R.drawable.exp15);
        } else if (expFrame == 16) {
            explosion.setImageResource(R.drawable.exp16);
        } else if (expFrame == 17) {
            explosion.setImageResource(R.drawable.exp17);
        } else if (expFrame == 18) {
            explosion.setImageResource(R.drawable.exp18);
        } else if (expFrame == 19) {
            explosion.setImageResource(R.drawable.exp19);
        } else if (expFrame == 20) {
            explosion.setImageResource(R.drawable.exp20);
        } else if (expFrame == 21) {
            explosion.setImageResource(R.drawable.exp21);
        } else if (expFrame == 22) {
            explosion.setImageResource(R.drawable.exp22);
        } else if (expFrame == 23) {
            explosion.setImageResource(R.drawable.exp23);
        } else if (expFrame == 24) {
            explosion.setImageResource(R.drawable.exp24);
        } else if (expFrame == 25) {
            explosion.setImageResource(R.drawable.exp25);
        }
    }

    //This method sets up all the necessary values before the tank explodes
    public void tankExplodeSetup() {
        tankExplosion.setVisibility(View.VISIBLE);
        expFrame = 1;
        expInAnim = true;

        //Moves the explosion to where the dead tank was
        if (player_won == 1) {
            expX = ObjectAnimator.ofFloat(tankExplosion, "translationX", tank2x - 80);
            expY = ObjectAnimator.ofFloat(tankExplosion, "translationY", tank2y + 80);
        } else if (player_won == 2) {
            expX = ObjectAnimator.ofFloat(tankExplosion, "translationX", tank1x - 80);
            expY = ObjectAnimator.ofFloat(tankExplosion, "translationY", tank1y + 80);
        }
        moveExp = new AnimatorSet();
        moveExp.setDuration(0);
        moveExp.play(expX).with(expY);
        moveExp.start();
    }

    //This method animates the tank exploding, it runs itself over and over
    public void tankExplode() {
        if (!gamePaused) {
            Handler tankExplosionDelay = new Handler();
            tankExplosionDelay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tankExplosionPics();        //Show an explosion frame
                    expFrame++;
                    if (expFrame <= 26) {
                        tankExplode();          //Repeat loop as long as the explosion
                    } else {

                        //When explosions are done, set the explosion to be invisible, then run the Winner animation
                        tankExplosion.setVisibility(View.INVISIBLE);
                        expInAnim = false;
                        winnerWinnerChickenDinner();
                    }
                }
            }, 120);
        }
    }

    //This method just holds all of the different explosion sprites for use one at a time like the other one
    public void tankExplosionPics() {
        if (expFrame == 1) {
            tankExplosion.setImageResource(R.drawable.exp1);
        } else if (expFrame == 2) {
            tankExplosion.setImageResource(R.drawable.exp2);
        } else if (expFrame == 3) {
            tankExplosion.setImageResource(R.drawable.exp3);
        } else if (expFrame == 4) {
            tankExplosion.setImageResource(R.drawable.exp4);
        } else if (expFrame == 5) {
            tankExplosion.setImageResource(R.drawable.exp5);
        } else if (expFrame == 6) {
            tankExplosion.setImageResource(R.drawable.exp6);
        } else if (expFrame == 7) {
            tankExplosion.setImageResource(R.drawable.exp7);
        } else if (expFrame == 8) {
            tankExplosion.setImageResource(R.drawable.exp8);
        } else if (expFrame == 9) {
            tankExplosion.setImageResource(R.drawable.exp9);
        } else if (expFrame == 10) {
            tankExplosion.setImageResource(R.drawable.exp10);
        } else if (expFrame == 11) {
            tankExplosion.setImageResource(R.drawable.exp11);
        } else if (expFrame == 12) {
            tankExplosion.setImageResource(R.drawable.exp12);
        } else if (expFrame == 13) {
            tankExplosion.setImageResource(R.drawable.exp13);
        } else if (expFrame == 14) {
            tankExplosion.setImageResource(R.drawable.exp14);
        } else if (expFrame == 15) {
            tankExplosion.setImageResource(R.drawable.exp15);
        } else if (expFrame == 16) {
            tankExplosion.setImageResource(R.drawable.exp16);
        } else if (expFrame == 17) {
            tankExplosion.setImageResource(R.drawable.exp17);
        } else if (expFrame == 18) {
            tankExplosion.setImageResource(R.drawable.exp18);
        } else if (expFrame == 19) {
            tankExplosion.setImageResource(R.drawable.exp19);
        } else if (expFrame == 20) {
            tankExplosion.setImageResource(R.drawable.exp20);
        } else if (expFrame == 21) {
            tankExplosion.setImageResource(R.drawable.exp21);
        } else if (expFrame == 22) {
            tankExplosion.setImageResource(R.drawable.exp22);
        } else if (expFrame == 23) {
            tankExplosion.setImageResource(R.drawable.exp23);
        } else if (expFrame == 24) {
            tankExplosion.setImageResource(R.drawable.exp24);
        } else if (expFrame == 25) {
            tankExplosion.setImageResource(R.drawable.exp25);
        }
    }

    //This method runs after a tank has been destroyed, sets some values, then runs the explosion animation of the tank
    public void eventTankWin() {
        menuVisibility(false);
        move_left.setVisibility(View.INVISIBLE);
        move_right.setVisibility(View.INVISIBLE);

        //Makes the destroyed tank invisible and sets their health to 0
        if (player_won == 1) {
            tank2.setVisibility(View.INVISIBLE);
            tank2_cannon.setVisibility(View.INVISIBLE);
            oppHealth.setText(player2name + "'s Health: 0");
        } else if (player_won == 2) {
            tank1.setVisibility(View.INVISIBLE);
            tank1_cannon.setVisibility(View.INVISIBLE);
            oppHealth.setText(player1name + "'s Health: 0");
        }
        bullet.setVisibility(View.INVISIBLE);
        endAnim = true;

        //Run the tank explosion animation
        tankExplodeSetup();
        tankExplode();
    }

    //This method runs when you click the Weapons button, it allows you to select your weapon of choice
    public void weaponMenu(View view) {
        menuVisibility(true);

        //Set some buttons invisible so you can't use them
        menu_exit.setVisibility(View.INVISIBLE);
        menu_resume.setVisibility(View.INVISIBLE);
        empSym.setVisibility(View.VISIBLE);
        shieldSym.setVisibility(View.VISIBLE);
        bulletSym.setVisibility(View.VISIBLE);
        shellSym.setVisibility(View.VISIBLE);
        weaponBack.setVisibility(View.VISIBLE);
        weapons.setVisibility(View.INVISIBLE);
        interact.setVisibility(View.INVISIBLE);

        //Set text values such as ammo, the prompt
        turn.setText("Pick Your Weapon Of Choice");
        turn.setVisibility(View.VISIBLE);
        if (!player_turn) {
            empAmmo.setText("EMP: " + ammo[1][1]);
            shieldAmmo.setText("Shield: " + ammo[1][2]);
            shellAmmo.setText("Blue Shells: " + ammo[1][4]);
        } else if (player_turn) {
            empAmmo.setText("EMP: " + ammo[2][1]);
            shieldAmmo.setText("Shield: " + ammo[2][2]);
            shellAmmo.setText("Blue Shells: " + ammo[2][4]);
        }

        //Make the clickable weapon icons visible
        empAmmo.setVisibility(View.VISIBLE);
        shieldAmmo.setVisibility(View.VISIBLE);
        shellAmmo.setVisibility(View.VISIBLE);
        bulletAmmo.setVisibility(View.VISIBLE);

        //Set all icon backgrounds transparent, then make the current weapon have a green background
        empSym.setBackgroundColor(Color.TRANSPARENT);
        shieldSym.setBackgroundColor(Color.TRANSPARENT);
        bulletSym.setBackgroundColor(Color.TRANSPARENT);
        shellSym.setBackgroundColor(Color.TRANSPARENT);
        if (!player_turn) {
            if (weaponUse[1] == 1) {
                empSym.setBackgroundColor(Color.GREEN);
            } else if (weaponUse[1] == 2) {
                shieldSym.setBackgroundColor(Color.GREEN);
            } else if (weaponUse[1] == 3) {
                bulletSym.setBackgroundColor(Color.GREEN);
            } else if (weaponUse[1] == 5) {
                shellSym.setBackgroundColor(Color.GREEN);
            }
        } else if (player_turn) {
            if (weaponUse[2] == 1) {
                empSym.setBackgroundColor(Color.GREEN);
            } else if (weaponUse[2] == 2) {
                shieldSym.setBackgroundColor(Color.GREEN);
            } else if (weaponUse[2] == 3) {
                bulletSym.setBackgroundColor(Color.GREEN);
            } else if (weaponUse[2] == 5) {
                shellSym.setBackgroundColor(Color.GREEN);
            }
        }
    }

    //This method exits the weapon menu
    public void backWeapon(View view) {
        menuVisibility(false);

        //Get rid of the menu options to go back to the game
        empSym.setVisibility(View.INVISIBLE);
        shieldSym.setVisibility(View.INVISIBLE);
        bulletSym.setVisibility(View.INVISIBLE);
        shellSym.setVisibility(View.INVISIBLE);
        weaponBack.setVisibility(View.INVISIBLE);
        weapons.setVisibility(View.VISIBLE);
        turn.setVisibility(View.INVISIBLE);
        empAmmo.setVisibility(View.INVISIBLE);
        shieldAmmo.setVisibility(View.INVISIBLE);
        shellAmmo.setVisibility(View.INVISIBLE);
        bulletAmmo.setVisibility(View.INVISIBLE);
        checkDropDistance();        //Check if the supply drop is in range

        //If the player selected an EMP or a Shield, set the Fire button text to "Use", else keep it "Fire"
        if (!player_turn) {
            if (weaponUse[1] == 1 || weaponUse[1] == 2) {
                fire.setText("USE");
            } else {
                fire.setText("FIRE");
            }
        } else if (player_turn) {
            if (weaponUse[2] == 1 || weaponUse[2] == 2) {
                fire.setText("USE");
            } else {
                fire.setText("FIRE");
            }
        }
    }

    //This method plays the animation where the battle bus comes with a banner saying "Winner Winner Chicken Dinner"
    public void winnerWinnerChickenDinner() {
        //Move the bus to its original spot off-screen
        ObjectAnimator moveBusBack = ObjectAnimator.ofFloat(battleBus, "translationX", -302);
        moveBusBack.setDuration(0);
        moveBusBack.start();

        battleBus.setVisibility(View.VISIBLE);
        banner.setVisibility(View.VISIBLE);

        //Animate the battle bus and banner to fly across the screen at the same time
        ObjectAnimator moveBus = ObjectAnimator.ofFloat(battleBus, "translationX", 3000);
        ObjectAnimator moveBanner = ObjectAnimator.ofFloat(banner, "translationX", 3000);
        AnimatorSet flyBanner = new AnimatorSet();
        flyBanner.play(moveBus).with(moveBanner);
        flyBanner.setDuration(15000);
        flyBanner.start();

        //After they fly across, show a dialog that asks if they would like to play another game or not
        Handler endGame = new Handler();
        endGame.postDelayed(new Runnable() {
            @Override
            public void run() {
                menu_shader.setVisibility(View.VISIBLE);
                yesPlayAgain.setVisibility(View.VISIBLE);
                noPlayAgain.setVisibility(View.VISIBLE);
                bulletInMotion.setVisibility(View.INVISIBLE);
                turn.setVisibility(View.VISIBLE);
                if (hp[1] <= 0) {
                    turn.setText(player2name + " Won! Play Again?");
                } else if (hp[2] <= 0) {
                    turn.setText(player1name + " Won! Play Again?");
                }
            }
        }, 15000);
    }

    //This method hides all the buttons on the screen so that the user can' mess anything up while the game is animating something important
    //If the parameter visible is true, hide the buttons, else put them back
    public void hideButtons(boolean visible) {
        if (visible) {
            shot_angle.setVisibility(View.INVISIBLE);
            shot_power.setVisibility(View.INVISIBLE);
            move_left.setVisibility(View.INVISIBLE);
            move_right.setVisibility(View.INVISIBLE);
            menu.setVisibility(View.INVISIBLE);
            fire.setVisibility(View.INVISIBLE);
            interact.setVisibility(View.INVISIBLE);
            weapons.setVisibility(View.INVISIBLE);
            currentWeapon.setVisibility(View.INVISIBLE);
        }

        if (!visible) {
            shot_angle.setVisibility(View.VISIBLE);
            shot_power.setVisibility(View.VISIBLE);
            if (canMove) {
                move_left.setVisibility(View.VISIBLE);
                move_right.setVisibility(View.VISIBLE);
            }
            menu.setVisibility(View.VISIBLE);
            fire.setVisibility(View.VISIBLE);
            weapons.setVisibility(View.VISIBLE);
            currentWeapon.setVisibility(View.VISIBLE);
            checkDropDistance();
        }
    }

    //This method renders the bullet from its previous coordinate to its next one
    public void bulletRender() {
        bullet.setVisibility(View.VISIBLE);
        projX = ObjectAnimator.ofFloat(bullet, "translationX", BulletCalculations.x);
        projY = ObjectAnimator.ofFloat(bullet, "translationY", BulletCalculations.y);
        projAngle = ObjectAnimator.ofFloat(bullet, "rotation", BulletCalculations.bulletAngle);
        shoot_bullet = new AnimatorSet();
        shoot_bullet.setDuration(20);
        shoot_bullet.play(projX).with(projY).with(projAngle);
        shoot_bullet.start();
        whiteTrail.invalidate();        //Draws the white dots/trail behind the bullet
    }

    //This method runs when the Menu button is clicked
    public void menu(View view) {
        if (!isMenuVisible)
            menuVisibility(true);
    }

    //This method restarts the game, takes you back to the title screen
    public void exitGame(View view) {
        notCrash = true;
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    //This method straight up kills the game
    public void killGame(View view) {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    //This method exits the in-game menu screen, going back to the gameplay
    public void resumeGame(View view) {
        menuVisibility(false);
    }

    //This method hides all of the buttons if view is true, else get rid of the menu screen
    public void menuVisibility(boolean view) {
        if (!view) {
            menu_exit.setVisibility(View.INVISIBLE);
            menu_resume.setVisibility(View.INVISIBLE);
            menu_shader.setVisibility(View.INVISIBLE);
            if (canMove) {
                move_right.setVisibility(View.VISIBLE);
                move_left.setVisibility(View.VISIBLE);
            }
            isMenuVisible = false;
        } else if (view) {
            menu_exit.setVisibility(View.VISIBLE);
            menu_resume.setVisibility(View.VISIBLE);
            menu_shader.setVisibility(View.VISIBLE);
            move_right.setVisibility(View.INVISIBLE);
            move_left.setVisibility(View.INVISIBLE);
            isMenuVisible = true;
        }
    }
}