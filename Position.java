public class Position {

    private int x;
    private int y;

    public Position(int y,int x){
        this.x = x;
        this.y = y;
    }
    public Position(Position pos)
    {
        this.x=pos.x;
        this.y=pos.y;


    }
    public int row(){
        return x;
    }
    public int col(){
        return y;
    }
    public Position get_position(){
        return new Position(y,x);
    }
    public void set_position(int x ,int y){
        this.x=x;
        this.y=y;
    }

    public String toString (){
        return ("("+this.col()+","+" "+this.row()+")");
    }

    public Boolean equals(Position p) {
        if(p.row()==this.row()&&p.col()==this.col())
            return true;
        return false;
    }
}
