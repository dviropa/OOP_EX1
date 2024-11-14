import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameLogic implements PlayableLogic {
    private Player pl1; // הגדרת שחקן 1
    private Player pl2; // הגדרת שחקן 2
    private boolean PlayerTurn; // הגדרת התור
    //public Disc[][] GameBoard = new Disc[8][8]; // הגדרת לוח המשחק שיהיה 8 על 8 כנדרש
    private   Disc[][] d =new  Disc[8][8];
public GameBoardClass GameBoard1=new GameBoardClass( d);
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
    private Stack<GameBoardClass> gameHistory = new Stack<>();

    @Override
    public boolean locate_disc(Position a, Disc disc) {
        //בדיקה שהמיקום שהדיסק מעוניין להגיע אליו ריק
        if(isFirstPlayerTurn()==true){
            if(disc.getType()=="⭕"){
                if(getFirstPlayer().number_of_unflippedable>0){
                    getFirstPlayer().reduce_unflippedable();
                    GameBoard1.addfUnflippableDisc();
                }

                else return false;
            }
            if(disc.getType()=="💣"){
                if(getFirstPlayer().number_of_bombs>0){

                    getFirstPlayer().reduce_bomb();
                GameBoard1.addfBombDisc();

                }
                else return false;
            }
        }
        else {
            if(disc.getType()=="⭕"){
                if(getSecondPlayer().number_of_unflippedable>0) {
                    getSecondPlayer().reduce_unflippedable();
                    GameBoard1.addfUnflippableDisc();
                }
                else return false;
            }
            if(disc.getType()=="💣"){
                if(getSecondPlayer().number_of_bombs>0) {
                    getSecondPlayer().reduce_bomb();
                    GameBoard1.addfBombDisc();
                }
                else return false;
            }
        }
        if (getDiscAtPosition(a) != null || !ValidMoves().contains(a)) {
            return false;
        }
        // ממקם את השחקן בלוח שלי
        GameBoard1.GameBoard[a.row()][a.col()] = disc;
        // עובר בלולאה על הmoves  האפשריים שלי
        for (int i = 0; i < PossibleMoves.length; i++) {
            int[] move = PossibleMoves[i];
            // סופר את מספר ההפיכות האפשריות במהלך הזה עם פונקציה שרשמנו למטה תחת הcount_flips
            countFlipsInPossibleMove(a.row(), a.col(), move[0], move[1], true);
        }
        // כאן אני מחליף בין התורות של השחקנים כלומר אם שחקן 1 שיחק אז עכשיו שחקן 2 ישחק
        PlayerTurn = !PlayerTurn;
        String player;
        if (isFirstPlayerTurn()) {
            player = "1";
        } else {
            player = "2";
        }
        System.out.println("The player " + player + " placed a " + disc.getType() + " in (" + a.row() + "," + a.col() + ")");
        gameHistory.push(new GameBoardClass(GameBoard1.copy_board(GameBoard1.GameBoard),GameBoard1.get_num_of_BombDisc(),GameBoard1.get_num_of_UnflippableDisc()));
        GameBoard1= new GameBoardClass(GameBoard1.copy_board(GameBoard1.GameBoard));

        return true;
    }


    @Override
    public Disc getDiscAtPosition(Position position) {
        // בודק אם המיקום שקיבלנו על הלוח הוא null ואם כן מחזיר null
        if (GameBoard1.GameBoard[position.row()][position.col()] == null)
            return null;
        // בודק אם המיקום שקיבלנו על הלוח הוא UnflippableDisc ואם כן מחזיר UnflippableDisc
        if (GameBoard1.GameBoard[position.row()][position.col()].getType() == "⭕") {
            return new UnflippableDisc(GameBoard1.GameBoard[position.row()][position.col()].getOwner());
        }
        // בודק אם המיקום שקיבלנו על הלוח הוא BombDisc ואם כן מחזיר BombDisc
        if (GameBoard1.GameBoard[position.row()][position.col()].getType() == "💣") {
            return new BombDisc(GameBoard1.GameBoard[position.row()][position.col()].getOwner());
        }
        // אחרי שעברנו על כל האפשרויות הוא מחזיר SimpleDisc
        return new SimpleDisc(GameBoard1.GameBoard[position.row()][position.col()].getOwner());
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
                if (GameBoard1.GameBoard[i][j] == null && countFlips(a) != 0) {
                    _valid_moves.add(a);
                }
            }
        }
        return _valid_moves;
    }

    @Override
    public int countFlips(Position a) {
        int totalFlips = 0;
        for (int[] direction : PossibleMoves) {// {0, 1},{0, -1},
            totalFlips += countFlipsInPossibleMove(a.row(), a.col(), direction[0], direction[1], false);
        }
        return totalFlips;
    }

    // פונקציה שסופרת כמה זה הופך
    private int countFlipsInPossibleMove(int row, int col, int rowDir, int colDir, boolean flip) {
        // קודם כל מגדיר בתור התחלה את מספר הflips לאפס
        int numOfFlips = 0;
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
        ArrayList<Disc> possible_flip = new ArrayList<>();

        //  מעבר על כל תא בהתאם לmove שנבחר וכל זה קורה בתנאי שהמהלך עדיין בתוך הלוח וגם
        //  המיקום הוא לא null וגם הדיסק אותו אנחנו פוגשים במיקום הוא של היריב
        while (isInGameBoard(currentRow, currentCol) && GameBoard1.GameBoard[currentRow][currentCol] != null && GameBoard1.GameBoard[currentRow][currentCol].getOwner().equals(anemy)) {
            numOfFlips++;
            if (flip == false && GameBoard1.GameBoard[currentRow][currentCol].getType() == "⭕") numOfFlips--;
            if (flip == true && GameBoard1.GameBoard[currentRow][currentCol].getType() != "⭕") {
                possible_flip.add(GameBoard1.GameBoard[currentRow][currentCol]);
            }
            if (flip == true&&GameBoard1.GameBoard[currentRow][currentCol].getType()=="💣"&&GameBoard1.GameBoard[currentRow][currentCol].getOwner()==anemy) {
                for (int i = 0; i < PossibleMoves.length; i++) {
                    int[] move = PossibleMoves[i];
                    while (isInGameBoard(move[0] + currentRow, move[1] + currentCol)) {
                        if (GameBoard1.GameBoard[move[0] + currentRow] [move[1] + currentCol]!=null &&GameBoard1.GameBoard[move[0] + currentRow] [move[1] + currentCol].getOwner()==anemy)
                            possible_flip.add(GameBoard1.GameBoard[move[0] + currentRow] [move[1] + currentCol]);
                    }

                }

            }
            // התקדמות לתא הבא בהתאם למהלך שעשינו
            currentRow += rowDir;
            currentCol += colDir;
        }
        // בודק אם התא הבא אם הוא מחוץ לתחום או שהוא null או מכיל דיסק שהוא לא של היריב אז תאפס את מספר ההפיכות
        if (!isInGameBoard(currentRow, currentCol) || GameBoard1.GameBoard[currentRow][currentCol] == null || GameBoard1.GameBoard[currentRow][currentCol].getOwner() != me) {
            numOfFlips = 0;
        }
        // בתנאי שמספר ההפיכות שלנו שונה מאפס אז תעבור על כל דיסקית ותשנה את הבעלות שלה לשחקן הנוכחי
        if (numOfFlips != 0) {
            for (int i = 0; i < possible_flip.size(); i++) {
                possible_flip.get(i).setOwner(me);
            }

        }
        // מחזיר בסוף את מספר ההפיכות בהתאם למהלך שתרצה לבצע
        return numOfFlips;
    }

    // בדיקה שהmove קורה בתוך גבולות הלוח שלנו
    private boolean isInGameBoard(int row, int col) {
        if ((row >= 0 && row < GameBoard1.GameBoard.length && col >= 0 && col < GameBoard1.GameBoard[0].length) == true)
            return true;
        else
            return false;
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
        return ValidMoves().isEmpty();
    }


    public void reset() {
        PlayerTurn = true;
        GameBoard1.GameBoard = new Disc[8][8];
        // מאפס את הדיסקים המיוחדים של כל שחקן
        getFirstPlayer().reset_bombs_and_unflippedable();
        getSecondPlayer().reset_bombs_and_unflippedable();

        // מיקום הדיסקים ההתחלתיים על הלוח
        GameBoard1.GameBoard[3][3] = new SimpleDisc(pl1);
        GameBoard1.GameBoard[4][4] = new SimpleDisc(pl1);
        GameBoard1.GameBoard[3][4] = new SimpleDisc(pl2);
        GameBoard1.GameBoard[4][3] = new SimpleDisc(pl2);
        // אתחול היסטוריית המשחק אם נדרש

        gameHistory.clear();
        gameHistory.push(new GameBoardClass(GameBoard1.copy_board(GameBoard1.GameBoard),GameBoard1.get_num_of_BombDisc(),GameBoard1.get_num_of_UnflippableDisc()));
        GameBoard1= new GameBoardClass(GameBoard1.copy_board(GameBoard1.GameBoard));

        this.ValidMoves();
    }

    @Override
    public void undoLastMove() {
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
        PlayerTurn = !PlayerTurn;
        if (gameHistory.size() == 1)
            PlayerTurn = true;

        // בדיקה אם יש מספיק מהלכים כדי לחזור אחורה
        if (gameHistory.size() != 1) {
            // הסרת המהלך האחרון מההיסטוריה כדי לחזור למהלך הקודם
            GameBoardClass Board1 =gameHistory.pop();
            int numBombDisc=anemy.number_of_bombs;
            int numUnflippableDisc=anemy.number_of_unflippedable;
            if(Board1.get_num_of_BombDisc()>0||Board1.get_num_of_UnflippableDisc()>0) {

                anemy.reset_bombs_and_unflippedable();
                if(Board1.get_num_of_BombDisc()>0)
                for (int i = 0; i < 3 - Board1.get_num_of_BombDisc(); i++) {
                    anemy.reduce_bomb();
                }
                else while (numBombDisc!=anemy.number_of_bombs){
                    anemy.reduce_bomb();
                }

                if(Board1.get_num_of_UnflippableDisc()>0)
                for (int i = 0; i < 2 - Board1.get_num_of_UnflippableDisc(); i++) {
                    anemy.reduce_unflippedable();
                }
               else while (numUnflippableDisc!=anemy.number_of_unflippedable){
                    anemy.reduce_unflippedable();
                }
            }
            // הגדרת הלוח למהלך הקודם בעזרת עותק חדש מההיסטוריה
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (gameHistory.peek().GameBoard[j][i] == null) GameBoard1.GameBoard[j][i] = gameHistory.peek().GameBoard[j][i];
                    else GameBoard1.GameBoard[j][i].setOwner(gameHistory.peek().GameBoard[j][i].getOwner());
                }
            }
        }


    }

    private Disc[][] copy_board(Disc[][] Board) {
        Disc[][] board = new Disc[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (Board[j][i] == null) {
                    board[j][i] = null;
                } else if (Board[j][i].getType() == "⭕") {
                    board[j][i] = new UnflippableDisc(Board[j][i].getOwner());
                } else if (Board[j][i].getType() == "💣") {
                    board[i][j] = new BombDisc(Board[j][i].getOwner());
                } else if (Board[j][i].getType() == "⬤") {
                    board[j][i] = new SimpleDisc(Board[j][i].getOwner());
                }
            }
        }
        return board;
    }
}
