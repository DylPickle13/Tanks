package com.tanks.tanks;

//A few necessary import statements
import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * Created by Dylan on 2018-06-08.
 */
public class Terrain extends View {

    //Path objects to hold the terrain
    static Path terrain_grass = new Path();
    static Path terrain_ground = new Path();

    static float[] y = new float[1921];     //Array that holds the generated polynomial function
    static int b, c;        //The two x-intercepts in the middle of the terrain
    Paint grass = new Paint(), ground = new Paint();        //Paint objects that define the look of the terrain

    //Constructor function sets up the paint objects
    public Terrain(Context context) {
        super(context);

        //Setting up the paint objects, what it's going to look like
        grass.setARGB(255, 0, 150, 0);
        grass.setAntiAlias(true);
        grass.setStrokeWidth(5);
        ground.setARGB(255, 80, 42, 42);
        ground.setAntiAlias(true);
        ground.setStrokeWidth(5);
    }

    //This method generates a random terrain by using a polynomial function
    public static void generatePath() {
        //Clear paths
        terrain_grass.reset();
        terrain_ground.reset();

        //Set random x-intercepts
        b = (int) ((Math.random() * 72) + 1300);
        c = (int) ((Math.random() * 200) + 500);

        //Fill the array with the function y-coordinates
        for (int xi = 0; xi <= 1920; xi++) {
            y[xi] = (1 / 80000000000000000000f) * (float) (Math.pow(xi, 2)) * (float) (Math.pow(xi - b, 2)) * (float) (Math.pow(xi - c, 2)) * (float) (Math.pow(xi - 1920, 2));
        }

        //Fill both paths with the function coordinates, make the ground path appear lower
        terrain_grass.moveTo(0, 1080 - y[0] - 77);
        terrain_ground.moveTo(0, 1080 - y[0]);
        for (int i = 1; i <= 1920; i++) {
            terrain_grass.lineTo(i, 1080 - y[i] - 77);
            terrain_ground.lineTo(i, 1080 - y[i]);
        }
    }

    //This method is called once at the beginning of the game, where it draws all of the terrain at once
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(terrain_grass, grass);
        canvas.drawPath(terrain_ground, ground);
    }
}
