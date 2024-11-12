public class UnflippableDisc implements Disc {
    Player owner;
    public UnflippableDisc(Player owner) {
        this.owner = owner;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player player) {
        owner = player;

    }

    @Override
    public String getType() {
        return "⭕";
    }
    //    @Override
    public Boolean equals(BombDisc b) {
        if(b.getOwner().equals(this.getOwner()))
            return true;
        return false;
    }
}