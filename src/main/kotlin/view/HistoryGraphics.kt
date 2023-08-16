package view

import model.Board
import model.Pos
import java.awt.Color
import java.awt.Graphics2D
import java.awt.MouseInfo
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min


class HistoryGraphics(var start: Pos, var end: Pos) {
    var scrollPos: Int = 0
    val cellHeight = 25
    val numberCellWidth = 30
    val cellWidth = (end.x - start.x + 1 - numberCellWidth) / 2


    val cellColor = Color(64, 64, 64)
    val backgroundColor = Color(55, 53, 49)
    val darkBackground = Color(38, 36, 33)
    val selectedColor = Color(42, 64, 83)
    val hoverColor = Color(54, 146, 231)

    fun draw(graphics: Graphics2D) {
        //get mouse position:
        val (history, historyIndex) = Board.board.history.getHistory()
        val height = (history.size / 2 + history.size % 2)

        if (height <= 0) return

        val img = BufferedImage(end.x - start.x + 1, height * cellHeight, BufferedImage.TYPE_INT_ARGB)

        val g = img.createGraphics() as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)


        g.color = backgroundColor
        g.fillRect(start.x, start.y, end.x - start.x, end.y - start.y)

        g.color = cellColor
        g.drawLine(start.x, start.y, end.x, start.y)

        for (i in 0 until cellHeight) {
            val cellWiths = listOf(numberCellWidth, (img.width - numberCellWidth) / 2, (img.width - numberCellWidth) / 2)
            //loop x from 0 to img.width and always add cellWiths[i] to x
            var x = 0
            cellWiths.forEachIndexed { index, cellWith ->
                g.color = if (index == 0)  backgroundColor else darkBackground
                if (i*2 + index - 1 == historyIndex) g.color = selectedColor
                if (index != 0 && mouse.x in start.x + x until start.x + x + cellWith && mouse.y + scrollPos in start.y + i * cellHeight until start.y + (i + 1) * cellHeight) {
                    g.color = hoverColor
                }
                g.fillRect(x, i * cellHeight, cellWith, cellHeight)
                g.color = cellColor
                g.drawRect(x, i * cellHeight, cellWith, cellHeight)

                var text = "${i+ 1}"
                if (index > 0) {
                    text = if (i*2 + index - 1 < history.size) history[i * 2 + index - 1] else ""
                }


                g.color = Color.white
                g.drawString(text, x + 6, i * cellHeight + 20)

                x += cellWith
            }
        }


        val subImage = img.getSubimage(0, scrollPos, img.width, min(img.height - scrollPos, end.y-start.y))
        graphics.drawImage(subImage, start.x, start.y, null)

        graphics.color = cellColor
        graphics.drawRect(start.x, start.y, end.x - start.x, img.height)
    }

    fun moseClick(pos: Pos) {
        if(pos.x !in start.x..end.x || pos.y !in start.y..end.y)
            return

        val (x, y) = pos - start + Pos(-numberCellWidth, scrollPos)
        val i = x / cellWidth
        val j = y / cellHeight
        val index = j * 2 + i

        val board = Board.board

        if (index !in 0 until board.history.size())
            return
        Board.board.history.goToIndex(index)
    }

    var mouse: Pos = Pos(0,0)
    fun mouseMoved(pos: Pos) {
        mouse = pos
    }

    fun mouseWheelMoved(scroll: Int) {
        val (history) = Board.board.history.getHistory()
        val height = (history.size / 2 + history.size % 2)  * cellHeight

        scrollPos = max(min(scrollPos + scroll*4, height - (end.y - start.y)), 0)
    }

}