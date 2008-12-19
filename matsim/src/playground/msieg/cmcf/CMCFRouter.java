/* *********************************************************************** *
 * project: org.matsim.*
 * CMCFRouter.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.msieg.cmcf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.matsim.gbl.Gbl;
import org.matsim.network.Link;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.network.Node;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.Population;

import playground.dgrether.utils.MatsimIo;
import playground.msieg.structure.HashPathFlow;
import playground.msieg.structure.PathFlow;

public abstract class CMCFRouter {

	private final String networkFile, plansFile, cmcfFile;
	
	protected NetworkLayer network;
	protected Population population;
	protected PathFlow<Node, Link> pathFlow;
	
	
	public CMCFRouter(String networkFile, String plansFile, String cmcfFile) {
		super();
		this.networkFile = networkFile;
		this.plansFile = plansFile;
		this.cmcfFile = cmcfFile;
	}

	public void loadEverything(){
		this.loadNetwork();
		this.loadPopulation();
		try {
			this.loadCMCFSolution();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void loadNetwork(){
		this.network = (NetworkLayer) Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE, null);
		new MatsimNetworkReader(network).readFile(networkFile);
	}
	
	protected void loadPopulation(){
		this.population = new Population(Population.NO_STREAMING);
		new MatsimPopulationReader(this.population, this.network).readFile(plansFile);
	}
	
	protected void loadCMCFSolution() throws NumberFormatException, IOException{
		this.pathFlow = new HashPathFlow<Node, Link>();
		BufferedReader in = new BufferedReader(new FileReader(this.cmcfFile));
		String line = null;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			/***
			 * Example of a line which has to be extracted:
			 * Flow 7.5 on path 1: 2 -> 12 (15000, 2): 7 16
			 */
			if(line.startsWith("Flow")){
				line = line.substring(5);
				Double flow = new Double(line.substring(0, line.indexOf(' ')));
				line = line.substring(line.indexOf(':')+2);
				String fromID = line.substring(0, line.indexOf(" ->"));
				line = line.substring(line.indexOf("-> ")+3);
				String toID = line.substring(0,line.indexOf(" ("));
				String pathString = line.substring(line.indexOf("): ")+3).trim();

				List<Link> path = new LinkedList<Link>();
				StringTokenizer st = new StringTokenizer(pathString);
				while(st.hasMoreTokens()){
					path.add(this.network.getLink(st.nextToken()));
				}
				pathFlow.add(this.network.getNode(fromID), this.network.getNode(toID), path, flow);
			}
		}
	}
	
	public void writePlans(String outPlansFile){
		MatsimIo.writePlans(this.population, outPlansFile);
	}
	
	abstract public void route();
}

