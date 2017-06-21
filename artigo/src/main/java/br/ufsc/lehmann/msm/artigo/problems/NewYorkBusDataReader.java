package br.ufsc.lehmann.msm.artigo.problems;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.db.source.DataRetriever;
import br.ufsc.db.source.DataSource;
import br.ufsc.db.source.DataSourceType;
import br.ufsc.lehmann.stopandmove.LatLongDistanceFunction;

public class NewYorkBusDataReader {
	
	public static final BasicSemantic<Double> DISTANCE = new BasicSemantic<>(3);
	public static final BasicSemantic<String> ROUTE = new BasicSemantic<>(4);
	public static final BasicSemantic<Integer> DIRECTION = new BasicSemantic<>(5);
	public static final BasicSemantic<Integer> VEHICLE = new BasicSemantic<>(6);
	public static final BasicSemantic<String> PHASE = new BasicSemantic<>(7);
	public static final BasicSemantic<Double> NEXT_STOP_DISTANCE = new BasicSemantic<>(8);
	public static final BasicSemantic<String> NEXT_STOP_ID = new BasicSemantic<>(9);
	public static final StopSemantic STOP_SEMANTIC = new StopSemantic(10, new LatLongDistanceFunction());

	public List<SemanticTrajectory> read() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		DataSource source = new DataSource("postgres", "postgres", "localhost", 5432, "postgis", DataSourceType.PGSQL, "bus.nyc_20140927", null, null);
		DataRetriever retriever = source.getRetriever();
		System.out.println("Executing SQL...");
		Connection conn = retriever.getConnection();
		conn.setAutoCommit(false);
		Statement st = conn.createStatement();
		st.setFetchSize(1000);
		ResultSet stopsData = st.executeQuery(
				"SELECT stop_id, start_lat, start_lon, end_lat, end_lon, centroid_lat, " + //
						"centroid_lon, start_time, end_time " + //
						"FROM stops_moves.bus_nyc_20140927");
		Map<Integer, Stop> stops = new HashMap<>();
		while(stopsData.next()) {
			int stopId = stopsData.getInt("stop_id");
			Stop stop = stops.get(stopId);
			if(stop == null) {
				stop = new Stop(stopId, null, stopsData.getTimestamp("start_time"), stopsData.getTimestamp("end_time"), new TPoint(stopsData.getDouble("start_lat"), stopsData.getDouble("start_lon")),
						new TPoint(stopsData.getDouble("end_lat"), stopsData.getDouble("end_lon")), new TPoint(stopsData.getDouble("centroid_lat"), stopsData.getDouble("centroid_lon")));
				stops.put(stopId, stop);
			}
		}
		ResultSet data = st.executeQuery(
				"select gid, time_received as \"time\", vehicle_id, trim(infered_route_id) as route, "
				/**/+ "trim(infered_trip_id) as trip_id, longitude, latitude, distance_along_trip, infered_direction_id, "
				/**/+ "trim(infered_phase) as phase, next_scheduled_stop_distance, next_scheduled_stop_id, semantic_stop_id "
				+ "from bus.nyc_20140927 "//
				+ "where infered_trip_id is not null "//
//				+ "and ('MTA NYCT_Q20A'=infered_route_id or 'MTA NYCT_Q13'=infered_route_id or 'MTABC_Q66'=infered_route_id or 'MTABC_Q65'=infered_route_id or 'MTA NYCT_Q32'=infered_route_id or 'MTA NYCT_M42'=infered_route_id or 'MTABC_Q49'=infered_route_id or 'MTA NYCT_Q28'=infered_route_id or 'MTA NYCT_X10'=infered_route_id or 'MTA NYCT_M102'=infered_route_id) "//
				+ "and ('MTA NYCT_Q20A'=infered_route_id or 'MTA NYCT_M102'=infered_route_id) "//
//				+ "and time_received < to_timestamp('2014-09-27 12:00:00', 'yyyy-MM-dd HH24:mi:ss')"//
				+ "order by time_received"//
				);
		Multimap<String, NewYorkBusRecord> records = MultimapBuilder.hashKeys().linkedListValues().build();
		System.out.println("Fetching...");
		while(data.next()) {
			Integer stop = data.getInt("semantic_stop_id");
			if(data.wasNull()) {
				stop = null;
			}
			NewYorkBusRecord record = new NewYorkBusRecord(
				data.getInt("gid"),
				data.getTimestamp("time"),
				data.getInt("vehicle_id"),
				data.getString("route"),
				data.getString("trip_id"),
				data.getDouble("longitude"),
				data.getDouble("latitude"),
				data.getDouble("distance_along_trip"),
				data.getInt("infered_direction_id"),
				data.getString("phase"),
				data.getDouble("next_scheduled_stop_distance"),
				data.getString("next_scheduled_stop_id"),
				stop
			);
			records.put(record.getTripId(), record);
		}
		st.close();
		System.out.printf("Loaded %d raw trajectories from database\n", records.size());
		System.out.printf("Loaded %d trajectories from database\n", records.keySet().size());
		List<SemanticTrajectory> ret = new ArrayList<>();
		Set<String> keys = records.keySet();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		int trajectoryId = 0;
		for (String trajId : keys) {
			SemanticTrajectory s = new SemanticTrajectory(trajectoryId++, 11);
			Collection<NewYorkBusRecord> collection = records.get(trajId);
			int i = 0;
			for (NewYorkBusRecord record : collection) {
				s.addData(i, Semantic.GID, record.getGid());
				s.addData(i, Semantic.GEOGRAPHIC, new TPoint(record.getLatitude(), record.getLongitude()));
				s.addData(i, Semantic.TEMPORAL, new TemporalDuration(Instant.ofEpochMilli(record.getTime().getTime()), Instant.ofEpochMilli(record.getTime().getTime())));
				s.addData(i, DIRECTION, record.getDirection());
				s.addData(i, ROUTE, record.getRoute());
				s.addData(i, DISTANCE, record.getDistanceAlongTrip());
				s.addData(i, VEHICLE, record.getVehicleId());
				s.addData(i, NEXT_STOP_DISTANCE, record.getNextStopDistance());
				s.addData(i, NEXT_STOP_ID, record.getNextStopId());
				s.addData(i, PHASE, record.getPhase());
				if(record.getSemanticStop() != null) {
					Stop stop = stops.get(record.getSemanticStop());
					s.addData(i, STOP_SEMANTIC, stop);
				}
				i++;
			}
			stats.addValue(s.length());
			ret.add(s);
		}
		System.out.printf("Semantic Trajectories statistics: mean - %.2f, min - %.2f, max - %.2f, sd - %.2f\n", stats.getMean(), stats.getMin(), stats.getMax(), stats.getStandardDeviation());
		return ret;
	}
}