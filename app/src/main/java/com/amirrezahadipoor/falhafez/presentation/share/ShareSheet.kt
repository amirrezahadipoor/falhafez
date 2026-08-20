package com.amirrezahadipoor.falhafez.presentation.share

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amirrezahadipoor.falhafez.core.designsystem.FalPalette
import com.amirrezahadipoor.falhafez.core.designsystem.FalText
import com.amirrezahadipoor.falhafez.core.theme.FalThemeSpec
import com.amirrezahadipoor.falhafez.domain.model.FalCategory
import com.amirrezahadipoor.falhafez.domain.model.Poem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** A bottom sheet with direct share buttons for every major network. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShareSheet(
    poem: Poem,
    category: FalCategory,
    spec: FalThemeSpec,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var file by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        file = withContext(Dispatchers.Default) {
            runCatching { ShareManager.renderFile(context.applicationContext, poem, category, spec) }
                .getOrNull()
        }
        if (file == null) error = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FalPalette.Navy,
        contentColor = FalPalette.Cream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text("اشتراک‌گذاری فال", style = FalText.heading, color = FalPalette.GoldBright)
            Spacer(Modifier.height(4.dp))
            Text(
                "تصویرِ فال را به پیام‌رسانِ دلخواهت بفرست.",
                style = FalText.caption,
                color = FalPalette.CreamMuted
            )
            Spacer(Modifier.height(20.dp))

            when {
                error -> Text(
                    "در ساختِ تصویر مشکلی پیش آمد.",
                    style = FalText.bodyMuted,
                    color = FalPalette.CreamMuted
                )
                file == null -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = FalPalette.Gold, modifier = Modifier.size(22.dp))
                    Text("در حال آماده‌سازی تصویر…", style = FalText.bodyMuted, color = FalPalette.CreamMuted)
                }
                else -> {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SocialNetwork.entries.forEach { network ->
                            val f = file
                            NetworkButton(network = network) {
                                if (f != null) {
                                    scope.launch { ShareManager.shareFileToApp(context, f, network) }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(22.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryShareAction(
                            label = if (saved) "ذخیره شد ✓" else "ذخیره در گالری",
                            icon = Icons.Outlined.SaveAlt,
                            modifier = Modifier.weight(1f)
                        ) {
                            val f = file ?: return@SecondaryShareAction
                            scope.launch(Dispatchers.IO) {
                                val ok = ShareManager.saveToGallery(context, f)
                                saved = ok
                            }
                        }
                        SecondaryShareAction(
                            label = "بیشتر…",
                            icon = Icons.Outlined.Share,
                            modifier = Modifier.weight(1f)
                        ) {
                            val f = file ?: return@SecondaryShareAction
                            scope.launch { ShareManager.shareFileGeneric(context, f) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkButton(network: SocialNetwork, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(network.brandColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = network.icon,
                contentDescription = network.label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(network.label, style = FalText.caption, color = FalPalette.Cream)
    }
}

@Composable
private fun SecondaryShareAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(FalPalette.NavySoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = FalPalette.Gold, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(label, style = FalText.button, color = FalPalette.Gold)
    }
}
