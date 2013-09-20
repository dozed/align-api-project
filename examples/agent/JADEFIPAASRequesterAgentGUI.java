//package fr.inrialpes.exmo.align.service.jade;

import jade.gui.GuiEvent;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class JADEFIPAASRequesterAgentGUI  extends  javax.swing.JFrame implements ActionListener{


	final static int IN_PROCESS = 0;
	final static int WAIT_CONFIRM = 1;
	final static int IN_LINE = 2;
	private int status = IN_PROCESS;

	private JADEFIPAASRequesterAgent myAgent;

//	Variables declaration - do not modify
	private JPanel LOADPane;
	private JPanel ALIGNPane;
	private JPanel RETRIEVEPane;
	private JPanel STOREPane;
	private JPanel FINDPane;
	private JPanel METADATAPane;
	private JPanel TRANSLATEPane;
	private JPanel CUTPane;
	private JPanel ReplyPane;
	private JPanel main;
	
	private JTextArea reply;
	private JScrollPane scrollReply;
	private JTabbedPane onglets;
	 
	//LOAD PANE

	private JLabel  LOADURLAlignmentLabel;
	private JTextField LOADURLAlignmentValue;
	private JLabel  LOADURLInformationLabel;
	private JPanel LOADURLAlignmentPane;

	//ALIGN PANE

	private JLabel  ALIGNDURLOnto1Label;
	private JTextField ALIGNURLOnto1Value;
	private JLabel  ALIGNDURLOnto2Label;
	private JTextField ALIGNURLOnto2Value;
	private JLabel  ALIGNMethodLabel;
	private JTextField ALIGNMethodValue;
	private JLabel  ALIGNBaseAlignmentURLIDLabel;
	private JTextField ALIGNBaseAlignmentURLIDValue;
	private JPanel ALIGNURLOnto1Pane;
	private JPanel ALIGNURLOnto2Pane;
	private JPanel ALIGNMethodPane;
	private JPanel ALIGNBaseAlignmentPane;
	private JLabel  ALIGNURLInformationLabel;

	//RETRIEVE PANE
	private JLabel  RETRIEVEAlignmentURLIDLabel;
	private JTextField RETRIEVEAlignmentURLIDValue;
	private JLabel  RETRIEVERenderingMethodLabel;
	private JTextField RETRIEVERenderingMethodValue;
	private JPanel RETRIEVEAlignmentPane;
	private JPanel RETRIEVERenderingMethodPane;

	// STORE PANE
	private JLabel  STOREAlignmentURLIDLabel;
	private JTextField STOREAlignmentURLIDValue;
	private JPanel STOREAlignmentPane;

	// FIND PANE
	private JLabel  FINDURLOnto1Label;
	private JTextField FINDURLOnto1Value;
	private JLabel  FINDURLOnto2Label;
	private JTextField FINDURLOnto2Value;
	private JPanel FINDOnto1Pane;
	private JPanel FINDOnto2Pane;
	private JLabel  FINDURLInformationLabel;

	//	CUT PANE
	private JLabel  CUTAlignmentURLIDLabel;
	private JTextField CUTAlignmentURLIDValue;
	private JLabel  CUTMethodLabel;
	private JTextField CUTMethodValue;
	private JLabel  CUTThresholdLabel;
	private JTextField CUTThresholdValue;
	private JPanel CUTAlignmentPane;
	private JPanel CUTMethodPane;
	private JPanel CUTThresholdPane;

	//METADATA PANE

	//TRANSLATE PANE


	private JButton OKButton;

	private static final String OK = "_OK";
	private static final String Open = "_Open";

	//Event

	private static final int LOAD=1;
	private static final int ALIGN=2;
	private static final int RETRIEVE=3;
	private static final int STORE=4;
	private static final int FIND=5;
	private static final int METADATA=6;
	private static final int TRANSLATE=7;
	private static final int CUT=8;


	public JADEFIPAASRequesterAgentGUI(JADEFIPAASRequesterAgent a) {
//		----------------------------------------  Constructor

		myAgent = a;      // Reference to class BankClientAgent
		setTitle("Alignment Server Requester Test Agent - " + myAgent.getLocalName());

		LOADPane = new JPanel();
		ALIGNPane = new JPanel();
		RETRIEVEPane = new JPanel();
		STOREPane = new JPanel();
		FINDPane = new JPanel();
		METADATAPane = new JPanel();
		TRANSLATEPane = new JPanel();
		CUTPane = new JPanel();
		ReplyPane = new JPanel();
		reply = new JTextArea(20,50);
		reply.setEditable(false);
		scrollReply = new JScrollPane(reply,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.PAGE_AXIS));

		LOADPane.setLayout(new BoxLayout(LOADPane, BoxLayout.PAGE_AXIS));
		ALIGNPane.setLayout(new BoxLayout(ALIGNPane, BoxLayout.PAGE_AXIS));
		STOREPane.setLayout(new BoxLayout(STOREPane, BoxLayout.PAGE_AXIS));
		FINDPane.setLayout(new BoxLayout(FINDPane, BoxLayout.PAGE_AXIS));
		RETRIEVEPane.setLayout(new BoxLayout(RETRIEVEPane, BoxLayout.PAGE_AXIS));
		METADATAPane.setLayout(new BoxLayout(METADATAPane, BoxLayout.PAGE_AXIS));
		TRANSLATEPane.setLayout(new BoxLayout(TRANSLATEPane, BoxLayout.PAGE_AXIS));
		CUTPane.setLayout(new BoxLayout(CUTPane, BoxLayout.PAGE_AXIS));

		onglets = new JTabbedPane();


//		____________________
//		LOAD

		LOADURLAlignmentLabel = new JLabel ("Alignment URL");
		LOADURLAlignmentValue = new JTextField(30);
		LOADURLInformationLabel =new JLabel("This is the URL of the place where to find this alignment. It must be reachable by the server (i.e., file:// URI is acceptable if it is on the server).");

		LOADURLAlignmentPane = new JPanel();
		LOADURLAlignmentPane.add(LOADURLAlignmentLabel);
		LOADURLAlignmentPane.add(LOADURLAlignmentValue);
		LOADPane.add(LOADURLAlignmentPane);
		LOADPane.add(LOADURLInformationLabel);

//		-------
//		ALIGN

		ALIGNDURLOnto1Label = new JLabel("Ontology 1");
		ALIGNURLOnto1Value = new JTextField(30);
		ALIGNDURLOnto2Label = new JLabel("Ontology 2");
		ALIGNURLOnto2Value = new JTextField(30);
		ALIGNMethodLabel  = new JLabel("Method");;
		ALIGNMethodValue= new JTextField(30);
		ALIGNBaseAlignmentURLIDLabel = new JLabel("Initial alignment id");
		ALIGNBaseAlignmentURLIDValue = new JTextField(30);
		ALIGNURLInformationLabel = new JLabel("These are the URL of places where to find these ontologies. They must be reachable by the server (i.e., file:// URI are acceptable if they are on the server)");

		ALIGNURLOnto1Pane = new JPanel();
		ALIGNURLOnto2Pane = new JPanel();
		ALIGNMethodPane = new JPanel();
		ALIGNBaseAlignmentPane = new JPanel();


		ALIGNURLOnto1Pane.add(ALIGNDURLOnto1Label);
		ALIGNURLOnto1Pane.add(ALIGNURLOnto1Value);
		ALIGNURLOnto2Pane.add(ALIGNDURLOnto2Label);
		ALIGNURLOnto2Pane.add(ALIGNURLOnto2Value);
		ALIGNMethodPane.add(ALIGNMethodLabel);
		ALIGNMethodPane.add(ALIGNMethodValue);
		ALIGNBaseAlignmentPane.add(ALIGNBaseAlignmentURLIDLabel);
		ALIGNBaseAlignmentPane.add(ALIGNBaseAlignmentURLIDValue);

		ALIGNPane.add(ALIGNURLOnto1Pane);
		ALIGNPane.add(ALIGNURLOnto2Pane);
		ALIGNPane.add(ALIGNMethodPane);
		ALIGNPane.add(ALIGNBaseAlignmentPane);
		ALIGNPane.add(ALIGNURLInformationLabel);

//		----------------		
//		RETRIEVE		
		RETRIEVEAlignmentURLIDLabel = new JLabel("Alignment id");
		RETRIEVEAlignmentURLIDValue = new JTextField(30);
		RETRIEVERenderingMethodLabel = new JLabel("Rendering");;
		RETRIEVERenderingMethodValue = new JTextField(30);;

		RETRIEVEAlignmentPane = new JPanel();
		RETRIEVERenderingMethodPane = new JPanel();
		RETRIEVEAlignmentPane.add(RETRIEVEAlignmentURLIDLabel);
		RETRIEVEAlignmentPane.add(RETRIEVEAlignmentURLIDValue);
		RETRIEVERenderingMethodPane.add(RETRIEVERenderingMethodLabel);
		RETRIEVERenderingMethodPane.add(RETRIEVERenderingMethodValue);
		RETRIEVEPane.add(RETRIEVEAlignmentPane);
		RETRIEVEPane.add(RETRIEVERenderingMethodPane);

//		----------------				
//		STORE PANE
		STOREAlignmentURLIDLabel =  new JLabel("Alignment id");
		STOREAlignmentURLIDValue = new JTextField(30);

		STOREAlignmentPane = new JPanel();
		STOREAlignmentPane.add(STOREAlignmentURLIDLabel);
		STOREAlignmentPane.add(STOREAlignmentURLIDValue);
		STOREPane.add(STOREAlignmentPane);

//		----------------				
//		FIND PANE
		FINDURLOnto1Label = new JLabel("Ontology 1");
		FINDURLOnto1Value = new JTextField(30);
		FINDURLOnto2Label = new JLabel("Ontology 2");;
		FINDURLOnto2Value = new JTextField(30);
		FINDURLInformationLabel = new JLabel("These are the URI identifying the ontologies. Not those of places where to upload them");

		FINDOnto1Pane = new JPanel();
		FINDOnto2Pane = new JPanel();
		FINDOnto1Pane.add(FINDURLOnto1Label);
		FINDOnto1Pane.add(FINDURLOnto1Value);
		FINDOnto2Pane.add(FINDURLOnto2Label);
		FINDOnto2Pane.add(FINDURLOnto2Value);
		FINDPane.add(FINDOnto1Pane);
		FINDPane.add(FINDOnto2Pane);
		FINDPane.add(FINDURLInformationLabel);

//		----------------		
//		CUT		
		CUTAlignmentURLIDLabel = new JLabel("Alignment id");
		CUTAlignmentURLIDValue = new JTextField(30);
		CUTMethodLabel = new JLabel("Method");
		CUTMethodValue = new JTextField(30);
		CUTThresholdLabel = new JLabel("Threshold");
		CUTThresholdValue = new JTextField(30);

		CUTThresholdPane = new JPanel();
		CUTAlignmentPane = new JPanel();
		CUTMethodPane = new JPanel();
		CUTAlignmentPane.add(CUTAlignmentURLIDLabel);
		CUTAlignmentPane.add(CUTAlignmentURLIDValue);
		CUTMethodPane.add(CUTMethodLabel);
		CUTMethodPane.add(CUTMethodValue);
		CUTThresholdPane.add(CUTThresholdLabel);
		CUTThresholdPane.add(CUTThresholdValue);
		CUTPane.add(CUTAlignmentPane);
		CUTPane.add(CUTMethodPane);
		CUTPane.add(CUTThresholdPane);

//		----------------					
		//METADATA PANE

		//TRANSLATE PANE


//		---------------		

		onglets.addTab( "Load", LOADPane);
		onglets.addTab("Align", ALIGNPane);
		onglets.addTab("Retrieve", RETRIEVEPane);
		onglets.addTab("Store", STOREPane);
		onglets.addTab("Find", FINDPane);
		onglets.addTab("Metadata", METADATAPane);
		onglets.addTab("Translate", TRANSLATEPane);
		onglets.addTab("Cut", CUTPane);
		//ReplyPane.add(reply);
		ReplyPane.add(scrollReply);

		main.add(onglets);		
		main.add(ReplyPane);
		OKButton = new JButton("Send");	
		OKButton.setActionCommand(OK);
		OKButton.addActionListener(this);    
		main.add(OKButton);	
		getContentPane().add(main, java.awt.BorderLayout.CENTER);
		pack();
		// L,H
		setSize(850, 450);
		Rectangle r = getGraphicsConfiguration().getBounds();
		setLocation(r.x + (r.width - getWidth())/2,
				r.y + (r.height - getHeight())/2);
	}


	public void actionPerformed(ActionEvent ae) {
		int value=-1;
		int ongletActif;
		if (ae.getSource() == OKButton) {
			ongletActif=onglets.getSelectedIndex()+1;
			GuiEvent ge=new GuiEvent (this, ongletActif);
			switch (ongletActif){
			//LOAD
			case LOAD :
				ge.addParameter("url="+LOADURLAlignmentValue.getText());
				ge.addParameter("id="+LOADURLAlignmentValue.getText());
				break;
				//ALIGN
			case ALIGN :
				ge.addParameter("onto1="+ALIGNURLOnto1Value.getText());
				ge.addParameter("onto2="+ALIGNURLOnto2Value.getText());
				ge.addParameter("id="+ALIGNBaseAlignmentURLIDValue.getText());
				ge.addParameter("method="+ALIGNMethodValue.getText());
				break;
				//RETRIEVE	
			case RETRIEVE :
				ge.addParameter("id="+RETRIEVEAlignmentURLIDValue.getText());
				ge.addParameter("method="+RETRIEVERenderingMethodValue.getText());
				break;
				//STORE	
			case STORE :
				ge.addParameter("id="+STOREAlignmentURLIDValue.getText());
				break;
				//FIND	
			case FIND :
				ge.addParameter("onto1="+FINDURLOnto1Value.getText());
				ge.addParameter("onto2="+FINDURLOnto1Value.getText());
				break;
				//CUT	
			case CUT :
				ge.addParameter("id="+CUTAlignmentURLIDValue.getText());
				ge.addParameter("method="+CUTMethodValue.getText());
				ge.addParameter("threshold="+CUTThresholdValue.getText());
				break;
				//TODO ! METADATA & TRANSLATE
			}


			myAgent.postGuiEvent(ge);
		}else{
			//???
		}
		cleanGUI();
	}

	void shutDown() {
//		-----------------  Control the closing of this gui

		int rep = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
				myAgent.getLocalName(),
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (rep == JOptionPane.YES_OPTION) {
			GuiEvent ge = new GuiEvent(this, myAgent.QUIT);
			myAgent.postGuiEvent(ge);
		}
	}

	void alertInfo(String s) {
//		--------------------------

		Toolkit.getDefaultToolkit().beep();
//		msg.setText(s);
	}

	public void alertResponse(Object o) {
//		-------------------------------------

	}


	private void cleanGUI(){

		reply.setText("");
		LOADURLAlignmentValue.setText("");
		ALIGNURLOnto1Value.setText("");
		ALIGNURLOnto2Value.setText("");
		ALIGNMethodValue.setText("");
		ALIGNBaseAlignmentURLIDValue.setText("");
		RETRIEVEAlignmentURLIDValue.setText("");
		RETRIEVERenderingMethodValue.setText("");
		STOREAlignmentURLIDValue.setText("");
		FINDURLOnto1Value.setText("");
		FINDURLOnto2Value.setText("");
		CUTAlignmentURLIDValue.setText("");
		CUTMethodValue.setText("");
		CUTThresholdValue.setText("");

	}

	public void showReply(String toshow){
		reply.setText(toshow);
	}



}
