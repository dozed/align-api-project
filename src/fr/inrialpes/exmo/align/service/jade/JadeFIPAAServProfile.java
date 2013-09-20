/*
 * $Id: JadeFIPAAServProfile.java 1323 2010-03-10 10:54:28Z euzenat $
 *
 * Copyright (C) Orange R&D, 2006-2007
 * Copyright (C) INRIA, 2006-2007, 2009-2010
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service.jade;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.Logger;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import fr.inrialpes.exmo.align.service.AServException;
import fr.inrialpes.exmo.align.service.AServProtocolManager;
import fr.inrialpes.exmo.align.service.AlignmentServiceProfile;

import java.io.File;
import java.util.Properties;

public class JadeFIPAAServProfile implements AlignmentServiceProfile {


	private AgentContainer mc;
	private AgentController algagentcontroller;
	private Logger myLogger = Logger.getMyLogger(getClass().getName());

	public void init( Properties params, AServProtocolManager manager ) throws AServException {
		int port = 8888;
		int debug = 0;
		Object args[] = new Object[2];
		
		//set up the manager as an argument to pass to the JADEFIPAAServiceAgent
		args[0]=manager;
		
		// set up the Parameters as an argument to pass to the JADEFIPAServiceAgent
		args[1]=params;
		
		if ( params.getProperty( "jade" ) != null )
			port = Integer.parseInt( params.getProperty( "jade" ) );
		if ( params.getProperty( "debug" ) != null )
		    debug = Integer.parseInt( params.getProperty( "debug" ) ) - 1;

		/**		
	Properties props = new Properties();
		 **/
		try {
			// Get a hold on JADE runtime
			Runtime rt = Runtime.instance();

			// Exit the JVM when there are no more containers around
			rt.setCloseVM(true);

			/** Profile with no MTP( Message Transfer Protocol
		props.setProperty("nomtp", "true");
		Profile pMain = new ProfileImpl(props);
			 **/
			// create a default Profile
			Profile pMain = new ProfileImpl(null, port, null);

			if ( debug > 0 )
				System.out.println("Launching a whole in-process platform..."+pMain);
			mc = rt.createMainContainer(pMain);
			algagentcontroller = mc.createNewAgent("JadeFIPAAServiceAgent", JadeFIPAAServiceAgent.class.getName(), args);
			algagentcontroller.start();
		}
		catch(Exception e) {
			throw new AServException ( "Cannot launch Jade Server" , e );
		}
	}

	public void close(){
	    try{
		algagentcontroller.kill();
		mc.kill();
		myLogger.log(Logger.INFO, "Agent Alignement close");
	    } catch (ControllerException e) {
		myLogger.log(Logger.WARNING, "Error killing the alignment agent."); }
	    try {
		// Destroy the files please (JE)
		new File("APDescription.txt").delete();
		new File("MTPs-Main-Container.txt").delete();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	
}
