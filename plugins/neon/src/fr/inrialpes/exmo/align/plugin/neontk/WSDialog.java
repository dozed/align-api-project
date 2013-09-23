/*
 * $Id: WSDialog.java 1311 2010-03-07 22:51:10Z euzenat $
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


import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.SWT;

public class WSDialog extends Dialog {
	  private String message;
	  private String serverInput;
	  private String methodInput;
	  
	  public WSDialog(Shell parent) {
	    // Pass the default styles here
	    this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL );
	  }

 
	  public WSDialog(Shell parent, int style) {
	    // Let users override the default styles
	    super(parent, style);
	    setText("Parameters for matching");
	    setMessage("Parameters for matching");
	  }
 
	  public String getMessage() {
	    return message;
	  }

 
	  public void setMessage(String message) {
	    this.message = message;
	  }

 
	  public String getServerInput() {
	    return serverInput;
	  }
	  
	  public String getMethodInput() {
		    return methodInput;
	  }
 
	  public void setServerInput(String input) {
	    this.serverInput = input;
	  }
	  public void setMethodInput(String input) {
		    this.methodInput = input;
	  }

	  public String open() {
	    // Create the dialog window
	    Shell shell = new Shell(getParent(), getStyle());
	    shell.setText(getText());
	    createContents(shell);
	    shell.pack();
	    shell.open();
	    Display display = getParent().getDisplay();
	    while (!shell.isDisposed()) {
	      if (!display.readAndDispatch()) {
	        display.sleep();
	      }
	    }
	    // Return the entered value, or null
	    return serverInput;
	  }

 
	  private void createContents(final Shell shell) {
	    shell.setLayout(new GridLayout(3, true));

	    // Show the message
	    Label label = new Label(shell, SWT.NONE);
	    label.setText(message);
	    GridData data = new GridData();
	    data.horizontalSpan = 6;
	    label.setLayoutData(data);
	    
	    Label serverLabel = new Label(shell, SWT.NONE);
		serverLabel.setText("Server: ");
	    // Display the input box for server
	    final Text server = new Text(shell, SWT.BORDER);
	    server.setText("http://kameleon.ijs.si/ontolight/ontolight.asmx");
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    data.horizontalSpan = 6;
	    server.setLayoutData(data);
	    
	    Label methodLabel = new Label(shell, SWT.NONE);
		methodLabel.setText("Method: ");
	    // Display the input box for method
	    final Text wsmethod = new Text(shell, SWT.BORDER);
	    //text.setText("");
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    data.horizontalSpan = 6;
	    wsmethod.setLayoutData(data);

	    // Create the OK button and add a handler
	    // so that pressing it will set input
	    // to the entered value
	    Button ok = new Button(shell, SWT.PUSH);
	    ok.setText("OK");
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    ok.setLayoutData(data);
	    ok.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	        serverInput = server.getText();
	        methodInput = wsmethod.getText();
	        shell.close();
	      }
	    });

	    // Create the cancel button and add a handler
	    // so that pressing it will set input to null
	    Button cancel = new Button(shell, SWT.PUSH);
	    cancel.setText("Cancel");
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    cancel.setLayoutData(data);
	    cancel.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	        serverInput = null;
	        methodInput = null;
	        shell.close();
	      }
	    });

	    // Set the OK button as the default, so
	    // user can type input and press Enter
	    // to dismiss
	    shell.setDefaultButton(ok);
	  }
	}

