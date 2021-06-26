package com.succorfish.eliteoperator.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class DataHolder {

	private ArrayList<LinkedHashMap<String, String>> _Listholder;

	private LinkedHashMap<String, String> _lmap;

	public DataHolder() {
		super();
		_Listholder = new ArrayList<LinkedHashMap<String, String>>();
	}

	public void set_Lmap(String col, String value) {
		this._lmap.put(col, value);
	}

	public ArrayList<LinkedHashMap<String, String>> get_Listholder() {
		return _Listholder;
	}

	public void CreateRow() {
		this._lmap = new LinkedHashMap<String, String>();
	}

	public void AddRow() {
		this._Listholder.add(this._lmap);
	}
}
