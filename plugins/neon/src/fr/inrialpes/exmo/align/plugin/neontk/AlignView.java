/*
 * $Id: AlignView.java 1267 2010-02-16 16:28:56Z euzenat $
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

import org.eclipse.swt.SWT;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.PrintStream;// for debug
import java.io.StringWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.jar.Attributes.Name;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.util.Dictionary;
 
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.osgi.framework.Bundle;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.FileLocator;

//import org.eclipse.jface.dialogs.MessageDialog;
//import org.semanticweb.kaon2.api.OntologyManager;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;

//import com.ontoprise.config.IConfig;
import org.neontoolkit.core.NeOnCorePlugin;

//import org.neontoolkit.datamodel.DatamodelPlugin;
//import org.neontoolkit.core.DatamodelTypes;
//import com.ontoprise.ontostudio.datamodel.exception.ControlException;

import org.neontoolkit.core.command.project.CreateProject;
import org.neontoolkit.core.project.IOntologyProject;
import org.neontoolkit.core.project.OntologyProjectManager;
import org.neontoolkit.io.util.ImportExportUtils;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import com.ontoprise.ontostudio.owl.model.OWLManchesterProjectFactory;
import com.ontoprise.ontostudio.owl.gui.io.OntologyFileSystemImport;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.HTMLRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.plugin.neontk.AlignFormLayoutFactory;
import fr.inrialpes.exmo.align.plugin.neontk.AlignFormSectionFactory;
import fr.inrialpes.exmo.align.plugin.neontk.OnlineAlign;
import fr.inrialpes.exmo.align.plugin.neontk.WSDialog;
import fr.inrialpes.exmo.align.plugin.neontk.OnlineDialog;
 
 
public class AlignView extends ViewPart implements SelectionListener, Listener {
		
    public static final String ID = "fr.inrialpes.exmo.align.plugin.neontk.alignView";	
    private Combo  methods, renderer, localAlignBox, ontoBox1, ontoBox2,  serverAlignBox;
    private Button cancelButton, discardButton,  resButton, onto1Refresh, onto2Refresh,
	localImportButton, serverImportButton, uploadButton, 
	localTrimButton, serverTrimButton, connButton, goButton, offlineButton, onlineButton;
    private Button storeButton,  matchButton, findButton, findAllButton;// fetchButton;
    //private String selectedProject = null;
    //private Section ontoSelectSection;
    //private Section alignViewSection;
		
    private String selectedOnto1, selectedOnto2,  selectedLocalAlign, selectedServerAlign;
    //private String[] ontoList = new String[0];
    private String[]  methodList = new String[0];
    private HashMap<String,String> ontoByProj = new HashMap<String,String>(0);
		
    private Composite htmlClient = null;
    private Browser htmlBrowser = null;
    private FormToolkit formToolkit = null;
		
    public static Hashtable<String,Alignment>  alignmentTable = new Hashtable<String,Alignment>();
    private Hashtable<String,String>  idMap = new Hashtable<String,String>();
    static String [] forUniqueness = new String[0];
    static int alignId = 0;
		
    Composite composite = null;
    int width = 700;
    int buttonWidth = 150;
    int buttonHeight = 30;
		
    boolean online = false;

    String selectedHost = "aserv.inrialpes.fr"; 
    String selectedPort = "80"; 
		
    String selectedMethod = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
    String wserver = "http://kameleon.ijs.si/ontolight/ontolight.asmx";
    String wsmethod= "";
	    
    String alignProject = "AlignmentProject";
		
    public OnlineAlign   onlineAlign  = null;
    public OfflineAlign  offlineAlign = null;
    public File ontoFolder = null;
    public File alignFolder = null;
		
    @Override
	public void createPartControl(final Composite parent) {
	//FormToolkit formToolkit = new FormToolkit(Display.getCurrent());
	formToolkit = new FormToolkit(Display.getCurrent());
	ScrolledForm scrolledForm = formToolkit.createScrolledForm(parent);		
	Composite body = scrolledForm.getBody();
	body.setLayout(AlignFormLayoutFactory.createFormTableWrapLayout(false,1));
	composite = formToolkit.createComposite(body);
	composite.setLayout(AlignFormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
	composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			
	createOntologyChoosePart(composite, formToolkit);		
	createAlignmentPart(composite, formToolkit);
			 
	// JE: This should be in constructor!
	IWorkspaceRoot root =  org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot();
	IPath location = root.getLocation();
	String  path = location.toOSString();
		     
	//ontoFolder = new File(path + location.SEPARATOR + "onto");
	ontoFolder = new File(path + location.SEPARATOR + "align");
	if (!ontoFolder.exists()) ontoFolder.mkdir();

	alignFolder = new File(path + location.SEPARATOR + "align");
	if (!alignFolder.exists()) alignFolder.mkdir();
		    
	offlineInit( true );
    }
		
    private void createOntologyChoosePart(final Composite parent,
					  final FormToolkit toolkit){
			
	String sectionTitle = "Input";		
	Composite client ;
	int columns = 5;
	int textWidth = 600;
	int textHeight = 20;
	GridData gd = new GridData();
	gd.widthHint = textWidth;
	gd.heightHint = textHeight;
					
	client = AlignFormSectionFactory.createGridExpandableSection(toolkit, parent, sectionTitle, columns, false);//false
	Section ontoSelectSection = AlignFormSectionFactory.getGridExpendableSection();		
	ontoSelectSection.setExpanded(true);
			
	onlineButton = new Button(client, SWT.PUSH);
	onlineButton.setText("Online");
	onlineButton.setSize(buttonWidth, buttonHeight);
	onlineButton.addSelectionListener(this);
	// JE-RR for current version of the Alignment plug in
	onlineButton.setEnabled( true );
	//onlineButton.setEnabled( false );
			
	offlineButton = new Button(client, SWT.PUSH);
	offlineButton.setText("Offline");
	offlineButton.setSize(buttonWidth, buttonHeight);
	offlineButton.addSelectionListener(this);
	offlineButton.setEnabled( false );
			
	Label dummy1 = new Label(client, SWT.NONE);
	Label dummy2 = new Label(client, SWT.NONE);
	Label dummy3 = new Label(client, SWT.NONE);
	//Label dummy4 = new Label(client, SWT.NONE);
 
	// Choose ontology 1	
	Label onto1 = new Label(client, SWT.NONE);		
	onto1.setText("Ontology 1 ");	
	// Putting them read-only solves too many problems
	ontoBox1 = new Combo( client, SWT.DROP_DOWN | SWT.READ_ONLY );
	ontoBox1.setLayoutData( gd );
	ontoBox1.setEnabled(true);
	ontoBox1.addSelectionListener(this);
	onto1Refresh = new Button(client, SWT.PUSH);
	onto1Refresh.setText("Refresh");
	onto1Refresh.setSize(buttonWidth, buttonHeight);
	onto1Refresh.addSelectionListener(this);
	onto1Refresh.setEnabled(true);
	Label dummy5 = new Label(client, SWT.NONE);
	Label dummy6 = new Label(client, SWT.NONE);
	//Label dummy7 = new Label(client, SWT.NONE);
			
	//Choose ontology 2
	Label onto2 = new Label(client, SWT.NONE);
	onto2.setText("Ontology 2 ");		
	// Putting them read-only solves too many problems
	ontoBox2 = new Combo( client, SWT.DROP_DOWN | SWT.READ_ONLY );
	ontoBox2.setLayoutData(gd);
	ontoBox2.setEnabled(true);
	ontoBox2.addSelectionListener(this);
	//onto2Refresh = new Button(client, SWT.PUSH);
	//onto2Refresh.setText("Refresh");
	//onto2Refresh.setSize(buttonWidth, buttonHeight);
	//onto2Refresh.addSelectionListener(this);
	//onto2Refresh.setEnabled(true);
	Label dummy8 = new Label(client, SWT.NONE);
	Label dummy9 = new Label(client, SWT.NONE);
	// replaces the useless refresh
	Label dummy10 = new Label(client, SWT.NONE);
			
	//methods
	Label methodLabel = new Label(client, SWT.NONE);
	methodLabel.setText("Methods ");		
	methods = new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY );
	methods.setLayoutData(gd);
	methods.setEnabled(true);
	methods.addSelectionListener(this);
			
	//match
	matchButton = new Button(client, SWT.PUSH);
	matchButton.setText("Match");
	matchButton.setSize(buttonWidth, buttonHeight);
	matchButton.addSelectionListener(this);
	matchButton.setEnabled(true);
			
	Label dummy11 = new Label(client, SWT.NONE);
	Label dummy12 = new Label(client, SWT.NONE);
	//Label dummy13 = new Label(client, SWT.NONE);
			
	//server alignment list
	Label serverAlignLabel  = new Label(client, SWT.NONE );
	serverAlignLabel.setText("Server alignments");
			
	serverAlignBox =  new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY );
	serverAlignBox.setLayoutData(gd);
	serverAlignBox.setEnabled( false );
	serverAlignBox.addSelectionListener(this);
			
			
	//import
	serverImportButton = new Button(client, SWT.PUSH);
	serverImportButton.setText("Import");
	serverImportButton.setSize(buttonWidth, buttonHeight);
	serverImportButton.addSelectionListener(this);
	serverImportButton.setEnabled(false);
			
	//trim
	serverTrimButton = new Button(client, SWT.PUSH);
	serverTrimButton.setText("Trim");
	serverTrimButton.setSize(buttonWidth, buttonHeight);
	serverTrimButton.addSelectionListener(this);
	serverTrimButton.setEnabled(false);
			
	//store
	storeButton = new Button(client, SWT.PUSH);
	storeButton.setText("Store");
	storeButton.setSize(buttonWidth, buttonHeight);
	storeButton.addSelectionListener(this);
	storeButton.setEnabled(false);
			
	//fetch
	//fetchButton = new Button(client, SWT.PUSH);
	//fetchButton.setText("Fetch");
	//fetchButton.setSize(buttonWidth, buttonHeight);
	//fetchButton.addSelectionListener(this);
	//fetchButton.setEnabled(false);
			
	//local alignment list
	Label localAlignLabel  = new Label(client, SWT.NONE );
	localAlignLabel.setText("Local alignments");
			
	localAlignBox =  new Combo(client, SWT.DROP_DOWN | SWT.READ_ONLY );
	localAlignBox.setLayoutData(gd);
	localAlignBox.setEnabled(false);
	localAlignBox.addSelectionListener(this);
			
	//local import
	localImportButton = new Button(client, SWT.PUSH);
	localImportButton.setText("Import");
	localImportButton.setSize(buttonWidth, buttonHeight);
	localImportButton.addSelectionListener(this);
	localImportButton.setEnabled(false);
			
	//local trim
	localTrimButton = new Button(client, SWT.PUSH);
	localTrimButton.setText("Trim");
	localTrimButton.setSize(buttonWidth, buttonHeight);
	localTrimButton.addSelectionListener(this);
	localTrimButton.setEnabled(false);
			
	//upload
	uploadButton = new Button(client, SWT.PUSH);
	uploadButton.setText("Upload");
	uploadButton.setSize(buttonWidth, buttonHeight);
	uploadButton.addSelectionListener(this);
	uploadButton.setEnabled(false);
			
	//Label dummy14 = new Label(client, SWT.NONE);
			 
	Label dummy15 = new Label(client, SWT.NONE);
	Label dummy16 = new Label(client, SWT.NONE);
	Label dummy17 = new Label(client, SWT.NONE);
	Label dummy18 = new Label(client, SWT.NONE);
	Label dummy19 = new Label(client, SWT.NONE);
	//Label dummy20 = new Label(client, SWT.NONE);
			
	Label dummy21 = new Label(client, SWT.NONE);
			
	//find
	findButton = new Button(client, SWT.PUSH);
	findButton.setText("Find alignments for ontologies");
	findButton.setSize(buttonWidth, buttonHeight);
	findButton.addSelectionListener(this);
	findButton.setEnabled(false);
			 
	Label dummy22 = new Label(client, SWT.NONE);
	Label dummy23 = new Label(client, SWT.NONE);
	Label dummy24 = new Label(client, SWT.NONE);
	//Label dummy25 = new Label(client, SWT.NONE);
	Label dummy26 = new Label(client, SWT.NONE);
			
	//find all
	findAllButton = new Button(client, SWT.PUSH);
	findAllButton.setText("Find all alignments from server");
	findAllButton.setSize(buttonWidth, buttonHeight);
	findAllButton.addSelectionListener(this);
	findAllButton.setEnabled(false);
			
	toolkit.paintBordersFor(client);
    }
		
    private void createAlignmentPart(final Composite parent,
				     final FormToolkit toolkit){
			
	String sectionTitle = "View Alignment";
			 
	htmlClient = AlignFormSectionFactory.createHtmlSection(toolkit, parent, sectionTitle);
 
	FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
			
	htmlClient.setLayout( fillLayout );
	htmlClient.setLocation(0, 0);
	htmlBrowser = new Browser(htmlClient, SWT.BORDER );
		     
	toolkit.paintBordersFor(htmlClient);
    }
		
    public void viewHTMLAlign( String alignKey ) {
	//System.out.println(" html with alignKey="+alignKey);
	htmlBrowser.setText("");
	formToolkit.paintBordersFor(htmlClient);
	//I do not know why it no longer works if URI casting is put here!!!
	Alignment align = AlignView.alignmentTable.get(alignKey);
			 
	StringWriter htmlMessage = null;
	try {
	    htmlMessage = new StringWriter();
	    AlignmentVisitor htmlV = new HTMLRendererVisitor(  new PrintWriter ( htmlMessage )  );
	    align.render( htmlV );
	} catch(Exception ex) { ex.printStackTrace(); }
			
	String html = htmlMessage.toString();
	if(html == null || html.equals("")) {
	    htmlBrowser.setText("");
	    //System.out.println(" no html");
	} else {
	    htmlBrowser.setText( html );
	    //System.out.println(" some html");
	}
	formToolkit.paintBordersFor(htmlClient);
    }

    //fill "alignmentTable"
    public String fetchAlignFromServer(String id) {
	Alignment align = null;
	String alignKey = null;
			   
	if( idMap.containsKey(id) ) {
	    alignKey = idMap.get(id);
	    align =  AlignView.alignmentTable.get( alignKey );
	} else {
				     
	    FileWriter out = null;
						
	    String rdfalignStr = onlineAlign.getRDFAlignment( id );
				 	
	    alignKey =  alignFolder.getAbsolutePath() + File.separator + getNewAlignId();
			   	   
	    //System.out.println("alignKey="+alignKey);
	    String rdfPath =  alignKey + ".rdf";
				
	    try {
					
		File rdfFile = new File( rdfPath );
		out = new FileWriter( rdfFile );
		out.write( rdfalignStr );
		out.flush();
		out.close();
					
		File file = new File(rdfPath);
					
		AlignmentParser ap = new AlignmentParser(0);
		ap.setEmbedded( true );
					
		//System.out.println(" parsing string ... ");
		//align = ap.parseString( rdfalignStr  );
		align = ap.parse( file.toURI().toString() );
 				
		//if(align==null) System.out.println("align null" ); else System.out.println("align non null" );
					
		AlignView.alignmentTable.put( alignKey, align );
		idMap.put(id, alignKey);
		localAlignBox.setEnabled(true);
		localAlignBox.add(alignKey, 0);
		selectedLocalAlign = alignKey;
		localAlignBox.select(0);
		localAlignBox.redraw();
					
	    } catch ( Exception ex ) { ex.printStackTrace();}
	}
			   
	return alignKey;
    }
		
    public void handleEvent(Event e) {
    }
		
    public void widgetSelected(SelectionEvent e) {
	if ( e.getSource().equals( this.onlineButton ) ) {
	    //this.onlineButton.setExpanded(true);
	    OnlineDialog onDialog = new OnlineDialog( getSite().getShell() );
	    onDialog.setInput("aserv.inrialpes.fr");
	    onDialog.setMessage("URL for alignment server:");
	    onDialog.open();
	    if ( !(onDialog.getInput() == null) && !onDialog.getInput().equals("")) { 
		online = true;
		selectedHost = onDialog.getInput();
		onlineAlign = new OnlineAlign(selectedPort, selectedHost );
		
		String list[] = onlineAlign.getMethods();
		if(list == null || list.length ==0) { 
		    MessageDialog.openError(this.getSite().getShell(), "Error message",
					    "Impossible connection!");
		    return;
		}
		onlineButton.setEnabled( false );
		offlineButton.setEnabled( true );
		findButton.setEnabled(true);
		findAllButton.setEnabled(true);
		serverAlignBox.removeAll();
		serverAlignBox.setEnabled( false );
		serverImportButton.setEnabled( false );
		serverTrimButton.setEnabled( false );
		storeButton.setEnabled( false );
		//fetchButton.setEnabled( false );
		
		if(localAlignBox.getItems().length == 0) {
		    localAlignBox.setEnabled( false );
		    localImportButton.setEnabled( false );
		    localTrimButton.setEnabled( false );
		    uploadButton.setEnabled( false );
		    selectedLocalAlign = null;
		    //setButtons( 3 ); //no localList, no server list 
		} else {
		    localAlignBox.setEnabled( true );
		    localImportButton.setEnabled( true );
		    localTrimButton.setEnabled( true );
		    uploadButton.setEnabled( true );
		    
		}
		selectedOnto1 = null;
		selectedOnto2 = null;
		selectedServerAlign = null;
		methods.removeAll();
		selectedMethod = list[0];				
		methods.setItems( list );
		methods.select(0);
		methods.redraw(); 	
		ontoByProj = refreshOntoList( online );
	    }
	} else if ( e.getSource() == offlineButton ) {
	    offlineInit( false );
	} else if (e.getSource() == ontoBox1) {			
	    int index = ontoBox1.getSelectionIndex();
	    selectedOnto1 = ontoBox1.getItem(index);
	} else if (e.getSource() == ontoBox2) {			
	    int index = ontoBox2.getSelectionIndex();
	    selectedOnto2 = ontoBox2.getItem(index);
	} else if (e.getSource() == methods) {			
	    int index = methods.getSelectionIndex();
	    selectedMethod = methods.getItem(index);
	} else if ( e.getSource() == onto1Refresh || e.getSource() == onto2Refresh ) {
	    ontoByProj = refreshOntoList( online );
	} else if (e.getSource() == serverAlignBox) {			
	    int index = serverAlignBox.getSelectionIndex();
	    selectedServerAlign = serverAlignBox.getItem(index);
	    String alignKey = fetchAlignFromServer(selectedServerAlign);
	    viewHTMLAlign( alignKey );
	    
	} else if (e.getSource() == localAlignBox) {		
	    
	    int index = localAlignBox.getSelectionIndex();
	    selectedLocalAlign = localAlignBox.getItem(index);
	    //System.out.println(" local selected="+selectedLocalAlign);
	    viewHTMLAlign(selectedLocalAlign);
	    
	    //processing ontology matching
	} else if (e.getSource() == matchButton) {
	    selectedOnto1 = ontoBox1.getText();
	    selectedOnto2 = ontoBox2.getText();
	    if ( selectedOnto1 == null  || selectedOnto2 == null ) {
		MessageDialog.openError(this.getSite().getShell(), "Error message",
					"Choose two ontologies from the lists! "); 
		return;
	    }
	    if( online ) {
		if (!selectedOnto1.startsWith("http://") || !selectedOnto2.startsWith("http://") ) {
		    MessageDialog.openError(this.getSite().getShell(), "Error message", "Ontology URLs are required.");
		    return;
		}
		if( selectedMethod.equals("fr.inrialpes.exmo.align.service.WSAlignment") ) {
		    WSDialog pa = new WSDialog( getSite().getShell() );
		    pa.setServerInput("http://kameleon.ijs.si/ontolight/ontolight.asmx");
		    pa.open();
		    
		    if( pa.getServerInput().equals("")) {
			MessageDialog.openError(this.getSite().getShell(), "Error message", "No available server!");
			return;
		    } else {
			wserver = pa.getServerInput();
			wsmethod = pa.getMethodInput();
			String alignId = onlineAlign.getAlignIdMonoThread( selectedMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
			if( alignId == null || alignId .equals(""))  {
			    MessageDialog.openError(this.getSite().getShell(), "Error message", "The alignment has not been produced.");
			    return;
			}
			
			serverAlignBox.add(alignId, 0);
			selectedServerAlign = alignId;
			serverImportButton.setEnabled( true );
			serverTrimButton.setEnabled( true );
			storeButton.setEnabled( true );
			serverAlignBox.select(0);
			serverAlignBox.redraw(); 
			String alignKey = fetchAlignFromServer(selectedServerAlign);
			viewHTMLAlign(alignKey);
		    }
		    
		} else { 
		    String alignId = onlineAlign.getAlignIdMonoThread( selectedMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
		    if( alignId == null || alignId .equals(""))  {
			MessageDialog.openError(this.getSite().getShell(), "Error message", "The alignment has not been produced.");
			return;
		    }
		    //onAlign.getAlignId( matchMethod, wserver, wsmethod, selectedOnto1, selectedOnto2 );
		    serverAlignBox.add(alignId, 0);
		    selectedServerAlign = alignId;
		    serverAlignBox.setEnabled( true );
		    serverImportButton.setEnabled( true );
		    serverTrimButton.setEnabled( true );
		    storeButton.setEnabled( true );
		    //fetchButton.setEnabled( true );
		    serverAlignBox.select(0);
		    serverAlignBox.redraw();
		    String alignKey = fetchAlignFromServer(selectedServerAlign);
		    viewHTMLAlign(alignKey);
		}
	    } else { //offline
		/*
MessageDialog.openError(this.getSite().getShell(), "Error message",
					"method: "+selectedMethod); 
MessageDialog.openError(this.getSite().getShell(), "Error message",
			"onto1: "+selectedOnto1);
MessageDialog.openError(this.getSite().getShell(), "Error message",
					"onto2: "+selectedOnto2);
MessageDialog.openError(this.getSite().getShell(), "Error message",
					"onto2: "+selectedOnto2);
		*/
		try {
		    selectedLocalAlign = offlineAlign.matchAndExportAlign( selectedMethod, ontoByProj.get(selectedOnto1), selectedOnto1, ontoByProj.get(selectedOnto2), selectedOnto2);
		} catch ( Exception ex ) {
		    MessageDialog.openError(this.getSite().getShell(), "Error message", ex.getMessage() );
		    return;
		}
		/*
MessageDialog.openError(this.getSite().getShell(), "Error message",
					"returned: "+selectedLocalAlign);
		*/		localAlignBox.setEnabled( true );
		localAlignBox.add( selectedLocalAlign, 0 );
		localImportButton.setEnabled( true );
		localTrimButton.setEnabled( true );
		localAlignBox.select(0);
		localAlignBox.redraw();
		//if( AlignView.alignmentTable.get( selectedLocalAlign ) != null) System.out.println("Offline match OK");
		//else System.out.println("Offline match Non ");
		viewHTMLAlign( selectedLocalAlign );
	    } //offline
	    //processing alignment fetching
	}  else if (e.getSource() == storeButton) {
	    if(selectedServerAlign == null) { 
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Choose an alignment ID from the list!");
		return;      
	    }
	    
	    String sto = onlineAlign.storeAlign(selectedServerAlign);  
	    
	    if(sto == null || sto.equals("") ) {
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Alignment cannot be stored!");
		return;
	    } else {	 
		MessageDialog.openConfirm(this.getSite().getShell(), "Confirmation", "Alignment: "+ selectedServerAlign + "\n is stored!");
	    }
	    
	} else if (e.getSource() == serverTrimButton) {
	    
	    if(selectedServerAlign == null) { 
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Choose an alignment ID from the list!");
		return;
	    }
	    
	    OnlineDialog trimDialog = new OnlineDialog( getSite().getShell() );
	    trimDialog.setInput("0.5");
	    trimDialog.setMessage("Threshold:");
	    trimDialog.open();
	    String thres = trimDialog.getInput(); 
	    if ( trimDialog.getInput() == null) return;
	    if ( trimDialog.getInput().equals("")) { 
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Invalid threshold!");
		return;
	    }
	    
	    String at = onlineAlign.trimAlign(selectedServerAlign, thres);
	    if(at == null) return;
	    if(at.equals("")) {
		MessageDialog.openError(this.getSite().getShell(), "Error message", "No alignment is obtained!");
		return;
	    }
	    
	    serverAlignBox.add( at, 0 );
	    selectedServerAlign = at; 	
	    serverAlignBox.select(0);
	    serverAlignBox.redraw();
	    String alignKey = fetchAlignFromServer(selectedServerAlign);
	    viewHTMLAlign(alignKey);
	    
	} else if (e.getSource() == localTrimButton) {
	    if(selectedLocalAlign == null) { 
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Choose an alignment ID from the list!");
		return;
	    }
	    
	    OnlineDialog trimDialog = new OnlineDialog( getSite().getShell() );
	    trimDialog.setInput("0.5");
	    trimDialog.setMessage("Threshold:");
	    trimDialog.open();
	    String thres = trimDialog.getInput(); 
	    if ( trimDialog.getInput() == null) return;
	    if ( trimDialog.getInput().equals("")) { 
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Invalid threshold!");
		return;
	    }
	    
	    String resId = offlineAlign.trimAndExportAlign( new Double(thres), selectedLocalAlign );
	    
	    localAlignBox.add( resId, 0 );
	    selectedLocalAlign = resId; 	
	    localAlignBox.select(0);
	    localAlignBox.redraw();	 
	    viewHTMLAlign(selectedLocalAlign);
	    
	} else if (e.getSource() == serverImportButton) {
	    
	    if(selectedServerAlign == null) { 
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Choose an alignment ID from the list!");
		return;
	    }
	    
	    URIAlignment align = null;
	    String owlalignStr = null;
	    String alignKey = fetchAlignFromServer( selectedServerAlign );
	    align = (URIAlignment)AlignView.alignmentTable.get( alignKey );
	    try {
		StringWriter owlMessage = new StringWriter();
		AlignmentVisitor owlV = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlMessage )  );
		ObjectAlignment al = ObjectAlignment.toObjectAlignment( align );
		//BasicAlignment al =  (BasicAlignment)align;
		al.render( owlV );
		owlalignStr = owlMessage.toString();
	    }catch(Exception ex) {
		ex.printStackTrace();
	    }
	    
	    if(owlalignStr==null)  {
		MessageDialog.openError(this.getSite().getShell(), "Error message", "OWL alignment cannot be imported.");
		return;
	    }
	    
	    try {
		
		OnlineDialog localImportDialog = new OnlineDialog( getSite().getShell() );
		localImportDialog.setInput("AlignmentProject");
		localImportDialog.setMessage("Enter a project name:");
		localImportDialog.open();
		String inputName = localImportDialog.getInput(); 
		
		if (inputName==null || inputName.equals("")) {
		    MessageDialog.openError(this.getSite().getShell(), "Error message", "Project name is not valid!"); 
		    return;
		}
                               
		String[] projects = OntologyProjectManager.getDefault().getOntologyProjects();
		
		//String[] projects = DatamodelPlugin.getDefault().getOntologyProjects();
		if(projects!=null) {
		    boolean found = false;	
		    
		    for(int i=0; i < projects.length; i++) {
	    		
			if(projects[i].equals(inputName)) { 
			    found = true;break;
			}
		    }
		    
		    if(!found) {
	    		
			//Properties proper = new Properties();
			//proper.put(IConfig.ONTOLOGY_LANGUAGE.toString(), IConfig.OntologyLanguage.OWL.toString());
	    		
			new CreateProject( inputName, OWLManchesterProjectFactory.FACTORY_ID, new Properties()).run();
   			
			//new CreateProject( inputName, DatamodelTypes.RAM, proper ).run();
		    }
		}
	    	
		String owlPath =  ontoFolder.getAbsolutePath() + File.separator + getNewAlignId() + ".owl";
		File owlFile = new File( owlPath );
		if ( owlFile.exists() ) owlFile.delete();
		
		//System.out.println("owl impoted ="+owlalignStr);
		
		FileWriter out = new FileWriter( owlFile );
		out.write( owlalignStr );
		out.flush();
		out.close();
		
		//ImportExportUtils ieControl = new ImportExportUtils();
   	    	
		URI uris[] = new URI[1];
		uris[0] = new File(owlPath).toURI();
		new OntologyFileSystemImport(inputName, new String[] {uris[0].toString()}).run();
		//String[] importedUri = new String[1];
		//importedUri[0] = ImportExportUtils.copyOntologyFileToProject(uris[0].toString(), inputName);
		//ImportExportUtils.addOntologiesToProject(inputName, importedUri);
		/*
		  try {
		  ImportExportControl ieControl = new ImportExportControl();
		  URI uris[] = new URI[1];
		  uris[0] = new File( owlPath).toURI();
		  String[] importedModules = ieControl.importFileSystem(inputName, uris,  null);
		  
		  //ieControl.addOntologies2Project(importedModules, inputName);
		  } catch (  ControlException ex ) { }
		*/ 
	    } 
	    catch ( Exception ex ) { ex.printStackTrace();}
	    
	} else if (e.getSource() == localImportButton) {
	    if(selectedLocalAlign == null) {
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Choose an alignment ID from list!");
		return;
	    }
	    
	    File fn = new File(selectedLocalAlign + ".owl");
	    
	    StringWriter htmlMessage = new StringWriter();
	    AlignmentVisitor htmlV = new HTMLRendererVisitor(  new PrintWriter ( htmlMessage )  );
	    
	    try {
		AlignView.alignmentTable.get(selectedLocalAlign).render( htmlV );
		
		viewHTMLAlign(  selectedLocalAlign  );
		
		String inputName=null;
		OnlineDialog localImportDialog = new OnlineDialog( getSite().getShell() );
		localImportDialog.setInput("AlignmentProject");
		localImportDialog.setMessage("Enter a project name:");
		localImportDialog.open();
		inputName = localImportDialog.getInput(); 
       					
		if (inputName==null || inputName.equals("")) { 
		    MessageDialog.openError(this.getSite().getShell(), "Error message", "Project name is not valid!");
		    return;
		}
   						
		String[] projects = OntologyProjectManager.getDefault().getOntologyProjects();
					   
		if(projects!=null) {
		    boolean found = false;	
   	    				
		    for(int i=0; i < projects.length; i++) {	 
			if(projects[i].equals(inputName)) { 
			    found = true;break;
			}
		    }
   	    				
		    if(!found) {
			//Properties proper = new Properties();
			//proper.put(IConfig.ONTOLOGY_LANGUAGE.toString(), IConfig.OntologyLanguage.OWL.toString());
			new CreateProject( inputName, OWLManchesterProjectFactory.FACTORY_ID, new Properties()).run();
			//new CreateProject(	inputName, DatamodelTypes.RAM, proper ).run();
           				
		    }
   					 
		    //ImportExportControl ieControl = new ImportExportControl();
		    //ImportExportUtils ieControl = new ImportExportUtils();
   	    				
		    URI uris[] = new URI[1];
		    uris[0] = new File(fn.getAbsolutePath()).toURI();

		    new OntologyFileSystemImport(inputName, new String[] {uris[0].toString()}).run();

		    //String[] importedUri = new String[1]; 
		    //importedUri[0] = ImportExportUtils.copyOntologyFileToProject(uris[0].toString(), inputName);
		    //ImportExportUtils.addOntologiesToProject(inputName, importedUri);
		    //ieControl.importFileSystem(inputName, uris, null);
   	    				 
		}
	    }
	    catch ( Exception ex ) { ex.printStackTrace();};
       			 
	} else if (e.getSource() == uploadButton) {
	    if(selectedLocalAlign == null) { 
		MessageDialog.openError(this.getSite().getShell(), "Error message", "Choose an alignment ID from the list!");
		return;
	    }
	    String uploadedId = null;
	    try {
		uploadedId = onlineAlign.uploadAlign(selectedLocalAlign + ".rdf");
		if(uploadedId == null) {
		    MessageDialog.openError(this.getSite().getShell(), "Error message", "Alignment cannot be uploaded!");
		    return;
		}
	        			 
		MessageDialog.openConfirm(this.getSite().getShell(), "Confirmation", "Alignment is uploaded with ID: \n" + uploadedId);
	    } catch ( Exception ex ) { ex.printStackTrace();}
				   
	} else if ( e.getSource().equals( this.findAllButton ) ) {
				   
	    String[] onList = null;
	    String[] offList = null;
				   
	    if(online) {
		onList = onlineAlign.getAllAlign();
		if(onList == null || onList.length==0) {
		    MessageDialog.openError(this.getSite().getShell(), "Error message", "No available alignment"); 
		    return;
		}
		//serverAlignBox.setEnabled( true );
		serverAlignBox.removeAll();
		serverAlignBox.setItems(onList);
		selectedServerAlign = onList[0]; 
		serverAlignBox.select(0);
		serverAlignBox.redraw();
       			 
		offList  = offlineAlign.getAllAlign( );
		if( offList.length > 0 ) {
		    //localAlignBox.setEnabled( true );
		    localAlignBox.removeAll();
		    localAlignBox.setItems( offList );
		    selectedLocalAlign = offList[0];
		    localAlignBox.select(0);
		    localAlignBox.redraw();
		}
	    } 
      			   
	    if(onList.length > 0 ) { 
		serverAlignBox.setEnabled( true );
		serverImportButton.setEnabled( true );
		serverTrimButton.setEnabled( true );
		storeButton.setEnabled( true );
		//fetchButton.setEnabled( true );
	    }
	    if(offList.length > 0 ) {
		uploadButton.setEnabled( true );
		localAlignBox.setEnabled( true );
		localImportButton.setEnabled( true );
		localTrimButton.setEnabled( true );
	    }    			
	} else if ( e.getSource() == findButton ) {
	    String[] onList = null;
	    selectedOnto1 = ontoBox1.getText();
	    selectedOnto2 = ontoBox2.getText();
	    if(online) {
		if(selectedOnto1 == null || selectedOnto1.equals("") || selectedOnto2 == null || selectedOnto2.equals("")) {
		    MessageDialog.openError(this.getSite().getShell(), "Error message", "Choose two ontologies from the lists!"); 
		    return;
		}
		onList = onlineAlign.findAlignForOntos( selectedOnto1, selectedOnto2);
		if(onList == null || onList.length==0) {
		    MessageDialog.openError(this.getSite().getShell(), "Error message", "No available alignment."); 
		    return;
		}
		serverAlignBox.removeAll();
      					
		if(onList.length > 0) {
		    serverAlignBox.setEnabled( true );
		    serverAlignBox.setItems(onList);
		    selectedServerAlign = onList[0]; 
		    serverImportButton.setEnabled( true );
		    serverTrimButton.setEnabled( true );
		    storeButton.setEnabled( true );
		    //fetchButton.setEnabled( true );
		    serverAlignBox.select(0);
		} else {
		    selectedServerAlign = null; 
		    serverAlignBox.setEnabled( false );
		    serverImportButton.setEnabled( false );
		    serverTrimButton.setEnabled( false );
		    storeButton.setEnabled( false );
		    //fetchButton.setEnabled( false );
		}
		serverAlignBox.redraw();
	    }
	}
    }
			 
    public static int getNewAlignId() {
			   
	boolean found = false;
	alignId++;
	while(!found) {
				   
	    boolean sw = false;
	    for(int i=0;i< forUniqueness.length; i++) 
		if(forUniqueness[i].equals((new Integer(alignId)).toString())) {
		    alignId++;
		    sw = true;
		    break;
		}
			   
	    if( !sw ) found = true;
				   
	}
				   
	return alignId;
    }
		
    /**
     *  This function is taken from Alignment Server with processing Jar in Jar
     */
		
    public static void implementations( Class tosubclass, Set<String> list , String cp,  boolean debug ){
	Set<String> visited = new HashSet<String>();
	//String classPath = System.getProperty("java.class.path",".");
	String classPath = cp;
	int count=0;
	// Hack: this is not necessary
	//classPath = classPath.substring(0,classPath.lastIndexOf(File.pathSeparatorChar));
			
	//StringTokenizer tk = new StringTokenizer(classPath,File.pathSeparator);
	StringTokenizer tk = new StringTokenizer(classPath, "," );
	//System.err.println("classPath for Impl.="+ classPath );
			
	classPath = "";
	while ( tk != null && tk.hasMoreTokens() ) {
	    StringTokenizer tk2 = tk;
	    tk = null;
	    //System.err.println( "path" + tk2.toString());
	    // Iterate on Classpath
	    while ( tk2.hasMoreTokens() ) {
		try {
		    URI nn = new URI( tk2.nextToken() );
		    //System.err.println("token="+ nn);
		    File file = new File( nn );
		    if ( file.isDirectory() ) {
			//System.err.println("DIR "+file);
			String subs[] = file.list();
			for(int index = 0 ; index < subs.length ; index ++ ){
			    //System.err.println("subs=    "+subs[index]);
			    // IF class
			    if ( subs[index].endsWith(".class") ) {
				String classname = subs[index].substring(0,subs[index].length()-6);
				//System.err.println("classname=    "+classname);
				if (classname.startsWith(File.separator)) 
				    classname = classname.substring(1);
				classname = classname.replace(File.separatorChar,'.');
				try {
				    // JE: Here there is a bug that is that it is not possible
				    // to have ALL interfaces with this function!!!
				    // This is really stupid but that's life
				    // So it is compulsory that AlignmentProcess be declared 
				    // as implemented
				    Class[] cls = Class.forName(classname).getInterfaces();
				    for ( int i=0; i < cls.length ; i++ ){
					if ( cls[i] == tosubclass ) {
					    if (debug ) System.err.println(" -j-> "+classname);
					    list.add( classname );
					}
					if ( debug ) System.err.println("       I> "+cls[i] );
				    }
				    // Not one of our classes
				} catch ( NoClassDefFoundError ncdex ) {
				} catch (ClassNotFoundException cnfex) {
				} catch (UnsatisfiedLinkError ule) {
				} catch (ExceptionInInitializerError eiie) {
				    // This one has been added for OMWG, this is a bad error
				}
			    }
			}
		    } else if ( (file.toString().endsWith(".jar") ) &&
				!visited.contains( file.toString() ) &&
				file.exists() ) {
					 
			visited.add( file.toString() );
			try { 
			    JarFile jar = new JarFile( file );
			    Enumeration enumeration = jar.entries();
			    while( enumeration != null && enumeration.hasMoreElements() ) {
				JarEntry je = (JarEntry)enumeration.nextElement();
				String classname = je.toString();
						 
				int len = classname.length()-6;
						
				if( len > 0 && classname.substring(len).compareTo(".class") == 0 ) {
				    classname = classname.substring(0,len);
				    // Beware, in a Jarfile the separator is always "/"
				    // and it would not be dependent on the current system anyway.
				    //classname = classname.replaceAll(File.separator,".");
				    classname = classname.replaceAll("/",".");
						    
				    //System.err.println("classname in jar =    "+classname);
						    
				    try {
					if ( classname.equals("org.apache.xalan.extensions.ExtensionHandlerGeneral") ) throw new ClassNotFoundException( "Stupid JAVA/Xalan bug");
					Class cl = Class.forName(classname);
					Class[] ints = cl.getInterfaces();
					for ( int i=0; i < ints.length ; i++ ){
					    if ( ints[i] == tosubclass ) {
						if (debug ) System.err.println(" -j-> "+classname);
						list.add( classname );
					    }
					    if ( debug ) System.err.println("       I> "+ints[i] );
					}
				    } catch ( NoClassDefFoundError ncdex ) {
				    } catch ( ClassNotFoundException cnfex ) {
					if ( debug ) System.err.println("   ******** "+classname);
				    } catch ( UnsatisfiedLinkError ule ) {
				    } catch (ExceptionInInitializerError eiie) {
					// This one has been added for OMWG, this is a bad error
				    }
				} else if( classname.endsWith(".jar") ) {
				    //If jarEntry is a jarfile
				    //System.err.println(  "jarEntry is a jarfile="+je.getName() );
				    InputStream jarSt = jar.getInputStream( (ZipEntry)je );
				    // JE2010: START OF CRITICAL SECTION
				    //File f = new File("tmpFileXXX"+ count++);
				    File f = File.createTempFile( "tmpFileXXX"+count++, "jar" );
							
				    try {			
					OutputStream out = new FileOutputStream( f );
					byte buf[]=new byte[1024];
					int len1 ;
					while( (len1 = jarSt.read(buf))>0 )
					    out.write(buf,0,len1);
					out.close();
					jarSt.close();
				    }
				    catch (IOException e){
					System.err.println( "Cannot write tmp data!" );
				    }
						    
				    JarFile inJar = new JarFile( f );
				    Enumeration enumeration2 = inJar.entries();
							
				    while( enumeration2.hasMoreElements() ) {
					JarEntry je2 = (JarEntry)enumeration2.nextElement();
					String classname2 = je2.toString();
							 
					int len3 = classname2.length()-6;
							
					if( len3 > 0 && classname2.substring(len3).compareTo(".class") == 0 ) {
					    classname2 = classname2.substring(0,len3);
					    // Beware, in a Jarfile the separator is always "/"
					    // and it would not be dependent on the current system anyway.
					    //classname = classname.replaceAll(File.separator,".");
					    classname2 = classname2.replaceAll("/",".");
							    
					    //System.err.println("classname in jar in jar =    "+classname2);
							    
					    try {
						if ( classname2.equals("org.apache.xalan.extensions.ExtensionHandlerGeneral") ) throw new ClassNotFoundException( "Stupid JAVA/Xalan bug");
						Class cl = Class.forName(classname2);
						Class[] ints = cl.getInterfaces();
						for ( int i=0; i < ints.length ; i++ ){
						    if ( ints[i] == tosubclass ) {
							//System.err.println(classname2 + " added");
							list.add( classname2 );
						    }
						}
					    } catch ( NoClassDefFoundError ncdex ) {
					    } catch ( ClassNotFoundException cnfex ) {
								 
					    } catch ( UnsatisfiedLinkError ule ) {
					    } catch (ExceptionInInitializerError eiie) {
						// This one has been added for OMWG, this is a bad error
					    }
					}
				    }//while
				    f.delete();
				    // JE2010: END OF CRITICAL SECTION
				} // else If endWith .jar
					    
					    
				// Iterate on needed Jarfiles
				// JE(caveat): this deals naively with Jar files,
				// in particular it does not deal with section'ed MANISFESTs
				Attributes mainAttributes = jar.getManifest().getMainAttributes();
				String path = mainAttributes.getValue( Name.CLASS_PATH );
				if ( debug ) System.err.println("  >CP> "+path);
				if ( path != null && !path.equals("") ) {
				    // JE: Not sure where to find the other Jars:
				    // in the path or at the local place?
				    //classPath += File.pathSeparator+file.getParent()+File.separator + path.replaceAll("[ \t]+",File.pathSeparator+file.getParent()+File.separator);
				    // This replaces the replaceAll which is not tolerant on Windows in having "\" as a separator
				    // Is there a way to make it iterable???
				    for( StringTokenizer token = new StringTokenizer(path," \t"); token.hasMoreTokens(); )
					//classPath += File.pathSeparator+file.getParent()+File.separator+token.nextToken();
					classPath += ","+file.getParent()+File.separator+token.nextToken();
				}
					    
			    } //while 
			} catch (NullPointerException nullexp) { //Raised by JarFile
			    System.err.println("Warning "+file+" unavailable");
			}
		    }
		} catch( Exception e ) {
		    continue;
		}
	    }
				    
	    if ( !classPath.equals("") ) {
		//tk =  new StringTokenizer(classPath,File.pathSeparator);
		tk =  new StringTokenizer(classPath,",");
		classPath = "";
	    }
	}
    }

    /**
     *  This function is taken from Alignment Server 
     */
    public static Set<String> implementations( String interfaceName, String cp ) {
	Set<String> list = new HashSet<String>();
		 
	try {
	    Class toclass = Class.forName(interfaceName);    
	    implementations( toclass, list, cp, false );
	} catch (ClassNotFoundException ex) {
	    System.err.println("Class "+interfaceName+" not found!");
	    //System.err.flush();
	    //System.err.close();
	}
			 
	return list;
    }
		 	
    // JE: Most of this should only be called once: at the beginning
    // And this should be pushed within OfflineAlign
    // (with the same interface as for the Online version)
    void offlineInit( boolean init ) {
	online = false;
	//methods.removeAll();
	serverAlignBox.removeAll();
	//localAlignBox.removeAll();
	//ontoBox1.removeAll();
	//ontoBox2.removeAll();
	
	Bundle bundle = AlignmentPlugin.getDefault().getBundle();
	
	Dictionary dic = bundle.getHeaders();
	Set<String> ms = new HashSet<String>();
	String classpath = dic.get("Bundle-ClassPath").toString();
	//System.out.println("classpath="+  classpath);
	
	URL url = FileLocator.find( AlignmentPlugin.getDefault().getBundle(), new Path(""), null );
	try {
	    url = FileLocator.resolve(url);
	}catch (IOException ioe){
	    ioe.printStackTrace();
	}
	
	//try {
	    //System.setErr( new PrintStream( new File( alignFolder.getAbsolutePath() + File.separator + "debug.txt") ) );
	    //System.err.println( "AlignmentPlugin bundle??? : "+AlignmentPlugin.getDefault().getBundle() );
	//System.err.println("url ="+  url.toString() );
	
	if( ! url.toString().startsWith("jar") ) {
	    
	    String[] files = classpath.split(",");
	    classpath = "";
	    for(int k=0; k < files.length; k++) {
		//System.err.println("files="+  files[k]);
		classpath += url.toString()+files[k]+",";
	    }
	    //System.err.println("classpath2="+  classpath);
	    ms = implementations( "org.semanticweb.owl.align.AlignmentProcess", classpath );
	} else { // Plugin is a jarfile
	    String path = url.getFile();
	    //To avoid "!/"
	    if( ! path.endsWith(".jar") ) path = path.substring( 0, path.length() - 2 );
	    ms = implementations( "org.semanticweb.owl.align.AlignmentProcess",  path);
	}
	ms.remove("fr.inrialpes.exmo.align.impl.DistanceAlignment"); // this one is abstract
	ms.remove("fr.inrialpes.exmo.align.ling.JWNLAlignment"); // requires WordNet
	System.err.println("ms="+ms.size());
	//System.err.flush();
	//System.err.close();
	methodList = new String[ms.size()];
	int j=0;
	for( String m : ms ) methodList[j++] = m;
	//methodList[0] = "fr.inrialpes.exmo.align.impl.method.NameEqAlignment";
	//methodList[1] = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
	//methodList[2] = "fr.inrialpes.exmo.align.impl.method.SMOANameAlignment";
	//methodList[3] = "fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment";
	//methodList[4] = "fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment";
	//methodList[5] = "fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment";
	//methodList[6] = "fr.inrialpes.exmo.align.impl.method.ClassStructAlignment";
	//methodList[7] = "fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment";
	
	if( methodList.length > 0 ) selectedMethod = methodList[0];
	methods.removeAll();
	methods.setItems( methodList );
	methods.setEnabled(true);
	methods.select(0);
	methods.redraw();
	//	}catch (IOException ioe){
	//	    ioe.printStackTrace();
	//System.err.flush();
	//System.err.close();
	//	}
			
	//System.out.println( "alignFolder=" + alignFolder );
			
	offlineAlign = new OfflineAlign( alignFolder, ontoFolder );
			
	//initButtons( false ); 
			
	if ( init ) offlineAlign.getAllAlignFromFiles();
	
	String[] list = offlineAlign.getAllAlign();
	localAlignBox.removeAll();
 		
	if ( list.length > 0) {
	    forUniqueness = new String[list.length];
	    for(int i=0; i< list.length; i++){
		//System.out.println( "filename=" + list[i] );
		File f = new File(list[i]);
		forUniqueness[i] = f.getName();
	    }
	    localAlignBox.setItems( list ); 
	    selectedLocalAlign = list[0];

	    localAlignBox.select(0);
	    
	    localAlignBox.setEnabled( true );
	    localImportButton.setEnabled( true );
	    localTrimButton.setEnabled( true );
	    
	} else {
	    //setButtons( 0 );
	    selectedLocalAlign = null;
	    
	    localAlignBox.setEnabled( false );
	    localImportButton.setEnabled( false );
	    localTrimButton.setEnabled( false );
	}

	findButton.setEnabled(false);
	findAllButton.setEnabled(false);
	    
	serverAlignBox.setEnabled( false );
	serverImportButton.setEnabled( false );
	serverTrimButton.setEnabled( false );
	storeButton.setEnabled( false );
	//fetchButton.setEnabled( false );
	uploadButton.setEnabled( false );
	    
	ontoBox1.setEnabled(true);
	ontoBox1.redraw();
	//onto1Refresh.setEnabled(true);
	//onto1Refresh.redraw();

	ontoBox2.setEnabled(true);
	ontoBox2.redraw();
	//onto2Refresh.setEnabled(true);
	//onto2Refresh.redraw();

	// JE-RR
	onlineButton.setEnabled( true );
	offlineButton.setEnabled( false );

	localAlignBox.redraw();
	ontoByProj = refreshOntoList( online );
    }
		 
    private HashMap<String,String> refreshOntoList( boolean online ) {
	// Find the list of ontologies
	HashMap<String,String> vec = new HashMap<String,String>();
	try {
	    String[] projects = OntologyProjectManager.getDefault().getOntologyProjects();
	    if ( projects != null ) {
		for ( int i=0; i < projects.length; i++ ) {	 
		    if ( projects[i]!=null ) {
			IOntologyProject ontoProject = OntologyProjectManager.getDefault().getOntologyProject( projects[i] );
			// With the new version (2.3.0, 240+), it is not necessary to filter... but nothing gets local
			// This change has been made with Andreas Harth
			/*
			  if( !online ) {	
			  Set<String> onto = ontoProject.getAvailableOntologyURIs();
			  URI[] strSet = ontoProject.getOntologyFiles();
			  //System.out.println("size of uris offline=" + strSet.length + "("+ projects[i] +")" );
			  for(int k=0; k < strSet.length; k++) {	
			  String st = strSet[k].toString();
			  if(st.startsWith("file:"))
			  vec.put(st,projects[i]);
			  }
			  } else {
			*/
			Set<String> strSet = ontoProject.getAvailableOntologyURIs();
			//System.out.println("size of uris online=" + strSet.size() + "("+ projects[i] +")");
			String[] uris = strSet.toArray( new String[0] );
			for ( int k=0; k < uris.length; k++ ) {
			    vec.put( uris[k], projects[i] );		
			}
			/*
			  }
			*/
		    }
		}
	    } else {
		//System.out.printf("No Ontology Project !" );
		return null;
	    }
	} catch ( Exception ex ) { ex.printStackTrace();};
	
	// Put it in the boxes
	
	String[] keys1 = vec.keySet().toArray(new String[0]);
	String[] keys2 = vec.keySet().toArray(new String[0]);
	
	if ( keys1.length > 0 ) {
	    //ontoList = new String[ keys.length ];
	    //ontoBox1.removeAll();
	    ontoBox1.setItems(keys1);
	    ontoBox1.select(0);
	    ontoBox1.redraw();
	    selectedOnto1 = keys1[0];
	    //ontoBox2.removeAll();
	    ontoBox2.setItems(keys2);
	    ontoBox2.select(0);
	    ontoBox2.redraw();
	    selectedOnto2 = keys2[0];
	}
			
	return vec;	 
    }

    @Override
	public void setFocus() {
	// TODO Auto-generated method stub
    }
    public void widgetDefaultSelected(SelectionEvent e) {
	// TODO Auto-generated method stub
    }	
}
