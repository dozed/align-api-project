/*
 * $Id: AlignFormLayoutFactory.java 1311 2010-03-07 22:51:10Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2008
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.plugin.neontk;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.swt.SWT;

public class AlignFormLayoutFactory {

  public static final int DEFAULT_CLEAR_MARGIN = 10;
  public static final int CONTROL_HORIZONTAL_INDENT = 3;
  public static final int FORM_BODY_MARGIN_TOP = 12;
  public static final int FORM_BODY_MARGIN_BOTTOM = 12;
  public static final int FORM_BODY_MARGIN_LEFT = 6;
  public static final int FORM_BODY_MARGIN_RIGHT = 6;
    public static final int FORM_BODY_HORIZONTAL_SPACING = 8;
  public static final int FORM_BODY_VERTICAL_SPACING = 5;
  public static final int FORM_BODY_MARGIN_HEIGHT = 0;
  public static final int FORM_BODY_MARGIN_WIDTH = 0;
  public static final int SECTION_CLIENT_MARGIN_TOP = 10;
  public static final int SECTION_CLIENT_MARGIN_BOTTOM = 10;
  public static final int SECTION_CLIENT_MARGIN_LEFT = 6;
  public static final int SECTION_CLIENT_MARGIN_RIGHT = 6;
  public static final int SECTION_CLIENT_HORIZONTAL_SPACING = 8;
  public static final int SECTION_CLIENT_VERTICAL_SPACING = 8;
  public static final int SECTION_CLIENT_MARGIN_HEIGHT = 0;
  public static final int SECTION_CLIENT_MARGIN_WIDTH = 0;
  public static final int SECTION_HEADER_VERTICAL_SPACING = 6;
  public static final int CLEAR_MARGIN_TOP = DEFAULT_CLEAR_MARGIN;
  public static final int CLEAR_MARGIN_BOTTOM = DEFAULT_CLEAR_MARGIN;
  public static final int CLEAR_MARGIN_LEFT = DEFAULT_CLEAR_MARGIN;
  public static final int CLEAR_MARGIN_RIGHT = DEFAULT_CLEAR_MARGIN;
  public static final int CLEAR_HORIZONTAL_SPACING = 8;
  public static final int CLEAR_VERTICAL_SPACING = 8;
  public static final int CLEAR_MARGIN_HEIGHT = 0;
  public static final int CLEAR_MARGIN_WIDTH = 0;
  public static final int FORM_PANE_MARGIN_TOP = 0;
  public static final int FORM_PANE_MARGIN_BOTTOM = 0;
  public static final int FORM_PANE_MARGIN_LEFT = 0;
  public static final int FORM_PANE_MARGIN_RIGHT = 0;
  public static final int FORM_PANE_HORIZONTAL_SPACING = FORM_BODY_HORIZONTAL_SPACING;
  public static final int FORM_PANE_VERTICAL_SPACING = FORM_BODY_VERTICAL_SPACING;
  public static final int FORM_PANE_MARGIN_HEIGHT = 0;
  public static final int FORM_PANE_MARGIN_WIDTH = 0;

  private AlignFormLayoutFactory() {
    // empty implementation
  }
  
  //public static FillLayout createFormFillLayout(  ) {
	//  FillLayout layout = new FillLayout();
	//  layout.type = SWT.VERTICAL;
	//  return layout;
  //}
  
  public static void configureLayout(Control c, FormAttachment left,
			FormAttachment top, FormAttachment right, FormAttachment bottom) {
		FormData fd = new FormData();
		if (left != null) {
			fd.left = left;
		}
		if (top != null) {
			fd.top = top;
		}
		if (right != null) {
			fd.right = right;
		}
		if (bottom != null) {
			fd.bottom = bottom;
		}
		c.setLayoutData(fd);
	}

  public static GridLayout createFormGridLayout( final boolean makeColumnsEqualWidth,
                                                 final int numColumns ) {
    GridLayout layout = new GridLayout();
    layout.marginHeight = FORM_BODY_MARGIN_HEIGHT;
    layout.marginWidth = FORM_BODY_MARGIN_WIDTH;
    layout.marginTop = FORM_BODY_MARGIN_TOP;
    layout.marginBottom = FORM_BODY_MARGIN_BOTTOM;
    layout.marginLeft = FORM_BODY_MARGIN_LEFT;
    layout.marginRight = FORM_BODY_MARGIN_RIGHT;
    layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;
    return layout;
  }

  public static GridLayout createClearGridLayout( final boolean makeColumnsEqualWidth,
                                                  final int numColumns ) {
    GridLayout layout = new GridLayout();
    layout.marginHeight = CLEAR_MARGIN_HEIGHT;
    layout.marginWidth = CLEAR_MARGIN_WIDTH;
    layout.marginTop = CLEAR_MARGIN_TOP;
    layout.marginBottom = CLEAR_MARGIN_BOTTOM;
    layout.marginLeft = CLEAR_MARGIN_LEFT;
    layout.marginRight = CLEAR_MARGIN_RIGHT;
    layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
    layout.verticalSpacing = CLEAR_VERTICAL_SPACING;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;
    return layout;
  }

  public static TableWrapLayout createFormTableWrapLayout( final boolean makeColumnsEqualWidth,
                                                           final int numColumns ) {
    TableWrapLayout layout = new TableWrapLayout();
    layout.topMargin = FORM_BODY_MARGIN_TOP;
    layout.bottomMargin = FORM_BODY_MARGIN_BOTTOM;
    layout.leftMargin = FORM_BODY_MARGIN_LEFT;
    layout.rightMargin = FORM_BODY_MARGIN_RIGHT;
    layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;
    return layout;
  }

  public static TableWrapLayout createFormPaneTableWrapLayout( final boolean makeColumnsEqualWidth,
                                                               final int numColumns ) {
    TableWrapLayout layout = new TableWrapLayout();
    layout.topMargin = FORM_PANE_MARGIN_TOP;
    layout.bottomMargin = FORM_PANE_MARGIN_BOTTOM;
    layout.leftMargin = FORM_PANE_MARGIN_LEFT;
    layout.rightMargin = FORM_PANE_MARGIN_RIGHT;
    layout.horizontalSpacing = FORM_PANE_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_PANE_VERTICAL_SPACING;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;
    return layout;
  }

  public static GridLayout createFormPaneGridLayout( final boolean makeColumnsEqualWidth,
                                                     final int numColumns ) {
    GridLayout layout = new GridLayout();
    layout.marginHeight = FORM_PANE_MARGIN_HEIGHT;
    layout.marginWidth = FORM_PANE_MARGIN_WIDTH;
    layout.marginTop = FORM_PANE_MARGIN_TOP;
    layout.marginBottom = FORM_PANE_MARGIN_BOTTOM;
    layout.marginLeft = FORM_PANE_MARGIN_LEFT;
    layout.marginRight = FORM_PANE_MARGIN_RIGHT;
    layout.horizontalSpacing = FORM_PANE_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_PANE_VERTICAL_SPACING;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;
    return layout;
  }

  public static TableWrapLayout createClearTableWrapLayout( final boolean makeColumnsEqualWidth,
                                                            final int numColumns ) {
    TableWrapLayout layout = new TableWrapLayout();
    layout.topMargin = CLEAR_MARGIN_TOP;
    layout.bottomMargin = CLEAR_MARGIN_BOTTOM;
    layout.leftMargin = CLEAR_MARGIN_LEFT;
    layout.rightMargin = CLEAR_MARGIN_RIGHT;
    layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
    layout.verticalSpacing = CLEAR_VERTICAL_SPACING;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;
    return layout;
  }

  public static GridLayout createSectionClientGridLayout( final boolean makeColumnsEqualWidth,
                                                          final int numColumns ) {
    GridLayout layout = new GridLayout();
    layout.marginHeight = SECTION_CLIENT_MARGIN_HEIGHT;
    layout.marginWidth = SECTION_CLIENT_MARGIN_WIDTH;
    layout.marginTop = SECTION_CLIENT_MARGIN_TOP;
    layout.marginBottom = SECTION_CLIENT_MARGIN_BOTTOM;
    layout.marginLeft = SECTION_CLIENT_MARGIN_LEFT;
    layout.marginRight = SECTION_CLIENT_MARGIN_RIGHT;
    layout.horizontalSpacing = SECTION_CLIENT_HORIZONTAL_SPACING;
    layout.verticalSpacing = SECTION_CLIENT_VERTICAL_SPACING;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;
    return layout;
  }

  public static TableWrapLayout createSectionClientTableWrapLayout( final boolean makeColumnsEqualWidth,
                                                                    final int numColumns ) {
    TableWrapLayout layout = new TableWrapLayout();
    layout.topMargin = SECTION_CLIENT_MARGIN_TOP;
    layout.bottomMargin = SECTION_CLIENT_MARGIN_BOTTOM;
    layout.leftMargin = SECTION_CLIENT_MARGIN_LEFT;
    layout.rightMargin = SECTION_CLIENT_MARGIN_RIGHT;
    layout.horizontalSpacing = SECTION_CLIENT_HORIZONTAL_SPACING;
    layout.verticalSpacing = SECTION_CLIENT_VERTICAL_SPACING;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;
    return layout;
  }
  
}
