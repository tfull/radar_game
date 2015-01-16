package basic;

public class LimitLog{
    protected String[] log;
    protected int capacity;
    protected int size;
    protected int begin;

    public LimitLog(){
        this(10);
    }

    public LimitLog(int capacity){
        this.log = new String[capacity];
        this.size = 0;
        this.capacity = capacity;
        this.begin = 0;
    }

    public void push(String string){
        if(this.size >= this.capacity){
            this.log[this.begin] = null;
            this.size -= 1;
            this.increment();
        }
        this.size += 1;
        this.log[this.end()] = string;
    }

    public String pop() throws Exception{
        if(this.size <= 0){
            throw new Exception();
        }
        String string = this.log[this.begin];
        this.log[this.begin] = null;
        this.increment();
        this.size -= 1;
        return string;
    }

    public String look(int at){
        String string = this.log[(this.begin + at) % this.capacity];
        if(string == null){
            throw new IndexOutOfBoundsException();
        }
        return string;
    }

    public int size(){
        return this.size;
    }

    public int capacity(){
        return this.capacity;
    }

    public void clear(){
        this.log = new String[this.capacity];
        this.size = 0;
        this.begin = 0;
    }

    protected void increment(){
        this.begin = (this.begin + 1) % this.capacity;
    }

    protected int end(){
        return ((this.begin + this.size - 1) % this.capacity);
    }
}
