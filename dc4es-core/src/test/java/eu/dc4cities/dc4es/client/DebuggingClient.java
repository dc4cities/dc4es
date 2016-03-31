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

package eu.dc4cities.dc4es.client;

import java.io.IOException;

import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;

import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.dc4es.client.ForecastClient;

/**
 * Simple debugging client for testing purposes
 * 
 *
 * 
 */
public class DebuggingClient {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ForecastClient forecastClient = new ForecastClient(
				"http://localhost:8080/dc4es-service/");

		// forecast request
		TimeSlotBasedEntity timeSlotBasedEntity = new TimeSlotBasedEntity();

		// test whole day (96 slots), 15. August 2014 00:00:00 - 23:45:00
		DateTime basis = eu.dc4cities.dc4es.converter.DateTimeFormat.FORMAT
				.parseDateTime("2014-08-15T00:00:00");

		DateTime from = basis.toDateTime();
		DateTime to = basis.plusHours(23).plusMinutes(45);

		timeSlotBasedEntity.setDateFrom(from);
		timeSlotBasedEntity.setDateTo(to);

		Amount<Duration> timeSlotDuration = Amount.valueOf(15, NonSI.MINUTE);
		timeSlotBasedEntity.setTimeSlotDuration(timeSlotDuration);

		// TODO ERDS name
		String erdsName = "spanish_grid";

		ErdsForecast erdsForecast = forecastClient.getForecast(erdsName,
				timeSlotBasedEntity);

		// TODO do something
	}
}
