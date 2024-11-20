import java.util.Comparator;

public class GreedyAI extends AIPlayer {
    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        Player p = new GreedyAI(gameStatus.isFirstPlayerTurn());
        if (gameStatus.isFirstPlayerTurn() == true)p= gameStatus.getFirstPlayer();
        if (gameStatus.isFirstPlayerTurn() == false)p= gameStatus.getSecondPlayer();
        Position maxpos=new Position(0,0);
        int max=0;
      for (Position pos:gameStatus.ValidMoves()){
          if(max<gameStatus.countFlips(pos)) {
              maxpos=pos;
              max=gameStatus.countFlips(pos);
          }
          if(max==gameStatus.countFlips(pos))
             if(pos.col()>maxpos.col()){
                 maxpos=pos;
                 max=gameStatus.countFlips(pos);
             }

             if(pos.col()==maxpos.col() && pos.row()>maxpos.row()){
                 maxpos=pos;
                 max=gameStatus.countFlips(pos);
             }
      }
        gameStatus.ValidMoves().sort(Comparator.comparingInt(Position::row).thenComparingInt(Position::col));
        return new Move(maxpos, new SimpleDisc(p));
    }
}