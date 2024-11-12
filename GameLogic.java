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
        gamehistory.add(GameBoard);
    }
//    private int[][] numOfKiils = new int[8][8];
    private List<Disc[][]> gamehistory = new ArrayList<>();
    private List<Position>[][] killspos = new List[8][8];
    private Player PlayersTurn;

    @Override
    public boolean locate_disc(Position a, Disc disc) {
//        GameBoard[a.row()][a.col()] = disc;
//        return true;


        if (getDiscAtPosition(a) != null) return false;//בדיקה שהמיקום שהדיסק מעוניין להגיע איליו ריק
        if (Contains(ValidMoves(),a)) {
            GameBoard[a.row()][a.col()] = disc;// ממקם את השחקן בלוח
           List<Position> l=killspos[a.row()][a.col()];
            for (int i = 0; i <l.size() ; i++) {
               GameBoard[l.get(i).row()][l.get(i).col()]=new SimpleDisc(disc.getOwner());
            }
            if (PlayersTurn == getFirstPlayer()) PlayersTurn = getSecondPlayer();
            else PlayersTurn = getFirstPlayer();

            return true;
        }
        return false;
    }
private Boolean Contains (List<Position> ValidMoves,Position a){
        if (ValidMoves.size()==0)return false;
    for (int i = 0; i < ValidMoves.size(); i++) {
        if(ValidMoves.get(i).equals(a))return  true;
    }
    return false;
}
    @Override
    public Disc getDiscAtPosition(Position position) {
        if (GameBoard[position.row()][position.col()] == null)
            return null; // בודק אם המיקום שקיבלנו על הלוח הוא null ואם כן מחזיר null
        return GameBoard[position.row()][position.col()]; // במידה והמיקום לא היה null הוא מחזיר למי שייך הדיסקית באותה נקודה
    }

    @Override
    public int getBoardSize() {
        return 8;
    }

    @Override
    public List<Position> ValidMoves() {
        for (int i = 0; i <killspos.length ; i++) {
            for (int j = 0; j < killspos.length; j++) {
                killspos[j][i]=new ArrayList<>();
            }
        }
        List<Position> l = new ArrayList<Position>();

            for (Position p : listposi()) {
                l.addAll(healp_validmovs(p));
            }
        return l;
    }
    public List<Position> listposi(){
        List<Position> p = new ArrayList<Position>();
        Player tempp= getSecondPlayer();
        if (isFirstPlayerTurn() == false) {
            tempp= getFirstPlayer();
        }
        for (int i = 0; i <GameBoard.length ; i++) {
            for (int j = 0; j < GameBoard.length; j++) {
                if(GameBoard[j][i]!=null)
                if (GameBoard[j][i].getOwner().equals(tempp))p.add(new Position(j,i));
            }
        }
        return p;
    }


    public List<Position> healp_validmovs(Position pos) {
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
        Position temp = new Position(pos.row() - 1, pos.col() - 1);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (temp.row() + j < 9 && temp.row() + j >0 && temp.col() + j < 9 && temp.col() + j >0) {
                    Position newp = new Position(temp.row() + j, temp.col() + i);
                    if (getDiscAtPosition(newp) != null) break;
//                    if(GameBoard[newp.X][newp.Y].getOwner()==anemy)return false;
                    int caunt = 0;
                    List<Position> killsposlist = new ArrayList<Position>();
                    int x = pos.row() - newp.row();
                    int y = pos.col() - newp.col();
                    Position p= new Position(pos.row(),pos.col());
                    for (int k = 0; k < 8; k++) {
                        if(k==0){
                            p.set_position(pos.row()+x,pos.col()+y);
                        }
                        else p.set_position(p.row()+x,p.col()+y);

//                        x += p.row();
//                        y += p.col();
                        if (p.row() < 8 && p.row() > 0 && p.col() < 8 && p.col() > 0) {
                            if (GameBoard[p.row()][p.col()] == null) {
                                caunt = 0;
                                killsposlist = null;
                                break;
                            }
                            if (GameBoard[p.row()][p.col()].getOwner() == me) {
                                l.add(newp);
//                                numOfKiils[newp.row()][newp.col()] += caunt;
                                if( killspos[newp.row()][newp.col()].size()==0)killspos[newp.row()][newp.col()]=killsposlist;
                               else killspos[newp.row()][newp.col()].addAll(killsposlist);
                                break;
                            }
                            if (GameBoard[p.row()][p.col()].getOwner() == anemy) {
                                caunt++;
                                killsposlist.add(new Position(p.row(),p.col()));
                            }
                        }
                    }
                }

            }
        }

        if (getDiscAtPosition(pos) == null) return l;
        return l;
    }


    @Override
    public int countFlips(Position a) {
        ValidMoves();
        return killspos[a.row()][a.col()].size();
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
//        List<Position> l = ValidMoves();
//        if (l.isEmpty()) {
//            if (isFirstPlayerTurn()) getFirstPlayer().addWin();
//            else getSecondPlayer().addWin();
//            reset();
//            return true;
//        }
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
