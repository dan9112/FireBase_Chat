package ru.lord.firebase_chat.presentation.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import ru.lord.firebase_chat.R
import ru.lord.firebase_chat.getUriById
import ru.lord.firebase_chat.presentation.sign_in.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userData: UserData?,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = userData?.userName ?: "",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    AsyncImage(
                        model = userData?.profilePictureUrl ?: LocalContext.current
                            .resources
                            .getUriById(R.drawable.ic_launcher_foreground),
                        contentDescription = "Profile picture",
                        modifier = Modifier.clip(CircleShape),
                        contentScale = ContentScale.Inside
                    )
                },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text(text = stringResource(id = R.string.sign_out))
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = it)
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 5.dp)
            ) {
                val (backgroundImage, messages, inputRow) = createRefs()
                Image(
                    painter = painterResource(id = R.drawable.chinese_dragon),
                    contentDescription = "Background image",
                    modifier = Modifier
                        .constrainAs(backgroundImage) {
                            linkTo(
                                top = parent.top,
                                bottom = parent.bottom,
                                start = parent.start,
                                end = parent.end
                            )
                            width = Dimension.fillToConstraints
                            height = Dimension.fillToConstraints
                        },
                    contentScale = ContentScale.Inside,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
                    alpha = 0.35f
                )
                Row(
                    modifier = Modifier
                        .constrainAs(inputRow) {
                            linkTo(
                                start = parent.start,
                                end = parent.end
                            )
                            bottom.linkTo(parent.bottom)
                            width = Dimension.matchParent
                            height = Dimension.wrapContent
                        }
                        .background(color = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    var value by remember { mutableStateOf("") }
                    TextField(
                        value = value,
                        onValueChange = { newText ->
                            value = newText
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(text = stringResource(id = R.string.message))
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        singleLine = false,
                        maxLines = 4,
                        colors = TextFieldDefaults.textFieldColors(
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    Button(
                        onClick = { /*TODO*/ },
                        enabled = value.isNotBlank(),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        )
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_menu_send),
                            contentDescription = "Send image",
                            modifier = Modifier.wrapContentSize(),
                            colorFilter = if (value.isNotBlank()) ColorFilter.tint(
                                MaterialTheme.colorScheme.primary,
                                blendMode = BlendMode.SrcAtop
                            ) else null
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier.constrainAs(messages) {
                        linkTo(
                            top = parent.top,
                            bottom = inputRow.top,
                            start = parent.start,
                            end = parent.end
                        )
                    }
                ) {

                }
            }
        }
    }
}
