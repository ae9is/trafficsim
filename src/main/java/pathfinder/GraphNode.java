/*
  SPDX-License-Identifier: GPL-3.0-only

  Part of the AI for Games library
  Copyright (c) 2011 Peter Lager
	Licensed under the GNU LGPL v2.1

	This copy is relicensed under the GNU GPL v3.0
 */

package pathfinder;


/**
 * This class represents a node (vertex) that can be used with
 * the Graph class. <br>
 * 
 * The node has a position in 3D space for 2D applications the z value should 
 * be zero. <br>
 * 
 * Each node should be given a unique ID number >= 0. Node ID numbers do
 * not need to start at 0 (zero) or be sequential but they must be unique. <br>
 * 
 * It is the responsibility of the user to ensure that each node ID is unique 
 * as duplicate ID numbers can lead to unpredictable behaviour.
 * 
 * @author Peter Lager
 *
 */
@SuppressWarnings("ComparableType")
public class GraphNode implements Comparable<Object> {

	protected long id;
	
	double x,y,z;
	
	
	protected GraphNode(){
		id = -1;
	}
	
	/**
	 * Create a node with a given ID
	 * @param id
	 */
	public GraphNode(long id){
		this.id = id;
		x = y = z = 0;
	}

	/**
	 * Create a node
	 * @param id unique id number for this node
	 * @param x
	 * @param y
	 * @param z set to 0 (zero for 2D applications
	 */
	public GraphNode(long id, double x, double y, double z) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	
	/**
	 * Create a node
	 * @param id unique id number for this node
	 * @param x
	 * @param y
	 */
	public GraphNode(long id, double x, double y) {
		this.id = id;
		this.x = x;
		this.y = y;
		z = 0;
	}

	/**
	 * Get the node ID 
	 * @return the id
	 */
	public long id() {
		return id;
	}

	/**
	 * Change the node id. <br>
	 * Care should be taken to ensure the new ID number is unique
	 * @param id the id to set
	 */
	public void id(long id) {
		this.id = id;
	}

	/**
	 * @return the x position
	 */
	public double x() {
		return x;
	}

	/**
	 * @return the x position as a float
	 */
	public float xf() {
		return (float) x;
	}

	/**
	 * @param x the new x position
	 */
	public void x(float x) {
		this.x = x;
	}

	/**
	 * @return the y position
	 */
	public double y() {
		return y;
	}

	/**
	 * @return the y position as a float
	 */
	public float yf() {
		return (float) y;
	}

	/**
	 * @param y the new y position
	 */
	public void y(float y) {
		this.y = y;
	}

	/**
	 * @return the z position
	 */
	public double z() {
		return z;
	}

	/**
	 * @return the y position as a float
	 */
	public float zf() {
		return (float) z;
	}

	/**
	 * @param z the new z position
	 */
	public void z(float z) {
		this.z = z;
	}

	/**
	 * Compare nodes based on ID number. <br>
	 */
	public int compareTo(Object o) {
		GraphNode node = (GraphNode) o;
		if(id == node.id)
			return 0;
		else 
			return (id < node.id) ? -1 : 1;
	}
	
}
