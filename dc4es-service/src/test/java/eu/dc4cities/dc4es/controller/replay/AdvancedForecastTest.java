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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;

import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Test;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.dc4es.model.ForecastRequest;
import eu.dc4cities.dc4es.unit.AdvancedUnit;

/**
 * This test case checks the correct functionality of the csv reader and tests
 * the correctness of the linear interpolation which is needed in cases where
 * the time intervals within the forecast request do not match the ones in the
 * csv files
 * 
 *
 * 
 */
public class AdvancedForecastTest extends Base {
	int timeIntervalInMillis = 15 * 60 * 1000;
	int timeIntervalInMinutesForReq = 15;
	String delimiter = ";";
	int powerRef = 1000;
	int renPercRef = 40;
	int emRef = 10;
	int startcountingnumber = 0;

	@Test
	public void test() throws IOException {

		DateTime basis = new DateTime(2014, 6, 1, 10, 0, 0, 0);
		DateTime dateTime = new DateTime(2014, 6, 1, 10, 0, 0, 0);

		try {
			FileWriter writer = new FileWriter(tmpCsvFile);
			int power = powerRef;
			int renPerc = renPercRef;
			int em = emRef;
			for (int i = 0; i < 10; i++) {
				writer.append(dateTime.toString(DateTimeFormat
						.forPattern(ReplayConstants.DATE_TIME_PATTERN)));
				writer.append(delimiter);
				writer.append(String.valueOf(power));
				writer.append(delimiter);
				writer.append(String.valueOf(renPerc));
				writer.append(delimiter);
				writer.append(String.valueOf(em));
				writer.append('\n');
				dateTime = dateTime.plusMillis(timeIntervalInMillis);
				power = powerRef - 500 + i * 15;
				renPerc = renPercRef - 30 + 3 * i;
				em = emRef - 8 + 3 * i;
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("=============== CSV ======================");
		System.out.println(FileUtils.readFileToString(tmpCsvFile));
		System.out.println("=============== CSV ======================");

		ReplaySimControllerImpl sim = new ReplaySimControllerImpl(
				tmpCsvFile.getAbsolutePath(), delimiter);

		DateTime ft = basis.minusHours(1);
		DateTime from = basis.plusMillis(15000 * 60);
		DateTime to = basis.plusMillis(30000 * 60);

		System.out.println("FT " + ft.toString() + ", FROM " + from.toString()
				+ ", TO " + to.toString());

		// ****** TEST WITH INTERVALLS MATCHING WITH THE ONES IN THE CSV ******
		TimeSlotBasedEntity timeSlotBasedEntity = new TimeSlotBasedEntity();

		timeSlotBasedEntity.setDateFrom(from);
		timeSlotBasedEntity.setDateTo(to);

		Amount<Duration> timeSlotDuration = Amount.valueOf(15, NonSI.MINUTE);
		timeSlotBasedEntity.setTimeSlotDuration(timeSlotDuration);

		System.out.println(timeSlotBasedEntity.getDateFrom() + " to "
				+ timeSlotBasedEntity.getDateTo());

		ForecastRequest req = new ForecastRequest(timeSlotBasedEntity);
		// ERDS name
		String erdsName = "some_erds";
		req.setErdsName(erdsName);

		Object[] out = new Object[4];
		ErdsForecast resp = sim.getForecast(req);

		System.out.println(ToStringBuilder.reflectionToString(resp));

		int i = 0;
		for (TimeSlotErdsForecast tvp : resp.getTimeSlotForecasts()) {
			System.out.println("*** " + Integer.toString(tvp.getTimeSlot()));
			out[i] = Integer.toString(tvp.getTimeSlot());

			i++;
			System.out.println(tvp.getPower());
			out[i] = tvp.getPower();
			i++;
			System.out.println(tvp.getCo2Factor());
			out[i] = tvp.getCo2Factor();
			i++;
			System.out.println(tvp.getRenewablePercentage());
			out[i] = tvp.getRenewablePercentage();
			i++;
			System.out.println("");
		}
		// String[] exp = { "10.15.00", "500", "2", "10", "10.30.00", "515",
		// "5",
		// "13" };

		Object[] exp = { Integer.toString(startcountingnumber), Amount.valueOf(500L, javax.measure.unit.SI.WATT),
				Amount.valueOf(2L, GasEmission.UNIT),
				Amount.valueOf(10L, javax.measure.unit.NonSI.PERCENT)
//                , "2",
//				Amount.valueOf(515L, javax.measure.unit.SI.WATT),
//				Amount.valueOf(5L, AdvancedUnit.CEF),
//				Amount.valueOf(13L, javax.measure.unit.NonSI.PERCENT)
        };
		Assert.assertArrayEquals(exp, out);

		// ****** TEST WITH INTERVALLS SMALLER THAN THE ONES IN THE CSV ******
		timeSlotBasedEntity.setTimeSlotDuration(Amount.valueOf(
				(timeIntervalInMinutesForReq / 3),
				javax.measure.unit.NonSI.MINUTE));
		ForecastRequest req2 = new ForecastRequest(timeSlotBasedEntity);

		System.out.println(timeSlotBasedEntity.getDateFrom() + " to "
				+ timeSlotBasedEntity.getDateTo());

		Object[] out2 = new Object[4 * 3];
		ErdsForecast resp2 = sim.getForecast(req2);
		int i2 = 0;
		for (TimeSlotErdsForecast tvp : resp2.getTimeSlotForecasts()) {
			System.out.println("*** " + Integer.toString(tvp.getTimeSlot()));
			out2[i2] = Integer.toString(tvp.getTimeSlot());

			i2++;
			System.out.println(tvp.getPower());
			out2[i2] = tvp.getPower();
			i2++;
			System.out.println(tvp.getCo2Factor());
			out2[i2] = tvp.getCo2Factor();
			i2++;
			System.out.println(tvp.getRenewablePercentage());
			out2[i2] = tvp.getRenewablePercentage();
			i2++;
			System.out.println("");
		}

		System.out.println(Arrays.toString(out2));

		Object[] exp2 = { Integer.toString(startcountingnumber),
				Amount.valueOf(500L, javax.measure.unit.SI.WATT),
				Amount.valueOf(2L, GasEmission.UNIT),
				Amount.valueOf(10L, javax.measure.unit.NonSI.PERCENT), Integer.toString(startcountingnumber+1),
				Amount.valueOf(505L, javax.measure.unit.SI.WATT),
				Amount.valueOf(3L, GasEmission.UNIT),
				Amount.valueOf(11L, javax.measure.unit.NonSI.PERCENT), Integer.toString(startcountingnumber+2),
				Amount.valueOf(510L, javax.measure.unit.SI.WATT),
				Amount.valueOf(4L, GasEmission.UNIT),
				Amount.valueOf(12L, javax.measure.unit.NonSI.PERCENT)
//                , "4",
//				Amount.valueOf(515L, javax.measure.unit.SI.WATT),
//				Amount.valueOf(5L, AdvancedUnit.CEF),
//				Amount.valueOf(13L, javax.measure.unit.NonSI.PERCENT)
        };
		Assert.assertArrayEquals(exp2, out2);

		// ****** TEST WITH INTERVALLS LARGER THAN THE ONES IN THE CSV ******
		timeSlotBasedEntity.setTimeSlotDuration(Amount.valueOf(30,
				javax.measure.unit.NonSI.MINUTE));
		timeSlotBasedEntity.setDateTo(to.plusMinutes(15));
		ForecastRequest req3 = new ForecastRequest(timeSlotBasedEntity);

		System.out.println(timeSlotBasedEntity.getDateFrom() + " to "
				+ timeSlotBasedEntity.getDateTo());

		Object[] out3 = new Object[4];
		ErdsForecast resp3 = sim.getForecast(req3);

		int i3 = 0;
		for (TimeSlotErdsForecast tvp : resp3.getTimeSlotForecasts()) {
			System.out.println("*** " + Integer.toString(tvp.getTimeSlot()));
			out3[i3] = Integer.toString(tvp.getTimeSlot());

			i3++;
			System.out.println(tvp.getPower());
			out3[i3] = tvp.getPower();
			i3++;
			System.out.println(tvp.getCo2Factor());
			out3[i3] = tvp.getCo2Factor();
			i3++;
			System.out.println(tvp.getRenewablePercentage());
			out3[i3] = tvp.getRenewablePercentage();
			i3++;
			System.out.println("");
		}
		Object[] exp3 = { Integer.toString(startcountingnumber),
				Amount.valueOf(500L, javax.measure.unit.SI.WATT),
				Amount.valueOf(2L, GasEmission.UNIT),
				Amount.valueOf(10L, javax.measure.unit.NonSI.PERCENT)
//                , "2",
//				Amount.valueOf(530L, javax.measure.unit.SI.WATT),
//				Amount.valueOf(8L, AdvancedUnit.CEF),
//				Amount.valueOf(16L, javax.measure.unit.NonSI.PERCENT)
        };
		Assert.assertArrayEquals(exp3, out3);

		// ****** TEST WITH TIME IN REQ THAT DOES NOT EXACTELY MATCH A TIME IN
		// THE CSV ******
		timeSlotBasedEntity.setTimeSlotDuration(Amount.valueOf(10,
				javax.measure.unit.NonSI.MINUTE));
		timeSlotBasedEntity.setDateFrom(from.plusMinutes(5));
		timeSlotBasedEntity.setDateTo(from.plusMinutes(15));

		ForecastRequest req4 = new ForecastRequest(timeSlotBasedEntity);

		System.out.println(timeSlotBasedEntity.getDateFrom() + " to "
				+ timeSlotBasedEntity.getDateTo());

		Object[] out4 = new Object[4];
		ErdsForecast resp4 = sim.getForecast(req4);
		int i4 = 0;
		for (TimeSlotErdsForecast tvp : resp4.getTimeSlotForecasts()) {
			System.out.println("*** " + Integer.toString(tvp.getTimeSlot()));
			out4[i4] = Integer.toString(tvp.getTimeSlot());
			i4++;
			System.out.println(tvp.getPower());
			out4[i4] = tvp.getPower();
			i4++;
			System.out.println(tvp.getCo2Factor());
			out4[i4] = tvp.getCo2Factor();
			i4++;
			System.out.println(tvp.getRenewablePercentage());
			out4[i4] = tvp.getRenewablePercentage();
			i4++;
			System.out.println("");
		}
		Object[] exp4 = { Integer.toString(startcountingnumber),
				Amount.valueOf(505L, javax.measure.unit.SI.WATT),
				Amount.valueOf(3L, GasEmission.UNIT),
				Amount.valueOf(11L, javax.measure.unit.NonSI.PERCENT)
//                , "2",
//				Amount.valueOf(515L, javax.measure.unit.SI.WATT),
//				Amount.valueOf(5L, AdvancedUnit.CEF),
//				Amount.valueOf(13L, javax.measure.unit.NonSI.PERCENT)
        };
		Assert.assertArrayEquals(exp4, out4);

	}

}
