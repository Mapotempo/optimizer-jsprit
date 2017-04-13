/***
 * Copyright © Mapotempo, 2016
 *
 * This file is part of Mapotempo.
 *
 * Mapotempo is free software. You can redistribute it and/or
 * modify since you respect the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Mapotempo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the Licenses for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Mapotempo. If not, see:
 * <http://www.gnu.org/licenses/agpl.html>
***/
package com.mapotempo.optimizer.jsprit;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class RunTest {
	@Test
	public void testMainNoOption() throws IOException {
		Run.main(new String[] {});
	}

	@Test
	public void testMainHelpOption() throws IOException {
		Run.main(new String[] { "--help" });
	}

	@Test
	public void testMain() throws IOException {
		String matrix = this.getClass().getClassLoader().getResource("time-2.matrix").getPath();
		String instance = this.getClass().getClassLoader().getResource("v1s2.xml").getPath();
		String solution = File.createTempFile("solution", "").getAbsolutePath();
		String solveTime = Integer.toString(100);
		Run.main(new String[] { "--time_matrix", matrix, "--instance", instance, "--solution", solution, "--ms", solveTime , "--nearby"});
	}

	@Test
	public void testRun() throws IOException {
		String matrix = this.getClass().getClassLoader().getResource("time-2.matrix").getPath();
		String instance = this.getClass().getClassLoader().getResource("v1s2.xml").getPath();
		String solution = File.createTempFile("solution", "").getAbsolutePath();
		new Run("algorithmConfig.xml", solution, matrix, null, instance, null, false, 100, null, null, null, null, 1, false, true, null);
	}
}
