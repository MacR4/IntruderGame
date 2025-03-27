package com.badlogic.drop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
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

/** First screen of the application. Displayed after the application is created.  */
class FirstScreen : Screen {

    private lateinit var stage: Stage
    private lateinit var playerTexture: Texture
    private lateinit var playerSprite: Sprite
    private lateinit var batch: SpriteBatch
    lateinit var touchpad: Touchpad
    var playerX = 100f
    var playerY = 100f
    var playerSpeed = 5f

    override fun show() {
        // Prepare your screen here.
        batch = SpriteBatch()
        playerTexture = Texture("BasicCharcter.png")
        playerSprite = Sprite(playerTexture)
        playerX = Gdx.graphics.width / 2f - playerSprite.width /2
        playerY = Gdx.graphics.height / 2f - playerSprite.height /2
        stage = Stage(ScreenViewport(), batch)
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
        // Draw your screen here. "delta" is the time since last render in seconds.

        // Clear Screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        ScreenUtils.clear(Color.BLACK)

        // Update player position
        val x = touchpad.knobPercentX
        val y = touchpad.knobPercentY
        playerX += x * playerSpeed * delta * 60
        playerY += y * playerSpeed * delta * 60


        // Update and draw stage
        stage.act(delta)
        stage.draw()

        // Draw player sprite
        batch.begin()
        playerSprite.setPosition(playerX, playerY)
        playerSprite.draw(batch)
        batch.end()
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
        playerTexture.dispose()
    }
}
