package contents;

import java.awt.Point;

public class Ship{
    public static String[] NAMES = { "駆逐艦", "潜水艦", "巡洋艦", "戦艦", "空母" };
    public static int[] LENGTHS = { 2, 3, 3, 4, 5 };
    
    public Point point;
    public boolean active;
    public int direction;

    public Ship(Point point, boolean active, int direction){
        this.point = point;
        this.active = active;
        this.direction = direction;
    }
}
