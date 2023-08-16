package view

import model.Board
import model.Pos
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min


class HistoryGraphics(private var start: Pos, private var end: Pos) {
    private var scrollPos: Int = 0
    private var scrollPosBefore: Int = 0
    private val cellHeight = 25
    private val numberCellWidth = 30
    private val cellWidth = (end.x - start.x + 1 - numberCellWidth) / 2

    private val cellColor = Color(64, 64, 64)
    private val backgroundColor = Color(55, 53, 49)
    private val darkBackground = Color(38, 36, 33)
    private val selectedColor = Color(42, 64, 83)
    private val hoverColor = Color(54, 146, 231)

    private var mouse: Pos = Pos(0, 0)
    private val autoScrollAnimation = Animation(duration = 0.5f, interpolationType = InterpolationType.EASE_IN_OUT)
    private var historyIndexBefore = -1
    fun draw(graphics: Graphics2D) {
        val (history, historyIndex) = Board.board.history.getHistory()
        val rows = (history.size / 2 + history.size % 2)
        val height = rows * cellHeight

        if (height <= 0) return

        val img = BufferedImage(end.x - start.x + 1, height, BufferedImage.TYPE_INT_ARGB)

        if(historyIndex != historyIndexBefore) {
            autoScrollAnimation.reset()
            val selectedHeight = ((historyIndex+1) / 2 + (historyIndex+1) % 2)*cellHeight

            scrollPosBefore = scrollPos
            scrollPos = max(min(selectedHeight - (end.y-start.y) / 2, height - (end.y - start.y)), 0)
            historyIndexBefore = historyIndex
        }
        val t = autoScrollAnimation.eval()
        val scrollPos = (this.scrollPos*t + scrollPosBefore*(1-t)).toInt()

        val g = img.createGraphics() as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)


        g.color = backgroundColor
        g.fillRect(start.x, start.y, end.x - start.x, end.y - start.y)

        g.color = cellColor
        g.drawLine(start.x, start.y, end.x, start.y)

        for (i in 0 until rows) {
            val cellWiths =
                listOf(numberCellWidth, (img.width - numberCellWidth) / 2, (img.width - numberCellWidth) / 2)
            var x = 0
            cellWiths.forEachIndexed { index, cellWith ->
                g.color = if (index == 0) backgroundColor else darkBackground
                if (index != 0 && i * 2 + index - 1 == historyIndex) g.color = selectedColor
                if (index != 0 && mouse.x in start.x + x until start.x + x + cellWith && mouse.y + scrollPos in start.y + i * cellHeight until start.y + (i + 1) * cellHeight) {
                    g.color = hoverColor
                }
                g.fillRect(x, i * cellHeight, cellWith, cellHeight)
                g.color = cellColor
                g.drawRect(x, i * cellHeight, cellWith, cellHeight)

                var text = "${i + 1}"
                if (index > 0) {
                    text = if (i * 2 + index - 1 < history.size) history[i * 2 + index - 1] else ""
                }

                g.color = Color.white
                g.font = g.font.deriveFont(17f)

                g.drawString(text, x + 6, i * cellHeight + 20)

                x += cellWith
            }
        }


        val subImage = img.getSubimage(0, scrollPos, img.width, min(img.height - scrollPos, end.y - start.y))
        graphics.drawImage(subImage, start.x, start.y, null)

        graphics.color = cellColor
        graphics.drawRect(start.x, start.y, end.x - start.x, subImage.height)
    }

    fun moseClick(pos: Pos) {
        if (pos.x !in start.x..end.x || pos.y !in start.y..end.y)
            return

        val (x, y) = pos - start + Pos(-numberCellWidth, scrollPos)
        val i = x / cellWidth
        val j = y / cellHeight
        val index = j * 2 + i

        val board = Board.board

        if (index !in 0 until board.history.size()) return
        Board.board.history.goToIndex(index)
    }

    fun mouseMoved(pos: Pos) {
        mouse = pos
    }

    fun mouseWheelMoved(scroll: Int) {
        val (history) = Board.board.history.getHistory()
        val height = (history.size / 2 + history.size % 2) * cellHeight

        scrollPos = max(min(scrollPos + scroll * 5, height - (end.y - start.y)), 0)
    }

}