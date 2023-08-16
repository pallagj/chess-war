package model

enum class PieceType {
    PAWN,
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT,
    KING;

    fun getStringIcon() : String {
        return when(this) {
            PAWN -> ""
            QUEEN -> "♕"
            ROOK -> "♖"
            BISHOP -> "♗"
            KNIGHT -> "♘"
            KING -> "♔"
        }
    }
}