package com.xinyanruanjian.imagemapview;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageMapView extends AppCompatImageView {
    
    private static final String TAG = ImageMapView.class.getSimpleName();

    private final List<Area> areas = new ArrayList<>();
    
    private OnAreaClickListener onAreaClickListener;
    
    private float currentX;
    private float currentY;

    private OnClickListener onClickListener;

    private int mapWidth;
    private int mapHeight;

    public ImageMapView(@NonNull Context context) {
        this(context, null);
    }

    public ImageMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ImageMapView);
            int resourceId = ta.getResourceId(R.styleable.ImageMapView_imageMap, 0);
            if(resourceId != 0) {
                loadMapInfo(resourceId);
            }

            ta.recycle();
        }

        if(onClickListener == null) {
            setOnClickListener(v -> {});
        }
    }

    @Override
    public void setOnClickListener(@NonNull OnClickListener l) {
        if(null == l) {
            throw new IllegalArgumentException("OnClickListener must be not null");
        }
        onClickListener = l;
        super.setOnClickListener(l);
    }

    private void loadMapInfo(int xmlId) {
        try {
            XmlResourceParser xpp = getResources().getXml(xmlId);

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    // Start document
                    //  This is a useful branch for a debug log if
                    //  parsing is not working
                } else if (eventType == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();
                    if("map".equalsIgnoreCase(tag)) {
                        mapHeight = xpp.getAttributeIntValue(null, "height", 0);
                        mapWidth = xpp.getAttributeIntValue(null, "width", 0);
                    } else if ("area".equalsIgnoreCase(tag)) {
                        Area a = null;
                        String shape = xpp.getAttributeValue(null, "shape");
                        String coords = xpp.getAttributeValue(null, "coords");
                        int id = xpp.getIdAttributeResourceValue(0);

                        // as a name for this area, try to find any of these
                        // attributes
                        //  name attribute is custom to this impl (not standard in html area tag)
                        String name = xpp.getAttributeValue(null, "name");
                        if (name == null) {
                            name = xpp.getAttributeValue(null, "title");
                        }
                        if (name == null) {
                            name = xpp.getAttributeValue(null, "alt");
                        }

                        if ((shape != null) && (coords != null)) {
                            a = addShape(shape, name, coords, id);
                            if (a != null) {
                                // add all of the area tag attributes
                                // so that they are available to the
                                // implementation if needed (see getAreaAttribute)
                                for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                    String attrName = xpp.getAttributeName(i);
                                    String attrVal = xpp.getAttributeValue(null, attrName);
                                    a.addValue(attrName, attrVal);
                                }
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {

                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(mapWidth == 0 || mapHeight == 0) {
            throw new IllegalStateException("width and height must not be 0, width: " + mapWidth + ", height: " + mapHeight);
        }

        Log.v(TAG, "Area size: " + areas.size());
    }

    protected Area addShape(String shape, String name, String coords, int id) {
        Area a = null;
        if (id != 0) {
            if (shape.equalsIgnoreCase("rect")) {
                String[] v = coords.split(",");
                if (v.length == 4) {
                    a = new RectArea(id, name, Float.parseFloat(v[0]),
                            Float.parseFloat(v[1]),
                            Float.parseFloat(v[2]),
                            Float.parseFloat(v[3]));
                }
            }
            if (shape.equalsIgnoreCase("circle")) {
                String[] v = coords.split(",");
                if (v.length == 3) {
                    a = new CircleArea(id, name, Float.parseFloat(v[0]),
                            Float.parseFloat(v[1]),
                            Float.parseFloat(v[2])
                    );
                }
            }
            if (shape.equalsIgnoreCase("poly")) {
                a = new PolyArea(id, name, coords);
            }
            if (a != null) {
                addArea(a);
            }
        }
        return a;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.currentX = event.getX();
        this.currentY = event.getY();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        OnAreaClickListener listener = this.onAreaClickListener;

        if(listener != null) {
            Point point = getImagePoint(currentX, currentY);
            Log.v(TAG, "Map coor: " + point);
            synchronized (areas) {
                for (Area area : areas) {
                    if(area.isInArea(point.x, point.y)) {
                        Log.d(TAG, "Area " + area.getName() + "[" + area.getId() + "] was clicked");
                        listener.onAreaClicked(this, area);
                        return true;
                    }
                }
            }
        }
        
        return super.performClick();
    }

    private Point getImagePoint(float x, float y) {
        Matrix matrix = getImageMatrix();
        Matrix copy = new Matrix();
        matrix.invert(copy);
        RectF rectF = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, x, y);
            copy.mapRect(rectF);
            float scaleX = mapWidth * 1.0f / drawable.getIntrinsicWidth();
            float scaleY = mapHeight * 1.0f / drawable.getIntrinsicHeight();
            rectF.right = rectF.right * scaleX;
            rectF.bottom = rectF.bottom * scaleY;
        }

        return new Point(Math.round(rectF.right), Math.round(rectF.bottom));
    }

    public void addArea(Area area) {
        synchronized (areas) {
            areas.add(area);
        }
    }

    public synchronized void removeArea(Area area) {
        synchronized (areas) {
            areas.remove(area);
        }
    }

    public void setOnAreaClickListener(OnAreaClickListener onAreaClickListener) {
        this.onAreaClickListener = onAreaClickListener;
    }

    public OnAreaClickListener getOnAreaClickListener() {
        return onAreaClickListener;
    }

    /**
     * Area is abstract Base for tappable map areas
     * descendants provide hit test and focal point
     */
    public static abstract class Area {
        int id;
        String name;
        HashMap<String, String> values;

        public Area(int id, String name) {
            this.id = id;
            if (name != null) {
                this.name = name;
            }
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        // all xml values for the area are passed to the object
        // the default impl just puts them into a hashmap for
        // retrieval later
        public void addValue(String key, String value) {
            if (values == null) {
                values = new HashMap<String, String>();
            }
            values.put(key, value);
        }

        public String getValue(String key) {
            String value = null;
            if (values != null) {
                value = values.get(key);
            }
            return value;
        }

        abstract boolean isInArea(float x, float y);

        abstract float getOriginX();

        abstract float getOriginY();
    }

    /**
     * Rectangle Area
     */
    public static class RectArea extends Area {
        float left;
        float top;
        float right;
        float bottom;


        RectArea(int id, String name, float left, float top, float right, float bottom) {
            super(id, name);
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public boolean isInArea(float x, float y) {
            boolean ret = false;
            if ((x > left) && (x < right)) {
                if ((y > top) && (y < bottom)) {
                    ret = true;
                }
            }
            return ret;
        }

        public float getOriginX() {
            return left;
        }

        public float getOriginY() {
            return top;
        }

        @Override
        public String toString() {
            return "RectArea{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", values=" + values +
                    ", left=" + left +
                    ", top=" + top +
                    ", right=" + right +
                    ", bottom=" + bottom +
                    '}';
        }
    }

    /**
     * Polygon area
     */
    public static class PolyArea extends Area {
        ArrayList<Integer> xpoints = new ArrayList<Integer>();
        ArrayList<Integer> ypoints = new ArrayList<Integer>();

        // centroid point for this poly
        float x;
        float y;

        // number of points (don't rely on array size)
        int points;

        // bounding box
        int top = -1;
        int bottom = -1;
        int left = -1;
        int right = -1;

        public PolyArea(int id, String name, String coords) {
            super(id, name);

            // split the list of coordinates into points of the
            // polygon and compute a bounding box
            String[] v = coords.split(",");

            int i = 0;
            while ((i + 1) < v.length) {
                int x = Integer.parseInt(v[i]);
                int y = Integer.parseInt(v[i + 1]);
                xpoints.add(x);
                ypoints.add(y);
                top = (top == -1) ? y : Math.min(top, y);
                bottom = (bottom == -1) ? y : Math.max(bottom, y);
                left = (left == -1) ? x : Math.min(left, x);
                right = (right == -1) ? x : Math.max(right, x);
                i += 2;
            }
            points = xpoints.size();

            // add point zero to the end to make
            // computing area and centroid easier
            xpoints.add(xpoints.get(0));
            ypoints.add(ypoints.get(0));

            computeCentroid();
        }

        /**
         * area() and computeCentroid() are adapted from the implementation
         * of polygon.java  published from a princeton case study
         * The study is here: http://introcs.cs.princeton.edu/java/35purple/
         * The polygon.java source is here: http://introcs.cs.princeton.edu/java/35purple/Polygon.java.html
         */

        // return area of polygon
        public double area() {
            double sum = 0.0;
            for (int i = 0; i < points; i++) {
                sum = sum + (xpoints.get(i) * ypoints.get(i + 1)) - (ypoints.get(i) * xpoints.get(i + 1));
            }
            sum = 0.5 * sum;
            return Math.abs(sum);
        }

        // compute the centroid of the polygon
        public void computeCentroid() {
            double cx = 0.0, cy = 0.0;
            for (int i = 0; i < points; i++) {
                cx = cx + (xpoints.get(i) + xpoints.get(i + 1)) * (ypoints.get(i) * xpoints.get(i + 1) - xpoints.get(i) * ypoints.get(i + 1));
                cy = cy + (ypoints.get(i) + ypoints.get(i + 1)) * (ypoints.get(i) * xpoints.get(i + 1) - xpoints.get(i) * ypoints.get(i + 1));
            }
            cx /= (6 * area());
            cy /= (6 * area());
            x = Math.abs((int) cx);
            y = Math.abs((int) cy);
        }


        @Override
        public float getOriginX() {
            return x;
        }

        @Override
        public float getOriginY() {
            return y;
        }

        /**
         * This is a java port of the
         * W. Randolph Franklin algorithm explained here
         * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
         */
        @Override
        public boolean isInArea(float testx, float testy) {
            int i, j;
            boolean c = false;
            for (i = 0, j = points - 1; i < points; j = i++) {
                if (((ypoints.get(i) > testy) != (ypoints.get(j) > testy)) &&
                        (testx < (xpoints.get(j) - xpoints.get(i)) * (testy - ypoints.get(i)) / (ypoints.get(j) - ypoints.get(i)) + xpoints.get(i)))
                    c = !c;
            }
            return c;
        }

        @Override
        public String toString() {
            return "PolyArea{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", values=" + values +
                    ", xpoints=" + xpoints +
                    ", ypoints=" + ypoints +
                    ", x=" + x +
                    ", y=" + y +
                    ", points=" + points +
                    ", top=" + top +
                    ", bottom=" + bottom +
                    ", left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    /**
     * Circle Area
     */
    public static class CircleArea extends Area {
        float x;
        float y;
        float radius;

        CircleArea(int id, String name, float x, float y, float radius) {
            super(id, name);
            this.x = x;
            this.y = y;
            this.radius = radius;

        }

        public boolean isInArea(float x, float y) {
            float dx = this.x - x;
            float dy = this.y - y;

            // if tap is less than radius distance from the center
            float d = (float) Math.sqrt((dx * dx) + (dy * dy));
            return d < radius;
        }

        public float getOriginX() {
            return x;
        }

        public float getOriginY() {
            return y;
        }

        @Override
        public String toString() {
            return "CircleArea{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", values=" + values +
                    ", x=" + x +
                    ", y=" + y +
                    ", radius=" + radius +
                    '}';
        }
    }
    
    public interface OnAreaClickListener {
        void onAreaClicked(ImageMapView imv, Area area);
    }
}
