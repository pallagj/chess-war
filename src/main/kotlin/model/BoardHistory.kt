package model

data class StepHistory(
    var piece: Piece, var from: Pos, var to: Pos, var hit: Piece?,  var additionalStep: StepHistory? = null, var change:Pair<Piece,Piece>? = null
)

class BoardHistory(
    private var board: Board,
    private var stepHistory: List<StepHistory> = listOf(),
    var historyIndex: Int = -1,
    var historyIndexBefore: Int = -1
) {
    fun add(piece: Piece, from: Pos, to: Pos, hit: Piece?, additionalStep: Boolean = false) {
        if (additionalStep) {
            stepHistory[historyIndex].additionalStep = StepHistory(piece, from, to, hit)
            return
        }

        this.stepHistory = this.stepHistory.subList(0, this.historyIndex + 1)
        this.stepHistory = this.stepHistory.plus(StepHistory(piece, from, to, hit))

        this.historyIndexBefore = this.historyIndex
        this.historyIndex++
    }

    fun undo(dropFuture: Boolean = false) {
        if (historyIndex < 0) {
            throw Exception("No history to undo")
        }

        var lastStep: StepHistory? = this.stepHistory[this.historyIndex]

        //Change
        if (lastStep!!.change != null) {
            board.pieces -= lastStep.change!!.second
            board.pieces += lastStep.change!!.first
        }

        //Move
        while (lastStep != null) {
            lastStep.piece.position = lastStep.from

            if (lastStep.hit != null) {
                board.pieces += lastStep.hit!!
            }

            lastStep = lastStep.additionalStep
        }

        this.historyIndexBefore = this.historyIndex
        this.historyIndex--

        /*if(dropFuture) {
            this.stepHistory = this.stepHistory.subList(0, this.historyIndex + 1)
            this.historyIndexBefore = this.historyIndex
        }*/
    }

    fun redo() {
        if (this.historyIndex == this.stepHistory.size - 1) {
            throw Exception("No history to redo")
        }

        this.historyIndexBefore = this.historyIndex
        this.historyIndex++

        var lastStep : StepHistory? = this.stepHistory[this.historyIndex]

        if (lastStep!!.change != null) {
            board.pieces += lastStep.change!!.second
            board.pieces -= lastStep.change!!.first
        }

        while (lastStep != null) {
            lastStep.piece.position = lastStep.to

            if (lastStep.hit != null) {
                board.pieces = board.pieces.filter { it != lastStep!!.hit }
            }

            lastStep = lastStep.additionalStep
        }



    }

    fun goToIndex(targetIndex: Int) {
        if (targetIndex < -1 || targetIndex > this.stepHistory.size - 1) {
            throw Exception("Invalid history index")
        }

        val currentIndex = this.historyIndex

        if (targetIndex < this.historyIndex) {
            while (targetIndex < this.historyIndex) {
                this.undo()
            }
        } else {
            while (this.historyIndex < targetIndex) {
                this.redo()
            }
        }

        this.historyIndexBefore = currentIndex
    }

    fun lastChange(): Map<Piece, Pair<Pos?, Pos?>> {
        val changes = HashMap<Piece, Pair<Pos?, Pos?>>()

        val historyIndex = this.historyIndex
        val historyIndexBefore = this.historyIndexBefore

        goToIndex(historyIndexBefore)

        board.pieces.forEach {
            changes[it] = Pair(it.position, null)
        }

        goToIndex(historyIndex)

        board.pieces.forEach {
            if (changes.containsKey(it)) {
                changes[it] = Pair(changes[it]!!.first, it.position)
            } else {
                changes[it] = Pair(null, it.position)
            }
        }

        this.historyIndexBefore = historyIndexBefore

        return changes
    }

    fun nextPlayer(): PieceColor {
        if (this.historyIndex == -1) return PieceColor.WHITE

        return if (this.historyIndex % 2 == 0) PieceColor.BLACK else PieceColor.WHITE
    }

    fun getLastHistory(color: PieceColor): StepHistory? {
        if (stepHistory.subList(0, historyIndex + 1).none { it.piece.color == color }) return null

        return stepHistory.subList(0, historyIndex + 1).last { it.piece.color == color }
    }

    fun getLastHistory(): StepHistory? {
        if (stepHistory.subList(0, historyIndex + 1).isEmpty()) return null

        return stepHistory[historyIndex]
    }

    fun getLastHistoryBefore(): StepHistory? {
        if (stepHistory.subList(0, historyIndexBefore + 1).isEmpty()) return null

        return stepHistory[historyIndexBefore]
    }

    fun getHistory(): Pair<List<String>, Int> {
        val history = mutableListOf<String>()
        var i = 0
        while (i < stepHistory.size) {
            val step = stepHistory[i]
            val change = if (step.change == null) "" else "${step.change!!.second.type.ordinal}"
            history.add("${step.from.x}${step.from.y}${step.to.x}${step.to.y}" + change)
            i++
        }
        return Pair(history, historyIndex)
    }

    fun size(): Int {
        return stepHistory.size
    }

    fun isMoved(piece: Piece): Boolean {
        return stepHistory.subList(0, historyIndex + 1).any { it.piece == piece }
    }

    fun clone() : BoardHistory{
        return BoardHistory(board, stepHistory.map{it.copy()}, historyIndex, historyIndexBefore)
    }

    fun addChange(change: Pair<Piece, Piece>?) {
        stepHistory[historyIndex].change = change
    }
}