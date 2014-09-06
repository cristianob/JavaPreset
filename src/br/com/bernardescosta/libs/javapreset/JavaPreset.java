/**
 * Copyright (C) 2014  Cristiano Costa
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package br.com.bernardescosta.libs.javapreset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * JavaCSVPreset
 * 
 * How to use:
 * * Create the CSV file (The class will create the file with a simple 
 * template if it not exists for reference). Remember to end the file with
 * a line break!
 * 
 * Reading:
 * * Use getPresetsName to get a Vector<String> with the name of the presets
 * * Use getPresetFieldValue(preset, field) to get the value of a field
 * 
 * Writing:
 * 
 * @author Cristiano Costa
 * @version 1.0
 */
public class JavaPreset {
	private String fn;
	
	public int fieldLenght = 0;
	
	private Vector<String> fieldNames = new Vector<String>();
	private Vector<Vector<String>> presets = new Vector<Vector<String>>();
	
	public JavaPreset(String _fn) throws IOException, ParseException {
		fn = _fn;
		parseFile();
	}
	
	public Vector<String> getFieldNames() {
		return fieldNames;
	}
	
	public Vector<String> getPresetsName() {
		Vector<String> ret = new Vector<String>();
		
		for(int i=0; i < presets.size(); i++) {
			ret.add(presets.get(i).get(0));
		}
		
		return ret;
	}
	
	public String getPresetFieldValue(String preset, String value) {
		int preset_pos = -1;
		for(int i=0; i < presets.size(); i++) {
			if(presets.get(i).get(0).equals(preset)) preset_pos = i;
		}
		if(preset_pos == -1) throw new NoSuchElementException("Preset not found!");
		
		int value_pos = fieldNames.indexOf(value);
		if(value_pos == -1) throw new NoSuchElementException("Value not found!");
		
		return presets.get(preset_pos).get(value_pos + 1);
	}
	
	public void addPreset(String preset, Vector<String> values) throws IOException {
		if(values.size() != fieldLenght) throw new IllegalArgumentException("Values size doesn't match");
		
		BufferedWriter output = new BufferedWriter(new FileWriter(fn, true));
		output.write(preset +"|");
		for(int i = 0; i < values.size(); i++){
			output.write(values.get(i));
			if(i < values.size() - 1)
				output.write("|");
		}
		output.write("\n");
		output.close();
		
		Vector<String> addToPresets = new Vector<String>();
		addToPresets.add(preset);
		addToPresets.addAll(values);
		presets.add(addToPresets);
	}
	
	private void parseFile() throws IOException, ParseException {
		File f = new File(fn);
		if(f.exists() && f.isDirectory()) throw new IOException("File is a directory");
		if(!f.exists()) { 
			f.createNewFile();
			BufferedWriter output = new BufferedWriter(new FileWriter(f));
			output.write("#JAVACSVPRESET#\n\nFild Name\nSecond Field Name\n...\n\n");
			output.close();
			
			throw new ParseException("File isn't created, please fill it with the fields data", 0);
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		 
		String line = null;
		int session = 0;
		while ((line = br.readLine()) != null) {
			if(line.equals("")) { session++; continue; }
			
			switch(session) {
				case 0: //Header
					if(line.equals("#JAVACSVPRESET#\n")) throw new ParseException("File isn't in the JavaCSVPreset format", 0);
					break;
				
				case 1: //Names & Types
					fieldNames.add(line);
					fieldLenght++;
					break;
					
				case 2: //Presets
					String splited[] = line.split("\\|");
					if(splited.length != fieldNames.size() + 1) throw new ParseException("File isn't in the JavaCSVPreset format. Error in session 2", 2);
					
					Vector<String> preset_parse = new Vector<String>();
					for(int i=0; i < fieldNames.size() + 1; i++)
						preset_parse.add(splited[i]);
					
					presets.add(preset_parse);
					break;
			}			
		}
		
		if(session < 2)throw new ParseException("Please fill the CSV with the fields data", 0);
	}
}
