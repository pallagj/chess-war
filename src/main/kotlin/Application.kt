import model.Pos
import view.Animation
import view.BoardGraphics
import view.HistoryGraphics
import view.InfoGraphics
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

class Application : javax.swing.JPanel() {
    private val boardGraphics = BoardGraphics(Pos(25, 25), Pos(500, 500))
    private val infoGraphics = InfoGraphics(Pos(525, 25), Pos(775, 500))
    private val historyGraphics = HistoryGraphics(Pos(525, 85), Pos(775, 500))

    init {
        Animation.registerCallback { repaint(); }
    }

    override fun paintComponent(graphics: Graphics?) {
        super.paintComponent(graphics)
        if(graphics == null) return

        val panelImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g: Graphics2D = panelImage.createGraphics()

        g.setRenderingHint(
            java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON)

        g.color = Color(22, 21, 18)
        g.fillRect(0, 0, width, height)

        boardGraphics.draw(g)
        infoGraphics.draw(g)
        historyGraphics.draw(g)

        graphics.drawImage(panelImage, 0, 0, null)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val img = javax.imageio.ImageIO.read(java.io.File("src\\main\\resources\\icons\\Chess_nlt45.svg.png"))

            val frame = javax.swing.JFrame()

            //Add icon to the frame and name
            frame.iconImage = img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)
            frame.title = "ChessWars"

            val panel = Application()
            frame.add(panel)
            frame.setSize(810, 560)
            frame.isVisible = true
            frame.defaultCloseOperation = javax.swing.JFrame.EXIT_ON_CLOSE
            frame.isResizable = false
            frame.setLocationRelativeTo(null)

            panel.addMouseMotionListener(object : java.awt.event.MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent?) {
                    super.mouseMoved(e)
                    if (e != null) {
                        panel.historyGraphics.mouseMoved(Pos(e.x, e.y))
                        panel.boardGraphics.mouseMoved(Pos(e.x, e.y))
                    }
                }
            })

            panel.addMouseWheelListener { e ->
                if (e != null) {
                    panel.historyGraphics.mouseWheelMoved(e.wheelRotation)
                }
            }

            panel.addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    super.mouseClicked(e)
                    val x = e.x
                    val y = e.y
                    panel.boardGraphics.click(Pos(x, y))
                    panel.historyGraphics.moseClick(Pos(x, y))
                    panel.repaint()
                }
            })
        }
    }

}