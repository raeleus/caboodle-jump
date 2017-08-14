/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ray3k.caboodlejump.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ray3k.caboodlejump.Entity;
import com.ray3k.caboodlejump.states.GameState;

public class CameraControllerEntity extends Entity {
    private GameState gameState;
    private int bgCounter;

    public CameraControllerEntity(GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        bgCounter = 0;
    }
    
    @Override
    public void create() {
        
    }

    @Override
    public void act(float delta) {
        gameState.getGameCamera().position.set(getX(), getY(), 0.0f);
        int count = (int) (getY() / Gdx.graphics.getHeight()) + 1;
        if (count > bgCounter) {
            bgCounter = count;
            BackgroundEntity bg = new BackgroundEntity(gameState);
            bg.setPosition(0.0f, bgCounter * Gdx.graphics.getHeight());
            bg.setWidth(Gdx.graphics.getWidth());
            bg.setHeight(Gdx.graphics.getHeight());
        }
        
        if (getY() + Gdx.graphics.getHeight() / 2.0f > gameState.getPlatformGoalY()) {
            gameState.spawnPlatform(gameState.getPlatformGoalY());
            gameState.setPlatformGoalY(gameState.getPlatformGoalY() + gameState.getPlatformSpacing());
        }
    }

    @Override
    public void act_end(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

}
