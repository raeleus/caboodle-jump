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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.ray3k.caboodlejump.Core;
import com.ray3k.caboodlejump.Entity;
import com.ray3k.caboodlejump.states.GameState;

public class PlayerEntity extends Entity {
    private Skeleton skeleton;
    private AnimationState animationState;
    private SkeletonBounds skeletonBounds;
    private GameState gameState;
    private boolean hit;
    private static final float CAMERA_MOVE_LINE = 550.0f;
    private static final float MOVE_SPEED = 1400.0f;
    private static final float FRICTION = 1200.0f;
    private BoundingBoxAttachment horizontalBbox;
    private BoundingBoxAttachment landingBbox;
    private Polygon tempPoly;
    private Polygon tempPoly2;
    private boolean jumping;

    public PlayerEntity(final GameState gameState) {
        super(gameState.getEntityManager(), gameState.getCore());
        jumping = false;
        tempPoly = new Polygon();
        tempPoly2 = new Polygon();
        setDepth(-100);
        this.gameState = gameState;
        SkeletonData skeletonData;
        if (gameState.getSelectedCharacter().equals("rex")) {
            skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/Jumpasaurus Rex.json", SkeletonData.class);
        } else {
            skeletonData = getCore().getAssetManager().get(Core.DATA_PATH + "/spine/Jump Bud.json", SkeletonData.class);
        }
        skeleton = new Skeleton(skeletonData);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "stand", true);
        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("jump")) {
                    gameState.playJumpSound();
                    jumping = true;
                    setMotion(gameState.getJumpPower(), 90.0f);
                    setGravity(gameState.getGravity(), 270.0f);
                }
            }
            
        });
        
        skeletonBounds = new SkeletonBounds();
        skeletonBounds.update(skeleton, true);
        hit = false;
        
        for (BoundingBoxAttachment bbox : skeletonBounds.getBoundingBoxes()) {
            if (bbox.getName().equals("bbox")) {
                landingBbox = bbox;
            } else if (bbox.getName().equals("hbbox")) {
                horizontalBbox = bbox;
            }
        }
    }
    
    @Override
    public void create() {
    }
    
    @Override
    public void act(float delta) {
        skeleton.setPosition(getX(), getY());
        animationState.update(delta);
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        skeletonBounds.update(skeleton, true);
        
        if (animationState.getCurrent(0).getAnimation().getName().equals("stand")) {
            if (Gdx.input.isKeyJustPressed(Keys.UP)) {
                animationState.setAnimation(0, "jump", false);
            }
        } else if (jumping) {
            if (Gdx.input.isKeyPressed(Keys.LEFT)) {
                addMotion(MOVE_SPEED * delta, 180.0f);
            } else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
                addMotion(MOVE_SPEED * delta, 0.0f);
            } else {
                setXspeed(approach(getXspeed(), 0.0f, FRICTION * delta));
            }
            
            if (getYspeed() < 0.0f) {
                for (Entity entity : gameState.getEntityManager().getEntities()) {
                    if (entity instanceof PlatformEntity) {
                        PlatformEntity platform = (PlatformEntity) entity;
                        
                        tempPoly.setVertices(terminate(skeletonBounds.getPolygon(landingBbox).items, landingBbox.getWorldVerticesLength()));
                        tempPoly2.setVertices(terminate(platform.getSkeletonBounds().getPolygon(platform.getSkeletonBounds().getBoundingBoxes().first()).items, platform.getSkeletonBounds().getBoundingBoxes().first().getWorldVerticesLength()));
                        if (Intersector.overlapConvexPolygons(tempPoly, tempPoly2)) {
                            platform.landed();
                            setXspeed(0.0f);
                            setYspeed(0.0f);
                            setGravity(0.0f, 270.0f);
                            animationState.setAnimation(0, "land", false);
                            animationState.addAnimation(0, "jump", false, 0.0f);
                            jumping = false;
                            if (!platform.isScored()) {
                                gameState.playPointSound();
                                platform.setScored(true);
                                gameState.addScore(1);
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (getY() > gameState.getCameraControllerEntity().getY() - Gdx.graphics.getHeight() / 2.0f + CAMERA_MOVE_LINE) {
            gameState.getCameraControllerEntity().addY(getY() - (gameState.getCameraControllerEntity().getY() - Gdx.graphics.getHeight() / 2.0f + CAMERA_MOVE_LINE));
        } else if (getY() < gameState.getCameraControllerEntity().getY() - Gdx.graphics.getHeight() / 2.0f - 100) {
            new GameOverTimerEntity(gameState, 2.0f);
            gameState.playFallSound();
            dispose();
        }
        
        tempPoly.setVertices(terminate(skeletonBounds.getPolygon(horizontalBbox).items, horizontalBbox.getWorldVerticesLength()));
        if (tempPoly.getBoundingRectangle().x < 0) {
            addX(-tempPoly.getBoundingRectangle().x);
            setXspeed(0.0f);
        } else if (tempPoly.getBoundingRectangle().x + tempPoly.getBoundingRectangle().width > Gdx.graphics.getWidth()) {
            addX(-(tempPoly.getBoundingRectangle().x + tempPoly.getBoundingRectangle().width - Gdx.graphics.getWidth()));
            setXspeed(0.0f);
        }
    }

    private float approach(float value, float goal, float increment) {
        if (value > goal) {
            value -= increment;
            if (value < goal) {
                value = goal;
            }
        } else if (value < goal) {
            value += increment;
            if (value > goal) {
                value = goal;
            }
        }
        return value;
    }
    
    private float[] terminate(float[] values, int count) {
        float[] returnValue = new float[count];
        for (int i = 0; i < count; i++) {
            returnValue[i] = values[i];
        }
        return returnValue;
    }
    
    @Override
    public void act_end(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        getCore().getSkeletonRenderer().draw(spriteBatch, skeleton);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }
    
    public void setSkin(String name) {
        skeleton.setSkin(name);
    }
}
