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

import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.physics.CollisionSystem
import nz.net.ultraq.redhorizon.engine.scene.SceneUpdateSystem
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.scripts.ScriptSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import static nz.net.ultraq.spintowin.ScopedValues.*

import org.lwjgl.system.Configuration
import picocli.CommandLine
import picocli.CommandLine.Command

/**
 * Entry point for the Spin To Win game.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'spin-to-win')
class SpinToWin implements Runnable {

	static {
		Configuration.STACK_SIZE.set(10240)
	}

	static void main(String[] args) {
		System.exit(new CommandLine(new SpinToWin()).execute(args))
	}

	private Window window
	private Framebuffer framebuffer
	private BasicShader shader
	private ResourceManager resourceManager
	private SpinToWinScene scene

	@Override
	void run() {

		try {
			// Init devices
			window = new OpenGLWindow(640, 480, 'Spin To Win')
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.BLACK)
				.withVSync(true)
			framebuffer = new OpenGLFramebuffer(640, 480)
			shader = new BasicShader()
			var inputEventHandler = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)
			resourceManager = new ResourceManager('nz/net/ultraq/spintowin/assets/')

			ScopedValue
				.where(WINDOW, window)
				.where(RESOURCE_MANAGER, resourceManager)
				.run(() -> {

					// Init scene and systems
					scene = new SpinToWinScene().tap {
						addDebugComponents(window, camera, inputEventHandler)
					}
					var engine = new Engine()
						.addSystem(new InputSystem(inputEventHandler))
						.addSystem(new ScriptSystem(new ScriptEngine('.'), inputEventHandler))
						.addSystem(new CollisionSystem())
						.addSystem(new GraphicsSystem(window, framebuffer, shader))
						.addSystem(new SceneUpdateSystem())
						.withScene(scene)

					// Game loop
					window.show()
					var deltaTimer = new DeltaTimer()
					while (!window.shouldClose()) {
						engine.update(deltaTimer.deltaTime())
						Thread.yield()
					}
				})
		}
		finally {
			scene?.close()
			resourceManager?.close()
			shader?.close()
			framebuffer?.close()
			window?.close()
		}
	}
}
