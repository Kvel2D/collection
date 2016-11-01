package com.mygdx.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

val spriteBatch = SpriteBatch()

val transitionTime = 0.5f

class Game(val recursionLevel: Int = 0) {
    enum class GameState {
        Choice, Transition, Last, End
    }

    var state = GameState.Choice
    var stateTimer = 0f
    val inputTimeout = 0.5f

    val viewport = IntRect(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
    val fakeViewport = IntRect(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
    val fakeFakeViewport = IntRect(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)

    val background = assets.getTexture("background.jpg")
    val arts = mutableListOf<Texture>()
    val artsR = mutableListOf<TextureRegion>()
    val answers = BooleanArray(11, { i -> false })
    val positions = Array(11, { i -> Vector2() })
    val frame = assets.getTexture("frame.png")
    val frameSmall = assets.getTexture("frame_small.png")
    val cursor = assets.getTexture("cursor.png")

    val isart: Button
    val notart: Button
    val isartFake: Button
    val notartFake: Button

    var currentArt = 1

    var fakeGame: Game? = null

    init {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        Texture.setAssetManager(assets)

        if (recursionLevel != 0) {
            for (i in 0..recursionLevel-1) {
                viewport.x += 800 / (Math.pow(4.0, i.toDouble())).toInt()
                viewport.y += 500 / (Math.pow(4.0, i.toDouble())).toInt()
            }
            viewport.w = (viewport.w / Math.pow(4.0, recursionLevel.toDouble())).toInt()
            viewport.h = (viewport.h / Math.pow(4.0, recursionLevel.toDouble())).toInt()
        }

        fakeViewport.w = viewport.w / 2
        fakeViewport.h = viewport.h / 2
        fakeViewport.x = (viewport.x + viewport.w / 2f - fakeViewport.w / 2f).toInt()
        fakeViewport.y = (viewport.y + viewport.h / 2f - fakeViewport.h / 2f  + viewport.h / 8f).toInt()

        fakeFakeViewport.w = fakeViewport.w / 2
        fakeFakeViewport.h = fakeViewport.h / 2
        fakeFakeViewport.x = (fakeViewport.x + fakeViewport.w / 2f - fakeFakeViewport.w / 2f).toInt()
        fakeFakeViewport.y = (fakeViewport.y + fakeViewport.h / 2f - fakeFakeViewport.h / 2f).toInt() + fakeViewport.h / 8

        isart = Button(assets.getTexture("isartEN.png"), 300f, 100f)
        notart = Button(assets.getTexture("notartEN.png"), Constants.VIEWPORT_WIDTH - 300f, 100f)
        isartFake = Button(assets.getTexture("isartEN.png"), 300f, 100f)
        notartFake = Button(assets.getTexture("notartEN.png"), Constants.VIEWPORT_WIDTH - 300f, 100f)

        for (i in 1..11) {
            arts.add(assets.getTexture("art$i.png"))
        }
        arts.shuffle()

        arts.forEach {
            artsR.add(TextureRegion(it))
        }

        positions[0] = Vector2(MathUtils.random(50f, 100f), MathUtils.random(5f, 20f))
    }

    fun dispose() {
        spriteBatch.dispose()
    }

    fun updateChoice(deltaTime: Float) {
        stateTimer += deltaTime

        val touchX = (Gdx.input.x - viewport.x) * Constants.VIEWPORT_WIDTH / viewport.w
        val touchY = (Constants.VIEWPORT_HEIGHT - Gdx.input.y - viewport.y) * Constants.VIEWPORT_HEIGHT / viewport.h
        isart.update(deltaTime, touchX, touchY)
        notart.update(deltaTime, touchX, touchY)

        if (Gdx.input.isTouched && stateTimer > inputTimeout) {
            if (isart.bounds.contains(touchX.toFloat(), touchY.toFloat())
                    || notart.bounds.contains(touchX.toFloat(), touchY.toFloat())) {
                state = GameState.Transition
                stateTimer = 0f

                isart.startDisappearing()
                notart.startDisappearing()
            }
            if (isart.bounds.contains(touchX.toFloat(), touchY.toFloat())) {
                answers[currentArt] = true
            }
        }

        Gdx.gl.glViewport(viewport.x, viewport.y, viewport.w, viewport.h)
        if (recursionLevel == 0) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }
        spriteBatch.begin()

        spriteBatch.draw(background, 0f, 0f)

        spriteBatch.draw(arts[currentArt], 640f - arts[currentArt].width / 2f, 450f - arts[currentArt].height / 2f)

        isart.draw()
        notart.draw()

        spriteBatch.end()
    }

    fun updateTransition(deltaTime: Float) {
        stateTimer += deltaTime

        if (stateTimer > transitionTime) {
            state = GameState.Choice
            stateTimer = 0f

            currentArt++
            if (currentArt > arts.size - 1) {
                currentArt = arts.size - 1
                state = GameState.Last
            }

            val lang: String
            if (currentArt < 6) {
                lang = "EN"
            } else if (currentArt < 10) {
                val k = MathUtils.random(1, 5)
                lang = when (k) {
                    1 -> "IT"
                    2 -> "GR"
                    3 -> "FR"
                    4 -> "JP"
                    else -> "RU"
                }
            } else if (state != GameState.Last) {
                lang = "NA"
            } else {
                lang = "EN"
            }

            isart.region = TextureRegion(assets.getTexture("isart$lang.png"))
            notart.region = TextureRegion(assets.getTexture("notart$lang.png"))
            isart.stopDisappearing()
            notart.stopDisappearing()
        }

        val touchX = (Gdx.input.x - viewport.x) * Constants.VIEWPORT_WIDTH / viewport.w
        val touchY = (Constants.VIEWPORT_HEIGHT - Gdx.input.y - viewport.y) * Constants.VIEWPORT_HEIGHT / viewport.h
        isart.update(deltaTime, touchX, touchY)
        notart.update(deltaTime, touchX, touchY)

        Gdx.gl.glViewport(viewport.x, viewport.y, viewport.w, viewport.h)
        if (recursionLevel == 0) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }
        spriteBatch.begin()

        spriteBatch.draw(background, 0f, 0f)

        spriteBatch.draw(arts[currentArt], 640f - arts[currentArt].width / 2f, 450f - arts[currentArt].height / 2f)

        isart.draw()
        notart.draw()

        spriteBatch.end()
    }

    fun updateLast(deltaTime: Float) {
        stateTimer += deltaTime

        var touchX = (Gdx.input.x - viewport.x) * Constants.VIEWPORT_WIDTH / viewport.w
        var touchY = (Constants.VIEWPORT_HEIGHT - Gdx.input.y - viewport.y) * Constants.VIEWPORT_HEIGHT / viewport.h

        if (Gdx.input.justTouched() &&
                (isart.bounds.contains(touchX.toFloat(), touchY.toFloat())
                        || notart.bounds.contains(touchX.toFloat(), touchY.toFloat()))) {
            state = GameState.End

            var maxWidth = 0
            var maxHeight = 0
            arts.forEach {
                if (it.width > maxWidth) {
                    maxWidth = it.width
                }
                if (it.height > maxHeight) {
                    maxHeight = it.height
                }
            }
            maxWidth /= 2;
            maxHeight /= 2;

            var x = MathUtils.random(50f, 100f)
            var y = 0
            for (i in 1..10) {
                if (x < viewport.w - arts[i - 1].width / 2 - arts[i].width / 2) {
                    x += arts[i - 1].width / 2 + MathUtils.random(50f, 100f)
                    positions[i].x = x
                } else {
                    y++
                    x = MathUtils.random(50f, 100f)
                    positions[i].x = x
                }
                positions[i].y = y * maxHeight.toFloat() * 1.05f + MathUtils.random(10f, 30f)
            }

            fakeFakeViewport.x = 800
            fakeFakeViewport.y = 500
            fakeGame = Game(recursionLevel + 1)
        }

        isart.update(deltaTime, touchX, touchY)
        notart.update(deltaTime, touchX, touchY)

        Gdx.gl.glViewport(viewport.x, viewport.y, viewport.w, viewport.h)
        if (recursionLevel == 0) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }
        spriteBatch.begin()
        spriteBatch.draw(background, 0f, 0f)
        if (recursionLevel == 0) {
            spriteBatch.draw(frame, fakeViewport.x - 20f * viewport.w / Constants.VIEWPORT_WIDTH, fakeViewport.y - 20f * viewport.w / Constants.VIEWPORT_WIDTH)
        } else if (recursionLevel == 1){
            spriteBatch.draw(frame, fakeViewport.x - frame.width * 0.84f - 20f * viewport.w / Constants.VIEWPORT_WIDTH.toFloat(),
                    fakeViewport.y - frame.height * 0.79f - 20f * viewport.w / Constants.VIEWPORT_WIDTH.toFloat())
        } else {
            spriteBatch.draw(frame, fakeViewport.x - frame.width * 1.05f - 20f * viewport.w / Constants.VIEWPORT_WIDTH.toFloat(),
                    fakeViewport.y - frame.height - 20f * viewport.w / Constants.VIEWPORT_WIDTH.toFloat())
        }
        isart.draw()
        notart.draw()
        spriteBatch.end()

        touchX = (viewport.w / 2f - Math.abs(viewport.w / 2f - (Gdx.input.x - viewport.x) * Constants.VIEWPORT_WIDTH / viewport.w)).toInt()
        touchY = (Constants.VIEWPORT_HEIGHT - Gdx.input.y - viewport.y) * Constants.VIEWPORT_HEIGHT / viewport.h + viewport.y
        isartFake.update(deltaTime, touchX, touchY)
        notartFake.update(deltaTime, touchX, touchY)

        Gdx.gl.glViewport(fakeViewport.x, fakeViewport.y, fakeViewport.w, fakeViewport.h)
        spriteBatch.begin()
        spriteBatch.draw(background, 0f, 0f)
        spriteBatch.draw(arts[0], 640f - arts[0].width / 2f, 450f - arts[0].height / 2f)
        isartFake.draw()
        notartFake.draw()
        val fakeCursorX = viewport.w / 2f - Math.abs(viewport.w / 2f - Gdx.input.x)
        val fakeCursorY = (viewport.h - Gdx.input.y).toFloat()
        spriteBatch.draw(cursor, fakeCursorX, fakeCursorY)
        spriteBatch.end()
    }

    fun updateEnd(deltaTime: Float) {

        Gdx.gl.glViewport(viewport.x, viewport.y, viewport.w, viewport.h)
        if (recursionLevel == 0) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        }
        spriteBatch.begin()
        spriteBatch.draw(background, 0f, 0f)

        for (i in 0..10) {
            if (answers[i]) {
                spriteBatch.drawScaled(artsR[i], positions[i].x - artsR[i].regionWidth / 4f, positions[i].y - artsR[i].regionHeight / 4f, 0.5f)
            }
        }

        spriteBatch.draw(frameSmall, fakeFakeViewport.x - 10f, fakeFakeViewport.y - 10f)
        spriteBatch.end()

        fakeGame!!.update(deltaTime)
    }

    fun update(deltaTime: Float) {
        when (state) {
            GameState.Choice -> {
                updateChoice(deltaTime)
            }
            GameState.Transition -> {
                updateTransition(deltaTime)
            }
            GameState.Last -> {
                updateLast(deltaTime)
            }
            GameState.End -> {
                updateEnd(deltaTime)
            }
        }
    }
}

class Button {
    enum class ButtonState {
        Untouched, Touched, Disappearing
    }

    var state = ButtonState.Untouched
    var stateTimer = 0f
    val stateTimerMax = 0.1f
    val touchedSize = 1.2f
    var region: TextureRegion
    val bounds = Rectangle()
    val x: Float
    val y: Float
    var scale = 1f
    val touchMargin = 1.2f
    var alpha = 1f

    constructor(texture: Texture, x: Float, y: Float) {
        this.region = TextureRegion(texture)
        this.x = x
        this.y = y
    }

    fun startDisappearing() {
        state = ButtonState.Disappearing
        stateTimer = 0f
    }

    fun stopDisappearing() {
        state = ButtonState.Untouched
        stateTimer = 0f
        alpha = 1f
    }

    fun update(deltaTime: Float, touchX: Int, touchY: Int) {
        when (state) {
            ButtonState.Touched -> {
                stateTimer += deltaTime
                if (stateTimer > stateTimerMax) {
                    stateTimer = stateTimerMax
                }
                scale = MathUtils.lerp(1f, touchedSize, stateTimer / stateTimerMax)
                bounds.set(x - (region.regionWidth * touchMargin * scale) / 2f, y - (region.regionHeight * touchMargin * scale) / 2f,
                        region.regionWidth * touchMargin * scale, region.regionHeight * touchMargin * scale)


                if (!bounds.contains(touchX.toFloat(), touchY.toFloat())) {
                    state = ButtonState.Untouched
                }
            }
            ButtonState.Untouched -> {
                stateTimer -= deltaTime
                if (stateTimer < 0f) {
                    stateTimer = 0f
                }
                scale = MathUtils.lerp(1f, touchedSize, stateTimer / stateTimerMax)
                bounds.set(x - (region.regionWidth * touchMargin * scale) / 2f, y - (region.regionHeight * touchMargin * scale) / 2f,
                        region.regionWidth * touchMargin * scale, region.regionHeight * touchMargin * scale)


                if (bounds.contains(touchX.toFloat(), touchY.toFloat())) {
                    state = ButtonState.Touched
                }
            }
            ButtonState.Disappearing -> {
                stateTimer += deltaTime
                alpha = MathUtils.lerp(1f, 0f, stateTimer / transitionTime)
            }
        }
    }

    // draw centered horizontally and vertically
    fun draw() {
        spriteBatch.setColor(1f, 1f, 1f, alpha)
        spriteBatch.drawScaled(region, x - region.regionWidth / 2f, y - region.regionHeight / 2f, scale)
        spriteBatch.setColor(1f, 1f, 1f, 1f)
    }
}