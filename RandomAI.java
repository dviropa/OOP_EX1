import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomAI extends AIPlayer {

    // בנאי שמקבל אינדיקציה האם השחקן הוא הראשון
    public RandomAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        // זיהוי השחקן הנוכחי (השחקן הראשון או השני)
        Player p = new RandomAI(gameStatus.isFirstPlayerTurn());
        if (gameStatus.isFirstPlayerTurn() == true) p = gameStatus.getFirstPlayer();
        if (gameStatus.isFirstPlayerTurn() == false) p = gameStatus.getSecondPlayer();

        // בחירת מיקום אקראי מתוך הרשימה של מהלכים חוקיים
        Random random = new Random();
        int randomNumber = ThreadLocalRandom.current().nextInt(0, gameStatus.ValidMoves().size());

        // בחירת סוג דיסק אקראי (פשוט, פצצה, או לא הפיך)
        int randomdisc = ThreadLocalRandom.current().nextInt(0, 3);
        Disc d = new BombDisc(p);

        // יצירת הדיסק המתאים לפי סוגו ובדיקת המגבלות של השחקן
        if (randomdisc == 0 && p.getNumber_of_bombs() > 0) {
            d = new BombDisc(p);
        } else if (randomdisc == 1 && p.getNumber_of_unflippedable() > 0) {
            d = new UnflippableDisc(p);
        } else d = new SimpleDisc(p);

        // החזרת מהלך עם המיקום האקראי והדיסק הנבחר
        return new Move(gameStatus.ValidMoves().get(randomNumber), d);
    }

}