package game;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import network.NetDescriptor;
import contents.*;
import io.SWriter;

public class CLIClient{
    protected NetDescriptor descriptor;
    protected BufferedReader input;
    protected SWriter output;
    protected Field field;
    protected RivalField rival_field;
    protected String name;
    
    public static void main(String[] args){
        String namearg = "Guest";
        if(args.length < 2){
            System.err.println("few arguments");
            System.exit(1);
        }else if(args.length >= 3){
            namearg = args[2];
        }

        try{
            CLIClient client = new CLIClient(namearg, args[0], Integer.parseInt(args[1]));
            client.run();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public CLIClient(String name, String host, int port) throws Exception{
        this.name = name;
        this.descriptor = new NetDescriptor(host, port);
        this.input = new BufferedReader(new InputStreamReader(System.in));
        this.output = new SWriter(new OutputStreamWriter(System.out));
    }


    public void run() throws Exception{
        this.descriptor.send(this.name);
        this.output.printLine("Waiting connection. ......");
        this.field = new Field(this.name);
        this.rival_field = new RivalField(this.descriptor.readLine());
        if(this.rival_field.getName() == null){ throw new IOException(); }
        this.output.printLine(this.rival_field.getName() + " connected. Get ready.");
        this.loadField();
        this.descriptor.send(field.getShipsInformation());
        this.output.printLine("Waiting arrangement. ......");
        {
            String inst = this.descriptor.receive();
            if(inst != null && inst.equals("Ready")){
                this.output.printLine("Get ready! Battle start!");
            }else{
                throw new IOException();
            }
        }
        this.printFields();

        while(true){
            String instruction = descriptor.receive();
            if(instruction.equals("OK")){
                this.output.printLine("Turn: Attack");
                String attack;
                while(true){
                    String line = this.input.readLine();
                    Scanner scanner = new Scanner(line);
                    if(! scanner.hasNext()){ continue; }
                    attack = scanner.next();
                    if(this.rival_field.isAbleToAttack(attack)){
                        this.descriptor.send(attack);
                        break;
                    }else{
                        this.output.printLine("Exception: Input once more.");
                    }
                }
                instruction = this.descriptor.receive();
                Scanner scanner = new Scanner(instruction);
                String command = scanner.next();
                if(command.equals("Win")){
                    int kind = scanner.nextInt();
                    String s = scanner.next();
                    int dir = scanner.nextInt();

                    this.rival_field.sink(kind, s, dir);
                    this.output.printLine("Hit! You won!");
                    this.printFields();
                    break;
                }else if(command.equals("Sink")){
                    int kind = scanner.nextInt();
                    String s = scanner.next();
                    int dir = scanner.nextInt();

                    this.rival_field.sink(kind, s, dir);
                    this.output.printLine("Hit! " + Ship.NAMES[kind - 1] + " sank!");
                }else if(command.equals("Hit")){
                    this.rival_field.set(attack, - BasicField.SHIP_NUMBER - 2);
                    this.output.printLine("Hit!");
                }else if(command.equals("Blank")){
                    this.rival_field.set(attack, - BasicField.SHIP_NUMBER - 1);
                    this.output.printLine("Blank.");
                }
                this.printFields();
            }else{
                this.output.printLine("Turn: Receive");
                instruction = this.descriptor.receive();
                this.output.printLine("Rival selected: " + instruction);
                int result = this.field.attackFrom(instruction);

                if(result > 0){
                    this.output.printLine("Hit!");
                    this.printFields();
                }else if(result < 0){
                    this.output.printLine("Hit! " + Ship.NAMES[- result - 1] + " sank!");
                    this.printFields();
                    if(this.field.isDead()){
                        this.output.printLine("You lost!");
                        this.rival_field.askAnswer(this.descriptor.receive());
                        this.rival_field.printAnswer(this.output);
                        break;
                    }
                }else{
                    this.output.printLine("Blank.");
                    this.printFields();
                }
            }
        }
        
        this.descriptor.close();
        this.input.close();
        this.output.close();
    }

    protected void printFields() throws IOException{
        this.field.printField(this.output);
        this.rival_field.printField(this.output);
    }

    protected void loadField() throws IOException{
        while(true){
            this.field.printField();
            this.output.printLine("Commands:");
            this.output.printLine("  set $kind([1-5]) $point([A-J][0-9]) $direction(0|1)");
            this.output.printLine("  remove $point([A-J][0-9]|[1-5])");
            this.output.printLine("  random");
            this.output.printLine("  reset");
            this.output.printLine("  OK");
            this.output.print("$ ");
            Scanner scanner = new Scanner(this.input.readLine());
            ArrayList<String> args = new ArrayList<String>();

            while(scanner.hasNext()){
                args.add(scanner.next());
            }

            if(args.size() <= 0){ continue; }

            String command = args.get(0);

            if(command.equals("set")){
                try{
                    this.field.set(Integer.parseInt(args.get(1)), args.get(2), Integer.parseInt(args.get(3)));
                }catch(Exception e){
                    this.output.printLine("Exception (set): Input again.");
                }
            }else if(command.equals("remove")){
                try{
                    String arg = args.get(1);
                    if(arg.length() == 1 && arg.charAt(0) > '1' && arg.charAt(0) < '5'){
                        this.field.remove(Integer.parseInt(arg));
                    }else{
                        this.field.remove(arg);
                    }
                }catch(Exception e){
                    this.output.printLine("Exception (remove): Input again.");
                }
            }else if(command.equals("random")){
                this.field.entrust();
            }else if(command.equals("reset")){
                this.field.reset();
            }else if(command.equals("OK")){
                if(this.field.isReady()){
                    break;
                }else{
                    this.output.printLine("Not get ready.");
                }
            }else{
                this.output.printLine("Unknown command.");
            }
        }
    }
}
