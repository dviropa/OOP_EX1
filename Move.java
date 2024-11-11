
public class Move {
    private Position p;
    private Disc d;

    public Move(Position p, Disc d) {
        this.p = p;
        this.d = d;
    }

    public Position position() {
        return p;
    }

    public Disc disc() {
        return d;
    }
}
