import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.HashSet;
import java.util.Set;

public class GameLogic implements PlayableLogic {
    private List<Position> BomPossible_flip = new ArrayList<>();
    private HashSet<Disc> templist = new HashSet<>();
    private Player pl1; // הגדרת שחקן 1
    private Player pl2; // הגדרת שחקן 2
    private boolean PlayerTurn; // הגדרת התור
    public Disc[][] GameBoard = new Disc[8][8]; // הגדרת לוח המשחק שיהיה 8 על 8 כנדרש

    // אחרי הבנה של כל המהלכים האפשריים שלי שהם שמונת הריבועים
// שמסביב לדיסקית שלי אני מגדיר פה מערך עם כל הכיוונים האפשריים שלי
    private static int[][] PossibleMoves = {
            {0, 1},    // ימינה
            {0, -1},   // שמאלה
            {1, 0},    // למטה
            {-1, 0},   // למעלה
            {1, 1},    // באלכסון ימינה למטה
            {1, -1},   // באלכסון שמאלה למטה
            {-1, 1},   // באלכסון ימינה למעלה
            {-1, -1}   // באלכסון שמאלה למעלה
    };

    public GameLogic() {
    }

    // שימוש במחסנית של היסטוריית המשחק בשביל מהלך אחד אחורה שלי
    private Stack<Disc[][]> gameHistory = new Stack<>();
    private Stack<Disc> Dischistory = new Stack<>();
    // private List<Position>BomPossible_flip= new ArrayList<>();
    private boolean print = false;

    @Override
    public boolean locate_disc(Position a, Disc disc) {
        //בדיקה שהמיקום שהדיסק מעוניין להגיע אליו ריק
        if (getDiscAtPosition(a) != null || !ValidMoves().contains(a)) {
            return false;
        }
        if (disc.getType() == "⭕") {
            if (disc.getOwner().number_of_unflippedable > 0) {
                disc.getOwner().reduce_unflippedable();
                Dischistory.add(disc);
            } else return false;
        } else if (disc.getType() == "💣") {
            if (disc.getOwner().number_of_bombs > 0) {
                disc.getOwner().reduce_bomb();
                Dischistory.add(disc);
            } else return false;
        } else Dischistory.add(disc);
        // ממקם את השחקן בלוח שלי
        print = false;
        GameBoard[a.row()][a.col()] = disc;
        // עובר בלולאה על הmoves  האפשריים שלי
        for (int i = 0; i < PossibleMoves.length; i++) {
            int[] move = PossibleMoves[i];
            // סופר את מספר ההפיכות האפשריות במהלך הזה עם פונקציה שרשמנו למטה תחת הcount_flips
            countFlipsInPossibleMove(a.row(), a.col(), move[0], move[1], true);
        }
        // כאן אני מחליף בין התורות של השחקנים כלומר אם שחקן 1 שיחק אז עכשיו שחקן 2 ישחק
        PlayerTurn = !PlayerTurn;
        gameHistory.push(copy_board(GameBoard));
        GameBoard = copy_board(GameBoard);
        print = false;
        System.out.println();
        return true;
    }


    @Override
    public Disc getDiscAtPosition(Position position) {
        // בודק אם המיקום שקיבלנו על הלוח הוא null ואם כן מחזיר null
        if (GameBoard[position.row()][position.col()] == null)
            return null;
        // בודק אם המיקום שקיבלנו על הלוח הוא UnflippableDisc ואם כן מחזיר UnflippableDisc
        if (GameBoard[position.row()][position.col()].getType().equals("⭕")) {
            return new UnflippableDisc(GameBoard[position.row()][position.col()].getOwner());
        }
        // בודק אם המיקום שקיבלנו על הלוח הוא BombDisc ואם כן מחזיר BombDisc
        if (GameBoard[position.row()][position.col()].getType().equals("💣")) {
            return new BombDisc(GameBoard[position.row()][position.col()].getOwner());
        }
        // אחרי שעברנו על כל האפשרויות הוא מחזיר SimpleDisc
        return new SimpleDisc(GameBoard[position.row()][position.col()].getOwner());
    }

    @Override
    public int getBoardSize() {
        // מחזיר פשוט את הsize של הלוח
        return 8;
    }

    @Override
    public List<Position> ValidMoves() {
        // הגדרת רשימה _ valid_moves שיהיה של positions
        ArrayList<Position> _valid_moves;
        // מאתחל את המערך של ומתחיל לחפש מה הם המהלכים האפשריים שלי בשביל להוסיף למערך
        _valid_moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Position a = new Position(i, j);
                // אם גם המשבצת שאליה אני רוצה להגיע היא null וגם יש לי countflips
                // ששונה מאפס אז אפשר להוסיף את המהלך הזה למהלכים האפשריים שלי
                if (GameBoard[i][j] == null && countFlips(a) != 0) {
                    _valid_moves.add(a);
                }
            }
        }
        return _valid_moves;
    }

    @Override
    public int countFlips(Position a) {
        //int totalFlips = 0;
        HashSet<Disc> totalFlips = new HashSet<>();
        HashSet<Disc> l = new HashSet<>();
        for (int[] direction : PossibleMoves) {// {0, 1},{0, -1},

            l = countFlipsInPossibleMove(a.row(), a.col(), direction[0], direction[1], false);
            for (Disc d : l) {
                if (!totalFlips.contains(d)) totalFlips.add(d);
            }
        }
        return totalFlips.size();
    }

    // פונקציה שסופרת כמה זה הופך
    private HashSet<Disc> countFlipsInPossibleMove(int row, int col, int rowDir, int colDir, boolean flip) {
        // קודם כל מגדיר בתור התחלה את מספר הflips לאפס

        // הגדרה של שני השחקנים שלנו ואז אני מבצע בדיקה של מי התור
        Player anemy;
        Player me;
        // בדיקה של מי התור
        if (isFirstPlayerTurn()) {
            me = pl1;
            anemy = pl2;
        } else {
            anemy = pl1;
            me = pl2;
        }
        // הגדרה של השורה והעמודה המתאימים בהתאם לכיוון אליו רוצה השחקן ללכת
        int currentRow = row + rowDir;
        int currentCol = col + colDir;

        // רשימה לאחסון הדיסקין שניתנים להיפוך
        HashSet<Disc> possible_flip = new HashSet<>();
        BomPossible_flip.clear();
        templist.clear();
        //  מעבר על כל תא בהתאם לmove שנבחר וכל זה קורה בתנאי שהמהלך עדיין בתוך הלוח וגם
        //  המיקום הוא לא null וגם הדיסק אותו אנחנו פוגשים במיקום הוא של היריב

        while (isInGameBoard(currentRow, currentCol) && GameBoard[currentRow][currentCol] != null && GameBoard[currentRow][currentCol].getOwner().equals(anemy)) {

            if (GameBoard[currentRow][currentCol].getType().equals("⬤")) {
                possible_flip.add(GameBoard[currentRow][currentCol]);
            } else if (GameBoard[currentRow][currentCol].getType().equals("💣")) {
                if (!BomPossible_flip.contains(new Position(currentRow, currentCol)))
                    BomPossible_flip.add(new Position(currentRow, currentCol));
                possible_flip.add(GameBoard[currentRow][currentCol]);
            }
            // התקדמות לתא הבא בהתאם למהלך שעשינו
            currentRow += rowDir;
            currentCol += colDir;

            // בודק אם התא הבא אם הוא מחוץ לתחום או שהוא null או מכיל דיסק שהוא לא של היריב אז תאפס את מספר ההפיכות
            if (!isInGameBoard(currentRow, currentCol) || GameBoard[currentRow][currentCol] == null) {
                possible_flip.clear();
                BomPossible_flip.clear();
                templist.clear();
            }
        }
        // בתנאי שמספר ההפיכות שלנו שונה מאפס אז תעבור על כל דיסקית ותשנה את הבעלות שלה לשחקן הנוכחי
        // ArrayList<Disc> templist = new ArrayList<>();
        int tempBomPossible_flipsize = BomPossible_flip.size();
        Boolean b = true;
        if (BomPossible_flip.size() > 0) {
            int c = 0;
            while (BomPossible_flip.size() > 0 && b == true) {
                Position p;
                if (BomPossible_flip.size() > c) {
                    p = BomPossible_flip.get(c);

                    if (BomPossible_flip.size() == 3 && flip == false) {
                    }
                    for (Disc h : funk(p.row(), p.col(), anemy)) {
                        templist.add(h);
                    }
                    if (tempBomPossible_flipsize == BomPossible_flip.size() && c == BomPossible_flip.size()) {
                        b = false;
                        // break;
                    } else tempBomPossible_flipsize = BomPossible_flip.size();
                    c++;
                } else break;
            }

            for (Disc D : templist) {
//           if(!possible_flip.contains(D))
                possible_flip.add(D);

            }
//        for (Position p:BomPossible_flip){
//            if(!possible_flip.contains(GameBoard[p.col()][p.row()]))possible_flip.add(GameBoard[p.col()][p.row()]);
//        }
        }
        if (possible_flip.size() != 0 && flip) {

            String player;
            if (!isFirstPlayerTurn()) {
                player = "1";
            } else {
                player = "2";
            }
            if (print == false) {
                print = true;
                System.out.println("Player " + player + " placed a " + GameBoard[row][col].getType() + " in (" + row + "," + col + ")");
            }


            for (Disc d : possible_flip) {
                if (d != null) {
                    d.setOwner(me);
                    int r = 0, c = 0;
                    for (int i = 0; i < GameBoard.length; i++) {
                        for (int j = 0; j < GameBoard.length; j++) {
                            if (GameBoard[i][j] == d) {
                                r = i;
                                c = j;
                            }
                        }
                    }
                    System.out.println("Player " + player + " flipped the " + d.getType() + " in (" + r + "," + c + ")");
                }
            }

        }
        // מחזיר בסוף את מספר ההפיכות בהתאם למהלך שתרצה לבצע
        return possible_flip;
    }

    private HashSet<Disc> funk(int currentRow, int currentCol, Player anemy) {
        if (GameBoard[currentRow][currentCol].getType().equals("💣")) {
            // הפיכת שמונת הכיוונים שסביב הפצצה
            for (int[] move : PossibleMoves) {
                int bombRow = currentRow + move[0];
                int bombCol = currentCol + move[1];

                // בדיקה שהמיקום חוקי ונמצא בתוך גבולות הלוח
                if (isInGameBoard(bombRow, bombCol) && GameBoard[bombRow][bombCol] != null) {

                    if (GameBoard[bombRow][bombCol].getOwner().equals(anemy)) {
//                        if (!GameBoard[bombRow][bombCol].getType().equals("⭕")/* &&!Contains(BomPossible_flip,GameBoard[bombRow][bombCol])*/) {
//                            templist.add(GameBoard[bombRow][bombCol]);
////                            if (GameBoard[bombRow][bombCol].getType().equals("💣") &&!Contains(possible_flip,GameBoard[bombRow][bombCol]))
//                                BomPossible_flip.add(new Position(bombRow,bombCol));
//                            templist.add( GameBoard[currentRow][currentCol]);
//
//                        }
                        if (GameBoard[bombRow][bombCol].getType().equals("⬤")) {
                            templist.add(GameBoard[bombRow][bombCol]);
                        } else if (GameBoard[bombRow][bombCol].getType().equals("💣")) {
                            if (!BomPossible_flip.contains(new Position(bombRow, bombCol)))
                                BomPossible_flip.add(new Position(bombRow, bombCol));
                            templist.add(GameBoard[bombRow][bombCol]);
                        }

                    }
                }
                bombRow = currentRow - move[0];
                bombCol = currentCol - move[1];
            }
        }
        return templist;
    }

    // בדיקה שהmove קורה בתוך גבולות הלוח שלנו
    private boolean isInGameBoard(int row, int col) {
        return row >= 0 && row < GameBoard.length && col >= 0 && col < GameBoard[0].length;

    }

    @Override
    public Player getFirstPlayer() {
        // הגדרת השחקן הראשון להיות pl1
        return pl1;
    }

    @Override
    public Player getSecondPlayer() {
        // הגדרת השחקן השני להיות pl2
        return pl2;
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        // השמה של pl1 להיות השחקן הראשון וpl2 להיות השחקן השני
        pl1 = player1;
        pl2 = player2;
    }

    @Override
    public boolean isFirstPlayerTurn() {
        // מחזיר של true/false בשאלה אם זה התור של השחקן הראשון
        return PlayerTurn;
    }

    @Override
    public boolean isGameFinished() {
        // בודק אם יש עוד מהלכים שאפשר לעשות במשחק ואם אין אז מחזיר true והמשחק נגמר
        if (ValidMoves().isEmpty()) {
            int p1 = 0, p2 = 0;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (GameBoard[i][j] == null) {
                    } else if (GameBoard[i][j].getOwner().isPlayerOne == true) p1++;
                    else if (GameBoard[i][j].getOwner().isPlayerOne == false) p2++;


                }
            }
            if (p1 > p2) getFirstPlayer().addWin();
            if (p1 < p2) getSecondPlayer().addWin();
            reset();
            String player;
            String otherplayer;
            int cp = p1, op = p2;

            if (isFirstPlayerTurn()) {
                player = "1";
                otherplayer = "2";
            } else {
                op = p1;
                cp = p2;
                player = "2";

                otherplayer = "1";
            }


            System.out.println("Player " + player + " wins with " + cp + " discs! Player " + otherplayer + "  had " + op + " discs. ");
        }

        return ValidMoves().isEmpty();
    }


    public void reset() {
        PlayerTurn = true;
        GameBoard = new Disc[8][8];
        // מאפס את הדיסקים המיוחדים של כל שחקן
        getFirstPlayer().reset_bombs_and_unflippedable();
        getSecondPlayer().reset_bombs_and_unflippedable();

        // מיקום הדיסקים ההתחלתיים על הלוח
        GameBoard[3][3] = new SimpleDisc(pl1);
        GameBoard[4][4] = new SimpleDisc(pl1);
        GameBoard[3][4] = new SimpleDisc(pl2);
        GameBoard[4][3] = new SimpleDisc(pl2);
        // אתחול היסטוריית המשחק אם נדרש

        gameHistory.clear();
        Dischistory.clear();
        Dischistory.push(new BombDisc(getFirstPlayer()));
        gameHistory.push(copy_board(GameBoard));

        this.ValidMoves();
    }

    @Override
    public void undoLastMove() {
        System.out.println("Undoing last move:");
        Player anemy;
        Player me;

        Disc[][] temp_GameBoard = new Disc[8][8];
        // בדיקה של מי התור
        if (isFirstPlayerTurn()) {
            me = pl1;
            anemy = pl2;
        } else {
            anemy = pl1;
            me = pl2;
        }
        PlayerTurn = !PlayerTurn;
        if (gameHistory.size() == 1)
            PlayerTurn = true;

        // בדיקה אם יש מספיק מהלכים כדי לחזור אחורה
        if (gameHistory.size() != 1) {
            // הסרת המהלך האחרון מההיסטוריה כדי לחזור למהלך הקודם
            temp_GameBoard = copy_board(gameHistory.pop());
            Disc d = Dischistory.pop();
            // הגדרת הלוח למהלך הקודם בעזרת עותק חדש מההיסטוריה


            int numBombDisc = 0;
            int numUnflippableDisc = 0;
            if (d.getType() == "💣") numBombDisc = 1;
            if (d.getType() == "⭕") numUnflippableDisc = 1;
            int anemy_bom = anemy.number_of_bombs;
            int anemy_unflip = anemy.number_of_unflippedable;
            if (numBombDisc > 0 || numUnflippableDisc > 0) {

                anemy.reset_bombs_and_unflippedable();

                while (anemy_bom + numBombDisc != anemy.number_of_bombs) {
                    anemy.reduce_bomb();
                }
                while (anemy_unflip + numUnflippableDisc != anemy.number_of_unflippedable) {
                    anemy.reduce_unflippedable();
                }
            }
            // הגדרת הלוח למהלך הקודם בעזרת עותק חדש מההיסטוריה
            GameBoard = copy_board(gameHistory.peek());


        }
        Position pivotDisc = new Position(0, 0);
        List<Position> flipedDiscs = new ArrayList<>();

        for (int i = 0; i < temp_GameBoard.length; i++) {
            for (int j = 0; j < temp_GameBoard.length; j++) {
                if (GameBoard[i][j] != temp_GameBoard[i][j]) {
                    if (GameBoard[i][j] == null) pivotDisc = new Position(i, j);
                    else if (GameBoard[i][j].getOwner() != temp_GameBoard[i][j].getOwner())
                        flipedDiscs.add(new Position(i, j));
                }

            }
        }
        if (flipedDiscs.size() == 0) System.out.println("No previous move available to undo .");
        else {
            System.out.println("Undo: removing " + temp_GameBoard[pivotDisc.row()][pivotDisc.col()].getType() + " from (" + pivotDisc.row() + "," + pivotDisc.col() + ") ");
            for (Position position : flipedDiscs) {
                System.out.println("Undo: flipping back " + temp_GameBoard[position.row()][position.col()].getType() + " in (" + position.row() + "," + position.col() + ") ");
            }
        }

    }

    private Disc[][] copy_board(Disc[][] Board) {
        Disc[][] board = new Disc[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (Board[i][j] == null) {
                    board[i][j] = null;
                } else if (Board[i][j].getType().equals("⭕")) {
                    board[i][j] = new UnflippableDisc(Board[i][j].getOwner());
                } else if (Board[i][j].getType().equals("💣")) {
                    board[i][j] = new BombDisc(Board[i][j].getOwner());
                } else if (Board[i][j].getType().equals("⬤")) {
                    board[i][j] = new SimpleDisc(Board[i][j].getOwner());
                }
            }
        }
        return board;
    }
}