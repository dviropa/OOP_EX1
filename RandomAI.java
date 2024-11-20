import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomAI extends AIPlayer {
    public RandomAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        Player p = new RandomAI(gameStatus.isFirstPlayerTurn());
        if (gameStatus.isFirstPlayerTurn() == true)p= gameStatus.getFirstPlayer();
        if (gameStatus.isFirstPlayerTurn() == false)p= gameStatus.getSecondPlayer();
        Random random = new Random();
        int randomNumber = ThreadLocalRandom.current().nextInt(0, gameStatus.ValidMoves().size());
        int randomdisc = ThreadLocalRandom.current().nextInt(0, 3);
        Disc d= new BombDisc(p);

        if (randomdisc == 0 && p.getNumber_of_bombs() > 0) {
            d = new BombDisc(p);
        }
        else if (randomdisc == 1 && p.getNumber_of_unflippedable() > 0) {
                    d = new UnflippableDisc(p);
        }
        else d = new SimpleDisc(p);

        return new Move(gameStatus.ValidMoves().get(randomNumber), d);
    }

}