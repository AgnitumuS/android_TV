package com.example.wind.mycomic.custom;

import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.wind.mycomic.R;

/**
 * Created by wind on 2017/1/7.
 */

public class DetailsOverviewLogoPresenter extends Presenter {

    /**
     * ViewHolder for Logo view of CustomDetailsOverviewRow.
     */
    public static class ViewHolder extends Presenter.ViewHolder {

        protected FullWidthDetailsOverviewRowPresenter mParentPresenter;
        protected FullWidthDetailsOverviewRowPresenter.ViewHolder mParentViewHolder;
        private boolean mSizeFromDrawableIntrinsic;

        public ViewHolder(View view) {
            super(view);
        }

        public FullWidthDetailsOverviewRowPresenter getParentPresenter() {
            return mParentPresenter;
        }

        public FullWidthDetailsOverviewRowPresenter.ViewHolder getParentViewHolder() {
            return mParentViewHolder;
        }

        /**
         * @return True if layout size of ImageView should be changed to intrinsic size of Drawable,
         *         false otherwise. Used by
         *         {@link DetailsOverviewLogoPresenter#onBindViewHolder(Presenter.ViewHolder, Object)}
         *         .
         *
         * @see DetailsOverviewLogoPresenter#onCreateView(ViewGroup)
         * @see DetailsOverviewLogoPresenter#onBindViewHolder(Presenter.ViewHolder, Object)
         */
        public boolean isSizeFromDrawableIntrinsic() {
            return mSizeFromDrawableIntrinsic;
        }

        /**
         * Change if the ImageView layout size should be synchronized to Drawable intrinsic size.
         * Used by
         * {@link DetailsOverviewLogoPresenter#onBindViewHolder(Presenter.ViewHolder, Object)}.
         *
         * @param sizeFromDrawableIntrinsic True if layout size of ImageView should be changed to
         *        intrinsic size of Drawable, false otherwise.
         *
         * @see DetailsOverviewLogoPresenter#onCreateView(ViewGroup)
         * @see DetailsOverviewLogoPresenter#onBindViewHolder(Presenter.ViewHolder, Object)
         */
        public void setSizeFromDrawableIntrinsic(boolean sizeFromDrawableIntrinsic) {
            mSizeFromDrawableIntrinsic = sizeFromDrawableIntrinsic;
        }
    }

    /**
     * Create a View for the Logo, default implementation loads from
     * {@link R.layout#lb_fullwidth_details_overview_logo}. Subclass may override this method to use
     * a fixed layout size and change ImageView scaleType. If the layout params is WRAP_CONTENT for
     * both width and size, the ViewHolder would be using intrinsic size of Drawable in
     * {@link #onBindViewHolder(Presenter.ViewHolder, Object)}.
     *
     * @param parent Parent view.
     * @return View created for the logo.
     */
    public View onCreateView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lb_fullwidth_details_overview_logo, parent, false);
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = onCreateView(parent);
        ViewHolder vh = new ViewHolder(view);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        vh.setSizeFromDrawableIntrinsic(lp.width == ViewGroup.LayoutParams.WRAP_CONTENT &&
                lp.width == ViewGroup.LayoutParams.WRAP_CONTENT);
        return vh;
    }

    /**
     * Called from {@link FullWidthDetailsOverviewRowPresenter} to setup FullWidthDetailsOverviewRowPresenter
     * and FullWidthDetailsOverviewRowPresenter.ViewHolder that hosts the logo.
     * @param viewHolder
     * @param parentViewHolder
     * @param parentPresenter
     */
    public void setContext(ViewHolder viewHolder,
                           FullWidthDetailsOverviewRowPresenter.ViewHolder parentViewHolder,
                           FullWidthDetailsOverviewRowPresenter parentPresenter) {
        viewHolder.mParentViewHolder = parentViewHolder;
        viewHolder.mParentPresenter = parentPresenter;
    }

    /**
     * Returns true if the logo view is bound to image. Subclass may override. The default
     * implementation returns true when {@link CustomDetailsOverviewRow#getImageDrawable()} is not null.
     * If subclass of DetailsOverviewLogoPresenter manages its own image drawable, it should
     * override this function to report status correctly and invoke
     * {@link FullWidthDetailsOverviewRowPresenter#notifyOnBindLogo(FullWidthDetailsOverviewRowPresenter.ViewHolder)}
     * when image view is bound to the drawable.
     */
    public boolean isBoundToImage(ViewHolder viewHolder, CustomDetailsOverviewRow row) {
        return row != null && row.getImageDrawable() != null;
    }

    /**
     * Bind logo View to drawable of CustomDetailsOverviewRow and call notifyOnBindLogo().  The
     * default implementation assumes the Logo View is an ImageView and change layout size to
     * intrinsic size of ImageDrawable if {@link ViewHolder#isSizeFromDrawableIntrinsic()} is true.
     * @param viewHolder ViewHolder to bind.
     * @param item CustomDetailsOverviewRow object to bind.
     */
    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        CustomDetailsOverviewRow row = (CustomDetailsOverviewRow) item;
        ImageView imageView = ((ImageView) viewHolder.view);
        imageView.setImageDrawable(row.getImageDrawable());
        if (isBoundToImage((ViewHolder) viewHolder, row)) {
            ViewHolder vh = (ViewHolder) viewHolder;
            if (vh.isSizeFromDrawableIntrinsic()) {
                ViewGroup.LayoutParams lp = imageView.getLayoutParams();
                lp.width = row.getImageDrawable().getIntrinsicWidth();
                lp.height = row.getImageDrawable().getIntrinsicHeight();
                if (imageView.getMaxWidth() > 0 || imageView.getMaxHeight() > 0) {
                    float maxScaleWidth = 1f;
                    if (imageView.getMaxWidth() > 0) {
                        if (lp.width > imageView.getMaxWidth()) {
                            maxScaleWidth = imageView.getMaxWidth() / (float) lp.width;
                        }
                    }
                    float maxScaleHeight = 1f;
                    if (imageView.getMaxHeight() > 0) {
                        if (lp.height > imageView.getMaxHeight()) {
                            maxScaleHeight = imageView.getMaxHeight() / (float) lp.height;
                        }
                    }
                    float scale = Math.min(maxScaleWidth, maxScaleHeight);
                    lp.width = (int) (lp.width * scale);
                    lp.height = (int) (lp.height * scale);
                }
                imageView.setLayoutParams(lp);
            }
            vh.mParentPresenter.notifyOnBindLogo(vh.mParentViewHolder);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

}

