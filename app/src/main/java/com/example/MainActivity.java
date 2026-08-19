package com.example;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Activity implementing a driverless Bluetooth HID Touchpad, Mouse, and Keyboard host.
 *
 * Utilizes the native Android BluetoothHidDevice API (API 28+) to broadcast
 * standard USB HID Mouse, Keyboard, and Consumer Control reports directly to laptops,
 * PCs, and tablets without any server-side software or driver installation.
 */
public class MainActivity extends AppCompatActivity implements TouchpadGestureListener.TouchpadCallback {

    private static final String TAG = "BTTouchpad";
    private static final String PREFS_NAME = "touchpad_settings";
    private static final String PREF_SENSITIVITY = "sensitivity";
    private static final String PREF_SCROLL_SPEED = "scroll_speed";
    private static final String PREF_HAPTIC = "haptic_feedback";

    // UI Components - Status & Actions
    private View viewStatusDot;
    private TextView tvConnectionStatus;
    private Button btnSearchPair;
    private Button btnMakeDiscoverable;
    private ImageButton btnToggleKeyboard;
    private ImageButton btnSettings;

    // UI Components - Keyboard Panel & Controls
    private MaterialCardView cardKeyboardPanel;
    private EditText etKeyboardInput;
    private ImageButton btnSendText;
    private ImageButton btnKeyBackspace;
    private Button btnKeyEnter;
    private Button btnKeyEsc;
    private Button btnKeyTab;
    private Button btnKeySpace;
    private Button btnKeyWin;
    private Button btnKeyCtrlC;
    private Button btnKeyCtrlV;
    private Button btnKeyCtrlZ;
    private Button btnKeyCtrlA;
    private Button btnKeyAltTab;
    private Button btnKeyArrowUp;
    private Button btnKeyArrowDown;
    private Button btnKeyArrowLeft;
    private Button btnKeyArrowRight;
    private ImageButton btnMediaPlayPause;
    private ImageButton btnMediaVolDown;
    private ImageButton btnMediaVolUp;
    private ImageButton btnMediaMute;

    // UI Components - Touchpad & Mouse Buttons
    private View viewTouchpad;
    private LinearLayout viewScrollStrip;
    private Button btnLeftClick;
    private Button btnMiddleClick;
    private Button btnRightClick;

    // Bluetooth HID Components
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHidDevice hidDevice;
    private BluetoothDevice connectedDevice;
    private boolean isHidAppRegistered = false;
    private boolean isConnected = false;

    // Gesture & Input Handlers
    private GestureDetector gestureDetector;
    private TouchpadGestureListener gestureListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService keySenderExecutor = Executors.newSingleThreadExecutor();
    private Vibrator vibrator;

    // Mouse State & Settings
    private byte currentButtonsState = HidDescriptor.MOUSE_BUTTON_NONE;
    private float cursorSensitivity = 1.2f;
    private float scrollSpeed = 1.0f;
    private boolean hapticEnabled = true;
    private float scrollAccumulator = 0f;

    // Discovery & Pairing
    private AlertDialog deviceListDialog;
    private DeviceListAdapter deviceListAdapter;
    private final List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private final Set<String> discoveredAddresses = new HashSet<>();
    private boolean isScanning = false;

    // Permission Launcher for Android 12+ (API 31+) & Legacy
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    initBluetoothHid();
                } else {
                    Toast.makeText(this, R.string.permission_rationale, Toast.LENGTH_LONG).show();
                    updateStatusUi(R.string.status_not_connected, getColor(R.color.status_disconnected));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPreferences();
        initVibrator();
        initViews();
        setupTouchpadGestures();
        setupHardwareButtons();
        setupScrollStrip();
        setupKeyboardControls();

        checkPermissionsAndInitBluetooth();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keySenderExecutor.shutdownNow();
        unregisterDiscoveryReceiver();
        cleanupBluetoothHid();
    }

    // =========================================================================
    // Preference & Vibrator Initialization
    // =========================================================================

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        cursorSensitivity = prefs.getFloat(PREF_SENSITIVITY, 1.2f);
        scrollSpeed = prefs.getFloat(PREF_SCROLL_SPEED, 1.0f);
        hapticEnabled = prefs.getBoolean(PREF_HAPTIC, true);
    }

    private void savePreferences() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(PREF_SENSITIVITY, cursorSensitivity);
        editor.putFloat(PREF_SCROLL_SPEED, scrollSpeed);
        editor.putBoolean(PREF_HAPTIC, hapticEnabled);
        editor.apply();
    }

    private void initVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            }
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    private void performHaptic(int durationMs) {
        if (!hapticEnabled || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(durationMs);
        }
    }

    // =========================================================================
    // UI Setup & Listeners
    // =========================================================================

    private void initViews() {
        viewStatusDot = findViewById(R.id.view_status_dot);
        tvConnectionStatus = findViewById(R.id.tv_connection_status);
        btnSearchPair = findViewById(R.id.btn_search_pair);
        btnMakeDiscoverable = findViewById(R.id.btn_make_discoverable);
        btnToggleKeyboard = findViewById(R.id.btn_toggle_keyboard);
        btnSettings = findViewById(R.id.btn_settings);

        cardKeyboardPanel = findViewById(R.id.card_keyboard_panel);
        etKeyboardInput = findViewById(R.id.et_keyboard_input);
        btnSendText = findViewById(R.id.btn_send_text);
        btnKeyBackspace = findViewById(R.id.btn_key_backspace);
        btnKeyEnter = findViewById(R.id.btn_key_enter);
        btnKeyEsc = findViewById(R.id.btn_key_esc);
        btnKeyTab = findViewById(R.id.btn_key_tab);
        btnKeySpace = findViewById(R.id.btn_key_space);
        btnKeyWin = findViewById(R.id.btn_key_win);
        btnKeyCtrlC = findViewById(R.id.btn_key_ctrl_c);
        btnKeyCtrlV = findViewById(R.id.btn_key_ctrl_v);
        btnKeyCtrlZ = findViewById(R.id.btn_key_ctrl_z);
        btnKeyCtrlA = findViewById(R.id.btn_key_ctrl_a);
        btnKeyAltTab = findViewById(R.id.btn_key_alt_tab);
        btnKeyArrowUp = findViewById(R.id.btn_key_arrow_up);
        btnKeyArrowDown = findViewById(R.id.btn_key_arrow_down);
        btnKeyArrowLeft = findViewById(R.id.btn_key_arrow_left);
        btnKeyArrowRight = findViewById(R.id.btn_key_arrow_right);
        btnMediaPlayPause = findViewById(R.id.btn_media_play_pause);
        btnMediaVolDown = findViewById(R.id.btn_media_vol_down);
        btnMediaVolUp = findViewById(R.id.btn_media_vol_up);
        btnMediaMute = findViewById(R.id.btn_media_mute);

        viewTouchpad = findViewById(R.id.view_touchpad);
        viewScrollStrip = findViewById(R.id.view_scroll_strip);
        btnLeftClick = findViewById(R.id.btn_left_click);
        btnMiddleClick = findViewById(R.id.btn_middle_click);
        btnRightClick = findViewById(R.id.btn_right_click);

        btnSearchPair.setOnClickListener(v -> showDeviceSelectionDialog());
        btnMakeDiscoverable.setOnClickListener(v -> makePhoneDiscoverable());
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        btnToggleKeyboard.setOnClickListener(v -> toggleKeyboardPanel());

        updateStatusUi(R.string.status_not_connected, getColor(R.color.status_disconnected));
    }

    private void toggleKeyboardPanel() {
        if (cardKeyboardPanel.getVisibility() == View.VISIBLE) {
            cardKeyboardPanel.setVisibility(View.GONE);
            hideSoftKeyboard(etKeyboardInput);
        } else {
            cardKeyboardPanel.setVisibility(View.VISIBLE);
            etKeyboardInput.requestFocus();
            showSoftKeyboard(etKeyboardInput);
        }
    }

    private void showSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateStatusUi(int stringResId, int color) {
        mainHandler.post(() -> {
            tvConnectionStatus.setText(stringResId);
            tvConnectionStatus.setTextColor(color);
            viewStatusDot.getBackground().setTint(color);
        });
    }

    private void updateStatusUi(String text, int color) {
        mainHandler.post(() -> {
            tvConnectionStatus.setText(text);
            tvConnectionStatus.setTextColor(color);
            viewStatusDot.getBackground().setTint(color);
        });
    }

    // =========================================================================
    // Touchpad Gesture Setup
    // =========================================================================

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchpadGestures() {
        gestureListener = new TouchpadGestureListener(this);
        gestureListener.setSensitivity(cursorSensitivity);
        gestureDetector = new GestureDetector(this, gestureListener);

        viewTouchpad.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupHardwareButtons() {
        btnLeftClick.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    performHaptic(20);
                    setButtonState(HidDescriptor.MOUSE_BUTTON_LEFT, true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    setButtonState(HidDescriptor.MOUSE_BUTTON_LEFT, false);
                    return true;
            }
            return false;
        });

        btnMiddleClick.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    performHaptic(20);
                    setButtonState(HidDescriptor.MOUSE_BUTTON_MIDDLE, true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    setButtonState(HidDescriptor.MOUSE_BUTTON_MIDDLE, false);
                    return true;
            }
            return false;
        });

        btnRightClick.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    performHaptic(30);
                    setButtonState(HidDescriptor.MOUSE_BUTTON_RIGHT, true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    setButtonState(HidDescriptor.MOUSE_BUTTON_RIGHT, false);
                    return true;
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupScrollStrip() {
        viewScrollStrip.setOnTouchListener(new View.OnTouchListener() {
            private float lastY = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastY = event.getY();
                        scrollAccumulator = 0f;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaY = lastY - event.getY();
                        lastY = event.getY();
                        scrollAccumulator += deltaY * scrollSpeed * 0.25f;
                        int wheelSteps = (int) scrollAccumulator;
                        if (wheelSteps != 0) {
                            sendMouseReport(currentButtonsState, 0, 0, wheelSteps);
                            scrollAccumulator -= wheelSteps;
                            performHaptic(8);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        scrollAccumulator = 0f;
                        return true;
                }
                return false;
            }
        });
    }

    // =========================================================================
    // Keyboard & Shortcut Controls Setup
    // =========================================================================

    private void setupKeyboardControls() {
        // Text Send Action
        btnSendText.setOnClickListener(v -> {
            String text = etKeyboardInput.getText().toString();
            if (!text.isEmpty()) {
                sendText(text);
                etKeyboardInput.setText("");
                performHaptic(20);
            }
        });

        etKeyboardInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String text = etKeyboardInput.getText().toString();
                if (!text.isEmpty()) {
                    sendText(text);
                    etKeyboardInput.setText("");
                } else {
                    sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_ENTER);
                }
                performHaptic(20);
                return true;
            }
            return false;
        });

        // Key Buttons
        btnKeyBackspace.setOnClickListener(v -> {
            performHaptic(15);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_BACKSPACE);
        });

        btnKeyEnter.setOnClickListener(v -> {
            performHaptic(20);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_ENTER);
        });

        btnKeyEsc.setOnClickListener(v -> {
            performHaptic(15);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_ESC);
        });

        btnKeyTab.setOnClickListener(v -> {
            performHaptic(15);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_TAB);
        });

        btnKeySpace.setOnClickListener(v -> {
            performHaptic(15);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_SPACE);
        });

        btnKeyWin.setOnClickListener(v -> {
            performHaptic(25);
            sendKeyStroke(HidDescriptor.MODIFIER_LEFT_GUI, HidDescriptor.KEY_NONE);
        });

        btnKeyCtrlC.setOnClickListener(v -> {
            performHaptic(20);
            sendKeyStroke(HidDescriptor.MODIFIER_LEFT_CTRL, HidDescriptor.KEY_C);
        });

        btnKeyCtrlV.setOnClickListener(v -> {
            performHaptic(20);
            sendKeyStroke(HidDescriptor.MODIFIER_LEFT_CTRL, HidDescriptor.KEY_V);
        });

        btnKeyCtrlZ.setOnClickListener(v -> {
            performHaptic(20);
            sendKeyStroke(HidDescriptor.MODIFIER_LEFT_CTRL, HidDescriptor.KEY_Z);
        });

        btnKeyCtrlA.setOnClickListener(v -> {
            performHaptic(20);
            sendKeyStroke(HidDescriptor.MODIFIER_LEFT_CTRL, HidDescriptor.KEY_A);
        });

        btnKeyAltTab.setOnClickListener(v -> {
            performHaptic(25);
            sendKeyStroke(HidDescriptor.MODIFIER_LEFT_ALT, HidDescriptor.KEY_TAB);
        });

        btnKeyArrowUp.setOnClickListener(v -> {
            performHaptic(15);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_UP_ARROW);
        });

        btnKeyArrowDown.setOnClickListener(v -> {
            performHaptic(15);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_DOWN_ARROW);
        });

        btnKeyArrowLeft.setOnClickListener(v -> {
            performHaptic(15);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_LEFT_ARROW);
        });

        btnKeyArrowRight.setOnClickListener(v -> {
            performHaptic(15);
            sendKeyStroke(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_RIGHT_ARROW);
        });

        // Media Buttons
        btnMediaPlayPause.setOnClickListener(v -> {
            performHaptic(20);
            sendConsumerReport(HidDescriptor.MEDIA_PLAY_PAUSE);
        });

        btnMediaVolDown.setOnClickListener(v -> {
            performHaptic(15);
            sendConsumerReport(HidDescriptor.MEDIA_VOLUME_DOWN);
        });

        btnMediaVolUp.setOnClickListener(v -> {
            performHaptic(15);
            sendConsumerReport(HidDescriptor.MEDIA_VOLUME_UP);
        });

        btnMediaMute.setOnClickListener(v -> {
            performHaptic(25);
            sendConsumerReport(HidDescriptor.MEDIA_MUTE);
        });
    }

    // =========================================================================
    // TouchpadCallback Implementations (from GestureListener)
    // =========================================================================

    @Override
    public void onMouseMove(float dx, float dy) {
        int intDx = Math.round(dx);
        int intDy = Math.round(dy);
        if (intDx != 0 || intDy != 0) {
            sendMouseReport(currentButtonsState, intDx, intDy, 0);
        }
    }

    @Override
    public void onLeftClick() {
        performHaptic(18);
        triggerButtonClick(HidDescriptor.MOUSE_BUTTON_LEFT);
    }

    @Override
    public void onDoubleClick() {
        performHaptic(25);
        triggerButtonClick(HidDescriptor.MOUSE_BUTTON_LEFT);
        mainHandler.postDelayed(() -> triggerButtonClick(HidDescriptor.MOUSE_BUTTON_LEFT), 60);
    }

    @Override
    public void onRightClick() {
        performHaptic(40);
        triggerButtonClick(HidDescriptor.MOUSE_BUTTON_RIGHT);
    }

    // =========================================================================
    // HID Report Dispatching Logic
    // =========================================================================

    /**
     * Sends a momentary button click (Press report followed by Release report).
     */
    private void triggerButtonClick(byte buttonMask) {
        if (!isConnected || hidDevice == null || connectedDevice == null) {
            return;
        }
        byte pressedMask = (byte) (currentButtonsState | buttonMask);
        sendMouseReport(pressedMask, 0, 0, 0);

        mainHandler.postDelayed(() -> {
            sendMouseReport(currentButtonsState, 0, 0, 0);
        }, 30);
    }

    /**
     * Updates persistent button mask state (for hold & drag).
     */
    private void setButtonState(byte buttonMask, boolean pressed) {
        if (pressed) {
            currentButtonsState |= buttonMask;
        } else {
            currentButtonsState &= ~buttonMask;
        }
        sendMouseReport(currentButtonsState, 0, 0, 0);
    }

    /**
     * Assembles and broadcasts standard 4-byte HID Mouse report (Report ID 1).
     */
    @SuppressLint("MissingPermission")
    private void sendMouseReport(byte buttons, int dx, int dy, int wheel) {
        if (hidDevice == null || connectedDevice == null || !isConnected) {
            return;
        }
        try {
            byte[] report = HidDescriptor.buildMouseReport(buttons, dx, dy, wheel);
            hidDevice.sendReport(connectedDevice, HidDescriptor.REPORT_ID_MOUSE, report);
        } catch (Exception e) {
            Log.e(TAG, "Error sending Mouse HID report", e);
        }
    }

    /**
     * Sends a single keystroke (Key Press report followed by Key Release report).
     */
    public void sendKeyStroke(byte modifier, byte keycode) {
        if (!isConnected || hidDevice == null || connectedDevice == null) {
            return;
        }
        keySenderExecutor.execute(() -> {
            try {
                sendKeyboardReportInternal(modifier, keycode);
                Thread.sleep(20);
                sendKeyboardReportInternal(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_NONE);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Translates a string of text into consecutive HID keystrokes.
     */
    public void sendText(String text) {
        if (!isConnected || hidDevice == null || connectedDevice == null || text == null) {
            return;
        }
        keySenderExecutor.execute(() -> {
            try {
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    HidDescriptor.KeyStroke stroke = HidDescriptor.charToKeyStroke(c);
                    if (stroke != null) {
                        sendKeyboardReportInternal(stroke.modifier, stroke.keycode);
                        Thread.sleep(15);
                        sendKeyboardReportInternal(HidDescriptor.MODIFIER_NONE, HidDescriptor.KEY_NONE);
                        Thread.sleep(15);
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Assembles and broadcasts standard 8-byte HID Keyboard report (Report ID 2).
     */
    @SuppressLint("MissingPermission")
    private void sendKeyboardReportInternal(byte modifier, byte keycode) {
        if (hidDevice == null || connectedDevice == null || !isConnected) {
            return;
        }
        try {
            byte[] report = HidDescriptor.buildKeyboardReport(modifier, keycode);
            hidDevice.sendReport(connectedDevice, HidDescriptor.REPORT_ID_KEYBOARD, report);
        } catch (Exception e) {
            Log.e(TAG, "Error sending Keyboard HID report", e);
        }
    }

    /**
     * Assembles and broadcasts 2-byte Consumer Control / Media report (Report ID 3).
     */
    @SuppressLint("MissingPermission")
    public void sendConsumerReport(short usageCode) {
        if (!isConnected || hidDevice == null || connectedDevice == null) {
            return;
        }
        keySenderExecutor.execute(() -> {
            try {
                byte[] report = HidDescriptor.buildConsumerReport(usageCode);
                hidDevice.sendReport(connectedDevice, HidDescriptor.REPORT_ID_CONSUMER, report);
                Thread.sleep(30);
                byte[] releaseReport = HidDescriptor.buildConsumerReport((short) 0x0000);
                hidDevice.sendReport(connectedDevice, HidDescriptor.REPORT_ID_CONSUMER, releaseReport);
            } catch (Exception e) {
                Log.e(TAG, "Error sending Consumer HID report", e);
            }
        });
    }

    // =========================================================================
    // Bluetooth HID Profile & Device Management
    // =========================================================================

    private void checkPermissionsAndInitBluetooth() {
        List<String> requiredPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (requiredPermissions.isEmpty()) {
            initBluetoothHid();
        } else {
            permissionLauncher.launch(requiredPermissions.toArray(new String[0]));
        }
    }

    @SuppressLint("MissingPermission")
    private void initBluetoothHid() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            updateStatusUi(R.string.enable_bluetooth_prompt, getColor(R.color.status_connecting));
            return;
        }

        updateStatusUi(R.string.status_registering, getColor(R.color.status_connecting));

        // Obtain proxy for HID_DEVICE Profile
        bluetoothAdapter.getProfileProxy(this, serviceListener, BluetoothProfile.HID_DEVICE);
    }

    private final BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Bluetooth HID Profile Proxy Connected");
                hidDevice = (BluetoothHidDevice) proxy;
                registerHidApp();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.d(TAG, "Bluetooth HID Profile Proxy Disconnected");
                hidDevice = null;
                isHidAppRegistered = false;
                isConnected = false;
                updateStatusUi(R.string.status_disconnected, getColor(R.color.status_disconnected));
            }
        }
    };

    /**
     * Registers the Android phone as a standard Bluetooth HID Combo Mouse & Keyboard SDP service.
     */
    @SuppressLint("MissingPermission")
    private void registerHidApp() {
        if (hidDevice == null) return;

        // Bluetooth SDP Settings: Name, Provider, Descriptor, Subclass COMBO (0xC0)
        BluetoothHidDeviceAppSdpSettings sdpSettings = new BluetoothHidDeviceAppSdpSettings(
                "Bluetooth Touchpad & Keyboard",
                "Android Bluetooth HID Mouse & Keyboard",
                "Android",
                BluetoothHidDevice.SUBCLASS1_COMBO,
                HidDescriptor.COMPOSITE_REPORT_DESCRIPTOR
        );

        // Quality of Service (QoS) Settings
        BluetoothHidDeviceAppQosSettings qosSettings = new BluetoothHidDeviceAppQosSettings(
                BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
                800,
                9,
                0,
                11250,
                BluetoothHidDeviceAppQosSettings.MAX
        );

        try {
            boolean registered = hidDevice.registerApp(
                    sdpSettings,
                    qosSettings,
                    null,
                    Executors.newSingleThreadExecutor(),
                    hidCallback
            );
            Log.d(TAG, "HID Device registerApp call result: " + registered);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register HID app", e);
            updateStatusUi(R.string.status_hid_error, getColor(R.color.status_disconnected));
        }
    }

    /**
     * HID Device State & Protocol Callbacks.
     */
    private final BluetoothHidDevice.Callback hidCallback = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            Log.d(TAG, "onAppStatusChanged: registered = " + registered);
            isHidAppRegistered = registered;
            if (registered) {
                if (pluggedDevice != null) {
                    connectedDevice = pluggedDevice;
                }
                updateStatusUi(R.string.status_hid_ready, getColor(R.color.status_idle));
            } else {
                updateStatusUi(R.string.status_not_connected, getColor(R.color.status_disconnected));
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            Log.d(TAG, "onConnectionStateChanged: device=" + device.getName() + ", state=" + state);
            switch (state) {
                case BluetoothProfile.STATE_CONNECTED:
                    connectedDevice = device;
                    isConnected = true;
                    String deviceName = (device != null && device.getName() != null) ? device.getName() : "PC/Laptop";
                    updateStatusUi(getString(R.string.status_connected, deviceName), getColor(R.color.status_connected));
                    performHaptic(50);
                    break;

                case BluetoothProfile.STATE_CONNECTING:
                    updateStatusUi(R.string.status_connecting, getColor(R.color.status_connecting));
                    break;

                case BluetoothProfile.STATE_DISCONNECTING:
                    updateStatusUi("Disconnecting…", getColor(R.color.status_connecting));
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    connectedDevice = null;
                    isConnected = false;
                    updateStatusUi(R.string.status_hid_ready, getColor(R.color.status_idle));
                    break;
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            if (hidDevice != null) {
                if (id == HidDescriptor.REPORT_ID_KEYBOARD) {
                    byte[] emptyKeyboard = HidDescriptor.buildKeyboardReleaseReport();
                    hidDevice.replyReport(device, type, id, emptyKeyboard);
                } else if (id == HidDescriptor.REPORT_ID_CONSUMER) {
                    byte[] emptyConsumer = HidDescriptor.buildConsumerReport((short) 0x0000);
                    hidDevice.replyReport(device, type, id, emptyConsumer);
                } else {
                    byte[] emptyMouse = HidDescriptor.buildMouseReport(HidDescriptor.MOUSE_BUTTON_NONE, 0, 0, 0);
                    hidDevice.replyReport(device, type, id, emptyMouse);
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            if (hidDevice != null) {
                hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            if (hidDevice != null) {
                hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void cleanupBluetoothHid() {
        if (hidDevice != null) {
            try {
                if (connectedDevice != null) {
                    hidDevice.disconnect(connectedDevice);
                }
                if (isHidAppRegistered) {
                    hidDevice.unregisterApp();
                }
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up Bluetooth HID", e);
            }
            hidDevice = null;
        }
    }

    // =========================================================================
    // Device Discovery, Pairing & Dialogs
    // =========================================================================

    @SuppressLint("MissingPermission")
    private void makePhoneDiscoverable() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, R.string.enable_bluetooth_prompt, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivity(discoverableIntent);
        Toast.makeText(this, "Phone is now discoverable for 2 minutes. Open PC Bluetooth Settings to pair!", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("MissingPermission")
    private void showDeviceSelectionDialog() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, R.string.enable_bluetooth_prompt, Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_device_list, null);
        ListView lvDevices = dialogView.findViewById(R.id.lv_devices);
        TextView tvEmpty = dialogView.findViewById(R.id.tv_empty_devices);
        LinearLayout layoutScanning = dialogView.findViewById(R.id.layout_scanning_indicator);
        Button btnDialogScan = dialogView.findViewById(R.id.btn_dialog_scan);
        Button btnDialogCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        discoveredDevices.clear();
        discoveredAddresses.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                discoveredDevices.add(device);
                discoveredAddresses.add(device.getAddress());
            }
        }

        deviceListAdapter = new DeviceListAdapter(this, discoveredDevices);
        lvDevices.setAdapter(deviceListAdapter);

        if (discoveredDevices.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        deviceListDialog = builder.create();

        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice selectedDevice = discoveredDevices.get(position);
            stopDiscovery();
            deviceListDialog.dismiss();
            connectToDevice(selectedDevice);
        });

        btnDialogScan.setOnClickListener(v -> {
            if (isScanning) {
                stopDiscovery();
                btnDialogScan.setText("Scan Nearby");
                layoutScanning.setVisibility(View.GONE);
            } else {
                startDiscovery();
                btnDialogScan.setText(R.string.stop_scan);
                layoutScanning.setVisibility(View.VISIBLE);
            }
        });

        btnDialogCancel.setOnClickListener(v -> {
            stopDiscovery();
            deviceListDialog.dismiss();
        });

        deviceListDialog.setOnDismissListener(dialog -> stopDiscovery());
        deviceListDialog.show();
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        if (hidDevice == null) {
            Toast.makeText(this, "HID Service is initializing...", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = (device.getName() != null) ? device.getName() : device.getAddress();
        updateStatusUi("Connecting to " + name + "…", getColor(R.color.status_connecting));

        try {
            boolean result = hidDevice.connect(device);
            Log.d(TAG, "HID connect initiated to " + name + ", success = " + result);
            if (!result) {
                Toast.makeText(this, "Connection attempt initiated. Ensure PC Bluetooth is ON.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect to device", e);
            Toast.makeText(this, "Failed to connect: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void startDiscovery() {
        if (bluetoothAdapter == null) return;
        registerDiscoveryReceiver();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        isScanning = true;
    }

    @SuppressLint("MissingPermission")
    private void stopDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        isScanning = false;
    }

    private boolean isReceiverRegistered = false;

    private void registerDiscoveryReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(discoveryReceiver, filter);
            isReceiverRegistered = true;
        }
    }

    private void unregisterDiscoveryReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(discoveryReceiver);
            } catch (Exception ignored) {
            }
            isReceiverRegistered = false;
        }
    }

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getAddress() != null && !discoveredAddresses.contains(device.getAddress())) {
                    discoveredAddresses.add(device.getAddress());
                    discoveredDevices.add(device);
                    if (deviceListAdapter != null) {
                        deviceListAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    // =========================================================================
    // Settings Dialog
    // =========================================================================

    private void showSettingsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        TextView tvSpeedVal = dialogView.findViewById(R.id.tv_speed_val);
        SeekBar sbSpeed = dialogView.findViewById(R.id.sb_speed);
        TextView tvScrollSpeedVal = dialogView.findViewById(R.id.tv_scroll_speed_val);
        SeekBar sbScrollSpeed = dialogView.findViewById(R.id.sb_scroll_speed);
        SwitchMaterial switchHaptic = dialogView.findViewById(R.id.switch_haptic);
        Button btnClose = dialogView.findViewById(R.id.btn_close_settings);

        int currentSpeedProgress = Math.round(cursorSensitivity * 10f);
        sbSpeed.setProgress(currentSpeedProgress);
        tvSpeedVal.setText(getString(R.string.sensitivity_label, cursorSensitivity));

        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float val = Math.max(0.2f, progress / 10.0f);
                cursorSensitivity = val;
                tvSpeedVal.setText(getString(R.string.sensitivity_label, val));
                if (gestureListener != null) {
                    gestureListener.setSensitivity(val);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                savePreferences();
            }
        });

        int currentScrollProgress = Math.round(scrollSpeed * 10f);
        sbScrollSpeed.setProgress(currentScrollProgress);
        tvScrollSpeedVal.setText(getString(R.string.scroll_speed_label, scrollSpeed));

        sbScrollSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float val = Math.max(0.2f, progress / 10.0f);
                scrollSpeed = val;
                tvScrollSpeedVal.setText(getString(R.string.scroll_speed_label, val));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                savePreferences();
            }
        });

        switchHaptic.setChecked(hapticEnabled);
        switchHaptic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hapticEnabled = isChecked;
            savePreferences();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // =========================================================================
    // Device List Adapter
    // =========================================================================

    private static class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

        public DeviceListAdapter(Context context, List<BluetoothDevice> devices) {
            super(context, 0, devices);
        }

        @NonNull
        @SuppressLint("MissingPermission")
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
            }

            BluetoothDevice device = getItem(position);
            TextView tvName = convertView.findViewById(R.id.tv_device_name);
            TextView tvAddress = convertView.findViewById(R.id.tv_device_address);
            TextView tvStatus = convertView.findViewById(R.id.tv_device_status_tag);

            if (device != null) {
                String name = device.getName();
                tvName.setText((name != null && !name.isEmpty()) ? name : "Unknown Device");
                tvAddress.setText(device.getAddress());

                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    tvStatus.setText("Paired");
                    tvStatus.setTextColor(getContext().getColor(R.color.primary_light));
                } else {
                    tvStatus.setText("Discovered");
                    tvStatus.setTextColor(getContext().getColor(R.color.text_secondary));
                }
            }

            return convertView;
        }
    }
}
