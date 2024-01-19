package com.sayempro.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sayempro.tictactoe.ui.theme.TicTacToeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameState {
    PLAYER, AI, DRAW, CONT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val playerTurn = remember {
                        mutableStateOf(true)
                    }
                    val moves = remember {
                        mutableStateListOf<Boolean?>(
                            null, null, null, null, null, null, null, null, null
                        )
                    }

                    val games = remember {
                        mutableIntStateOf(0)
                    }
                    val playerWin = remember {
                        mutableIntStateOf(0)
                    }
                    val aiWin = remember {
                        mutableIntStateOf(0)
                    }
                    val gameState = remember { mutableStateOf(checkGameBoard(moves)) }

                    TTTScreen(
                        playerTurn = playerTurn,
                        moves = moves,
                        games = games,
                        gameState = gameState,
                        playerWin = playerWin,
                        aiWin = aiWin
                    )
                }
            }
        }
    }
}

fun checkGameBoard(moves: List<Boolean?>): GameState {
    var state: GameState = GameState.CONT
    var nullcount = 0
    for (i in 0..2) {
        var matchRowPl = 0
        var matchRowAi = 0
        var matchCornerPl = 0
        var matchCornerAi = 0
        var matchColPl = 0
        var matchColAi = 0
        for (j in 0..2) {
            if (moves[i * 3 + j] == true) {
                matchRowPl++
            }
            if (moves[i * 3 + j] == false) {
                matchRowAi++
            }
            if (moves[i + j * 3] == true) {
                matchColPl++
            }
            if (moves[i + j * 3] == false) {
                matchColAi++
            }
            if (moves[i * 3 + j] == null) {
                nullcount++
            }
        }

        if ((moves[0] == true && moves[4] == true && moves[8] == true) || (moves[2] == true && moves[4] == true && moves[6] == true)) {
            matchCornerPl = 3
        }
        if ((moves[0] == false && moves[4] == false && moves[8] == false) || (moves[2] == false && moves[4] == false && moves[6] == false)) {
            matchCornerAi = 3
        }
        if (matchRowPl == 3 || matchCornerPl == 3 || matchColPl == 3) {
            state = GameState.PLAYER
        }
        if (matchRowAi == 3 || matchCornerAi == 3 || matchColAi == 3) {
            state = GameState.AI
        }

    }
    if (nullcount == 0 && state == GameState.CONT) {
        state = GameState.DRAW
    }
    return state
}

@Composable
fun TTTScreen(
    playerTurn: MutableState<Boolean>,
    moves: SnapshotStateList<Boolean?>,
    games: MutableState<Int>,
    gameState: MutableState<GameState>,
    playerWin: MutableIntState,
    aiWin: MutableIntState
) {


    val onTap: (Offset) -> Unit = {
        if (playerTurn.value && gameState.value == GameState.CONT) {
            val x = (it.x / 333).toInt()
            val y = (it.y / 333).toInt()
            val cellIndex = y * 3 + x
            if (moves[cellIndex] == null) {
                moves[cellIndex] = true
                gameState.value = checkGameBoard(moves)
                playerTurn.value = false
            }
        }
    }



    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Tic Tac Toe", fontSize = 30.sp, modifier = Modifier.padding(10.dp))
        Header(playerTurn = playerTurn.value, games = games)
        Board(moves = moves, onTap = onTap)
        //AI moves
        if (!playerTurn.value && gameState.value == GameState.CONT) {
            CircularProgressIndicator(color = Color.Red, modifier = Modifier.padding(16.dp))
            val coroutineScope = rememberCoroutineScope()
            LaunchedEffect(key1 = Unit) {
                coroutineScope.launch {
                    delay(1000)
                    while (true) {
                        val i = Random.nextInt(0, 9)
                        if (moves[i] == null) {
                            moves[i] = false
                            gameState.value = checkGameBoard(moves)
                            playerTurn.value = true
                            break
                        }
                    }
                }
            }
        }
        if (gameState.value != GameState.CONT) {

            val coroutineScope = rememberCoroutineScope()
            LaunchedEffect(key1 = Unit) {
                coroutineScope.launch {
                    delay(1000)
                    when (gameState.value) {
                        GameState.PLAYER -> playerWin.intValue++
                        GameState.AI -> aiWin.intValue++
                        else -> {}
                    }
                    for (i in 0..8) moves[i] = null
                    gameState.value = GameState.CONT
                    games.value++


                }
            }
            Text(
                text = "Game Over! ${
                    if (gameState.value == GameState.PLAYER) {
                        "You won!"
                    } else if (gameState.value == GameState.DRAW) "Draw." else {
                        "You lost."
                    }
                }",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )

//
//
//            AlertDialog(onDismissRequest = {
//
//
////            moves.removeRange(0, 8)
////            moves.addAll(listOf(null, null, null, null, null, null, null, null, null))
//            },
//                confirmButton = {
//                    Button(onClick = { gameState.value = null }) {
//                        Text(text = "Ok")
//                    }
//                },
//                text = { Text(text = "Game Over! ${if (gameState.value == GameState.PLAYER) "You won!" else if (gameState.value == GameState.DRAW) "Draw." else "You lost."}") })

        }
        Footer(playerWin = playerWin, aiWin = aiWin)

    }


}

@Composable
fun Header(playerTurn: Boolean, games: MutableState<Int>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val playerColor = if (playerTurn) Color(0xFF414C8B) else Color.LightGray
        val aiColor = if (playerTurn) Color.LightGray else Color(0xFFE9582B)
        Box(
            modifier = Modifier
                .width(100.dp)
                .background(playerColor)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Player", modifier = Modifier.padding(10.dp)
//                        .align(Alignment.TopCenter)
                )
                Image(
                    painter = painterResource(id = R.drawable.ttc_circle),
                    contentDescription = null,
//                    modifier = Modifier.align(Alignment.BottomCenter),
                    colorFilter = ColorFilter.tint(Color(0xFFFFFFFF))
                )
            }

        }
//        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.width(100.dp)) {
            Text(
                text = "${games.value} games",
                modifier = Modifier.padding(5.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

//        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .width(130.dp)
                .background(aiColor),
//            contentAlignment = Alignment.TopCenter,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Computer", modifier = Modifier.padding(10.dp)
//                        .align(Alignment.TopCenter),
                )
                Image(
                    painter = painterResource(id = R.drawable.ttc_cross), contentDescription = null,
//                    modifier = Modifier.align(Alignment.BottomCenter),
                    colorFilter = ColorFilter.tint(Color(0xFFFFFCFD))
                )
            }

        }
    }
}

@Composable
fun Footer(playerWin: MutableIntState, aiWin: MutableIntState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val playerColor = Color(0xFF414C8B)
        val aiColor = Color(0xFFE9582B)
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(50.dp)
                .background(playerColor)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Player", modifier = Modifier.padding(10.dp)
//                        .align(Alignment.TopCenter)
                )
                Image(
                    painter = painterResource(id = R.drawable.ttc_circle),
                    contentDescription = null,
//                    modifier = Modifier.align(Alignment.BottomCenter),
                    colorFilter = ColorFilter.tint(Color(0xFFFFFFFF))
                )
                Text(
                    text = "${playerWin.value}", modifier = Modifier.padding(10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(50.dp))

        Box(
            modifier = Modifier
                .width(150.dp)
                .height(50.dp)
                .background(aiColor),
//            contentAlignment = Alignment.TopCenter,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Computer", modifier = Modifier.padding(10.dp)
//                        .align(Alignment.TopCenter),
                )
                Image(
                    painter = painterResource(id = R.drawable.ttc_cross),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFFFFFCFD))
                )
                Text(
                    text = "${aiWin.value}", modifier = Modifier.padding(10.dp)
                )
            }

        }
    }
}

@Composable
fun Board(moves: List<Boolean?>, onTap: (Offset) -> Unit) {
    Box(modifier = Modifier
        .aspectRatio(1f)
        .padding(32.dp)
        .background(Color.LightGray)
        .pointerInput(Unit) {
            detectTapGestures(onTap = onTap)
        }) {
        Column(
            modifier = Modifier.fillMaxSize(1f), verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth(1f)
                    .background(Color.Black)
            ) {}
            Row(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth(1f)
                    .background(Color.Black)
            ) {}
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxSize(1f)) {
            Column(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(1f)
                    .background(Color.Black)
            ) {

            }
            Column(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(1f)
                    .background(Color.Black)
            ) {

            }
        }
        Column(modifier = Modifier.fillMaxSize(1f)) {
            for (i in 0..2) {
                Row(modifier = Modifier.weight(1f)) {
                    for (j in 0..2) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            GetIconFromMove(move = moves[i * 3 + j])
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun GetIconFromMove(move: Boolean?) {
    when (move) {
        //Player
        true -> Image(
            painter = painterResource(id = R.drawable.ttc_circle),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(1f),
            colorFilter = ColorFilter.tint(Color(0xFF414C8B))
        )
        //Computer
        false -> Image(
            painter = painterResource(id = R.drawable.ttc_cross),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(1f),
            colorFilter = ColorFilter.tint(Color(0xFFE9582B))
        )

        null -> Image(
            painter = painterResource(id = R.drawable.ttc_null),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(1f),
            colorFilter = ColorFilter.tint(Color(0xD3CA90C6))
        )
    }
}