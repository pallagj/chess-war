package view

import model.Board
import model.Pos
import java.awt.*
import java.util.*


class InfoGraphics(private var start: Pos, private var end: Pos) {
    fun draw(g: Graphics2D) {
        val board = Board.board

        g.color = Color(38, 36, 33)
        g.fillRect(start.x, start.y, end.x - start.x, end.y - start.y)

        val turnColor = board.history.nextPlayer().toString().lowercase()

        g.color = Color(240, 217, 181)

        g.font = Font("Yu Gothic UI Light", Font.BOLD, 23)
        var text = turnColor.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + "'s turn"
        if(board.finished()) {
            text = board.getResult()
        }

        val fm = g.fontMetrics
        val x = (start.x + end.x - fm.stringWidth(text)) / 2
        val y = start.y + 40
        g.drawString(text, x, y)
    }
}