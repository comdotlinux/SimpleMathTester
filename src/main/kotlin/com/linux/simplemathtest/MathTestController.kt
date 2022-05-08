package com.linux.simplemathtest

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.system.exitProcess


enum class Operation(val notation: String) {
    ADDITION("+") {
        override fun calculate(lhs: Int, rhs: Int): Int = lhs + rhs
    },
    SUBTRACTION("-") {
        override fun calculate(lhs: Int, rhs: Int): Int = lhs - rhs
    },
    MULTIPLICATION("*") {
        override fun calculate(lhs: Int, rhs: Int): Int = lhs * rhs
    },
    DIVISION("/") {
        override fun calculate(lhs: Int, rhs: Int): Int = lhs / rhs
    };

    abstract fun calculate(lhs: Int, rhs: Int): Int
}

data class ValueLimits(var min: Int = 0, var max: Int = 100)
data class AllowedOperations(var addition: Boolean = true, var subtraction: Boolean = false, var multiplication: Boolean = false, var division: Boolean = false) {
    fun randomOperation(lhs: Int, rhs: Int): OperationWithTimedResult {
        val operations: MutableList<Operation> = mutableListOf()
        if (addition) operations.add(Operation.ADDITION)
        if (subtraction) operations.add(Operation.SUBTRACTION)
        if (multiplication) operations.add(Operation.MULTIPLICATION)
        if (division) operations.add(Operation.DIVISION)
        val operation = operations[Random.nextInt(operations.size)]
        return OperationWithTimedResult(lhs.toString(), operation.notation, rhs.toString(), operation.calculate(lhs, rhs).toString())
    }
}

data class OperationWithTimedResult(val lhs: String, val operation: String, val rhs: String, val result: String, private val startTime: Long = System.currentTimeMillis()) {
    fun tookTime(): Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
    fun prettify(inputValue: String): String = "$lhs $operation $rhs = $result took ${tookTime()}s to enter $inputValue"
}

class MathTestController {

    @FXML
    lateinit var minValue: TextField

    @FXML
    lateinit var maxValue: TextField

    @FXML
    lateinit var revealAnswer: Button

    @FXML
    lateinit var lhs: Label

    @FXML
    lateinit var rhs: Label

    @FXML
    lateinit var operation: Label

    @FXML
    lateinit var answer: Label

    @FXML
    lateinit var answerInput: TextField

    @FXML
    lateinit var duration: Label

    @FXML
    lateinit var correctHistory: ListView<Any>

    @FXML
    lateinit var incorrectHistory: ListView<Any>

    private val allowedOperations = AllowedOperations()
    private val limits = ValueLimits()
    private var generateNextOnCheck = false

    fun onAdditionChecked(actionEvent: ActionEvent) {
        allowedOperations.addition = (actionEvent.target as CheckBox).isSelected
    }

    fun onMinValueChanged(actionEvent: ActionEvent) {
        limits.min = (actionEvent.target as TextField).text.toIntOrNull() ?: 0
        minValue.text = limits.min.toString()
    }

    fun onSubtractionChecked(actionEvent: ActionEvent) {
        allowedOperations.subtraction = (actionEvent.target as CheckBox).isSelected
    }

    fun onMultiplicationChecked(actionEvent: ActionEvent) {
        allowedOperations.multiplication = (actionEvent.target as CheckBox).isSelected
    }

    fun onDivisionChecked(actionEvent: ActionEvent) {
        allowedOperations.division = (actionEvent.target as CheckBox).isSelected
    }

    fun onMaxValueChanged(actionEvent: ActionEvent) {
        limits.max = (actionEvent.target as TextField).text.toIntOrNull() ?: 100
        maxValue.text = limits.max.toString()
    }


    private lateinit var currentOperation: OperationWithTimedResult

    fun generate() {
        currentOperation = allowedOperations.randomOperation(Random.nextInt(limits.min, limits.max), Random.nextInt(limits.min, limits.max))
        lhs.text = currentOperation.lhs
        rhs.text = currentOperation.rhs
        operation.text = currentOperation.operation
        answer.text = currentOperation.result
        answerInput.text = ""
        answer.isVisible = false
        duration.isVisible = false
    }

    fun onRevealAnswer() {
        if (generateNextOnCheck) {
            revealAnswer.onAction = EventHandler { sleep(1000); generate() }
        }
        answer.isVisible = true
        duration.isVisible = true
        duration.text = "took ${currentOperation.tookTime()}s"
        if (answerInput.text == currentOperation.result) {
            answer.textFill = javafx.scene.paint.Color.GREEN
            duration.textFill = javafx.scene.paint.Color.GREEN
            correctHistory.items.add(Label(currentOperation.prettify(answerInput.text)))
        } else {
            answer.textFill = javafx.scene.paint.Color.RED
            duration.textFill = javafx.scene.paint.Color.RED
            incorrectHistory.items.add(Label(currentOperation.prettify(answerInput.text)))
        }


    }

    fun onGenerateNextOnCheck(actionEvent: ActionEvent) {
        generateNextOnCheck = (actionEvent.target as CheckBox).isSelected
    }

    fun onQuit() {
        exitProcess(0)
    }
}
