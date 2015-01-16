package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetDescriptor{
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;

    public NetDescriptor(){
    }

    public NetDescriptor(Socket socket) throws IOException{
        this.open(socket);
    }

    public NetDescriptor(String host, int port) throws IOException{
        this.open(host, port);
    }

    public void open(Socket socket) throws IOException{
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void open(String host, int port) throws UnknownHostException, IOException{
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public String readLine() throws IOException{
        return this.reader.readLine();
    }

    public String receive() throws IOException{
        return this.reader.readLine();
    }

    public void write(String string) throws IOException{
        this.writer.write(string);
    }

    public void writeFlush(String string) throws IOException{
        this.writer.write(string);
        this.writer.flush();
    }

    public void writeLine(String line) throws IOException{
        this.writer.write(line + "\n");
    }

    public void writeLineFlush(String line) throws IOException{
        this.writer.write(line + "\n");
        this.writer.flush();
    }

    public void send(String line) throws IOException{
        this.writer.write(line + "\n");
        this.writer.flush();
    }

    public void flush() throws IOException{
        this.writer.flush();
    }

    public void close() throws IOException{
        this.reader.close();
        this.writer.close();
        this.socket.close();
    }
}
