package io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SWriter extends BufferedWriter{
    public SWriter(OutputStreamWriter osw){
        super(osw);
    }
    
    public void writeFlush(String string) throws IOException{
        this.write(string);
        this.flush();
    }

    public void writeLine(String string) throws IOException{
        this.write(string + "\n");
    }

    public void writeLineFlush(String string) throws IOException{
        this.writeLine(string);
        this.flush();
    }

    public void print(String string) throws IOException{
        this.writeFlush(string);
    }

    public void printLine(String string) throws IOException{
        this.writeLineFlush(string);
    }
}
