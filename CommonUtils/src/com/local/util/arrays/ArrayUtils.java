package com.local.util.arrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrayUtils {

	
	public static Object [] searchTwoDimensionalArray(String [] columnNames, Object [] searchRowValues, Object[][] matrix, String ... columnNamesToIgnore) {

		try {
			
			final int columnLength = columnNames.length;
			final List<String> columnNamesList = Arrays.asList(columnNames);
			
			List<Integer> ignoreSearchIndexes = 
					(columnNamesToIgnore == null) ? new ArrayList<Integer>() :
								Stream.of(columnNamesToIgnore).filter(p -> (columnNamesList.indexOf(p) >= 0))
										.map(p -> columnNamesList.indexOf(p)).collect(Collectors.toList());
			
			for (Object[] data : matrix) {

				System.out.println("comparing matrix = "+Arrays.asList(data));
				
				boolean valueEqual = true;
				
				for(int i = 0; i<columnLength; i++) {
					
					if(ignoreSearchIndexes.contains(i)) {continue;}
					
					valueEqual &= (data[i].equals(searchRowValues[i]));
					
					System.out.println("comparing : @index = "+i+", data = "+data[i]+", rowValue = "+searchRowValues[i]);
					
					/* NOTE: Notice that a break can be set already if valueEqual == false, 
					 * but it will just give a flaw for skipping the whole data matrix.
					 * Since this data structure will not be huge & to
					 * avoid dormant ArrayIndexOutOfBoundsException, do fail-fast - iterate all rows.*/
				}
				
				if(valueEqual) {
					return data;
				}
			}

			return new String[] {};
			
		} catch (Exception e) {
			
			throw new IllegalStateException("error on traversing two-dimensional array. Ensure correct length is set for both columnNames, searchValues & matrix", e);
		
		}
	}
	
	
	
	public static void main(String []s) {
		
		Object [][] matrix = { { "A", "Jan", 20 }, { "B", "Feb", 30 }, { "C", "Mar", 40 }, { "D", "Apr", 50 } };
		
		String [] columnNames = {"letter","month", "age"};
		
		Object [] searchRowValues = {"D", "Apr", 41};
		
		System.out.println(Arrays.asList(searchTwoDimensionalArray(columnNames, searchRowValues, matrix, "age")));
		
	}

}
