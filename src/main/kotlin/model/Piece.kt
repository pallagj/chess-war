package model

class MoveRule(
    var steps: Array<Pos>, var maxRepeat: Int = 1
)


abstract class Piece(
    var letter: String, var color: PieceColor, var type: PieceType, var position: Pos, val initPosition: Pos = position,

    var move: MoveRule, var initMove: MoveRule = move, var hit: MoveRule = move, var canKnockOut: Boolean = true,

    var board: Board
) {
    private fun getMovesFromRule(rule: MoveRule, knock: Boolean, knockOutFilter: Boolean = true): Set<Pos> {
        val moves = mutableSetOf<Pos>()

        val steps = rule.steps
        val maxRepeat = rule.maxRepeat

        for (step in steps) {
            for (i in 1..maxRepeat) {
                val move = position + step * i

                if (!board.isInBoard(move)) break
                if (!knock && board.isPieceAt(move)) break
                if (board.at(move)?.color == this.color) break
                if (knock && !board.canKnockOut(move, this) && i == maxRepeat) break

                moves += move

                if (board.isPieceAt(move)) break
            }
        }

        if (!knockOutFilter) // TODO : not knocking is wrong
            return moves

        return moves.filter { move ->
            board.pieces.filter { it.color == this.color && !it.canKnockOut }.all {
                val history = board.history.clone()
                hardStep(move, false)
                val output = !it.attackedAtPosition()
                board.history.undo()
                board.history = history
                output
            }
        }.toSet()
    }


    private fun possibleMoves(knockOutFilter: Boolean = true): Set<Pos> {
        return setOf(
            if (!this.isMoved()) getMovesFromRule(this.initMove, false, knockOutFilter)
            else getMovesFromRule(this.move, false, knockOutFilter),
            getMovesFromRule(this.hit, true, knockOutFilter),
        ).flatten().toSet()
    }

    fun isMoved(): Boolean {
        return board.history.isMoved(this)
    }

    open fun getPossibleMoves(): Set<Pos> {
        return this.possibleMoves()
    }

    open fun knockOutPositions(piece: Piece): List<Pos> {
        return listOf(position)
    }

    open fun step(command: String) {
        val from = Pos(command.substring(0, 2))
        val to = Pos(command.substring(2, 4))

        if (from != position) return

        if (this.getPossibleMoves().contains(to)) {
            hardStep(to)
        } else {
            throw Exception("Invalid move")
        }
    }

    fun hardStep(to: Pos, additionalStep: Boolean = false) {
        val hit = board.knockOut(to, this)
        board.history.add(this, position, to, hit, additionalStep)
        position = to
    }

    fun attackedAtPosition(position: Pos = this.position): Boolean {
        if (position != this.position) {
            val history = board.history.clone()
            hardStep(position)

            val output = attackedAtPosition()

            board.history.undo()
            board.history = history

            return output
        }

        return board.pieces
            .filter { it.color != this.color }
            .any { it.possibleMoves(false).contains(position) }
    }

}