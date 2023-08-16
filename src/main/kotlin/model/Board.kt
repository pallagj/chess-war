package model

import java.util.*



class Board {
    var pieces: List<Piece> = listOf()
    var history: BoardHistory = BoardHistory(this)


    init {
        val boardMap = listOf(
            listOf("R", "N", "B", "Q", "K", "B", "N", "R"),
            listOf("P", "P", "P", "P", "P", "P", "P", "P"),
            listOf(" ", " ", " ", " ", " ", " ", " ", " "),
            listOf(" ", " ", " ", " ", " ", " ", " ", " "),
            listOf(" ", " ", " ", " ", " ", " ", " ", " "),
            listOf(" ", " ", " ", " ", " ", " ", " ", " "),
            listOf("p", "p", "p", "p", "p", "p", "p", "p"),
            listOf("r", "n", "b", "q", "k", "b", "n", "r")
        )

        this.pieces = boardMap.mapIndexed { y, row ->
            row.mapIndexed { x, letter ->
                when (letter) {
                    "P" -> Pawn(PieceColor.WHITE, Pos(x + 1, y + 1), this)
                    "R" -> Rook(PieceColor.WHITE, Pos(x + 1, y + 1), this)
                    "N" -> Knight(PieceColor.WHITE, Pos(x + 1, y + 1), this)
                    "B" -> Bishop(PieceColor.WHITE, Pos(x + 1, y + 1), this)
                    "Q" -> Queen(PieceColor.WHITE, Pos(x + 1, y + 1), this)
                    "K" -> King(PieceColor.WHITE, Pos(x + 1, y + 1), this)

                    "p" -> Pawn(PieceColor.BLACK, Pos(x + 1, y + 1), this)
                    "r" -> Rook(PieceColor.BLACK, Pos(x + 1, y + 1), this)
                    "n" -> Knight(PieceColor.BLACK, Pos(x + 1, y + 1), this)
                    "b" -> Bishop(PieceColor.BLACK, Pos(x + 1, y + 1), this)
                    "q" -> Queen(PieceColor.BLACK, Pos(x + 1, y + 1), this)
                    "k" -> King(PieceColor.BLACK, Pos(x + 1, y + 1), this)
                    else -> null
                }
            }
        }.flatten().filterNotNull()
    }

    fun canKnockOut(move: Pos, attacker: Piece): Boolean {
        return this.pieces.any { it.color != attacker.color && it.knockOutPositions(attacker).contains(move) }
    }

    fun knockOut(at: Pos, attacker: Piece) : Piece? {
        val piece = this.pieces.find { it.color != attacker.color && it.knockOutPositions(attacker).contains(at) }
        this.pieces = this.pieces.filter { it != piece}
        return piece
    }

    fun at(position: Pos): Piece? {
        return this.pieces.find { it.position == position }
    }

    fun isEmptyAt(position: Pos): Boolean {
        return this.at(position) == null
    }

    fun isPieceAt(position: Pos): Boolean {
        return this.at(position) != null
    }

    fun finished(): Boolean {
        return pieces.filter { it.color == PieceColor.WHITE }.sumOf { it.getPossibleMoves().size } == 0
                || pieces.filter { it.color == PieceColor.BLACK }.sumOf { it.getPossibleMoves().size } == 0
    }

    fun step(command: String) {
        pieces.filter { it.color == history.nextPlayer() }.forEach { it.step(command) }
    }

    fun isInBoard(move: Pos): Boolean {
        return move.x in 1..8 && move.y in 1..8
    }

    fun getResult(): String {
        if(pieces.filter{it.type == PieceType.KING} .all {!it.attackedAtPosition() })
            return "Draw!"
        else if(pieces.filter { it.color == PieceColor.WHITE }.sumOf { it.getPossibleMoves().size } == 0)
            return "Black wins!"
        else if(pieces.filter { it.color == PieceColor.BLACK }.sumOf { it.getPossibleMoves().size } == 0)
            return "White wins!"
        return ""
    }

    companion object {
        val board = Board()
    }
}
