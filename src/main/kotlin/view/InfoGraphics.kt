package view

import model.Board
import model.Piece
import model.PieceColor
import model.Pos
import java.awt.*
import javax.swing.ImageIcon


class InfoGraphics(var start: Pos, var end: Pos) {
    fun draw(g: Graphics2D) {
        val board = Board.board

        g.color = Color(38, 36, 33)
        g.fillRect(start.x, start.y, end.x - start.x, end.y - start.y)

        var turnColor = board.history.nextPlayer().toString().lowercase()

        g.color = Color(240, 217, 181)

        g.font = Font("Yu Gothic UI Light", Font.BOLD, 23)
        var text = turnColor.capitalize() + "'s turn"
        if(board.finished()) {
            text = board.getResult()
        }
        //draw center text (use font metrics to center) between start and end
        val fm = g.getFontMetrics()
        val x = (start.x + end.x - fm.stringWidth(text)) / 2
        val y = start.y + 40
        g.drawString(text, x, y)
    }

    fun click(pos: Pos) {
        var pos = pos - start

    }
}