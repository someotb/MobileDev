package com.example.calculjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculjc.ui.theme.CalculJCTheme
import com.example.calculjc.ui.theme.Orange
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge()
            CalculJCTheme {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var displayText by remember { mutableStateOf("0") }
    var expressionText by remember { mutableStateOf("") }
    var operatorSelected by remember { mutableStateOf(false) }
    var resultDisplayed by remember { mutableStateOf(false) }
    val context = LocalContext.current

//    fun playSound(context: Context) {
//        val mediaPlayer = MediaPlayer.create(context, R.raw.or)
//        mediaPlayer.start()
//        mediaPlayer.setOnCompletionListener {
//            it.release()
//        }
//    }

    fun onDigitClick(digit: String) {
        if (digit == ",") {
            if (displayText.contains(",")) return

            if (displayText == "0" || operatorSelected || resultDisplayed) {
                displayText = "0,"
                if (expressionText.isEmpty() || expressionText == "0" || operatorSelected || resultDisplayed) {
                    expressionText = "0,"
                } else {
                    expressionText += ","
                }
                operatorSelected = false
                resultDisplayed = false
            } else {
                displayText += ","
                expressionText += ","
            }
            return
        }


        if (resultDisplayed) {
            expressionText = digit
            displayText = digit
            resultDisplayed = false
            operatorSelected = false
        } else if (operatorSelected) {
            displayText = digit
            expressionText += digit
            operatorSelected = false
        } else {
            if (displayText == "0") {
                displayText = digit
                if (expressionText.isEmpty() || expressionText == "0") {
                    expressionText = digit
                } else {
                    expressionText += digit
                }
            } else {
                displayText += digit
                expressionText += digit
            }
        }
    }


    fun onOperatorClick(op: String) {
        if (resultDisplayed) {
            expressionText = displayText
            resultDisplayed = false
        }
        if (expressionText.isNotEmpty() && "+-×÷".contains(expressionText.last())) {
            expressionText = expressionText.dropLast(1) + op
        } else {
            expressionText += op
        }
        operatorSelected = true
    }

    fun onEqualClick() {
        val result = evaluateExpressionWithPrecedence(expressionText)
        if (result == null) {
            displayText = "Ошибка"
        } else {
            displayText = if (result % 1.0 == 0.0) result.toInt().toString() else result.toString()
            expressionText += "=$displayText"
            resultDisplayed = true
        }
    }

    fun onClearClick() {
        displayText = "0"
        expressionText = ""
        resultDisplayed = false
        operatorSelected = false
    }

    fun onToggleSign() {
        if (displayText == "0") return
        displayText = if (displayText.startsWith("-")) {
            displayText.substring(1)
        } else {
            "-$displayText"
        }
    }

    val colorLightGray = ButtonDefaults.buttonColors(
        containerColor = Color.Gray,
        contentColor = Color.White
    )

    val colorDarkGray = ButtonDefaults.buttonColors(
        containerColor = Color.DarkGray,
        contentColor = Color.White
    )

    val colorOrange = ButtonDefaults.buttonColors(
        containerColor = Orange,
        contentColor = Color.White
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (expressionText.isNotEmpty()) {
                    Text(
                        text = expressionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    fontSize = 32.sp,
                    maxLines = 1
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onClearClick() },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorLightGray,
                    shape = CircleShape
                ) {
                    Text("AC", fontSize = 25.sp)
                }
                Button(
                    onClick = { onToggleSign() },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorLightGray,
                    shape = CircleShape
                ) {
                    Text("±", fontSize = 34.sp)
                }
                Button(
                    onClick = { onOperatorClick("%") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorLightGray,
                    shape = CircleShape
                ) {
                    Text("%", fontSize = 34.sp)
                }
                Button(
                    onClick = { onOperatorClick("÷") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorOrange,
                    shape = CircleShape
                ) {
                    Text("÷", fontSize = 34.sp)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onDigitClick("7") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("7", fontSize = 34.sp)
                }
                Button(
                    onClick = { onDigitClick("8") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("8", fontSize = 34.sp)
                }
                Button(
                    onClick = { onDigitClick("9") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("9", fontSize = 34.sp)
                }
                Button(
                    onClick = { onOperatorClick("×") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorOrange,
                    shape = CircleShape
                ) {
                    Text("×", fontSize = 34.sp)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onDigitClick("4") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("4", fontSize = 34.sp)
                }
                Button(
                    onClick = { onDigitClick("5") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("5", fontSize = 34.sp)
                }
                Button(
                    onClick = { onDigitClick("6") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("6", fontSize = 34.sp)
                }
                Button(
                    onClick = { onOperatorClick("-") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorOrange,
                    shape = CircleShape
                ) {
                    Text("-", fontSize = 34.sp)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onDigitClick("1") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("1", fontSize = 34.sp)
                }
                Button(
                    onClick = { onDigitClick("2") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("2", fontSize = 34.sp)
                }
                Button(
                    onClick = { onDigitClick("3") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("3", fontSize = 34.sp)
                }
                Button(
                    onClick = { onOperatorClick("+") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorOrange,
                    shape = CircleShape
                ) {
                    Text("+", fontSize = 34.sp)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
//                        playSound(context)
                              },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("ιllιlι", fontSize = 18.sp)
                }
                Button(
                    onClick = { onDigitClick("0") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text("0", fontSize = 34.sp)
                }
                Button(
                    onClick = { onDigitClick(",") },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorDarkGray,
                    shape = CircleShape
                ) {
                    Text(",", fontSize = 34.sp)
                }
                Button(
                    onClick = { onEqualClick() },
                    modifier = Modifier
                        .background(Color.Black)
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = colorOrange,
                    shape = CircleShape
                ) {
                    Text("=", fontSize = 34.sp)
                }
            }
        }
    }
}

fun tokenize(expr: String): List<String> {
    val tokens = mutableListOf<String>()
    var currentNumber = ""
    for (ch in expr) {
        when {
            ch.isDigit() || ch == '.' -> {
                currentNumber += ch
            }
            "+-×÷".contains(ch) -> {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber)
                    currentNumber = ""
                }
                tokens.add(ch.toString())
            }
            else -> {
                // Пропускаем прочие символы
            }
        }
    }
    if (currentNumber.isNotEmpty()) {
        tokens.add(currentNumber)
    }
    return tokens
}

fun processMulDiv(tokens: List<String>): List<String>? {
    if (tokens.isEmpty()) return emptyList()
    val resultTokens = mutableListOf<String>()
    var currentValue = tokens[0].toDoubleOrNull() ?: return null
    var i = 1
    while (i < tokens.size) {
        val op = tokens[i]
        if (i + 1 >= tokens.size) return null
        val nextNumber = tokens[i + 1].toDoubleOrNull() ?: return null
        when (op) {
            "×" -> {
                currentValue *= nextNumber
                i += 2
            }
            "÷" -> {
                if (nextNumber == 0.0) return null
                currentValue /= nextNumber
                i += 2
            }
            "+", "-" -> {
                resultTokens.add(currentValue.toString())
                resultTokens.add(op)
                currentValue = nextNumber
                i += 2
            }
            else -> return null
        }
    }
    resultTokens.add(currentValue.toString())
    return resultTokens
}

fun processAddSub(tokens: List<String>): Double? {
    if (tokens.isEmpty()) return null
    var currentValue = tokens[0].toDoubleOrNull() ?: return null
    var i = 1
    while (i < tokens.size) {
        val op = tokens[i]
        if (i + 1 >= tokens.size) return null
        val nextNumber = tokens[i + 1].toDoubleOrNull() ?: return null
        currentValue = when (op) {
            "+" -> currentValue + nextNumber
            "-" -> currentValue - nextNumber
            else -> return null
        }
        i += 2
    }
    return currentValue
}

fun evaluateExpressionWithPrecedence(expr: String): Double? {
    val normalizedExpr = expr.replace(",", ".")
    val tokens = tokenize(normalizedExpr)
    if (tokens.isEmpty()) return null
    val afterMulDiv = processMulDiv(tokens) ?: return null
    return processAddSub(afterMulDiv)
}
