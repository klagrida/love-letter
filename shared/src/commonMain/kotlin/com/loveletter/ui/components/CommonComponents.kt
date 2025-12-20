package com.loveletter.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveletter.ui.theme.LoveRed
import com.loveletter.ui.theme.RoyalGold

@Composable
fun LoveLetterButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSecondary: Boolean = false
) {
    if (isSecondary) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LoveRed,
                contentColor = Color.White
            )
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LoveLetterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp)
        )
        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun MessageBanner(
    message: String,
    modifier: Modifier = Modifier,
    type: MessageType = MessageType.INFO,
    onDismiss: (() -> Unit)? = null
) {
    val backgroundColor = when (type) {
        MessageType.SUCCESS -> Color(0xFF4CAF50)
        MessageType.ERROR -> MaterialTheme.colorScheme.error
        MessageType.WARNING -> Color(0xFFFFA000)
        MessageType.INFO -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (type) {
                        MessageType.SUCCESS -> "✓"
                        MessageType.ERROR -> "✕"
                        MessageType.WARNING -> "⚠"
                        MessageType.INFO -> "ℹ"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", color = Color.White)
                }
            }
        }
    }
}

enum class MessageType {
    SUCCESS, ERROR, WARNING, INFO
}

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = LoveRed)
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    emoji: String = "📭",
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 48.sp
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        action?.invoke()
    }
}

@Composable
fun GameHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 24.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "💌", fontSize = 24.sp)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LoveRed
                    )
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WinnerBanner(
    winnerName: String,
    isGameWinner: Boolean,
    modifier: Modifier = Modifier,
    onAction: () -> Unit,
    actionLabel: String = if (isGameWinner) "New Game" else "Next Round"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGameWinner) RoyalGold else Color(0xFF4CAF50)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isGameWinner) "🏆" else "👑",
                fontSize = 48.sp
            )
            Text(
                text = if (isGameWinner) {
                    "$winnerName Wins the Game!"
                } else {
                    "$winnerName Wins the Round!"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            LoveLetterButton(
                text = actionLabel,
                onClick = onAction
            )
        }
    }
}

@Composable
fun DeckCounter(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🃏", fontSize = 16.sp)
        Text(
            text = "$count cards",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
