/**
 * University of Illinois/NCSA
 * Open Source License
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.  
 * All rights reserved.
 * 
 * Developed by: 
 * 
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 * 
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions: 
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers. 
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the 
 *    documentation and/or other materials provided with the distribution. 
 * 
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */ 

package org.seasr.meandre.applet.widgets;

import javax.swing.JPanel;
import java.awt.*;

import org.seasr.datatypes.datamining.table.*;

/**
	A Chart is similar to a Graph, but it does not plot
	data points.  Aggregate values are shown, such as in
	a BarChart or PieChart.
*/
public abstract class Chart extends JPanel {
	// Legend

        /* FOLLOWING VARIABLES WERE MADE PROTECTED BY RITESH

        */
	protected double legendleftoffset, legendtopoffset;
        protected double legendwidth, legendheight;
	// Offset from left
	protected double leftoffset;

	// Offset from right
	protected double rightoffset;

	// Offset from top
	protected double topoffset;

	// Offset from bottom
	protected double bottomoffset;

	// Empty space
	protected int smallspace = 5;
	int largespace = 10;
	protected double samplecolorsize = 8;

	// the data
	protected DataSet set;
	GraphSettings settings;
	protected Table table;
	protected int bins;

	// dimensions of the chart
	double graphwidth, graphheight;
	protected int gridsize;

	// labels to show
	protected String title, xlabel, ylabel;

	// Font
	Font font;
	protected FontMetrics metrics;
	protected int fontheight, fontascent;

	// color generation
	/*Color[] colors = {
		new Color(253, 204, 138), new Color(148, 212, 161),
		new Color(153, 185, 216), new Color(189, 163, 177),
		new Color(213, 213, 157), new Color(193,  70,  72),
		new Color( 29, 136, 161), new Color(187, 116, 130),
		new Color(200, 143,  93), new Color(127, 162, 133)
	};*/

/*	Color[] colors = {new Color(71, 74, 98), new Color(191, 191, 115),
		new Color(111, 142, 116), new Color(178, 198, 181),
		new Color(153, 185, 216), new Color(96, 93, 71),
		new Color(146, 205, 163), new Color(203, 84, 84),
		new Color(217, 183, 170), new Color(140, 54, 57),
		new Color(203, 136, 76)
	};*/

        // add more colors - Dora Cai 03/09/29
       protected Color[] colors = {new Color(30, 60, 90), new Color(30, 60, 150),
               new Color(30, 120, 90), new Color(30, 120, 150),
               new Color(90, 60, 90), new Color(90, 60, 150),
               new Color(90, 120, 90), new Color(150, 90, 30),
               new Color(150, 90, 90), new Color(150, 90, 150),
               new Color(150, 90, 210), new Color(150, 150, 30),
               new Color(150, 150, 90), new Color(150, 150, 210),
               new Color(210, 90, 30), new Color(210, 90, 90),
               new Color(210, 90, 150), new Color(210, 90, 210),
               new Color(210, 150, 30), new Color(210, 210, 30),
               new Color(240, 90, 30), new Color(240, 90, 90),
               new Color(240, 90, 120)
       };


	abstract public void initOffsets();
	abstract public void resize();

	public Chart(Table t, DataSet d, GraphSettings g) {
		table = t;
		set = d;
		settings = g;
		bins = table.getNumRows();
	}

	/**
		Get a color.
	*/
	public Color getColor(int i) {
		return colors[i % colors.length];
	}
}
