package me.curz46.chess4j;

import me.curz46.chess4j.ChessPiece.*;
import me.curz46.chess4j.util.Vector2i;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static me.curz46.chess4j.Player.BLACK;
import static me.curz46.chess4j.Player.WHITE;

public class ChessState {

    private Set<ChessPiece> pieces;
    private Player turn = WHITE;

    private ChessPiece pieceAwaitsPromotion;

    public ChessState(Set<ChessPiece> pieces) {
        this.pieces = pieces;
    }

    public ChessState() {
        this(ChessState.generatePieces());
    }

    public ChessState copy() {
        return new ChessState(
                pieces.stream()
                        .map(ChessPiece::copy)
                        .collect(Collectors.toSet())
        );
    }

    public ChessPiece getEmptyPiece() {
        return ChessPiece.NONE;
    }

    public ChessPiece takePiece(Vector2i position) {
        ChessPiece piece = getPiece(position);
        takePiece(piece);
        return piece;
    }

    public void setPieceAwaitsPromotion(ChessPiece piece) {
        pieceAwaitsPromotion = piece;
    }

    public void replacePiece(ChessPiece piece, ChessPiece newPiece) {
        if(piece.getPosition() != newPiece.getPosition()) {
            // not necessary but good check for original purpose - pawn promotion
            throw new RuntimeException("Replacing a ChessPiece requires the new piece to have the same position.");
        }

        pieces.remove(piece);
        pieces.add(newPiece);
    }

    public void takePiece(ChessPiece piece) {
        if(piece == getEmptyPiece() || !pieces.contains(piece))
            throw new RuntimeException("Attempted to take an invalid ChessPiece. (NONE|!contains)");
        pieces.remove(piece);
        piece.setTaken(true);
    }

    public ChessPiece getPiece(Vector2i position) {
        return pieces.stream()
                .filter(p -> p.getPosition().equals(position))
                .findFirst()
                .orElse(ChessPiece.NONE);
    }

    public Set<ChessPiece> getPieces() {
        return Collections.unmodifiableSet(pieces);
    }

    public Set<ChessPiece> getPieces(Class<? extends ChessPiece> type) {
        return pieces.stream()
                .filter(p -> p.getClass().equals(type))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<ChessPiece> generatePieces() {
        Set<ChessPiece> pieces = new HashSet<>();
        pieces.addAll(ChessState.generatePieces(WHITE, 0));
        pieces.addAll(ChessState.generatePieces(BLACK, 7));
        return pieces;
    }

    private static Set<ChessPiece> generatePieces(Player actor, int y) {
        Set<ChessPiece> pieces = new HashSet<>();

        pieces.add(new Rook(actor, Vector2i.from(0, y), false));
        pieces.add(new Knight(actor, Vector2i.from(1, y), false));
        pieces.add(new Bishop(actor, Vector2i.from(2, y), false));

        pieces.add(new Queen(actor, Vector2i.from(3, y), false));
        pieces.add(new King(actor, Vector2i.from(4, y), false));

        pieces.add(new Bishop(actor, Vector2i.from(5, y), false));
        pieces.add(new Knight(actor, Vector2i.from(6, y), false));
        pieces.add(new Rook(actor, Vector2i.from(7, y), false));

        for(int x = 0; x < 8; x++) {
            pieces.add(new Pawn(actor, Vector2i.from(x, y + (actor == WHITE ? 1 : -1)), false));
        }

        return pieces;
    }

    public Optional<ChessPiece> getPieceAwaitsPromotion() {
        return Optional.ofNullable(pieceAwaitsPromotion);
    }

    public void setTurn(Player player) {
        turn = player;
    }

    public Player getTurn() {
        return turn;
    }

}
