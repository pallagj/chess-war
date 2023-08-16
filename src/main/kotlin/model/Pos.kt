package model
data class Pos (
    var x: Int,
    var y: Int
){
    constructor(pos: String) : this(pos.toInt() / 10, pos.toInt() % 10)
    operator fun plus(other: Pos): Pos {
        return Pos(this.x + other.x, this.y + other.y)
    }

    operator fun minus(other: Pos): Pos {
        return Pos(this.x - other.x, this.y - other.y)
    }

    operator fun times(other: Int): Pos {
        return Pos(this.x * other, this.y * other)
    }

    operator fun div(other: Int): Pos {
        return Pos(this.x / other, this.y / other)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Pos) {
            return this.x == other.x && this.y == other.y
        }
        return false
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    fun getChessNotation(): String {
        return "${'a' + this.x - 1}${this.y}"
    }
    override fun toString(): String {
        return "${this.x}${this.y}"
    }

}