/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import pathfinder.Graph;
import pathfinder.GraphEdge;
import pathfinder.GraphNode;
import pathfinder.GraphSearch_Dijkstra;
import pathfinder.IGraphSearch;
import util.Log;

/**
 *  
 */
public class RoutePlanner {
	private OsmParser parser;
	private Graph graph;
	private IGraphSearch graphSearcher;
	private ArrayList<Intersection> intersections;

	public RoutePlanner(OsmParser parser) {
		this.parser = parser;
		makeGraph(); // for route-finding
		intersections = new ArrayList<>();
		setIntersectionNodes(); // for simulation. call after graph created.
		// TODO in order to use A star need to make graph only include connected nodes
		// float costFactor = 1.0f;
		// graphSearcher = new pathfinder.GraphSearch_Astar(graph, new pathfinder.AshManhattan(costFactor));
		graphSearcher = new GraphSearch_Dijkstra(graph);
	}

	// TODO FIXME only pass nodes that connect to edges to search algorithm.
	// Also preferrably only pass nodes/edges that all connect to search algorithm in order to use A star.
	private void makeGraph() {
		// Make a list of edges between nodes in each way's node list
		graph = new Graph();
		// Note: only make graph from roads (not footpaths, buildings, etc...)
		for (Way way : parser.getRoads().values()) {
			boolean isOneway = way.isOneway();
			// TODO optimize cost depending on way type (ex. side street = 30, main road = 60, highway = 90)
			double costMultiplier = (way.getMaxspeedKm() > 0) ? 1 / way.getMaxspeedKm() : 1;
			LinkedList<Node> list = way.getNodes();
			Node prev = null;
			// Note: adding uni-directional edge forward/backward for each node,
			// which means we end up with a bidirectional graph.
			// Don't graph.addEdge(n1, n2, cost, cost) or we'll end up with a duplicate edge!
			// For one-way streets don't add edge from current node to previous.
			for (int i = 0; i < list.size(); i++) {
				Node node = list.get(i);
				Node next = (i < list.size() - 1) ? list.get(i + 1) : null;
				if (prev != null && !isOneway) {
					double cost = costMultiplier * ((node.getLat() - prev.getLat()) * (node.getLat() - prev.getLat())
							+ (node.getLon() - prev.getLon()) * (node.getLon() - prev.getLon()));
					graph.addEdge(node.getId(), prev.getId(), cost);
				}
				if (next != null) {
					double cost = costMultiplier * ((node.getLat() - next.getLat()) * (node.getLat() - next.getLat())
							+ (node.getLon() - next.getLon()) * (node.getLon() - next.getLon()));
					graph.addEdge(node.getId(), next.getId(), cost);
				}
				prev = node;
			}
		}
		// Add all nodes to graph
		// TODO make Node implement GraphNode interface and refactor current GraphNode class to some other name.
		// NOTE we are treating lon/lat as x/y coordinates for the sake of speed here
		for (Node node : parser.getNodes().values()) {
			graph.addNode(new GraphNode(node.getId(), node.getLon(), node.getLat()));
		}
		graph.compact(); // Removes unnecessary (floating) edges
	}

	private void setIntersectionNodes() {
		// Note: Graph's GraphNodes map 1-1 with OsmParser's Nodes
		for (Node node : parser.getNodes().values()) {
			// Need list of edges *to* intersection node because of one-way streets
			LinkedList<GraphEdge> edges = graph.getEdgeListTo(node.getId());
			ArrayList<Node> approaching = new ArrayList<>();
			for (GraphEdge edge : edges) {
				approaching.add(parser.getNodes().get(edge.from().id()));
			}
			// Intersection if 2+ ways or 3+ nodes approach it
			if (approaching.size() >= 3) {
				// Get the ways that approach the intersection.
				// Need this to get speed limits (which determine yellow light times) and to determine
				//  what type of intersection to set to.
				ArrayList<Way> approachingWays = new ArrayList<>();
				int majorWayCount = 0;
				for (Node approachNode : approaching) {
					ArrayList<Node> list = new ArrayList<>();
					list.add(node);
					list.add(approachNode);
					Way way = getFirstRoadContaining(list);
					approachingWays.add(way);
					if (way.getMaxspeedKm() >= RoadTypes.SPD_NORMAL) {
						majorWayCount++;
					}
				}
				// TODO: set intersection to appropriate type (ex. lit)
				Intersection intersection;
				int type;
				if (majorWayCount >= 2) {
					type = Intersection.FULLY_LIT;
					// } else if (majorWayCount >= 1 ) {
					// TODO half-lit intersection
				} else {
					type = Intersection.ALL_WAY_STOP;
				}
				intersection = new Intersection(type, node, approaching, approachingWays);
				node.setIntersection(intersection);
				intersections.add(intersection);
			}
		}
	}

	public ArrayList<Intersection> getIntersections() {
		return intersections;
	}

	// Returns nearest node in graph to coordinates. Returns null if nearest farther than maxDistance.
	public Node getNearestNode(double lon, double lat, double maxDistance) {
		GraphNode gnode = graph.getNodeAt(lon, lat, 0, maxDistance);
		return getNode(gnode);
	}

	private Node getNode(GraphNode gnode) {
		if (gnode == null) {
			return null;
		}
		Node node = parser.getNodes().get(gnode.id());
		if (node == null) {
			Log.error("Could not find node matching graph node with id : " + gnode.id());
		}
		return node;
	}

	public ArrayList<Node> getNewRoute(Node start, Node dest) {
		if (start == null || dest == null) {
			Log.error("No start or destination nodes given to RoutePlanner, return null route");
			return null;
		}
		if (parser == null) {
			Log.error("No or empty map information (parser) given to RoutePlanner, return null route");
			return null;
		}
		long startTime = System.currentTimeMillis();
		graphSearcher.search(start.getId(), dest.getId(), false); // forget examined edges
		long stopTime = System.currentTimeMillis();
		ArrayList<Node> route = new ArrayList<>();
		for (GraphNode gnode : graphSearcher.getRoute()) {
			route.add(getNode(gnode));
		}
		if (route.isEmpty()) {
			Log.warning("No route found from Node " + start.getId() + " to Node " + dest.getId());
			return null;
		} else {
			Log.info("Finding route from Node " + start.getId() + " to Node " + dest.getId() + " took "
					+ (stopTime - startTime) + " ms.");
		}
		return route;
	}

	public Way getFirstWayContaining(Node node) {
		return getFirstOtherWayContaining(null, node);
	}

	public Way getFirstRoadContaining(Node node) {
		return getFirstOtherRoadContaining(null, node);
	}

	public Way getFirstRoadContaining(Collection<Node> nodes) {
		return getFirstOtherRoadContaining(null, nodes);
	}

	public Way getFirstOtherWayContaining(Way current, Node node) {
		for (Way way : parser.getWays().values()) {
			if (!way.equals(current)) {
				if (way.getNodeRefs().contains(node.getId())) {
					return way;
				}
			}
		}
		return null;
	}

	public Way getFirstOtherRoadContaining(Way current, Node node) {
		for (Way way : parser.getRoads().values()) {
			if (!way.equals(current)) {
				if (way.getNodeRefs().contains(node.getId())) {
					return way;
				}
			}
		}
		return null;
	}

	// Return first way containing all in a list of nodes, other than "current"
	public Way getFirstOtherRoadContaining(Way current, Collection<Node> nodes) {
		ArrayList<Long> nodeIds = new ArrayList<>();
		for (Node node : nodes) {
			nodeIds.add(node.getId());
		}
		for (Way way : parser.getRoads().values()) {
			if (!way.equals(current)) {
				boolean containsNodes = true;
				ArrayList<Long> wayNodeRefs = way.getNodeRefs();
				for (Long id : nodeIds) {
					if (!wayNodeRefs.contains(id)) {
						containsNodes = false;
						break;
					}
				}
				if (containsNodes) {
					return way;
				}
			}
		}
		return null;
	}
}
