package model

enum class PieceColor {
    WHITE,
    BLACK;

    fun getDirection() : Int {
        return if (this == WHITE) +1 else -1
    }
}