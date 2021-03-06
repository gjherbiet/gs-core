/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */

package org.graphstream.graph;

/**
 * An interface aimed at dynamically creating edge objects.
 * 
 * @since September 2007
 */
public interface EdgeFactory<T extends Edge> {
	/**
	 * Create a new instance of edge.
	 * 
	 * @param id
	 *            The new edge identifier.
	 * @param src
	 *            The source node.
	 * @param dst
	 *            The target node.
	 * @param directed
	 *            Is the edge directed (in the direction source toward target).
	 * @return The newly created edge.
	 */
	T newInstance(String id, Node src, Node dst, boolean directed);
}