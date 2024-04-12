/*
  SPDX-License-Identifier: GPL-3.0-only

  Part of the AI for Games library
  Copyright (c) 2011 Peter Lager
  Licensed under the GNU LGPL v2.1

  This copy is relicensed under the GNU GPL v3.0
 */

package pathfinder;

/**
 * Interface for all A* heuristic classes
 * 
 * @see		AshCrowFlight
 * @see		AshManhattan
 * 
 * @author Peter Lager
 */
public interface AstarHeuristic {

	/**
	 * Estimate the cost between the node and the target.
	 */
	public double getCost(GraphNode node, GraphNode target);
	
}
