/**
 * 
 */
package playground.kai.urbansim;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.Id;
import org.matsim.basic.v01.BasicLeg.Mode;
import org.matsim.population.Act;
import org.matsim.population.Plan;
import org.matsim.utils.geometry.Coord;
import org.matsim.utils.io.IOUtils;

import playground.kai.urbansim.ids.IdFactory;

/**
 * Various standalone utilities that don't need to be inside a specific package
 * 
 * @author nagel
 *
 */
public class Utils {
	private static final Logger log = Logger.getLogger(Utils.class);

	/**This reads a key-value mapping from a column-oriented file.  The "String key" and the "String value" parameters give
	 * the column headers.  The IdFactories are there so that there is at least some runtime safety net: If you pull an Id that 
	 * you did not expect, it will complain.   "Map<Id,Id> yFromx" contains the result.
	 * @param yFromX
	 * @param key
	 * @param keyIdFactory
	 * @param value
	 * @param valueIdFactory
	 * @param filename
	 */
	public static void readKV( Map<Id,Id> yFromX, String key, IdFactory keyIdFactory, String value, IdFactory valueIdFactory, String filename ) {
		try {
			log.info( "Starting to read some key-value pairs from " + filename ) ;
			
			BufferedReader reader = IOUtils.getBufferedReader( filename ) ;
	
			String header = reader.readLine() ;
			Map<String,Integer> idxFromKey = Utils.createIdxFromKey( header ) ;
	
			String line = reader.readLine() ;
			while ( line != null ) {
				String[] parts = line.split("[\t\n]+");
	
				int idx = idxFromKey.get(value) ;
				Id valueId = valueIdFactory.createId( parts[idx] ) ;
	
				idx = idxFromKey.get(key) ;
				Id keyId = keyIdFactory.createId( parts[idx] ) ;
	
				yFromX.put(keyId, valueId) ;
	
				line = reader.readLine() ;
			}
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		log.info( "DONE with reading some key-value pairs." ) ;
	}

	/**If, say, households point to buildings, and buildings point to parcels, and all you need is households to parcels,
	 * then this small helper utility can be used to construct the direct mapping.  This may be particularly important to save
	 * memory since the original two mappings can then be cleared (remember to do this, though!).
	 * 
	 * One reason for not using it is that dealing with special cases gets increasingly difficult (e.g. houssholds not having buildings).
	 * 
	 * @param aFromC
	 * @param aFromB
	 * @param bFromC
	 */
	public static void constructAFromC( Map<Id,Id> aFromC, Map<Id,Id> aFromB, Map<Id,Id> bFromC ) {
		for ( Iterator<Entry<Id,Id>> it = bFromC.entrySet().iterator() ; it.hasNext(); ) {
			Entry<Id,Id> entry = it.next();
			Id cc = entry.getKey() ;
			Id bb = entry.getValue();
			
			Id aa = aFromB.get(bb) ;
			
			aFromC.put(cc,aa) ;
		}
	}

	/**
	 * This is used to parse a header line from a tab-delimited urbansim header and generate a Map that allows to look up column
	 * numbers (starting from 0) by giving the header line.  
	 * 
	 * I.e. if you have a header "from_id <tab> to_id <tab> travel_time", then idxFromKey.get("to_id") will return "1".
	 * 
	 * This makes the reading of column-oriented files independent from the sequence of the columns.
	 * 
	 * Could be made more general by putting the separator into the argument.
	 * 
	 * @param line
	 * @return idxFromKey as described above (mapping from column headers into column numbers)
	 */
	static Map<String,Integer> createIdxFromKey( String line ) {
		String[] keys = line.split("[\t\n]+") ;
		
		Map<String,Integer> idxFromKey = new HashMap<String, Integer>() ;
		for ( int ii=0 ; ii<keys.length ; ii++ ) {
			idxFromKey.put(keys[ii], ii ) ;
		}
		return idxFromKey ;
	}
	
	private static final String ACT_HOME = "home" ;
	private static final String ACT_WORK = "work" ;
	
	
	/**
	 * Helper method to start a plan by inserting the home location.  This is really only useful together with "completePlanToHwh",
	 * which completes the plan, and benefits from the fact that the Strings for the "home" and the "work" act are now concentrated 
	 * here.
	 * 
	 * @param plan
	 * @param homeCoord
	 */
	static void makeHomePlan( Plan plan, Coord homeCoord ) {
		plan.createAct(ACT_HOME, homeCoord) ;
	}
	/**
	 * Helper method to complete a plan with *wh in a consistent way.  Assuming that the first activity is the home activity.
	 * 
	 * @param plan
	 * @param workCoord
	 */
	static void completePlanToHwh ( Plan plan, Coord workCoord ) {
		Act act = plan.getFirstActivity();
		act.setEndTime( 7.*3600. ) ;
		Coord homeCoord = act.getCoord();

		plan.createLeg(Mode.car);
		act = plan.createAct(ACT_WORK, workCoord ) ;
		act.setDuration( 8.*3600. ) ;

		plan.createLeg(Mode.car) ;
		plan.createAct(ACT_HOME, homeCoord ) ;				
	}

}
