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

/**
 * The player object in the scene.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> {

	/**
	 * Constructor, creates a new player object.
	 */
	Player() {

		var resourceManager = RESOURCE_MANAGER.get()
		addChild(new Sprite(resourceManager.loadImage('ship_E.png')))
			.scale(0.5f)
		addChild(new ScriptNode(PlayerScript))
	}

	/**
	 * Script of player behaviours.
	 */
	static class PlayerScript extends Script<Player> {

		private Camera camera

		// Movement and rotation
		private Vector2f positionXY = new Vector2f()
		private Vector2f worldCursorPosition = new Vector2f()
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
				positionXY.set(node.position)
				worldCursorPosition
					.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
					.sub(positionXY, headingToCursor)
				var lastHeading = heading
				heading = headingToCursor.angle(Vector2f.UP)
				node.rotate(0f, 0f, lastHeading - heading as float)
			}
		}
	}
}
