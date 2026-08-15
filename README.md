# QuirkBoard Android Keyboard

This is an actual Android IME, not a website keyboard.

## What it does
- Appears as a selectable Android keyboard.
- Sends text to other apps through Android's InputConnection API.
- Applies custom substitutions such as `e -> &` and `s -> £`.
- Autocorrect works on the ORIGINAL spelling before substitutions are applied.
- Example: a typo corresponding to `hello` is corrected to `hello`, then output becomes `h&llo`.
- Settings screen lets you edit the mappings.

## Install
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Build/install the app.
4. Open QuirkBoard.
5. Tap `ENABLE / CHOOSE QUIRKBOARD KEYBOARD`.
6. Enable QuirkBoard and select it as the current keyboard.
7. Type in another app.

## Important
This replaces the phone's active software keyboard with QuirkBoard. Android does not provide a normal way for a website or ordinary app to sit invisibly on top of another keyboard and rewrite everything it types. The supported approach is an IME (`InputMethodService`).
