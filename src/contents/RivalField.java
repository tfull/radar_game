package contents;

import java.awt.Point;
import io.SWriter;
import java.io.IOException;
import java.util.Scanner;

public class RivalField extends BasicField{
    public RivalField(){
        super();
    }

    public RivalField(String name){
        super(name);
    }

    public void printField(){
        super.printField(1);
    }

    public void printField(SWriter writer) throws IOException{
        super.printField(writer, 1);
    }

    public void printAnswer(SWriter writer) throws IOException{
        writer.printLine("Answer: " + this.name);
        writer.print(" | ");
        for(int i = 0; i < FIELD_SIZE; i++){
            writer.print((char)('A' + (char)i) + " ");
        }
        writer.print("\n-+");
        for(int i = 0; i < 2 * FIELD_SIZE + 1; i++){
            writer.print("-");
        }
        writer.print("\n");
        for(int i = 0; i < FIELD_SIZE; i++){
            writer.print(i + "|");
            String before = " ";
            String now = " ";
            for(int j = 0; j < FIELD_SIZE; j++){
                switch(this.field[i][j]){
                case 0:
                    writer.print(before + "-");
                    now = " ";
                    break;
                case - SHIP_NUMBER - 1:
                    writer.print(before + "x");
                    now = " ";
                    break;
                case - SHIP_NUMBER - 2:
                    writer.print(before + "@");
                    now = " ";
                    break;
                default:
                    if(this.field[i][j] > 0){
                        if(before.equals("]")){
                            writer.print("|");
                        }else{
                            writer.print("[");
                        }
                        writer.print(this.field[i][j] + "");
                        now = "]";
                    }else{
                        writer.print(before + (- this.field[i][j]));
                        now = " ";
                    }
                    break;   
                }
                if(j >= FIELD_SIZE - 1){
                    writer.print(now);
                }
                before = now;
            }
            writer.print("\n");
        }
    }

    public void set(Point p, int i){
        this.field[p.y][p.x] = i;
    }

    public void set(String s, int i){
        this.set(BasicField.stringToPoint(s), i);
    }

    public void sink(int kind, Point p, int dir) throws Exception{
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
            this.field[p.y + i * dy][p.x + i * dx] = - kind;
        }

        this.ships[kind - 1] = new Ship(p, false, dir);
    }

    public void sink(int kind, String s, int dir) throws Exception{
        this.sink(kind, stringToPoint(s), dir);
    }

    public boolean isAbleToAttack(Point p){
        try{
            if(this.field[p.y][p.x] == 0){
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            return false;
        }
    }

    public boolean isAbleToAttack(String s){
        try{
            return this.isAbleToAttack(stringToPoint(s));
        }catch(Exception e){
            return false;
        }
    }

    public void askAnswer(String ship_information){
        Scanner scanner = new Scanner(ship_information);
        
        for(int i = 0; i < SHIP_NUMBER; i++){
            String s = scanner.next();
            String[] ss = s.split(":");
            Point p = stringToPoint(ss[0]);
            int dir = Integer.parseInt(ss[1]);
            int dx, dy;
            if(dir == 0){
                dx = 0;
                dy = 1;
            }else{
                dx = 1;
                dy = 0;
            }
            int ship_length = Ship.LENGTHS[i];
            for(int j = 0; j < ship_length; j++){
                if(this.field[p.y + j * dy][p.x + j * dx] < 0){
                    this.field[p.y + j * dy][p.x + j * dx] = - (i + 1);
                }else{
                    this.field[p.y + j * dy][p.x + j * dx] = i + 1;
                }
            }
        }
    }
}
