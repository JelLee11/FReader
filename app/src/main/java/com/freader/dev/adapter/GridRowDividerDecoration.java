package com.freader.dev.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.R; // for Material3 attributes

public class GridRowDividerDecoration extends RecyclerView.ItemDecoration {

  private final Paint paint;
  private final int dividerHeight;
  private final int spanCount;
  private final int spacingBelowDivider;
  private final int itemSpacing;

  public GridRowDividerDecoration(
      Context context,
      int spanCount,
      int dividerHeightPx,
      int spacingBelowDividerPx,
      int itemSpacingPx) {
    this.spanCount = spanCount;
    this.dividerHeight = dividerHeightPx;
    this.spacingBelowDivider = spacingBelowDividerPx;
    this.itemSpacing = itemSpacingPx;

    this.paint = new Paint();
    // this.paint.setColor(resolveAttrColor(context, R.attr.woodDividerColor));
    this.paint.setColor(0xFF8a5e2c);
    this.paint.setStyle(Paint.Style.FILL);
  }

  private int resolveAttrColor(Context context, int attrRes) {
    TypedValue typedValue = new TypedValue();
    context.getTheme().resolveAttribute(attrRes, typedValue, true);
    return typedValue.data;
  }

  @Override
  public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
    int childCount = parent.getChildCount();

    for (int i = 0; i < childCount; i += spanCount) {
      View child = parent.getChildAt(i);
      if (child == null) continue;

      RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

      int left = parent.getPaddingLeft();
      int right = parent.getWidth() - parent.getPaddingRight();
      int top = child.getBottom() + params.bottomMargin;
      int bottom = top + dividerHeight;

      canvas.drawRect(left, top, right, bottom, paint);
    }
  }

  @Override
  public void getItemOffsets(
      Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view);
    int column = position % spanCount;

    // Calculate even left/right spacing for centering items
    outRect.left = itemSpacing * (spanCount - column) / spanCount;
    outRect.right = itemSpacing * (column + 1) / spanCount;

    // Add top space for divider and spacing (on all rows)
    outRect.top = dividerHeight + spacingBelowDivider;
    outRect.bottom = 0;
  }
}
