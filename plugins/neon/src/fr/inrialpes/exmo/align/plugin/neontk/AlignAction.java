/*
 * $Id: AlignAction.java 1233 2010-02-13 11:22:27Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2010
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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
 
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IViewPart;
 
/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class AlignAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private IViewPart view;
	
	/**
	 * The constructor.
	 */
	public AlignAction() {
		 
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(AlignView.ID);
		
		try{
		window.getActivePage().showView(AlignView.ID);
		}
		catch(Exception e) {
			
		}
	}
	
    /**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
		IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(AlignView.ID);
		try{
		window.getActivePage().showView(AlignView.ID);
		}
		catch(Exception e) {
			
		}
		
	}
	
	private void initBrowser2() {
	     
	}
}
