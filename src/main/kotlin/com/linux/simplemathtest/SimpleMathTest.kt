package com.linux.simplemathtest

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class SimpleMathTest : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(SimpleMathTest::class.java.getResource("simple-math-test.fxml"))
        val scene = Scene(fxmlLoader.load(), 650.0, 400.0)
        stage.title = "Simple Maths Tester!"
        stage.scene = scene
        stage.isResizable = false
        stage.show()
    }
}

fun main() {
    Application.launch(SimpleMathTest::class.java)
}
