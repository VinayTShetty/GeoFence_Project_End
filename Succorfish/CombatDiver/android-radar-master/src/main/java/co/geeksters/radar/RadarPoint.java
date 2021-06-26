package co.geeksters.radar;

/**
 * Created by Karam Ahkouk on 04/06/15.
 */
public class RadarPoint {
    float x;
    float y;
    int radius;
    int angle;
    int pinColor;
    String identifier;

    public RadarPoint(String identifier, float x, float y, int radius, int angle, int color) {
        this.identifier = identifier;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public RadarPoint(String identifier, float x, float y, int angle, int color) {
        this.identifier = identifier;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.pinColor = color;
    }
}
