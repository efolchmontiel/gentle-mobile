package com.gentlemanprogramming.gentlemobile

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.provider.OpenableColumns
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.gentlemanprogramming.gentlemobile.notifications.AgentAlertTracker
import com.gentlemanprogramming.gentlemobile.notifications.ConnectionService
import com.gentlemanprogramming.gentlemobile.notifications.ForegroundHub
import com.gentlemanprogramming.gentlemobile.notifications.ForegroundServiceGate
import com.gentlemanprogramming.gentlemobile.notifications.TrackedAgent
import com.gentlemanprogramming.gentlemobile.notifications.ensureChannels
import com.gentlemanprogramming.gentlemobile.notifications.postAgentAlert
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.gentlemanprogramming.gentlemobile.supervision.ForegroundAction
import com.gentlemanprogramming.gentlemobile.supervision.PollTracker
import com.gentlemanprogramming.gentlemobile.supervision.SupervisionMachine
import com.gentlemanprogramming.gentlemobile.supervision.SupervisionState
import com.gentlemanprogramming.gentlemobile.supervision.livenessLabel
import com.gentlemanprogramming.gentlemobile.core.SurfaceDto
import com.gentlemanprogramming.gentlemobile.core.acceptHostKey
import com.gentlemanprogramming.gentlemobile.core.AgentDto
import com.gentlemanprogramming.gentlemobile.core.AgentStatusDto
import com.gentlemanprogramming.gentlemobile.core.CompanionConnection
import com.gentlemanprogramming.gentlemobile.core.CompanionException
import com.gentlemanprogramming.gentlemobile.core.ConnectOutcome
import com.gentlemanprogramming.gentlemobile.core.DiskDto
import com.gentlemanprogramming.gentlemobile.core.FolderEntryDto
import com.gentlemanprogramming.gentlemobile.core.KnownHostDto
import com.gentlemanprogramming.gentlemobile.core.HostProfileDto
import com.gentlemanprogramming.gentlemobile.core.PaneDto
import com.gentlemanprogramming.gentlemobile.core.PaneReadDto
import com.gentlemanprogramming.gentlemobile.core.SnapshotDto
import com.gentlemanprogramming.gentlemobile.core.SurfaceSizeDto
import com.gentlemanprogramming.gentlemobile.core.TabDto
import com.gentlemanprogramming.gentlemobile.core.TerminalSession
import com.gentlemanprogramming.gentlemobile.core.UploadProgressListener
import com.gentlemanprogramming.gentlemobile.core.composePrompt as coreComposePrompt
import com.gentlemanprogramming.gentlemobile.core.checkDeviceKeyRegistered
import com.gentlemanprogramming.gentlemobile.core.planUpload as corePlanUpload
import com.gentlemanprogramming.gentlemobile.core.connect as coreConnect
import com.gentlemanprogramming.gentlemobile.core.defaultSessionsDir
import com.gentlemanprogramming.gentlemobile.core.forgetHost
import com.gentlemanprogramming.gentlemobile.core.listKnownHosts
import com.gentlemanprogramming.gentlemobile.core.refreshLease
import com.gentlemanprogramming.gentlemobile.core.releaseLease
import com.gentlemanprogramming.gentlemobile.core.resumeSession as coreResumeSession
import com.gentlemanprogramming.gentlemobile.core.startSessionInDir as coreStartSessionInDir
import com.gentlemanprogramming.gentlemobile.core.startTerminal as coreStartTerminal
import com.gentlemanprogramming.gentlemobile.ui.approval.ApprovalBoardModel
import com.gentlemanprogramming.gentlemobile.ui.approval.ApprovalGate
import com.gentlemanprogramming.gentlemobile.ui.approval.ApprovalHold
import com.gentlemanprogramming.gentlemobile.ui.approval.ApprovalParser
import com.gentlemanprogramming.gentlemobile.ui.approval.ApprovalRead
import com.gentlemanprogramming.gentlemobile.ui.approval.ApprovalRisk
import com.gentlemanprogramming.gentlemobile.ui.approval.ApprovalWaitTracker
import com.gentlemanprogramming.gentlemobile.ui.sessions.HeldLease
import com.gentlemanprogramming.gentlemobile.ui.AgentActivity
import com.gentlemanprogramming.gentlemobile.ui.AgentRowModel
import com.gentlemanprogramming.gentlemobile.R
import com.gentlemanprogramming.gentlemobile.ui.locale.AppLanguage
import com.gentlemanprogramming.gentlemobile.ui.locale.AppLocale
import com.gentlemanprogramming.gentlemobile.ui.locale.LocaleStore
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.gentlemanprogramming.gentlemobile.ui.sessions.ResumedSessionScreen
import com.gentlemanprogramming.gentlemobile.ui.workspaces.WorkspacesModel
import com.gentlemanprogramming.gentlemobile.ui.workspaces.WorkspacesScreen
import com.gentlemanprogramming.gentlemobile.ui.hosts.HostsModel
import com.gentlemanprogramming.gentlemobile.ui.hosts.HostDeletion
import com.gentlemanprogramming.gentlemobile.ui.hosts.HostMenuEntry
import com.gentlemanprogramming.gentlemobile.ui.hosts.HostMenuModel
import com.gentlemanprogramming.gentlemobile.ui.hosts.HostRowModel
import com.gentlemanprogramming.gentlemobile.ui.hosts.HostScreenModel
import com.gentlemanprogramming.gentlemobile.ui.hosts.HostScreenState
import com.gentlemanprogramming.gentlemobile.ui.hosts.HostRowState
import com.gentlemanprogramming.gentlemobile.ui.sessions.SessionsConfig
import com.gentlemanprogramming.gentlemobile.ui.sessions.SessionsModel
import com.gentlemanprogramming.gentlemobile.ui.sessions.SessionsScreen
import com.gentlemanprogramming.gentlemobile.ui.sessions.StartSessionScreen
import com.gentlemanprogramming.gentlemobile.identity.DeviceIdentity
import com.gentlemanprogramming.gentlemobile.identity.DeviceKeyFingerprint
import com.gentlemanprogramming.gentlemobile.pair.ConnectValidator
import com.gentlemanprogramming.gentlemobile.pair.FingerprintCheck
import com.gentlemanprogramming.gentlemobile.pair.HostStore
import com.gentlemanprogramming.gentlemobile.pair.HostsStore
import com.gentlemanprogramming.gentlemobile.pair.KnownHostsFile
import com.gentlemanprogramming.gentlemobile.pair.PairClient
import com.gentlemanprogramming.gentlemobile.pair.PairLink
import com.gentlemanprogramming.gentlemobile.pair.PairLinkResult
import com.gentlemanprogramming.gentlemobile.pair.PairParams
import com.gentlemanprogramming.gentlemobile.pair.QrScannerScreen
import com.gentlemanprogramming.gentlemobile.pair.SavedHost
import com.gentlemanprogramming.gentlemobile.pair.SavedHostEntry
import com.gentlemanprogramming.gentlemobile.security.BiometricDecision
import com.gentlemanprogramming.gentlemobile.security.BiometricUnlockStore
import com.gentlemanprogramming.gentlemobile.security.biometricDecision
import com.gentlemanprogramming.gentlemobile.security.parseHostSpec
import com.gentlemanprogramming.gentlemobile.security.requireBiometricUnlock
import com.gentlemanprogramming.gentlemobile.ui.settings.HooksScreen
import com.gentlemanprogramming.gentlemobile.ui.settings.HooksStatus
import com.gentlemanprogramming.gentlemobile.ui.settings.SettingsScreen
import com.gentlemanprogramming.gentlemobile.ui.settings.mapHooksStatus
import com.gentlemanprogramming.gentlemobile.ui.settings.AdvancedSettings
import com.gentlemanprogramming.gentlemobile.ui.settings.AdvancedSettingsStore
import com.gentlemanprogramming.gentlemobile.ui.settings.ShortcutPadStore
import com.gentlemanprogramming.gentlemobile.ui.settings.ShortcutsScreen
import com.gentlemanprogramming.gentlemobile.ui.settings.VozScreen
import com.gentlemanprogramming.gentlemobile.dictation.DictationEngineChoice
import com.gentlemanprogramming.gentlemobile.dictation.ModelManager
import com.gentlemanprogramming.gentlemobile.dictation.ModelRepository
import com.gentlemanprogramming.gentlemobile.dictation.ModelStatus
import com.gentlemanprogramming.gentlemobile.dictation.VozSettings
import com.gentlemanprogramming.gentlemobile.dictation.VozSettingsStore
import com.gentlemanprogramming.gentlemobile.dictation.WhisperCatalogue
import com.gentlemanprogramming.gentlemobile.dictation.effectiveEngine
import com.gentlemanprogramming.gentlemobile.dictation.resolveActiveModel
import com.gentlemanprogramming.gentlemobile.ui.session.AddResult
import com.gentlemanprogramming.gentlemobile.ui.session.ShortcutPad
import com.gentlemanprogramming.gentlemobile.ui.session.PaneSwitcherModel
import com.gentlemanprogramming.gentlemobile.ui.session.formatBytes
import com.gentlemanprogramming.gentlemobile.ui.session.Attachment
import com.gentlemanprogramming.gentlemobile.ui.session.AttachmentQueue
import com.gentlemanprogramming.gentlemobile.ui.session.AttachmentState
import com.gentlemanprogramming.gentlemobile.ui.session.WorkspaceTabsModel
import com.gentlemanprogramming.gentlemobile.ui.settings.ThemePickerScreen
import com.gentlemanprogramming.gentlemobile.ui.splash.SplashScreen
import com.gentlemanprogramming.gentlemobile.ui.theme.Air
import com.gentlemanprogramming.gentlemobile.ui.theme.AppTheme
import com.gentlemanprogramming.gentlemobile.ui.theme.AppTypeface
import com.gentlemanprogramming.gentlemobile.ui.theme.GentleMobileTheme
import com.gentlemanprogramming.gentlemobile.ui.theme.TerminalFaceStore
import com.gentlemanprogramming.gentlemobile.ui.theme.TypefaceStore
import com.gentlemanprogramming.gentlemobile.ui.theme.LocalPalette
import com.gentlemanprogramming.gentlemobile.ui.theme.SharedPreferencesStore
import com.gentlemanprogramming.gentlemobile.ui.theme.resolvedTheme
import com.gentlemanprogramming.gentlemobile.ui.theme.ThemeStore
import com.gentlemanprogramming.gentlemobile.ui.terminal.TerminalScreen
import com.gentlemanprogramming.gentlemobile.ui.terminal.TerminalFixtureScreen
import com.gentlemanprogramming.gentlemobile.ui.terminal.CursorMode
import java.io.File
import java.util.concurrent.Executors

/**
 * The real lane-A read path: one screen, plus the host-key challenge state.
 * No fake rows — everything shown comes from a snapshot of a live host over
 * SSH, through the Rust core. An empty snapshot says so instead of inventing
 * hosts. Tapping an agent opens its session screen: the agent's pane drawn
 * as a real cell grid from the surface stream (latest-wins in the core,
 * polled at ~12 fps — never a queue of stale frames), falling back to the
 * pane's visible text over `pane.read` until the first surface arrives, and
 * a composer that submits the daemon's own `agent.prompt` — never raw
 * keystrokes.
 *
 * Pairing (T9): the identity key is generated on this phone at first launch
 * and stays in app-private storage; the screen shows the public line to run
 * `--add-key` with, a paste field for the host's `gc://` link, and a camera
 * scanner behind the design's Pair screen.
 *
 * Settings/security (T18): the biometric gate — a BiometricPrompt (weak or
 * device credential) standing between the user and every session open and
 * every explicit connect — the key regeneration, per-host known-hosts
 * management through the core, and the local revoke, each behind its own
 * confirmation. The gate never touches the key file: it wraps the actions
 * that use it.
 */
class MainActivity : FragmentActivity() {

    /** S24-1: the stored language wraps the base context, so the choice
     *  survives process death and every resource access follows it from
     *  the very first frame. */
    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(AppLocale.wrapStored(base))
    }

    /** T15 (S11): the agent pane a notification tap asked to open. Set from
     *  the launch intent here and from onNewIntent when the task is already
     *  running (singleTop); consumed by the connect screen once the live
     *  snapshot actually contains the agent. */
    private val openAgentPane = androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // The launch theme carries the splash background; this is the real
        // app theme from the first composed frame on.
        setTheme(R.style.Theme_GentleMobile)
        super.onCreate(savedInstanceState)
        // Notification channels exist before anything can post (T15): the
        // persistent connection channel is quiet, the agent-alert channel
        // is the one that must be seen.
        ensureChannels(this)
        openAgentPane.value = intent?.getStringExtra(ConnectionService.EXTRA_OPEN_AGENT_PANE)
        // Edge-to-edge with real insets: the session composer must stay above
        // the keyboard and the navigation bar, which only works when the app
        // receives IME/nav-bar insets instead of the decor consuming them.
        // The app is always dark, so force light (white) status-bar icons.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        // Debug-only, per-launch override used by the tests; absent in release.
        val devSessionsDirOverride =
            if ((applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                intent?.getStringExtra("sessions_dir")
            } else {
                null
            }
        // Debug-only, per-launch preview of the hosts work (S34): the live
        // list's rows and one host's own screen, on fixture hosts, so the
        // screens behind a live connection can be looked at. Same gate as
        // wrap_preview — absent in release, view layer only.
        val hostsPreview =
            if ((applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                intent?.getBooleanExtra("hosts_preview", false) == true
            } else {
                false
            }
        // Debug-only, per-launch proof of the real PTY terminal's rendering:
        // a canned stream, no sshd. Same gate as wrap_preview.
        val terminalFixture =
            if ((applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                intent?.getBooleanExtra("terminal_fixture", false) == true
            } else {
                false
            }
        setContent {
            val themeStore = remember {
                ThemeStore(SharedPreferencesStore(this@MainActivity))
            }
            var appTheme by remember { mutableStateOf(themeStore.selected()) }
            val typefaceStore = remember {
                TypefaceStore(SharedPreferencesStore(this@MainActivity))
            }
            var typeface by remember { mutableStateOf(typefaceStore.selected()) }
            val terminalFaceStore = remember {
                TerminalFaceStore(SharedPreferencesStore(this@MainActivity))
            }
            var terminalFace by remember { mutableStateOf(terminalFaceStore.selected()) }
            val advancedStore = remember {
                AdvancedSettingsStore(SharedPreferencesStore(this@MainActivity))
            }
            var advanced by remember { mutableStateOf(advancedStore.load()) }
            val shortcutPadStore = remember {
                ShortcutPadStore(SharedPreferencesStore(this@MainActivity))
            }
            var shortcutPad by remember { mutableStateOf(shortcutPadStore.load()) }
            val vozStore = remember {
                VozSettingsStore(SharedPreferencesStore(this@MainActivity))
            }
            var voz by remember { mutableStateOf(vozStore.load()) }
            // S33: Follow the phone — dark renders Cute, light renders Paper;
            // off (the default) the pinned theme stands.
            var followThePhone by remember { mutableStateOf(themeStore.followThePhone()) }
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val effectiveTheme = resolvedTheme(appTheme, followThePhone, systemDark)
            // S24-1: the language lives in the app, persists, and applies
            // immediately — the composition re-provides the context in the
            // chosen locale, so every stringResource call follows without a
            // restart. The choice is re-read in attachBaseContext after a
            // process death.
            val localeStore = remember {
                LocaleStore(SharedPreferencesStore(this@MainActivity))
            }
            var language by remember { mutableStateOf(localeStore.selected()) }
            val localizedContext = remember(language) {
                AppLocale.wrap(this@MainActivity, language)
            }
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localizedContext,
                // S24-1: the wrapped context is not the ComponentActivity,
                // so the owners activity-compose derives by casting
                // LocalContext must be provided explicitly.
                LocalActivityResultRegistryOwner provides this@MainActivity,
                LocalLifecycleOwner provides this@MainActivity,
                LocalOnBackPressedDispatcherOwner provides this@MainActivity,
            ) {
                GentleMobileTheme(
                    effectiveTheme,
                    typeface,
                    onSetTypeface = { chosen ->
                        // Applied immediately (the composition rebuilds the
                        // typography) and persisted for the next launch.
                        typefaceStore.select(chosen)
                        typeface = chosen
                    },
                    terminalFace = terminalFace,
                    onSetTerminalFace = { chosen ->
                        terminalFaceStore.select(chosen)
                        terminalFace = chosen
                    },
                ) {
                // The chrome follows the theme it renders: Paper's light page
                // needs dark icons on the system bars, the dark themes keep
                // light ones.
                androidx.compose.runtime.LaunchedEffect(effectiveTheme) {
                    enableEdgeToEdge(
                        statusBarStyle =
                            if (effectiveTheme == AppTheme.PAPER) {
                                SystemBarStyle.light(
                                    android.graphics.Color.TRANSPARENT,
                                    android.graphics.Color.TRANSPARENT,
                                )
                            } else {
                                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                            },
                        navigationBarStyle =
                            if (effectiveTheme == AppTheme.PAPER) {
                                SystemBarStyle.light(
                                    android.graphics.Color.TRANSPARENT,
                                    android.graphics.Color.TRANSPARENT,
                                )
                            } else {
                                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                            },
                    )
                }
                if (terminalFixture) {
                    TerminalFixtureScreen(
                        theme = effectiveTheme,
                        onBack = { finish() },
                    )
                } else if (hostsPreview) {
                    HostsPreview()
                } else {
                    // The identity key lives in app-private storage now. The
                    // legacy external copy (the old adb-push path) is migrated
                    // once by DeviceIdentity.ensure, which also generates the
                    // keypair on first launch — no files are pushed any more.
                    // Host-key trust is per host (S24-4): each saved host owns
                    // its own known_hosts file, named by [KnownHostsFile].
                    val keyPath = File(filesDir, DeviceIdentity.KEY_FILE).absolutePath
                    ConnectScreen(
                        sessionsDirOverride = devSessionsDirOverride,
                        keyPath = keyPath,
                        // The pinned choice travels down (the Settings
                        // row and the picker mark the stored pin), while the
                        // rendering follows effectiveTheme above.
                        appTheme = appTheme,
                        followThePhone = followThePhone,
                        language = language,
                        advanced = advanced,
                        shortcutPad = shortcutPad,
                        voz = voz,
                        openAgentPane = openAgentPane,
                        onSetLanguage = { chosen ->
                            // Applied immediately (the composition provides
                            // a context in the new locale), persisted for the
                            // next launch.
                            localeStore.select(chosen)
                            language = chosen
                        },
                        onSelectTheme = { theme ->
                            // Applied immediately, persisted for the next launch,
                            // and Follow the phone turns off: a pin wins over the
                            // follow switch (board 07, L31).
                            followThePhone = false
                            themeStore.setFollowThePhone(false)
                            appTheme = theme
                            themeStore.select(theme)
                        },
                        onSetFollowThePhone = { follow ->
                            followThePhone = follow
                            themeStore.setFollowThePhone(follow)
                        },
                        onSetAdvanced = { settings ->
                            advanced = settings
                            advancedStore.save(settings)
                        },
                        onSetShortcutPad = { pad ->
                            shortcutPad = pad
                            shortcutPadStore.save(pad)
                        },
                        onSetVoz = { settings ->
                            voz = settings
                            vozStore.save(settings)
                        },
                    )
                }
            }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // A notification tap while the task already exists (singleTop):
        // record the agent to open, and keep the intent current so a later
        // recreation does not replay a stale one.
        openAgentPane.value = intent.getStringExtra(ConnectionService.EXTRA_OPEN_AGENT_PANE)
        setIntent(intent)
    }
}

// Background workers: read polling and user actions run off the UI thread
// on separate threads, so a stalled read never blocks a send. Internal:
// the Sessions screen (T17a) shares the same workers for its SFTP list and
// lease calls.
internal val executor = Executors.newFixedThreadPool(2)
internal val mainHandler = Handler(Looper.getMainLooper())

/** Log tag for supervision evidence: poller registration and stop lines. */
private const val TAG = "Supervision"

/**
 * The desktop-size surface the phone asks the host for (S27): the phone
 * mirrors the desktop's terminal, and a desktop terminal is wider than a
 * phone screen — 110×40 is a desktop-like grid that VERIFIED fits a Pixel
 * 6 with the painted advance and 8 px of air per side (120 was measured
 * overflowing: the painted advance at small sizes is wider than the font
 * file's nominal 0.6 em). The fit scales the whole frame to the card and
 * the pinch-zoom reads it. Terminal cells, not pixels; the core
 * sanitizes anything invalid back to its 80×24 default, so this only
 * ever changes what the daemon renders.
 */
private val DESKTOP_SURFACE = SurfaceSizeDto(cols = 110u, rows = 40u)

private sealed interface UiState {
    /** S24-4: the host list — the app's home when no link is live. */
    data object Hosts : UiState
    data object Form : UiState
    data object Connecting : UiState
    data object Scanning : UiState
    data class Challenge(val fingerprint: String, val pinned: String?, val keyBlob: String) : UiState
    data class Connected(
        val connection: CompanionConnection,
        val snapshot: SnapshotDto?,
    ) : UiState
    data class Session(val connection: CompanionConnection, val agent: AgentDto) : UiState
    data class Failed(val message: String) : UiState
}

/** Screen state + connect generation, so poll callbacks can stop cleanly. */
private class ScreenState {
    var ui by mutableStateOf<UiState>(UiState.Hosts)
    var generation: Int = 0
 /** One session state per agent pane, so drafts survive going back. */
    val sessions = mutableMapOf<String, SessionUi>()

    /** Connected splits into the Nodes screen and the agent Inbox; this
     * flag is the entry point from the host row into the agent list. */
    var inboxOpen by mutableStateOf(false)

    /** T17a (S15/S18): the profile and device lease id of the live
     * connection — the Sessions screen opens its own pinned SSH session
     * per call, and the advisory lease carries the device id. */
    var profile by mutableStateOf<HostProfileDto?>(null)
    var deviceLeaseId by mutableStateOf<String?>(null)

    /** The advisory lease this device holds on a host session, and the
     * generation counter that stops its heartbeat loop. */
    var heldLease by mutableStateOf<HeldLease?>(null)
    var leaseHeartbeatGeneration: Int = 0

    /** T17b: the RPC lane's live resumed session, plus the words the
     *  handshake reported. Closed screens keep the reason visible. */
    var resumedSession by mutableStateOf<com.gentlemanprogramming.gentlemobile.core.RpcResumedSession?>(null)
    var resumedTranscript by mutableStateOf<List<com.gentlemanprogramming.gentlemobile.core.TranscriptEntryDto>>(emptyList())
    var resumedMessageCount by mutableStateOf(0UL)
    var resumedReclaimed by mutableStateOf(false)
    /** S24-5: the live session was started fresh (not resumed) — the header
     *  says `new`, never `resumed`. */
    var resumedIsNew by mutableStateOf(false)
    var resumedDraft by mutableStateOf("")
    var resumedSending by mutableStateOf(false)
    var resumedSendError by mutableStateOf<String?>(null)
    var resumedCloseError by mutableStateOf<String?>(null)

    /** T42/T48: the real terminal — our own PTY lane drawn locally, the
     *  only session view. */
    var terminalSession by mutableStateOf<TerminalSession?>(null)
    var terminalStarting by mutableStateOf(false)
    var terminalError by mutableStateOf<String?>(null)
    var terminalCwd by mutableStateOf<String?>(null)
    /** T49: whether the terminal PTY is running an agent (what the app
     *  itself launched) rather than the bare shell. A bare shell offers no
     *  camera/clip, because nothing would receive the attachment line. */
    var terminalAgent by mutableStateOf(false)

    /** S24-5: the start-session screen's state — the sessions the history
     *  screen already listed (the repositories derive from them, nothing is
     *  scanned), whether a fresh start is in flight, and the reason a start
     *  failed (shown on the start screen, never a generic "failed"). */
    var sessionsForStart by mutableStateOf<List<com.gentlemanprogramming.gentlemobile.core.SessionDto>>(emptyList())
    var startingSession by mutableStateOf(false)
    var startSessionError by mutableStateOf<String?>(null)

    /** S26-2: the host's workspaces, the screen's rows and their loading
     *  state. `null` rows mean "not loaded yet" — an empty list is the
     *  daemon's own answer, never a read we failed. */
    var workspaces by mutableStateOf<List<com.gentlemanprogramming.gentlemobile.core.WorkspaceRowDto>?>(null)
    var workspacesLoading by mutableStateOf(false)
    var workspacesError by mutableStateOf<String?>(null)

    /** S26-2: the workspace the inbox is filtered to, with the human's own
     *  label for the header. `null` shows every agent — the way back. */
    var workspaceFilter by mutableStateOf<String?>(null)
    var workspaceFilterLabel by mutableStateOf<String?>(null)

    /** S26-3: the `+` flow — path check in flight, its measured verdict
     *  (the git note rides in the DTO), the create in flight, and the
     *  reason a create failed. */
    var addChecking by mutableStateOf(false)
    var addVerdict by mutableStateOf<com.gentlemanprogramming.gentlemobile.core.DirCheckDto?>(null)
    var creatingWorkspace by mutableStateOf(false)
    var createWorkspaceError by mutableStateOf<String?>(null)

    /** T27 (S30-3): the `+` browser — open, where it stands (the machine's
     *  disks, or inside a folder), and what each level read. `null` lists
     *  mean "not loaded yet" — an empty list is the host's own answer. */
    var browseOpen by mutableStateOf(false)
    var browseLevel by mutableStateOf<com.gentlemanprogramming.gentlemobile.ui.workspaces.WorkspacesModel.BrowseState>(
        com.gentlemanprogramming.gentlemobile.ui.workspaces.WorkspacesModel.BrowseState.Disks,
    )
    var disks by mutableStateOf<List<com.gentlemanprogramming.gentlemobile.core.DiskDto>?>(null)
    var disksLoading by mutableStateOf(false)
    var disksError by mutableStateOf<String?>(null)
    var folders by mutableStateOf<List<com.gentlemanprogramming.gentlemobile.core.FolderEntryDto>?>(null)
    var foldersLoading by mutableStateOf(false)
    var foldersError by mutableStateOf<String?>(null)

    /** T8c supervision: the four words (connecting, connected, interrupted,
     * failed) and the liveness rule, in one tested place. Screens read this
     * machine instead of a separate interrupted flag that can drift from
     * what the connection is really doing. */
    val machine = SupervisionMachine()

    /** Registered pollers, logged at every change so teardown — cancelling
     *  a session, going back to the host list, replacing the connection — is
     *  provable with a counter that returns to zero. */
    val pollers = PollTracker()

    /** S24-4: the saved host list, in the order hosts were added. Reloaded
     *  after every save and removal. */
    var hosts by mutableStateOf<List<SavedHostEntry>>(emptyList())

    /** S24-8: the per-agent last-observed activity this connection has seen;
     *  reset with every new connection, read by the inbox rows. */
    val activity = AgentActivity()

    /** T31 (S32b): the approval board's state. The tracker and the parse
     *  map reset with every new connection — a fresh host knows nothing
     *  about what this one observed. */
    val approvalWait = ApprovalWaitTracker()
    var approvalParses by mutableStateOf<Map<String, ApprovalRead>>(emptyMap())
    var approvalWalk by mutableStateOf(false)
    var approvalWalkIndex by mutableStateOf(0)
    var approvalHold by mutableStateOf<ApprovalHold?>(null)
    var approvalBusy by mutableStateOf(false)
    var approvalError by mutableStateOf<Pair<String, String>?>(null)

    /** One pane's fresh read lands in the board's state: the map grows by
     *  one entry and the timestamp makes every read honest about its age. */
    fun rememberApproval(paneId: String, parse: ApprovalParser.Parse) {
        approvalParses = approvalParses +
            (paneId to ApprovalRead(parse, System.currentTimeMillis()))
    }

    /** S24-1: the application context, captured once so messages composed
     *  on the worker threads (errors, drop reasons) can resolve their
     *  strings in the stored locale at creation time. */
    lateinit var appContext: Context

    /** T15 (S11): the pure alert brain, fed by its own slow poller over the
     *  connection the app already holds — no extra network traffic, no
     *  relay. Reset whenever a new connection goes live. */
    val alertTracker = AgentAlertTracker()

    /** The foreground service runs exactly while the machine says
     *  connected; the unit-tested gate owns the start/stop pairing. */
    val serviceGate = ForegroundServiceGate()

    /** Whether the app is (probably) visible: the notification suppression
     *  rule — never notify about the agent whose session is on screen. */
    var appInForeground by mutableStateOf(true)
}

/** The mutable state of one open agent session. */
private class SessionUi {
    /** The workspace's own tabs and panes (T47): from the snapshot the
     *  daemon keeps sending while the session is open — the pane list's
     *  rows and the agent each tab hosts. Empty until the first snapshot. */
    var tabs by mutableStateOf<List<TabDto>>(emptyList())
    var panes by mutableStateOf<List<PaneDto>>(emptyList())
    /** The agent whose pane lives in each tab — what a tab tap can make
     *  visible. Tabs without an agent have no entry. */
    var tabAgents by mutableStateOf<Map<String, AgentDto>>(emptyMap())
    /** Every agent the snapshot knows, for the pane list's rows. */
    var agents by mutableStateOf<List<AgentDto>>(emptyList())

    /** The host's typed refusal when a pane focus could not land. */
    var tabError by mutableStateOf<String?>(null)

    /** Attachments (T49): the queue owns the honest state machine; the
     * snapshot list is Compose state so the rows redraw on every transition.
     * Thumbnails are decoded once per attachment, keyed by id; the source
     * URIs are kept so a Retry can re-read the bytes (the transfer is
     * one-shot). */
    val attachmentQueue = AttachmentQueue()
    var attachments by mutableStateOf<List<Attachment>>(emptyList())
    val thumbnails = mutableMapOf<Long, ImageBitmap>()
    val attachmentUris = mutableMapOf<Long, Uri>()
    /** A pick-time refusal (oversize, unusable name), shown near the
     * composer — the queue refuses before anything is half-added. */
    var attachmentError by mutableStateOf<String?>(null)

    /** Re-publish the queue snapshot; call after every queue mutation. */
    fun syncAttachments() {
        attachments = attachmentQueue.snapshot()
        if (attachmentQueue.snapshot().none {
                it.state is AttachmentState.Ready ||
                    it.state is AttachmentState.Uploading ||
                    it.state is AttachmentState.Failed
            }
        ) {
            // Nothing in flight: a stale refusal would only confuse.
            attachmentError = null
        }
    }

    /** A new connection owns a fresh core store: the snapshot is empty again
     *  and attachments did not survive — their upload session died with the
     *  connection. */
    fun resetBaseline() {
        tabs = emptyList()
        panes = emptyList()
        tabAgents = emptyMap()
        agents = emptyList()
        tabError = null
        attachmentQueue.snapshot().filter {
            it.state !is AttachmentState.Uploaded
        }.forEach { attachmentQueue.remove(it.id) }
        thumbnails.clear()
        syncAttachments()
    }

    /** A sent line handed every uploaded file to the agent: the rows leave
     *  with the draft, so the next message starts clean. */
    fun clearSentAttachments() {
        attachmentQueue.snapshot()
            .filter { it.state is AttachmentState.Uploaded }
            .forEach { attachmentQueue.remove(it.id) }
        thumbnails.clear()
        attachmentUris.clear()
        syncAttachments()
    }
}

/** The screens reachable from the Pair flow; the design shows back buttons. */
private enum class Route { Main, Settings, Themes, Shortcuts, Hooks, Voz, Host, Sessions, StartSession, Resumed, Workspaces }

/** The words a resume or a prompt failure shows: typed refusals keep their
 *  exact message (the core's Display carries the reason), anything else
 *  falls back to the exception's own words. Never a generic "failed". */
private fun describeResumeFailure(error: Exception): String =
    error.message ?: error.toString()

@Composable
fun ConnectScreen(
    sessionsDirOverride: String? = null,
    keyPath: String,
    appTheme: AppTheme,
    followThePhone: Boolean,
    language: AppLanguage,
    advanced: AdvancedSettings,
    shortcutPad: ShortcutPad,
    voz: VozSettings,
    onSetLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (AppTheme) -> Unit,
    onSetFollowThePhone: (Boolean) -> Unit,
    onSetAdvanced: (AdvancedSettings) -> Unit,
    onSetShortcutPad: (ShortcutPad) -> Unit,
    onSetVoz: (VozSettings) -> Unit,
    openAgentPane: androidx.compose.runtime.MutableState<String?>,
) {
    val screen = remember { ScreenState() }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    // The theme the app actually renders (follow-the-phone resolves it); the
    // terminal palettes must match it, not the stored pin.
    val effectiveTheme = resolvedTheme(appTheme, followThePhone, androidx.compose.foundation.isSystemInDarkTheme())
    // S24-1: the worker threads resolve their strings (errors, drop
    // reasons) through the application context, in the stored locale.
    screen.appContext = context.applicationContext

    // T50 (Advanced → Behaviour): keep the screen on only while a terminal
    // is actually open, never app-wide.
    LaunchedEffect(advanced.keepScreenOn, screen.ui) {
        val activity = AppLocale.unwrapToLifecycle(context) as? FragmentActivity
        val keep = advanced.keepScreenOn && screen.ui is UiState.Session
        activity?.window?.apply {
            if (keep) {
                addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    var route by remember { mutableStateOf(Route.Main) }

    // T54 (Voz): the model manager owns downloads, cancels and deletes, and
    // reports each model's status; the Voz screen and the terminal composer
    // both read the resolved active model from the Voz settings.
    val modelRepository = remember { ModelRepository(File(context.filesDir, "models")) }
    var modelStatuses by remember { mutableStateOf<Map<String, ModelStatus>>(emptyMap()) }
    val modelManager = remember {
        ModelManager(
            repository = modelRepository,
            executor = executor,
            mainHandler = mainHandler,
            onState = { id, status ->
                modelStatuses = modelStatuses + (id to status)
            },
        )
    }
    LaunchedEffect(Unit) { modelManager.refresh() }
    val installedIds = modelStatuses.filterValues { it is ModelStatus.Installed }.keys
    val activeModel = resolveActiveModel(voz.activeModelId, installedIds)
    val activeModelPath = activeModel?.let { modelRepository.fileFor(it).absolutePath }
    val resolvedEngine = effectiveEngine(voz.engine, activeModel)

    // T55 (Hooks): the host's answer about this phone's key, and whether a
    // check is in flight — re-asked on screen entry and on demand.
    var hooksStatus by remember { mutableStateOf<HooksStatus?>(null) }
    var hooksChecking by remember { mutableStateOf(false) }

    // S25: the form starts from the last connection (the optional Name
    // label travels with it, kept for the multi-host work), empty on
    // first launch with only the SSH port as default.
    val hostStore = remember { HostStore(SharedPreferencesStore(context)) }
    val savedForm = remember { hostStore.load() }
    // S24-4: the saved host list lives beside the last-used form: the form
    // prefill stays, and the list is what the app opens on.
    val hostsStore = remember { HostsStore(SharedPreferencesStore(context)) }
    // T57: the host whose menu is open, and the host being edited with its
    // typed name and address.
    var hostMenuTarget by remember { mutableStateOf<SavedHostEntry?>(null) }
    var editTarget by remember { mutableStateOf<SavedHostEntry?>(null) }
    var editName by remember { mutableStateOf("") }
    var editHost by remember { mutableStateOf("") }
    var editPort by remember { mutableStateOf("") }
    var editUser by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<SavedHostEntry?>(null) }
    var hostName by remember { mutableStateOf(savedForm.name) }
    var host by remember { mutableStateOf(savedForm.host) }
    var port by remember { mutableStateOf(savedForm.port) }
    var user by remember { mutableStateOf(savedForm.user) }
    var session by remember { mutableStateOf("default") }
    // Device identity: generated on first launch, migrated from the old
    // external path once, shown so the user never copies files around.
    var identity by remember { mutableStateOf<DeviceIdentity.Identity?>(null) }
    var identityError by remember { mutableStateOf<String?>(null) }
    // Settings/security state (T18): the persisted biometric switch, the
    // device's real capability, and the pinned hosts the core holds.
    val biometricStore = remember { BiometricUnlockStore(SharedPreferencesStore(context)) }
    var biometricDecision by remember { mutableStateOf<BiometricDecision?>(null) }
    var biometricEnabled by remember { mutableStateOf(false) }

    fun loadIdentity() {
        executor.execute {
            try {
                val ensured = DeviceIdentity.ensure(context)
                mainHandler.post { identity = ensured }
            } catch (error: Exception) {
                mainHandler.post { identityError = describe(context, error) }
            }
        }
    }

    /** The saved host list (S24-4): re-read after every save or removal. The
     *  list is small and local, so a plain read is enough. */
    fun refreshSavedHosts() {
        screen.hosts = hostsStore.load()
    }

    /** T57 (Editar): save the edited name and address. The address is the
     *  host's identity, so a changed one drops the old row and its pin before
     *  the new one is saved. Invalid fields never save. */
    fun applyEditHost() {
        val entry = editTarget ?: return
        val name = editName.trim()
        val h = editHost.trim()
        val p = editPort.trim()
        val u = editUser.trim()
        if (ConnectValidator.host(h) != null ||
            ConnectValidator.port(p) != null ||
            ConnectValidator.user(u) != null
        ) {
            return
        }
        val addressChanged = !h.equals(entry.host, ignoreCase = true) || p != entry.port
        if (addressChanged) {
            hostsStore.remove(entry.host, entry.port)
            val pinPath = File(context.filesDir, HostDeletion.pinFile(entry)).absolutePath
            executor.execute {
                runCatching {
                    forgetHost(pinPath, entry.host, entry.port.trim().toIntOrNull()?.toUShort() ?: 0u)
                }
            }
        }
        hostsStore.save(SavedHostEntry(name.ifEmpty { "$u@$h" }, h, p, u))
        if (entry.host.equals(host.trim(), ignoreCase = true) && entry.port.trim() == port.trim()) {
            hostName = name
            host = h
            port = p
            user = u
        }
        editTarget = null
        refreshSavedHosts()
    }

    /** The saved host list (S24-4), read once on entry. */
    LaunchedEffect(Unit) {
        refreshSavedHosts()
        loadIdentity()
    }

    /** The switch's persisted state plus what this device can enforce. */
    fun refreshBiometrics() {
        biometricEnabled = biometricStore.enabled()
        executor.execute {
            val decision = biometricDecision(context)
            mainHandler.post { biometricDecision = decision }
        }
    }

    /** T55 (Hooks): ask the host, over the pinned SFTP lane, whether this
     *  phone's key is in its authorized_keys — a truthful word, never a
     *  guess. Without a live connection there is nothing to ask. */
    fun runHooksCheck() {
        val profile = screen.profile
        val line = identity?.publicKeyLine
        if (screen.ui !is UiState.Connected || profile == null || line == null) {
            hooksChecking = false
            hooksStatus = HooksStatus.ConnectFirst
            return
        }
        val keyType = DeviceKeyFingerprint.keyType(line) ?: return
        val keyBlob = DeviceKeyFingerprint.keyBlob(line) ?: return
        hooksChecking = true
        hooksStatus = null
        executor.execute {
            try {
                val registered = checkDeviceKeyRegistered(profile, keyType, keyBlob)
                mainHandler.post {
                    hooksChecking = false
                    hooksStatus = mapHooksStatus(connected = true, registered = registered, error = null)
                }
            } catch (error: Exception) {
                mainHandler.post {
                    hooksChecking = false
                    hooksStatus = mapHooksStatus(connected = true, registered = null, error = describe(context, error))
                }
            }
        }
    }

    // The biometric gate (T18): when the switch is on and the device can
    // enforce it, a BiometricPrompt stands between the user and the gated
    // action — opening a session, or an explicit connect, which is the
    // device key being used. It never touches the key file itself.
    fun gated(action: () -> Unit) {
        // The composition context may be locale-wrapped (S24-1): the gate
        // needs the real FragmentActivity behind it.
        val activity = AppLocale.unwrapToLifecycle(context) as? FragmentActivity
        if (activity != null) {
            requireBiometricUnlock(activity, biometricStore, biometricDecision, action)
        } else {
            action()
        }
    }

    // Settings reads live state on entry: the biometric capability is
    // re-asked every time the screen opens (T18). Hooks re-checks when the
    // screen opens or once the device key has loaded.
    LaunchedEffect(route, identity) {
        if (route == Route.Settings) {
            refreshBiometrics()
        }
        if (route == Route.Hooks) {
            runHooksCheck()
        }
    }

    // T15 (S11): POST_NOTIFICATIONS on Android 13+, requested once, with an
    // explanation on screen while the system dialog is up. A denial keeps
    // the app fully working — the Settings row then says notifications are
    // off and why. Below Android 13 there is nothing to ask (null).
    var notificationsGranted by remember {
        mutableStateOf(notificationPermissionGranted(context))
    }
    var notificationsAsked by remember { mutableStateOf(false) }
    val notificationsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationsGranted = granted
        notificationsAsked = true
    }
    fun askForNotifications() {
        if (Build.VERSION.SDK_INT < 33) return
        if (notificationsGranted == true || notificationsAsked) return
        notificationsAsked = true
        notificationsPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    // Splash (S21, board 01): the mark, the wordmark and a thin progress line
    // for exactly as long as the cold-start key work runs — never a fake
    // delay. It hides as soon as the first screen is ready, including when
    // the key work failed (the error is shown instead of a spinner).
    if (identity == null && identityError == null) {
        SplashScreen()
        return
    }

    /** Release the advisory lease this device holds (S18): clear the local
     *  state, stop the heartbeat, and ask the host to remove the file — only
     *  the core knows whether it was still ours. */
    fun releaseHeldLease(state: ScreenState) {
        val held = state.heldLease ?: return
        state.heldLease = null
        state.leaseHeartbeatGeneration += 1
        val profile = state.profile ?: return
        val deviceId = state.deviceLeaseId ?: return
        executor.execute {
            try {
                releaseLease(profile, held.sessionId, deviceId, held.token)
            } catch (_: Exception) {
            }
        }
    }

    /** The heartbeat loop (S18): while the app holds a lease, refresh it
     *  every heartbeat interval so it stays live on the host's clock. A
     *  failed refresh — lease lost, replaced, or the link gone — clears the
     *  hold honestly instead of claiming a session we no longer own. */
    fun scheduleLeaseHeartbeat(state: ScreenState) {
        state.leaseHeartbeatGeneration += 1
        val generation = state.leaseHeartbeatGeneration
        mainHandler.postDelayed({
            if (generation != state.leaseHeartbeatGeneration) return@postDelayed
            val held = state.heldLease ?: return@postDelayed
            val profile = state.profile ?: return@postDelayed
            val deviceId = state.deviceLeaseId ?: return@postDelayed
            executor.execute {
                val alive = try {
                    refreshLease(profile, held.sessionId, deviceId, held.token)
                } catch (_: Exception) {
                    false
                }
                mainHandler.post {
                    if (generation != state.leaseHeartbeatGeneration) return@post
                    if (!alive) {
                        // The lease is gone; the screen reloads its list on
                        // its own actions, and the row honestly returns to
                        // whatever the host says.
                        state.heldLease = null
                    }
                    scheduleLeaseHeartbeat(state)
                }
            }
        }, LEASE_HEARTBEAT_MS)
    }

    /** Open one agent's session screen — the exact path the inbox row, a
     *  workspace row (S28-5) and a notification tap all take. The biometric
     *  gate wraps it like any other session open. */
    fun openAgentSession(current: UiState.Connected, agent: AgentDto) {
        gated {
            val connection = current.connection
            screen.generation += 1
            val generation = screen.generation
            val sessionUi = screen.sessions.getOrPut(agent.paneId) { SessionUi() }
            screen.ui = UiState.Session(connection, agent)
            // T48: the terminal is the only session view — a bare shell in
            // the agent's workspace folder, sized by the phone.
            screen.terminalSession = null
            screen.terminalStarting = false
            screen.terminalError = null
            screen.terminalCwd = agent.cwd.orEmpty()
            screen.terminalAgent = false
            // T47: the pane list's rows ride the snapshot, polled while the
            // session is open, scoped to this agent's workspace.
            pollWorkspaceTabs(screen, sessionUi, connection, agent.workspaceId, agent.paneId, generation)
        }
    }

    /** S24-5: start a fresh session in `dir` (a repository with history, or
     *  a validated manual path) and open the same transcript + composer the
     *  resume path uses. The core validates the directory, spawns the child
     *  without `--session`, and takes the lease under the session id the
     *  child reports — only once the session exists. A failure reports the
     *  host's own reason on the start screen and leaves nothing half-open. */
    fun startFreshSession(
        state: ScreenState,
        profile: HostProfileDto,
        deviceId: String,
        dir: String,
        onDone: () -> Unit,
    ) {
        if (state.startingSession) return
        state.startingSession = true
        state.startSessionError = null
        executor.execute {
            try {
                val token = java.util.UUID.randomUUID().toString()
                val outcome = coreStartSessionInDir(profile, dir, deviceId, token)
                mainHandler.post {
                    state.startingSession = false
                    // The lease id is the session id the child itself
                    // reported; the token is the one this call sent.
                    state.heldLease = HeldLease(outcome.session.sessionId(), token)
                    scheduleLeaseHeartbeat(state)
                    state.resumedSession = outcome.session
                    state.resumedTranscript = outcome.transcript
                    state.resumedMessageCount = outcome.session.messageCount()
                    state.resumedReclaimed = false
                    state.resumedIsNew = true
                    state.resumedDraft = ""
                    state.resumedSending = false
                    state.resumedSendError = null
                    state.resumedCloseError = null
                    onDone()
                }
            } catch (e: Exception) {
                mainHandler.post {
                    state.startingSession = false
                    // The host's own reason, word for word — no generic
                    // "failed", and nothing half-open (the core closed the
                    // child; no lease was ever written for a session that
                    // never became ours). UniFFI's generated message is the
                    // bare field list, so the typed directory refusal gets
                    // its full sentence, in the stored locale.
                    state.startSessionError = when (e) {
                        is com.gentlemanprogramming.gentlemobile.core.CompanionException.RpcStartNotADirectory ->
                            AppLocale.wrapStored(state.appContext)
                                .getString(R.string.start_dir_missing, e.path)
                        else -> describeResumeFailure(e)
                    }
                }
            }
        }
    }

    /** S26-2: read the host's workspaces over the request lane — one
     *  `workspace.list` plus one read-only `worktree.list` per workspace
     *  for the branch. The list is re-read on every screen entry and after
     *  every successful `+`. */
    fun loadWorkspaces(state: ScreenState, connection: CompanionConnection) {
        if (state.workspacesLoading) return
        state.workspacesLoading = true
        state.workspacesError = null
        executor.execute {
            try {
                val rows = connection.listWorkspaces()
                mainHandler.post {
                    state.workspaces = rows
                    state.workspacesLoading = false
                }
            } catch (e: Exception) {
                mainHandler.post {
                    state.workspacesLoading = false
                    state.workspacesError = describe(AppLocale.wrapStored(state.appContext), e)
                }
            }
        }
    }

    /** S26-2/S28-5: tapping a workspace row — focus it on the host (the
     *  daemon's own switch, exactly what tapping it in the herdr TUI does)
     *  and go **straight in**: one agent opens its session directly, more
     *  (or none) open the workspace's agent list — no screen in between
     *  that only repeats the workspace's name (S28-5). A focus refusal is
     *  shown when the workspaces screen is next visited; the entry still
     *  proceeds on local truth. */
    fun openWorkspace(state: ScreenState, connection: CompanionConnection, row: com.gentlemanprogramming.gentlemobile.core.WorkspaceRowDto) {
        val current = state.ui
        if (current !is UiState.Connected) return
        executor.execute {
            val failure = try {
                connection.focusWorkspace(row.id)
                null
            } catch (e: Exception) {
                describe(AppLocale.wrapStored(state.appContext), e)
            }
            // The agents of this workspace, from the latest snapshot — the
            // decision's input is the daemon's own answer, not the row's
            // cached count.
            val agents = try {
                connection.snapshot()?.agents?.filter { it.workspaceId == row.id }.orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
            mainHandler.post {
                if (failure != null) state.workspacesError = failure
                when (val entry = WorkspacesModel.workspaceEntry(agents)) {
                    is WorkspacesModel.WorkspaceEntry.Session -> openAgentSession(current, entry.agent)
                    WorkspacesModel.WorkspaceEntry.AgentList -> {
                        state.workspaceFilter = row.id
                        state.workspaceFilterLabel = row.label
                        state.inboxOpen = true
                        // The inbox renders on the Main route; staying on
                        // the workspaces route would keep the list on screen.
                        route = Route.Main
                    }
                }
            }
        }
    }

    /** S26-3: the `+` flow — validate the typed path over SFTP (exists,
     *  a directory, readable; whether it is git), then create the
     *  workspace. The verdict is always shown, never skipped: measured
     *  live, the daemon silently falls back to `$HOME` for a missing
     *  directory, so the validation is the guard. A non-git directory may
     *  still be added — a workspace does not require git. */
    fun addProject(state: ScreenState, connection: CompanionConnection, profile: HostProfileDto, rawPath: String) {
        if (state.addChecking || state.creatingWorkspace) return
        val path = rawPath.trim()
        state.addChecking = true
        state.addVerdict = null
        state.createWorkspaceError = null
        executor.execute {
            try {
                val verdict = com.gentlemanprogramming.gentlemobile.core.checkHostDir(profile, path)
                mainHandler.post {
                    state.addChecking = false
                    state.addVerdict = verdict
                    if (verdict.verdict == com.gentlemanprogramming.gentlemobile.core.DirVerdictDto.OK) {
                        state.creatingWorkspace = true
                        executor.execute {
                            try {
                                connection.createWorkspace(path, WorkspacesModel.workspaceLabel(path))
                                mainHandler.post {
                                    state.creatingWorkspace = false
                                    // T27: the browser closes on success — the
                                    // refreshed workspaces list is the answer.
                                    state.browseOpen = false
                                    loadWorkspaces(state, connection)
                                }
                            } catch (e: Exception) {
                                mainHandler.post {
                                    state.creatingWorkspace = false
                                    state.createWorkspaceError = describe(AppLocale.wrapStored(state.appContext), e)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    state.addChecking = false
                    state.createWorkspaceError = describe(AppLocale.wrapStored(state.appContext), e)
                }
            }
        }
    }

    /** A typed listing failure is its own sentence (T27): the same plain
     *  words the path check uses, plus the host-level answer. */
    fun describeBrowse(context: Context, error: Exception): String = when (error) {
        is CompanionException.BrowseMissing ->
            context.getString(R.string.ws_path_missing, error.path)
        is CompanionException.BrowseNotADirectory ->
            context.getString(R.string.ws_not_a_directory, error.path)
        is CompanionException.BrowseNoPermission ->
            context.getString(R.string.ws_no_permission, error.path)
        is CompanionException.BrowseHost ->
            context.getString(R.string.ws_browse_host, error.detail)
        else -> describe(context, error)
    }

    /** T27 (S30-3): the `+` browser — the machine's disks over the same
     *  one-shot SFTP lane the uploads use. The rows are exactly what the
     *  core sent (root first, then by mount point); a failure is typed and
     *  shown as its own sentence, never an empty list. */
    fun openBrowse(state: ScreenState, connection: CompanionConnection) {
        if (state.disksLoading) return
        state.browseOpen = true
        state.browseLevel = com.gentlemanprogramming.gentlemobile.ui.workspaces.WorkspacesModel.BrowseState.Disks
        state.addVerdict = null
        state.createWorkspaceError = null
        if (state.disks != null) return
        state.disksLoading = true
        state.disksError = null
        executor.execute {
            try {
                val listed = connection.listDisks()
                mainHandler.post {
                    state.disks = listed
                    state.disksLoading = false
                }
            } catch (e: Exception) {
                mainHandler.post {
                    state.disksLoading = false
                    state.disksError = describeBrowse(AppLocale.wrapStored(state.appContext), e)
                }
            }
        }
    }

    /** The folders of the directory the human tapped into (T27): loaded on
     *  every entry, so up-and-down navigation never shows a stale folder. */
    fun loadFolders(state: ScreenState, connection: CompanionConnection, path: String) {
        state.folders = null
        state.foldersLoading = true
        state.foldersError = null
        executor.execute {
            try {
                val listed = connection.listFolders(path)
                mainHandler.post {
                    state.folders = listed
                    state.foldersLoading = false
                }
            } catch (e: Exception) {
                mainHandler.post {
                    state.foldersLoading = false
                    state.foldersError = describeBrowse(AppLocale.wrapStored(state.appContext), e)
                }
            }
        }
    }

    fun disconnectLive() {
        // T15 (S11): the notification's Disconnect action can land while a
        // session screen is up; the user is disconnecting the connection,
        // whatever screen shows it.
        val connection = when (val current = screen.ui) {
            is UiState.Connected -> current.connection
            is UiState.Session -> current.connection
            else -> null
        }
        if (connection != null) {
            screen.generation += 1
            executor.execute {
                try {
                    connection.disconnect()
                } catch (_: Exception) {
                }
            }
            // S24-4: the host list is the home screen — after a disconnect
            // the app shows the saved hosts, not the Pair form. The host's
            // own screen (S34) may be where the disconnect came from: it
            // falls back to the list too.
            screen.ui = UiState.Hosts
            screen.machine.reset()
            route = Route.Main
        }
        // S18: disconnecting releases any advisory lease this device holds —
        // a dead link must not keep a session claimed.
        releaseHeldLease(screen)
    }

    /** T50: deleting a host removes the saved host and its own pin — and
     *  nothing else. A deleted live host disconnects first. */
    fun deleteHost(entry: SavedHostEntry) {
        if (entry.host.equals(host.trim(), ignoreCase = true) && entry.port.trim() == port.trim()) {
            disconnectLive()
        }
        hostsStore.remove(entry.host, entry.port)
        val pinPath = File(context.filesDir, HostDeletion.pinFile(entry)).absolutePath
        executor.execute {
            try {
                forgetHost(pinPath, entry.host, entry.port.trim().toIntOrNull()?.toUShort() ?: 0u)
            } catch (_: Exception) {
            }
        }
        deleteTarget = null
        refreshSavedHosts()
    }

    // S18: app teardown releases the advisory lease — the composition is
    // the app's lifetime for the connect flow, so disposing it is the
    // last chance to hand the claim back.
    DisposableEffect(Unit) {
        onDispose { releaseHeldLease(screen) }
    }

    if (route == Route.Settings) {
        SettingsScreen(
            biometricDecision = biometricDecision,
            biometricEnabled = biometricEnabled,
            theme = appTheme,
            followThePhone = followThePhone,
            language = language,
            advanced = advanced,
            onSetFollowThePhone = onSetFollowThePhone,
            onSetLanguage = onSetLanguage,
            onSetAdvanced = onSetAdvanced,
            connected = screen.ui is UiState.Connected,
            notificationsGranted = notificationsGranted,
            onOpenThemes = { route = Route.Themes },
            onOpenShortcuts = { route = Route.Shortcuts },
            onOpenHooks = { route = Route.Hooks },
            onOpenVoz = { route = Route.Voz },
            onToggleBiometric = { enabled ->
                biometricEnabled = enabled
                biometricStore.setEnabled(enabled)
            },
            onRevoke = {
                // Wipe what this device holds and come back to the Pair
                // screen with a fresh key; the host side keeps trusting the
                // old key until gentle-mobile-pair --revoke runs there.
                disconnectLive()
                executor.execute {
                    DeviceIdentity.wipeAt(context.filesDir)
                    mainHandler.post {
                        identity = null
                        identityError = null
                        screen.ui = UiState.Hosts
                        screen.machine.reset()
                        route = Route.Main
                        loadIdentity()
                    }
                }
            },
            onDisconnect = {
                disconnectLive()
                route = Route.Main
            },
            onOpenNotificationSettings = {
                // Permission honesty (T15): a denied row opens the system's
                // own notification settings for this app, one tap away.
                try {
                    context.startActivity(
                        Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                } catch (_: Exception) {
                }
            },
            onBack = { route = Route.Main },
        )
        return
    }
    if (route == Route.Themes) {
        ThemePickerScreen(
            current = appTheme,
            onSelect = onSelectTheme,
            // Board 06: the topbar's back returns to settings.
            onBack = { route = Route.Settings },
        )
        return
    }
    if (route == Route.Shortcuts) {
        ShortcutsScreen(
            pad = shortcutPad,
            onPadChange = onSetShortcutPad,
            onBack = { route = Route.Settings },
        )
        return
    }
    if (route == Route.Hooks) {
        HooksScreen(
            fingerprint = identity?.publicKeyLine?.let { DeviceKeyFingerprint.fingerprint(it) },
            status = hooksStatus,
            checking = hooksChecking,
            theme = effectiveTheme,
            onRegenerate = {
                // T55: a new key means new trust — the previous QR stops
                // working and the host must trust the new key.
                executor.execute {
                    try {
                        val fresh = DeviceIdentity.regenerate(context)
                        mainHandler.post {
                            identity = fresh
                            runHooksCheck()
                        }
                    } catch (error: Exception) {
                        mainHandler.post { identityError = describe(context, error) }
                    }
                }
            },
            onBack = { route = Route.Settings },
        )
        return
    }
    if (route == Route.Voz) {
        VozScreen(
            settings = voz,
            modelStatuses = modelStatuses,
            activeModelPath = activeModelPath,
            onSetEngine = { engine -> onSetVoz(voz.copy(engine = engine)) },
            onSetAutoSend = { auto -> onSetVoz(voz.copy(autoSend = auto)) },
            onUseModel = { id ->
                onSetVoz(voz.copy(engine = DictationEngineChoice.LOCAL_WHISPER, activeModelId = id))
            },
            onDownload = { id -> WhisperCatalogue.byId(id)?.let { modelManager.download(it) } },
            onCancelDownload = { id -> WhisperCatalogue.byId(id)?.let { modelManager.cancel(it) } },
            onDeleteModel = { id -> WhisperCatalogue.byId(id)?.let { modelManager.delete(it) } },
            onReplacementsChange = { replacements -> onSetVoz(voz.copy(replacements = replacements)) },
            onBack = { route = Route.Settings },
        )
        return
    }

    // S34 (T35): one saved host's own screen, reached by tapping it in the
    // hosts list. It carries that host's own things — Workspaces, Session
    // history, the connection state and the disconnect action — so the list
    // itself stays a list. Only a host the app knows is live opens it; for
    // a machine that is not connected the row still connects first, as it
    // always did.
    if (route == Route.Host) {
        val liveEntry = HostsModel.liveEntry(screen.hosts, host, port)
        if (screen.ui is UiState.Connected && liveEntry != null) {
            HostScreen(
                entry = liveEntry,
                state = HostsModel.hostScreen(screen.machine.state),
                onBack = { route = Route.Main },
                onOpenWorkspaces = { route = Route.Workspaces },
                onOpenSessions = { route = Route.Sessions },
                onDisconnect = { disconnectLive() },
            )
            return
        }
        // No live connection behind the route: back to the host list.
        route = Route.Main
    }

    // T17a (S15/S18): the host's Gentle Shell sessions, listed over SFTP
    // and guarded by the advisory lease. Reached from the Nodes screen.
    if (route == Route.Sessions) {
        val sessionsProfile = screen.profile
        val sessionsDeviceId = screen.deviceLeaseId
        if (sessionsProfile != null && sessionsDeviceId != null) {
            SessionsScreen(
                profile = sessionsProfile,
                deviceLeaseId = sessionsDeviceId,
                // The sessions root is the host's own default. A dev override
                // exists only in a debug build and only for this launch
                // (`--es sessions_dir <path>`), never a silent persisted value.
                sessionsDir = sessionsDirOverride ?: defaultSessionsDir(sessionsProfile.user),
                heldLease = screen.heldLease,
                onLeaseTaken = { sessionId, token ->
                    screen.heldLease = HeldLease(sessionId, token)
                    scheduleLeaseHeartbeat(screen)
                },
                onLeaseReleased = {
                    screen.heldLease = null
                    screen.leaseHeartbeatGeneration += 1
                },
                onResume = { session, sessionsDir ->
                    val profile = sessionsProfile
                    val deviceId = sessionsDeviceId
                    executor.execute {
                        try {
                            val token = java.util.UUID.randomUUID().toString()
                            val outcome = coreResumeSession(
                                profile,
                                "${sessionsDir.trimEnd('/')}/${session.fileName}",
                                session.sessionId,
                                deviceId,
                                token,
                            )
                            mainHandler.post {
                                screen.heldLease = HeldLease(session.sessionId, token)
                                scheduleLeaseHeartbeat(screen)
                                screen.resumedSession = outcome.session
                                screen.resumedTranscript = outcome.transcript
                                screen.resumedMessageCount = outcome.session.messageCount()
                                screen.resumedReclaimed = outcome.reclaimed
                                screen.resumedIsNew = false
                                screen.resumedDraft = ""
                                screen.resumedSending = false
                                screen.resumedSendError = null
                                screen.resumedCloseError = null
                                route = Route.Resumed
                            }
                        } catch (e: Exception) {
                            mainHandler.post {
                                // Typed refusals stay honest, word for word.
                                screen.resumedSendError = describeResumeFailure(e)
                            }
                        }
                    }
                },
                onStartNew = { sessions ->
                    // S24-5: the repositories derive from what the list
                    // already read — nothing is scanned or invented.
                    screen.sessionsForStart = sessions
                    screen.startSessionError = null
                    screen.startingSession = false
                    route = Route.StartSession
                },
                onBack = {
                    releaseHeldLease(screen)
                    // The sessions screen opens from the host's own screen
                    // (S34); back returns there.
                    route = Route.Host
                },
                sortByName = advanced.sortSessionsByName,
            )
            return
        }
        // No live profile behind the route (the connection was reset): fall
        // back to the Nodes screen instead of rendering a dead screen.
        route = Route.Main
    }

    // S26-2: the host's workspaces — the human's repositories with their
    // branch, state and agent count, and the `+` that starts a project in a
    // path that has no history yet (S26-3). Loading and every action ride
    // the live connection's request lane; nothing here invents a row.
    if (route == Route.Workspaces) {
        val currentUi = screen.ui
        if (currentUi is UiState.Connected) {
            val wsProfile = screen.profile
            LaunchedEffect(Unit) {
                loadWorkspaces(screen, currentUi.connection)
            }
            WorkspacesScreen(
                rows = screen.workspaces,
                loading = screen.workspacesLoading,
                error = screen.workspacesError,
                needsYouByWorkspace = WorkspacesModel.needsYouByWorkspace(
                    currentUi.snapshot?.agents ?: emptyList(),
                ),
                browseOpen = screen.browseOpen,
                browseLevel = screen.browseLevel,
                disks = screen.disks,
                disksLoading = screen.disksLoading,
                disksError = screen.disksError,
                folders = screen.folders,
                foldersLoading = screen.foldersLoading,
                foldersError = screen.foldersError,
                checking = screen.addChecking,
                verdict = screen.addVerdict,
                creating = screen.creatingWorkspace,
                createError = screen.createWorkspaceError,
                onOpen = { row -> openWorkspace(screen, currentUi.connection, row) },
                onBrowseOpened = { openBrowse(screen, currentUi.connection) },
                onSelectDisk = { disk ->
                    screen.browseLevel = WorkspacesModel.selectDisk(screen.browseLevel, disk)
                    loadFolders(screen, currentUi.connection, disk.mountPoint)
                },
                onSelectFolder = { folder ->
                    // A row tap only moves the browser (T27): descending is
                    // navigation, the choice is the screen's one accent
                    // action — never a side effect of moving.
                    screen.browseLevel = WorkspacesModel.selectFolder(screen.browseLevel, folder)
                    loadFolders(screen, currentUi.connection, folder.path)
                },
                onChooseFolder = {
                    screen.addVerdict = null
                    screen.createWorkspaceError = null
                    if (wsProfile != null) {
                        // Choosing a folder is the action: the same
                        // validated create the typed path used, from the
                        // folder the human is standing in.
                        addProject(screen, currentUi.connection, wsProfile, WorkspacesModel.chosenPath(screen.browseLevel))
                    }
                },
                onBrowseBack = {
                    // The state machine decides: a nested folder climbs to its
                    // parent, a mount point goes back to the disk list, and the
                    // disk list asks for `null` (close) — the close handler
                    // answers instead. Climbing re-reads the parent, so the
                    // list never keeps showing the folder we just left.
                    when (
                        val target = WorkspacesModel
                            .browseBack(screen.browseLevel, screen.disks.orEmpty().map { it.mountPoint })
                    ) {
                        null -> Unit
                        WorkspacesModel.BrowseState.Disks -> screen.browseLevel = WorkspacesModel.BrowseState.Disks
                        is WorkspacesModel.BrowseState.InFolder -> {
                            screen.browseLevel = target
                            loadFolders(screen, currentUi.connection, target.path)
                        }
                    }
                },
                onCloseBrowse = {
                    screen.browseOpen = false
                    screen.addVerdict = null
                    screen.createWorkspaceError = null
                },
                onBack = {
                    // Leaving the screen keeps the list cached in the state:
                    // re-entering re-reads it, but a mid-add pause does not
                    // lose the work. Workspaces opens from the host's own
                    // screen (S34); back returns there.
                    route = Route.Host
                },
            )
            return
        }
        // No live connection behind the route: back to the host list.
        route = Route.Main
    }

    // S24-5: start a fresh session in a repository with history — the list
    // shows where the host's sessions already worked, and one accent action
    // spawns `gentle-shell --mode rpc` in that directory over the pinned
    // connection. The lease is taken by the core under the session id the
    // child itself reports, once the session exists.
    if (route == Route.StartSession) {
        val startProfile = screen.profile
        val startDeviceId = screen.deviceLeaseId
        if (startProfile != null && startDeviceId != null) {
            StartSessionScreen(
                sessions = screen.sessionsForStart,
                starting = screen.startingSession,
                startError = screen.startSessionError,
                onStartRepo = { dir -> startFreshSession(screen, startProfile, startDeviceId, dir, onDone = { route = Route.Resumed }) },
                onStartPath = { dir -> startFreshSession(screen, startProfile, startDeviceId, dir, onDone = { route = Route.Resumed }) },
                onBack = { route = Route.Sessions },
            )
            return
        }
        // No live profile behind the route: back to the Nodes screen.
        route = Route.Main
    }

    // T17b (S15/S17): the resumed session — transcript read-only, prompts
    // through the RPC lane, dialogs answered, close releases the lease.
    if (route == Route.Resumed) {
        val resumed = screen.resumedSession
        if (resumed != null) {
            // Poll the pump's visible state without extra network traffic:
            // the driver updates its shared store on every record.
            LaunchedEffect(resumed) {
                while (true) {
                    screen.resumedTranscript = resumed.transcript()
                    kotlinx.coroutines.delay(500)
                }
            }
            ResumedSessionScreen(
                sessionId = resumed.sessionId(),
                messageCount = screen.resumedMessageCount,
                transcript = screen.resumedTranscript,
                reclaimed = screen.resumedReclaimed,
                isNew = screen.resumedIsNew,
                pendingDialog = resumed.pendingDialog(),
                draft = screen.resumedDraft,
                onDraftChange = { screen.resumedDraft = it },
                sending = screen.resumedSending,
                sendError = screen.resumedSendError,
                closed = resumed.isClosed(),
                closeError = screen.resumedCloseError ?: resumed.lastError(),
                onSend = {
                    val text = screen.resumedDraft
                    if (text.isNotBlank() && !screen.resumedSending) {
                        screen.resumedSending = true
                        screen.resumedSendError = null
                        executor.execute {
                            try {
                                val transcript = resumed.sendPrompt(text)
                                mainHandler.post {
                                    screen.resumedTranscript = transcript
                                    screen.resumedMessageCount = resumed.messageCount()
                                    screen.resumedDraft = ""
                                    screen.resumedSending = false
                                }
                            } catch (e: Exception) {
                                mainHandler.post {
                                    screen.resumedSendError = describeResumeFailure(e)
                                    screen.resumedSending = false
                                }
                            }
                        }
                    }
                },
                onAnswerDialog = { dialogId, cancelled, value ->
                    executor.execute {
                        try {
                            resumed.answerDialog(dialogId, cancelled, value, null)
                        } catch (_: Exception) {
                        }
                    }
                },
                onClose = {
                    executor.execute {
                        var closeError: String? = null
                        try {
                            resumed.closeSession()
                        } catch (e: Exception) {
                            closeError = e.message ?: e.toString()
                        }
                        mainHandler.post {
                            screen.resumedCloseError = closeError
                            releaseHeldLease(screen)
                            route = Route.Sessions
                        }
                    }
                },
                onBack = {
                    // Back without closing keeps the pump and the lease —
                    // the session continues while the app holds it (S18);
                    // the row still says held by this device.
                    route = Route.Sessions
                },
            )
            return
        }
        // The session is gone (teardown): back to the list.
        route = Route.Sessions
    }

    // Pairing link paste + QR scan.
    var pairLink by remember { mutableStateOf("") }
    var pairError by remember { mutableStateOf<String?>(null) }
    var cameraMessage by remember { mutableStateOf<String?>(null) }
    // S25-1: per-field validation messages — one per field, set on submit.
    var hostError by remember { mutableStateOf<String?>(null) }
    var portError by remember { mutableStateOf<String?>(null) }
    var userError by remember { mutableStateOf<String?>(null) }
    // S25-3: the fingerprint an `fp=` link pinned — shown before
    // connecting and enforced at the handshake. It changes only when a
    // new link is applied; a refusal still fails closed on any mismatch.
    var linkFingerprint by remember { mutableStateOf<String?>(null) }
    // One-scan pairing (S22): when the applied link carries a pairing
    // endpoint + token, Connect first POSTs this phone's key to the
    // listener, then opens SSH. The paste field runs the same flow.
    var pairParams by remember { mutableStateOf<PairParams?>(null) }
    var pairStatus by remember { mutableStateOf<String?>(null) }
    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            cameraMessage = null
            screen.ui = UiState.Scanning
        } else {
            cameraMessage = context.getString(R.string.pair_camera_denied)
        }
    }

    // S24-1: the pairing refusals, built in the stored locale.
    val pairInvalid = { reason: PairLink.InvalidReason ->
        val id = when (reason) {
            PairLink.InvalidReason.EMPTY -> R.string.pair_invalid_empty
            PairLink.InvalidReason.NOT_GC -> R.string.pair_invalid_not_gc
            PairLink.InvalidReason.MISSING_QUERY -> R.string.pair_invalid_missing_query
            PairLink.InvalidReason.TOKEN_WITHOUT_PAIR -> R.string.pair_invalid_token_without_pair
            PairLink.InvalidReason.PAIR_WITHOUT_TOKEN -> R.string.pair_invalid_pair_without_token
            PairLink.InvalidReason.BAD_TOKEN -> R.string.pair_invalid_bad_token
            PairLink.InvalidReason.BAD_PAIR_HOSTPORT -> R.string.pair_invalid_bad_pair_hostport
            PairLink.InvalidReason.BAD_AUTHORITY -> R.string.pair_invalid_bad_authority
            PairLink.InvalidReason.BAD_HOST_NAME -> R.string.pair_invalid_bad_host
            PairLink.InvalidReason.BAD_PORT -> R.string.pair_invalid_bad_port
            PairLink.InvalidReason.MISSING_USER -> R.string.pair_invalid_missing_query
            PairLink.InvalidReason.BAD_USER_URL -> R.string.pair_invalid_bad_user_url
            PairLink.InvalidReason.BAD_USER_NAME -> R.string.pair_invalid_bad_user_name
            PairLink.InvalidReason.UNSUPPORTED_VERSION -> R.string.pair_invalid_version
            PairLink.InvalidReason.BAD_FINGERPRINT -> R.string.pair_invalid_fp
        }
        context.getString(id)
    }
    // S25-1: the same one-message-per-field rule for the form validator.
    val fieldMessage = { reason: ConnectValidator.Reason ->
        val id = when (reason) {
            ConnectValidator.Reason.HOST_EMPTY -> R.string.pair_field_host_empty
            ConnectValidator.Reason.HOST_INVALID -> R.string.pair_field_host_invalid
            ConnectValidator.Reason.PORT_INVALID -> R.string.pair_field_port_invalid
            ConnectValidator.Reason.USER_EMPTY -> R.string.pair_field_user_empty
            ConnectValidator.Reason.USER_INVALID -> R.string.pair_field_user_invalid
        }
        context.getString(id)
    }
    val pairRefusal = { status: Int, detail: String? ->
        val suffix = detail?.let { context.getString(R.string.pair_err_detail_suffix, it) }.orEmpty()
        when (status) {
            401 -> context.getString(R.string.pair_err_token) + suffix
            403 -> context.getString(R.string.pair_err_expired) + suffix
            409 -> context.getString(R.string.pair_err_used) + suffix
            400 -> detail?.let { context.getString(R.string.pair_err_key_detail, it) }
                ?: context.getString(R.string.pair_err_key_malformed)
            else -> context.getString(R.string.pair_err_other, status) + suffix
        }
    }
    val pairUnreachable = { pairHost: String, pairPort: String, reach: PairClient.Reach ->
        val where = "$pairHost:$pairPort"
        when (reach) {
            PairClient.Reach.NETWORK -> context.getString(R.string.pair_err_network, where)
            PairClient.Reach.NOT_LISTENING -> context.getString(R.string.pair_err_not_listening, where)
            PairClient.Reach.UNKNOWN_HOST -> context.getString(R.string.pair_err_unknown_host, pairHost)
        }
    }

    fun applyPairLink(raw: String): Boolean {
        val parsed = PairLink.parse(raw, pairInvalid)
        return when (parsed) {
            is PairLinkResult.Parsed -> {
                host = parsed.host
                port = parsed.port
                user = parsed.user
                pairParams = parsed.pair
                linkFingerprint = parsed.fingerprint
                hostError = null
                portError = null
                userError = null
                pairLink = ""
                pairError = null
                true
            }
            is PairLinkResult.Invalid -> {
                pairError = parsed.message
                false
            }
        }
    }

    fun connect(acceptChallenge: UiState.Challenge?, pair: PairParams?) {
        // S24-4: one live connection at a time. A connect while another host
        // is live switches hosts: the old link goes down cleanly first —
        // its pollers stop with the generation bump below, the supervision
        // machine restarts, the foreground service stops with the state
        // change, and any advisory lease is handed back.
        (screen.ui as? UiState.Connected)?.connection?.let { existing ->
            executor.execute {
                try {
                    existing.disconnect()
                } catch (_: Exception) {
                }
            }
        }
        (screen.ui as? UiState.Session)?.connection?.let { existing ->
            executor.execute {
                try {
                    existing.disconnect()
                } catch (_: Exception) {
                }
            }
        }
        releaseHeldLease(screen)
        screen.generation += 1
        val generation = screen.generation
        // S25-3: the fingerprint pinned by the last applied link, enforced
        // at the handshake below. Captured here so the worker sees one
        // consistent value.
        val linkFp = linkFingerprint
        // Supervision (T8c): a new attempt begins — first connect, manual
        // retry, or the one automatic foreground attempt. The challenge accept
        // continues the attempt already in flight, so it does not re-begin.
        if (acceptChallenge == null && screen.machine.state !is SupervisionState.Connecting) {
            screen.machine.begin()
        }
        screen.ui = UiState.Connecting
        pairStatus = pair?.let {
            context.getString(R.string.pair_status_pairing, "${it.host}:${it.port}")
        }
        // S24-4: host-key trust is per host. Each saved host owns its own
        // known_hosts file — a name derived from the exact host:port — so a
        // pin on one machine can never satisfy another, even two entries
        // pointing at the same machine over different addresses.
        val knownHostsPath = File(
            context.filesDir,
            KnownHostsFile.fileName(host, port.trim().toIntOrNull() ?: 0),
        ).absolutePath
        val profile = HostProfileDto(
            host = host,
            port = port.toUShortOrNull() ?: 0u,
            user = user,
            identityPath = keyPath,
            knownHostsPath = knownHostsPath,
        )
        // T17a: the Sessions screen rides this profile and this device id.
        screen.profile = profile
        screen.deviceLeaseId = identity?.publicKeyLine?.let { SessionsModel.deviceLeaseId(it) }
        val challenge = acceptChallenge
        val keyLine = identity?.publicKeyLine
        executor.execute {
            try {
                if (pair != null) {
                    if (keyLine == null) {
                        mainHandler.post {
                            screen.ui = UiState.Failed(
                                context.getString(R.string.pair_no_key_yet),
                            )
                        }
                        return@execute
                    }
                    when (
                        val reply = PairClient.sendKey(
                            pair.host,
                            pair.port,
                            pair.token,
                            keyLine,
                            pairUnreachable,
                            pairRefusal,
                        )
                    ) {
                        is PairClient.Result.Refused -> {
                            mainHandler.post {
                                if (screen.generation == generation) {
                                    screen.machine.onRefused(reply.message)
                                    screen.ui = UiState.Failed(reply.message)
                                }
                            }
                            // Never fall through to SSH: the key was not
                            // trusted, the connect would fail with a second
                            // error that hides the pairing reason.
                            return@execute
                        }
                        is PairClient.Result.Accepted -> mainHandler.post {
                            if (screen.generation == generation) {
                                // The token burned on use: the key is trusted
                                // now, later connects go straight to SSH.
                                pairParams = null
                                pairStatus = context.getString(R.string.pair_status_accepted)
                            }
                        }
                    }
                }
                if (challenge != null) {
                    acceptHostKey(knownHostsPath, profile.host, profile.port, challenge.keyBlob)
                }
                if (!File(keyPath).exists()) {
                    mainHandler.post {
                        screen.ui = UiState.Failed(
                            context.getString(R.string.pair_no_key_file, keyPath),
                        )
                    }
                    return@execute
                }
                var outcome = coreConnect(profile, session, DESKTOP_SURFACE)
                // S25-3: a link that carried fp= pinned the host key before
                // connecting. On a match the question is already answered —
                // the user compared the fingerprint the host printed when
                // scanning — so accept and continue; on a mismatch refuse
                // and say why, never a second look-alike prompt.
                if (outcome is ConnectOutcome.Challenge && linkFp != null) {
                    when (val verdict = FingerprintCheck.compare(linkFp, outcome.v1.fingerprint)) {
                        is FingerprintCheck.Verdict.Match -> {
                            acceptHostKey(knownHostsPath, profile.host, profile.port, outcome.v1.keyBlob)
                            outcome = coreConnect(profile, session, DESKTOP_SURFACE)
                        }
                        is FingerprintCheck.Verdict.Mismatch -> {
                            val message = context.getString(
                                R.string.pair_fp_mismatch,
                                FingerprintCheck.hexToOpenSsh(verdict.linkFingerprint)
                                    ?: verdict.linkFingerprint,
                                verdict.presented,
                            )
                            mainHandler.post {
                                if (screen.generation == generation) {
                                    screen.machine.onRefused(message)
                                    screen.ui = UiState.Failed(message)
                                }
                            }
                            // Never fall through to SSH against a host the
                            // link does not name.
                            return@execute
                        }
                    }
                }
                when (outcome) {
                    is ConnectOutcome.Challenge -> mainHandler.post {
                        if (screen.generation == generation) {
                            // Still the same attempt, paused for the human.
                            screen.ui = UiState.Challenge(
                                fingerprint = outcome.v1.fingerprint,
                                pinned = outcome.v1.pinnedFingerprint,
                                keyBlob = outcome.v1.keyBlob,
                            )
                        }
                    }
                    is ConnectOutcome.Connected -> {
                        val connection = outcome.v1
                        mainHandler.post {
                            if (screen.generation == generation) {
                                // A new connection is a fresh baseline: every
                                // session's grid and read start empty again,
                                // and the host row — not the inbox — leads.
                                // The alert brain re-primes too: the first
                                // snapshot of THIS connection is the baseline
                                // (an agent already in needs you never alerts).
                                // The last-used clock does the same (S24-8).
                                screen.sessions.values.forEach { it.resetBaseline() }
                                screen.alertTracker.reset()
                                screen.activity.reset()
                                // T31: the approval board observes nothing about
                                // this connection yet — no waits, no reads, no walk.
                                screen.approvalWait.reset()
                                screen.approvalParses = emptyMap()
                                screen.approvalWalk = false
                                screen.approvalWalkIndex = 0
                                screen.approvalHold = null
                                screen.approvalBusy = false
                                screen.approvalError = null
                                screen.inboxOpen = false
                                // S24-4: a successful connection saves the
                                // host with its name — the human's own words,
                                // falling back to user@host when the field
                                // was left empty. Same address = a rename.
                                hostsStore.save(
                                    SavedHostEntry(
                                        name = hostName,
                                        host = host,
                                        port = port,
                                        user = user,
                                    ),
                                )
                                refreshSavedHosts()
                                screen.machine.onLive()
                                screen.ui = UiState.Connected(connection, snapshot = null)
                                // S34: connecting first, then its screen — a
                                // successful connect opens that host's own
                                // screen, where its things live.
                                route = Route.Host
                                // T15 (S11): the foreground service keeps the
                                // SSH link alive in the background, and the
                                // slow alert poller watches for transitions.
                                if (screen.serviceGate.onConnected(host)) {
                                    ContextCompat.startForegroundService(
                                        context,
                                        ConnectionService.connectionIntent(context, host),
                                    )
                                }
                                pollAlerts(context, screen, connection)
                                // T31 (S32b): the approval board's slow reads ride
                                // the same pollers pattern — registered for the
                                // connection's life, doing nothing when the board
                                // is closed or no agent is waiting.
                                pollApprovals(screen, connection, generation)
                            }
                        }
                        poll(screen, connection, generation)
                    }
                }
            } catch (error: Exception) {
                val message = describe(context, error)
                mainHandler.post {
                    // A refused attempt fails with its reason and never
                    // retries on its own (T8c).
                    if (screen.generation == generation) {
                        screen.machine.onRefused(message)
                    }
                    screen.ui = UiState.Failed(message)
                }
            }
        }
    }

    /** Open one saved host (S24-4): switching hosts disconnects the live one
     *  cleanly first — supervision stops, the foreground service stops, the
     *  pollers stop — then the same connect path runs for the tapped entry. */
    fun connectTo(entry: SavedHostEntry) {
        disconnectLive()
        hostName = entry.name
        host = entry.host
        port = entry.port
        user = entry.user
        hostError = null
        portError = null
        userError = null
        pairParams = null
        linkFingerprint = null
        pairStatus = null
        pairLink = ""
        pairError = null
        askForNotifications()
        gated { connect(acceptChallenge = null, pair = null) }
    }

    /** Open the Pair/Connect screen from the host list (S24-4). The screen
     *  starts from the last-used form; a successful connection saves the
     *  host with its name. One live connection at a time: opening the form
     *  from a live host disconnects it first, honestly. */
    fun openAddHost() {
        disconnectLive()
        hostError = null
        portError = null
        userError = null
        pairParams = null
        linkFingerprint = null
        pairStatus = null
        screen.ui = UiState.Form
    }

    // Foreground return (T8c + T41): the machine decides. A drop observed
    // while the app was backgrounded is never latched — the return probes the
    // live connection first, and only a failed probe surfaces the state; a
    // successful probe clears the stale drop with no reconnect. An
    // interrupted link gets exactly one automatic attempt, never a loop.
    fun onAppForeground() {
        when (screen.machine.onForegrounded()) {
            ForegroundAction.Retry -> {
                connect(acceptChallenge = null, pair = null)
            }
            ForegroundAction.Probe -> {
                val connection = when (val current = screen.ui) {
                    is UiState.Connected -> current.connection
                    is UiState.Session -> current.connection
                    else -> null
                }
                if (connection == null) return
                val generation = screen.generation
                executor.execute {
                    val dead = try {
                        connection.isDisconnected()
                    } catch (_: Exception) {
                        true
                    }
                    if (dead) {
                        mainHandler.post {
                            if (screen.generation == generation) {
                                screen.machine.onDropped(
                                    AppLocale.wrapStored(screen.appContext)
                                        .getString(R.string.session_reason_link_down),
                                )
                            }
                        }
                    } else {
                        mainHandler.post {
                            if (screen.generation == generation) {
                                screen.machine.onProbeAlive()
                            }
                        }
                        // Cheap probe: ping now; a host that stays silent
                        // ends the connection within a few seconds, and
                        // the pollers report the interruption.
                        try {
                            connection.probeLiveness()
                        } catch (_: Exception) {
                        }
                    }
                }
            }
            ForegroundAction.Nothing -> {}
        }
    }

    // ON_RESUME is the supervision trigger: verify liveness or retry once,
    // instead of trusting a socket that may have died unnoticed. ON_PAUSE
    // flips the visibility flag the notification suppression reads (T15):
    // the agent on screen never alerts, the background always may.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    screen.appInForeground = true
                    notificationsGranted = notificationPermissionGranted(context)
                    onAppForeground()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    screen.appInForeground = false
                    screen.machine.onBackgrounded()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // T15 (S11): the foreground service runs exactly while the supervision
    // machine says connected — one start per connection, one stop when the
    // link drops, fails, or the user disconnects. The unit-tested gate
    // (ForegroundServiceGate) owns the pairing, so a drop during a retry
    // never leaves an orphaned notification behind.
    LaunchedEffect(Unit) {
        snapshotFlow { screen.machine.state }.collect { state ->
            if (state is SupervisionState.Connected) {
                if (screen.serviceGate.onConnected(host)) {
                    ContextCompat.startForegroundService(
                        context,
                        ConnectionService.connectionIntent(context, host),
                    )
                }
            } else if (screen.serviceGate.onConnectionEnded()) {
                context.stopService(Intent(context, ConnectionService::class.java))
            }
        }
    }

    // T15 (S11): the notification's Disconnect action reaches the screen
    // that owns the live connection. Unregistered when this screen goes
    // away, so a stale teardown can never fire.
    DisposableEffect(Unit) {
        ForegroundHub.userDisconnect = {
            disconnectLive()
            route = Route.Main
        }
        onDispose { ForegroundHub.userDisconnect = null }
    }

    // T15 (S11): a notification tap opens that agent's session. The pane id
    // may arrive before the agent is in a snapshot (or before any
    // connection exists), so it stays pending until the connected snapshot
    // actually contains it.
    LaunchedEffect(openAgentPane.value, screen.ui) {
        val paneId = openAgentPane.value ?: return@LaunchedEffect
        val current = screen.ui
        if (current is UiState.Connected) {
            val agent = current.snapshot?.agents?.firstOrNull { it.paneId == paneId }
            if (agent != null) {
                openAgentPane.value = null
                openAgentSession(current, agent)
            }
        }
    }

    Column(
        modifier = (if (screen.ui is UiState.Session) {
            // The session screen must be sized by the window itself: inside
            // the scrollable connect column its weighted pane block gets
            // unbounded constraints and collapses to zero height.
            Modifier
        } else {
            Modifier
        })
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Real window insets: status bar at the top, IME + navigation bar
            // at the bottom, so the composer always sits above both.
            .safeDrawingPadding()
            .then(
                if (screen.ui is UiState.Session) {
                    Modifier
                } else {
                    Modifier.verticalScroll(rememberScrollState())
                }
            )
            .padding(16.dp),
    ) {
        if (screen.ui !is UiState.Session) {
            // The session screen has its own compact header; the topbar stays
            // quiet. S24-7: no product name in the chrome — the one word the
            // screen needed removed — and the single way into Settings is a
            // gear icon, labelled for screen readers, instead of the repeated
            // `Settings` word.
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                // S24-4: the form and the challenge sit on top of the host
                // list — the chevron is the way back to it. Not shown on the
                // list screens themselves (Hosts, Connected): there the chevron
                // would repeat what the screen already is.
                val backShown = screen.ui is UiState.Form ||
                    screen.ui is UiState.Connecting ||
                    screen.ui is UiState.Failed ||
                    screen.ui is UiState.Challenge
                if (backShown) {
                    val backToHosts = stringResource(R.string.cd_back_to_hosts)
                    Text(
                        text = "‹",
                        color = LocalPalette.current.accent,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clickable { screen.ui = UiState.Hosts }
                            .semantics { contentDescription = backToHosts }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { route = Route.Settings },
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = LocalPalette.current.accentEdge,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        when (val current = screen.ui) {
            is UiState.Scanning -> {
                QrScannerScreen(
                    onQr = { link ->
                        screen.ui = UiState.Form
                        val applied = applyPairLink(link)
                        // One scan runs the whole flow: post the key, then
                        // connect over SSH — the host-key challenge still
                        // shows after.
                        if (applied && pairParams != null) {
                            connect(acceptChallenge = null, pair = pairParams)
                        }
                    },
                    onCancel = { screen.ui = UiState.Form },
                )
            }
            UiState.Form, UiState.Connecting, is UiState.Failed -> {
                // --- 01-pair.html: one surface, hairline-separated blocks, ---
                // --- no nested cards, one accent action.                  ---
                Text(
                    text = stringResource(R.string.pair_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.pair_intro),
                    color = LocalPalette.current.muted,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(12.dp))
                Hairline()

                // The Connection form — the primary path, like Moshi's
                // "New connection": optional name, host + compact port,
                // user, one help line, one accent Connect. The device key
                // is already in use; the screen says which one signs
                // instead of offering a picker, and there is no password
                // or protocol selector — SSH + device key only.
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalPalette.current.accent,
                    unfocusedBorderColor = LocalPalette.current.line,
                    focusedContainerColor = LocalPalette.current.surface,
                    unfocusedContainerColor = LocalPalette.current.surface,
                    cursorColor = LocalPalette.current.accent,
                )
                OutlinedTextField(
                    value = hostName,
                    onValueChange = { hostName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.pair_form_name)) },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 13.sp,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = host,
                    onValueChange = {
                        host = it
                        // The pinned fingerprint belongs to the host KEY, not
                        // the address: editing a field keeps it, and the
                        // handshake still refuses a key the link did not name.
                        hostError = null
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.pair_form_host)) },
                    isError = hostError != null,
                    supportingText = hostError?.let { error ->
                        { Text(error, color = LocalPalette.current.error, fontSize = 11.sp) }
                    },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = {
                            port = it
                            portError = null
                        },
                        singleLine = true,
                        label = { Text(stringResource(R.string.pair_form_port)) },
                        isError = portError != null,
                        supportingText = portError?.let { error ->
                            { Text(error, color = LocalPalette.current.error, fontSize = 11.sp) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = fieldColors,
                        modifier = Modifier.weight(0.35f),
                    )
                    OutlinedTextField(
                        value = user,
                        onValueChange = {
                            user = it
                            userError = null
                        },
                        singleLine = true,
                        label = { Text(stringResource(R.string.pair_form_user)) },
                        isError = userError != null,
                        supportingText = userError?.let { error ->
                            { Text(error, color = LocalPalette.current.error, fontSize = 11.sp) }
                        },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = fieldColors,
                        modifier = Modifier.weight(0.65f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                // One line of help: the connection signs in with this
                // phone's key — no password, no protocol selector.
                Text(
                    text = stringResource(R.string.pair_form_help),
                    color = LocalPalette.current.muted,
                    fontSize = 11.sp,
                )
                // The pinned host key fingerprint, only when a link
                // carried fp=: shown before connecting (S25). When nothing
                // pinned it, this line simply does not exist.
                linkFingerprint?.let { pinned ->
                    Spacer(Modifier.height(8.dp))
                    FingerprintCheck.hexToOpenSsh(pinned)?.let { shown ->
                        Text(
                            text = stringResource(R.string.pair_fp_label) + " · " + shown,
                            color = LocalPalette.current.muted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        // Validation on submit (S25-1): one message per
                        // field, never a generic error; only a valid form
                        // reaches the connect path. The notification
                        // permission rides the connect action (T15), and
                        // the biometric gate treats connecting as the
                        // device key being used.
                        hostError = ConnectValidator.host(host, fieldMessage)
                        portError = ConnectValidator.port(port, fieldMessage)
                        userError = ConnectValidator.user(user, fieldMessage)
                        if (hostError == null && portError == null && userError == null) {
                            // The form is what the user asked to connect to:
                            // keep it, including the Name label (the multi-
                            // host work reads it from here).
                            hostStore.save(
                                SavedHost(
                                    name = hostName,
                                    host = host,
                                    port = port,
                                    user = user,
                                ),
                            )
                            askForNotifications()
                            gated { connect(acceptChallenge = null, pair = pairParams) }
                        }
                    },
                    enabled = current !is UiState.Connecting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalPalette.current.accent,
                        contentColor = LocalPalette.current.bg,
                        // A disabled action must read as disabled, not as a
                        // broken dark button: keep it legible and obviously off.
                        disabledContainerColor = LocalPalette.current.accentPressed,
                        disabledContentColor = LocalPalette.current.bg,
                    ),
                ) {
                    Text(
                        text = when {
                            current is UiState.Connecting && pairParams != null ->
                                stringResource(R.string.pair_button_pairing)
                            current is UiState.Connecting ->
                                stringResource(R.string.pair_button_connecting)
                            else -> stringResource(R.string.pair_button)
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (current is UiState.Connecting) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = pairStatus
                            ?: stringResource(R.string.pair_status_probing),
                        color = LocalPalette.current.muted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                if (current is UiState.Failed) {
                    Notice(current.message, LocalPalette.current.error)
                }
                Spacer(Modifier.height(12.dp))
                // The quiet secondary: Scan QR. One line under it — no
                // block label, no second headline (reworked screen).
                OutlinedButton(
                    onClick = {
                        cameraMessage = null
                        if (context.packageManager.hasSystemFeature(
                                android.content.pm.PackageManager.FEATURE_CAMERA_ANY,
                            )
                        ) {
                            cameraPermission.launch(android.Manifest.permission.CAMERA)
                        } else {
                            cameraMessage = context.getString(R.string.pair_no_camera)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LocalPalette.current.muted,
                    ),
                ) {
                    Text(stringResource(R.string.pair_scan_qr))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.pair_scan_hint),
                    color = LocalPalette.current.muted,
                    fontSize = 11.sp,
                )
                cameraMessage?.let { message ->
                    Spacer(Modifier.height(6.dp))
                    Text(text = message, color = LocalPalette.current.muted, fontSize = 12.sp)
                }
                if (notificationsAsked && notificationsGranted == null) {
                    // The request is up: the explanation the honest flow
                    // needs, never a bare dialog.
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.pair_notifications_explaining),
                        color = LocalPalette.current.muted,
                        fontSize = 11.sp,
                    )
                }
            }

            is UiState.Challenge -> {
                Text(
                    text = stringResource(R.string.chal_verify),
                    color = LocalPalette.current.muted,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.chal_presents),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = current.fingerprint,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LocalPalette.current.surfaceRaised, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                )
                if (current.pinned != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.chal_pinned_differs, current.pinned),
                        color = LocalPalette.current.error,
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { connect(acceptChallenge = current, pair = pairParams) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalPalette.current.accent,
                        contentColor = LocalPalette.current.bg,
                        // A disabled action must read as disabled, not as a
                        // broken dark button: keep it legible and obviously off.
                        disabledContainerColor = LocalPalette.current.line,
                        disabledContentColor = LocalPalette.current.muted,
                    ),
                ) {
                    Text(text = stringResource(R.string.chal_accept), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { screen.ui = UiState.Form },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LocalPalette.current.muted,
                    ),
                ) {
                    Text(text = stringResource(R.string.chal_cancel))
                }
            }

            is UiState.Connected -> {
                // Supervision (T8c): the interruption lives in the machine,
                // not in a flag that can drift. The last snapshot stays on
                // screen behind the board's interrupted notice, and Retry
                // now re-runs the connect path.
                val interrupted = screen.machine.state as? SupervisionState.Interrupted
                if (interrupted != null) {
                    InterruptedNotice(
                        reason = interrupted.reason,
                        onRetry = { gated { connect(acceptChallenge = null, pair = pairParams) } },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (screen.inboxOpen) {
                    InboxSection(
                        screen = screen,
                        current = current,
                        onBackToWorkspaces = { route = Route.Workspaces },
                        onDisconnect = { disconnectLive() },
                        onOpenAgent = { openAgentSession(current, it) },
                    )
                } else {
                    // --- 02-nodes.html + S34: the hosts screen is a    ---
                    // --- clean list: one hairline row per host, the    ---
                    // --- live one first, Rename on each, Add as the    ---
                    // --- one accent action. Workspaces, Session        ---
                    // --- history, the connection state and the         ---
                    // --- disconnect action live inside the host's      ---
                    // --- own screen (Route.Host).                      ---
                    val snapshot = current.snapshot
                    if (snapshot == null) {
                        Text(
                            text = stringResource(R.string.nodes_waiting),
                            color = LocalPalette.current.muted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                    // The host list itself never waits for the first snapshot:
                    // its rows are local data (S24-4), and only the agent
                    // count used to need the host's answer.
                    Text(
                        text = stringResource(R.string.nodes_your_hosts),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    val rows = HostsModel.listRows(
                        screen.hosts,
                        live = HostsModel.liveEntry(screen.hosts, host, port),
                        liveState = if (interrupted != null) {
                            HostScreenState.Interrupted
                        } else {
                            HostScreenState.Connected
                        },
                    )
                    HostRows(
                        rows = rows,
                        onOpen = { row ->
                            if (row.state == HostRowState.Saved) {
                                connectTo(row.entry)
                            } else {
                                route = Route.Host
                            }
                        },
                        onMore = { entry -> hostMenuTarget = entry },
                    )
                    Spacer(Modifier.height(24.dp))
                        // The one accent action of the list (S24-4): add a
                        // host. It opens the Pair/Connect screen; the new
                        // host is saved with its name on a successful
                        // connection. Adding from a live host disconnects it
                        // first — one live connection at a time.
                        Button(
                            onClick = { openAddHost() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LocalPalette.current.accent,
                                contentColor = LocalPalette.current.bg,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.hosts_add),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
            }

            UiState.Hosts -> {
                // --- 02-nodes.html: the saved host list (S24-4). One surface,
                // hairline-free rows, the name the human gave each machine as
                // the title and user@host:port as the mono meta. The one
                // accent action adds a host.
                Text(
                    text = stringResource(R.string.nodes_your_hosts),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                if (screen.hosts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.hosts_empty),
                        color = LocalPalette.current.muted,
                        fontSize = 13.sp,
                    )
                } else {
                    HostRows(
                        rows = HostsModel.listRows(
                            screen.hosts,
                            live = null,
                            liveState = HostScreenState.Unconnected,
                        ),
                        onOpen = { row -> connectTo(row.entry) },
                        onMore = { entry -> hostMenuTarget = entry },
                    )
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { openAddHost() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalPalette.current.accent,
                        contentColor = LocalPalette.current.bg,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.hosts_add),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            is UiState.Session -> {
                val sessionUi = screen.sessions.getOrPut(current.agent.paneId) { SessionUi() }
                val profile = screen.profile
                // T49: the camera and clip pickers behind the terminal's
                // composer — the photo picker needs no permission; the camera
                // intent writes into a provider-owned cache file.
                val cameraUri = remember { mutableStateOf<Uri?>(null) }
                val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri != null) {
                        startAttachmentUpload(context, current.connection, sessionUi, uri)
                    }
                }
                val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
                    val uri = cameraUri.value
                    cameraUri.value = null
                    if (taken && uri != null) {
                        startAttachmentUpload(context, current.connection, sessionUi, uri)
                    }
                }
                val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri != null) {
                        startAttachmentUpload(context, current.connection, sessionUi, uri)
                    }
                }
                fun openCamera() {
                    val dir = java.io.File(context.cacheDir, "attachments").apply { mkdirs() }
                    val file = java.io.File(dir, "camera-${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.attachments", file)
                    cameraUri.value = uri
                    try {
                        cameraLauncher.launch(uri)
                    } catch (error: Exception) {
                        sessionUi.attachmentError = context.getString(R.string.session_no_camera_app)
                    }
                }
                fun openPicker() {
                    try {
                        pickLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    } catch (error: Exception) {
                        sessionUi.attachmentError = context.getString(R.string.session_no_gallery_app)
                    }
                }
                fun openFile() {
                    try {
                        fileLauncher.launch(arrayOf("*/*"))
                    } catch (error: Exception) {
                        sessionUi.attachmentError = context.getString(R.string.session_no_file_app)
                    }
                }
                val paneRows = PaneSwitcherModel.rows(
                    panes = sessionUi.panes,
                    agents = sessionUi.agents,
                    tabs = sessionUi.tabs,
                    workspaceId = current.agent.workspaceId,
                    currentPaneId = current.agent.paneId,
                )
                val tabRows = PaneSwitcherModel.tabRows(
                    tabs = sessionUi.tabs,
                    workspaceId = current.agent.workspaceId,
                )
                TerminalScreen(
                    title = AgentRowModel.sessionTitle(current.agent.cwd, current.agent.name),
                    theme = appTheme,
                    session = screen.terminalSession,
                    starting = screen.terminalStarting,
                    error = screen.terminalError,
                    attachments = sessionUi.attachments,
                    thumbnails = sessionUi.thumbnails,
                    paneRows = paneRows,
                    tabRows = tabRows,
                    tabError = sessionUi.tabError,
                    workspaces = screen.workspaces ?: emptyList(),
                    agents = sessionUi.agents,
                    currentWorkspaceId = current.agent.workspaceId,
                    currentPaneId = current.agent.paneId,
                    onStart = { cols, rows ->
                        if (profile != null && screen.terminalSession == null && !screen.terminalStarting) {
                            screen.terminalStarting = true
                            screen.terminalError = null
                            val cwd = screen.terminalCwd.orEmpty()
                            executor.execute {
                                try {
                                    val started = coreStartTerminal(profile, cwd, cols.toUInt(), rows.toUInt())
                                    mainHandler.post {
                                        screen.terminalSession = started
                                        screen.terminalStarting = false
                                        screen.terminalError = null
                                    }
                                } catch (e: Exception) {
                                    mainHandler.post {
                                        screen.terminalStarting = false
                                        screen.terminalError = describe(context, e)
                                    }
                                }
                            }
                        }
                    },
                    onReconnect = { cols, rows ->
                        if (profile != null && !screen.terminalStarting) {
                            screen.terminalSession?.closeSession()
                            screen.terminalSession = null
                            screen.terminalStarting = true
                            screen.terminalError = null
                            val cwd = screen.terminalCwd.orEmpty()
                            executor.execute {
                                try {
                                    val started = coreStartTerminal(profile, cwd, cols.toUInt(), rows.toUInt())
                                    mainHandler.post {
                                        screen.terminalSession = started
                                        screen.terminalStarting = false
                                        screen.terminalError = null
                                    }
                                } catch (e: Exception) {
                                    mainHandler.post {
                                        screen.terminalStarting = false
                                        screen.terminalError = describe(context, e)
                                    }
                                }
                            }
                        }
                    },
                    onBack = {
                        screen.terminalSession?.closeSession()
                        screen.terminalSession = null
                        screen.terminalError = null
                        screen.generation += 1
                        screen.ui = UiState.Connected(current.connection, snapshot = null)
                        poll(screen, current.connection, screen.generation)
                        route = Route.Workspaces
                    },
                    onCreateTab = {
                        val connection = current.connection
                        val generation = screen.generation
                        executor.execute {
                            val failure = try {
                                connection.createTab(current.agent.workspaceId, true)
                                null
                            } catch (error: Exception) {
                                describe(context, error)
                            }
                            mainHandler.post {
                                if (screen.generation == generation) {
                                    sessionUi.tabError = failure
                                }
                            }
                        }
                    },
                    onDeleteTab = { tabRow ->
                        val connection = current.connection
                        val generation = screen.generation
                        executor.execute {
                            val failure = try {
                                connection.closeTab(tabRow.tabId)
                                null
                            } catch (error: Exception) {
                                describe(context, error)
                            }
                            mainHandler.post {
                                if (screen.generation == generation) {
                                    sessionUi.tabError = failure
                                }
                            }
                        }
                    },
                    onPaneSelected = { row ->
                        val connection = current.connection
                        val generation = screen.generation
                        executor.execute {
                            val failure = try {
                                connection.focusPane(row.paneId)
                                null
                            } catch (error: Exception) {
                                describe(context, error)
                            }
                            mainHandler.post {
                                if (screen.generation == generation) {
                                    sessionUi.tabError = failure
                                }
                            }
                        }
                    },
                    onCamera = { openCamera() },
                    onPickImage = { openPicker() },
                    onPickFile = { openFile() },
                    onRetryUpload = { id ->
                        val uri = sessionUi.attachmentUris[id]
                        if (uri != null) {
                            sessionUi.attachmentQueue.retry(id)
                            sessionUi.syncAttachments()
                            uploadAttachment(context, current.connection, sessionUi, id, uri)
                        }
                    },
                    onRemoveAttachment = { id ->
                        sessionUi.attachmentQueue.remove(id)
                        sessionUi.thumbnails.remove(id)
                        sessionUi.attachmentUris.remove(id)
                        sessionUi.syncAttachments()
                    },
                    onAttachmentsConsumed = { sessionUi.clearSentAttachments() },
                    cursorMode = advanced.cursorMode,
                    cursorBlink = advanced.cursorBlink,
                    useComposer = advanced.useComposer,
                    onUseComposerChange = { onSetAdvanced(advanced.copy(useComposer = it)) },
                    sendOnEnter = advanced.sendOnEnter,
                    shortcutPad = shortcutPad,
                    dictationEngine = resolvedEngine,
                    autoSend = voz.autoSend,
                    activeModelPath = activeModelPath,
                    replacements = voz.replacements,
                )
            }
        }

        hostMenuTarget?.let { target ->
            HostMenuDialog(
                entry = target,
                onEdit = { entry ->
                    hostMenuTarget = null
                    editTarget = entry
                    editName = entry.name
                    editHost = entry.host
                    editPort = entry.port
                    editUser = entry.user
                },
                onDelete = { entry ->
                    hostMenuTarget = null
                    deleteTarget = entry
                },
                onDismiss = { hostMenuTarget = null },
            )
        }

        editTarget?.let { target ->
            EditHostDialog(
                title = stringResource(R.string.hosts_edit_title),
                name = editName,
                host = editHost,
                port = editPort,
                user = editUser,
                hostError = ConnectValidator.host(editHost, fieldMessage),
                portError = ConnectValidator.port(editPort, fieldMessage),
                userError = ConnectValidator.user(editUser, fieldMessage),
                onNameChange = { editName = it },
                onHostChange = { editHost = it },
                onPortChange = { editPort = it },
                onUserChange = { editUser = it },
                onSave = { applyEditHost() },
                onCancel = { editTarget = null },
            )
        }

        deleteTarget?.let { target ->
            DeleteHostDialog(
                entry = target,
                onConfirm = { deleteHost(target) },
                onCancel = { deleteTarget = null },
            )
        }
    }
}

/**
 * Snapshot polling: ask the core for the latest full snapshot every 500 ms
 * while the connection is live. One poller slot is registered for the
 * poller's whole life — rescheduling keeps it, stopping closes it, and the
 * log line at every change is the teardown proof (T8c). A dead connection
 * is reported to the supervision machine: the screen keeps the last
 * snapshot, rendered as not live (board 03), and Retry now re-runs the
 * connect path.
 */
private const val POLL_INTERVAL_MS = 500L

/** The advisory lease heartbeat cadence: matches the core's
 * LEASE_HEARTBEAT_INTERVAL_SECS, so a refresh always lands well before the
 * 90 s staleness threshold (S18). */
private const val LEASE_HEARTBEAT_MS = 30_000L

private fun poll(screen: ScreenState, connection: CompanionConnection, generation: Int) {
    val slot = screen.pollers.register()
    Log.i(TAG, "snapshot poller registered, active=${screen.pollers.count}")
    fun runOnce() {
        executor.execute {
            var reschedule = false
            try {
                val snapshot = try {
                    connection.snapshot()
                } catch (error: Exception) {
                    mainHandler.post {
                        if (screen.generation == generation) {
                            screen.machine.onDropped(
                                describe(AppLocale.wrapStored(screen.appContext), error),
                            )
                        }
                    }
                    return@execute
                }
                val ended = try {
                    connection.isDisconnected()
                } catch (_: Exception) {
                    true
                }
                mainHandler.post {
                    val current = screen.ui
                    if (screen.generation == generation &&
                        current is UiState.Connected &&
                        current.connection === connection
                    ) {
                        if (ended) {
                            screen.machine.onDropped(
                                AppLocale.wrapStored(screen.appContext)
                                    .getString(R.string.session_reason_host_closed),
                            )
                        } else if (snapshot != null) {
                            screen.ui = current.copy(snapshot = snapshot)
                        }
                    }
                }
                reschedule = !ended
            } finally {
                if (!reschedule) {
                    slot.close()
                    Log.i(TAG, "snapshot poller stopped, active=${screen.pollers.count}")
                }
            }
            if (reschedule) {
                mainHandler.postDelayed({
                    val current = screen.ui
                    if (screen.generation == generation &&
                        current is UiState.Connected &&
                        current.connection === connection
                    ) {
                        runOnce()
                    } else {
                        slot.close()
                        Log.i(TAG, "snapshot poller stopped, active=${screen.pollers.count}")
                    }
                }, POLL_INTERVAL_MS)
            }
        }
    }
    runOnce()
}

/**
 * Alert polling (T15, S11): a slow, separate poller that exists only for
 * notifications. It rides the same connection and the same core snapshot
 * the UI already uses — no extra network traffic, no relay — and it does
 * not stop while a session screen is up: the phone must still tell you
 * when an agent needs you even after you opened someone else's session.
 * Deliberately slower than the UI's 500 ms snapshot poll (2 s): the shade
 * does not need render cadence, and the battery rule stands — no new
 * wakeup pattern beyond what supervision already costs. The pure rules
 * (baseline, transition-only, suppression, debounce) live in
 * AgentAlertTracker and are unit-tested.
 */
private const val ALERT_POLL_MS = 2000L

private fun pollAlerts(
    context: Context,
    screen: ScreenState,
    connection: CompanionConnection,
) {
    val slot = screen.pollers.register()
    Log.i(TAG, "alert poller registered, active=${screen.pollers.count}")
    fun runOnce() {
        executor.execute {
            var reschedule = false
            try {
                val ended = try {
                    connection.isDisconnected()
                } catch (_: Exception) {
                    true
                }
                if (!ended) {
                    val snapshot = try {
                        connection.snapshot()
                    } catch (_: Exception) {
                        null
                    }
                    if (snapshot != null) {
                        mainHandler.post {
                            // The suppression rule reads UI state, so the
                            // transition decision runs on the main thread.
                            val showingPaneId = if (screen.appInForeground) {
                                (screen.ui as? UiState.Session)?.agent?.paneId
                            } else {
                                null
                            }
                            val alerts = screen.alertTracker.onSnapshot(
                                snapshot.agents.map {
                                    TrackedAgent(it.paneId, it.name, it.cwd, it.status)
                                },
                                showingPaneId,
                            )
                            alerts.forEach { postAgentAlert(context, it) }
                        }
                    }
                }
                reschedule = !ended
            } finally {
                if (!reschedule) {
                    slot.close()
                    Log.i(TAG, "alert poller stopped, active=${screen.pollers.count}")
                }
            }
            if (reschedule) {
                mainHandler.postDelayed({
                    val ended = try {
                        connection.isDisconnected()
                    } catch (_: Exception) {
                        true
                    }
                    if (!ended) {
                        runOnce()
                    } else {
                        slot.close()
                        Log.i(TAG, "alert poller stopped, active=${screen.pollers.count}")
                    }
                }, ALERT_POLL_MS)
            }
        }
    }
    runOnce()
}

/**
 * Approval-board polling (T31, S32b): a slow poller that feeds the wait
 * tracker with every snapshot and re-reads the panes of the agents the
 * board is showing — the ones it groups as waiting — so the question, the
 * exact command and the options on screen are the pane's own current
 * words. It rides the connection the app already holds, does nothing while
 * the board is closed or nothing is waiting, and stops with the
 * connection. The tap path re-reads the pane itself (ApprovalGate), so a
 * fresh read here is display cadence, not the safety gate.
 */
private const val APPROVAL_POLL_MS = 2000L

/** How long a displayed approval stays honest before the poller re-reads
 *  the pane: the pane may have moved on since it was shown. */
private const val APPROVAL_READ_TTL_MS = 2000L

private fun pollApprovals(
    screen: ScreenState,
    connection: CompanionConnection,
    generation: Int,
) {
    val slot = screen.pollers.register()
    Log.i(TAG, "approval poller registered, active=${screen.pollers.count}")
    fun boardCurrent(): Boolean {
        val current = screen.ui
        return screen.generation == generation &&
            current is UiState.Connected &&
            current.connection === connection
    }
    fun runOnce() {
        executor.execute {
            var reschedule = false
            try {
                val ended = ended(connection)
                val snapshot = if (ended) {
                    null
                } else {
                    (screen.ui as? UiState.Connected)
                        ?.takeIf { it.connection === connection }?.snapshot
                }
                if (snapshot != null) {
                    val statuses = snapshot.agents.associate { it.paneId to it.status }
                    mainHandler.post {
                        if (boardCurrent()) {
                            screen.approvalWait.onStatuses(statuses, System.currentTimeMillis())
                        }
                    }
                }
                if (!ended && screen.inboxOpen && snapshot != null) {
                    val agents = WorkspacesModel.agentsInWorkspace(snapshot.agents, screen.workspaceFilter)
                        .filter { it.status == AgentStatusDto.BLOCKED }
                    val now = System.currentTimeMillis()
                    val stale = agents.filter { agent ->
                        val read = screen.approvalParses[agent.paneId]
                        read == null || now - read.readAtMs >= APPROVAL_READ_TTL_MS
                    }
                    for (agent in stale) {
                        try {
                            val pane = connection.readPane(agent.paneId)
                            mainHandler.post {
                                if (boardCurrent()) {
                                    screen.rememberApproval(agent.paneId, ApprovalParser.parse(pane.text))
                                }
                            }
                        } catch (error: Exception) {
                            Log.i(TAG, "approval read failed for ${agent.paneId}: ${error.message}")
                        }
                    }
                }
                reschedule = !ended
            } finally {
                if (!reschedule) {
                    slot.close()
                    Log.i(TAG, "approval poller stopped, active=${screen.pollers.count}")
                }
            }
            if (reschedule) {
                mainHandler.postDelayed({
                    if (boardCurrent() && !ended(connection)) {
                        runOnce()
                    } else {
                        slot.close()
                        Log.i(TAG, "approval poller stopped, active=${screen.pollers.count}")
                    }
                }, APPROVAL_POLL_MS)
            }
        }
    }
    runOnce()
}

/**
 * The board's answer path (S32b-3, S32b-4): the first tap on a risky
 * option only holds it — the pane is never told. On the confirming tap
 * (or any safe option), the pane is read again and the answer is sent
 * only when the question is still the one that was shown; if it changed,
 * the fresh view replaces the shown one and nothing was sent.
 */
private fun answerApproval(
    screen: ScreenState,
    connection: CompanionConnection,
    paneId: String,
    option: ApprovalParser.Option,
    shown: ApprovalParser.Parse.Question,
    generation: Int,
) {
    if (screen.approvalBusy) return
    if (
        ApprovalRisk.isRisky(option.label, shown.command, shown.question) &&
        screen.approvalHold != ApprovalHold(paneId, option.number)
    ) {
        // Held: a first tap asks for confirmation, it does not send.
        screen.approvalHold = ApprovalHold(paneId, option.number)
        return
    }
    screen.approvalHold = null
    screen.approvalBusy = true
    screen.approvalError = null
    executor.execute {
        val fresh = try {
            ApprovalParser.parse(connection.readPane(paneId).text)
        } catch (_: Exception) {
            null
        }
        mainHandler.post {
            val current = screen.ui
            if (screen.generation != generation || current !is UiState.Connected || current.connection !== connection) {
                screen.approvalBusy = false
                return@post
            }
            if (fresh != null) screen.rememberApproval(paneId, fresh)
            screen.approvalBusy = false
            when (ApprovalGate.decide(shown, fresh)) {
                ApprovalGate.Verdict.Stale -> Unit
                ApprovalGate.Verdict.Send ->
                    sendApprovalAnswer(screen, connection, paneId, option.number.toString(), generation)
            }
        }
    }
}

/** The confirmed answer: the pane's own number, then Enter — the keys a
 *  keyboard would send into that pane's dialog. */
private fun sendApprovalAnswer(
    screen: ScreenState,
    connection: CompanionConnection,
    paneId: String,
    numberText: String,
    generation: Int,
) {
    screen.approvalBusy = true
    executor.execute {
        try {
            connection.sendKeys(paneId, listOf(numberText, "enter"))
            mainHandler.post {
                val current = screen.ui
                if (screen.generation == generation && current is UiState.Connected && current.connection === connection) {
                    screen.approvalBusy = false
                    // The answer is in: the walk steps to the next waiting
                    // agent, and stops when every one has been walked to.
                    if (screen.approvalWalk) screen.approvalWalkIndex += 1
                }
            }
        } catch (error: Exception) {
            mainHandler.post {
                val current = screen.ui
                if (screen.generation == generation && current is UiState.Connected && current.connection === connection) {
                    screen.approvalBusy = false
                    screen.approvalError = paneId to describe(AppLocale.wrapStored(screen.appContext), error)
                }
            }
        }
    }
}

/** The notification permission as this device currently has it: `null`
 *  below Android 13, where there is no runtime permission to deny. */
private fun notificationPermissionGranted(context: Context): Boolean? =
    if (Build.VERSION.SDK_INT >= 33) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        null
    }

private fun describe(context: Context, error: Exception): String = when (error) {
    is CompanionException.HostKeyMismatch ->
        context.getString(R.string.err_host_key_mismatch, error.pinned, error.presented)
    is CompanionException.HostKeyUnknown ->
        context.getString(R.string.err_host_key_unknown, error.fingerprint)
    is CompanionException.RequestRefused ->
        context.getString(R.string.err_request_refused, error.code.toString(), error.detail)
    is CompanionException.UnsupportedMethod ->
        context.getString(R.string.err_unsupported_method, error.method)
    is CompanionException.Transport ->
        context.getString(R.string.err_transport, error.detail)
    is CompanionException.Timeout ->
        context.getString(R.string.err_timeout, error.detail)
    else -> error.message ?: error.javaClass.simpleName
}

/** True while this pane's session screen is still the one on display. */
private fun sessionStillCurrent(
    screen: ScreenState,
    sessionUi: SessionUi,
    paneId: String,
    generation: Int,
): Boolean {
    val current = screen.ui
    if (screen.generation != generation || current !is UiState.Session) return false
    return screen.sessions[current.agent.paneId] === sessionUi
}

/** Tabs polling (S27-7): while a session is open, read the daemon's
 *  snapshot every 500 ms so the card's top strip carries the current
 *  workspace's own tabs — the data the snapshot already includes
 *  (`tab.list {workspace_id}` rows on the wire, measured from
 *  `herdr api schema --json`) — plus the pane→tab map a tap needs. Stops
 *  with the session, like the surface poller.
 */
private const val TABS_POLL_MS = 500L

private fun pollWorkspaceTabs(
    screen: ScreenState,
    sessionUi: SessionUi,
    connection: CompanionConnection,
    workspaceId: String,
    paneId: String,
    generation: Int,
) {
    val slot = screen.pollers.register()
    Log.i(TAG, "tabs poller registered, active=${screen.pollers.count}")
    fun runOnce() {
        executor.execute {
            var reschedule = false
            try {
                val snapshot = try {
                    connection.snapshot()
                } catch (_: Exception) {
                    null
                }
                mainHandler.post {
                    if (screen.generation != generation) return@post
                    if (snapshot != null) {
                        sessionUi.tabs = WorkspaceTabsModel.tabsOfWorkspace(snapshot.tabs, workspaceId)
                        sessionUi.panes = snapshot.panes
                        sessionUi.tabAgents = WorkspaceTabsModel.tabAgentsByTab(snapshot.panes, snapshot.agents)
                        sessionUi.agents = snapshot.agents
                    }
                }
                reschedule = !ended(connection)
            } finally {
                if (!reschedule) {
                    slot.close()
                    Log.i(TAG, "tabs poller stopped, active=${screen.pollers.count}")
                }
            }
            if (reschedule) {
                mainHandler.postDelayed({
                    if (sessionStillCurrent(screen, sessionUi, paneId, generation)) {
                        runOnce()
                    } else {
                        slot.close()
                        Log.i(TAG, "tabs poller stopped, active=${screen.pollers.count}")
                    }
                }, TABS_POLL_MS)
            }
        }
    }
    runOnce()
}

/** True while the connection is still alive — the shared end check the
 *  pollers use, so a dead link stops every loop the same way. */
private fun ended(connection: CompanionConnection): Boolean = try {
    connection.isDisconnected()
} catch (_: Exception) {
    true
}

/**
 * The inbox (03-inbox.html) as the connected host's agent list: one
 * surface, groups in the fixed order needs you → done → working → idle,
 * the group label carrying the word and the count. Extracted verbatim
 * from ConnectScreen (S28-4: the agent list is always the chosen
 * workspace's own) — the captures become explicit parameters, nothing
 * else changes.
 */
@Composable
private fun InboxSection(
    screen: ScreenState,
    current: UiState.Connected,
    onBackToWorkspaces: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenAgent: (AgentDto) -> Unit,
) {
                    // --- 03-inbox.html: one surface, groups in the fixed ---
                // --- order needs you → done → working → idle; the    ---
                // --- group label carries the word and the count, and ---
                // --- no row repeats it with a status chip.           ---
                // S24-7: the chevron alone explains the way back — no
                // repeated `Hosts` word. The row keeps a screen-reader
                // label because the chevron glyph reads as nothing.
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    val backToHosts = stringResource(R.string.cd_back_to_hosts)
                    Text(
                        text = "‹",
                        color = LocalPalette.current.accent,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clickable {
                                // The agent list is the workspace's own
                                // (S28-4/S28-5): back is the workspaces
                                // screen, where it was entered from.
                                onBackToWorkspaces()
                            }
                            .semantics { contentDescription = backToHosts }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.nodes_disconnect),
                        color = LocalPalette.current.muted,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { onDisconnect() }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                }
                val snapshot = current.snapshot
                when {
                    snapshot == null -> Text(
                        text = stringResource(R.string.nodes_waiting),
                        color = LocalPalette.current.muted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    else -> {
                        // S28-4: the agent list is always the chosen
                        // workspace's own — there is no flat all-agents
                        // list to duplicate it, and no line repeating
                        // the workspace's name (S28-5).
                        val agents = WorkspacesModel.agentsInWorkspace(
                            snapshot.agents,
                            screen.workspaceFilter,
                        )
                        val nowMs = System.currentTimeMillis()
                        val waitingMs = screen.approvalWait.snapshot()
                            .mapValues { (nowMs - it.value).coerceAtLeast(0L) }
                        val groups = ApprovalBoardModel.groups(agents, waitingMs)
                        val waiting = groups
                            .firstOrNull { it.group == ApprovalBoardModel.Group.NEEDS_YOU }
                            ?.agents ?: emptyList()
                        when {
                            agents.isEmpty() -> Text(
                                text = stringResource(R.string.inbox_no_agents),
                                color = LocalPalette.current.muted,
                                fontSize = 13.sp,
                            )
                            else -> {
                        // T31 (S32b): the approval board. The groups come out
                        // of the pure model in the fixed order needs you →
                        // working → done → idle, the honest `unknown` group
                        // last. Waiting agents lead, longest waiting first.
                        // The one needs-you count lives in the pill under the
                        // header and in the group labels — nowhere else; the
                        // pill is the walk's entry, stepping through every
                        // waiting agent one after another.
                        if (waiting.isNotEmpty()) {
                            ApprovalCounterRow(
                                count = waiting.size,
                                onStartWalk = {
                                    screen.approvalWalk = true
                                    screen.approvalWalkIndex = 0
                                },
                            )
                            Spacer(Modifier.height(Air.BETWEEN_CARDS_DP.dp))
                        }
                        if (screen.approvalWalk && waiting.isNotEmpty()) {
                            // The walk ends itself when its queue runs out
                            // (answered or stepped past the last one).
                            LaunchedEffect(waiting.size, screen.approvalWalkIndex) {
                                if (screen.approvalWalkIndex >= waiting.size) {
                                    screen.approvalWalk = false
                                    screen.approvalWalkIndex = 0
                                }
                            }
                            waiting.getOrNull(screen.approvalWalkIndex)?.let { target ->
                                ApprovalBlock(
                                    agent = target,
                                    read = screen.approvalParses[target.paneId],
                                    busy = screen.approvalBusy,
                                    held = screen.approvalHold,
                                    error = screen.approvalError
                                        ?.takeIf { it.first == target.paneId }?.second,
                                    onAnswer = { option, shown ->
                                        answerApproval(
                                            screen,
                                            current.connection,
                                            target.paneId,
                                            option,
                                            shown,
                                            screen.generation,
                                        )
                                    },
                                    footer = {
                                        ApprovalNextRow(
                                            position = screen.approvalWalkIndex + 1,
                                            total = waiting.size,
                                            onNext = {
                                                if (screen.approvalWalkIndex + 1 >= waiting.size) {
                                                    screen.approvalWalk = false
                                                    screen.approvalWalkIndex = 0
                                                } else {
                                                    screen.approvalWalkIndex += 1
                                                }
                                            },
                                        )
                                    },
                                )
                                Spacer(Modifier.height(Air.BETWEEN_CARDS_DP.dp))
                            }
                        }
                        groups.forEach { ordered ->
                            val (labelRes, needsYou) = when (ordered.group) {
                                ApprovalBoardModel.Group.NEEDS_YOU ->
                                    R.string.inbox_group_needs_you to true
                                ApprovalBoardModel.Group.WORKING ->
                                    R.string.inbox_group_working to false
                                ApprovalBoardModel.Group.DONE ->
                                    R.string.inbox_group_done to false
                                // S27-8: the word `Idle` never renders — idle
                                // agents list quietly, with no group label.
                                ApprovalBoardModel.Group.IDLE -> null to false
                                ApprovalBoardModel.Group.UNKNOWN ->
                                    R.string.inbox_group_unknown to false
                            }
                            if (labelRes != null) {
                                GroupLabel(
                                    groupLabelWithCount(stringResource(labelRes), ordered.agents.size),
                                    uppercase = false,
                                )
                            }
                            ordered.agents.forEach { agent ->
                                val read = screen.approvalParses[agent.paneId]
                                if (needsYou && read != null) {
                                    ApprovalBlock(
                                        agent = agent,
                                        read = read,
                                        busy = screen.approvalBusy,
                                        held = screen.approvalHold,
                                        error = screen.approvalError
                                            ?.takeIf { it.first == agent.paneId }?.second,
                                        onAnswer = { option, shown ->
                                            answerApproval(
                                                screen,
                                                current.connection,
                                                agent.paneId,
                                                option,
                                                shown,
                                                screen.generation,
                                            )
                                        },
                                        footer = null,
                                    )
                                } else {
                                    InboxRow(
                                        agent,
                                        activity = screen.activity,
                                        needsYou = needsYou,
                                        onOpen = { onOpenAgent(agent) },
                                    )
                                }
                            }
                        }
                            }
                        }
                    }
                }
}


/**
 * T13 pick → plan → add → upload. Runs on the main thread until the
 * transfer, which goes to the executor. Every refusal is honest and
 * pick-time: an oversize or unreadable image never becomes a half-added
 * attachment row.
 */
private fun startAttachmentUpload(
    context: android.content.Context,
    connection: CompanionConnection,
    sessionUi: SessionUi,
    uri: Uri,
) {
    try {
        val resolver = context.contentResolver
        var displayName: String? = null
        var size = -1L
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    displayName = cursor.getString(0)
                    if (!cursor.isNull(1)) size = cursor.getLong(1)
                }
            }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) {
            sessionUi.attachmentError = context.getString(R.string.att_unreadable)
            return
        }
        if (size < 0) size = bytes.size.toLong()
        val name = displayName?.takeIf { it.isNotBlank() } ?: "image.png"
        val plan = try {
            corePlanUpload(name, size.toULong())
        } catch (error: CompanionException) {
            sessionUi.attachmentError = describe(context, error)
            return
        }
        val thumbnail = decodeThumbnail(context, uri)
        val added = sessionUi.attachmentQueue.add(name, plan.storedName, size) { bytes, max ->
            context.getString(R.string.att_too_large, formatBytes(bytes), formatBytes(max))
        }
        val id = when (added) {
            is AddResult.Refused -> {
                sessionUi.attachmentError = added.reason
                return
            }
            is AddResult.Accepted -> added.id
        }
        if (thumbnail != null) sessionUi.thumbnails[id] = thumbnail
        sessionUi.attachmentUris[id] = uri
        sessionUi.syncAttachments()
        uploadAttachment(context, connection, sessionUi, id, uri)
    } catch (error: Exception) {
        sessionUi.attachmentError = describe(context, error)
    }
}

/**
 * The transfer itself: the bytes are re-read from the stored URI (so a
 * retry is always possible), handed to the core's SFTP lane, and every
 * outcome lands back on the queue on the main thread.
 */
private fun uploadAttachment(
    context: android.content.Context,
    connection: CompanionConnection,
    sessionUi: SessionUi,
    id: Long,
    uri: Uri,
) {
    val storedName = sessionUi.attachmentQueue.snapshot()
        .firstOrNull { it.id == id }?.storedName ?: return
    executor.execute {
        val outcome = try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw java.io.IOException(context.getString(R.string.att_unreadable_anymore))
            connection.uploadFile(
                storedName,
                bytes,
                object : UploadProgressListener {
                    override fun onProgress(sentBytes: ULong, totalBytes: ULong) {
                        mainHandler.post {
                            sessionUi.attachmentQueue.onProgress(id, sentBytes.toLong())
                            sessionUi.syncAttachments()
                        }
                    }
                },
            )
        } catch (error: Exception) {
            mainHandler.post {
                sessionUi.attachmentQueue.onFailed(id, describe(context, error))
                sessionUi.syncAttachments()
            }
            return@execute
        }
        mainHandler.post {
            sessionUi.attachmentQueue.onUploaded(id, outcome.remotePath, outcome.promptSuffix)
            sessionUi.syncAttachments()
        }
    }
}

/** A downsampled preview bitmap for the attachment row; `null` means the
 * plate stands in, never a broken image. */
private fun decodeThumbnail(context: android.content.Context, uri: Uri): ImageBitmap? = try {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (
        (bounds.outWidth / sample > 256 || bounds.outHeight / sample > 256) &&
        sample < 64
    ) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        ?.asImageBitmap()
} catch (_: Exception) {
    null
}

@Composable
private fun Hairline() {
    // 01-pair.html: blocks separated by a 1px line on the same surface —
    // never a nested card.
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalPalette.current.line),
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun Notice(message: String, color: androidx.compose.ui.graphics.Color) {
    // 01-pair.html `.notice`: a left rule and quiet text, never a filled
    // banner. The rejection text is readable, not a spinner that hides it.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .height(IntrinsicSize.Min),
    ) {
        Spacer(
            Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(color),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = message,
            color = color,
            fontSize = 13.sp,
        )
    }
}

// --- T31 (S32b): the approval board's surfaces. Content, never a dialog:
// the question, the pane's exact command in mono, full-width option rows
// at the 44 dp floor in the pane's own numbering, the risky ones held and
// marked with the warning glyph, the safe option with no mark. All words
// and tokens come from the pure rules in ui/approval and the strings
// resources; nothing here invents a command or renumbers an option.

/** The one needs-you count: the pill under the board's header, the walk's
 *  entry (board 09). A 44 dp tap target, dot and chevron from the board. */
@Composable
private fun ApprovalCounterRow(count: Int, onStartWalk: () -> Unit) {
    val pillShape = RoundedCornerShape(999.dp)
    val cd = stringResource(R.string.cd_approval_pill)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Air.TOUCH_DP.dp)
            .clickable(onClick = onStartWalk)
            .background(LocalPalette.current.surface, pillShape)
            .border(1.dp, LocalPalette.current.line, pillShape)
            .padding(horizontal = Air.CARD_PADDING_DP.dp)
            .semantics { contentDescription = cd },
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier
                .size(9.dp)
                .background(LocalPalette.current.error, androidx.compose.foundation.shape.CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (count == 1) {
                stringResource(R.string.approval_pill_one)
            } else {
                stringResource(R.string.approval_pill_many, count)
            },
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(text = "›", color = LocalPalette.current.muted, fontSize = 15.sp)
    }
}

/** One waiting agent's approval card: the agent's project name over the
 *  parsed body. When the pane has not been read yet it says it is reading
 *  — never an empty card claiming to be one. The optional footer carries
 *  the walk's progress row. */
@Composable
private fun ApprovalBlock(
    agent: AgentDto,
    read: ApprovalRead?,
    busy: Boolean,
    held: ApprovalHold?,
    error: String?,
    onAnswer: (ApprovalParser.Option, ApprovalParser.Parse.Question) -> Unit,
    footer: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalPalette.current.surface, RoundedCornerShape(Air.RADIUS_CARD_DP.dp))
            .border(1.dp, LocalPalette.current.line, RoundedCornerShape(Air.RADIUS_CARD_DP.dp))
            .padding(Air.CARD_PADDING_DP.dp),
    ) {
        Text(
            text = AgentRowModel.projectTitle(agent.cwd, agent.name),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = Air.ROW_NAME_SP.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        if (read == null) {
            Text(
                text = stringResource(R.string.session_reading_pane),
                color = LocalPalette.current.muted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
            )
        } else {
            ApprovalBody(
                paneId = agent.paneId,
                parse = read.parse,
                busy = busy,
                held = held,
                error = error,
                onAnswer = onAnswer,
            )
        }
        footer?.invoke()
    }
}

/** The parsed body of one waiting agent: question, exact command, the
 *  pane's own options — or the honest view when the pane does not read as
 *  an approval. */
@Composable
private fun ApprovalBody(
    paneId: String,
    parse: ApprovalParser.Parse,
    busy: Boolean,
    held: ApprovalHold?,
    error: String?,
    onAnswer: (ApprovalParser.Option, ApprovalParser.Parse.Question) -> Unit,
) {
    when (parse) {
        is ApprovalParser.Parse.Question -> {
            Text(
                text = parse.question,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = Air.BODY_SP.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = Air.BODY_SP.sp * Air.BODY_LINE_HEIGHT,
            )
            parse.command?.let { command ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = LocalPalette.current.accent)) {
                            append(stringResource(R.string.approval_command) + " ")
                        }
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                            append(command)
                        }
                    },
                    fontFamily = FontFamily.Monospace,
                    fontSize = Air.COMMAND_SP.sp,
                    lineHeight = Air.COMMAND_SP.sp * Air.BODY_LINE_HEIGHT,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            LocalPalette.current.surfaceRaised,
                            RoundedCornerShape(Air.RADIUS_FIELD_DP.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            parse.options.forEachIndexed { index, option ->
                if (index > 0) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(Air.HAIRLINE_DP.dp)
                            .background(LocalPalette.current.line),
                    )
                }
                ApprovalOptionRow(
                    option = option,
                    risky = ApprovalRisk.isRisky(option.label, parse.command, parse.question),
                    heldNow = held == ApprovalHold(paneId, option.number),
                    busy = busy,
                    onAnswer = { onAnswer(option, parse) },
                )
            }
        }
        is ApprovalParser.Parse.Honest -> {
            // S32b-5: no fake approval block — which case it is, the pane's
            // own lines, and the keys to press. A wrong approval is worse
            // than a manual one.
            Text(
                text = when (parse.reason) {
                    ApprovalParser.Reason.EMPTY -> stringResource(R.string.approval_case_empty)
                    ApprovalParser.Reason.NO_OPTIONS -> stringResource(R.string.approval_case_no_options)
                    ApprovalParser.Reason.NO_QUESTION -> stringResource(R.string.approval_case_no_question)
                },
                color = LocalPalette.current.warning,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = parse.lines.takeLast(24).joinToString("\n"),
                color = LocalPalette.current.text,
                fontSize = Air.META_SP.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        LocalPalette.current.surfaceRaised,
                        RoundedCornerShape(Air.RADIUS_FIELD_DP.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.approval_answer_manually),
                color = LocalPalette.current.muted,
                fontSize = 12.sp,
            )
        }
    }
    error?.let { message ->
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = LocalPalette.current.error,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/** One answer: full-width, 44 dp floor, the pane's own number. Risky
 *  answers carry the warning glyph and are held on the first tap; the
 *  safe option gets no mark. */
@Composable
private fun ApprovalOptionRow(
    option: ApprovalParser.Option,
    risky: Boolean,
    heldNow: Boolean,
    busy: Boolean,
    onAnswer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Air.TOUCH_DP.dp)
            .clickable(enabled = !busy, onClick = onAnswer)
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = "${option.number}.",
            color = LocalPalette.current.muted,
            fontSize = Air.COMMAND_SP.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(32.dp),
        )
        Text(
            text = option.label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = Air.ROW_NAME_SP.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )
        if (heldNow) {
            Text(
                text = stringResource(R.string.approval_hold_again),
                color = LocalPalette.current.accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        val cdRisky = stringResource(R.string.cd_approval_risky)
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(Air.TOUCH_DP.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            if (risky) {
                Text(
                    text = "⚠",
                    color = LocalPalette.current.warning,
                    fontSize = 16.sp,
                    modifier = Modifier.semantics { contentDescription = cdRisky },
                )
            }
        }
    }
}

/** The walk's own row: where it stands, and the way to the next waiting
 *  agent without answering. */
@Composable
private fun ApprovalNextRow(position: Int, total: Int, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Air.TOUCH_DP.dp)
            .clickable(onClick = onNext)
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.approval_walk_of, position, total),
            color = LocalPalette.current.muted,
            fontSize = Air.META_SP.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.approval_walk_next),
            color = LocalPalette.current.accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** One agent row in the grouped inbox: name, working dir, kind — the
 * status lives in the group label, so the row carries no chip (board 03).
 * Elapsed time is not in the snapshot's AgentDto, so it is omitted rather
 * than invented. */
@Composable
private fun InboxRow(
    agent: AgentDto,
    activity: AgentActivity,
    needsYou: Boolean,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onOpen)
            .then(if (needsYou) Modifier.background(LocalPalette.current.selection) else Modifier),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        // The needs-you left rule is the only colour on the list (board 03).
        Spacer(
            Modifier
                .width(2.dp)
                .heightIn(min = 56.dp)
                .background(if (needsYou) LocalPalette.current.accentEdge else Color.Transparent),
        )
        Spacer(Modifier.width(if (needsYou) 10.dp else 12.dp))
        Column(Modifier.weight(1f)) {
            // S24-8: the project name leads, never the full path.
            Text(
                text = AgentRowModel.projectTitle(agent.cwd, agent.name),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // S24-8: the secondary line is the agent kind (when it is not
            // `pi`) plus the relative last-used time — never the full cwd;
            // the path lives inside the session screen.
            AgentRowModel.secondaryMeta(
                agent.kind,
                activity.lastUsed(agent.paneId),
                System.currentTimeMillis(),
            )?.let { meta ->
                Text(
                    text = meta,
                    color = LocalPalette.current.muted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(text = "›", color = LocalPalette.current.muted, fontSize = 15.sp)
    }
}

/** One host row on the Nodes screen (board 02): state is a plain word in
 * secondary text — no chip, no row fill — and the row opens the agent list.
 * A row that explains itself (S24-3) passes `state = null` and shows none.
 * The live host's word reads in the accent colour (S24-4): the connected
 * machine is marked, never left looking like the rest. */
/** T57 (Editar): the host's name and address — four fields, one save. The
 *  address is the host's identity, so it is editable here and saved by
 *  [applyEditHost]; a field that does not validate stays red and never saves. */
@Composable
private fun EditHostDialog(
    title: String,
    name: String,
    host: String,
    port: String,
    user: String,
    hostError: String?,
    portError: String?,
    userError: String?,
    onNameChange: (String) -> Unit,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onUserChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalPalette.current.surface, RoundedCornerShape(16.dp))
                .padding(20.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalPalette.current.accent,
                unfocusedBorderColor = LocalPalette.current.line,
                errorBorderColor = LocalPalette.current.error,
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                label = { Text(stringResource(R.string.pair_form_name)) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = host,
                onValueChange = onHostChange,
                singleLine = true,
                label = { Text(stringResource(R.string.pair_form_host)) },
                isError = hostError != null,
                supportingText = hostError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = port,
                onValueChange = onPortChange,
                singleLine = true,
                label = { Text(stringResource(R.string.pair_form_port)) },
                isError = portError != null,
                supportingText = portError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = user,
                onValueChange = onUserChange,
                singleLine = true,
                label = { Text(stringResource(R.string.pair_form_user)) },
                isError = userError != null,
                supportingText = userError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.set_cancel),
                    color = LocalPalette.current.muted,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.hosts_rename_save),
                    color = if (hostError == null && portError == null && userError == null) {
                        LocalPalette.current.accent
                    } else {
                        LocalPalette.current.textDisabled
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onSave)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
            }
        }
    }
}

/** T58: the host row's menu — Editar and Eliminar, the destructive one in
 *  the error colour with a trash icon and its own confirmation. Conectar is
 *  gone (tapping the row already connects) and Duplicar/Reordenar are left
 *  out: they cannot be done well yet, and a dead entry is worse than an
 *  absent one. */
@Composable
private fun HostMenuDialog(
    entry: SavedHostEntry,
    onEdit: (SavedHostEntry) -> Unit,
    onDelete: (SavedHostEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.surface, RoundedCornerShape(16.dp))
                .padding(8.dp),
        ) {
            Text(
                text = entry.name,
                color = p.muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
            HostMenuModel.entries().forEach { item ->
                val destructive = HostMenuModel.isDestructive(item)
                val label = when (item) {
                    HostMenuEntry.EDIT -> stringResource(R.string.hosts_menu_edit)
                    HostMenuEntry.DELETE -> stringResource(R.string.hosts_delete)
                }
                val action: () -> Unit = when (item) {
                    HostMenuEntry.EDIT -> { { onEdit(entry) } }
                    HostMenuEntry.DELETE -> { { onDelete(entry) } }
                }
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clickable {
                            onDismiss()
                            action()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    if (destructive) {
                        Icon(
                            painter = painterResource(R.drawable.ic_trash),
                            contentDescription = null,
                            tint = p.error,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        text = label,
                        color = if (destructive) p.error else MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = if (destructive) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

/** T50: the confirmation after a host row's swipe-to-Eliminar. Deleting is
 *  destructive (the pin goes with the host), so it is its own dialog, never
 *  a silent swipe. */
@Composable
private fun DeleteHostDialog(
    entry: SavedHostEntry,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalPalette.current.surface, RoundedCornerShape(16.dp))
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.hosts_delete_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.hosts_delete_body, entry.name),
                color = LocalPalette.current.muted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.set_cancel),
                    color = LocalPalette.current.muted,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.hosts_delete),
                    color = LocalPalette.current.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NodeRow(
    name: String,
    state: String?,
    meta: String,
    onOpen: () -> Unit,
    stateAccent: Boolean = false,
    moreLabel: String? = null,
    onMore: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .combinedClickable(onClick = onOpen, onLongClick = { onMore?.invoke() }),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!state.isNullOrBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = state,
                        color = if (stateAccent) {
                            LocalPalette.current.accent
                        } else {
                            LocalPalette.current.muted
                        },
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
            Text(
                text = meta,
                color = LocalPalette.current.muted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // T57: the ⋯ affordance opens the host's menu; a long press does too.
        if (onMore != null && !moreLabel.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(Air.TOUCH_DP.dp)
                    .clickable(onClick = onMore)
                    .semantics { contentDescription = moreLabel },
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text("\u22EF", color = LocalPalette.current.muted, fontSize = 18.sp)
            }
        } else {
            Text(text = "›", color = LocalPalette.current.muted, fontSize = 15.sp)
        }
    }
}

/** The hairline between host rows (board 02): the hosts are rows separated
 *  by a fine line on one surface, never nested cards. */
@Composable
private fun RowHairline() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(Air.HAIRLINE_DP.dp)
            .background(LocalPalette.current.line),
    )
}

/** The hosts list's rows (T57): one row per host, a hairline between rows,
 *  the state as one word, and a ⋯ affordance — or a long press on the row —
 *  that opens the host's menu. Never a swipe. */
@Composable
private fun HostRows(
    rows: List<HostRowModel>,
    onOpen: (HostRowModel) -> Unit,
    onMore: (SavedHostEntry) -> Unit,
) {
    rows.forEachIndexed { index, row ->
        key(row.entry.host, row.entry.port) {
            NodeRow(
                name = row.entry.name,
                state = when (row.state) {
                    HostRowState.Connected -> stringResource(R.string.nodes_state_row_connected)
                    HostRowState.Interrupted -> stringResource(R.string.nodes_state_row_interrupted)
                    HostRowState.Saved -> stringResource(R.string.nodes_state_row_saved)
                },
                meta = HostsModel.address(row.entry),
                stateAccent = row.state != HostRowState.Saved,
                onOpen = { onOpen(row) },
                moreLabel = stringResource(R.string.hosts_menu_more),
                onMore = { onMore(row.entry) },
            )
        }
        if (index < rows.size - 1) {
            RowHairline()
        }
    }
}

/** One saved host's own screen (S34, T35): everything that host owns lives
 *  here instead of the hosts list — its name and address, the connection
 *  state in one word, Workspaces and Session history as rows, and the
 *  disconnect action while a link may be up. Where the list shows every
 *  machine, this screen is one machine's place. */
@Composable
private fun HostScreen(
    entry: SavedHostEntry,
    state: HostScreenModel,
    onBack: () -> Unit,
    onOpenWorkspaces: () -> Unit,
    onOpenSessions: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(
        modifier = Modifier
            // S34: the screen is the route's root — the early return above
            // never reaches the connect column, so the background must be
            // this screen's own, and it must fill the phone (the L33
            // class of bug: dark-on-dark below the content).
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        val backToHosts = stringResource(R.string.cd_back_to_hosts)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = "‹",
                color = LocalPalette.current.accent,
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .semantics { contentDescription = backToHosts }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = entry.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = Air.TITLE_SP.sp,
                lineHeight = (Air.TITLE_SP * Air.TITLE_LINE_HEIGHT).sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = HostsModel.address(entry),
            color = LocalPalette.current.muted,
            fontSize = Air.META_SP.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 44.dp),
        )
        Spacer(Modifier.height(Air.BETWEEN_CARDS_DP.dp))
        Text(
            text = when (state.state) {
                HostScreenState.Unconnected -> stringResource(R.string.nodes_state_not_connected)
                HostScreenState.Connecting -> stringResource(R.string.nodes_state_connecting)
                HostScreenState.Connected -> stringResource(R.string.nodes_state_connected)
                HostScreenState.Interrupted -> stringResource(R.string.nodes_state_interrupted)
                HostScreenState.Failed -> stringResource(R.string.nodes_state_failed)
            },
            color = if (state.state == HostScreenState.Connected) {
                LocalPalette.current.accent
            } else if (state.state == HostScreenState.Interrupted) {
                LocalPalette.current.muted
            } else {
                LocalPalette.current.error
            },
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
        )
        if (state.disconnectVisible) {
            Spacer(Modifier.height(4.dp))
            val disconnectLabel = stringResource(R.string.nodes_disconnect)
            Box(
                modifier = Modifier
                    .heightIn(min = Air.TOUCH_DP.dp)
                    .clickable(onClick = onDisconnect)
                    .semantics { contentDescription = disconnectLabel },
                contentAlignment = androidx.compose.ui.Alignment.CenterStart,
            ) {
                Text(
                    text = disconnectLabel,
                    color = LocalPalette.current.muted,
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(Modifier.height(Air.BETWEEN_CARDS_DP.dp))
        NodeRow(
            name = stringResource(R.string.nodes_workspaces),
            state = null,
            meta = stringResource(R.string.nodes_workspaces_sub),
            onOpen = onOpenWorkspaces,
        )
        RowHairline()
        NodeRow(
            name = stringResource(R.string.nodes_session_history),
            state = null,
            meta = stringResource(R.string.nodes_session_history_sub),
            onOpen = onOpenSessions,
        )
    }
}

/** Debug-only (S34 capture): the live list's rows and one host's own screen
 *  on fixture hosts, so what sits behind a live connection can be looked at
 *  without touching any daemon. Never in release. */
@Composable
private fun HostsPreview() {
    val fixtureHosts = listOf(
        SavedHostEntry("Desktop", "100.96.18.28", "22", "erick"),
        SavedHostEntry("Notebook", "100.96.18.29", "22", "erick"),
        SavedHostEntry("pi-lab", "192.168.18.10", "2222", "erick"),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.nodes_your_hosts),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        HostRows(
            rows = HostsModel.listRows(
                fixtureHosts,
                live = fixtureHosts.first(),
                liveState = HostScreenState.Connected,
            ),
            onOpen = {},
            onMore = {},
        )
        Spacer(Modifier.height(24.dp))
        HostScreen(
            entry = fixtureHosts.first(),
            state = HostScreenModel(HostScreenState.Connected),
            onBack = {},
            onOpenWorkspaces = {},
            onOpenSessions = {},
            onDisconnect = {},
        )
    }
}

/** The group header: the word and the count, quiet (board 03). S24-6: the
 *  inbox's human words arrive already sentence-cased (`uppercase = false`);
 *  meta labels keep the design's uppercase step. */
@Composable
private fun GroupLabel(text: String, uppercase: Boolean = true) {
    Text(
        text = if (uppercase) text.uppercase() else text,
        color = LocalPalette.current.muted,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

/**
 * The interrupted-connection notice (board 03): a 2px error left rule with
 * quiet text — never a card and never a modal — and Retry now always visible
 * as accent text.
 */
@Composable
private fun InterruptedNotice(reason: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Spacer(
            Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(LocalPalette.current.error),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.inbox_interrupted_title),
                color = LocalPalette.current.error,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                // The honest reason, then the rule: what is on screen is the
                // last data received, never live data (board 03, T8c).
                text = stringResource(R.string.inbox_interrupted_suffix, reason),
                color = LocalPalette.current.muted,
                fontSize = 13.sp,
            )
        }
        Text(
            text = stringResource(R.string.inbox_retry_now),
            color = LocalPalette.current.accentEdge,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .heightIn(min = 44.dp)
                .clickable(onClick = onRetry)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        )
    }
}


/** S24-1: the localized inbox group label — the human word, the count only
 *  when it is not one (the rule AgentRowModel.groupLabel encodes). */
@Composable
private fun groupLabelWithCount(word: String, count: Int): String =
    if (count == 1) word
    else stringResource(R.string.inbox_group_count, word, count)

private fun String.toUShortOrNull(): UShort? =
    trim().toIntOrNull()?.let { if (it in 1..65535) it.toUShort() else null }

@Preview(showBackground = true)
@Composable
private fun ConnectScreenPreview() {
    GentleMobileTheme {
        ConnectScreen(
            keyPath = "/data/local/tmp/client_ed25519",
            appTheme = AppTheme.CUTE,
            followThePhone = false,
            language = AppLanguage.ENGLISH,
            advanced = AdvancedSettings(),
            shortcutPad = ShortcutPad.DEFAULT,
            voz = VozSettings(),
            onSetLanguage = {},
            openAgentPane = androidx.compose.runtime.mutableStateOf<String?>(null),
            onSelectTheme = {},
            onSetFollowThePhone = {},
            onSetAdvanced = {},
            onSetShortcutPad = {},
            onSetVoz = {},
        )
    }
}
