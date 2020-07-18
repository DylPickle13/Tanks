package com.tanks.tanks;

/**
 * Created by Dylan on 2018-03-07.
 */
class BulletCalculations {
    static float x, y, bulletAngle;     //x and y coordinates of bullet, and the angle
    static boolean collision = false;       //Boolean variable which turns true if the bullet hits something
    static int magnitude;       //The damage the bullet does
    private static float x_vel, y_vel, xInc, yInc, wind, y1, x1;        //Inital values before starting the shot
    private static int frame;       //Measures the frames of the bullet

    //This method sets up all the initial values before firing the bullet
    public static void bulletSetup() {
        wind = Tank.windFactor / 3;
        if (!Tank.player_turn) {
            float bullet_power = Tank.shot1[0] * 1.35f;
            float angle = Tank.shot1[1];
            x_vel = ((float) (bullet_power * Math.cos(Math.toRadians(angle)))) * -1;
            y_vel = (float) (bullet_power * Math.sin(Math.toRadians(angle)));
            x1 = Tank.tank1x + 50 - 70;
            y1 = (Tank.tank1y) - 5;
        } else if (Tank.player_turn) {
            float bullet_power = Tank.shot2[0] * 1.35f;
            float angle = Tank.shot2[1];
            x_vel = ((float) (bullet_power * Math.cos(Math.toRadians(angle)))) * -1;
            y_vel = (float) (bullet_power * Math.sin(Math.toRadians(angle)));
            x1 = Tank.tank2x + 50 - 70;
            y1 = (Tank.tank2y) - 5;
        }
        y_vel = y_vel * -1;
        x = x1;
        y = y1 - 20;
        xInc = x_vel / 10;
        yInc = y_vel / 10;
        frame = 0;

        //Sets up the BulletTrail class
        BulletTrail.clearArrays();
        BulletTrail.trailPointsX[frame] = BulletCalculations.x + 20;
        BulletTrail.trailPointsY[frame] = 1080 - Math.abs(BulletCalculations.y) - 97;
    }

    //This method updates the bullet values as it flies through the air
    public static void bulletUpdate() {
        final float GRAVITY = 7f;       //The acceleration due to gravity
        x = x + xInc;
        y = y + yInc;
        frame++;

        //Sets the dot of the trail to be drawn
        BulletTrail.trailPointsX[frame] = BulletCalculations.x + 20;
        BulletTrail.trailPointsY[frame] = 1080 - Math.abs(BulletCalculations.y) - 97;

        //Calculates the angle of the bullet
        bulletAngle = (float) (Math.toDegrees(Math.atan((y_vel) / (x_vel)))) - 90;

        //Keeps the bullet from facing the wrong way
        if (x_vel >= 0) {
            bulletAngle = bulletAngle + 180;
        }

        //If the frame is divisible by 10, add the x and y acceleration to the velocities (Wind and Gravity)
        if (frame % 10 == 0) {
            x_vel = x_vel + wind;
            y_vel = y_vel + GRAVITY;

            xInc = x_vel / 10;
            yInc = y_vel / 10;
        }

        //Checks for bullet collision
        if (x <= 1900 && x >= 0 && Math.abs(y) >= Terrain.y[(int) x + 20] && y <= 0) {
            collision = false;
            calcDistanceAndForce();
        } else {
            collision = true;
            calcDistanceAndForce();
        }
    }

    //This method calculates the distance the bullet is from either tank, and the force of the bullet when it hits a tank
    private static void calcDistanceAndForce() {
        double distanceP1, distanceP2;
        int RADIUS = 60;        //Hit box circle around tanks

        //Calculates the distance from each tank
        distanceP2 = Math.sqrt(Math.pow(Tank.tank2x - BulletCalculations.x, 2) + Math.pow(Math.abs(Tank.tank2y) - Math.abs(BulletCalculations.y), 2));
        distanceP1 = Math.sqrt(Math.pow(Tank.tank1x - BulletCalculations.x, 2) + Math.pow(Math.abs(Tank.tank1y) - Math.abs(BulletCalculations.y), 2));

        //If the bullet enters a hit box then calculate the force and call a method to subtract the damage
        if (!Tank.player_turn && distanceP1 > RADIUS && !collision) {
            if (distanceP2 <= RADIUS) {
                collision = true;
                Tank.hitTank = true;
                magnitude = (int) Math.sqrt(Math.pow(Math.abs(x_vel), 2) + Math.pow(Math.abs(y_vel), 2));
                subtractDamage(true);
            }
        } else if (collision && distanceP1 <= RADIUS) {
            collision = true;
            Tank.hitTank = true;
            magnitude = (int) Math.sqrt(Math.pow(Math.abs(x_vel), 2) + Math.pow(Math.abs(y_vel), 2));
            subtractDamage(false);
        } else if (Tank.player_turn && distanceP2 > RADIUS && !collision) {
            if (distanceP1 <= RADIUS) {
                collision = true;
                Tank.hitTank = true;
                magnitude = (int) Math.sqrt(Math.pow(Math.abs(x_vel), 2) + Math.pow(Math.abs(y_vel), 2));
                subtractDamage(false);
            }
        } else if (collision && distanceP2 <= RADIUS) {
            collision = true;
            Tank.hitTank = true;
            magnitude = (int) Math.sqrt(Math.pow(Math.abs(x_vel), 2) + Math.pow(Math.abs(y_vel), 2));
            subtractDamage(true);
        }
        playerWin();        //Checks to see if a tank has won
    }

    //This method subtracts damage from the chosen player
    public static void subtractDamage(boolean player) {
        int difference;

        //Relatively complex if condition seeing if there is an active shield, reducing its value, then moving on to subtract health
        if (!player) {
            if (Tank.shield[1] > 0) {
                difference = Tank.shield[1] - magnitude;
                if (difference <= 0) {
                    Tank.shield[1] = 0;
                    Tank.hp[1] -= Math.abs(difference);
                } else {
                    Tank.shield[1] = difference;
                }
            } else {
                Tank.hp[1] -= magnitude;
            }
        } else if (player) {
            if (Tank.shield[2] > 0) {
                if (Tank.shield[2] > 0) {
                    difference = Tank.shield[2] - magnitude;
                    if (difference <= 0) {
                        Tank.shield[2] = 0;
                        Tank.hp[2] -= Math.abs(difference);
                    } else {
                        Tank.shield[2] = difference;
                    }
                }
            } else {
                Tank.hp[2] -= magnitude;
            }
        }
    }

    //This method checks to see if a tank's health has gone <= 0, if so, a player has won
    public static void playerWin(){
        if (Tank.hp[2] <= 0) {
            Tank.player_won = 1;
        } else if (Tank.hp[1] <= 0) {
            Tank.player_won = 2;
        } else {
            Tank.player_won = 0;
        }
    }
}