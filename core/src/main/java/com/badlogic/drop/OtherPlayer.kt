package com.badlogic.drop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

class OtherPlayer (

    private val frameWidth: Float = 48f,
    private val frameHeight: Float = 48f,

    ) {


    private var stateTime = 0f
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

    fun render(x: Float, y: Float, batch: SpriteBatch, currentDirection: FirstScreen.Direction, delta: Float) {

        val animation = walkAnimations[currentDirection]!!
        val frame = if (x != 0f || y != 0f) {
            animation.getKeyFrame(stateTime)
        } else {
            animation.getKeyFrame(0f)
        }
        val scale = 4f
        batch.draw(frame, x, y, frameWidth / 2, frameHeight / 2, frameWidth, frameHeight, scale, scale, 0f)

        stateTime += delta
    }

    fun dispose() {
        walkTextures.forEach { it.dispose() }
    }
}
