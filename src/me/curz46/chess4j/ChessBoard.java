package me.curz46.chess4j;

import me.curz46.chess4j.ChessPiece.King;
import me.curz46.chess4j.ChessPiece.Pawn;
import me.curz46.chess4j.ChessPiece.Rook;
import me.curz46.chess4j.util.Tuple;
import me.curz46.chess4j.util.Vector2i;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static me.curz46.chess4j.Player.BLACK;
import static me.curz46.chess4j.Player.WHITE;

public class ChessBoard {

    private static final Set<Vector2i> boardPositions = IntStream.range(0, 8)
            .mapToObj(x -> IntStream.range(0, 8)
                    .mapToObj(y -> Vector2i.from(x, y)))
            .flatMap(Function.identity())
            .collect(Collectors.toSet());
    private final ChessState gameState;

    // allows checking of the 50-move rule
    private int fiftyMoveCounter = 0;

    public ChessBoard() {
        gameState = new ChessState();
    }

    public static Set<Vector2i> getBoardPositions() {
        return boardPositions;
    }

    public void doMove(Player player, Vector2i from, Vector2i to) {
        if(getTurn() != player) throw new RuntimeException("It is not this player's turn.");

        boolean isPawn = gameState.getPiece(from) instanceof Pawn;
        int count = (int) gameState.getPieces().stream()
                .filter(piece -> !piece.isTaken())
                .count();

        gameState.getPiece(from).doMove(gameState, to, true);
        gameState.setTurn(gameState.getTurn() == WHITE ? BLACK : WHITE);

        int newCount = (int) gameState.getPieces().stream()
                .filter(piece -> !piece.isTaken())
                .count();

        if(!isPawn && count == newCount) fiftyMoveCounter++;
        else fiftyMoveCounter = 0;
    }

    public boolean verifyMove(Vector2i from, Vector2i to) {
        return verifyMove(gameState.getPiece(from), to);
    }

    public boolean verifyMove(ChessPiece piece, Vector2i to) {
        Player player = getTurn();
        return piece != ChessPiece.NONE &&
                piece.getPlayer() == player &&
                piece.verifyMove(gameState, to, true);
    }

    public Set<Vector2i> getAllValidMoves() {
        Player player = getTurn();
        return gameState.getPieces().stream()
                .filter(piece -> piece.getPlayer() == player)
                .flatMap(piece -> getValidMoves(piece).stream())
                .collect(Collectors.toSet());
    }

    public Set<Vector2i> getValidMoves(ChessPiece piece) {
        return boardPositions.stream()
                .filter(vec -> verifyMove(piece, vec))
                .collect(Collectors.toSet());
    }

    public Set<Vector2i> getValidMoves(Vector2i from) {
        return boardPositions.stream()
                .filter(vec -> verifyMove(from, vec))
                .collect(Collectors.toSet());
    }

    public Set<Tuple<Vector2i, MoveMeta>> getValidMovesWithMeta(ChessPiece piece) {
        System.out.println("getting ValidMovesWithMeta");
        Set<Tuple<Vector2i, MoveMeta>> collect = getValidMoves(piece).stream()
                .map(vec -> {
                    MoveMeta meta;
                    // determine if this move takes an enemy piece
                    Player enemy = piece.getPlayer() == WHITE ? BLACK : WHITE;
                    ChessPiece pieceAtPosition = gameState.getPiece(vec);
                    if(pieceAtPosition.getPlayer() == enemy) {
                        meta = MoveMeta.ATTACKS;
                    } else {
                        // determine if this move is 'special':
                        if(piece.getPlayer() == pieceAtPosition.getPlayer()
                                && (pieceAtPosition instanceof King && piece instanceof Rook
                                || pieceAtPosition instanceof Rook && piece instanceof King)) {
                            // 1. castling
                            meta = MoveMeta.SPECIAL;
                        } else if(pieceAtPosition == ChessPiece.NONE
                                && piece instanceof Pawn
                                && vec.getY() == (enemy == WHITE ? 7 : 0)) {
                            // 2. pawn promotion
                            meta = MoveMeta.SPECIAL;
                        } else if(piece instanceof Pawn
                                && abs(piece.getTransform(vec).getX()) == 1
                                && pieceAtPosition == ChessPiece.NONE) {
                            Vector2i enemyPosition = enemy == WHITE
                                    ? vec.add(0, 1)
                                    : vec.sub(0, 1);
                            ChessPiece enemyPiece = gameState.getPiece(enemyPosition);
                            if(enemyPiece.getPlayer() == enemy
                                    && enemyPiece instanceof Pawn
                                    && ((Pawn) enemyPiece).hasJustMovedTwo()) {
                                // 3. en passant
                                meta = MoveMeta.SPECIAL;
                            } else {
                                meta = MoveMeta.DEFAULT;
                            }
                        } else {
                            meta = MoveMeta.DEFAULT;
                        }
                    }
                    return Tuple.of(vec, meta);
                })
                .collect(Collectors.toSet());
        System.out.println("done!");
        return collect;
    }

    public Set<Tuple<Vector2i, MoveMeta>> getValidMovesWithMeta(Vector2i from) {
        return getValidMovesWithMeta(gameState.getPiece(from));
    }

    public Player getTurn() {
        return gameState.getTurn();
    }

    public GameStatus getStatus() {
        if(isStalemate()) return GameStatus.STALEMATE;
        if(inCheck()) {
            if(getAllValidMoves().isEmpty()) {
                return gameState.getTurn() == WHITE ? GameStatus.WINNER_BLACK : GameStatus.WINNER_WHITE;
            }
            return GameStatus.CHECK;
        }
        return GameStatus.PLAYING;
    }

    public ChessState getState() {
        return gameState;
    }

    public boolean canCallDraw() {
        return fiftyMoveCounter >= 100;
    }

    private boolean isStalemate() {
        return !inCheck() && getAllValidMoves().isEmpty();
    }

    private boolean inCheck() {
        Player player = getTurn();
        Optional<ChessPiece> king = gameState.getPieces(King.class).stream()
                .filter(piece -> piece.getPlayer() == player)
                .findAny();
        if(!king.isPresent()) throw new RuntimeException("Somehow, this player doesn't have a King.");
        return gameState.getPieces().stream()
                .filter(piece -> piece.getPlayer() != player)
                .anyMatch(piece -> piece.verifyMove(gameState, king.get().getPosition(), true));
    }

    public enum GameStatus {

        PLAYING,
        CHECK,
        STALEMATE,
        WINNER_WHITE,
        WINNER_BLACK

    }

    public enum MoveMeta {

        DEFAULT,
        ATTACKS,
        SPECIAL

    }

}
