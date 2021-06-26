package co.geeksters.radar;

import android.location.Location;

/**
 * Created by Jaydeep on 23-01-2018.
 */

public class VoDrawLocationPoint {
    Location location;
    int drawAngle = 0;
    int drawColor = 0;

    public VoDrawLocationPoint(Location location, int angle, int color) {
        this.location = location;
        this.drawAngle = angle;
        this.drawColor = color;
    }
}
