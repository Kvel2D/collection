package com.mygdx.core

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input


class Main : ApplicationAdapter() {
    lateinit var game: Game

    override fun create() {
        game = Game()
    }

    override fun render() {
        game.update(Gdx.graphics.deltaTime)

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
        }
    }

    override fun dispose() {
        game.dispose()
        assets.dispose()
    }
}

