public class GameBoardClass {
    public GameBoardClass(Disc[][] G,int u,int b){
        player_UnflippableDisc=u;
        player_BombDisc=b;
        GameBoard=copy_board(G);
    }
    public GameBoardClass(Disc[][] G){
        player_UnflippableDisc=0;
        player_BombDisc=0;
        GameBoard=copy_board(G);
    }
    public Disc[][] GameBoard = new Disc[8][8];
    public int  player_BombDisc=0;
    public int  player_UnflippableDisc=0;
    public int get_num_of_BombDisc(){
        return player_BombDisc;
    }
    public int addfBombDisc(){
        return player_BombDisc++;
    }
    public int get_num_of_UnflippableDisc(){
        return player_UnflippableDisc;
    }
    public int addfUnflippableDisc(){
        return player_BombDisc++;
    }
    public Disc[][] copy_board(Disc[][] Board) {
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
