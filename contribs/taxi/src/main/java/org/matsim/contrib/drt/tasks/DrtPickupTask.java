/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.contrib.drt.tasks;

import java.util.HashSet;

import org.matsim.contrib.drt.DrtRequest;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;

public class DrtPickupTask extends StayTaskImpl implements DrtTaskWithRequests {
	private final DrtRequest request;

	public DrtPickupTask(double beginTime, double endTime, DrtRequest request) {
		super(beginTime, endTime, request.getFromLink());

		this.request = request;
		request.setPickupTask(this);
	}

	@Override
	public DrtTaskType getDrtTaskType() {
		return DrtTaskType.PICKUP;
	}

	public DrtRequest getRequest() {
		return request;
	}

	@Override
	protected String commonToString() {
		return "[" + getDrtTaskType().name() + "]" + super.commonToString();
	}

	@Override
	public HashSet<DrtRequest> getRequests() {
		HashSet<DrtRequest> t = new HashSet<>();
		t.add(request);
		return t;
	}

	@Override
	public void removeFromRequest(DrtRequest request) {
		if (request != this.request) {
			throw new IllegalStateException();
		}
		request.setPickupTask(null);

	}

	@Override
	public void removeFromAllRequests() {
		removeFromRequest(this.request);
	}
}
