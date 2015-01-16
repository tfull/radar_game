package game;

import java.awt.Point;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.*;
import network.NetDescriptor;
import contents.*;

public class Server{
    public static void main(String[] args){
        if(args.length < 1){
            System.err.println("few arguments");
            System.exit(1);
        }

        ServerSocket server_socket;
        NetDescriptor[] descriptors;

        try{
            server_socket = new ServerSocket(Integer.parseInt(args[0]));

            while(true){
                descriptors = new NetDescriptor[2];
                
                for(int i = 0; i < 2; i++){
                    descriptors[i] = new NetDescriptor(server_socket.accept());
                }
                
                new Task(descriptors).start();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class Task extends Thread{
    Field[] fields;
    RivalField[] rival_fields;
    NetDescriptor[] clients;
    
    public Task(NetDescriptor[] clients){
        this.clients = clients;
        this.fields = new Field[2];
        this.rival_fields = new RivalField[2];
        for(int i = 0; i < 2; i++){
            this.fields[i] = new Field();
            this.rival_fields[i] = new RivalField();
        }
    }
    
    @Override
    public void run(){
        try{
            for(int i = 0; i < 2; i++){
                this.fields[i].setName(this.clients[i].readLine());
            }
            for(int i = 0; i < 2; i++){
                this.clients[i].writeLineFlush(this.fields[1 - i].getName());
                this.rival_fields[i].setName(this.fields[1 - i].getName());
            }
            for(int i = 0; i < 2; i++){
                this.loadField(i);
            }
            for(int i = 0; i < 2; i++){
                this.clients[i].writeLineFlush("Ready");
            }
            
            int turn = new Random().nextInt(2);
        
            while(true){
                this.clients[turn].writeLineFlush("OK");
                this.clients[1 - turn].writeLineFlush("NG");
            
                String instruction = this.clients[turn].readLine();
                this.clients[1 - turn].writeLineFlush(instruction);
            
                int result = this.fields[1 - turn].attackFrom(instruction);
            
                if(result > 0){
                    this.clients[turn].writeLineFlush("Hit " + result);
                }else if(result < 0){
                    result = - result;
                    String location = Field.pointToString(this.fields[1 - turn].getShipPoint(result));
                    if(this.fields[1 - turn].isDead()){
                        this.clients[turn].writeLineFlush("Win " + result + " " + location + " " + this.fields[1 - turn].getDirection(result));
                        this.clients[1 - turn].send(this.fields[turn].getShipsInformation());
                        break;
                    }else{
                        this.clients[turn].writeLineFlush("Sink " + result + " " + location + " " + this.fields[1 - turn].getDirection(result));
                    }
                }else{
                    this.clients[turn].writeLineFlush("Blank");
                }
                turn = 1 - turn;
            }
            for(int i = 0; i < 2; i++){
                this.clients[i].close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    void loadField(int index) throws Exception{
        String instruction = this.clients[index].readLine();
        Scanner scanner = new Scanner(instruction);
        ArrayList<String> args = new ArrayList<String>();
        
        while(scanner.hasNext()){
            args.add(scanner.next());
        }
        
        if(args.size() < Field.SHIP_NUMBER){
            throw new Exception();
        }
        
        for(int i = 0; i < Field.SHIP_NUMBER; i++){
            String[] s = args.get(i).split(":");
            this.fields[index].set(i + 1, s[0], Integer.parseInt(s[1]));
        }
    }
    
    void broadcast(String string) throws IOException{
        this.clients[0].writeLineFlush(string);
        this.clients[1].writeLineFlush(string);
    }
}
