package com.example;

/**
 * Standard USB / Bluetooth HID Mouse Report Descriptor and Protocol Constants.
 * Compatible wrapper mapping to unified composite HidDescriptor.
 */
public final class MouseHidDescriptor {

    private MouseHidDescriptor() {
        // Non-instantiable utility class
    }

    public static final byte REPORT_ID_MOUSE = HidDescriptor.REPORT_ID_MOUSE;
    public static final byte BUTTON_NONE = HidDescriptor.MOUSE_BUTTON_NONE;
    public static final byte BUTTON_LEFT = HidDescriptor.MOUSE_BUTTON_LEFT;
    public static final byte BUTTON_RIGHT = HidDescriptor.MOUSE_BUTTON_RIGHT;
    public static final byte BUTTON_MIDDLE = HidDescriptor.MOUSE_BUTTON_MIDDLE;

    public static final byte[] MOUSE_REPORT_DESCRIPTOR = HidDescriptor.COMPOSITE_REPORT_DESCRIPTOR;

    public static byte[] buildReport(byte buttons, int dx, int dy, int wheel) {
        return HidDescriptor.buildMouseReport(buttons, dx, dy, wheel);
    }
}

