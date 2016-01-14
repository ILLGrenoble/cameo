/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.impl;

import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.proto.Messages.Float32Array;
import fr.ill.ics.cameo.proto.Messages.Float64Array;
import fr.ill.ics.cameo.proto.Messages.Int32Array;
import fr.ill.ics.cameo.proto.Messages.Int64Array;
import fr.ill.ics.cameo.proto.Messages.StringValue;

public class Serializer {

	public static byte[] serialize(String data) {
		
		// encode the data
		StringValue.Builder builder = StringValue.newBuilder();
		
		builder.setValue(data);
		
		StringValue value = builder.build();
		
		return value.toByteArray();
	}
	
	public static byte[] serialize(int[] data) {
		
		// encode the data, will be optimized
		Int32Array.Builder builder = Int32Array.newBuilder();
		
		for (int i = 0; i < data.length; i++) {
			builder.addValue(data[i]);
		}
		
		Int32Array array = builder.build();
		
		return array.toByteArray();
	}
	
	public static byte[] serialize(long[] data) {
		
		// encode the data, will be optimized
		Int64Array.Builder builder = Int64Array.newBuilder();
		
		for (int i = 0; i < data.length; i++) {
			builder.addValue(data[i]);
		}
		
		Int64Array array = builder.build();

		return array.toByteArray();
	}

	public static byte[] serialize(float[] data) {
	
		// encode the data, will be optimized
		Float32Array.Builder builder = Float32Array.newBuilder();
		
		for (int i = 0; i < data.length; i++) {
			builder.addValue(data[i]);
		}
		
		Float32Array array = builder.build();
		
		return array.toByteArray();
	}

	public static byte[] serialize(double[] data) {
	
		// encode the data, will be optimized
		Float64Array.Builder builder = Float64Array.newBuilder();
		
		for (int i = 0; i < data.length; i++) {
			builder.addValue(data[i]);
		}
		
		Float64Array array = builder.build();
		
		return array.toByteArray();
	}
		
	public static String parseString(byte[] data) {
		
		try {
			StringValue value = StringValue.parseFrom(data);
			return value.getValue();
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public static int[] parseInt32(byte[] data) {
		
		try {
			Int32Array array = Int32Array.parseFrom(data);
			List<Integer> list = array.getValueList();
			int size = list.size();
			int[] result = new int[size];
			
			int i = 0;
			for (Integer v : list) {
				result[i] = v;
				i++;
			}
			
			return result;
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public static long[] parseInt64(byte[] data) {
		
		try {
			Int64Array array = Int64Array.parseFrom(data);
			List<Long> list = array.getValueList();
			int size = list.size();
			long[] result = new long[size];
			
			int i = 0;
			for (Long v : list) {
				result[i] = v;
				i++;
			}
			
			return result;
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public static float[] parseFloat(byte[] data) {
	
		try {
			Float32Array array = Float32Array.parseFrom(data);
			List<Float> list = array.getValueList();
			int size = list.size();
			float[] result = new float[size];
			
			int i = 0;
			for (Float v : list) {
				result[i] = v;
				i++;
			}
			
			return result;
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public static double[] parseDouble(byte[] data) {
		
		try {
			Float64Array array = Float64Array.parseFrom(data);
			List<Double> list = array.getValueList();
			int size = list.size();
			double[] result = new double[size];
			
			int i = 0;
			for (Double v : list) {
				result[i] = v;
				i++;
			}
			
			return result;
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
}