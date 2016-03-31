/*
 * Copyright 2016 The DC4Cities author.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.dc4cities.dc4es.controller.replay;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

/**
 *
 * 
 */
public class Base {

	protected File tmpCsvFile;

	@Before
	public void before() {
		// save to tmp
		tmpCsvFile = new File(FileUtils.getTempDirectory(), "forecastreplay_"
				+ getClass().getSimpleName() + "_" + System.currentTimeMillis()
				+ ".csv");
	}

	@After
	public void after() {
		// remove tmp file
		FileUtils.deleteQuietly(tmpCsvFile);
	}
}
