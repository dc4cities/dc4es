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

import static javax.measure.unit.NonSI.MINUTE;

import javax.measure.unit.NonSI;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.dc4es.model.ForecastRequest;

public class CSVReaderTest extends BaseCSVGenerator {
	@Test
	public void test() {
		// Test the functionality for when the interval is equivalent for the
		// forecast and the csv
		ReplaySimControllerImpl sim = new ReplaySimControllerImpl(
				tmpCsvFile.getAbsolutePath(), delimiter);

		DateTime from = basis.plusMillis(15 * 60000);
		DateTime to = basis.plusMillis(720 * 60000);

		TimeSlotBasedEntity timeSlotBasedEntity = new TimeSlotBasedEntity();

		timeSlotBasedEntity.setDateFrom(from);
		timeSlotBasedEntity.setDateTo(to);
		timeSlotBasedEntity.setTimeSlotDuration(Amount
				.valueOf(15, NonSI.MINUTE));

		ForecastRequest forecastRequest = new ForecastRequest(
				timeSlotBasedEntity);
		forecastRequest.setErdsName("replay.grid");

		ErdsForecast resp = sim.getForecast(forecastRequest);
		for (TimeSlotErdsForecast timeSlotErdsForecast : resp
				.getTimeSlotForecasts()) {
			System.out.println("*** "
					+ timeSlotBasedEntity
							.getDateFrom()
							.plusMinutes(
									(int) timeSlotBasedEntity
											.getTimeSlotDuration()
											.times(timeSlotErdsForecast
													.getTimeSlot() - 1)
											.longValue(MINUTE))
							.toString(DateTimeFormat.forPattern("hh.mm.ss"))
					+ " ***");
			System.out.println(timeSlotErdsForecast.getPower());
			System.out.println(timeSlotErdsForecast.getCo2Factor());
			System.out.println(timeSlotErdsForecast.getRenewablePercentage());
			System.out.println(timeSlotErdsForecast.getConsumptionPrice());
			System.out.println("");
		}

	}

}
