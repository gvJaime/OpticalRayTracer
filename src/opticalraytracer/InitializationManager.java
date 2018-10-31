/***************************************************************************
 *   Copyright (C) 2014 by Paul Lutus                                      *
 *   lutusp@arachnoid.com                                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package opticalraytracer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class InitializationManager {
	OpticalRayTracer parent;
	ProgramValues programValues;
	String fileSep;
	String appName;
	String userDir;
	String userPath;
	String initPath;

	public InitializationManager(OpticalRayTracer p, ProgramValues pv) {
		this.parent = p;
		this.programValues = pv;
		fileSep = System.getProperty("file.separator");
		appName = parent.getClass().getSimpleName();
		userDir = System.getProperty("user.home");
		userPath = userDir + fileSep + "." + appName;
		initPath = userPath + fileSep + appName + ".ini";
		testMakeDirs(userPath);
	}

	protected void setFullConfiguration(String data) {
		if (data != null) {
			String pv = data.replaceFirst(
					"(?is).*?program \\{\\s*(.*?)\\s*\\}.*", "$1");
			programValues.setValues(pv);
			parent.componentList = new ArrayList<>();
			String s = "(?is)object \\{\\s*(.*?)\\s*\\}";
			Pattern pat = Pattern.compile(s);
			Matcher m = pat.matcher(data);
			while (m.find()) {
				String v = m.group(1);
				parent.makeNewComponent(v, false, Common.OBJECT_REFRACTOR);
			}
			parent.writeProgramControls();
		}
	}

	protected void readConfig() {
		String data = readFile(initPath);
		setFullConfiguration(data);
	}

	protected void readStream(InputStream in) {
		int blockLen = 512;
		int len = 0;
		byte[] bdata = new byte[blockLen];
		StringBuilder data = new StringBuilder();
		do {
			try {
				len = in.read(bdata,0,blockLen);
				data.append(new String(bdata,0,len));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				len = 0;
			}
		} while (len == blockLen);
		if (data.length() > 0) {
			setFullConfiguration(data.toString());
		}
	}

	protected void writeConfig(boolean includeHeader) {
		writeFile(initPath, getFullConfiguration(includeHeader));
	}

	protected String getFullConfiguration(boolean includeHeader) {
		StringBuilder sb = new StringBuilder();
		if (includeHeader) {
			sb.append(String.format("# %s\n", parent.fullAppName));
			sb.append("# http://arachnoid.com/OpticalRayTracer\n");
			String date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z")
					.format(new Date());
			sb.append(String.format("# %s\n\n", date));
		}
		sb.append(String.format("program {\n"));
		sb.append(programValues.getValues());
		sb.append(String.format("}\n\n"));
		for (OpticalComponent oc : parent.componentList) {
			sb.append(oc.getValues());
			sb.append(parent.lineSep);
		}
		return sb.toString();
	}

	private String readFile(String path) {
		String result = null;
		try {
			result = new String(Files.readAllBytes(Paths.get(path)),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// no initialization file, so first run
			parent.setDefaults(true);
		}
		return result;
	}

	private void writeFile(String path, String data) {
		try {
			PrintWriter out = new PrintWriter(path);
			out.write(data);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean testMakeDirs(String path) {
		File fpath = new File(path);
		if (fpath.exists()) {
			return false;
		} else {
			fpath.mkdirs();
			return true;
		}
	}
}
