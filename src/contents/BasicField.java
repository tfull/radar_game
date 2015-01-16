package contents;

import java.awt.Point;
import java.util.Random;
import io.SWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class BasicField{
    public static final int FIELD_SIZE = 10;
    public static final int SHIP_NUMBER = 5;

    protected int[][] field;
    protected Ship[] ships;
    protected String name;

    public BasicField(){
        this.field = new int[FIELD_SIZE][FIELD_SIZE];
        this.ships = new Ship[SHIP_NUMBER];
    }

    public BasicField(String name){
        this();
        this.name = name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
    
    /*
    protected void printField(String it){
        System.out.println(it + this.name);
        System.out.print(" |");
        for(int i = 0; i < FIELD_SIZE; i++){
            System.out.print((char)('A' + (char)i));
            if(i < FIELD_SIZE - 1){
                System.out.print(" ");
            }
        }
        System.out.print("\n-+");
        for(int i = 0; i < 2 * FIELD_SIZE - 1; i++){
            System.out.print("-");
        }
        System.out.print("\n");
        for(int i = 0; i < FIELD_SIZE; i++){
            System.out.print(i + "|");
            for(int j = 0; j < FIELD_SIZE; j++){
                switch(this.field[i][j]){
                case 0:
                    System.out.print("-");
                    break;
                case - SHIP_NUMBER - 1:
                    System.out.print("x");
                    break;
                case - SHP_NUMBER - 2:
                    System.out.print("@");
                default:
                    if(this.field[i][j] < 0){
                        System.out.print("@");
                    }else{
                        System.out.print(this.field[i][j]);
                    }
                    break;
                }
                if(j < FIELD_SIZE - 1){
                    System.out.print(" ");
                }
            }
            System.out.print("\n");
        }
    }
    */

    protected void printField(int mode){
        try{ this.printField(new SWriter(new OutputStreamWriter(System.out)), mode); }
        catch(Exception e){ }
    }

    protected void printField(SWriter writer, int mode) throws IOException{
        writer.printLine((mode == 0 ? "Player: " : "Rival: ") + this.name);
        writer.print(" |");
        for(int i = 0; i < FIELD_SIZE; i++){
            writer.print((char)('A' + (char)i) + "");
            if(i < FIELD_SIZE - 1){
                writer.print(" ");
            }
        }
        writer.print("\n-+");
        for(int i = 0; i < 2 * FIELD_SIZE - 1; i++){
            writer.print("-");
        }
        writer.print("\n");
        for(int i = 0; i < FIELD_SIZE; i++){
            writer.print(i + "|");
            for(int j = 0; j < FIELD_SIZE; j++){
                switch(this.field[i][j]){
                case 0:
                    writer.print("-");
                    break;
                case - SHIP_NUMBER - 1:
                    writer.print("x");
                    break;
                case - SHIP_NUMBER - 2:
                    writer.print("@");
                    break;
                default:
                    if(this.field[i][j] < 0){
                        if(mode == 0){
                            writer.print("@");
                        }else{
                            writer.print((- this.field[i][j]) + "");
                        }
                    }else{
                        writer.print(this.field[i][j] + "");
                    }
                    break;
                }
                if(j < FIELD_SIZE - 1){
                    writer.print(" ");
                }
            }
            writer.print("\n");
        }
    }

    public int get(Point p){
        return this.field[p.y][p.x];
    }

    public void set(int kind, Point p, int dir) throws Exception{
        int ship_length = Ship.LENGTHS[kind - 1];
        int dx, dy;

        if(this.ships[kind - 1] != null && this.ships[kind - 1].active){
            throw new Exception();
        }

        if(dir == 0){
            dx = 0;
            dy = 1;
        }else{
            dx = 1;
            dy = 0;
        }

        for(int i = 0; i < ship_length; i++){
            if(this.field[p.y + i * dy][p.x + i * dx] != 0){
                throw new Exception();
            }
        }

        for(int i = 0; i < ship_length; i++){
            this.field[p.y + i * dy][p.x + i * dx] = kind;
        }

        this.ships[kind - 1] = new Ship(p, true, dir);
    }

    public void set(int kind, String s, int dir) throws Exception{
        this.set(kind, stringToPoint(s), dir);
    }

    public void setForce(int kind, Point p, int dir){
        int ship_length = Ship.LENGTHS[kind - 1];
        int dx, dy;

        if(dir == 0){
            dx = 0;
            dy = 1;
        }else{
            dx = 1;
            dy = 0;
        }

        for(int i = 0; i < ship_length; i++){
            this.field[p.y + i * dy][p.x + i * dx] = kind;
        }

        this.ships[kind - 1] = new Ship(p, true, dir);
    }

    public void setForce(int kind, String s, int dir){
        this.setForce(kind, stringToPoint(s), dir);
    }

    public void entrust(){
        final int SIZE = FIELD_SIZE * FIELD_SIZE * 2;
        
        for(int i = 0; i < SHIP_NUMBER; i++){
            if(this.ships[i] != null){
                continue;
            }

            int[] nums = new int[SIZE];
            for(int j = 0; j < SIZE; j++){
                nums[j] = j;
            }
            Random random = new Random();
            for(int j = 0; j < SIZE; j++){
                int index = random.nextInt(SIZE - j);
                int val = nums[index];
                for(int k = index; k < SIZE - 1; k++){
                    nums[k] = nums[k + 1];
                }
                try{
                    this.set(i + 1, new Point(val % FIELD_SIZE, (val % (FIELD_SIZE * FIELD_SIZE)) / FIELD_SIZE), val / (FIELD_SIZE * FIELD_SIZE));
                    break;
                }catch(Exception e){
                    continue;
                }
            }
        }
    }

    public void remove(int kind) throws Exception{
        if(this.ships[kind - 1] != null){
            int ship_length = Ship.LENGTHS[kind - 1];
            Point p = this.ships[kind - 1].point;
            int dx, dy;

            if(this.ships[kind - 1].direction == 0){
                dx = 0;
                dy = 1;
            }else{
                dx = 1;
                dy = 0;
            }
            for(int i = 0; i < ship_length; i++){
                this.field[p.y + i * dy][p.x + i * dx] = 0;
            }
            this.ships[kind - 1] = null;
        }
    }

    public void remove(Point point) throws Exception{
        this.remove(this.field[point.y][point.x]);
    }
    
    public void remove(String s) throws Exception{
        this.remove(stringToPoint(s));
    }

    public void reset(){
        for(int i = 0; i < SHIP_NUMBER; i++){
            if(this.ships[i] != null){
                try{
                    this.remove(i + 1);
                }catch(Exception e){
                }
            }
        }
    }

    public int attackFrom(Point p){  // hit -> return kind, sink -> return  -kind, none -> 0
        int kind = this.field[p.y][p.x];
        
        if(kind > 0){
            this.field[p.y][p.x] = - this.field[p.y][p.x];
            if(this.isSunk(kind)){
                return (- kind);
            }else{
                return kind;
            }
        }else{
            this.field[p.y][p.x] = - SHIP_NUMBER - 1;
            return 0;
        }
    }

    public int attackFrom(String s){
        return this.attackFrom(stringToPoint(s));
    }

    public boolean isSunk(int kind){
        Point p = this.ships[kind - 1].point;
        int dx, dy;
        int len = Ship.LENGTHS[kind - 1];
        boolean sunk = true;

        if(this.ships[kind - 1].direction == 0){
            dx = 0;
            dy = 1;
        }else{
            dx = 1;
            dy = 0;
        }

        for(int i = 0; i < len; i++){
            if(this.field[p.y + i * dy][p.x + i * dx] == kind){
                sunk = false;
            }
        }

        if(sunk){
            this.ships[kind - 1].active = false;
            return true;
        }else{
            return false;
        }
    }

    public boolean isExist(){
        return (! this.isDead());
    }

    public boolean isDead(){
        boolean dead = true;

        for(int i = 0; i < SHIP_NUMBER; i++){
            if(this.ships[i].active == true){
                dead = false;
            }
        }

        return dead;
    }

    public boolean isReady(){
        for(int i = 0; i < SHIP_NUMBER; i++){
            if(this.ships[i] == null || (! this.ships[i].active)){
                return false;
            }
        }
        return true;
    }

    public String getShipsInformation(){
        String data = "";
        
        for(int i = 0; i < SHIP_NUMBER; i++){
            data += (char)('A' + (char)this.ships[i].point.x) + "" + this.ships[i].point.y + ":" + this.ships[i].direction + " ";
        }
        return data;
    }

    public boolean isLeave(int kind){
        if(this.ships[kind - 1] == null){
            return false;
        }else{
            return this.ships[kind - 1].active;
        }
    }

    public int getDirection(int kind){
        return this.ships[kind - 1].direction;
    }

    public Point getShipPoint(int kind){
        return this.ships[kind - 1].point;
    }

    public static Point stringToPoint(String s){
        return new Point((int)(s.charAt(0) - 'A'), Integer.parseInt(s.substring(1)));
    }

    public static String pointToString(Point p){
        return (char)('A' + (char)p.x) + "" + p.y;
    }
}
