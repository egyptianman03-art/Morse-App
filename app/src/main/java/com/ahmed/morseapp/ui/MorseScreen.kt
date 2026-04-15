package com.ahmed.morseapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmed.morseapp.converter.MorseConverter
import com.ahmed.morseapp.viewmodel.ConversionMode
import com.ahmed.morseapp.viewmodel.MorseUiState
import com.ahmed.morseapp.viewmodel.MorseViewModel

// ===================== الألوان =====================
val DarkBackground = Color(0xFF0D0D1A)
val DarkSurface = Color(0xFF1A1A2E)
val DarkCard = Color(0xFF16213E)
val AccentGold = Color(0xFFFFD700)
val AccentCyan = Color(0xFF00E5FF)
val AccentPurple = Color(0xFF9C27B0)
val TextPrimary = Color(0xFFE0E0E0)
val TextSecondary = Color(0xFF9E9E9E)
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFF44336)
val GradientStart = Color(0xFF6A11CB)
val GradientEnd = Color(0xFF2575FC)

@Composable
fun MorseScreen(viewModel: MorseViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            // ===== العنوان =====
            HeaderSection()

            Spacer(modifier = Modifier.height(16.dp))

            // ===== اختيار اللغة =====
            LanguageSelector(
                selectedLanguage = uiState.selectedLanguage,
                onLanguageSelected = viewModel::onLanguageChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== حقل الإدخال =====
            InputSection(
                inputText = uiState.inputText,
                onInputChanged = viewModel::onInputChanged,
                conversionMode = uiState.conversionMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== أزرار التحويل =====
            ConversionButtons(
                onTextToMorse = viewModel::convertTextToMorse,
                onMorseToText = viewModel::convertMorseToText,
                onClear = viewModel::clearAll
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== حقل النتيجة =====
            OutputSection(
                outputText = uiState.outputText,
                onCopy = { copyToClipboard(context, uiState.outputText) },
                onShare = { shareText(context, uiState.outputText) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== تحكم الصوت =====
            SoundControlSection(
                uiState = uiState,
                onPlayToggle = viewModel::playMorseSound,
                onVibrateToggle = viewModel::onVibrateToggled,
                onSpeedChanged = viewModel::onSpeedChanged,
                onVolumeChanged = viewModel::onVolumeChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== رسالة الحالة =====
            if (uiState.statusMessage.isNotEmpty()) {
                StatusMessage(
                    message = uiState.statusMessage,
                    isError = uiState.isError
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ===== جدول المرجع =====
            MorseReferenceSection()

            Spacer(modifier = Modifier.height(16.dp))

            // ===== تذييل الصفحة =====
            FooterSection()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ===================== العنوان =====================
@Composable
fun HeaderSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // اسم المطور
        Text(
            text = "Ahmed El-aref",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                brush = Brush.horizontalGradient(
                    colors = listOf(AccentGold, AccentCyan, AccentGold)
                )
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // خط زخرفي
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, AccentGold, Color.Transparent)
                    )
                )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // عنوان التطبيق
        Text(
            text = "محوّل شفرة مورس",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontSize = 22.sp
            )
        )

        Text(
            text = "عربي • إنجليزي • رموز",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AccentCyan,
                letterSpacing = 2.sp
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ===================== اختيار اللغة =====================
@Composable
fun LanguageSelector(
    selectedLanguage: MorseConverter.Language,
    onLanguageSelected: (MorseConverter.Language) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LanguageChip(
                label = "تلقائي",
                icon = "🌐",
                selected = selectedLanguage == MorseConverter.Language.AUTO,
                onClick = { onLanguageSelected(MorseConverter.Language.AUTO) }
            )
            LanguageChip(
                label = "عربي",
                icon = "🇸🇦",
                selected = selectedLanguage == MorseConverter.Language.ARABIC,
                onClick = { onLanguageSelected(MorseConverter.Language.ARABIC) }
            )
            LanguageChip(
                label = "English",
                icon = "🇬🇧",
                selected = selectedLanguage == MorseConverter.Language.ENGLISH,
                onClick = { onLanguageSelected(MorseConverter.Language.ENGLISH) }
            )
        }
    }
}

@Composable
fun LanguageChip(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) AccentPurple else DarkSurface
    val textColor = if (selected) Color.White else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = label,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

// ===================== حقل الإدخال =====================
@Composable
fun InputSection(
    inputText: String,
    onInputChanged: (String) -> Unit,
    conversionMode: ConversionMode
) {
    val hint = if (conversionMode == ConversionMode.TEXT_TO_MORSE)
        "أدخل النص هنا (عربي أو إنجليزي أو رموز)..."
    else
        "أدخل شفرة مورس هنا (. - /) ..."

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "📝 النص المدخل",
                color = AccentCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(text = hint, color = TextSecondary, fontSize = 14.sp)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = DarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentCyan
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 8
            )
        }
    }
}

// ===================== أزرار التحويل =====================
@Composable
fun ConversionButtons(
    onTextToMorse: () -> Unit,
    onMorseToText: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // زر تحويل إلى مورس
        GradientButton(
            text = "نص ← مورس 🔐",
            modifier = Modifier.weight(1f),
            gradientColors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
            onClick = onTextToMorse
        )

        // زر تحويل مورس إلى نص
        GradientButton(
            text = "مورس ← نص 🔓",
            modifier = Modifier.weight(1f),
            gradientColors = listOf(Color(0xFF00C9FF), Color(0xFF92FE9D)),
            onClick = onMorseToText
        )

        // زر المسح
        IconButton(
            onClick = onClear,
            modifier = Modifier
                .clip(CircleShape)
                .background(ErrorRed.copy(alpha = 0.15f))
                .size(52.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "مسح",
                tint = ErrorRed,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

// ===================== حقل النتيجة =====================
@Composable
fun OutputSection(
    outputText: String,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📡 النتيجة",
                    color = AccentGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // زر النسخ
                    ActionIconButton(
                        icon = Icons.Default.ContentCopy,
                        tint = AccentCyan,
                        onClick = onCopy,
                        description = "نسخ"
                    )
                    // زر المشاركة
                    ActionIconButton(
                        icon = Icons.Default.Share,
                        tint = AccentGold,
                        onClick = onShare,
                        description = "مشاركة"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = outputText,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(150))
                },
                label = "output_animation"
            ) { text ->
                if (text.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ستظهر النتيجة هنا...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = text,
                            color = AccentGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    description: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f))
            .size(38.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ===================== تحكم الصوت =====================
@Composable
fun SoundControlSection(
    uiState: MorseUiState,
    onPlayToggle: () -> Unit,
    onVibrateToggle: (Boolean) -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔊 التحكم في الصوت",
                color = AccentPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // زر التشغيل والإيقاف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val playScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(550),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "play_pulse"
                )

                Box(
                    modifier = Modifier
                        .scale(if (uiState.isPlaying) playScale else 1f)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.isPlaying)
                                Brush.radialGradient(listOf(ErrorRed, ErrorRed.copy(alpha = 0.6f)))
                            else
                                Brush.radialGradient(listOf(AccentPurple, AccentPurple.copy(alpha = 0.6f)))
                        )
                        .clickable(onClick = onPlayToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "إيقاف" else "تشغيل",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    // التحكم في الاهتزاز
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📳 اهتزاز",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Switch(
                            checked = uiState.vibrateEnabled,
                            onCheckedChange = onVibrateToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentPurple,
                                checkedTrackColor = AccentPurple.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // شريط السرعة
            Text(
                text = "⚡ السرعة: ${uiState.soundSpeed.toInt()} كلمة/دقيقة",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Slider(
                value = uiState.soundSpeed,
                onValueChange = onSpeedChanged,
                valueRange = 5f..30f,
                steps = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AccentCyan,
                    activeTrackColor = AccentCyan,
                    inactiveTrackColor = DarkSurface
                )
            )

            // شريط مستوى الصوت
            Text(
                text = "🔈 مستوى الصوت: ${(uiState.soundVolume * 100).toInt()}%",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Slider(
                value = uiState.soundVolume,
                onValueChange = onVolumeChanged,
                valueRange = 0.1f..1f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AccentGold,
                    activeTrackColor = AccentGold,
                    inactiveTrackColor = DarkSurface
                )
            )
        }
    }
}

// ===================== رسالة الحالة =====================
@Composable
fun StatusMessage(message: String, isError: Boolean) {
    val bgColor = if (isError) ErrorRed.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f)
    val borderColor = if (isError) ErrorRed else SuccessGreen

    AnimatedVisibility(
        visible = message.isNotEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Text(
                text = message,
                color = if (isError) ErrorRed else SuccessGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }
}

// ===================== جدول المرجع =====================
@Composable
fun MorseReferenceSection() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📖 جدول شفرة مورس",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AccentCyan
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // === الحروف العربية ===
                    ReferenceTableHeader("الحروف العربية 🇸🇦")
                    ReferenceGrid(MorseConverter.arabicReference)

                    Spacer(modifier = Modifier.height(8.dp))

                    // === الحروف الإنجليزية ===
                    ReferenceTableHeader("الحروف الإنجليزية 🇬🇧")
                    ReferenceGrid(MorseConverter.englishReference)

                    Spacer(modifier = Modifier.height(8.dp))

                    // === الأرقام ===
                    ReferenceTableHeader("الأرقام 🔢")
                    ReferenceGrid(MorseConverter.numbersReference)

                    Spacer(modifier = Modifier.height(8.dp))

                    // === الرموز ===
                    ReferenceTableHeader("الرموز ⌨️")
                    ReferenceGrid(MorseConverter.symbolsReference)
                }
            }
        }
    }
}

@Composable
fun ReferenceTableHeader(title: String) {
    Text(
        text = title,
        color = AccentGold,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
fun ReferenceGrid(items: List<Pair<String, String>>) {
    val chunked = items.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { (char, code) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = char,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = code,
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                // Fill empty cells
                repeat(4 - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ===================== التذييل =====================
@Composable
fun FooterSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, AccentGold.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "جميع الحقوق محفوظة لـ Ahmed El-aref ©2025",
            color = AccentGold.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "تطبيق يعمل بدون إنترنت 🛡️",
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ===================== وظائف مساعدة =====================
fun copyToClipboard(context: Context, text: String) {
    if (text.isEmpty()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Morse Code", text)
    clipboard.setPrimaryClip(clip)
}

fun shareText(context: Context, text: String) {
    if (text.isEmpty()) return
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_TITLE, "شفرة مورس - Ahmed El-aref")
    }
    context.startActivity(Intent.createChooser(shareIntent, "مشاركة عبر"))
}
