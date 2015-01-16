package contents;

import java.awt.Point;
import java.util.Random;
import io.SWriter;
import java.io.IOException;

public class Field extends BasicField{
    public Field(){
        super();
    }

    public Field(String name){
        super(name);
    }

    public void printField(){
        super.printField(0);
    }

    public void printField(SWriter writer) throws IOException{
        super.printField(writer, 0);
    }
}
