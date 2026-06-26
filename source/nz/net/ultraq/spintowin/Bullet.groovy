/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.spintowin

import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.spintowin.ScopedValues.RESOURCE_MANAGER

import org.joml.Math
import org.joml.Matrix4fc

/**
 * A bullet fired by the player.
 *
 * @author Emanuel Rabina
 */
class Bullet extends Node<Bullet> {

	static final float SPEED = 300f
	static final float LIFETIME = 2f

	/**
	 * Constructor, set up the bullet node.
	 */
	Bullet(Matrix4fc initialTransform) {

		setTransform(initialTransform).translate(0f, 16f) // Start slightly ahead of the object

		var resourceManager = RESOURCE_MANAGER.get()
		var bulletImage = resourceManager.loadImage('01.png')
		addChild(new Sprite(bulletImage)
			.rotate(0f, 0f, Math.toRadians(90f))
			.scale(0.5f))

		addChild(new ScriptNode(BulletScript))
	}

	/**
	 * Bullet behaviour script.
	 */
	static class BulletScript extends Script<Bullet> {

		private float bulletTimer
		private boolean queuedForRemoval = false

		@Override
		void update(float delta) {

			bulletTimer += delta

			// Destroy bullet if it reaches the max lifetime
			if (bulletTimer > LIFETIME && !queuedForRemoval) {
				node.scene.queueUpdate { ->
					node.remove().close()
				}
				queuedForRemoval = true
			}

			// Keep moving along
			else {
				node.translate(0f, SPEED * delta as float)
			}
		}
	}
}
