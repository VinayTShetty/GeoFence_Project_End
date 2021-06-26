package co.geeksters.radar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.location.Location;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Karam Ahkouk on 03/06/15.
 */
public class RadarScanView extends View {

    private ArrayList<RadarPoint> pinsInCanvas = new ArrayList<RadarPoint>();
    private Context context;
    private Canvas canvas;
    private int zoomDistance;


    private ArrayList<RadarPoint> points = new ArrayList<RadarPoint>();

    private RadarPoint referencePoint;


    private final int DEFAULT_MAX_DISTANCE = 10000;

    private final int DEFAULT_PINS_RADIUS = 12;
    private final int DEFAULT_CENTER_PIN_RADIUS = 18;
    private final int DEFAULT_PINS_COLORS = getResources().getColor(R.color.light_green);
    private final int DEFAULT_CENTER_PIN_COLOR = getResources().getColor(R.color.red);
    private final int DEFAULT_BACKGROUND_COLOR = getResources().getColor(R.color.dark_green);

    private int pinsImage;
    private int centerPinImage;

    private int radarBackground;
    private int maxDistance;
    private int pinsRadius;
    private int centerPinRadius;
    private int pinsColor;
    private int centerPinColor;
    private int backgroundColor;
    private int start;
    private Matrix matrix;

    public RadarScanView(Context context) {
        this(context, null);
        matrix = new Matrix();
        handler.post(run);
    }

    public RadarScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        matrix = new Matrix();
        handler.post(run);
    }

    public RadarScanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        matrix = new Matrix();
        handler.post(run);
        referencePoint = new RadarPoint("Boat", 10.00000f, 22.0000f, 0, 0);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.radar, 0, 0);

        try {

            pinsRadius = ta.getInt(R.styleable.radar_pins_radius, 0);
            centerPinRadius = ta.getInt(R.styleable.radar_center_pin_radius, 0);

            radarBackground = ta.getResourceId(R.styleable.radar_radar_image, 0);
            pinsImage = ta.getResourceId(R.styleable.radar_pins_image, 0);
            centerPinImage = ta.getResourceId(R.styleable.radar_center_pin_image, 0);

            if (ta.getString(R.styleable.radar_pins_color) != null) {
                pinsColor = Color.parseColor(ta.getString(R.styleable.radar_pins_color));
            }
            if (ta.getString(R.styleable.radar_center_pin_color) != null) {
                centerPinColor = Color.parseColor(ta.getString(R.styleable.radar_center_pin_color));
            }
            if (ta.getString(R.styleable.radar_background_color) != null) {
                backgroundColor = Color.parseColor(ta.getString(R.styleable.radar_background_color));
            }

            maxDistance = ta.getInt(R.styleable.radar_max_distance, 0);
        } finally {
            ta.recycle();
        }
    }

    public void setPoints(ArrayList<RadarPoint> points) {
        this.points = points;
    }

    public ArrayList<RadarPoint> getPoints() {
        return points;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        makeRadar();
    }

    private Handler handler = new Handler();
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            start += 2;
            matrix = new Matrix();
            matrix.postRotate(start, getWidth() / 2, getWidth() / 2);
            refresh();
            handler.postDelayed(run, 10);
        }
    };

    public void refresh() {
        invalidate();
    }

    protected void makeRadar() {
        pinsInCanvas = new ArrayList<RadarPoint>();
        int width = getWidth();
        int radiusPart = width / 2;
        Paint mPaintRadar = new Paint();
        mPaintRadar.setColor(getBackgroundColor());
        mPaintRadar.setAntiAlias(true);
        Shader shader = new SweepGradient(radiusPart, radiusPart, Color.TRANSPARENT, Color.parseColor("#4400FF00"));
//        Shader shader = new SweepGradient(radiusPart, radiusPart, Color.parseColor("#00A8D7A7"),
//                Color.parseColor("#ffA8D7A7"));
        mPaintRadar.setShader(shader);
        canvas.concat(matrix);
        canvas.drawCircle(radiusPart, radiusPart, radiusPart, mPaintRadar);

//        if (radarBackground != 0) {
//            drawImage(0, 0, radarBackground, width);
//        } else {
//            int radiusPart = width / 2;
//            drawPinCircle(width / 2, width / 2, getBackgroundColor(), (width / 2) - 100);
//            drawPinCircle(width / 2, width / 2, getBackgroundColor(), (width / 2) - 200);
//            drawPinCircle(width / 2, width / 2, getBackgroundColor(), (width / 2) - 300);
//            drawPinCircle(width / 2, width / 2, getBackgroundColor(), (width / 2) - 400);
//            drawPinCircle(width / 2, width / 2, getBackgroundColor(), width / 2);
//            Paint mPaintLine = new Paint();
//            mPaintLine.setColor(getBackgroundColor());
//            mPaintLine.setStrokeWidth(5);
//            int points = 8; //number of points
//            float pointAngle = 360 / points; //angle between points
//            for (float angle = 0; angle < 360; angle = angle + pointAngle) { //move round the circle to each point
//                float x = (float) (Math.cos(Math.toRadians(angle)) * radiusPart); //convert angle to radians for x and y coordinates
//                float y = (float) (Math.sin(Math.toRadians(angle)) * radiusPart);
//                canvas.drawLine(radiusPart, radiusPart, x + radiusPart, y + radiusPart, mPaintLine);
//            }
//            Paint mPaintRadar = new Paint();
//            mPaintRadar.setColor(getBackgroundColor());
//            mPaintRadar.setAntiAlias(true);
//
//            Shader shader = new SweepGradient(radiusPart, radiusPart, Color.parseColor("#00A8D7A7"),
//                    Color.parseColor("#ffA8D7A7"));
//            mPaintRadar.setShader(shader);
//            canvas.concat(matrix);
//            canvas.drawCircle(radiusPart, radiusPart, radiusPart, mPaintRadar);
//        }
//
//        if (centerPinImage != 0) {
//            long pnt = (width / 2) - getCenterPinRadius();
//            drawImage(pnt, pnt, centerPinImage, getCenterPinRadius() * 2);
//        } else {
//            drawPin(width / 2, width / 2, getCenterPinColor(), getCenterPinRadius());
//        }
//
//        int pxCanvas = width / 2;
//        int metterDistance;
//
//        maxDistance = getMaxDistance();
//
//        Location u0 = new Location("");
//        u0.setLatitude(referencePoint.x);
//        u0.setLongitude(referencePoint.y);
//
//        ArrayList<VoDrawLocationPoint> locations = buildLocations(u0);
//
//        metterDistance = zoomDistance + (zoomDistance / 16);
//        if (metterDistance > maxDistance) metterDistance = maxDistance;
//
//        drawPins(u0, locations, pxCanvas, metterDistance);

    }

    private int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    ArrayList<VoDrawLocationPoint> buildLocations(Location referenceLocation) {
        zoomDistance = 0;
        ArrayList<VoDrawLocationPoint> locations = new ArrayList<VoDrawLocationPoint>();
        for (int i = 0; i < points.size(); i++) {
            Location uLocation = new Location("");
            uLocation.setLatitude(points.get(i).x);
            uLocation.setLongitude(points.get(i).y);

            locations.add(new VoDrawLocationPoint(uLocation, points.get(i).angle, points.get(i).pinColor));
            if (zoomDistance < distanceBetween(referenceLocation, uLocation)) {
                zoomDistance = Math.round(distanceBetween(referenceLocation, uLocation));
            }
        }
        return locations;
    }

    void drawPins(Location referenceLocation, ArrayList<VoDrawLocationPoint> drawLocationArray, int pxCanvas, int metterDistance) {

//        Random rand = new Random();
        for (int i = 0; i < drawLocationArray.size(); i++) {
            int distance = Math.round(distanceBetween(referenceLocation, drawLocationArray.get(i).location));
            if (distance > maxDistance) continue;
            int virtualDistance = 1;
            if (metterDistance > 0) {
                virtualDistance = (distance * pxCanvas / metterDistance);
            }

//            int angle = rand.nextInt(360) + 1;

            long cX = pxCanvas + Math.round(virtualDistance * Math.cos(drawLocationArray.get(i).drawAngle * Math.PI / 180));
            long cY = pxCanvas + Math.round(virtualDistance * Math.sin(drawLocationArray.get(i).drawAngle * Math.PI / 180));

            pinsInCanvas.add(new RadarPoint(points.get(i).identifier, cX, cY, getPinsRadius() * 2, drawLocationArray.get(i).drawAngle));

            if (pinsImage != 0) {
                long pnt = cX - getPinsRadius();
                long pnt2 = cY - getPinsRadius();
                drawImage(pnt, pnt2, pinsImage, getPinsRadius() * 2);
            } else {
                drawPin(cX, cY, getPinsColor(), getPinsRadius());
            }
        }
    }


    float distanceBetween(Location l1, Location l2) {
        float lat1 = (float) l1.getLatitude();
        float lon1 = (float) l1.getLongitude();
        float lat2 = (float) l2.getLatitude();
        float lon2 = (float) l2.getLongitude();
        float R = 6371; // km
        float dLat = (float) ((lat2 - lat1) * Math.PI / 180);
        float dLon = (float) ((lon2 - lon1) * Math.PI / 180);
        lat1 = (float) (lat1 * Math.PI / 180);
        lat2 = (float) (lat2 * Math.PI / 180);

        float a = (float) (Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        float d = R * c * 1000;

        return d;
    }

    public void drawImage(long x, long y, int image, int size) {
        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), image);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap, size, size, true);

        canvas.drawBitmap(scaledBitmap, x, y, null);
    }

    public void drawPin(long x, long y, int Color, int radius) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color);
        canvas.drawCircle(x, y, radius, paint);
    }

    public void drawPinCircle(long x, long y, int Color, int radius) {
        Paint mPaintCircle = new Paint();
        mPaintCircle.setColor(Color);
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setStrokeWidth(5);//画笔宽度
        DashPathEffect dashPath = new DashPathEffect(new float[]{10, 10}, (float) 2.0);
        mPaintCircle.setPathEffect(dashPath);
        canvas.drawCircle(x, y, radius, mPaintCircle);
    }

    public String getTouchedPin(MotionEvent event) {

        int xTouch;
        int yTouch;

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside some circle
                return getTouchedCircle(xTouch, yTouch);
        }
        return null;
    }

    private String getTouchedCircle(final int xTouch, final int yTouch) {
        RadarPoint touched = null;

        for (RadarPoint rPoint : pinsInCanvas) {

            if ((rPoint.x - xTouch) * (rPoint.x - xTouch) + (rPoint.y - yTouch) * (rPoint.y - yTouch) <= rPoint.radius * rPoint.radius) {
                touched = rPoint;
                break;
            }
        }

        if (touched != null) return touched.identifier;
        return null;
    }

    public int getPinsRadius() {
        if (pinsRadius == 0) return DEFAULT_PINS_RADIUS;
        return pinsRadius;
    }

    public int getPinsColor() {
        if (pinsColor == 0) return DEFAULT_PINS_COLORS;
        return pinsColor;
    }

    public int getCenterPinRadius() {
        if (centerPinRadius == 0) return DEFAULT_CENTER_PIN_RADIUS;
        return centerPinRadius;
    }

    public int getCenterPinColor() {
        if (centerPinColor == 0) return DEFAULT_CENTER_PIN_COLOR;
        return centerPinColor;
    }

    public int getBackgroundColor() {
        if (backgroundColor == 0) return DEFAULT_BACKGROUND_COLOR;
        return backgroundColor;
    }

    public int getMaxDistance() {
        if (maxDistance == 0) return DEFAULT_MAX_DISTANCE;
        if (maxDistance < 0) return 1000000000;
        return maxDistance;
    }


    public void setReferencePoint(RadarPoint referencePoint) {
        this.referencePoint = referencePoint;
    }
}
