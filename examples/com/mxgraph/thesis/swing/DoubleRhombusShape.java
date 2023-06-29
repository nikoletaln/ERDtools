//Nikoleta Lavdaria

package com.mxgraph.thesis.swing;

import java.awt.Rectangle;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxRhombusShape;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

public class DoubleRhombusShape extends mxRhombusShape
{
	/**
	 * 
	 */
	public void paintShape(mxGraphics2DCanvas canvas, mxCellState state)
	{
		super.paintShape(canvas, state);

		int inset = (int) Math.round((mxUtils.getFloat(state.getStyle(),
				mxConstants.STYLE_STROKEWIDTH, 1) + 4)
				* canvas.getScale());

		Rectangle rect = state.getRectangle();
		int x = rect.x + inset;
		int y = rect.y + inset;
		int w = rect.width - 2 * inset;
		int h = rect.height - 2 * inset;
		int halfWidth = w / 2;
		int halfHeight = h / 2;

		int[] xPoints = {x + halfWidth, x + w,x + halfWidth,x};
        int[] yPoints= {y,y + halfHeight,  y + h,  y + halfHeight};
		canvas.getGraphics().drawPolygon(  xPoints,  yPoints, xPoints.length);
      
	}
}