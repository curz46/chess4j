package me.curz46.chess4j.test;

import me.curz46.chess4j.ChessBoard;

import static me.curz46.chess4j.Player.BLACK;
import static me.curz46.chess4j.Player.WHITE;
import static me.curz46.chess4j.util.Vector2i.*;

public class CheckmateInThree {
    public static void main(String[] args) {
        ChessBoard board = new ChessBoard();

        board.doMove(WHITE, e2, e4);
        board.doMove(BLACK, f7, f5);
        //take black pawn
        board.doMove(WHITE, e4, f5);
        board.doMove(BLACK, g7, g5);
        //checkmate
        board.doMove(WHITE, d1, h5);

        assert board.getStatus() == ChessBoard.GameStatus.WINNER_WHITE;
        System.out.println("passed");
    }
}
