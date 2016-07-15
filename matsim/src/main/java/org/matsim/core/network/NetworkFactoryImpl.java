/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008, 2011 by the members listed in the COPYING,  *
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

package org.matsim.core.network;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.utils.geometry.CoordUtils;

/**
 * @author dgrether
 * @author mrieser
 */
public final class NetworkFactoryImpl implements NetworkFactory {

	@SuppressWarnings("unused")
	private final static Logger log = Logger.getLogger(NetworkFactoryImpl.class);

	private LinkFactory linkFactory = null;
	

	private NetworkChangeEventFactory networkChangeEventFactory = new NetworkChangeEventFactoryImpl();

	private final Network network;

	private NetworkFactoryImpl(final Network network) {
		this.network = network;
		this.linkFactory = new LinkFactoryImpl();
	}

	@Override
	public Node createNode(final Id<Node> id, final Coord coord) {
		Node node = NetworkUtils.createNode(id);
		node.setCoord(coord) ;
		return node ;
	}

	@Override
	public Link createLink(Id<Link> id, Node fromNode, Node toNode) {
		return this.linkFactory.createLink(id, fromNode, toNode, 
				this.network, CoordUtils.calcEuclideanDistance(fromNode.getCoord(), toNode.getCoord()), 1.0, 1.0, 1.0);
	}

	/**
	 * @param time the time when the NetworkChangeEvent occurs
	 * @return a new NetworkChangeEvent
	 *
	 * @see #setNetworkChangeEventFactory(NetworkChangeEventFactory)
	 */
	@Override
	public NetworkChangeEvent createNetworkChangeEvent(double time) {
		return this.networkChangeEventFactory.createNetworkChangeEvent(time);
	}
	@Override
	public void setLinkFactory(final LinkFactory factory) {
		this.linkFactory = factory;
	}
	@Override
	public void setNetworkChangeEventFactory(NetworkChangeEventFactory networkChangeEventFactory) {
		this.networkChangeEventFactory = networkChangeEventFactory;
	}
}
