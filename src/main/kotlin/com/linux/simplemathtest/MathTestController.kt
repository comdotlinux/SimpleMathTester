package com.linux.simplemathtest

import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.system.exitProcess


enum class Operation(val notation: String) {
    ADDITION("+") {
        override fun calculate(lhs: Int, rhs: Int): Int = lhs + rhs
        override fun isValid(lhs: Int, rhs: Int, maxResult: Int): Boolean = calculate(lhs, rhs) <= maxResult
    },
    SUBTRACTION("-") {
        override fun calculate(lhs: Int, rhs: Int): Int = lhs - rhs
        override fun isValid(lhs: Int, rhs: Int, maxResult: Int): Boolean = lhs >= rhs && calculate(lhs, rhs) >= 0
    },
    MULTIPLICATION("*") {
        override fun calculate(lhs: Int, rhs: Int): Int = lhs * rhs
        override fun isValid(lhs: Int, rhs: Int, maxResult: Int): Boolean = calculate(lhs, rhs) <= maxResult
    },
    DIVISION("/") {
        override fun calculate(lhs: Int, rhs: Int): Int = lhs / rhs
        override fun isValid(lhs: Int, rhs: Int, maxResult: Int): Boolean = lhs % rhs == 0
    };

    abstract fun calculate(lhs: Int, rhs: Int): Int
    abstract fun isValid(lhs: Int, rhs: Int, maxResult: Int): Boolean
}

data class ValueLimits(var min: Int = 0, var max: Int = 100)
data class AllowedOperations(var addition: Boolean = true, var subtraction: Boolean = false, var multiplication: Boolean = false, var division: Boolean = false) {
    fun randomOperation(limits: ValueLimits): OperationWithTimedResult {
        val operations: MutableList<Operation> = mutableListOf()
        if (addition) operations.add(Operation.ADDITION)
        if (subtraction) operations.add(Operation.SUBTRACTION)
        if (multiplication) operations.add(Operation.MULTIPLICATION)
        if (division) operations.add(Operation.DIVISION)

        var operation: Operation
        var lhs: Int
        var rhs: Int
        do {
            operation = operations[Random.nextInt(operations.size)]
            lhs = Random.nextInt(limits.min, limits.max)
            rhs = Random.nextInt(limits.min, limits.max)
        } while (!operation.isValid(lhs, rhs, limits.max))

        return OperationWithTimedResult(lhs.toString(), operation.notation, rhs.toString(), operation.calculate(lhs, rhs).toString())
    }
}

data class OperationWithTimedResult(val lhs: String, val operation: String, val rhs: String, val result: String, private val startTime: Long = System.currentTimeMillis()) {
    fun tookTime(): Long = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
    fun prettify(inputValue: String): String = "$lhs $operation $rhs = $result took ${tookTime()}s to enter $inputValue"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OperationWithTimedResult

        if (lhs != other.lhs) return false
        if (operation != other.operation) return false
        if (rhs != other.rhs) return false
        if (result != other.result) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = lhs.hashCode()
        result1 = 31 * result1 + operation.hashCode()
        result1 = 31 * result1 + rhs.hashCode()
        result1 = 31 * result1 + result.hashCode()
        return result1
    }


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
        currentOperation = allowedOperations.randomOperation(limits)
        lhs.text = currentOperation.lhs
        rhs.text = currentOperation.rhs
        operation.text = currentOperation.operation
        answer.text = currentOperation.result
        answerInput.text = ""
        answer.isVisible = false
        duration.isVisible = false

        minValue.text = limits.min.toString()
        maxValue.text = limits.max.toString()
    }

    fun onRevealAnswer() {
        answer.isVisible = true
        duration.isVisible = true
        duration.text = "took ${currentOperation.tookTime()}s"

        if (answerInput.text == currentOperation.result) {
            answer.textFill = javafx.scene.paint.Color.GREEN
            duration.textFill = javafx.scene.paint.Color.GREEN
            correctHistory.items.addIfNotExists(Label(currentOperation.prettify(answerInput.text)))
        } else {
            answer.textFill = javafx.scene.paint.Color.RED
            duration.textFill = javafx.scene.paint.Color.RED
            incorrectHistory.items.addIfNotExists(Label(currentOperation.prettify(answerInput.text)))
        }
    }

    fun ObservableList<Any>.addIfNotExists(label: Label) {
        if (filterIsInstance<Label>().any { it.text == label.text }.not()) {
            add(label)
        }
    }

    fun onQuit() {
        exitProcess(0)
    }
}
