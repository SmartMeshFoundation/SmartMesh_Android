package com.lingtuan.firefly.custom.picker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WheelView extends View {
    public static final float LINE_SPACE_MULTIPLIER = 2.5F;
    public static final int TEXT_PADDING = -1;
    public static final int TEXT_SIZE = 16;//sp
    public static final int TEXT_COLOR_FOCUS = 0XFF0288CE;
    public static final int TEXT_COLOR_NORMAL = 0XFFBBBBBB;
    public static final int DIVIDER_COLOR = 0XFF83CDE6;
    public static final int DIVIDER_ALPHA = 220;
    public static final float DIVIDER_THICK = 2f;//px
    public static final int ITEM_OFF_SET = 3;
    private static final float ITEM_PADDING = 13f;//px,480X800 phone margin can not be too large
    private static final int ACTION_CLICK = 1;//Click
    private static final int ACTION_FLING = 2;//Fling
    private static final int ACTION_DRAG = 3;//Drag
    private static final int VELOCITY_FLING = 5;//Changing this value can change the taxi speed
    private static final float SCALE_CONTENT = 0.8F;//Non-intermediate text controls the height with this, flattening to create a 3D illusion

    private MessageHandler handler;
    private GestureDetector gestureDetector;
    private OnItemSelectListener onItemSelectListener;
    private OnWheelListener onWheelListener;
    private boolean onlyShowCenterLabel = true;//Whether the additional unit is only shown after the selected item
    private ScheduledFuture<?> mFuture;
    private Paint paintOuterText;//No options brush
    private Paint paintCenterText;//Selected items brush
    private Paint paintIndicator;//Dividing line brush
    private Paint paintShadow;//Shadow brush
    private List<WheelItem> items = new ArrayList<>();//All options
    private String label;//label
    private int maxTextWidth;//The largest text wide
    private int maxTextHeight;//The largest text high
    private int textSize = TEXT_SIZE;//Text size, in sp
    private float itemHeight;//Height of each line
    private Typeface typeface = Typeface.DEFAULT;//Font style
    private int textColorOuter = TEXT_COLOR_NORMAL;//Unselected text color
    private int textColorCenter = TEXT_COLOR_FOCUS;//Selected item text color
    private DividerConfig dividerConfig = new DividerConfig();
    private float lineSpaceMultiplier = LINE_SPACE_MULTIPLIER;//The distance between items can be used to set up and down spacing
    private int padding = TEXT_PADDING;//Left and right margins of text, in px
    private boolean isLoop = true;//Circulation scroll
    private float firstLineY;//The first line Y coordinate value
    private float secondLineY;//The second line Y coordinate
    private float totalScrollY = 0;//Scroll total height y value
    private int initPosition = -1;//Initialize the default selected items
    private int selectedIndex = -1;//The index of the selected item
    private int preCurrentIndex;
    private int visibleItemCount = ITEM_OFF_SET * 2 + 1;//Draw a few entries
    private int measuredHeight;//measured height
    private int measuredWidth;//measured width
    private int radius;//radius
    private int offset = 0;
    private float previousY = 0;
    private long startTime = 0;
    private int widthMeasureSpec;
    private int gravity = Gravity.CENTER;
    private int drawCenterContentStart = 0;//Middle selected text to start drawing position
    private int drawOutContentStart = 0;//Non-intermediate text starts drawing position
    private float centerContentOffset;//Offset
    private boolean useWeight = false;//Use the weight or the contents of the package？

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Screen density: 0.75,1.0,1.5,2.0,3.0, according to the density of different adaptation
        float density = getResources().getDisplayMetrics().density;
        if (density < 1) {
            centerContentOffset = 2.4F;
        } else if (1 <= density && density < 2) {
            centerContentOffset = 3.6F;
        } else if (1 <= density && density < 2) {
            centerContentOffset = 4.5F;
        } else if (2 <= density && density < 3) {
            centerContentOffset = 6.0F;
        } else if (density >= 3) {
            centerContentOffset = density * 2.5F;
        }
        judgeLineSpace();
        initView(context);
    }

    /**
     * Set the number of options displayed must be an odd number
     */
    public final void setVisibleItemCount(int count) {
        if (count % 2 == 0) {
            throw new IllegalArgumentException("must be odd");
        }
        if (count != visibleItemCount) {
            visibleItemCount = count;
        }
    }

    /**
     * Set whether to disable loop scrolling
     */
    public final void setCycleDisable(boolean cycleDisable) {
        isLoop = !cycleDisable;
    }

    /**
     * Set the number of wheel offset
     */
    public final void setOffset(@IntRange(from = 1, to = 5) int offset) {
        if (offset < 1 || offset > 5) {
            throw new IllegalArgumentException("must between 1 and 5");
        }
        int count = offset * 2 + 1;
        if (offset % 2 == 0) {
            count += offset;
        } else {
            count += offset - 1;
        }
        setVisibleItemCount(count);
    }

    public final int getSelectedIndex() {
        return selectedIndex;
    }

    public final void setSelectedIndex(int index) {
        if (items == null || items.isEmpty()) {
            return;
        }
        int size = items.size();
        if (index >= 0 && index < size && index != selectedIndex) {
            initPosition = index;
            totalScrollY = 0;//Return to the top, or reset the index, then the position will be offset, it will display the wrong position data
            offset = 0;
            invalidate();
        }
    }

    public final void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    /**
     * @deprecated use {@link #setOnItemSelectListener(OnItemSelectListener)} instead
     */
    @Deprecated
    public final void setOnWheelListener(OnWheelListener listener) {
        onWheelListener = listener;
    }


    public final void setItems(List<?> items) {
        this.items.clear();
        for (Object item : items) {
            if (item instanceof WheelItem) {
                this.items.add((WheelItem) item);
            } else if (item instanceof CharSequence || item instanceof Number) {
                this.items.add(new StringItem(item.toString()));
            } else {
                throw new IllegalArgumentException("please implements " + WheelItem.class.getName());
            }
        }
        remeasure();
        invalidate();
    }

    public final void setItems(List<?> items, int index) {
        setItems(items);
        setSelectedIndex(index);
    }

    public final void setItems(String[] list) {
        setItems(Arrays.asList(list));
    }

    public final void setItems(List<String> list, String item) {
        int index = list.indexOf(item);
        if (index == -1) {
            index = 0;
        }
        setItems(list, index);
    }

    public final void setItems(String[] list, int index) {
        setItems(Arrays.asList(list), index);
    }

    public final void setItems(String[] items, String item) {
        setItems(Arrays.asList(items), item);
    }

    /**
     * The unit string attached to the right
     */
    public final void setLabel(String label, boolean onlyShowCenterLabel) {
        this.label = label;
        this.onlyShowCenterLabel = onlyShowCenterLabel;
    }

    public final void setLabel(String label) {
        setLabel(label, true);
    }

    public final void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public void setTextColor(@ColorInt int colorNormal, @ColorInt int colorFocus) {
        this.textColorOuter = colorNormal;
        this.textColorCenter = colorFocus;
        paintOuterText.setColor(colorNormal);
        paintCenterText.setColor(colorFocus);
    }

    public void setTextColor(@ColorInt int color) {
        this.textColorOuter = color;
        this.textColorCenter = color;
        paintOuterText.setColor(color);
        paintCenterText.setColor(color);
    }

    public final void setTypeface(Typeface font) {
        typeface = font;
        paintOuterText.setTypeface(typeface);
        paintCenterText.setTypeface(typeface);
    }

    public final void setTextSize(float size) {
        if (size > 0.0F) {
            textSize = (int) (getContext().getResources().getDisplayMetrics().density * size);
            paintOuterText.setTextSize(textSize);
            paintCenterText.setTextSize(textSize);
        }
    }



    public void setDividerColor(@ColorInt int color) {
        dividerConfig.setColor(color);
        paintIndicator.setColor(color);
    }

    /**
     * @deprecated use {{@link #setDividerConfig(DividerConfig)} instead
     */
    @Deprecated
    public void setLineConfig(DividerConfig config) {
        setDividerConfig(config);
    }

    public void setDividerConfig(DividerConfig config) {
        if (null == config) {
            dividerConfig.setVisible(false);
            dividerConfig.setShadowVisible(false);
            return;
        }
        this.dividerConfig = config;
        paintIndicator.setColor(config.color);
        paintIndicator.setStrokeWidth(config.thick);
        paintIndicator.setAlpha(config.alpha);
        paintShadow.setColor(config.shadowColor);
        paintShadow.setAlpha(config.shadowAlpha);
    }

    public final void setLineSpaceMultiplier(@FloatRange(from = 2, to = 4) float multiplier) {
        lineSpaceMultiplier = multiplier;
        judgeLineSpace();
    }

    public void setPadding(int padding) {
        this.padding = Utils.dip2px(getContext(), padding);
    }

    public void setUseWeight(boolean useWeight) {
        this.useWeight = useWeight;
    }

    /**
     * Judge whether the spacing is within the effective range
     */
    private void judgeLineSpace() {
        if (lineSpaceMultiplier < 1.5f) {
            lineSpaceMultiplier = 1.5f;
        } else if (lineSpaceMultiplier > 4.0f) {
            lineSpaceMultiplier = 4.0f;
        }
    }

    private void initView(Context context) {
        handler = new MessageHandler(this);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                scrollBy(velocityY);
                return true;
            }
        });
        gestureDetector.setIsLongpressEnabled(false);
        initPaints();
    }

    private void initPaints() {
        paintOuterText = new Paint();
        paintOuterText.setAntiAlias(true);
        paintOuterText.setColor(textColorOuter);
        paintOuterText.setTypeface(typeface);
        paintOuterText.setTextSize(textSize);
        paintCenterText = new Paint();
        paintCenterText.setAntiAlias(true);
        paintCenterText.setColor(textColorCenter);
        paintCenterText.setTextScaleX(1.1F);
        paintCenterText.setTypeface(typeface);
        paintCenterText.setTextSize(textSize);
        paintIndicator = new Paint();
        paintIndicator.setAntiAlias(true);
        paintIndicator.setColor(dividerConfig.color);
        paintIndicator.setStrokeWidth(dividerConfig.thick);
        paintIndicator.setAlpha(dividerConfig.alpha);
        paintShadow = new Paint();
        paintShadow.setAntiAlias(true);
        paintShadow.setColor(dividerConfig.shadowColor);
        paintShadow.setAlpha(dividerConfig.shadowAlpha);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }


    /**
     * Measure again
     */
    private void remeasure() {
        if (items == null) {
            return;
        }
        measureTextWidthHeight();
        //Semicircular circumference
        int halfCircumference = (int) (itemHeight * (visibleItemCount - 1));
        //The circumference of the entire circle is divided by the PI to obtain the diameter, which is used as the overall height of the control
        measuredHeight = (int) ((halfCircumference * 2) / Math.PI);
        //Find the radius
        radius = (int) (halfCircumference / Math.PI);
        ViewGroup.LayoutParams params = getLayoutParams();
        //Control width
        if (useWeight) {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        } else if (params != null && params.width > 0) {
            measuredWidth = params.width;
        } else {
            measuredWidth = maxTextWidth;
            if (padding < 0) {
                padding = Utils.dip2px(getContext(), ITEM_PADDING);
            }
            measuredWidth += padding * 2;
            if (!TextUtils.isEmpty(label)) {
                measuredWidth += obtainTextWidth(paintCenterText, label);
            }
        }
        //Calculates the two horizontal lines and the baseline Y position of the selected item
        firstLineY = (measuredHeight - itemHeight) / 2.0F;
        secondLineY = (measuredHeight + itemHeight) / 2.0F;
        //Initialize the item's position
        if (initPosition == -1) {
            if (isLoop) {
                initPosition = (items.size() + 1) / 2;
            } else {
                initPosition = 0;
            }
        }
        preCurrentIndex = initPosition;
    }

    /**
     * Calculate the maximum length of the Text's width height
     */
    private void measureTextWidthHeight() {
        Rect rect = new Rect();
        for (int i = 0; i < items.size(); i++) {
            String s1 = obtainContentText(items.get(i));
            paintCenterText.getTextBounds(s1, 0, s1.length(), rect);
            int textWidth = rect.width();
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
            }
            paintCenterText.getTextBounds("Test", 0, 2, rect);
            maxTextHeight = rect.height() + 2;
        }
        itemHeight = lineSpaceMultiplier * maxTextHeight;
    }

    /**
     * Achieve smooth scrolling
     */
    private void smoothScroll(int actionType) {
        cancelFuture();
        if (actionType == ACTION_FLING || actionType == ACTION_DRAG) {
            offset = (int) ((totalScrollY % itemHeight + itemHeight) % itemHeight);
            if ((float) offset > itemHeight / 2.0F) {//If more than half of Item height, scroll to the next Item to go
                offset = (int) (itemHeight - (float) offset);
            } else {
                offset = -offset;
            }
        }
        //When stopped, the location of the offset, not all can correctly stop to the middle position, where the text to move back to the middle position
        SmoothScrollTimerTask command = new SmoothScrollTimerTask(this, offset);
        mFuture = Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(command, 0, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * Realization of rolling inertia
     */
    private void scrollBy(float velocityY) {
        cancelFuture();
        InertiaTimerTask command = new InertiaTimerTask(this, velocityY);
        mFuture = Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(command, 0, VELOCITY_FLING, TimeUnit.MILLISECONDS);
    }

    private void cancelFuture() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    private void itemSelectedCallback() {
        if (onItemSelectListener == null && onWheelListener == null) {
            return;
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (onItemSelectListener != null) {
                    onItemSelectListener.onSelected(selectedIndex);
                }
                if (onWheelListener != null) {
                    onWheelListener.onSelected(true, selectedIndex, items.get(selectedIndex).getName());
                }
            }
        }, 200L);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (items == null || items.size() == 0) {
            return;
        }
        //An array of visible items
        String[] visibleItemStrings = new String[visibleItemCount];
        //Rolling the Y value height to remove the height of each line, get the number of rolling items, the change number
        int change = (int) (totalScrollY / itemHeight);
        //The actual preselected item in the scroll (ie, the item passing through the middle position) = the position before sliding + the relative position sliding
        preCurrentIndex = initPosition + change % items.size();
        if (!isLoop) {//Do not cycle situation
            if (preCurrentIndex < 0) {
                preCurrentIndex = 0;
            }
            if (preCurrentIndex > items.size() - 1) {
                preCurrentIndex = items.size() - 1;
            }
        } else {//cycle
            if (preCurrentIndex < 0) {//For example: If the total is 5, preCurrentIndex = -1, then preCurrentIndex by loop, in fact, is above 0, which is 4 position
                preCurrentIndex = items.size() + preCurrentIndex;
            }
            if (preCurrentIndex > items.size() - 1) {//Similarly, above your own brain
                preCurrentIndex = preCurrentIndex - items.size();
            }
        }
        //With the fluency of the scroll, the total sliding distance and each item take the height, that is not a grid of rolling, each item does not necessarily roll into the corresponding Rect, the item corresponds to the lattice offset
        float itemHeightOffset = (totalScrollY % itemHeight);
        // Set the value of each element in the array
        int counter = 0;
        while (counter < visibleItemCount) {
            int index = preCurrentIndex - (visibleItemCount / 2 - counter);//Index value, that is, the middle of the current control item as the middle of the data source to calculate the relative source data source index value
            //To determine whether the cycle, if it is a circular data source also use the relative position of the loop to obtain the corresponding item value, if not the cycle is beyond the scope of the data source "blank string filled in the interface blank data item
            if (isLoop) {
                index = getLoopMappingIndex(index);
                visibleItemStrings[counter] = items.get(index).getName();
            } else if (index < 0) {
                visibleItemStrings[counter] = "";
            } else if (index > items.size() - 1) {
                visibleItemStrings[counter] = "";
            } else {
                visibleItemStrings[counter] = items.get(index).getName();
            }
            counter++;
        }
        //Draw the middle two horizontal lines
        if (dividerConfig.visible) {
            float ratio = dividerConfig.ratio;
            canvas.drawLine(measuredWidth * ratio, firstLineY, measuredWidth * (1 - ratio), firstLineY, paintIndicator);
            canvas.drawLine(measuredWidth * ratio, secondLineY, measuredWidth * (1 - ratio), secondLineY, paintIndicator);
        }
        if (dividerConfig.shadowVisible) {
            paintShadow.setColor(dividerConfig.shadowColor);
            paintShadow.setAlpha(dividerConfig.shadowAlpha);
            canvas.drawRect(0.0F, firstLineY, measuredWidth, secondLineY, paintShadow);
        }
        counter = 0;
        while (counter < visibleItemCount) {
            canvas.save();
            // Arc length L = itemHeight * counter - itemHeightOffset
            // Find the radian α = L / r (arc length / radius) [0, π]
            double radian = ((itemHeight * counter - itemHeightOffset)) / radius;
            // Radians converted to angles (turning the semicircle 90 degrees to the right with the Y axis as the axis, placing it in the first quadrant and the fourth quadrant
            // angle [-90°,90°]
            float angle = (float) (90D - (radian / Math.PI) * 180D);//The first item, starting at 90 degrees, gradually decreases to -90 degrees
            // There may be subtle deviations of the calculated values to ensure non-rendering from 90 ° to 90 ° negative
            if (angle >= 90F || angle <= -90F) {
                canvas.restore();
            } else {
                //Get the content text
                String contentText;
                //If the label is displayed in each mode, and item content is not empty, label is not empty
                String tempStr = obtainContentText(visibleItemStrings[counter]);
                if (!onlyShowCenterLabel && !TextUtils.isEmpty(label) && !TextUtils.isEmpty(tempStr)) {
                    contentText = tempStr + label;
                } else {
                    contentText = tempStr;
                }
                remeasureTextSize(contentText);
                //Calculate where to start drawing
                measuredCenterContentStart(contentText);
                measuredOutContentStart(contentText);
                float translateY = (float) (radius - Math.cos(radian) * radius - (Math.sin(radian) * maxTextHeight) / 2D);
                //According to Math.sin (radian) to change the origin of the canvas coordinate system, and then scale the canvas, making the text height to zoom, the formation of curved 3d visual difference
                canvas.translate(0.0F, translateY);
                canvas.scale(1.0F, (float) Math.sin(radian));
                if (translateY <= firstLineY && maxTextHeight + translateY >= firstLineY) {
                    // The entry goes through the first line
                    canvas.save();
                    canvas.clipRect(0, 0, measuredWidth, firstLineY - translateY);
                    canvas.scale(1.0F, (float) Math.sin(radian) * SCALE_CONTENT);
                    canvas.drawText(contentText, drawOutContentStart, maxTextHeight, paintOuterText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, firstLineY - translateY, measuredWidth, (int) (itemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * 1.0F);
                    canvas.drawText(contentText, drawCenterContentStart, maxTextHeight - centerContentOffset, paintCenterText);
                    canvas.restore();
                } else if (translateY <= secondLineY && maxTextHeight + translateY >= secondLineY) {
                    // The entry goes through the second line
                    canvas.save();
                    canvas.clipRect(0, 0, measuredWidth, secondLineY - translateY);
                    canvas.scale(1.0F, (float) Math.sin(radian) * 1.0F);
                    canvas.drawText(contentText, drawCenterContentStart, maxTextHeight - centerContentOffset, paintCenterText);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, secondLineY - translateY, measuredWidth, (int) (itemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * SCALE_CONTENT);
                    canvas.drawText(contentText, drawOutContentStart, maxTextHeight, paintOuterText);
                    canvas.restore();
                } else if (translateY >= firstLineY && maxTextHeight + translateY <= secondLineY) {
                    // Middle entry
                    canvas.clipRect(0, 0, measuredWidth, maxTextHeight);
                    //Center text
                    float Y = maxTextHeight - centerContentOffset;//Because the arc angle conversion down value, resulting in a slight deviation angle, plus the baseline brush will be offset, so the need to offset correction
                    int i = 0;
                    for (WheelItem item : items) {
                        if (item.getName().equals(tempStr)) {
                            selectedIndex = i;
                            break;
                        }
                        i++;
                    }
                    if (onlyShowCenterLabel && !TextUtils.isEmpty(label)) {
                        contentText += label;
                    }
                    canvas.drawText(contentText, drawCenterContentStart, Y, paintCenterText);
                } else {
                    // Other entries
                    canvas.save();
                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * SCALE_CONTENT);
                    canvas.drawText(contentText, drawOutContentStart, maxTextHeight, paintOuterText);
                    canvas.restore();
                }
                canvas.restore();
                paintCenterText.setTextSize(textSize);
            }
            counter++;
        }
    }

    /**
     * According to the length of the text reset the size of the text so that it can be fully displayed
     */
    private void remeasureTextSize(String contentText) {
        Rect rect = new Rect();
        paintCenterText.getTextBounds(contentText, 0, contentText.length(), rect);
        int width = rect.width();
        int size = textSize;
        while (width > measuredWidth) {
            size--;
            //Set the size of the text in the middle of 2 horizontal lines
            paintCenterText.setTextSize(size);
            paintCenterText.getTextBounds(contentText, 0, contentText.length(), rect);
            width = rect.width();
        }
        //Set the text size of 2 horizontal lines
        paintOuterText.setTextSize(size);
    }


    /**
     * Recursively calculate the corresponding index
     */
    private int getLoopMappingIndex(int index) {
        if (index < 0) {
            index = index + items.size();
            index = getLoopMappingIndex(index);
        } else if (index > items.size() - 1) {
            index = index - items.size();
            index = getLoopMappingIndex(index);
        }
        return index;
    }

    /**
     * According to the incoming object to get the value you want to display
   *
   * @param item The item of data source
   * @return corresponds to the string displayed
     */
    private String obtainContentText(Object item) {
        if (item == null) {
            return "";
        } else if (item instanceof WheelItem) {
            return ((WheelItem) item).getName();
        } else if (item instanceof Integer) {
            //If shaping is a minimum of two digits.
            return String.format(Locale.getDefault(), "%02d", (int) item);
        }
        return item.toString();
    }

    private void measuredCenterContentStart(String content) {
        Rect rect = new Rect();
        paintCenterText.getTextBounds(content, 0, content.length(), rect);
        switch (gravity) {
            case Gravity.CENTER://The display is centered
                drawCenterContentStart = (int) ((measuredWidth - rect.width()) * 0.5);
                break;
            case Gravity.LEFT:
                drawCenterContentStart = 0;
                break;
            case Gravity.RIGHT://Add offset
                drawCenterContentStart = measuredWidth - rect.width() - (int) centerContentOffset;
                break;
        }
    }

    private void measuredOutContentStart(String content) {
        Rect rect = new Rect();
        paintOuterText.getTextBounds(content, 0, content.length(), rect);
        switch (gravity) {
            case Gravity.CENTER:
                drawOutContentStart = (int) ((measuredWidth - rect.width()) * 0.5);
                break;
            case Gravity.LEFT:
                drawOutContentStart = 0;
                break;
            case Gravity.RIGHT:
                drawOutContentStart = measuredWidth - rect.width() - (int) centerContentOffset;
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.widthMeasureSpec = widthMeasureSpec;
        remeasure();
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean eventConsumed = gestureDetector.onTouchEvent(event);
        ViewParent parent = getParent();
        switch (event.getAction()) {
            //down
            case MotionEvent.ACTION_DOWN:
                startTime = System.currentTimeMillis();
                cancelFuture();
                previousY = event.getRawY();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                break;
            //move
            case MotionEvent.ACTION_MOVE:
                float dy = previousY - event.getRawY();
                previousY = event.getRawY();
                totalScrollY = totalScrollY + dy;
                // is loop。
                if (!isLoop) {
                    float top = -initPosition * itemHeight;
                    float bottom = (items.size() - 1 - initPosition) * itemHeight;
                    if (totalScrollY - itemHeight * 0.25 < top) {
                        top = totalScrollY - dy;
                    } else if (totalScrollY + itemHeight * 0.25 > bottom) {
                        bottom = totalScrollY - dy;
                    }
                    if (totalScrollY < top) {
                        totalScrollY = (int) top;
                    } else if (totalScrollY > bottom) {
                        totalScrollY = (int) bottom;
                    }
                }
                break;
            //Complete the slide, fingers left the screen
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                if (!eventConsumed) {//Did not consume the incident
                    /*
                     * About arc length calculation
                     */
                    float y = event.getY();
                    double L = Math.acos((radius - y) / radius) * radius;
                    //item0 Half is invisible, so itemHeight / 2 needs to be added
                    int circlePosition = (int) ((L + itemHeight / 2) / itemHeight);
                    float extraOffset = (totalScrollY % itemHeight + itemHeight) % itemHeight;
                    //Sliding arc length value
                    offset = (int) ((circlePosition - visibleItemCount / 2) * itemHeight - extraOffset);
                    if ((System.currentTimeMillis() - startTime) > 120) {
                        // Handle the drag event
                        smoothScroll(ACTION_DRAG);
                    } else {
                        // Process entry click event
                        smoothScroll(ACTION_CLICK);
                    }
                }
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                break;
        }
        invalidate();
        return true;
    }

    /**
     * Get the number of options
     */
    protected int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private int obtainTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    /**
     * Selected items of the dividing line
     */
    public static class DividerConfig {
        public static final float FILL = 0f;
        public static final float WRAP = 1f;
        protected boolean visible = true;
        protected boolean shadowVisible = false;
        protected int color = DIVIDER_COLOR;
        protected int shadowColor = TEXT_COLOR_NORMAL;
        protected int shadowAlpha = 100;
        protected int alpha = DIVIDER_ALPHA;
        protected float ratio = 0.1f;
        protected float thick = DIVIDER_THICK;

        public DividerConfig() {
            super();
        }

        public DividerConfig(@FloatRange(from = 0, to = 1) float ratio) {
            this.ratio = ratio;
        }

        /**
         * Is the line visible?
         */
        public DividerConfig setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        /**
         * Is the shadow visible?
         */
        public DividerConfig setShadowVisible(boolean shadowVisible) {
            this.shadowVisible = shadowVisible;
            if (shadowVisible && color == DIVIDER_COLOR) {
                color = shadowColor;
                alpha = 255;
            }
            return this;
        }

        /**
         * Shadow color
         */
        public DividerConfig setShadowColor(@ColorInt int color) {
            shadowVisible = true;
            shadowColor = color;
            return this;
        }

        /**
         * Shadow transparency
         */
        public DividerConfig setShadowAlpha(@IntRange(from = 1, to = 255) int alpha) {
            this.shadowAlpha = alpha;
            return this;
        }

        /**
         * Line color
         */
        public DividerConfig setColor(@ColorInt int color) {
            this.color = color;
            return this;
        }

        /**
         * Line transparency
         */
        public DividerConfig setAlpha(@IntRange(from = 1, to = 255) int alpha) {
            this.alpha = alpha;
            return this;
        }

        /**
         * Line ratio, the range is 0-1, 0 means the longest, 1 means the shortest
         */
        public DividerConfig setRatio(@FloatRange(from = 0, to = 1) float ratio) {
            this.ratio = ratio;
            return this;
        }

        /**
         * Line thick
         */
        public DividerConfig setThick(float thick) {
            this.thick = thick;
            return this;
        }

        @Override
        public String toString() {
            return "visible=" + visible + ",color=" + color + ",alpha=" + alpha + ",thick=" + thick;
        }

    }

    /**
     * @deprecated Use {@link #DividerConfig} instead
     */
    @Deprecated
    public static class LineConfig extends DividerConfig {
    }

    /**
     * Pure string entry for compatibility with older versions
     */
    private static class StringItem implements WheelItem {
        private String name;

        private StringItem(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    public interface OnItemSelectListener {
        /**
         * Swipe to select the callback
     *
         * @param index The index of the current selection    
         */
        void onSelected(int index);

    }

    /**
     * Compatible with legacy APIs
     *
     * @deprecated use {@link OnItemSelectListener} instead
     */
    @Deprecated
    public interface OnWheelListener {

        void onSelected(boolean isUserScroll, int index, String item);

    }

    /**
     * @deprecated use {@link OnItemSelectListener} instead
     */
    @Deprecated
    public interface OnWheelViewListener extends OnWheelListener {
    }

    private static class MessageHandler extends Handler {
        static final int WHAT_INVALIDATE = 1000;
        static final int WHAT_SMOOTH_SCROLL = 2000;
        static final int WHAT_ITEM_SELECTED = 3000;
        final WheelView view;

        MessageHandler(WheelView view) {
            this.view = view;
        }

        @Override
        public final void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_INVALIDATE:
                    view.invalidate();
                    break;
                case WHAT_SMOOTH_SCROLL:
                    view.smoothScroll(WheelView.ACTION_FLING);
                    break;
                case WHAT_ITEM_SELECTED:
                    view.itemSelectedCallback();
                    break;
            }
        }

    }

    private static class SmoothScrollTimerTask extends TimerTask {
        int realTotalOffset = Integer.MAX_VALUE;
        int realOffset = 0;
        int offset;
        final WheelView view;

        SmoothScrollTimerTask(WheelView view, int offset) {
            this.view = view;
            this.offset = offset;
        }

        @Override
        public void run() {
            if (realTotalOffset == Integer.MAX_VALUE) {
                realTotalOffset = offset;
            }
            //Subdivide the area to be scrolled into 10 small pieces, redraw it in 10 small units
            realOffset = (int) ((float) realTotalOffset * 0.1F);
            if (realOffset == 0) {
                if (realTotalOffset < 0) {
                    realOffset = -1;
                } else {
                    realOffset = 1;
                }
            }
            if (Math.abs(realTotalOffset) <= 1) {
                view.cancelFuture();
                view.handler.sendEmptyMessage(MessageHandler.WHAT_ITEM_SELECTED);
            } else {
                view.totalScrollY = view.totalScrollY + realOffset;
                //If not here, the cycle mode, then click the blank position need to roll back, otherwise there will be selected -1 item situation
                if (!view.isLoop) {
                    float itemHeight = view.itemHeight;
                    float top = (float) (-view.initPosition) * itemHeight;
                    float bottom = (float) (view.getItemCount() - 1 - view.initPosition) * itemHeight;
                    if (view.totalScrollY <= top || view.totalScrollY >= bottom) {
                        view.totalScrollY = view.totalScrollY - realOffset;
                        view.cancelFuture();
                        view.handler.sendEmptyMessage(MessageHandler.WHAT_ITEM_SELECTED);
                        return;
                    }
                }
                view.handler.sendEmptyMessage(MessageHandler.WHAT_INVALIDATE);
                realTotalOffset = realTotalOffset - realOffset;
            }
        }
    }

    private static class InertiaTimerTask extends TimerTask {
        float a = Integer.MAX_VALUE;
        final float velocityY;
        final WheelView view;

        InertiaTimerTask(WheelView view, float velocityY) {
            this.view = view;
            this.velocityY = velocityY;
        }

        @Override
        public final void run() {
            if (a == Integer.MAX_VALUE) {
                if (Math.abs(velocityY) > 2000F) {
                    if (velocityY > 0.0F) {
                        a = 2000F;
                    } else {
                        a = -2000F;
                    }
                } else {
                    a = velocityY;
                }
            }
            if (Math.abs(a) >= 0.0F && Math.abs(a) <= 20F) {
                view.cancelFuture();
                view.handler.sendEmptyMessage(MessageHandler.WHAT_SMOOTH_SCROLL);
                return;
            }
            int i = (int) ((a * 10F) / 1000F);
            view.totalScrollY = view.totalScrollY - i;
            if (!view.isLoop) {
                float itemHeight = view.itemHeight;
                float top = (-view.initPosition) * itemHeight;
                float bottom = (view.getItemCount() - 1 - view.initPosition) * itemHeight;
                if (view.totalScrollY - itemHeight * 0.25 < top) {
                    top = view.totalScrollY + i;
                } else if (view.totalScrollY + itemHeight * 0.25 > bottom) {
                    bottom = view.totalScrollY + i;
                }
                if (view.totalScrollY <= top) {
                    a = 40F;
                    view.totalScrollY = (int) top;
                } else if (view.totalScrollY >= bottom) {
                    view.totalScrollY = (int) bottom;
                    a = -40F;
                }
            }
            if (a < 0.0F) {
                a = a + 20F;
            } else {
                a = a - 20F;
            }
            view.handler.sendEmptyMessage(MessageHandler.WHAT_INVALIDATE);
        }

    }

}