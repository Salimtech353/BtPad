package com.example;

/**
 * Composite USB / Bluetooth HID Report Descriptor and Protocol Constants.
 *
 * Defines standard HID report structures conforming to USB HID 1.11 specification
 * for a composite peripheral providing:
 *  1. Mouse (Report ID 1): 3 Buttons, Relative X/Y, Vertical Scroll Wheel
 *  2. Keyboard (Report ID 2): Modifier Keys, Reserved Byte, 6-Key Rollover Array
 *  3. Consumer Control / Media Keys (Report ID 3): Play/Pause, Next, Prev, Volume, Mute
 */
public final class HidDescriptor {

    private HidDescriptor() {
        // Non-instantiable utility class
    }

    // =========================================================================
    // Report IDs
    // =========================================================================
    public static final byte REPORT_ID_MOUSE = 1;
    public static final byte REPORT_ID_KEYBOARD = 2;
    public static final byte REPORT_ID_CONSUMER = 3;

    // =========================================================================
    // Mouse Constants & Masks
    // =========================================================================
    public static final byte MOUSE_BUTTON_NONE = 0x00;
    public static final byte MOUSE_BUTTON_LEFT = 0x01;
    public static final byte MOUSE_BUTTON_RIGHT = 0x02;
    public static final byte MOUSE_BUTTON_MIDDLE = 0x04;

    // =========================================================================
    // Keyboard Modifier Masks (Byte 0 of Keyboard Report)
    // =========================================================================
    public static final byte MODIFIER_NONE = 0x00;
    public static final byte MODIFIER_LEFT_CTRL = 0x01;
    public static final byte MODIFIER_LEFT_SHIFT = 0x02;
    public static final byte MODIFIER_LEFT_ALT = 0x04;
    public static final byte MODIFIER_LEFT_GUI = 0x08;  // Windows Key / Cmd / Super
    public static final byte MODIFIER_RIGHT_CTRL = 0x10;
    public static final byte MODIFIER_RIGHT_SHIFT = 0x20;
    public static final byte MODIFIER_RIGHT_ALT = 0x40;
    public static final byte MODIFIER_RIGHT_GUI = (byte) 0x80;

    // =========================================================================
    // Keyboard Standard Keycodes (HID Usage Table Page 0x07)
    // =========================================================================
    public static final byte KEY_NONE = 0x00;
    public static final byte KEY_A = 0x04;
    public static final byte KEY_B = 0x05;
    public static final byte KEY_C = 0x06;
    public static final byte KEY_D = 0x07;
    public static final byte KEY_E = 0x08;
    public static final byte KEY_F = 0x09;
    public static final byte KEY_G = 0x0A;
    public static final byte KEY_H = 0x0B;
    public static final byte KEY_I = 0x0C;
    public static final byte KEY_J = 0x0D;
    public static final byte KEY_K = 0x0E;
    public static final byte KEY_L = 0x0F;
    public static final byte KEY_M = 0x10;
    public static final byte KEY_N = 0x11;
    public static final byte KEY_O = 0x12;
    public static final byte KEY_P = 0x13;
    public static final byte KEY_Q = 0x14;
    public static final byte KEY_R = 0x15;
    public static final byte KEY_S = 0x16;
    public static final byte KEY_T = 0x17;
    public static final byte KEY_U = 0x18;
    public static final byte KEY_V = 0x19;
    public static final byte KEY_W = 0x1A;
    public static final byte KEY_X = 0x1B;
    public static final byte KEY_Y = 0x1C;
    public static final byte KEY_Z = 0x1D;

    public static final byte KEY_1 = 0x1E;
    public static final byte KEY_2 = 0x1F;
    public static final byte KEY_3 = 0x20;
    public static final byte KEY_4 = 0x21;
    public static final byte KEY_5 = 0x22;
    public static final byte KEY_6 = 0x23;
    public static final byte KEY_7 = 0x24;
    public static final byte KEY_8 = 0x25;
    public static final byte KEY_9 = 0x26;
    public static final byte KEY_0 = 0x27;

    public static final byte KEY_ENTER = 0x28;
    public static final byte KEY_ESC = 0x29;
    public static final byte KEY_BACKSPACE = 0x2A;
    public static final byte KEY_TAB = 0x2B;
    public static final byte KEY_SPACE = 0x2C;
    public static final byte KEY_MINUS = 0x2D;
    public static final byte KEY_EQUAL = 0x2E;
    public static final byte KEY_LEFT_BRACKET = 0x2F;
    public static final byte KEY_RIGHT_BRACKET = 0x30;
    public static final byte KEY_BACKSLASH = 0x31;
    public static final byte KEY_SEMICOLON = 0x33;
    public static final byte KEY_APOSTROPHE = 0x34;
    public static final byte KEY_GRAVE = 0x35;
    public static final byte KEY_COMMA = 0x36;
    public static final byte KEY_PERIOD = 0x37;
    public static final byte KEY_SLASH = 0x38;
    public static final byte KEY_CAPS_LOCK = 0x39;

    public static final byte KEY_F1 = 0x3A;
    public static final byte KEY_F2 = 0x3B;
    public static final byte KEY_F3 = 0x3C;
    public static final byte KEY_F4 = 0x3D;
    public static final byte KEY_F5 = 0x3E;
    public static final byte KEY_F6 = 0x3F;
    public static final byte KEY_F7 = 0x40;
    public static final byte KEY_F8 = 0x41;
    public static final byte KEY_F9 = 0x42;
    public static final byte KEY_F10 = 0x43;
    public static final byte KEY_F11 = 0x44;
    public static final byte KEY_F12 = 0x45;

    public static final byte KEY_PRINT_SCREEN = 0x46;
    public static final byte KEY_SCROLL_LOCK = 0x47;
    public static final byte KEY_PAUSE = 0x48;
    public static final byte KEY_INSERT = 0x49;
    public static final byte KEY_HOME = 0x4A;
    public static final byte KEY_PAGE_UP = 0x4B;
    public static final byte KEY_DELETE = 0x4C;
    public static final byte KEY_END = 0x4D;
    public static final byte KEY_PAGE_DOWN = 0x4E;

    public static final byte KEY_RIGHT_ARROW = 0x4F;
    public static final byte KEY_LEFT_ARROW = 0x50;
    public static final byte KEY_DOWN_ARROW = 0x51;
    public static final byte KEY_UP_ARROW = 0x52;

    // =========================================================================
    // Consumer / Media Key Usages (HID Usage Table Page 0x0C)
    // =========================================================================
    public static final short MEDIA_PLAY_PAUSE = 0x00CD;
    public static final short MEDIA_NEXT_TRACK = 0x00B5;
    public static final short MEDIA_PREV_TRACK = 0x00B6;
    public static final short MEDIA_STOP = 0x00B7;
    public static final short MEDIA_VOLUME_UP = 0x00E9;
    public static final short MEDIA_VOLUME_DOWN = 0x00EA;
    public static final short MEDIA_MUTE = 0x00E2;

    // =========================================================================
    // Composite HID Report Descriptor (Mouse + Keyboard + Consumer)
    // =========================================================================
    public static final byte[] COMPOSITE_REPORT_DESCRIPTOR = new byte[] {
        // =====================================================================
        // COLLECTION 1: MOUSE (Report ID 1)
        // =====================================================================
        (byte) 0x05, (byte) 0x01,         // Usage Page (Generic Desktop Controls)
        (byte) 0x09, (byte) 0x02,         // Usage (Mouse)
        (byte) 0xA1, (byte) 0x01,         // Collection (Application)
        (byte) 0x09, (byte) 0x01,         //   Usage (Pointer)
        (byte) 0xA1, (byte) 0x00,         //   Collection (Physical)
        (byte) 0x85, REPORT_ID_MOUSE,     //     Report ID (1)
        // Buttons (Left, Right, Middle)
        (byte) 0x05, (byte) 0x09,         //     Usage Page (Button)
        (byte) 0x19, (byte) 0x01,         //     Usage Minimum (Button 1)
        (byte) 0x29, (byte) 0x03,         //     Usage Maximum (Button 3)
        (byte) 0x15, (byte) 0x00,         //     Logical Minimum (0)
        (byte) 0x25, (byte) 0x01,         //     Logical Maximum (1)
        (byte) 0x95, (byte) 0x03,         //     Report Count (3)
        (byte) 0x75, (byte) 0x01,         //     Report Size (1)
        (byte) 0x81, (byte) 0x02,         //     Input (Data, Variable, Absolute)
        // Padding bits (5 bits to align 1 byte)
        (byte) 0x95, (byte) 0x01,         //     Report Count (1)
        (byte) 0x75, (byte) 0x05,         //     Report Size (5)
        (byte) 0x81, (byte) 0x03,         //     Input (Constant, Variable, Absolute)
        // Relative X, Y Movement
        (byte) 0x05, (byte) 0x01,         //     Usage Page (Generic Desktop)
        (byte) 0x09, (byte) 0x30,         //     Usage (X)
        (byte) 0x09, (byte) 0x31,         //     Usage (Y)
        (byte) 0x15, (byte) 0x81,         //     Logical Minimum (-127)
        (byte) 0x25, (byte) 0x7F,         //     Logical Maximum (127)
        (byte) 0x75, (byte) 0x08,         //     Report Size (8)
        (byte) 0x95, (byte) 0x02,         //     Report Count (2)
        (byte) 0x81, (byte) 0x06,         //     Input (Data, Variable, Relative)
        // Vertical Scroll Wheel
        (byte) 0x09, (byte) 0x38,         //     Usage (Wheel)
        (byte) 0x15, (byte) 0x81,         //     Logical Minimum (-127)
        (byte) 0x25, (byte) 0x7F,         //     Logical Maximum (127)
        (byte) 0x75, (byte) 0x08,         //     Report Size (8)
        (byte) 0x95, (byte) 0x01,         //     Report Count (1)
        (byte) 0x81, (byte) 0x06,         //     Input (Data, Variable, Relative)
        (byte) 0xC0,                      //   End Collection (Physical)
        (byte) 0xC0,                      // End Collection (Application)

        // =====================================================================
        // COLLECTION 2: KEYBOARD (Report ID 2)
        // =====================================================================
        (byte) 0x05, (byte) 0x01,         // Usage Page (Generic Desktop Controls)
        (byte) 0x09, (byte) 0x06,         // Usage (Keyboard)
        (byte) 0xA1, (byte) 0x01,         // Collection (Application)
        (byte) 0x85, REPORT_ID_KEYBOARD,  //   Report ID (2)
        // Modifiers (L-Ctrl, L-Shift, L-Alt, L-GUI, R-Ctrl, R-Shift, R-Alt, R-GUI)
        (byte) 0x05, (byte) 0x07,         //   Usage Page (Key Codes)
        (byte) 0x19, (byte) 0xE0,         //   Usage Minimum (Left Control)
        (byte) 0x29, (byte) 0xE7,         //   Usage Maximum (Right GUI)
        (byte) 0x15, (byte) 0x00,         //   Logical Minimum (0)
        (byte) 0x25, (byte) 0x01,         //   Logical Maximum (1)
        (byte) 0x75, (byte) 0x01,         //   Report Size (1)
        (byte) 0x95, (byte) 0x08,         //   Report Count (8)
        (byte) 0x81, (byte) 0x02,         //   Input (Data, Variable, Absolute)
        // Reserved Byte (1 byte)
        (byte) 0x95, (byte) 0x01,         //   Report Count (1)
        (byte) 0x75, (byte) 0x08,         //   Report Size (8)
        (byte) 0x81, (byte) 0x01,         //   Input (Constant)
        // LED Status Report (5 LEDs: Num Lock, Caps Lock, Scroll Lock, Compose, Kana)
        (byte) 0x95, (byte) 0x05,         //   Report Count (5)
        (byte) 0x75, (byte) 0x01,         //   Report Size (1)
        (byte) 0x05, (byte) 0x08,         //   Usage Page (LEDs)
        (byte) 0x19, (byte) 0x01,         //   Usage Minimum (Num Lock)
        (byte) 0x29, (byte) 0x05,         //   Usage Maximum (Kana)
        (byte) 0x91, (byte) 0x02,         //   Output (Data, Variable, Absolute)
        // LED Padding (3 bits)
        (byte) 0x95, (byte) 0x01,         //   Report Count (1)
        (byte) 0x75, (byte) 0x03,         //   Report Size (3)
        (byte) 0x91, (byte) 0x01,         //   Output (Constant)
        // 6-Key Rollover Array
        (byte) 0x95, (byte) 0x06,         //   Report Count (6)
        (byte) 0x75, (byte) 0x08,         //   Report Size (8)
        (byte) 0x15, (byte) 0x00,         //   Logical Minimum (0)
        (byte) 0x25, (byte) 0x65,         //   Logical Maximum (101 keys)
        (byte) 0x05, (byte) 0x07,         //   Usage Page (Key Codes)
        (byte) 0x19, (byte) 0x00,         //   Usage Minimum (0)
        (byte) 0x29, (byte) 0x65,         //   Usage Maximum (101)
        (byte) 0x81, (byte) 0x00,         //   Input (Data, Array, Absolute)
        (byte) 0xC0,                      // End Collection (Application)

        // =====================================================================
        // COLLECTION 3: CONSUMER CONTROL / MEDIA KEYS (Report ID 3)
        // =====================================================================
        (byte) 0x05, (byte) 0x0C,         // Usage Page (Consumer)
        (byte) 0x09, (byte) 0x01,         // Usage (Consumer Control)
        (byte) 0xA1, (byte) 0x01,         // Collection (Application)
        (byte) 0x85, REPORT_ID_CONSUMER,  //   Report ID (3)
        (byte) 0x15, (byte) 0x00,         //   Logical Minimum (0)
        (byte) 0x26, (byte) 0xFF, (byte) 0x03, // Logical Maximum (1023)
        (byte) 0x19, (byte) 0x00,         //   Usage Minimum (0)
        (byte) 0x2A, (byte) 0xFF, (byte) 0x03, // Usage Maximum (1023)
        (byte) 0x75, (byte) 0x10,         //   Report Size (16 bits)
        (byte) 0x95, (byte) 0x01,         //   Report Count (1)
        (byte) 0x81, (byte) 0x00,         //   Input (Data, Array, Absolute)
        (byte) 0xC0                       // End Collection (Application)
    };

    // =========================================================================
    // Report Builder Helpers
    // =========================================================================

    /**
     * Builds a 4-byte mouse input report.
     */
    public static byte[] buildMouseReport(byte buttons, int dx, int dy, int wheel) {
        byte clampedDx = (byte) Math.max(-127, Math.min(127, dx));
        byte clampedDy = (byte) Math.max(-127, Math.min(127, dy));
        byte clampedWheel = (byte) Math.max(-127, Math.min(127, wheel));
        return new byte[] { buttons, clampedDx, clampedDy, clampedWheel };
    }

    /**
     * Builds an 8-byte keyboard input report with a single key.
     */
    public static byte[] buildKeyboardReport(byte modifier, byte keycode) {
        return new byte[] { modifier, 0x00, keycode, 0x00, 0x00, 0x00, 0x00, 0x00 };
    }

    /**
     * Builds an 8-byte keyboard input report with up to 6 keys.
     */
    public static byte[] buildKeyboardReport(byte modifier, byte[] keycodes) {
        byte[] report = new byte[8];
        report[0] = modifier;
        report[1] = 0x00; // Reserved
        if (keycodes != null) {
            int len = Math.min(keycodes.length, 6);
            System.arraycopy(keycodes, 0, report, 2, len);
        }
        return report;
    }

    /**
     * Builds an empty keyboard release report.
     */
    public static byte[] buildKeyboardReleaseReport() {
        return new byte[8];
    }

    /**
     * Builds a 2-byte consumer / media key input report.
     */
    public static byte[] buildConsumerReport(short usageCode) {
        return new byte[] {
            (byte) (usageCode & 0xFF),
            (byte) ((usageCode >> 8) & 0xFF)
        };
    }

    /**
     * Key code and modifier container for text translation.
     */
    public static class KeyStroke {
        public final byte modifier;
        public final byte keycode;

        public KeyStroke(byte modifier, byte keycode) {
            this.modifier = modifier;
            this.keycode = keycode;
        }
    }

    /**
     * Maps an ASCII / Unicode character to HID Modifier and KeyCode.
     */
    public static KeyStroke charToKeyStroke(char c) {
        if (c >= 'a' && c <= 'z') {
            return new KeyStroke(MODIFIER_NONE, (byte) (KEY_A + (c - 'a')));
        }
        if (c >= 'A' && c <= 'Z') {
            return new KeyStroke(MODIFIER_LEFT_SHIFT, (byte) (KEY_A + (c - 'A')));
        }
        if (c >= '1' && c <= '9') {
            return new KeyStroke(MODIFIER_NONE, (byte) (KEY_1 + (c - '1')));
        }
        if (c == '0') {
            return new KeyStroke(MODIFIER_NONE, KEY_0);
        }

        switch (c) {
            case ' ': return new KeyStroke(MODIFIER_NONE, KEY_SPACE);
            case '\n':
            case '\r': return new KeyStroke(MODIFIER_NONE, KEY_ENTER);
            case '\t': return new KeyStroke(MODIFIER_NONE, KEY_TAB);
            case '\b': return new KeyStroke(MODIFIER_NONE, KEY_BACKSPACE);
            case '-': return new KeyStroke(MODIFIER_NONE, KEY_MINUS);
            case '_': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_MINUS);
            case '=': return new KeyStroke(MODIFIER_NONE, KEY_EQUAL);
            case '+': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_EQUAL);
            case '[': return new KeyStroke(MODIFIER_NONE, KEY_LEFT_BRACKET);
            case '{': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_LEFT_BRACKET);
            case ']': return new KeyStroke(MODIFIER_NONE, KEY_RIGHT_BRACKET);
            case '}': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_RIGHT_BRACKET);
            case '\\': return new KeyStroke(MODIFIER_NONE, KEY_BACKSLASH);
            case '|': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_BACKSLASH);
            case ';': return new KeyStroke(MODIFIER_NONE, KEY_SEMICOLON);
            case ':': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_SEMICOLON);
            case '\'': return new KeyStroke(MODIFIER_NONE, KEY_APOSTROPHE);
            case '"': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_APOSTROPHE);
            case '`': return new KeyStroke(MODIFIER_NONE, KEY_GRAVE);
            case '~': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_GRAVE);
            case ',': return new KeyStroke(MODIFIER_NONE, KEY_COMMA);
            case '<': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_COMMA);
            case '.': return new KeyStroke(MODIFIER_NONE, KEY_PERIOD);
            case '>': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_PERIOD);
            case '/': return new KeyStroke(MODIFIER_NONE, KEY_SLASH);
            case '?': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_SLASH);
            case '!': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_1);
            case '@': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_2);
            case '#': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_3);
            case '$': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_4);
            case '%': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_5);
            case '^': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_6);
            case '&': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_7);
            case '*': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_8);
            case '(': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_9);
            case ')': return new KeyStroke(MODIFIER_LEFT_SHIFT, KEY_0);
            default:
                return null;
        }
    }
}
