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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.ray3k.caboodlejump.states.GameState;

public class MovingPlatformEntity extends PlatformEntity {
    private static Vector2 temp1 = new Vector2();
    private static Vector2 temp2 = new Vector2();
    private boolean left;
    private static final float PLATFORM_SPEED = 200.0f;

    public MovingPlatformEntity(final GameState gameState) {
        super(gameState);
        left = MathUtils.randomBoolean();
        setSkin("moving");
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        
        float targetX;
        if (left) {
            targetX = 70.0f;
        } else {
            targetX = Gdx.graphics.getWidth() - 70.0f;
        }
        
        moveTowardsPoint(targetX, getY(), PLATFORM_SPEED, delta);
        
        if (MathUtils.isEqual(getX(), targetX)) {
            left = !left;
        }
    }
    
    public void moveTowardsPoint(float x, float y, float speed, float delta) {
        float originalX = getX();
        float originalY = getY();
        
        temp1.set(getX(), getY());
        temp2.set(x, y);
        temp2.sub(temp1).nor();
        
        temp1.set(speed * delta, 0);
        temp1.rotate(temp2.angle());
        
        if (getX() != x) {
            addX(temp1.x);
        }
        if (getY() != y) {
            addY(temp1.y);
        }
        
        if (originalX < x && getX() > x || originalX > x && getX() < x) {
            setX(x);
        }
        
        if (originalY < y && getY() > y || originalY > y && getY() < y) {
            setY(y);
        }
    }
}
