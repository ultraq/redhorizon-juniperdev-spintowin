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
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.spintowin.ScopedValues.RESOURCE_MANAGER

import org.joml.Vector2f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The player object in the scene.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> {

	private static final Logger logger = LoggerFactory.getLogger(Player)

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

		// Movement and rotation
		private Vector3f unprojectResult = new Vector3f()
		private Vector2f headingToCursor = new Vector2f()
		private float heading = 0f

		@Override
		void init() {

			camera = node.scene.findByType(Camera)
		}

		@Override
		void update(float delta) {

			// Update rotation so the sprite will appear to look at the cursor
			var cursorPosition = input.cursorPosition()
			if (cursorPosition) {
				headingToCursor.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
				var lastHeading = heading
				heading = headingToCursor.angle(Vector2f.UP)
				node.rotate(0f, 0f, lastHeading - heading as float)
			}
		}
	}
}
