import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.HashSet;

public class GameLogic implements PlayableLogic {


    private List<Position> bombPossible_flip = new ArrayList<>();
    private HashSet<Disc> tempList = new HashSet<>();
    private Player pl1; // הגדרת שחקן 1
    private Player pl2; // הגדרת שחקן 2
    private boolean playerTurn; // הגדרת התור
    public Disc[][] gameBoard = new Disc[8][8]; // הגדרת לוח המשחק שיהיה 8 על 8 כנדרש

    private static int[][] possibleMoves = {
            // אחרי הבנה של כל המהלכים האפשריים שלי שהם שמונת הריבועים שמסביב לדיסקית שלי אני מגדיר פה מערך עם כל הכיוונים האפשריים שלי

            {0, 1},
            {0, -1},
            {1, 0},
            {-1, 0},
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
    };

    public GameLogic() {
    }

    // שימוש במחסנית של היסטוריית המשחק בשביל מהלך אחד אחורה שלי
    private Stack<Disc[][]> gameHistory = new Stack<>();
    private Stack<Disc> discHistory = new Stack<>();
    private boolean print = false;



    @Override
    public boolean locate_disc(Position a, Disc disc) {
        // בדיקה שהמיקום שהדיסק מעוניין להגיע אליו ריק ושמהלך זה חוקי
        if (getDiscAtPosition(a) != null || !ValidMoves().contains(a)) {
            return false; // אם המקום תפוס או המהלך אינו חוקי אז הוא מחזיר false

        }
        // טיפול בסוגים השונים של הדיסקים האפשריים
        if (disc.getType() == "⭕") {
            if (disc.getOwner().number_of_unflippedable > 0) {
                disc.getOwner().reduce_unflippedable();
                discHistory.add(disc);
            } else return false;
        } else if (disc.getType() == "💣") {
            if (disc.getOwner().number_of_bombs > 0) {
                disc.getOwner().reduce_bomb();
                discHistory.add(disc);
            } else return false;
        } else discHistory.add(disc);
        // מיקום הדיסק בלוח המשחק
        print = false; // מניעת הדפסה מיותרת
        gameBoard[a.row()][a.col()] = disc;
        // עובר בלולאה על הmoves  האפשריים שלי
        for (int i = 0; i < possibleMoves.length; i++) {
            int[] move = possibleMoves[i];
            // סופר את מספר ההפיכות האפשריות במהלך הזה
            countFlipsInPossibleMove(a.row(), a.col(), move[0], move[1], true);
        }
        // כאן אני מחליף בין התורות של השחקנים כלומר אם שחקן 1 שיחק אז עכשיו שחקן 2 ישחק
        playerTurn = !playerTurn;
        gameHistory.push(copy_board(gameBoard));
        gameBoard = copy_board(gameBoard);
        print = false;
        System.out.println();
        return true;
    }


    @Override
    public Disc getDiscAtPosition(Position position) {
        // בודק אם המיקום שקיבלנו על הלוח הוא null ואם כן מחזיר null
        if (gameBoard[position.row()][position.col()] == null)
            return null;
        // בודק אם המיקום שקיבלנו על הלוח הוא UnflippableDisc ואם כן מחזיר UnflippableDisc
        if (gameBoard[position.row()][position.col()].getType().equals("⭕")) {
            return new UnflippableDisc(gameBoard[position.row()][position.col()].getOwner());
        }
        // בודק אם המיקום שקיבלנו על הלוח הוא BombDisc ואם כן מחזיר BombDisc
        if (gameBoard[position.row()][position.col()].getType().equals("💣")) {
            return new BombDisc(gameBoard[position.row()][position.col()].getOwner());
        }
        // אחרי שעברנו על כל האפשרויות וזה לא null/BombDisc/UnflippableDisc הוא מחזיר SimpleDisc
        return new SimpleDisc(gameBoard[position.row()][position.col()].getOwner());
    }


    @Override
    public int getBoardSize() {
        // מחזיר פשוט את הsize של הלוח שזה 8 כי הלוח שלנו הוא 8 על 8
        return 8;
    }


    @Override
    public List<Position> ValidMoves() {
        // הגדרת רשימה _ valid_moves שיהיה מסוג positions
        ArrayList<Position> _valid_moves;
        // מאתחל את המערך של ומתחיל לחפש מה הם המהלכים האפשריים שלי בשביל להוסיף למערך
        _valid_moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Position a = new Position(i, j);
                // אם גם המשבצת שאליה אני רוצה להגיע היא null וגם יש לי countflips
                // ששונה מאפס אז אפשר להוסיף את המהלך הזה למהלכים האפשריים שלי
                if (gameBoard[i][j] == null && countFlips(a) != 0) {
                    _valid_moves.add(a);
                }
            }
        }
        // מחזיר את רשימת המהלכים האפשריים
        return _valid_moves;
    }


    @Override
    public int countFlips(Position a) {
        // יצירת HashSet לאחסון כל הדיסקים שניתן להפוך במהלך זה
        HashSet<Disc> totalFlips = new HashSet<>();
        HashSet<Disc> l = new HashSet<>();
        // עובר על כל הכיוונים האפשריים למהלכים (למשל, {0, 1}, {0, -1})
        for (int[] direction : possibleMoves) {
            // מחשב את הדיסקים שניתן להפוך בכיוון הנוכחי
            l = countFlipsInPossibleMove(a.row(), a.col(), direction[0], direction[1], false);
            // מוסיף את הדיסקים מ-l ל-totalFlips אם הם לא קיימים כבר
            for (Disc d : l) {
                if (!totalFlips.contains(d)) totalFlips.add(d);
            }
        }
        // מחזיר את מספר הדיסקים שניתן להפוך בסך הכול

        return totalFlips.size();
    }


    private HashSet<Disc> countFlipsInPossibleMove(int row, int col, int rowDir, int colDir, boolean flip) {
        // פונקציה שמחשבת אילו דיסקים ניתן להפוך במהלך מסוים בכיוון מסוים


        // הגדרה של שני השחקנים שלנו
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

        // רשימות לאחסון דיסקים שניתן להפוך ודיסקים הקשורים לפצצות
        HashSet<Disc> possible_flip = new HashSet<>();
        bombPossible_flip.clear();
        tempList.clear();

        // לולאה שעוברת בכיוון הרצוי עד שמוצאים סוף חוקי או תא שאינו ניתן להפיכה
        while (isInGameBoard(currentRow, currentCol) && gameBoard[currentRow][currentCol] != null && gameBoard[currentRow][currentCol].getOwner().equals(anemy)) {

            // אם הדיסק רגיל, מוסיפים אותו לרשימה
            if (gameBoard[currentRow][currentCol].getType().equals("⬤")) {
                possible_flip.add(gameBoard[currentRow][currentCol]);
                // אם הדיסק הוא פצצה, מוסיפים אותו לרשימה הרלוונטית
            } else if (gameBoard[currentRow][currentCol].getType().equals("💣")) {
                if (!bombPossible_flip.contains(new Position(currentRow, currentCol)))
                    bombPossible_flip.add(new Position(currentRow, currentCol));
                possible_flip.add(gameBoard[currentRow][currentCol]);
            }
            // התקדמות לתא הבא בהתאם למהלך שעשינו
            currentRow += rowDir;
            currentCol += colDir;

            // אם יצאנו מהתחום או הגענו לתא שאינו חוקי, מנקים את הרשימות
            if (!isInGameBoard(currentRow, currentCol) || gameBoard[currentRow][currentCol] == null) {
                possible_flip.clear();
                bombPossible_flip.clear();
                tempList.clear();
            }
        }
        // טיפול במקרים מיוחדים של פצצות, כולל שימוש בפונקציה רקורסיבית
        int tempBomPossible_flipsize = bombPossible_flip.size();
        Boolean b = true;
        if (bombPossible_flip.size() > 0) {
            int c = 0;
            while (bombPossible_flip.size() > 0 && b == true) {
                Position p;
                if (bombPossible_flip.size() > c) {
                    p = bombPossible_flip.get(c);

                    if (bombPossible_flip.size() == 3 && flip == false) {
                    }

                    // הוספת דיסקים מהרשימה הזמנית שנוצרה בפונקציה הרקורסיבית
                    for (Disc h : recursiveBombFunction(p.row(), p.col(), anemy)) {
                        tempList.add(h);
                    }

                    // בדיקה אם אין שינוי בגודל הרשימה כדי לצאת מהלולאה
                    if (tempBomPossible_flipsize == bombPossible_flip.size() && c == bombPossible_flip.size()) {
                        b = false;
                        // break;
                    } else tempBomPossible_flipsize = bombPossible_flip.size();
                    c++;
                } else break;
            }

            // הוספת כל הדיסקים שנמצאו כתוצאה מעיבוד הפצצות לרשימה הכללית
            for (Disc D : tempList) {
                possible_flip.add(D);

            }

        }

        // אם flip=true, מבצעים את ההפיכה בפועל
        if (possible_flip.size() != 0 && flip) {

            String player;
            if (!isFirstPlayerTurn()) {
                player = "1";
            } else {
                player = "2";
            }

            // הדפסת המהלך של השחקן
            if (print == false) {
                print = true;
                System.out.println("Player " + player + " placed a " + gameBoard[row][col].getType() + " in (" + row + "," + col + ")");
            }

            // הפיכת כל הדיסקים ברשימה לבעלות של השחקן הנוכחי
            for (Disc d : possible_flip) {
                if (d != null) {
                    d.setOwner(me);
                    int r = 0, c = 0;
                    // מציאת המיקום של הדיסק לצורך הדפסה
                    for (int i = 0; i < gameBoard.length; i++) {
                        for (int j = 0; j < gameBoard.length; j++) {
                            if (gameBoard[i][j] == d) {
                                r = i;
                                c = j;
                            }
                        }
                    }
                    System.out.println("Player " + player + " flipped the " + d.getType() + " in (" + r + "," + c + ")");
                }
            }

        }
        // החזרת רשימת הדיסקים שניתן להפוך
        return possible_flip;
    }


    private HashSet<Disc> recursiveBombFunction(int currentRow, int currentCol, Player anemy) {
        // בדיקה אם התא הנוכחי מכיל פצצה
        if (gameBoard[currentRow][currentCol].getType().equals("💣")) {
            // הפיכת שמונת הכיוונים שסביב הפצצה
            for (int[] move : possibleMoves) {
                int bombRow = currentRow + move[0];
                int bombCol = currentCol + move[1];

                // בדיקה שהמיקום חוקי ונמצא בתוך גבולות הלוח
                if (isInGameBoard(bombRow, bombCol) && gameBoard[bombRow][bombCol] != null) {

                    // בדיקה אם הדיסק בתא שייך ליריב
                    if (gameBoard[bombRow][bombCol].getOwner().equals(anemy)) {

                        // אם הדיסק הוא דיסק רגיל, מוסיפים אותו לרשימה
                        if (gameBoard[bombRow][bombCol].getType().equals("⬤")) {
                            tempList.add(gameBoard[bombRow][bombCol]);
                            // אם הדיסק הוא פצצה, מוסיפים אותו לרשימת הפצצות וממשיכים בתהליך
                        } else if (gameBoard[bombRow][bombCol].getType().equals("💣")) {
                            if (!bombPossible_flip.contains(new Position(bombRow, bombCol)))
                                bombPossible_flip.add(new Position(bombRow, bombCol));
                            tempList.add(gameBoard[bombRow][bombCol]);
                        }

                    }
                }
                // אם הדיסק הוא פצצה, מוסיפים אותו לרשימת הפצצות וממשיכים בתהליך
                bombRow = currentRow - move[0];
                bombCol = currentCol - move[1];
            }
        }
        // החזרת הרשימה הזמנית של דיסקים שנמצאו
        return tempList;
    }


    private boolean isInGameBoard(int row, int col) {
        // בדיקה שהmove קורה בתוך גבולות הלוח שלנו
        return row >= 0 && row < gameBoard.length && col >= 0 && col < gameBoard[0].length;

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
        return playerTurn;
    }


    @Override
    public boolean isGameFinished() {
        // בודק אם יש עוד מהלכים שאפשר לעשות במשחק ואם אין אז מחזיר true והמשחק נגמר
        if (ValidMoves().isEmpty()) {
            int p1 = 0, p2 = 0;
            // לולאה לספירת הדיסקים של כל שחקן על גבי לוח המשחק
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    // תא ריק, אין דיסק להתחשב בו
                    if (gameBoard[i][j] == null) {
                    } else if (gameBoard[i][j].getOwner().isPlayerOne == true) p1++; // שייך לשחקן הראשון
                    else if (gameBoard[i][j].getOwner().isPlayerOne == false) p2++; // שייך לשחקן השני


                }
            }
            // עדכון הניצחון לשחקן המתאים
            if (p1 > p2) getFirstPlayer().addWin();
            if (p1 < p2) getSecondPlayer().addWin();
            // מאתחל את המשחק מחדש
            reset();
            // משתנים לשמירת המידע על השחקנים והמנצח
            String player;
            String otherplayer;
            int cp = p1, op = p2;

            // זיהוי השחקן הנוכחי והשחקן היריב
            if (isFirstPlayerTurn()) {
                player = "1";
                otherplayer = "2";
            } else {
                op = p1;
                cp = p2;
                player = "2";

                otherplayer = "1";
            }

            // הודעת הדפסה על תוצאת המשחק
            System.out.println("Player " + player + " wins with " + cp + " discs! Player " + otherplayer + "  had " + op + " discs. ");
        }
        // מחזיר true אם אין מהלכים חוקיים יותר
        return ValidMoves().isEmpty();
    }


    public void reset() {
        // מאתחל את תור השחקנים - התור מתחיל אצל השחקן הראשון
        playerTurn = true;
        // מאתחל את לוח המשחק - 8x8 עם דיסקים ריקים
        gameBoard = new Disc[8][8];
        // מאפס את הדיסקים המיוחדים של כל שחקן
        getFirstPlayer().reset_bombs_and_unflippedable();
        getSecondPlayer().reset_bombs_and_unflippedable();

        //  מיקום הדיסקים ההתחלתיים על הלוח של כל שחקן
        gameBoard[3][3] = new SimpleDisc(pl1);
        gameBoard[4][4] = new SimpleDisc(pl1);
        gameBoard[3][4] = new SimpleDisc(pl2);
        gameBoard[4][3] = new SimpleDisc(pl2);

        // מאפס את היסטוריית המשחק - משמש למעקב אחרי מהלכים קודמים
        gameHistory.clear();
        discHistory.clear();
        // מוסיף פצצה להיסטוריית הדיסקים של השחקן הראשון כהתחלה
        discHistory.push(new BombDisc(getFirstPlayer()));
        // שומר את מצב הלוח ההתחלתי בהיסטוריית הלוחות
        gameHistory.push(copy_board(gameBoard));

        // מחשב את המהלכים החוקיים לשחקן הראשון במצב ההתחלתי
        this.ValidMoves();
    }


    @Override
    public void undoLastMove() {
        System.out.println("Undoing last move:");
        // הגדרה של שני השחקנים שלנו
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
        // הופך את התור (משנה בין שחקן ראשון לשני)
        playerTurn = !playerTurn;
        // אם מדובר במהלך הראשון, מחזיר את התור לשחקן הראשון
        if (gameHistory.size() == 1)
            playerTurn = true;

        // בדיקה אם יש מספיק מהלכים כדי לבצע פעולה של Undo
        if (gameHistory.size() != 1) {
            // מחיקת המהלך האחרון מההיסטוריה כדי לחזור למהלך הקודם
            temp_GameBoard = copy_board(gameHistory.pop());
            Disc d = discHistory.pop();

            // בדיקה אם המהלך האחרון כלל דיסק מיוחד (פצצה או דיסק שלא ניתן להפוך)
            int numBombDisc = 0;
            int numUnflippableDisc = 0;
            if (d.getType() == "💣") numBombDisc = 1;
            if (d.getType() == "⭕") numUnflippableDisc = 1;
            int anemy_bom = anemy.number_of_bombs;
            int anemy_unflip = anemy.number_of_unflippedable;
            if (numBombDisc > 0 || numUnflippableDisc > 0) {

                // מאפס את ספירת הדיסקים המיוחדים של היריב
                anemy.reset_bombs_and_unflippedable();

                // מעדכן מחדש את מספר הפצצות והדיסקים שאי אפשר להפוך של היריב
                while (anemy_bom + numBombDisc != anemy.number_of_bombs) {
                    anemy.reduce_bomb();
                }
                while (anemy_unflip + numUnflippableDisc != anemy.number_of_unflippedable) {
                    anemy.reduce_unflippedable();
                }
            }
            // משחזר את מצב הלוח למהלך הקודם
            gameBoard = copy_board(gameHistory.peek());


        }
        // מזהה את הדיסק שנוסף במהלך האחרון ואת הדיסקים שהתהפכו
        Position pivotDisc = new Position(0, 0);
        List<Position> flipedDiscs = new ArrayList<>();

        for (int i = 0; i < temp_GameBoard.length; i++) {
            for (int j = 0; j < temp_GameBoard.length; j++) {
                if (gameBoard[i][j] != temp_GameBoard[i][j]) {
                    if (gameBoard[i][j] == null) pivotDisc = new Position(i, j);
                    else if (gameBoard[i][j].getOwner() != temp_GameBoard[i][j].getOwner())
                        flipedDiscs.add(new Position(i, j));
                }

            }
        }
        // אם אין מהלך לבטל אז הוא מדפיס שאין מהלכים קודמים אפשריים
        if (flipedDiscs.size() == 0) System.out.println("No previous move available to undo .");
        else {
            // מציג הודעה על הדיסק שהוסר
            System.out.println("Undo: removing " + temp_GameBoard[pivotDisc.row()][pivotDisc.col()].getType() + " from (" + pivotDisc.row() + "," + pivotDisc.col() + ") ");
            // מציג הודעה על הדיסקים שהתהפכו חזרה
            for (Position position : flipedDiscs) {
                System.out.println("Undo: flipping back " + temp_GameBoard[position.row()][position.col()].getType() + " in (" + position.row() + "," + position.col() + ") ");
            }
        }

    }


    private Disc[][] copy_board(Disc[][] Board) {
        // פונקציה שמעתיקה את לוח המשחק הנוכחי ומחזירה עותק חדש שלו

        // יצירת לוח חדש בגודל 8x8 שיכיל את העותק
        Disc[][] board = new Disc[8][8];
        // מעבר על כל השורות והעמודות בלוח המקורי
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // אם התא ריק בלוח המקורי, התא בלוח החדש נשאר ריק
                if (Board[i][j] == null) {
                    board[i][j] = null;
                    // אם התא מכיל דיסק מסוג שלא ניתן להפוך, יוצרים דיסק חדש מאותו סוג עם אותו בעלים
                } else if (Board[i][j].getType().equals("⭕")) {
                    board[i][j] = new UnflippableDisc(Board[i][j].getOwner());
                    // אם התא מכיל דיסק מסוג פצצה, יוצרים דיסק פצצה חדש עם אותו בעלים
                } else if (Board[i][j].getType().equals("💣")) {
                    board[i][j] = new BombDisc(Board[i][j].getOwner());
                    // אם התא מכיל דיסק פשוט, יוצרים דיסק פשוט חדש עם אותו בעלים
                } else if (Board[i][j].getType().equals("⬤")) {
                    board[i][j] = new SimpleDisc(Board[i][j].getOwner());
                }
            }
        }
        // מחזירים את לוח המשחק החדש שהועתק
        return board;
    }


}