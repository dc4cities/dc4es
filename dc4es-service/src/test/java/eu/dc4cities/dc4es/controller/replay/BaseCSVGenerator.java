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

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 *
 * 
 */
public class BaseCSVGenerator {

	protected File tmpCsvFile;

    int timeIntervalInMillis = 15 * 60 * 1000;
    int timeIntervalInMinutesForReq = 15;
    String delimiter = ";";
    int powerRef = 1000;
    int renPercRef = 40;
    int emRef = 10;
    double  primEnergyRef = 10;
    double priceRef = 10;

    DateTime basis = new DateTime(2014, 6, 1, 0, 0, 0, 0);
    DateTime dateTime = new DateTime(2014, 6, 1, 0, 0, 0, 0);

	@Before
	public void before() {
		// save to tmp
		tmpCsvFile = new File(FileUtils.getTempDirectory(),
                "forecastreplay_"
				+ getClass().getSimpleName()
                + "_" + System.currentTimeMillis()
				+ ".csv");


        try {
            FileWriter writer = new FileWriter(tmpCsvFile);
            int power = powerRef;
            int renPerc = renPercRef;
            int em = emRef;
            Random r = new Random(42L);
            Random r2 = new Random(42L);
            for (int i = 0; i < 200; i++) {
                writer.append(dateTime.toString(DateTimeFormat
                        .forPattern(ReplayConstants.DATE_TIME_PATTERN)));
                writer.append(delimiter);
                writer.append(String.valueOf(power));
                writer.append(delimiter);
                writer.append(String.valueOf(renPerc));
                writer.append(delimiter);
                writer.append(String.valueOf(em));
                writer.append(delimiter);
                writer.append(String.valueOf(primEnergyRef));
                writer.append(delimiter);
                writer.append(String.valueOf(priceRef));
                writer.append('\n');
                dateTime = dateTime.plusMillis(timeIntervalInMillis);
                power = powerRef - 500 + r.nextInt(1000);
                renPerc = renPercRef - 30 + r.nextInt(60);
                em = emRef - 8 + r.nextInt(16);
                r2.nextInt(16);
                r2.nextInt(16);
                primEnergyRef = r2.nextDouble() * 5;
                priceRef = r2.nextDouble() * 20;
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    @Test
    public void test() {
        tmpCsvFile.exists();
    }
	@After
	public void after() {
		// remove tmp file
		FileUtils.deleteQuietly(tmpCsvFile);
	}
}
