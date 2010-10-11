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
package org.graphstream.stream.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.graphstream.graph.Graph;

/**
 * This class intends to output a dynamic graph into a Flash animation.
 * 
 * <p>
 * <b>This work may never to any usable feature. Please do not try to use it or
 * ask for help about it.</b>
 * </p>
 */
public class FileSinkSWF implements FileSink {
	private static class BitString {
		/**
		 * Bit 0 mask.
		 **/
		protected static final byte BIT_0 = (byte) Integer.parseInt("10000000",
				2);
		/**
		 * Bit 1 mask.
		 **/
		protected static final byte BIT_1 = (byte) Integer.parseInt("01000000",
				2);
		/**
		 * Bit 2 mask.
		 **/
		protected static final byte BIT_2 = (byte) Integer.parseInt("00100000",
				2);
		/**
		 * Bit 3 mask.
		 **/
		protected static final byte BIT_3 = (byte) Integer.parseInt("00010000",
				2);
		/**
		 * Bit 4 mask.
		 **/
		protected static final byte BIT_4 = (byte) Integer.parseInt("00001000",
				2);
		/**
		 * Bit 5 mask.
		 **/
		protected static final byte BIT_5 = (byte) Integer.parseInt("00000100",
				2);
		/**
		 * Bit 6 mask.
		 **/
		protected static final byte BIT_6 = (byte) Integer.parseInt("00000010",
				2);
		/**
		 * Bit 7 mask.
		 **/
		protected static final byte BIT_7 = (byte) Integer.parseInt("00000001",
				2);
		/**
		 * Set of all masks.
		 **/
		protected static final byte[] BITS = { BIT_0, BIT_1, BIT_2, BIT_3,
				BIT_4, BIT_5, BIT_6, BIT_7 };

		/**
		 * Buffer storing data.
		 * 
		 * @see java.nio.ByteBuffer
		 **/
		protected ByteBuffer buffer;
		/**
		 * Size of the buffer, current pointers.
		 **/
		private int size, curseur, bitCurseur;

		BitString(int... values) {
			this.size = this.curseur = this.bitCurseur = 0;

			int bitsize = 0;

			for (int i = 0; i < values.length; i++)
				bitsize = Math.max(bitsize,
						Integer.toBinaryString(Math.abs(values[i])).length());

			int totalbits = 5 + values.length * (bitsize + 1);

			__resize(totalbits / 8 + (totalbits % 8 == 0 ? 0 : 1));
			fill(false);

			String sizeBinary = Integer.toBinaryString(bitsize + 1);
			for (int i = sizeBinary.length() - 1; i > Math.max(0,
					sizeBinary.length() - 6); i--)
				add(sizeBinary.charAt(i) != '0');
			for (int i = sizeBinary.length(); i < 5; i++)
				add(false);

			String valueBinary;
			for (int i = 0; i < values.length; i++) {
				add(values[i] < 0);
				valueBinary = Integer.toBinaryString(Math.abs(values[i]));

				for (int j = 0; j < bitsize; j++) {
					if (j < valueBinary.length())
						add(valueBinary.charAt(bitsize - 1 - j) != '0');
					else
						add(values[i] < 0);
				}
			}
		}

		/**
		 * Resize the string.
		 * 
		 * @param size
		 *            in bytes new size
		 **/
		private void __resize(int size) {
			ByteBuffer nBuffer = ByteBuffer.allocate(size);

			for (int i = 0; i < Math.min(this.size, size); i++)
				nBuffer.put(buffer.get(i));

			this.buffer = nBuffer;
			this.size = size;
		}

		/**
		 * Bit value at the given index.
		 * 
		 * @param index
		 *            bit index
		 **/
		public boolean valueAt(int index) {
			return (buffer.get(index / 8) & BITS[index % 8]) != 0;
		}

		/**
		 * Add a bit.
		 * 
		 * @param value
		 *            bit value
		 **/
		public void add(boolean value) {
			if (curseur >= size) {
				__resize(size + 1);
			}

			if (value) {
				byte val = buffer.get(curseur);
				val = (byte) (val | (1 << (7 - bitCurseur)));
				buffer.put(curseur, val);
			}

			bitCurseur++;

			if (bitCurseur > 7) {
				bitCurseur = 0;
				curseur++;
			}
		}

		/**
		 * Fill with the given value.
		 */
		public void fill(boolean v) {
			fill(v, 0, size * 8);
		}

		public void fill(boolean v, int offset, int size) {
			for (int i = offset; i < offset + size; i++) {
				setValue(i, v);
			}
		}

		public void setValue(int id, boolean v) {
			byte val = buffer.get(id / 8);

			if (v) {
				val = (byte) (val | (1 << (7 - (id % 8))));
			} else {
				val = (byte) (val & (~(1 << (7 - (id % 8)))));
			}

			buffer.put(id / 8, val);
		}

		/**
		 * BitString to string.
		 **/
		public String toString() {
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < curseur; i++) {
				for (int j = 0; j < 8; j++)
					b.append(valueAt(i * 8 + j) ? '1' : '0');
			}
			for (int i = 0; i < bitCurseur; i++)
				b.append(valueAt(curseur * 8 + i) ? '1' : '0');
			return b.toString();
		}
	}

	private final static int BUFFER_SIZE = 1024000;

	private final static byte F = 0x46;
	private final static byte W = 0x57;
	private final static byte S = 0x53;
	private final static byte VERSION = 0x0A;

	ByteBuffer buffer;
	FileChannel channel;

	long position;
	long currentSize;

	@SuppressWarnings("unused")
	private void initChannelAndBuffer(String path) {
		if (channel != null)
			closeCurrentChannel();

		try {
			RandomAccessFile file = new RandomAccessFile(path, "rw");

			channel = file.getChannel();
			buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			position = 0;
			currentSize = 0;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void closeCurrentChannel() {

	}

	@SuppressWarnings("unused")
	private void writeHeader() {
		buffer.put(F);
		buffer.put(W);
		buffer.put(S);
		buffer.put(VERSION);
		buffer.putInt(0);
		writeRECT(buffer, 0, 0, 0, 0);
		buffer.putShort((short) 0);
	}

	private static void writeRECT(ByteBuffer buffer, int xmin, int xmax,
			int ymin, int ymax) {
		writeSingleBits(buffer, xmin, xmax, ymin, ymax);
	}

	private static void writeSingleBits(ByteBuffer buffer, int... values) {
		int max = Integer.MIN_VALUE;

		for (int i = 0; i < values.length; i++)
			max = Math.max(max, Math.abs(values[i]));

	}

	public void begin(String fileName) throws IOException {
		// TODO Auto-generated method stub

	}

	public void begin(OutputStream stream) throws IOException {
		// TODO Auto-generated method stub

	}

	public void end() throws IOException {
		// TODO Auto-generated method stub

	}

	public void flush() throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeAll(Graph graph, String fileName) throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeAll(Graph graph, OutputStream stream) throws IOException {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeAdded(String graphId, long timeId, String edgeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeChanged(String graphId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	public void edgeAttributeRemoved(String graphId, long timeId,
			String edgeId, String attribute) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeAdded(String graphId, long timeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeChanged(String graphId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	public void graphAttributeRemoved(String graphId, long timeId,
			String attribute) {
		// TODO Auto-generated method stub

	}

	public void nodeAttributeAdded(String graphId, long timeId, String nodeId,
			String attribute, Object value) {
		// TODO Auto-generated method stub

	}

	public void nodeAttributeChanged(String graphId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	public void nodeAttributeRemoved(String graphId, long timeId,
			String nodeId, String attribute) {
		// TODO Auto-generated method stub

	}

	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		// TODO Auto-generated method stub

	}

	public void edgeRemoved(String graphId, long timeId, String edgeId) {
		// TODO Auto-generated method stub

	}

	public void graphCleared(String graphId, long timeId) {
		// TODO Auto-generated method stub

	}

	public void nodeAdded(String graphId, long timeId, String nodeId) {
		// TODO Auto-generated method stub

	}

	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		// TODO Auto-generated method stub

	}

	public void stepBegins(String graphId, long timeId, double time) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		BitString bs = new BitString(1024, -1024);
		System.out.println(bs);
	}
}
