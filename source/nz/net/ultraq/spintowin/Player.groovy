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

import nz.net.ultraq.easings.EasingFunctions
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.spintowin.ScopedValues.RESOURCE_MANAGER

import org.joml.Vector2f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * The player object in the scene.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> {

	private static final Logger logger = LoggerFactory.getLogger(Player)

	private static int bulletCount = 1

	/**
	 * Constructor, creates a new player object.
	 */
	Player() {

		var resourceManager = RESOURCE_MANAGER.get()

		var shipImage = resourceManager.loadImage('ship_E.png')
		addChild(new Sprite(shipImage)
			.scale(0.5f))

		var tracerImage = resourceManager.loadImage('star_tiny.png')
		for (var i in 1..9) {
			addChild(new Sprite(tracerImage)
				.translate(0f, i * 50f as float)
				.scale(0.25f)
				.withName("Tracer $i"))
		}

		addChild(new ScriptNode(PlayerScript))
	}

	/**
	 * Script of player behaviours.
	 */
	static class PlayerScript extends Script<Player> {

		private Camera camera
		private Sprite sprite

		// Rotation
		private Vector3f unprojectResult = new Vector3f()
		private Vector2f headingToCursor = new Vector2f()
		private float heading = 0f

		// Shooting
		private float firingCooldown
		static final float FIRING_RATE = 0.2f

		// Spinning!
		private static float MAX_SPIN_SPEED = 20f
		private static float TIME_TO_MAX_SPIN = 2f
		private float spinTime = 0f

		@Override
		void init() {

			camera = node.scene.findByType(Camera)
			sprite = node.findByType(Sprite)
		}

		@Override
		void update(float delta) {

			updateHeading()
			updateSpin(delta)
			updateShooting(delta)
		}

		/**
		 * Update rotation so the sprite will appear to look at the cursor.
		 */
		private void updateHeading() {

			var cursorPosition = input.cursorPosition()
			if (cursorPosition) {
				headingToCursor.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
				var lastHeading = heading
				heading = headingToCursor.angle(Vector2f.UP)
				node.rotate(0f, 0f, lastHeading - heading as float)
			}
		}

		/**
		 * Fire bullets.
		 */
		private void updateShooting(float delta) {

			firingCooldown -= delta

			if ((input.keyPressed(GLFW_KEY_SPACE) || input.mouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) && firingCooldown <= 0f) {
				var scene = node.scene
				scene.queueUpdate { ->
					scene.addChild(new Bullet(node.transform)
						.withName("Bullet ${bulletCount++}"))
				}
				firingCooldown = FIRING_RATE
			}
		}

		/**
		 * Spin the ship!
		 */
		private void updateSpin(float delta) {

			if (input.keyPressed(GLFW_KEY_A)) {
				spinTime = Math.max(spinTime - delta, -TIME_TO_MAX_SPIN) as float
			}
			else if (input.keyPressed(GLFW_KEY_D)) {
				spinTime = Math.min(spinTime + delta, TIME_TO_MAX_SPIN) as float
			}
			else if (spinTime > 0f) {
				spinTime = Math.max(spinTime - delta, 0f) as float
			}
			else if (spinTime < 0f) {
				spinTime = Math.min(spinTime + delta, 0f) as float
			}

			if (spinTime) {
				var spin = EasingFunctions.easeInSine(spinTime / TIME_TO_MAX_SPIN as float) * MAX_SPIN_SPEED * delta as float
				logger.debug('Spin time: {} -> spin value: {} ', sprintf('%.2f', spinTime), sprintf('%.2f', spin))
				sprite.rotate(0f, spin, 0f)
			}
		}
	}
}
