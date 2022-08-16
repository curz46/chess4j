package me.curz46.chess4j;

import me.curz46.chess4j.util.Vector2i;

import java.util.Comparator;
import java.util.Optional;

import static java.lang.Math.abs;
import static me.curz46.chess4j.Player.BLACK;
import static me.curz46.chess4j.Player.WHITE;

public abstract class ChessPiece {

    public static final Empty NONE = new Empty(Player.NONE, null, false);

    protected Player player;
    protected Vector2i position;

    protected boolean moved;
    protected boolean taken;

    private PieceStatus status = PieceStatus.DEFAULT;

    public ChessPiece(Player player, Vector2i position) {
        this.player = player;
    }

    public ChessPiece(Player player, Vector2i position, boolean moved) {
        this.player = player;
        this.position = position;
    }

    public Player getPlayer() {
        return player;
    }

    public Vector2i getPosition() {
        return position;
    }

    public void setPosition(Vector2i newPosition) {
        position = newPosition;
    }

    public Vector2i getTransform(Vector2i to) {
        return to.sub(position);
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public abstract ChessPiece copy();

    public void doMove(ChessState state, Vector2i to, boolean notify) {
        moved = true;

        ChessPiece targetPiece = state.getPiece(to);
        if(targetPiece != NONE) {
            state.takePiece(targetPiece);
        }
        position = to;

        // update status of each ChessPiece whenever piece is moved
        if(notify)
            state.getPieces().forEach(piece -> piece.update(state));
    }

    public boolean verifyMove(ChessState state, Vector2i to, boolean determineCheck) {
        if(player == Player.NONE) return false; // wtf

        // a. check within bounds
        if(0 > to.getX() || to.getX() >= 8 || 0 > to.getY() || to.getY() >= 8) return false;

        // b. check that piece to move to isn't ally
        if(state.getPiece(to).getPlayer() == player) return false;

        if(!determineCheck) return true;
        // c. check that further moves by enemy pieces couldn't result in the loss of the King
        ChessState copiedState = state.copy();
        // apply move on duplicated, independent state
        copiedState.getPiece(position).doMove(copiedState, to, false);
        // determine whether or not an enemy piece could take the ally King in this state
        Optional<ChessPiece> king = copiedState.getPieces(King.class).stream()
                .filter(piece -> piece.getPlayer() == player)
                .findAny();
        boolean check = king.isPresent() && copiedState.getPieces().stream()
                .filter(piece -> piece.getPlayer() != player)
                .anyMatch(piece -> piece.verifyMove(copiedState, king.get().getPosition(), false));

        return !check;
    }

    public void update(ChessState state) {
        boolean attacked = state.getPieces().stream()
                .filter(piece -> piece.getPlayer() != player)
                .anyMatch(piece -> piece.verifyMove(state, getPosition(), true));
        status = attacked ? PieceStatus.ATTACKED : PieceStatus.DEFAULT;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public boolean hasMoved() {
        return moved;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public PieceStatus getStatus() {
        return status;
    }

    // determines if this move would collide with an ally or enemy piece should it travel in a straight line to the
    // destination
    protected boolean checkCollisions(ChessState state, Vector2i to) {
        Vector2i t = getTransform(to);
        if(abs(t.getX()) != abs(t.getY()) &&
                (abs(t.getX()) <= 0 || t.getY() != 0) &&
                (abs(t.getY()) <= 0 || t.getX() != 0)) {
            // this is not a transform vector such that it can be easily represented as (x, y)+a(dx, dy), where (x, y)
            // is current position and all unknowns are integers
            return true;
        }

        // should be -1, 0, 1
        int dx = t.getX() != 0 ? (t.getX() / abs(t.getX())) : 0;
        int dy = t.getY() != 0 ? (t.getY() / abs(t.getY())) : 0;
        int currentX = dx;
        int currentY = dy;

        while(to.getX() != (position.getX() + currentX) ||
                to.getY() != (position.getY() + currentY)) {
            ChessPiece piece = state.getPiece(Vector2i.from(
                    position.getX() + currentX,
                    position.getY() + currentY
            ));
            if(piece.getPlayer() != Player.NONE) return true;
            currentX += dx;
            currentY += dy;
        }
        return false;
    }

    public enum PieceStatus {

        DEFAULT,
        ATTACKED

    }

    public final static class Empty extends ChessPiece {

        public Empty(Player player, Vector2i initialPosition, boolean moved) {
            super(player, initialPosition, moved);
        }

        @Override
        public Empty copy() {
            throw new RuntimeException("Cannot copy an instance of ChessPiece.Empty!");
        }

    }

    public static final class Pawn extends ChessPiece {

        private boolean justMovedTwo;

        public Pawn(Player player, Vector2i initialPosition, boolean moved) {
            super(player, initialPosition, moved);
        }

        @Override
        public Pawn copy() {
            return new Pawn(player, position, moved);
        }

        @Override
        public void doMove(ChessState state, Vector2i to, boolean notify) {
            Vector2i t = getTransform(to);
            ChessPiece previousPiece = state.getPiece(to);

            super.doMove(state, to, notify);

            if(abs(t.getY()) == 2) {
                justMovedTwo = true;
            } else {
                justMovedTwo = false;
            }

            // PAWN PROMOTION
            if(position.getY() == (player == WHITE ? 7 : 0)) {
                state.setPieceAwaitsPromotion(this);
            }

            // EN PASSANT
            Vector2i enemyPiecePosition = player == WHITE
                    ? to.sub(0, 1)
                    : to.add(0, 1);
            ChessPiece enemyPiece = state.getPiece(enemyPiecePosition);
            if(abs(t.getX()) == 1
                    && abs(t.getY()) == 1
                    && previousPiece == ChessPiece.NONE
                    && enemyPiece.getPlayer() == (player == WHITE ? BLACK : WHITE)
                    && enemyPiece instanceof Pawn
                    && ((Pawn) enemyPiece).hasJustMovedTwo()) {
                // en passant, therefore take the piece
                state.takePiece(enemyPiece);
            }
        }

        @Override
        public boolean verifyMove(ChessState state, Vector2i to, boolean determineCheck) {
            if(!super.verifyMove(state, to, determineCheck)) return false;

            Vector2i transform = getTransform(to);
            // may only travel forward for WHITE, backwards for BLACK
            boolean validTransform = player == WHITE
                    ? transform.getY() == 1
                    : transform.getY() == -1;
            boolean validTransformFirstMove = player == WHITE
                    ? transform.getY() == 2
                    : transform.getY() == -2;
            if(!validTransform && (moved || !validTransformFirstMove)) return false;

            // confirm that it's the correct state to take a piece if necessary
            return (state.getPiece(to) == ChessPiece.NONE
                    ? transform.getX() == 0
                    : abs(transform.getX()) == 1) &&
                    !checkCollisions(state, to);
        }

        @Override
        public void update(ChessState state) {
            super.update(state);
            justMovedTwo = false;
        }

        public boolean hasJustMovedTwo() {
            return justMovedTwo;
        }

    }

    public static final class Rook extends ChessPiece {

        public Rook(Player player, Vector2i initialPosition, boolean moved) {
            super(player, initialPosition, moved);
        }

        @Override
        public Rook copy() {
            return new Rook(player, position, moved);
        }

        @Override
        public boolean verifyMove(ChessState state, Vector2i to, boolean determineCheck) {
            Vector2i t = getTransform(to);

//            final ChessPiece pieceAtTo = state.getPiece(to);
//            final int boardStart = this.player == WHITE ? 0 : 7;
//            if (this.position.getY() == boardStart
//                && pieceAtTo.getPosition().getY() == boardStart
//                && pieceAtTo instanceof King
//                && t.getY() == 0
//                && abs(t.getX()) == 2)

            return (super.verifyMove(state, to, determineCheck) &&
                    ((abs(t.getX()) >= 1 && t.getY() == 0) ||
                            (abs(t.getY()) >= 1 && t.getX() == 0))) &&
                    !checkCollisions(state, to);
        }

    }

    public static final class Knight extends ChessPiece {

        public Knight(Player player, Vector2i initialPosition, boolean moved) {
            super(player, initialPosition, moved);
        }

        @Override
        public Knight copy() {
            return new Knight(player, position, moved);
        }

        @Override
        public boolean verifyMove(ChessState state, Vector2i to, boolean determineCheck) {
            Vector2i t = getTransform(to);
            return super.verifyMove(state, to, determineCheck) &&
                    ((abs(t.getX()) == 2 && abs(t.getY()) == 1) ||
                            (abs(t.getY()) == 2 && abs(t.getX()) == 1));
        }

    }

    public static final class Bishop extends ChessPiece {

        public Bishop(Player player, Vector2i initialPosition, boolean moved) {
            super(player, initialPosition, moved);
        }

        @Override
        public Bishop copy() {
            return new Bishop(player, position, moved);
        }

        @Override
        public boolean verifyMove(ChessState state, Vector2i to, boolean determineCheck) {
            Vector2i t = getTransform(to);
            return (super.verifyMove(state, to, determineCheck) &&
                    abs(t.getX()) == abs(t.getY())) &&
                    !checkCollisions(state, to);
        }

    }

    public static final class Queen extends ChessPiece {

        public Queen(Player player, Vector2i initialPosition, boolean moved) {
            super(player, initialPosition, moved);
        }

        @Override
        public Queen copy() {
            return new Queen(player, position, moved);
        }

        @Override
        public boolean verifyMove(ChessState state, Vector2i to, boolean determineCheck) {
            Vector2i t = getTransform(to);

            return (super.verifyMove(state, to, determineCheck) &&
                    ((abs(t.getX()) >= 1 && t.getY() == 0) ||
                            (abs(t.getY()) >= 1 && t.getX() == 0) ||
                            (abs(t.getX()) == abs(t.getY())))) &&
                    !checkCollisions(state, to);
        }

    }

    public static final class King extends ChessPiece {

        public King(Player player, Vector2i initialPosition, boolean moved) {
            super(player, initialPosition, moved);
        }

        @Override
        public King copy() {
            return new King(player, position, moved);
        }

        @Override
        public void doMove(ChessState state, Vector2i to, boolean notify) {
            Vector2i t = getTransform(to);
            super.doMove(state, to, notify);

            if(abs(t.getX()) != 2 || t.getY() != 0) return;
            getClosestAlly(state, to, Rook.class).ifPresent(rook -> {
                // sign of X-difference to determine where to move Rook
                Vector2i delta = position.sub(rook.getPosition());
                int sign = delta.getX() / abs(delta.getX());
                Vector2i newPosition = position.add(sign, 0);
                rook.setPosition(newPosition);
                rook.setMoved(true);
            });
        }

        @Override
        public boolean verifyMove(ChessState state, Vector2i to, boolean determineCheck) {
            boolean validCastle = verifyCastling(state, to);
            if(validCastle) return true;

            Vector2i t = getTransform(to);
            return super.verifyMove(state, to, determineCheck) &&
                    // check that has only moved a maximum magnitude of sqrt(2)
                    abs(t.getX()) <= 1 && abs(t.getY()) <= 1;
        }

        private boolean verifyCastling(ChessState state, Vector2i to) {
            // CASTLING:
            // - King cannot have moved
            // - Rook cannot have moved
            // - King cannot be *in* check
            // - King cannot move to *be* in check
            // - any position between current and
            // - King must move 2 spaces
            // - Rook moves to other side of King
            Vector2i t = getTransform(to);
            if(moved) return false;
            if(abs(t.getX()) != 2 || t.getY() != 0) return false;

            // get closest Rook
            ChessPiece rook = getClosestAlly(state, to, Rook.class).orElse(null);
            if(rook == null) return false;
            if(rook.hasMoved()) return false;

            // check all squares between are empty
            boolean piecesBetween = ChessBoard.getBoardPositions().stream()
                    .filter(vec -> position.getX() < vec.getX() && vec.getX() < rook.getPosition().getX() ||
                            rook.getPosition().getX() < vec.getX() && vec.getX() < position.getX())
                    .anyMatch(vec -> state.getPiece(vec) != ChessPiece.NONE);
            if(piecesBetween) return false;

            // check squares that King will transverse are not in check
            boolean piecesInCheck = ChessBoard.getBoardPositions().stream()
                    .filter(vec -> position.getX() < vec.getX() && vec.getX() < to.getX() ||
                            to.getX() < vec.getX() && vec.getX() < position.getX())
                    .anyMatch(vec -> state.getPieces().stream()
                            .anyMatch(piece -> piece.verifyMove(state, vec, true)));
            if(piecesInCheck) return false;

            // castling possible
            return true;
        }

        private <T extends ChessPiece> Optional<ChessPiece> getClosestAlly(ChessState state,
                                                                           Vector2i position,
                                                                           Class<T> clazz) {
            return state.getPieces(clazz).stream()
                    .filter(piece -> piece.getPlayer() == player)
                    .sorted(Comparator.comparingDouble(piece -> piece.getPosition().distance(position)))
                    .findFirst();
        }

    }

}
