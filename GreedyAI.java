import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedyAI extends AIPlayer {

    // בנאי שמקבל אינדיקציה האם השחקן הוא הראשון
    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        // זיהוי השחקן הנוכחי
        Player p = new GreedyAI(gameStatus.isFirstPlayerTurn());
        if (gameStatus.isFirstPlayerTurn() == true)p= gameStatus.getFirstPlayer();
        if (gameStatus.isFirstPlayerTurn() == false)p= gameStatus.getSecondPlayer();

        // משתנים למציאת המהלך הטוב ביותר
        Position maxpos=new Position(0,0);
        int max=0;
        List<Position> l = new ArrayList();
        max=0;

        // לולאה על כל המהלכים החוקיים
        for (Position pos:gameStatus.ValidMoves()){
            if(max<gameStatus.countFlips(pos)) {
                l.clear();
                maxpos=pos;
                max=gameStatus.countFlips(pos);
                l.add(pos);
            }
            else if(max==gameStatus.countFlips(pos)) l.add(pos);
        }
        // סידור הרשימה לפי עמודה ואחר כך שורה (כדי לשבור שוויון)
        l.sort(Comparator.comparingInt(Position::col).thenComparingInt(Position::row));

        // החזרת המהלך האחרון ברשימה (הוא הסידורי המועדף במקרה של שוויון)
        return new Move(l.get(l.size()-1), new SimpleDisc(p));//  return new Move(maxpos, new SimpleDisc(p));

    }
}