package model

import kotlin.math.abs

fun pawnYStep(color: PieceColor): Int {
    return if (color == PieceColor.WHITE) +1 else -1
}

//Own exception class when pawn change is not specified:
class PawnChangeNotSpecifiedException(message: String) : Exception(message)


class Pawn(
    color: PieceColor, position: Pos, board: Board
) : Piece(
    letter = "P",
    color = color,
    type = PieceType.PAWN,
    position = position,
    initMove = MoveRule(steps = arrayOf(Pos(0, pawnYStep(color))), maxRepeat = 2),
    move = MoveRule(steps = arrayOf(Pos(0, pawnYStep(color)))),
    hit = MoveRule(steps = arrayOf(Pos(-1, pawnYStep(color)), Pos(1, pawnYStep(color)))),
    board = board
) {
    override fun knockOutPositions(piece: Piece): List<Pos> {
        if(piece.type != PieceType.PAWN)
            return super.knockOutPositions(piece)

        val (piece, from, to) = board.history.getLastHistory(this.color)
            ?: return listOf(position)

        if (piece == this && abs(from.y - to.y) == 2) {
            return listOf(from + Pos(0, pawnYStep(color)), to)
        }

        return listOf(position)
    }

    override fun step(command: String) {
        val change = if (command.length > 4) command.substring(4, 5).toInt() else null
        val history = board.history.clone()

        super.step(command)

        if (abs(position.y - initPosition.y) == 6) {
            if(change == null) {
                board.history.undo()
                board.history = history
                throw PawnChangeNotSpecifiedException("Pawn change not specified")
            }

            board.pieces = board.pieces.filter { it != this }

            when (change) {
                1 -> board.pieces += Queen(color, position, board)
                2 -> board.pieces += Rook(color, position, board)
                3 -> board.pieces += Bishop(color, position, board)
                4 -> board.pieces += Knight(color, position, board)
                else -> throw PawnChangeNotSpecifiedException("Pawn change not specified")
            }

            board.history.addChange(Pair(this, board.pieces.last()))
        }
    }
}

class Knight(
    color: PieceColor, position: Pos, board: Board
) : Piece(
    letter = "N", color = color, type = PieceType.KNIGHT, position = position, move = MoveRule(
        steps = arrayOf(
            Pos(-1, 2), Pos(1, 2), Pos(-1, -2), Pos(1, -2), Pos(-2, 1), Pos(2, 1), Pos(-2, -1), Pos(2, -1)
        )
    ), board = board
)

class Bishop(
    color: PieceColor, position: Pos, board: Board
) : Piece(
    letter = "B", color = color, type = PieceType.BISHOP, position = position, move = MoveRule(
        steps = arrayOf(
            Pos(-1, 1), Pos(1, 1), Pos(-1, -1), Pos(1, -1)
        ), maxRepeat = 8
    ), board = board
)

class Rook(
    color: PieceColor, position: Pos, board: Board
) : Piece(
    letter = "R", color = color, type = PieceType.ROOK, position = position, move = MoveRule(
        steps = arrayOf(
            Pos(0, 1), Pos(0, -1), Pos(1, 0), Pos(-1, 0)
        ), maxRepeat = 8
    ), board = board
)

class Queen(
    color: PieceColor, position: Pos, board: Board
) : Piece(
    letter = "Q", color = color, type = PieceType.QUEEN, position = position, move = MoveRule(
        steps = arrayOf(
            Pos(0, 1), Pos(0, -1), Pos(1, 0), Pos(-1, 0), Pos(-1, 1), Pos(1, 1), Pos(-1, -1), Pos(1, -1)
        ), maxRepeat = 8
    ), board = board
)

class King(
    color: PieceColor, position: Pos, board: Board
) : Piece(
    letter = "K", color = color, type = PieceType.KING, position = position, move = MoveRule(
        steps = arrayOf(
            Pos(0, 1), Pos(0, -1), Pos(1, 0), Pos(-1, 0), Pos(-1, 1), Pos(1, 1), Pos(-1, -1), Pos(1, -1)
        )
    ), board = board, knockable = false
) {
    override fun getPossibleMoves(): Set<Pos> {
        val specialMoves = mutableListOf<Pos>()

        if (isMoved()  || attackedAtPosition()) return super.getPossibleMoves()

        board.pieces.filter { it.color == this.color }.filter { it.letter == "R" && it.position.y == this.initPosition.y}.filter { !it.isMoved() }
            .forEach { rook ->
                val kPos = this.position.x
                val rPos = rook.position.x

                val dist = abs(rPos - kPos)
                val kToR = (rPos - kPos) / dist

                if (attackedAtPosition(Pos(kPos + 1 * kToR, this.position.y))) return@forEach
                if (attackedAtPosition(Pos(kPos + 2 * kToR, this.position.y))) return@forEach

                for (i in 1 until dist) {
                    if (board.isPieceAt(Pos(kPos + i * kToR, this.position.y))) return@forEach
                }

                specialMoves += Pos(kPos + kToR * 2, this.position.y)
            }

        return listOf(super.getPossibleMoves(), specialMoves).flatten().toSet()
    }

    override fun step(command: String) {
        val from = Pos(command.substring(0, 2))
        val to = Pos(command.substring(2, 4))

        if(from != position) return;

        super.step(command)

        if (from.x - to.x == 2) {
            val rook = board.pieces.first { it.color == this.color && it.letter == "R" && it.position.x == 1  && it.position.y == this.initPosition.y && !it.isMoved()}
            rook.hardStep(Pos(4, rook.position.y), true)
        } else if (from.x - to.x == -2) {
            val rook = board.pieces.first { it.color == this.color && it.letter == "R" && it.position.x == 8  && it.position.y == this.initPosition.y && !it.isMoved()}
            rook.hardStep(Pos(6, rook.position.y), true)
        }
    }
}