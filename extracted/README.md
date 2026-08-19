# Bluetooth Touchpad & Keyboard (Android HID)

A driverless Bluetooth HID application that turns your Android device into a wireless Touchpad (Mouse) and Keyboard for PCs, laptops, and tablets.

## Features
- **Touchpad Mouse**: Smooth cursor navigation, left/right/middle click, double tap, and dedicated scroll strip.
- **Keyboard & Shortcuts**: Full text transmission, special keys (`Enter`, `Backspace`, `Esc`, `Tab`, `Space`, `Win/Cmd`), hotkeys (`Ctrl+C`, `Ctrl+V`, `Ctrl+Z`, `Ctrl+A`, `Alt+Tab`, arrow keys), and media controls (`Play/Pause`, `Volume`, `Mute`).
- **Driverless Connection**: Uses native Android `BluetoothHidDevice` API (API 28+) — no server software required on the connected PC.

---

## 🚀 GitHub Actions দিয়ে APK বিল্ড করার নিয়ম

এই প্রোজেক্টে স্বয়ংক্রিয়ভাবে **GitHub Actions** ওয়ার্কফ্লো কনফিগার করা আছে।

### যেভাবে APK ডাউনলোড করবেন:
1. কোডটি আপনার **GitHub Repository**-তে পুশ (Push) করুন।
2. GitHub রিপোজিটরির **Actions** ট্যাবে যান।
3. **"Build Android APK"** ওয়ার্কফ্লোটি স্বয়ংক্রিয়ভাবে রান হবে (অথবা **Run workflow** বাটনে ক্লিক করে ম্যানুয়ালি রান করতে পারেন)।
4. বিল্ড সম্পন্ন হলে **Artifacts** সেকশনে ক্লিক করে `BluetoothTouchpad-Debug-APK` বা `BluetoothTouchpad-Release-APK` জিপ ফাইলটি ডাউনলোড করে নিন।
5. আনজিপ করলেই আপনার ইন্সটলযোগ্য `.apk` ফাইল পেয়ে যাবেন।
