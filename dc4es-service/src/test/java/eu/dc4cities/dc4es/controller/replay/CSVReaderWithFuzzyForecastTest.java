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

import javax.measure.unit.NonSI;

import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.dc4es.model.ForecastRequest;

public class CSVReaderWithFuzzyForecastTest extends BaseCSVGenerator {
	double fuzzynessFactor = 1.01;

	@Test
	public void test() {
		// Test the functionality for when the interval is equivalent for the
		// forecast and the csv
		ReplaySimControllerImpl sim = new ReplaySimControllerImpl(
				tmpCsvFile.getAbsolutePath(),ReplayConstants.DATE_TIME_PATTERN, delimiter, fuzzynessFactor);

		DateTime ft = basis.minusHours(1);
		DateTime from = basis.plusMillis(15000*60);
		DateTime to = basis.plusMillis(30000*60);

		TimeSlotBasedEntity timeSlotBasedEntity = new TimeSlotBasedEntity();

		timeSlotBasedEntity.setDateFrom(from);
		timeSlotBasedEntity.setDateTo(to);
		timeSlotBasedEntity.setTimeSlotDuration(Amount
				.valueOf(15, NonSI.MINUTE));



		ForecastRequest forecastRequest = new ForecastRequest(
				timeSlotBasedEntity);
		forecastRequest.setErdsName("replay.grid");

		ErdsForecast resp = sim.getForecast(forecastRequest);

		for (TimeSlotErdsForecast tvp : resp.getTimeSlotForecasts()){




//			System.out.println("*** " + tvp.getTime().toString(DateTimeFormat.forPattern("hh.mm.ss")) + " ***");
			System.out.println("*** " + tvp.getTimeSlot() + " ***");
			System.out.println(tvp.getPower());
			System.out.println(tvp.getCo2Factor());
			System.out.println(tvp.getRenewablePercentage());
			System.out.println("");
		}

	}

}
