package com.tanks.tanks;

/**
 * Created by Dylan on 2018-05-27.
 */
class SupplyDrop {
    static int supplyPosition, item;        //Coordinate for the supply drop, and the item that is in the drop
    static boolean drop;        //Stores whether the drop is actually going to drop

    //This method runs half the time every 3 turns, it checks whether it's ok for a supply drop to come down, and what item will be in it
    public static void checkTurn() {
        if (Tank.tank2x - Tank.tank1x > 400) {      //If the tanks are sufficiently far apart
            int dropChance = (int) (Math.random() * 2 + 1);     //Half chance of dropping
            if (dropChance == 1) {
                drop = true;
                supplyPosition = (int) (Math.random() * (Tank.tank2x - Tank.tank1x) + 1 + Tank.tank1x);     //Generate a random coordinate
                do {
                    item = (int) (Math.random() * 4 + 1);
                } while (item == 3);
            } else {
                drop = false;
            }
        } else {
            drop = false;
        }
    }
}
