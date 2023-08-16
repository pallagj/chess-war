package view

import model.*
import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt


enum class ChangeState {
    NO_CHANGE, BEFORE_CHANGE, AT_CHANGE
}

class BoardGraphics(private var start: Pos, private var end: Pos, val drawCoordinates: Boolean = false) {
    private var selectedPos: Pos? = null
    private val cell = (end - start) / 8

    private var pieces = mutableMapOf<Piece, ImageIcon>()
    private val moveAnimation = Animation(duration=0.3f, interpolationType = InterpolationType.EASE_IN_OUT)
    private val changeAnimation = Animation(duration=0.1f, interpolationType = InterpolationType.EASE_IN_OUT)
    private var lastHistoryIndex = -1

    private var inChange = ChangeState.NO_CHANGE
    private var selectedTarget = Pos(0, 0)

    private var hoverMouse = Pos(0, 0)

    init {
        Board.board.pieces.forEach {
            val cell = (end - start) / 8
            val imageIcon =
                ImageIcon("src\\main\\resources\\icons\\Chess_${it.letter.lowercase()}${if (it.color == PieceColor.WHITE) "l" else "d"}t45.svg.png")
            val image = imageIcon.image // transform it
            val newImg = image.getScaledInstance(cell.x, cell.y, Image.SCALE_SMOOTH) // scale it the smooth way
            pieces[it] = ImageIcon(newImg) // transform it back
        }
    }

    fun chessToBoard(pos: Pos): Pos {
        return Pos((pos.x - 1) * cell.x, (8 - pos.y) * cell.y) + start
    }

    fun boardToChess(pos: Pos): Pos {
        return Pos((pos.x - start.x) / cell.x + 1, 8 - (pos.y - start.y) / cell.y)
    }

    fun getIcon(color: PieceColor, type: PieceType): ImageIcon? {
        pieces.forEach { (piece, icon) ->
            if (piece.color == color && piece.type == type) {
                return icon
            }
        }

        return null
    }

    fun draw(g: Graphics2D) {
        val board = Board.board
        if (lastHistoryIndex != board.history.historyIndex) {
            moveAnimation.reset()
            lastHistoryIndex = Board.board.history.historyIndex
            inChange = ChangeState.NO_CHANGE
            selectedPos = null
        }

        g.color = Color(240, 217, 181)
        g.fillRect(start.x, start.y, cell.x * 8, cell.y * 8)

        //Draw chess board
        for (i in 0..7) {
            for (j in 0..7) {
                if ((i + j) % 2 == 1) {
                    g.color = Color(181, 136, 99)
                    g.fillRect(i * cell.x + start.x, j * cell.y + start.y, cell.x, cell.y)
                }

                val piece = board.at(Pos(i + 1, 8 - j))
                if (piece != null && !piece.knockable && piece.attackedAtPosition()) {
                    g.color = Color(139, 0, 0)
                    //gradient with center g.color and outside transperent:
                    g.paint = RadialGradientPaint(
                        Point(i * cell.x + start.x + cell.x / 2, j * cell.y + start.y + cell.y / 2),
                        cell.x / 1.5f,
                        floatArrayOf(0.0f, 0.25f, 0.89f, 1f),
                        arrayOf(Color(255, 0, 0), Color(231, 0, 0), Color(169, 0, 0, 0), Color(158, 0, 0, 0))
                    )
                    g.fillRect(i * cell.x + start.x, j * cell.y + start.y, cell.x, cell.y)
                }

            }
        }

        g.drawRect(start.x, start.y, cell.x * 8, cell.y * 8)

        //draw last historyMove
        var lastStepHistory = board.history.getLastHistory()
        if (lastStepHistory != null) {
            listOf(lastStepHistory.from, lastStepHistory.to).forEach { p ->
                g.color = if ((p.x + p.y) % 2 == 0) Color(170, 162, 58)
                else Color(205, 210, 106)

                val (x, y) = chessToBoard(p)

                g.fillRect(x, y, cell.x, cell.y)
            }
        }


        //Draw selected cell
        if (selectedPos != null) {
            val x = (selectedPos!!.x - 1) * cell.x + start.x
            val y = (9 - selectedPos!!.y - 1) * cell.y + start.y

            val width = cell.x
            val height = cell.y

            g.color = if ((selectedPos!!.x + selectedPos!!.y) % 2 == 0) Color(100, 111, 64)
            else Color(130, 151, 105)

            g.fillRect(x, y, width, height)

            val piece = board.at(selectedPos!!)

            piece?.getPossibleMoves()?.forEach {
                g.color = if ((it.x + it.y) % 2 == 0) Color(100, 111, 64)
                else Color(130, 151, 105)

                if (hoverMouse == it) {
                    g.fillRect(
                        (it.x - 1) * cell.x + start.x, (9 - it.y - 1) * cell.y + start.y, width, height
                    )
                } else {
                    if (board.isEmptyAt(it)) {
                        g.fillOval(
                            (it.x - 1) * cell.x + start.x + (cell.x / 2.0 - cell.x / (2 * 3.6)).toInt(),
                            (9 - it.y - 1) * cell.y + start.y + (cell.y / 2.0 - cell.y / (2 * 3.6)).toInt(),
                            (cell.x / 3.6).toInt(),
                            (cell.y / 3.6).toInt()
                        )
                    } else {
                        //set paint as oval inside empty and outside fill:
                        g.paint = RadialGradientPaint(
                            Point(
                                (it.x - 1) * cell.x + start.x + cell.x / 2,
                                (9 - it.y - 1) * cell.y + start.y + cell.y / 2
                            ), cell.x / 1.75f, floatArrayOf(0.9f, 1f), arrayOf(Color(0, 0, 0, 0), g.color)
                        )
                        g.fillRect(
                            (it.x - 1) * cell.x + start.x, (9 - it.y - 1) * cell.y + start.y, width, height
                        )
                    }
                }
            }
        }

        //Draw coordinates
        if (drawCoordinates) {
            for (i in 0..7) {
                for (j in 0..7) {
                    g.color = Color(0, 0, 0)
                    g.drawString(
                        "${i + 1}${8 - j}", i * cell.x + start.x, j * (cell.y) + start.y + 11
                    )
                }
            }
        }

        g.color = Color.RED

        //Draw pieces
        var changes = board.history.lastChange().toMutableMap()

        if (inChange != ChangeState.NO_CHANGE) {
            //in changes all value pair second is the first
            changes.replaceAll { _, v -> Pair(v.second, v.second) }
            changes = changes.filter { (k, v) -> k.position != selectedTarget }.toMutableMap()
            changes[board.at(selectedPos!!)!!] = Pair(selectedPos, selectedTarget)
        }

        changes.forEach { (piece, posChange) ->
            if (posChange.first == null && posChange.second == null) {
                return@forEach
            }

            var from = posChange.first ?: posChange.second!!
            var to = posChange.second ?: posChange.first!!

            from = Pos(from.x - 1, 8 - from.y)
            to = Pos(to.x - 1, 8 - to.y)

            val w = moveAnimation.eval()

            var x = (from.x * (1.0 - w) + to.x * w) * cell.x + start.x
            var y = (from.y * (1.0 - w) + to.y * w) * cell.y + start.y

            if(from == to) {
                x = ((from.x) * cell.x + start.x).toDouble()
                y = ((from.y) * cell.y + start.y).toDouble()
            }

            if (posChange.first == null) g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, w.toFloat())

            if (posChange.second == null) g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                (1f - w).toFloat()
            )


            g.drawImage(getIcon(piece.color, piece.type)!!.image, x.toInt(), y.toInt(), cell.x, cell.y, null)
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)
        }

        //Draw pawn change
        if (inChange != ChangeState.NO_CHANGE) {
            g.color = Color(0, 0, 0, 150)
            g.fillRect(start.x, start.y, cell.x * 8, cell.y * 8)
        }


        if (moveAnimation.eval() == 1.0) {
            if (inChange == ChangeState.BEFORE_CHANGE) {
                inChange = ChangeState.AT_CHANGE
            }
        }

        if (inChange == ChangeState.AT_CHANGE) {
            val color = board.history.nextPlayer()
            val forChagne = listOf(
                PieceType.QUEEN,
                PieceType.ROOK,
                PieceType.BISHOP,
                PieceType.KNIGHT,
            )

            forChagne.forEachIndexed { index, pieceType ->
                val img = getIcon(color, pieceType)!!.image
                val (x, y) = chessToBoard(selectedTarget)

                var w = changeAnimation.eval().toFloat()
                if (selectedTarget.y - index * color.getDirection() != selectedChange)
                    w = 0f

                val background = Color(
                    (135*(1-w) + 207*w).toInt(),
                    (135*(1-w) + 98*w).toInt(),
                    (128*(1-w) + 34*w).toInt()
                )

                g.paint = RadialGradientPaint(
                    Point(x + cell.x / 2, y + index * color.getDirection() * cell.x + cell.x / 2),
                    cell.x.toFloat() * (1 + (sqrt(2f) - 1f) * w),
                    floatArrayOf(0f, 0.5f, 0.51f),
                    arrayOf(Color.WHITE, background, Color(background.blue, background.green, background.blue, 0))
                )
                g.fillRect(x, y + index * color.getDirection() * cell.x, cell.x, cell.y)

                val zoom = ((1-w)*7+5)
                val bufferedImage = BufferedImage(cell.x, cell.y, BufferedImage.TYPE_INT_ARGB)
                bufferedImage.graphics.drawImage(img, 0, 0, null)
                val scaledImg = bufferedImage.getScaledInstance(
                    (cell.x - 2 * zoom).toInt(), (cell.y - 2 * zoom).toInt(), Image.SCALE_SMOOTH
                )

                g.drawImage(
                    scaledImg,
                    (x + zoom).toInt(),
                    (y + index * color.getDirection() * cell.x + zoom).toInt(),
                    (cell.x - zoom * 2).toInt(),
                    (cell.y - zoom * 2).toInt(),
                    null
                )
            }


        }


    }

    fun click(pos: Pos) {
        val board = Board.board

        val cell = (end - start) / 8

        val x = (pos.x - start.x) / cell.x + 1
        val y = 8 - (pos.y - start.y) / cell.y

        if (inChange == ChangeState.AT_CHANGE) {
            val (x, y) = boardToChess(pos)

            val selected = Math.abs(selectedTarget.y - y) + 1

            if (selected in 1..4 && x == selectedTarget.x) {
                board.step("${selectedPos!!}$selectedTarget${selected}")
                inChange = ChangeState.NO_CHANGE
                lastHistoryIndex = board.history.historyIndex

                selectedPos = null
                return
            }
        }

        if (inChange != ChangeState.NO_CHANGE) {
            return
        }

        if (board.isPieceAt(Pos(x, y)) && board.at(Pos(x, y))!!.color == board.history.nextPlayer()) {
            selectedPos = Pos(x, y)
        } else if (selectedPos != null) {
            try {
                board.step("${selectedPos!!.x}${selectedPos!!.y}$x$y")
                selectedPos = null
                inChange = ChangeState.NO_CHANGE
            } catch (_: PawnChangeNotSpecifiedException) {
                inChange = ChangeState.BEFORE_CHANGE
                moveAnimation.reset()
                selectedTarget = Pos(x, y)
            } catch (_: Exception) {
                selectedPos = null
            }
        }
    }

    var selectedChange = 0
    fun mouseMoved(pos: Pos) {
        hoverMouse = boardToChess(pos)

        if (inChange == ChangeState.AT_CHANGE) {
            val (x, y) = boardToChess(pos)

            if (x == selectedTarget.x && Math.abs(selectedTarget.y - y) <= 4) {
                if (y != selectedChange) {
                    selectedChange = y
                    changeAnimation.reset()
                }
            } else {
                selectedChange = 0
            }
        }
    }
}