package com.lingtuan.firefly.chat;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.adapter.ChatFaceAdapter;
import com.lingtuan.firefly.custom.viewpager.CirclePageIndicator;
import com.lingtuan.firefly.listener.FaceOnItemListener;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * To call the class definition of expression
 */
public class FaceUtils implements FaceOnItemListener, View.OnClickListener {

    private static FaceUtils instance;

    private Context mContext;

    private View mFaceView;

    private ViewPager mViewPager;

    private CirclePageIndicator indicator;

    private EditText mInputContent;


    private ImageView preSelected, groupOne;

    private String[] faceStringList;

    private FaceOnItemListener itemListener = null;


    private FaceUtils(Context mContext) {
        this.mContext = mContext;
        faceStringList = mContext.getResources().getStringArray(R.array.chat_smiley_list);
    }

    public static FaceUtils getInstance(Context mContext) {
        if (instance == null) {
            instance = new FaceUtils(mContext);
        }
        return instance;
    }
    public void showFaceView(View faceView, EditText mInputContent,View stubBottomBg) {
        this.mInputContent = mInputContent;
        if (mFaceView == null) {
            mFaceView = faceView;
            initView(faceView, this);
            mFaceView.setVisibility(View.VISIBLE);
            stubBottomBg.setVisibility(View.VISIBLE);
        } else {
            boolean visibleState = mFaceView.getVisibility() == View.VISIBLE;
            mFaceView.setVisibility(visibleState ? View.GONE : View.VISIBLE);
            stubBottomBg.setVisibility(visibleState ? View.GONE : View.VISIBLE);
        }
    }

    private void initView(View faceView, final FaceOnItemListener itemListener) {
        this.itemListener = itemListener;
        groupOne = (ImageView) faceView.findViewById(R.id.groupOne);
        groupOne.setBackgroundColor(mContext.getResources().getColor(R.color.btn_e8e8e8));
        preSelected = groupOne; //Enter the page for the first time and record the last click the view default is: the first group
        groupOne.setOnClickListener(this);

        mViewPager = (ViewPager) faceView.findViewById(R.id.include_face_viewpager);
        indicator = (CirclePageIndicator) faceView.findViewById(R.id.include_face_viewpager_indicator);
        Integer[] faceIds = Utils.getFaceListRes();
        bindFaceToGridView(5, false, faceIds, null, 18);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.groupOne: // The default expression
                resetFaceData(view, false);
                break;
            default:
                break;
        }
    }

    /**
     * Reset the expression data source
     * @param view : Click on the view of
     */

    String[] faceNameIds = null;

    private void resetFaceData(View view, final boolean isGif) {
        ImageView selectView = (ImageView) view;

        if (preSelected == selectView) {
            return;
        }

        selectView.setBackgroundColor(mContext.getResources().getColor(R.color.btn_e8e8e8));
        if (preSelected != null) {      // The last time the selected item state reduction
            preSelected.setBackgroundColor(mContext.getResources().getColor(R.color.btn_FFF7F7F7));
            preSelected = null;
        }
        preSelected = selectView; // Record the currently selected item

        int pageCount = 1; // The default expression is only one page
        final int pageItemCount = isGif ? 8 : 18; // According to each item quantity, the quantity including the delete icon icon

        Integer[] faceIds = Utils.getFaceListRes();
        int count = faceIds.length;
        if (count % pageItemCount == 0) {
            pageCount = count / pageItemCount;
        } else if (count > pageItemCount) {
            pageCount = (count / pageItemCount) + 1;
        }
        faceNameIds = null;
        bindFaceToGridView(pageCount, isGif, faceIds, faceNameIds, pageItemCount);
    }

    /**
     * @param pageCount     Total number of pages
     * @param isGif         Group is a GIF
     * @param faceIds       The item resource files array
     * @param pageItemCount Total number of items per page
     *                      Binding expression images to the corresponding item
     */
    private void bindFaceToGridView(int pageCount, final boolean isGif, Integer[] faceIds, String faceNameIds[], final int pageItemCount) {
        ArrayList<GridView> pageViews = new ArrayList<>();
        GridView gridView;
        for (int i = 0; i < pageCount; i++) {
            gridView = new GridView(mContext);
            gridView.setNumColumns(isGif ? 4 : 6); // GIF group four big expressions in a row, or buy into six small expression
            gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            gridView.setHorizontalSpacing(isGif ? 15 : 10);
            gridView.setVerticalSpacing(isGif ? 15 : 10);
            gridView.setGravity(Gravity.CENTER);
            gridView.setSelector(R.drawable.scrollview_item_bg);
            gridView.setPadding(0, 10, 0, 10);
            gridView.setAdapter(new ChatFaceAdapter(faceIds, faceNameIds, mContext, i, isGif));
            final int page = i;
            gridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (itemListener != null) {  // pageItemCount-1 In order to take off to delete the item
                        if (isGif) { // Click on the GIF need to send out directly
                            itemListener.onItemListener(page, (page * pageItemCount) + position, isGif, false);
                        } else {
                            itemListener.onItemListener(page, page * (pageItemCount - 1) + position, isGif, position == pageItemCount - 1);
                        }
                    }
                }
            });
            pageViews.add(gridView);
        }
        mViewPager.setAdapter(new ChatFacePagerAdapter(pageViews));
        indicator.setViewPager(mViewPager);
    }


    /**
     * Expression adapter
     */
    class ChatFacePagerAdapter extends PagerAdapter {

        private List<GridView> pageViews;

        public ChatFacePagerAdapter(List<GridView> pageViews) {
            this.pageViews = pageViews;
        }

        @Override
        public int getCount() {
            return pageViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        // To delete a page on a page
        @Override
        public void destroyItem(View arg0, int position, Object arg2) {
            ((ViewPager) arg0).removeView(pageViews.get(position % pageViews.size()));
        }

        // Loading a page
        @Override
        public Object instantiateItem(View arg0, int position) {
            try {
                ((ViewPager) arg0).addView(pageViews.get(position), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pageViews.get(position);
        }
    }

    @Override
    public void onItemListener(int page, int position, boolean isGif, boolean isDelete) {
        Editable mEditable = mInputContent.getText();
        int selectIndex = mInputContent.getSelectionStart();
        if (isDelete) {
            if (selectIndex == 0) {
                return;
            }
            //Delete the index of forward string is complete expression, if the string is directly delete the expression, or delete a single character
            String strSub = mEditable.toString().substring(0, selectIndex);
            boolean isFace = false;
            for (int i = 0; i < faceStringList.length; i++) {
                if (strSub.endsWith(faceStringList[i])) {
                    isFace = true;
                    mEditable.delete(selectIndex - faceStringList[i].length(), selectIndex);
                    break;
                }
            }
            if (!isFace) {
                mEditable.delete(selectIndex - 1, selectIndex);
            }
        } else {
            if (position >= faceStringList.length) {
                return;
            }
            CharSequence charSequence = NextApplication.mSmileyParser.addSmileySpans1(faceStringList[position]);
            mEditable.insert(selectIndex, charSequence);
        }
    }

    public void destory() {
        instance = null;
    }
}
