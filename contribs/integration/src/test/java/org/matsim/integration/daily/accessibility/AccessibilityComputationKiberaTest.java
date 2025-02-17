/* *********************************************************************** *
 * project: org.matsim.*												   *
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
package org.matsim.integration.daily.accessibility;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.accessibility.AccessibilityConfigGroup;
import org.matsim.contrib.accessibility.AccessibilityModule;
import org.matsim.contrib.accessibility.FacilityTypes;
import org.matsim.contrib.accessibility.Modes4Accessibility;
import org.matsim.contrib.accessibility.utils.AccessibilityUtils;
import org.matsim.contrib.accessibility.utils.VisualizationUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.testcases.MatsimTestUtils;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author dziemke
 */
public class AccessibilityComputationKiberaTest {
	public static final Logger LOG = Logger.getLogger(AccessibilityComputationKiberaTest.class);
	
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

//	@Test
//	public void testQuick() {
//		run(1000., false, false);
//	}
//	@Test
//	public void testLocal() {
//		run(10., false, true);
//	}
	@Test
	public void testOnServer() throws IOException, InterruptedException{
		run(100., true, false);
	}
	
	public void run(Double cellSize, boolean push2Geoserver, boolean createQGisOutput) throws IOException, InterruptedException {

		final Config config = ConfigUtils.createConfig(new AccessibilityConfigGroup());
		Envelope envelope = new Envelope(252000, 256000, 9854000, 9856000);
		String scenarioCRS = "EPSG:21037"; // = Arc 1960 / UTM zone 37S, for Nairobi, Kenya
				
		// Input (if pre-downloaded)
//		String folderStructure = "../../";
//		String networkFile = "matsimExamples/countries/ke/kibera/2015-11-05_network_paths_detailed.xml";
//		// Adapt folder structure that may be different on different machines, in particular on server
//		folderStructure = PathUtils.tryANumberOfFolderStructures(folderStructure, networkFile);
//		config.network().setInputFile(folderStructure + networkFile);
//		config.facilities().setInputFile(folderStructure + "matsimExamples/countries/ke/kibera/2015-11-05_facilities.xml");
	
		// Input (directly from OSM)
		CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(scenarioCRS, "EPSG:4326");
		Coord southwest = transformation.transform(new Coord(envelope.getMinX(), envelope.getMinY()));
		Coord northeast = transformation.transform(new Coord(envelope.getMaxX(), envelope.getMaxY()));
		URL osm = new URL("http://api.openstreetmap.org/api/0.6/map?bbox=" + southwest.getX() + "," + southwest.getY() + "," + northeast.getX() + "," + northeast.getY());
		HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
		HttpURLConnection connection2 = (HttpURLConnection) osm.openConnection(); // TODO Might be more elegant without downloading this twice
		
	    Network network = CreateNetwork.createNetwork(connection.getInputStream(), scenarioCRS);
	    
	    double buildingTypeFromVicinityRange = 0.;
		ActivityFacilities facilities = RunCombinedOsmReaderKibera.createFacilites(connection2.getInputStream(), scenarioCRS, buildingTypeFromVicinityRange);
		
		config.global().setCoordinateSystem(scenarioCRS);
		
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory(utils.getOutputDirectory());
		config.controler().setLastIteration(0);
		config.controler().setRunId("ke_kibera_" + AccessibilityUtils.getDate() + "_" + cellSize.toString().split("\\.")[0]);
		
		AccessibilityConfigGroup acg = ConfigUtils.addOrGetModule(config, AccessibilityConfigGroup.class);
		acg.setCellSizeCellBasedAccessibility(cellSize.intValue());
		acg.setEnvelope(new Envelope(252000, 256000, 9854000, 9856000));
		acg.setComputingAccessibilityForMode(Modes4Accessibility.walk, true);
		acg.setComputingAccessibilityForMode(Modes4Accessibility.freespeed, false);
		acg.setOutputCrs("EPSG:21037"); // = Arc 1960 / UTM zone 37S, for Nairobi, Kenya
		
		ConfigUtils.setVspDefaults(config);
		
//		final Scenario scenario = ScenarioUtils.loadScenario(config);
		MutableScenario scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
		scenario.setNetwork(network);
		scenario.setActivityFacilities(facilities);
		
		// Activity types
		final List<String> activityTypes = Arrays.asList(new String[]{FacilityTypes.DRINKING_WATER, FacilityTypes.CLINIC});
		LOG.info("Using activity types: " + activityTypes);
		
		// Network density points (as proxy for population density)
		final ActivityFacilities densityFacilities = AccessibilityUtils.createFacilityForEachLink(scenario.getNetwork()); // will be aggregated in downstream code!
		
		final Controler controler = new Controler(scenario);
		
		for (String activityType : activityTypes) {
			AccessibilityModule module = new AccessibilityModule();
			module.setConsideredActivityType(activityType);
			module.addAdditionalFacilityData(densityFacilities);
			module.setPushing2Geoserver(push2Geoserver);
			controler.addOverridingModule(module);
		}
		
		controler.run();
		
		// QGis
		if (createQGisOutput) {
			final boolean includeDensityLayer = false;
			final Integer range = 9; // In the current implementation, this need always be 9
			final Double lowerBound = 0.; // (upperBound - lowerBound) ideally nicely divisible by (range - 2)
			final Double upperBound = 3.5;
			final int populationThreshold = (int) (50 / (1000/cellSize * 1000/cellSize));
			
			String osName = System.getProperty("os.name");
			String workingDirectory = config.controler().getOutputDirectory();
			for (String actType : activityTypes) {
				String actSpecificWorkingDirectory = workingDirectory + actType + "/";
				for (Modes4Accessibility mode : acg.getIsComputingMode()) {
					// TODO maybe use envelope and crs from above
					VisualizationUtils.createQGisOutput(actType, mode.toString(), new Envelope(252000, 256000, 9854000, 9856000), workingDirectory, "EPSG:21037", includeDensityLayer,
							lowerBound, upperBound, range, cellSize.intValue(), populationThreshold);
					VisualizationUtils.createSnapshot(actSpecificWorkingDirectory, mode.toString(), osName);
				}
			}  
		}
	}
}