package com.example.wind.mycomic.custom;

import android.app.Activity;
import android.graphics.Matrix;
import android.os.Handler;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.transition.TransitionListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by wind on 2016/12/31.
 */

public class CustomDetailsOverviewSharedElementHelper extends SharedElementCallback {

    static final String TAG = "DetailsOverviewSharedElementHelper";
    static final boolean DEBUG = false;

    CustomDetailsOverviewRowPresenter.ViewHolder mViewHolder;
    Activity mActivityToRunTransition;
    boolean mStartedPostpone;
    String mSharedElementName;
    int mRightPanelWidth;
    int mRightPanelHeight;

    private ImageView.ScaleType mSavedScaleType;
    private Matrix mSavedMatrix;

    private boolean hasImageViewScaleChange(View snapshotView) {
        return snapshotView instanceof ImageView;
    }

    private void saveImageViewScale() {
        if (mSavedScaleType == null) {
            // only save first time after initialize/restoreImageViewScale()
            ImageView imageView = mViewHolder.mImageView;
            mSavedScaleType = imageView.getScaleType();
            mSavedMatrix = mSavedScaleType == ImageView.ScaleType.MATRIX ? imageView.getMatrix() : null;
        }
    }

    private static void updateImageViewAfterScaleTypeChange(ImageView imageView) {
        // enforcing imageView to update its internal bounds/matrix immediately
        imageView.measure(
                View.MeasureSpec.makeMeasureSpec(imageView.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(imageView.getMeasuredHeight(), View.MeasureSpec.EXACTLY));
        imageView.layout(imageView.getLeft(), imageView.getTop(),
                imageView.getRight(), imageView.getBottom());
    }

    private void changeImageViewScale(View snapshotView) {
        ImageView snapshotImageView = (ImageView) snapshotView;
        ImageView imageView = mViewHolder.mImageView;
        imageView.setScaleType(snapshotImageView.getScaleType());
        if (snapshotImageView.getScaleType() == ImageView.ScaleType.MATRIX) {
            imageView.setImageMatrix(snapshotImageView.getImageMatrix());
        }
        updateImageViewAfterScaleTypeChange(imageView);
    }

    private void restoreImageViewScale() {
        if (mSavedScaleType != null) {
            ImageView imageView = mViewHolder.mImageView;
            imageView.setScaleType(mSavedScaleType);
            if (mSavedScaleType == ImageView.ScaleType.MATRIX) {
                imageView.setImageMatrix(mSavedMatrix);
            }
            // only restore once unless another save happens
            mSavedScaleType = null;
            updateImageViewAfterScaleTypeChange(imageView);
        }
    }

    @Override
    public void onSharedElementStart(List<String> sharedElementNames,
                                     List<View> sharedElements, List<View> sharedElementSnapshots) {
        if (sharedElements.size() < 1) {
            return;
        }
        View overviewView = sharedElements.get(0);
        if (mViewHolder == null || mViewHolder.mOverviewFrame != overviewView) {
            return;
        }
        View snapshot = sharedElementSnapshots.get(0);
        if (hasImageViewScaleChange(snapshot)) {
            saveImageViewScale();
            changeImageViewScale(snapshot);
        }
        View imageView = mViewHolder.mImageView;
        final int width = overviewView.getWidth();
        final int height = overviewView.getHeight();
        imageView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        imageView.layout(0, 0, width, height);
        final View rightPanel = mViewHolder.mRightPanel;
        if (mRightPanelWidth != 0 && mRightPanelHeight != 0) {
            rightPanel.measure(View.MeasureSpec.makeMeasureSpec(mRightPanelWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(mRightPanelHeight, View.MeasureSpec.EXACTLY));
            rightPanel.layout(width, rightPanel.getTop(), width + mRightPanelWidth,
                    rightPanel.getTop() + mRightPanelHeight);
        } else {
            rightPanel.offsetLeftAndRight(width - rightPanel.getLeft());
        }
        mViewHolder.mActionsRow.setVisibility(View.INVISIBLE);
        mViewHolder.mDetailsDescriptionFrame.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSharedElementEnd(List<String> sharedElementNames,
                                   List<View> sharedElements, List<View> sharedElementSnapshots) {
        if (sharedElements.size() < 1) {
            return;
        }
        View overviewView = sharedElements.get(0);
        if (mViewHolder == null || mViewHolder.mOverviewFrame != overviewView) {
            return;
        }
        restoreImageViewScale();
        // temporary let action row take focus so we defer button background animation
        mViewHolder.mActionsRow.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        mViewHolder.mActionsRow.setVisibility(View.VISIBLE);
        mViewHolder.mActionsRow.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mViewHolder.mDetailsDescriptionFrame.setVisibility(View.VISIBLE);
    }

    void setSharedElementEnterTransition(Activity activity, String sharedElementName,
                                         long timeoutMs) {
        if (activity == null && !TextUtils.isEmpty(sharedElementName) ||
                activity != null && TextUtils.isEmpty(sharedElementName)) {
            throw new IllegalArgumentException();
        }
        if (activity == mActivityToRunTransition &&
                TextUtils.equals(sharedElementName, mSharedElementName)) {
            return;
        }
        if (mActivityToRunTransition != null) {
            ActivityCompat.setEnterSharedElementCallback(mActivityToRunTransition, null);
        }
        mActivityToRunTransition = activity;
        mSharedElementName = sharedElementName;
        ActivityCompat.setEnterSharedElementCallback(mActivityToRunTransition, this);
        ActivityCompat.postponeEnterTransition(mActivityToRunTransition);
        if (timeoutMs > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mStartedPostpone) {
                        return;
                    }
                    startPostponedEnterTransition();
                }
            }, timeoutMs);
        }
    }

    void onBindToDrawable(CustomDetailsOverviewRowPresenter.ViewHolder vh) {
        if (mViewHolder != null) {
            ViewCompat.setTransitionName(mViewHolder.mOverviewFrame, null);
        }
        // After we got a image drawable,  we can determine size of right panel.
        // We want right panel to have fixed size so that the right panel don't change size
        // when the overview is layout as a small bounds in transition.
        mViewHolder = vh;
        mViewHolder.mRightPanel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mViewHolder.mRightPanel.removeOnLayoutChangeListener(this);
                mRightPanelWidth = mViewHolder.mRightPanel.getWidth();
                mRightPanelHeight = mViewHolder.mRightPanel.getHeight();
            }
        });
        mViewHolder.mRightPanel.postOnAnimation(new Runnable() {
            @Override
            public void run() {
                ViewCompat.setTransitionName(mViewHolder.mOverviewFrame, mSharedElementName);
                Object transition = TransitionHelper.getSharedElementEnterTransition(
                        mActivityToRunTransition.getWindow());
                if (transition != null) {
                    TransitionHelper.addTransitionListener(transition, new TransitionListener() {
                        @Override
                        public void onTransitionEnd(Object transition) {
                            // after transition if the action row still focused, transfer
                            // focus to its children
                            if (mViewHolder.mActionsRow.isFocused()) {
                                mViewHolder.mActionsRow.requestFocus();
                            }
                            TransitionHelper.removeTransitionListener(transition, this);
                        }
                    });
                }
                startPostponedEnterTransition();
            }
        });
    }

    void startPostponedEnterTransition() {
        if (!mStartedPostpone) {
            ActivityCompat.startPostponedEnterTransition(mActivityToRunTransition);
            mStartedPostpone = true;
        }
    }
}

