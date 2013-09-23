/*
 * $Id: AlignFormSectionFactory.java 1311 2010-03-07 22:51:10Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2009
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

public class AlignFormSectionFactory {
  
  private static Section gridExpandableSection;
  private static Section simpleSection;
  
  public static Composite createStaticSection( final FormToolkit toolkit, 
                                               final Composite parent, 
                                               final String sectionTitle, 
                                               final String sectionDescription, 
                                               final int numOfColumns ) {
    Section section;
    section = toolkit.createSection( parent, 
                                     ExpandableComposite.TITLE_BAR | 
                                     Section.DESCRIPTION | 
                                     SWT.WRAP );
    section.setText( sectionTitle );    
    section.setDescription( sectionDescription );
    
    toolkit.createCompositeSeparator( section );
    section.setLayout( AlignFormLayoutFactory.createClearTableWrapLayout( false, 1 ) );
    TableWrapData data = new TableWrapData( TableWrapData.FILL_GRAB );
    section.setLayoutData( data );
    Composite client = toolkit.createComposite( section );
    client.setLayout( AlignFormLayoutFactory.createSectionClientTableWrapLayout( false, numOfColumns ) );
    section.setClient( client );
    return client;
  }
  
    public static Composite createHtmlSection( 	final FormToolkit toolkit, 
						final Composite parent, 
						final String sectionTitle) {
	Section section = toolkit.createSection( parent, 
						 ExpandableComposite.TITLE_BAR | 
						 Section.DESCRIPTION | 
						 ExpandableComposite.TWISTIE | 
						 SWT.WRAP );
	section.setText(sectionTitle);
	TableWrapData td = new TableWrapData();
	td.grabHorizontal = true;
	td.heightHint = 400; //added
	section.setLayoutData(td);
	section.setExpanded( false ); 
	
	Composite client = toolkit.createComposite( section );
	   
	section.setClient( client );
	return client;
  }

  public static Composite createExpandableSection(  final FormToolkit toolkit, 
                                                    final Composite parent, 
                                                    final String sectionTitle, 
                                                    final String sectionDescription, 
                                                    final int numOfColumns,                                             
                                                    final boolean isInitialyExpanded ) {
    Section section;
    if ( isInitialyExpanded ) {
      section = toolkit.createSection( parent, 
                                       ExpandableComposite.TITLE_BAR | 
                                       Section.DESCRIPTION | 
                                       ExpandableComposite.TWISTIE | 
                                       SWT.WRAP );
    } else {
      section = toolkit.createSection( parent, 
                                       ExpandableComposite.TITLE_BAR |
                                       Section.DESCRIPTION |
                                       ExpandableComposite.TWISTIE );
    }
    section.setText( sectionTitle );    
    section.setDescription( sectionDescription );
    toolkit.createCompositeSeparator( section );
    section.setLayout( AlignFormLayoutFactory.createClearTableWrapLayout( false, 1 ) );
    TableWrapData data = new TableWrapData( TableWrapData.FILL_GRAB );
    section.setLayoutData( data );
    Composite client = toolkit.createComposite( section );
    client.setLayout( AlignFormLayoutFactory.createSectionClientTableWrapLayout( false, numOfColumns ) );
    section.setClient( client );
    return client;
 }
  
  public static Composite createGridStaticSection( final FormToolkit toolkit,
                                                   final Composite parent,
                                                   final String sectionTitle,
                                                   final String sectionDescription,
                                                   final int numOfColumns ) {
    
    Section section;
		if (sectionDescription.length() > 0) {
			section = toolkit.createSection(parent,
					ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
							| SWT.WRAP);
			section.setText(sectionTitle);
			section.setDescription(sectionDescription);
		} else {
			section = toolkit.createSection(parent,
					ExpandableComposite.TITLE_BAR |  SWT.WRAP);
			section.setText(sectionTitle);
		}
   
    toolkit.createCompositeSeparator( section );
    section.setLayout( AlignFormLayoutFactory.createClearTableWrapLayout( false, 1 ) );
    TableWrapData data = new TableWrapData( TableWrapData.FILL_GRAB );
    section.setLayoutData( data );
    
    Composite client = toolkit.createComposite( section );
    client.setLayout( AlignFormLayoutFactory.createSectionClientGridLayout( false, numOfColumns ) );
    section.setClient( client );
    return client;
  }
  
  public static Composite createGridExpandableSection( final FormToolkit toolkit,
                                                       final Composite parent,
                                                       final String sectionTitle,
                                                       final String sectionDescription,
                                                       final int numOfColumns,
                                                       final boolean isInitialyExpanded ) {
    
    Section section;
    if ( isInitialyExpanded ) {
      section = toolkit.createSection( parent, 
                                       ExpandableComposite.TITLE_BAR |
                                       Section.DESCRIPTION |
                                       ExpandableComposite.TWISTIE |
                                       SWT.WRAP );
    } else {
      section = toolkit.createSection( parent, 
                                       ExpandableComposite.TITLE_BAR |
                                       Section.DESCRIPTION |
                                       ExpandableComposite.TWISTIE );
    }
    section.setText( sectionTitle );
    section.setDescription( sectionDescription );
    toolkit.createCompositeSeparator( section );
    section.setLayout( AlignFormLayoutFactory.createClearTableWrapLayout( false, 1 ) );
    TableWrapData data = new TableWrapData( TableWrapData.FILL_GRAB );
    section.setLayoutData( data );
    gridExpandableSection = section;
    Composite client = toolkit.createComposite( section );
    client.setLayout( AlignFormLayoutFactory.createSectionClientGridLayout( false, numOfColumns ) );
    section.setClient( client );
    return client;
  }
  
  
  public static Composite createGridExpandableSection(
			final FormToolkit toolkit, final Composite parent,
			final String sectionTitle, final int numOfColumns, 
			final boolean isInitialyExpanded) {

		Section section;
		if (isInitialyExpanded) {
			section = toolkit.createSection(parent,
					ExpandableComposite.TITLE_BAR 
							| ExpandableComposite.TWISTIE | SWT.WRAP);
		} else {
			section = toolkit.createSection(parent,
					ExpandableComposite.TITLE_BAR 
							| ExpandableComposite.TWISTIE);
		}
		section.setText(sectionTitle);
		toolkit.createCompositeSeparator(section);
		section.setLayout(AlignFormLayoutFactory
				.createClearTableWrapLayout(false, 1));
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		section.setLayoutData(data);
		gridExpandableSection = section;
		Composite client = toolkit.createComposite(section);
		client.setLayout(AlignFormLayoutFactory.createSectionClientGridLayout(false,
				numOfColumns));
		section.setClient(client);
		return client;
	}
  
  public static Section getGridExpendableSection() {
    return gridExpandableSection;
  }
  
  public static Section getSimpleSection() {
	    return simpleSection;
	  }
  
   
} 
