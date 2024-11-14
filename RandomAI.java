import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomAI extends AIPlayer {
    public RandomAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {


        Random random = new Random();
        int randomNumber = ThreadLocalRandom.current().nextInt(0, gameStatus.ValidMoves().size());
        int randomdisc = ThreadLocalRandom.current().nextInt(0, 2);

        if(gameStatus.isFirstPlayerTurn()==true){

             if(randomdisc==0&&gameStatus.getFirstPlayer().getNumber_of_bombs()>0){
                 Disc d=new BombDisc(gameStatus.getFirstPlayer());
                 gameStatus.locate_disc(gameStatus.ValidMoves().get(randomNumber),d);
             }

            else if(randomdisc==1&&gameStatus.getFirstPlayer().getNumber_of_unflippedable()>0){
                Disc d=new UnflippableDisc(gameStatus.getFirstPlayer());
                 gameStatus.locate_disc(gameStatus.ValidMoves().get(randomNumber),d);
             }

            else{
                Disc d=new SimpleDisc(gameStatus.getFirstPlayer());
                 gameStatus.locate_disc(gameStatus.ValidMoves().get(randomNumber),d);
            }

        }
        else {

              if(randomdisc==0&&gameStatus.getSecondPlayer().getNumber_of_bombs()>0){

                  Disc d=new BombDisc(gameStatus.getFirstPlayer());
                  gameStatus.locate_disc(gameStatus.ValidMoves().get(randomNumber),d);
              }

            else  if(randomdisc==1&&gameStatus.getSecondPlayer().getNumber_of_unflippedable()>0){
                Disc d=new UnflippableDisc(gameStatus.getFirstPlayer());
                  gameStatus.locate_disc(gameStatus.ValidMoves().get(randomNumber),d);
              }

            else {
                Disc d=new SimpleDisc(gameStatus.getSecondPlayer());
                  gameStatus.locate_disc(gameStatus.ValidMoves().get(randomNumber),d);
              }

        }


        return null;
    }
}