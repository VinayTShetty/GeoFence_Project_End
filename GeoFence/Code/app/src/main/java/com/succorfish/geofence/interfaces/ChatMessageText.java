package com.succorfish.geofence.interfaces;

import android.content.DialogInterface;

public interface ChatMessageText {
      void PositiveMethod(DialogInterface dialog, int id, String typedMessage);
      void NegativeMethod(DialogInterface dialog, int id);
}
