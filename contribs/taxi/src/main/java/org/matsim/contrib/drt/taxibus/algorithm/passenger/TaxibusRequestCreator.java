/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.contrib.drt.taxibus.algorithm.passenger;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.DrtRequest;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.passenger.*;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

public class TaxibusRequestCreator implements PassengerRequestCreator {
	@Override
	public PassengerRequest createRequest(Id<Request> id, MobsimPassengerAgent passenger, Link fromLink, Link toLink,
			double t0, double t1, double now) {
		return new DrtRequest(id, passenger, fromLink, toLink, t0, now);
	}
}
