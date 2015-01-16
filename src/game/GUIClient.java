package game;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferStrategy;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import network.*;
import contents.*;
import basic.*;

public class GUIClient{
    protected JFrame frame;
    protected BufferStrategy strategy;
    protected NetDescriptor descriptor;
    protected Field field;
    protected RivalField rival_field;
    protected LimitLog history;
    protected boolean cursor_active;
    protected int mode;
    protected Mode1 mode1;
    protected Mode2 mode2;
    protected Mode3 mode3;
    
    public static void main(String[] args){
    	String namearg = "Guest";
    	if(args.length < 2){
    		System.err.println("few arguments");
    		System.exit(1);
    	}else if(args.length >= 3){
    		namearg = args[2];
    	}
    	try{
    		GUIClient client = new GUIClient(args[0], Integer.parseInt(args[1]), namearg);
    		client.start();
    	}catch(Exception e){
    		e.printStackTrace();
    		System.exit(1);
    	}
    }
    
    public GUIClient(String host, int port, String name) throws Exception{
    	this.frame = new JFrame("Radar Game");
    	this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.frame.setSize(1000, 750);
    	this.frame.setLocationRelativeTo(null);
    	this.frame.setVisible(true);
    	this.frame.setResizable(false);

    	this.frame.setIgnoreRepaint(true);
    	this.frame.createBufferStrategy(2);
    	this.strategy = this.frame.getBufferStrategy();

    	this.frame.addMouseListener(new CursorCommand());
    	
    	this.field = new Field(name);
    	this.history = new LimitLog(5);
    	this.descriptor = new NetDescriptor(host, port);
    	this.mode = 0;
    	this.mode1 = new Mode1();
    	this.mode2 = new Mode2();
    	this.cursor_active = false;
    }
    
    protected void start() throws Exception{
    	new Timer().schedule(new RenderTask(), 0, 16);
    	
    	this.descriptor.send(this.field.getName());
    	synchronized(this.history){ this.history.push("Waiting connection. ......"); }
    	this.rival_field = new RivalField(this.descriptor.receive());
    	if(this.rival_field.getName() == null){ throw new Exception(); }
    	synchronized(this.history){ this.history.push(this.rival_field.getName() + " connected."); }
    	
    	this.mode = 1;
    	this.cursor_active = true;
    	String instruction = this.descriptor.receive();
    	if(instruction == null || ! instruction.equals("Ready")){ throw new Exception(); }
    	this.mode = 2;
    	this.cursor_active = true;
    	
    	while(this.mode == 2){
    		instruction = this.descriptor.receive();
    		if(instruction.equals("OK")){
    			this.mode2.attack_mode = true;
    			synchronized(this.history){ this.history.push("Your turn."); }
    			instruction = this.descriptor.receive();
    			Scanner scanner = new Scanner(instruction);
    			String command = scanner.next();
    			if(command.equals("Win")){
    				int kind = scanner.nextInt();
    				String s = scanner.next();
    				int dir = scanner.nextInt();
    				
    				this.rival_field.sink(kind, s, dir);
    				synchronized(this.history){ this.history.push("Hit! You won!"); }
    				this.mode = 3;
    			}else if(command.equals("Sink")){
    				int kind = scanner.nextInt();
    				String s = scanner.next();
    				int dir = scanner.nextInt();
    				
    				this.rival_field.sink(kind, s, dir);
    				synchronized(this.history){ this.history.push("Hit!" + Ship.NAMES[kind - 1] + " sank!"); }
    			}else if(command.equals("Hit")){
    				this.rival_field.set(this.mode2.selected_point, - BasicField.SHIP_NUMBER - 2);
    				synchronized(this.history){ this.history.push("Hit!"); }
    			}else if(command.equals("Blank")){
    				this.rival_field.set(this.mode2.selected_point, - BasicField.SHIP_NUMBER - 1);
    				synchronized(this.history){ this.history.push("Blank."); }
    			}
    			this.mode2.selected_point = null;
    		}else if(instruction.equals("NG")){
    			this.mode2.attack_mode = false;
    			synchronized(this.history){ this.history.push("Rival's turn."); }
    			instruction = this.descriptor.receive();
    			String attack = "Rival selected: " + instruction + ".";
    			int result = this.field.attackFrom(instruction);
    			
    			if(result > 0){
    				synchronized(this.history){ this.history.push(attack + " Hit!"); }
    			}else if(result < 0){
    				synchronized(this.history){ this.history.push(attack + " " + Ship.NAMES[- result - 1] + " sank!"); }
    				if(this.field.isDead()){
    					synchronized(this.history){ this.history.push("You lost!"); }
    					this.rival_field.askAnswer(this.descriptor.receive());
    					this.mode = 3;
    				}
    			}else{
    				synchronized(this.history){ this.history.push(attack + " Blank."); }
    			}
    		}
    	}
        this.descriptor.close();
    }
    
    protected void render(){
    	Graphics2D g = (Graphics2D)this.strategy.getDrawGraphics();
    	g.setBackground(Color.BLACK);
    	g.clearRect(0, 0, this.frame.getWidth(), this.frame.getHeight());
    	
    	if(this.mode == 0){
    		this.renderHistory(g);
    	}else if(this.mode == 1){
    		this.render1(g);
    	}else if(this.mode == 2){
    		this.render2(g);
    	}else{
    		this.render3(g);
    	}
    	
    	g.dispose();
    	this.strategy.show();
    }
    
    protected void render1(Graphics2D g){
    	final String[] ARGS = { Ship.NAMES[0], Ship.NAMES[1], Ship.NAMES[2], Ship.NAMES[3], Ship.NAMES[4], "random", "reset", "remove" };
    	
    	this.renderHistory(g);
    	
    	g.setColor(Color.WHITE);
    	if(this.field.isReady()){
    		g.fillRect(900, 650, 52, 52);
    		g.setColor(Color.RED);
    		g.fillRect(901, 651, 50, 50);
    		g.setColor(Color.YELLOW);
    		g.drawString("OK", 904, 666);
    		g.setColor(Color.WHITE);
    	}
    	
    	final int X1 = 700;
    	final int Y1 = 100;
    	final int B = 50;
    	
    	this.renderField(g, this.field, "Player: ", 0);
    	
		g.fillRect(X1, Y1, B + 2, (B + 1) * 8 + 1);
		for(int i = 0; i < Field.SHIP_NUMBER + 3; i++){
			if(i < Field.SHIP_NUMBER){
				if(this.field.isLeave(i + 1)){
					g.setColor(Color.BLACK);
				}else if(this.mode1.selected_ship == i + 1){
					g.setColor(Color.RED);
				}else{
					g.setColor(Color.BLUE);
				}
			}else if(i == Field.SHIP_NUMBER + 2){
				if(this.mode1.remove_mode){
					g.setColor(Color.YELLOW);
				}
			}else{
				g.setColor(Color.BLUE);
			}
			g.fillRect(X1 + 1, Y1 + 1 + (B + 1) * i, B, B);
		}
		g.setColor(Color.YELLOW);
		for(int i = 0; i < Field.SHIP_NUMBER + 2; i++){
			g.drawString((i + 1) + "", X1 + 1 + 3, Y1 + 1 + (B + 1) * i + 15);
			g.drawString(ARGS[i], X1 + B + 10, Y1 + 1 + (B + 1) * i + 30);
		}
		if(this.mode1.remove_mode){
			g.setColor(Color.GREEN);
		}
		g.drawString((Field.SHIP_NUMBER + 2 + 1) + "", X1 + 1 + 3, Y1 + 1 + (B + 1) * (Field.SHIP_NUMBER + 2) + 15);
		g.drawString(ARGS[Field.SHIP_NUMBER + 2], X1 + B + 10, Y1 + 1 + (B + 1) * (Field.SHIP_NUMBER + 2) + 30);
		
		
    }
    
    protected void render2(Graphics2D g){
    	this.renderHistory(g);
    	this.renderField(g, this.rival_field, "Rival: ", 0);
    	this.renderField(g, this.field, "Player: ", 1);
    	if(this.mode2.selected_point != null){
    		g.setColor(Color.WHITE);
    		g.fillRect(900, 650, 52, 52);
    		g.setColor(Color.RED);
    		g.fillRect(901, 651, 50, 50);
            g.setColor(Color.YELLOW);
    		g.drawString("OK", 904, 666);
    	}
    }
    
    protected void render3(Graphics2D g){
    	this.renderHistory(g);
    	this.renderField(g, this.rival_field, "Rival: ", 0);
    	this.renderField(g, this.field, "Player: ", 1);
    }
    
    protected void renderHistory(Graphics2D g){
    	Color color = g.getColor();
    	g.setColor(Color.WHITE);
    	synchronized(this.history){
    		for(int i = 0; i < this.history.size(); i++){
    			g.drawString(this.history.look(i), 700, 550 + i * 20);
    		}
    	}
    	g.setColor(color);
    }
    
    protected void renderField(Graphics2D g, BasicField basic_field, String it, int mode){
    	Color color = g.getColor();
    	g.setColor(Color.WHITE);
    	if(mode == 0){
    		g.drawString(it + basic_field.getName(), 20, 50);	
    		g.fillRect(20, 80, 611, 611);
    	}else{
    		g.drawString(it + basic_field.getName(), 669, 50);
    		g.fillRect(669, 80, 311, 311);
    	}
    	if(this.mode2.selected_point != null){
    		g.setColor(Color.YELLOW);
    		g.fillRect(20 + 61 * this.mode2.selected_point.x, 80 + 61 * this.mode2.selected_point.y, 62, 62);
    		
    	}
		for(int i = 0; i < Field.FIELD_SIZE; i++){
			for(int j = 0; j < Field.FIELD_SIZE; j++){
				int val = basic_field.get(new Point(j, i));
				switch(val){
				case 0:
					g.setColor(Color.BLACK);
					break;
				case 1:
				case -1:
					g.setColor(Color.BLUE);
					break;
				case 2:
				case -2:
					g.setColor(Color.RED);
					break;
				case 3:
				case -3:
					g.setColor(Color.GREEN);
					break;
				case 4:
				case -4:
					g.setColor(Color.ORANGE);
					break;
				case 5:
				case -5:
					g.setColor(new Color(128, 0, 128));
					break;
				default:
					g.setColor(Color.BLACK);
					break;	
				}
				if(mode == 0){
					g.fillRect(21 + 61 * j, 81 + 61 * i, 60, 60);
				}else{
					g.fillRect(670 + 31 * j, 81 + 31 * i, 30, 30);
				}
				g.setColor(Color.WHITE);
				if(mode == 0){
					if(val == - BasicField.SHIP_NUMBER - 1){
						g.drawString("X", 36 + 61 * j, 96 + 61 * i);
					}else if(val < 0){
						g.drawString("Hit", 36 + 61 * j, 96 + 61 * i);
					}
				}else{
					if(val == - BasicField.SHIP_NUMBER - 1){
						g.drawString("X", 675 + 31 * j, 96 + 31 * i);
					}else if(val < 0){
						g.drawString("Hit", 675 + 31 * j, 96 + 31 * i);
					}
				}
			}
		}
		g.setColor(color);
    }
        
    class RenderTask extends TimerTask{
    	@Override
    	public void run(){
    		GUIClient.this.render();
    	}
    }

	class CursorCommand extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent event){
			if(! GUIClient.this.cursor_active){ return; }
			Point point = event.getPoint();
			if(GUIClient.this.mode == 1){
				if(point.x >= 21 && point.x <= 631 && point.y >= 81 && point.y <= 689){
					if((point.x - 20) % 61 != 0 && (point.y - 80) % 61 != 0){
						Point click_point = new Point((point.x - 20) / 61, (point.y - 80) / 61);

						if(GUIClient.this.mode1.remove_mode){
							try{
								GUIClient.this.field.remove(click_point);
							}catch(Exception e){
								synchronized(GUIClient.this.history){ GUIClient.this.history.push("取り除けません。"); }
							}
						}else{
							if(GUIClient.this.mode1.selected_ship > 0){
								try{
									if(event.getButton() == MouseEvent.BUTTON1){
										GUIClient.this.field.set(GUIClient.this.mode1.selected_ship, click_point, 0);
									}else{
										GUIClient.this.field.set(GUIClient.this.mode1.selected_ship, click_point, 1);
									}
									GUIClient.this.mode1.selected_ship = 0;

								}catch(Exception e){
									synchronized(GUIClient.this.history){ GUIClient.this.history.push("そこには置けません。"); }
								}
							}
						}
					}
				}else if(point.x >= 701 && point.x <= 750 && point.y >= 101 && point.y <= 507){
					if((point.y - 100) % 31 != 0){
						int val = (point.y - 100) / 51 + 1;
						switch(val){
						case 8:
							GUIClient.this.mode1.remove_mode = ! GUIClient.this.mode1.remove_mode;
							break;
						case 7:
							GUIClient.this.field.reset();
							break;
						case 6:
							GUIClient.this.field.entrust();
							break;
						default:
							if(GUIClient.this.mode1.remove_mode){
								if(GUIClient.this.field.isLeave(val)){
									try{ GUIClient.this.field.remove(val); }
									catch(Exception e){ }
								}
							}else{
								if(! GUIClient.this.field.isLeave(val)){
									GUIClient.this.mode1.selected_ship = val;
								}
							}
						}
					}
				}else if(point.x >= 901 && point.x <= 950 && point.y >= 651 && point.y <= 700){
					if(GUIClient.this.field.isReady()){
						synchronized(GUIClient.this.history){ GUIClient.this.history.push("配置を終えるのを待っています。 ....."); }
						try{
							GUIClient.this.descriptor.send(GUIClient.this.field.getShipsInformation());
						}catch(Exception e){
							e.printStackTrace();
							System.exit(1);
						}
						GUIClient.this.cursor_active = false;
					}
				}
			}else if(GUIClient.this.mode == 2){
				if(! GUIClient.this.mode2.attack_mode){ return; }
				if(point.x >= 21 && point.x <= 631 && point.y >= 81 && point.y <= 689){
					if((point.x - 20) % 61 != 0 && (point.y - 80) % 61 != 0){
						Point p = new Point((point.x - 20) / 61, (point.y - 80) / 61);
						if(GUIClient.this.rival_field.isAbleToAttack(p)){
							GUIClient.this.mode2.selected_point = p;
							if(event.getButton() == MouseEvent.BUTTON3){
								synchronized(GUIClient.this.history){ GUIClient.this.history.push(BasicField.pointToString(p) + " selected."); }
								try{ 
									GUIClient.this.descriptor.send(BasicField.pointToString(p));
								}
								catch(Exception e){ }
							}
						}
					}
				}else if(point.x >= 901 && point.x <= 950 && point.y >= 651 && point.y <= 700){
					if(GUIClient.this.mode2.selected_point != null){
						try{
							GUIClient.this.descriptor.send(Field.pointToString(GUIClient.this.mode2.selected_point));
						}
						catch(Exception e){ }
					}
				}
			}
		}
	}
}

class Mode1{
	public int selected_ship;
	public boolean remove_mode;
	
	public Mode1(){
		this.selected_ship = -1;
		this.remove_mode = false;
	}
}

class Mode2{
	public boolean attack_mode;
	public Point selected_point;
	public int display_mode;
	
	public Mode2(){
		this.attack_mode = false;
		this.selected_point = null;
		this.display_mode = 0;
	}
}

class Mode3{
	
}
