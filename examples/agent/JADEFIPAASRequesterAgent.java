//package fr.inrialpes.exmo.align.service.jade;

import java.util.Iterator;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

public class JADEFIPAASRequesterAgent extends GuiAgent{

	transient protected JADEFIPAASRequesterAgentGUI myGui;  // The gui

	public static final int WAIT = -1;
	public static final int QUIT = 0;
	private int command = WAIT;

//	Event

	private static final int LOAD=1;
	private static final int ALIGN=2;
	private static final int RETRIEVE=3;
	private static final int STORE=4;
	private static final int FIND=5;
	private static final int METADATA=6;
	private static final int TRANSLATE=7;
	private static final int CUT=8;

	private AID AlignementAgent=null;

	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	protected void setup() {
//		------------------------

		// Register language and ontology

		// Set up the gui
		myGui = new JADEFIPAASRequesterAgentGUI(this);
		myGui.setVisible(true);



		//TODO test align
		//********************************* Alignment-service
		DFAgentDescription template2 = new DFAgentDescription();
		ServiceDescription sd2 = new ServiceDescription();
		sd2.setType("Alignment-service");
		template2.addServices(sd2);


		try {
			DFAgentDescription[] result = DFService.search(this, template2);
			for (int i = 0; i < result.length; ++i) {
				if(!(result[i].getName().equals(this.getAID())) ){
					System.out.println( "TEST---------- "+result[i].getName().getName());
					AlignementAgent = result[i].getName();
				}
			}
		}catch (FIPAException fe) {
			myLogger.log(Logger.WARNING, this.getName() + " : Error searching in the DF.", fe);
		}


		/**
		 * 
		 ACLMessage test =new ACLMessage(ACLMessage.REQUEST);
		//test.setContent("LOAD::http://localhost:8089/1170260746023/500");
		test.setContent("LOAD::file:///c:/ftrd/model/scenar/align.rdf");
		test.addReceiver(AlignementAgent);
		this.send(test);
		 **/

		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					myGui.showReply(msg.getContent());
				}
				else {
					block();
				}
			}
		});//end of CyclicBehaviour



	}

	protected void takeDown() {
//		---------------------------  Terminate the program properly

		System.out.println(getLocalName() + " is now shutting down.");
		if (myGui!=null) {
			myGui.setVisible(false);
			myGui.dispose();
		}
	}

	protected void onGuiEvent(GuiEvent ev) {
//		----------------------------------------  Receive user command via the gui

		ACLMessage toSend =new ACLMessage(ACLMessage.REQUEST);
		toSend.addReceiver(AlignementAgent);
		command = ev.getType();
		toSend = setPerformative(toSend, command);
		for(Iterator iter=ev.getAllParameter();iter.hasNext();){
			//toSend=addArgument(toSend, (String)ev.getParameter(i));
			String next=(String)iter.next();

			if (!(next.endsWith("="))){
				toSend=addArgument(toSend,next);
			}
		}
		myLogger.log(Logger.INFO, this.getLocalName() + "send : "+ toSend.getContent());
		this.send(toSend);
	}

	void alertGui(Object response) {
//		--------------------------------  Process the response of the server
//		to the gui for display
		myGui.alertResponse(response);



	}

	void resetStatusGui() {
//		-----------------------  Reset the status of the gui
		//   myGui.resetStatus();
	}

	private ACLMessage setPerformative(ACLMessage mess, int command){
		ACLMessage toreturn = mess;
		switch (command){
		//LOAD
		case LOAD :
			mess.setContent("LOAD::");
			break;
			//ALIGN
		case ALIGN :
			mess.setContent("ALIGN::");
			break;
			//RETRIEVE	
		case RETRIEVE :
			mess.setContent("RETRIEVE::");
			break;
			//STORE	
		case STORE :
			mess.setContent("STORE::");
			break;
			//FIND	
		case FIND :
			mess.setContent("FIND::");
			break;
			//CUT	
		case CUT :
			mess.setContent("CUT::");
			break;
			//TODO ! METADATA & TRANSLATE
		}	
		return toreturn;
	}

	private ACLMessage addArgument(ACLMessage mess, String arg){
		ACLMessage toReturn = mess;
		String ancien;
		ancien = mess.getContent();
		mess.setContent(ancien+arg+"::");
		return toReturn;
	}




}
