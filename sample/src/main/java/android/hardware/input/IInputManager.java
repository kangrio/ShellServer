package android.hardware.input;

import android.view.InputEvent;

public interface IInputManager {
    boolean injectInputEvent(InputEvent inputEvent, int i);
}
