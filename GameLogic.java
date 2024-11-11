import java.util.ArrayList;
import java.util.List;

public class GameLogic implements PlayableLogic {
    private Player pl1 = new HumanPlayer(true);
    private Player pl2 = new HumanPlayer(false);

    public Disc[][] GameBoard = new Disc[8][8];
    public GameLogic(){
        for (int i = 0; i <GameBoard.length ; i++) {
            for (int j = 0; j <GameBoard.length ; j++) {
                GameBoard[j][i]=null;
            }
        }
        Disc disc1 = new SimpleDisc(getFirstPlayer());
        Disc disc2 = new SimpleDisc(getSecondPlayer());
        // מיקום הדיסקים ההתחלתיים על הלוח
        GameBoard[4][4] = disc1;
        GameBoard[3][3] = disc1;
        GameBoard[4][3] = disc2;
        GameBoard[3][4] = disc2;
    }
    private int[][] numOfKiils = new int[8][8];
    private List<Disc[][]> gamehistory = new ArrayList<>();
    private List<Position>[][] killspos = new List[8][8];
    private Player PlayersTurn;

    @Override
    public boolean locate_disc(Position a, Disc disc) {
        if (getDiscAtPosition(a) != null) return false;//בדיקה שהמיקום שהדיסק מעוניין להגיע איליו ריק
        if (ValidMoves().contains(a)) {
            GameBoard[a.row()][a.col()] = disc;// ממקם את השחקן בלוח
            return true;
        }
        return false;
    }

    @Override
    public Disc getDiscAtPosition(Position position) {
        if (GameBoard[position.row()][position.col()] == null)
            return null; // בודק אם המיקום שקיבלנו על הלוח הוא null ואם כן מחזיר null
        Disc d = new SimpleDisc(GameBoard[position.row()][position.col()].getOwner());
        return d; // במידה והמיקום לא היה null הוא מחזיר למי שייך הדיסקית באותה נקודה
    }

    @Override
    public int getBoardSize() {
        return 8;
    }

    @Override
    public List<Position> ValidMoves() {
        List<Position> l = new ArrayList<Position>();

            for (Position p : listposi()) {
                l.addAll(healp_validmovs(p));
            }
        return l;
    }
    public List<Position> listposi(){
        List<Position> p = new ArrayList<Position>();
        Player tempp= getFirstPlayer();
        if (isFirstPlayerTurn() == false) {
            tempp= getSecondPlayer();
        }
        for (int i = 0; i <GameBoard.length ; i++) {
            for (int j = 0; j < GameBoard.length; j++) {
                if (GameBoard[j][i]==tempp)p.add(new Position(j,i));
            }
        }
        return p;
    }


    public List<Position> healp_validmovs(Position p) {
        Player anemy;
        Player me;
        if (isFirstPlayerTurn() == true) {
            anemy = getSecondPlayer();
            me = getFirstPlayer();
        } else {
            anemy = getFirstPlayer();
            me = getSecondPlayer();
        }
        List<Position> l = new ArrayList<Position>();
        Position temp = new Position(p.row() - 1, p.col() - 1);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (temp.row() + j < 9 && temp.row() + j < 0 && temp.col() + j < 9 && temp.col() + j < 0) {
                    Position newp = new Position(temp.row() + j, temp.col() + i);
                    if (getDiscAtPosition(newp) != null) break;
//                    if(GameBoard[newp.X][newp.Y].getOwner()==anemy)return false;
                    int caunt = 0;
                    List<Position> killsposlist = new ArrayList<Position>();
                    int x = p.row() - newp.row();
                    int y = p.col() - newp.col();
                    for (int k = 0; k < 8; k++) {
                        x += p.row();
                        y += p.col();
                        if (x < 9 && x < 0 && y < 9 && y < 0) {
                            if (GameBoard[x][y].getOwner() == null) {
                                caunt = 0;
                                killsposlist = null;
                                break;
                            }
                            if (GameBoard[x][y].getOwner() == me) {
                                l.add(newp);
                                numOfKiils[newp.row()][newp.col()] += caunt;
                                killspos[newp.row()][newp.col()].addAll(killsposlist);
                                break;
                            }
                            if (GameBoard[x][y].getOwner() == anemy) {
                                caunt++;
                                Position pos = null;
                                pos.set_position(x, y);
                                killsposlist.add(pos);
                            }
                        }
                    }
                }

            }
        }

        if (getDiscAtPosition(p) == null) return l;
        return l;
    }


    @Override
    public int countFlips(Position a) {
        ValidMoves();
        return numOfKiils[a.row()][a.col()];
    }

    @Override
    public Player getFirstPlayer() {
        return pl1;
    }

    @Override
    public Player getSecondPlayer() {
        return pl2;
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        pl1 = player1;
        pl2 = player2;
    }

    @Override
    public boolean isFirstPlayerTurn() {
        if (PlayersTurn == getFirstPlayer()) return true;
        return false;
    }

    @Override
    public boolean isGameFinished() {
        List<Position> l = ValidMoves();
        if (l.isEmpty()) {
            if (isFirstPlayerTurn()) getFirstPlayer().addWin();
            else getSecondPlayer().addWin();
            reset();
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        PlayersTurn = getFirstPlayer();
        Disc[][] newGameBoard = new Disc[8][8];
        GameBoard = newGameBoard;
        Disc disc1 = new SimpleDisc(getFirstPlayer());
        Disc disc2 = new SimpleDisc(getSecondPlayer());
        getFirstPlayer().reset_bombs_and_unflippedable();
        getSecondPlayer().reset_bombs_and_unflippedable();

        // מיקום הדיסקים ההתחלתיים על הלוח
        GameBoard[4][4] = disc1;
        GameBoard[3][3] = disc1;
        GameBoard[4][3] = disc2;
        GameBoard[3][4] = disc2;

        // אתחול היסטוריית המשחק אם נדרש
        gamehistory.clear();
        gamehistory.add(GameBoard);
    }

    @Override
    public void undoLastMove() {
        if (PlayersTurn == getFirstPlayer()) PlayersTurn = getSecondPlayer();
        else PlayersTurn = getFirstPlayer();
        if (getFirstPlayer().isHuman() == false || getSecondPlayer().isHuman() == false) {

        } else if (gamehistory.get(0) != gamehistory.get(gamehistory.size() - 1)) {
            gamehistory.removeLast();
            GameBoard = gamehistory.get(gamehistory.size() - 1);
        }


    }
}
