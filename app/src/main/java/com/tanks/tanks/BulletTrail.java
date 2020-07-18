package com.tanks.tanks;

//A few necessary imports
import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * Created by Dylan on 2018-05-19.
 */
public class BulletTrail extends View {
    static float[] trailPointsX = new float[2000];      //Array to hold the x trail points
    static float[] trailPointsY = new float[2000];      //Array to hold the y trail points
    Paint whitePaint = new Paint();     //Paint object that defines what the trail looks like

    //Constructor function sets up the paint object
    public BulletTrail(Context context) {
        super(context);

        //Setting up the paint, what it is going to look like
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(5);
        whitePaint.setAntiAlias(true);
    }

    //Clears the arrays so that new points can be inputted for the next bullet shot
    public static void clearArrays() {
        for (int i = 0; i <= 1999; i += 7) {
            trailPointsX[i] = 0;
            trailPointsY[i] = 0;
        }
    }

    //This method is called multiple times whenever the dots are rendered. Called by invalidate() in Tank class
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i <= 1999; i += 7) {
            if (trailPointsX[i] != 0 && trailPointsY[i] != 0) {
                canvas.drawPoint(trailPointsX[i], trailPointsY[i], whitePaint);
            }
        }
    }
}