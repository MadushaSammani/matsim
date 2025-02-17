/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package playground.jbischoff.drt.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.av.robotaxi.scoring.TaxiFareConfigGroup;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.file.VehicleReader;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.contrib.taxi.run.TaxiConfigGroup;
import org.matsim.contrib.taxi.run.TaxiOptimizerModules;
import org.matsim.contrib.taxi.run.TaxiOutputModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import com.google.inject.Binder;
import com.google.inject.name.Names;

import playground.jbischoff.drt.config.DRTConfigGroup;
import playground.jbischoff.drt.routingModule.StopBasedDRTRoutingModule;

/**
 * This class runs an example robotaxi scenario including scoring. The
 * simulation runs for 10 iterations, this takes quite a bit time (25 minutes or
 * so). You may switch on OTFVis visualisation in the main method below.
 * The scenario should run out of the box without any additional files.
 * If required, you may find all input files in the resource path 
 * or in the jar maven has downloaded).
 * There are two vehicle files: 2000 vehicles and 5000, which may be set in the config.
 * Different fleet sizes can be created using {@link org.matsim.contrib.CreateSharedTaxiVehicles.vehicles.CreateTaxiVehicles}
 * 
 * 
 */
public class RunTaxiWithStopsExample {

	public static void main(String[] args) {
		String configFile = "C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/input/configPM120.10.xml";
		RunTaxiWithStopsExample.run(configFile, false);
	}

	public static void run(String configFile, boolean otfvis) {
		Config config = ConfigUtils.loadConfig(configFile, new DvrpConfigGroup(), new TaxiConfigGroup(),
				new OTFVisConfigGroup(), new TaxiFareConfigGroup());
		
		DRTConfigGroup drt = new DRTConfigGroup();
		drt.setEstimatedBeelineDistanceFactor(1.3);
		drt.setEstimatedSpeed(30/3.6);
		drt.setMaximumWalkDistance(500);
		drt.setOperationalScheme("stationbased");
		config.addModule(drt);
		
		createControler(config, otfvis).run();
	}

	public static Controler createControler(Config config, boolean otfvis) {


		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		FleetImpl fleet = new FleetImpl();
		new VehicleReader(scenario.getNetwork(), fleet).parse(TaxiConfigGroup.get(config).getTaxisFileUrl(config.getContext()));

		Controler controler = new Controler(scenario);
			controler.addOverridingModule(new TaxiOutputModule());

		Scenario scenario2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new TransitScheduleReader(scenario2).readFile("C:/Users/Joschka/Documents/shared-svn/projects/vw_rufbus/projekt2/input/network/stopsWRS_300m.xml");
		
		controler.addOverridingModule(TaxiOptimizerModules.createDefaultModule(fleet));
        controler.addOverridingModule(new AbstractModule() {
					
			@Override
			public void install() {
				bind(TransitSchedule.class).annotatedWith(Names.named(DRTConfigGroup.DRTMODE)).toInstance(scenario2.getTransitSchedule());;
				addRoutingModuleBinding(DRTConfigGroup.DRTMODE).to(StopBasedDRTRoutingModule.class).asEagerSingleton();
				
			}
		});

		if (otfvis) {
			controler.addOverridingModule(new OTFVisLiveModule());
		}

		return controler;
	}

}
