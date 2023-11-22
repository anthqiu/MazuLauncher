package xyz.qhurc.mazulauncher

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.launch
import xyz.qhurc.mazulauncher.ui.theme.MazuLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LauncherScreen()
        }
    }
}

val CARD_SIZE = 120.dp
val CARD_PADDING = 8.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }

//    val apps = Array<ApplicationInfo>(3 * getapps.size) { idx -> getapps[idx % getapps.size] }

    val focusedCardIndex = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val CARDS_IN_SCREEN =
        (getScreenWidthInDp(context) / (CARD_SIZE + CARD_PADDING).value - 0.5f).toInt()
    var firstItemIndex = 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusable(true) // 使 Column 可以被聚焦
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown)
                    when (keyEvent.key) {
                        Key.DirectionRight -> {
                            if (focusedCardIndex.value < apps.size - 1) {
                                focusedCardIndex.value += 1
                                if (focusedCardIndex.value - firstItemIndex > CARDS_IN_SCREEN - 2)
                                    firstItemIndex = focusedCardIndex.value - CARDS_IN_SCREEN + 2
                                if (firstItemIndex < 0) firstItemIndex = 0
                                if (firstItemIndex >= apps.size) firstItemIndex = apps.size - 1
                            }
                            Log.d(
                                "Main",
                                "Right ${focusedCardIndex.value} ${apps[focusedCardIndex.value].packageName}"
                            )
                        }

                        Key.DirectionLeft -> {
                            if (focusedCardIndex.value > 0) {
                                focusedCardIndex.value -= 1
                                if (focusedCardIndex.value < firstItemIndex)
                                    firstItemIndex = focusedCardIndex.value
                            }
                            Log.d(
                                "Main",
                                "Left ${focusedCardIndex.value} ${apps[focusedCardIndex.value].packageName}"
                            )
                        }

                        Key.ButtonA, Key.Enter, Key.NumPadEnter -> {
                            Log.d(
                                "Main",
                                "Enter ${focusedCardIndex.value} ${apps[focusedCardIndex.value].packageName}"
                            )
                            startApp(context, apps[focusedCardIndex.value].packageName)
                        }

                        else -> Log.d("Main", keyEvent.toString())
                    }
                true
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(state = listState) {
            item { Spacer(modifier = Modifier.size(10.dp)) }

            itemsIndexed(apps) { index, card ->
                val app = apps[index]
                val appLabel = packageManager.getApplicationLabel(app)
                val appIcon = packageManager.getApplicationIcon(app.packageName)
                AppCard(
                    label = appLabel.toString(),
                    isFocused = index == focusedCardIndex.value,
                    icon = appIcon
                ) {
                    if (index == focusedCardIndex.value)
                        startApp(context, packageName = app.packageName)
                    else
                        focusedCardIndex.value = index
                }

                if (index == firstItemIndex)
                    LaunchedEffect(focusedCardIndex.value) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
            }

            item {
            }
        }
    }
}

@Composable
fun AppCard(label: String, icon: Drawable, isFocused: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isFocused) label else "")
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isFocused) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.surfaceVariant,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            modifier = Modifier
                .padding(CARD_PADDING)
                .width(CARD_SIZE)
                .height(CARD_SIZE)

        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick)
                    .focusable()
            ) {
                Image(
                    bitmap = icon.toBitmap().asImageBitmap(),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}

fun getScreenWidthInDp(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    return (displayMetrics.widthPixels / displayMetrics.density).toInt()
}

fun startApp(context: Context, packageName: String) {
    val packageManager = context.packageManager
    context.startActivity(packageManager.getLaunchIntentForPackage(packageName))
}