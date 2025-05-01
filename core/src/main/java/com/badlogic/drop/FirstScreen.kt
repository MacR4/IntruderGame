package com.badlogic.drop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import org.w3c.dom.Text
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import java.io.OutputStream
import java.net.Socket
import java.util.UUID

/** First screen of the application. Displayed after the application is created.  */
class FirstScreen : Screen {

    private lateinit var stage: Stage
    private lateinit var batch: SpriteBatch
    private lateinit var touchpad: Touchpad
    private lateinit var player: Player
    private lateinit var tiledMap: TiledMap

    private val queue = ThreadSafeQueue<String>()
    private lateinit var outStream: OutputStream
    private var clientId: String = ""
    private val otherPlayers = mutableMapOf<String, OtherPlayer>()
    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private var otherX: Float = 0f
    private var otherY: Float = 0f
    private var otherDirection: Direction = Direction.DOWN

    enum class Direction { UP, DOWN, LEFT, RIGHT }

    override fun show() {

        clientId = UUID.randomUUID().toString()

        val socket = Socket("10.0.2.2", 4300)
        val receiverThread = TcpReceiver(socket, queue)

        receiverThread.start()
        outStream = socket.getOutputStream()

        Thread {
            outStream.write(("CONNECT $clientId ").toByteArray())
            outStream.flush()
        }.start()

        // Prepare your screen here.
        batch = SpriteBatch()
        stage = Stage(ScreenViewport(), batch)
        player = Player()

        tiledMap = TmxMapLoader().load("Map1.tmx")
        val touchpadSkin = Skin()
        touchpadSkin.add("touchBackground", Texture("touchpad.png"))
        touchpadSkin.add("touchKnob", Texture("touchpad-knob.png"))
        val touchpadStyle = Touchpad.TouchpadStyle()
        val touchpadBackground = touchpadSkin.getDrawable("touchBackground")
        val touchpadKnob = touchpadSkin.getDrawable("touchKnob")
        touchpadStyle.background = touchpadBackground
        touchpadStyle.knob = touchpadKnob
        touchpad = Touchpad(10f, touchpadStyle)
        touchpad.x = 40f
        touchpad.y = 40f
        stage.addActor(touchpad)
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        // Clear Screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        ScreenUtils.clear(Color.BLACK)

        // Update player position
        val moveX = touchpad.knobPercentX
        val moveY = touchpad.knobPercentY

        player.update(delta, moveX, moveY)

        val newX = player.returnX()
        val newY = player.returnY()
        val direction = player.returnDirection()

        // Only send new position to server if it has changed
        if (newX != lastX || newY != lastY) {
            Thread {
                outStream.write(("MOVE $clientId $newX $newY $direction").toByteArray())
                outStream.flush()
            }.start()
            lastX = newX
            lastY = newY
        }

        // Process incoming messages
        var message = queue.dequeue()

        while (message != null) {
            Gdx.app.log("Network", "Received message: $message")
            val tokens = message.split(" ")
            val id = tokens[1]
            if (id != clientId){
                otherX = tokens[2].toFloat()
                otherY = tokens[3].toFloat()
                otherDirection= when (tokens[4]) {
                    "UP" -> Direction.UP
                    "LEFT" -> Direction.LEFT
                    "RIGHT" -> Direction.RIGHT
                    else -> Direction.DOWN
                }
                otherPlayers[id] = OtherPlayer()
            }
            message = queue.dequeue()
        }

        batch.begin()
        tiledMap.layers.forEach { layer ->
            if (layer is TiledMapTileLayer) {
                renderTileLayer(layer)  // Render the tiles
            }
        }


        otherPlayers.values.forEach { otherPlayer ->
            otherPlayer.render(otherX, otherY, batch, otherDirection, delta)
        }

        player.render(batch)
        batch.end()

        // Update and draw the stage
        stage.act(delta)
        stage.draw()

    }

    private fun renderTileLayer(layer: TiledMapTileLayer) {
        // Render the individual tiles in the layer
        val width = layer.width
        val height = layer.height

        for (x in 0 until width) {
            for (y in 0 until height) {
                val cell = layer.getCell(x, y)
                if (cell != null) {
                    val tile = cell.tile
                    batch.draw(tile.textureRegion, x * layer.tileWidth.toFloat(), y * layer.tileHeight.toFloat())
                }
            }
        }
    }


    override fun resize(width: Int, height: Int) {
        // Resize your screen here. The parameters represent the new window size.
        stage.viewport.update(width, height, true)
    }

    override fun pause() {
        // Invoked when your application is paused.
    }

    override fun resume() {
        // Invoked when your application is resumed after pause.
    }

    override fun hide() {
        // This method is called when another screen replaces this one.
    }

    override fun dispose() {
        // Destroy screen's assets here.
        stage.dispose()
        batch.dispose()
        tiledMap.dispose()
        player.dispose()

    }
}
