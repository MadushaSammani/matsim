/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package org.matsim.contrib.noise.examples;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.noise.NoiseOfflineCalculation;
import org.matsim.contrib.noise.NoiseConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * (1) Computes noise emissions, immissions, person activities and damages based on a standard events file.
 * (2) Optionally throws noise immission damage events for the causing agent and the affected agent.
 * 
 * @author ikaddoura
 *
 */
public class NoiseOfflineCalculationExample {
	
	private static String runDirectory = "pathTo/RunDirectory/";
	private static String outputDirectory = "pathTo/analysis-output-directory/";
	private static int lastIteration = 100;
				
	public static void main(String[] args) {
	
		Config config = ConfigUtils.createConfig(new NoiseConfigGroup());
		config.network().setInputFile(runDirectory + "output_network.xml.gz");
		config.plans().setInputFile(runDirectory + "output_plans.xml.gz");
		config.controler().setOutputDirectory(runDirectory);
		config.controler().setLastIteration(lastIteration);		
						
		// adjust the default noise parameters
		NoiseConfigGroup noiseParameters = (NoiseConfigGroup) config.getModule("noise");
		noiseParameters.setReceiverPointGap(12345789.);
		// ...
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		NoiseOfflineCalculation noiseCalculation = new NoiseOfflineCalculation(scenario, outputDirectory);
		noiseCalculation.run();		
	}
}
		

