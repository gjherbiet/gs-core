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

package org.graphstream.stream.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import org.graphstream.stream.Sink;

public class RMISink
	extends UnicastRemoteObject 
	implements RMIAdapterOut, Sink
{
	private static final long serialVersionUID = 23444722897331612L;

	ConcurrentHashMap<String,RMIAdapterIn> inputs;
	
	public RMISink()
		throws RemoteException
	{
		inputs = new ConcurrentHashMap<String,RMIAdapterIn>();
	}
	
	public RMISink( String name )
		throws RemoteException
	{
		super(); bind(name);
	}
	
	public void bind( String name )
	{
		try
		{
			Naming.rebind( String.format( "//localhost/%s", name ), this );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void register(String url)
		throws RemoteException
	{
		try
		{
			RMIAdapterIn in = (RMIAdapterIn) Naming.lookup(url);
			
			if( in != null )
				inputs.put(url,in);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void unregister(String url)
		throws RemoteException
	{
		if( inputs.containsKey(url) )
			inputs.remove(url);
	}
	
	public void edgeAttributeAdded(String graphId, long timeId, String edgeId,
			String attribute, Object value)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.edgeAttributeAdded(graphId,timeId,edgeId,attribute,value);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void edgeAttributeChanged(String graphId, long timeId, String edgeId,
			String attribute, Object oldValue, Object newValue)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.edgeAttributeChanged(graphId,timeId,edgeId,attribute,oldValue,newValue);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void edgeAttributeRemoved(String graphId, long timeId, String edgeId,
			String attribute)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.edgeAttributeRemoved(graphId,timeId,edgeId,attribute);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void graphAttributeAdded(String graphId, long timeId, String attribute,
			Object value)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.graphAttributeAdded(graphId,timeId,attribute,value);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void graphAttributeChanged(String graphId, long timeId, String attribute,
			Object oldValue, Object newValue)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.graphAttributeChanged(graphId,timeId,attribute,oldValue,newValue);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void graphAttributeRemoved(String graphId, long timeId, String attribute)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.graphAttributeRemoved(graphId,timeId,attribute);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void nodeAttributeAdded(String graphId, long timeId, String nodeId,
			String attribute, Object value)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.nodeAttributeAdded(graphId,timeId,nodeId,attribute,value);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void nodeAttributeChanged(String graphId, long timeId, String nodeId,
			String attribute, Object oldValue, Object newValue)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.nodeAttributeChanged(graphId,timeId,nodeId,attribute,oldValue,newValue);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void nodeAttributeRemoved(String graphId, long timeId, String nodeId,
			String attribute)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.nodeAttributeRemoved(graphId,timeId,nodeId,attribute);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void edgeAdded(String graphId, long timeId, String edgeId, String fromNodeId,
			String toNodeId, boolean directed)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.edgeAdded(graphId,timeId,edgeId,fromNodeId,toNodeId,directed);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void edgeRemoved(String graphId, long timeId, String edgeId)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.edgeRemoved(graphId,timeId,edgeId);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void graphCleared(String graphId, long timeId)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.graphCleared(graphId,timeId);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void nodeAdded(String graphId, long timeId, String nodeId)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.nodeAdded(graphId,timeId,nodeId);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void nodeRemoved(String graphId, long timeId, String nodeId)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.nodeRemoved(graphId,timeId,nodeId);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}

	public void stepBegins(String graphId, long timeId, double step)
	{
		for( RMIAdapterIn in : inputs.values() )
		{
			try
			{
				in.stepBegins(graphId,timeId,step);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
}