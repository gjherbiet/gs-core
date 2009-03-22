/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Copyright 2006 - 2009
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */

package org.miv.graphstream.graph;

/**
 * Interface to listen at changes in the graph structure.
 * 
 * <p>Graph elements listeners are called each time an element of the graph (node or
 * edge) is added or removed. It is also called for special events like "steps" that
 * introduce the notion of time in graphs.</p>
 */
public interface GraphElementsListener
{
	/**
	 * A node was inserted in the given graph.
	 * @param graphId Identifier of the graph where the node was added.
	 * @param nodeId Identifier of the added node.
	 */
	public void nodeAdded( String graphId, String nodeId );

	/**
	 * A node was removed from the graph.
	 * @param graphId Identifier of the graph where the node will be removed.
	 * @param nodeId Identifier of the removed node.
	 */
	public void nodeRemoved( String graphId, String nodeId );

	/**
	 * An edge was inserted in graph.
	 * @param graphId Identifier of the graph where the edge was added.
	 * @param edgeId Identifier of the added edge.
	 * @param fromNodeId Identifier of the first node of the edge.
	 * @param toNodeId Identifier of the second node of the edge.
	 * @param directed If true, the edge is directed.
	 */
	public void edgeAdded( String graphId, String edgeId, String fromNodeId, String toNodeId, boolean directed );

	/**
	 * An edge of graph was removed.The nodes the edge connects may already have been
	 * removed from the graph.
	 * @param graphId The graph where the edge will be removed.
	 * @param edgeId The edge that will be removed.
	 */
	public void edgeRemoved( String graphId, String edgeId );
	
	/**
	 * The whole graph was cleared. All the nodes, edges and attributes of the
	 * graph are removed.
	 * @param graphId The graph cleared.
	 */
	public void graphCleared( String graphId );
	
	/**
	 * <p>
	 * Since dynamic graphs are based on discrete event modifications, the notion of step is defined
	 * to simulate elapsed time between events. So a step is a event that occurs in the graph, it
	 * does not modify it but it gives a kind of timestamp that allow the tracking of the progress
	 * of the graph over the time.
	 * </p>
	 *
	 * <p>
	 * This kind of event is useful for dynamic algorithms that listen to the dynamic graph and need
	 * to measure the time in the graph's evolution.
	 * </p>
	 * 
	 * @param graphId Identifier of the graph where the step starts.
	 * @param time A numerical value that may give a timestamp to track the evolution of the graph
	 *   over the time.
	 */
	public void stepBegins( String graphId, double time );
}