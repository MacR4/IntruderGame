package com.badlogic.drop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader

class Player (

    private val frameWidth: Float = 48f,
    private val frameHeight: Float = 48f,
    private var x: Float = Gdx.graphics.width / 2f - frameWidth /2,
    private var y: Float = Gdx.graphics.height / 2f - frameHeight /2,
    private var speed: Float = 5f,
    private var tiledMap: TiledMap = TmxMapLoader().load("Map1.tmx"),
    private var waterTiles: TiledMapTileLayer = tiledMap.layers.get("WATER") as TiledMapTileLayer

) {


    private var stateTime = 0f
    private var currentDirection = FirstScreen.Direction.DOWN
    private val walkTextures = mutableListOf<Texture>()

    private val walkAnimations: Map<FirstScreen.Direction, Animation<TextureRegion>> = mapOf(
        FirstScreen.Direction.LEFT to loadWalkAnimation("LeftWalkSheet.png"),
        FirstScreen.Direction.RIGHT to loadWalkAnimation("RightWalkSheet.png"),
        FirstScreen.Direction.UP to loadWalkAnimation("BackwardWalkSheet.png"),
        FirstScreen.Direction.DOWN to loadWalkAnimation("ForwardWalkSheet.png")
    )

    private fun loadWalkAnimation(fileName: String): Animation<TextureRegion> {
        val texture = Texture(Gdx.files.internal(fileName))
        walkTextures.add(texture)

        val regions2D = TextureRegion.split(texture, 48, 48)
        val frames = regions2D[0]
        return Animation(0.1f, *frames).apply {
            playMode = Animation.PlayMode.LOOP
        }
    }

    fun update(delta: Float, moveX: Float, moveY: Float) {

        val newX = x + moveX * speed * delta * 60
        val newY = y + moveY * speed * delta * 60

        if (!isCellWater(newX, newY)) {
            x = newX
            y = newY
        }

        // Clamp to screen bounds
        x = x.coerceIn(0f, Gdx.graphics.width - frameWidth)
        y = y.coerceIn(0f, Gdx.graphics.height - frameHeight)
        if (moveX != 0f || moveY != 0f) {
            currentDirection = when {
                kotlin.math.abs(moveX) > kotlin.math.abs(moveY) && moveX > 0 -> FirstScreen.Direction.RIGHT
                kotlin.math.abs(moveX) > kotlin.math.abs(moveY) && moveX < 0 -> FirstScreen.Direction.LEFT
                moveY > 0 -> FirstScreen.Direction.UP
                else -> FirstScreen.Direction.DOWN
            }
            stateTime += delta
        }

    }

    fun returnX(): Float{ return x }

    fun returnY(): Float{ return y }

    fun returnDirection(): FirstScreen.Direction{ return currentDirection }

    fun render(batch: SpriteBatch) {
        val animation = walkAnimations[currentDirection]!!
        val frame = if (x != 0f || y != 0f) {
            animation.getKeyFrame(stateTime)
        } else {
            animation.getKeyFrame(0f)
        }
        val scale = 4f
        batch.draw(frame, x, y, frameWidth / 2, frameHeight / 2, frameWidth, frameHeight, scale, scale, 0f)
    }

    private fun isCellWater(x: Float, y: Float): Boolean {
        val cellX = (x / waterTiles.tileWidth).toInt()
        val cellY = (y / waterTiles.tileHeight).toInt()
        val cell = waterTiles.getCell(cellX, cellY)
        return cell != null // If there's a cell here, it's water and the player shouldn't be able to walk on it
    }

    fun dispose() {
        walkTextures.forEach { it.dispose() }
    }
}
