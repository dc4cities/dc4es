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

package eu.dc4cities.dc4es.controller;

import static javax.measure.unit.NonSI.PERCENT;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;
import static javax.measure.unit.SI.WATT;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Duration;

import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.jscience.physics.amount.Amount;

import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.metrics.MetricCatalog;
import eu.dc4cities.dc4es.configuration.Configuration;
import eu.dc4cities.dc4es.model.ForecastRequest;
import eu.dc4cities.dc4es.unit.AdvancedUnit;
import eu.dc4cities.energis.client.HttpClient;
import eu.dc4cities.energis.client.builder.Granularity;
import eu.dc4cities.energis.client.builder.QueryBuilder;
import eu.dc4cities.energis.client.builder.TimeValue;
import eu.dc4cities.energis.client.response.QueryResponse;

/**
 * 
 *
 *
 */
public class ForecastControllerImpl implements ForecastController {

	private Configuration configuration;

	public ForecastControllerImpl(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public ErdsForecast getForecast(ForecastRequest forecastRequest) {
		ErdsForecast erdsForecast = new ErdsForecast(forecastRequest.getErdsName());
		boolean needsForecast = configuration.needsForecast();
		if (needsForecast) {
			QueryResponse queryResponse = getFromForecaster(forecastRequest, MetricCatalog.RENEWABLE_POWER + ".forecasted");
			DateTime dateFrom = forecastRequest.getDateFrom();
			DateTime dateTo = forecastRequest.getDateTo();
			erdsForecast.setDateFrom(dateFrom);
			erdsForecast.setDateTo(dateTo);
			Amount<Duration> slotDuration = null;

			if (queryResponse.getTimeValues().size() > 1) {
				long timeDiff = queryResponse.getTimeValues().get(1).getTimestamp().getMillis()
						- queryResponse.getTimeValues().get(0).getTimestamp().getMillis();
				slotDuration = Amount.valueOf(timeDiff, MILLI(SECOND));
			}
			if (forecastRequest.getTimeSlotDuration().compareTo(slotDuration) != 0) {
				List<TimeValue> interpolated = interpolate(queryResponse.getTimeValues(), forecastRequest);
				queryResponse = new QueryResponse(interpolated);
			}

			erdsForecast.setTimeSlotDuration(forecastRequest.getTimeSlotDuration());
			List<TimeSlotErdsForecast> timeSlotsErdsForecastList = new ArrayList<TimeSlotErdsForecast>();
			int i = 1;

			for (TimeValue currentTimeValue : queryResponse.getTimeValues()) {
				TimeSlotErdsForecast timeSlotErdsForecast = new TimeSlotErdsForecast(i);
				timeSlotErdsForecast.setPower(Amount.valueOf((long) currentTimeValue.getValue(), WATT));
				timeSlotsErdsForecastList.add(timeSlotErdsForecast);
				i++;
			}

			if (configuration.getStaticCarbonEmissionValue().getExactValue() == -1) {
				queryResponse = getFromForecaster(forecastRequest, MetricCatalog.CARBON_EMISSION_VALUE + ".actual");
				if (queryResponse.getTimeValues().size() > 1) {
					long timeDiff = queryResponse.getTimeValues().get(1).getTimestamp().getMillis()
							- queryResponse.getTimeValues().get(0).getTimestamp().getMillis();
					slotDuration = Amount.valueOf(timeDiff, MILLI(SECOND));
				}
				if (!forecastRequest.getTimeSlotDuration().equals(slotDuration)) {
					List<TimeValue> interpolated = interpolate(queryResponse.getTimeValues(), forecastRequest);
					queryResponse = new QueryResponse(interpolated);
				}
				i = 0;

				for (TimeValue currentTimeValue : queryResponse.getTimeValues()) {
					TimeSlotErdsForecast timeSlotErdsForecast = timeSlotsErdsForecastList.get(i);
					timeSlotErdsForecast.setCo2Factor(Amount.valueOf((long) currentTimeValue.getValue(), GasEmission.UNIT));
					i++;
				}

			} else {
				for (TimeSlotErdsForecast currentTimeSlotErdsForecast : timeSlotsErdsForecastList) {
					currentTimeSlotErdsForecast.setCo2Factor(configuration.getStaticCarbonEmissionValue());
				}
			}

			if (configuration.getStaticRenewableEnergyPercentage().getExactValue() == -1) {
				queryResponse = getFromForecaster(forecastRequest, MetricCatalog.RENEWABLE_ENERGY_PERCENTAGE + ".forecasted");

				if (queryResponse.getTimeValues().size() > 1) {
					long timeDiff = queryResponse.getTimeValues().get(1).getTimestamp().getMillis()
							- queryResponse.getTimeValues().get(0).getTimestamp().getMillis();
					slotDuration = Amount.valueOf(timeDiff, MILLI(SECOND));
				}
				if (!forecastRequest.getTimeSlotDuration().equals(slotDuration)) {
					List<TimeValue> interpolated = interpolate(queryResponse.getTimeValues(), forecastRequest);
					queryResponse = new QueryResponse(interpolated);
				}

				i = 0;

				for (TimeValue currentTimeValue : queryResponse.getTimeValues()) {
					TimeSlotErdsForecast timeSlotErdsForecast = timeSlotsErdsForecastList.get(i);
					timeSlotErdsForecast.setRenewablePercentage(Amount.valueOf((long) currentTimeValue.getValue(), PERCENT));
					i++;
				}

				erdsForecast.setTimeSlotForecasts(timeSlotsErdsForecastList);
			} else {
				for (TimeSlotErdsForecast currentTimeSlotErdsForecast : timeSlotsErdsForecastList) {
					currentTimeSlotErdsForecast.setRenewablePercentage(configuration.getStaticRenewableEnergyPercentage());
				}
			}
			erdsForecast.setTimeSlotForecasts(timeSlotsErdsForecastList);
		} else {
			// getFromHistoricalDB(forecastRequest);
		}

		return erdsForecast;
	}

	private List<TimeValue> interpolate(List<TimeValue> timeValues, ForecastRequest forecastRequest) {
		long timeDiff = timeValues.get(1).getTimestamp().getMillis() - timeValues.get(0).getTimestamp().getMillis();
		List<DateTime> requestedTimestamps = new ArrayList<>();
		List<TimeValue> interpolatedTimeValueList = new ArrayList<>();
		DateTime dateFrom = forecastRequest.getDateFrom();
		DateTime dateTo = forecastRequest.getDateTo();
		DateTime currentDateTime = dateFrom;
		while (currentDateTime.isBefore(dateTo) || currentDateTime.equals(dateTo)) {
			requestedTimestamps.add(currentDateTime);
			currentDateTime = currentDateTime.plus(forecastRequest.getTimeSlotDuration().longValue(MILLI(SECOND)));
		}
		Amount<Duration> actualTimeSlotDuration = Amount.valueOf(timeDiff, MILLI(SECOND));
		for (int i = 0; i < requestedTimestamps.size(); i++) {
			LocalDateTime currentTime = requestedTimestamps.get(i).toLocalDateTime();
			for (int j = 0; j < timeValues.size(); j++) {
				TimeValue currentTimeValuePair = timeValues.get(j);
				if (currentTime.equals(currentTimeValuePair.getTimestamp().toLocalDateTime())) {
					TimeValue interpolated = new TimeValue(currentTime.toDateTime(DateTimeZone.UTC), currentTimeValuePair.getValue());
					interpolatedTimeValueList.add(interpolated);
					break;
				} else if ((currentTimeValuePair.getTimestamp().plus(actualTimeSlotDuration.longValue(MILLI(SECOND)))).toLocalDateTime().isAfter(currentTime)) {
					long toPreviousSlot = currentTime.toDateTime(DateTimeZone.UTC).getMillis() - currentTimeValuePair.getTimestamp().toLocalDateTime().toDateTime(DateTimeZone.UTC).getMillis();
					double pctNextSlot = (double) toPreviousSlot / (double) actualTimeSlotDuration.longValue(MILLI(SECOND));
					int intValue = (int) ((1 - pctNextSlot) * currentTimeValuePair.getValue() + pctNextSlot * timeValues.get(j + 1).getValue());
					TimeValue interpolated = new TimeValue(currentTime.toDateTime(DateTimeZone.UTC), intValue);
					interpolatedTimeValueList.add(interpolated);
					break;
				}
			}
		}
		return interpolatedTimeValueList;
	}

	private QueryResponse getFromForecaster(ForecastRequest forecastRequest, String metric) {
		QueryBuilder queryBuilder = QueryBuilder.getInstance();

		String host = configuration.getForecasterURL().getHost();

		HttpClient client = new HttpClient(host, 80, "test");
		client.setRetryCount(10);

		DateTime startAbsolute = forecastRequest.getDateFrom().minusMinutes(forecastRequest.getDateFrom().getMinuteOfHour())
				.minusSeconds(forecastRequest.getDateFrom().getSecondOfMinute());
		DateTime endAbsolute;
		if (forecastRequest.getDateTo().getMinuteOfHour() == 0 && forecastRequest.getDateTo().getSecondOfMinute() == 0) {
			endAbsolute = forecastRequest.getDateTo().plusHours(1);
		} else {
			endAbsolute = forecastRequest.getDateTo().plusHours(2).minusMinutes(forecastRequest.getDateTo().getMinuteOfHour())
					.minusSeconds(forecastRequest.getDateTo().getSecondOfMinute());
		}
		String assetCode = forecastRequest.getErdsName();

		queryBuilder.setStart(startAbsolute).setEnd(endAbsolute).setMetricName(metric).setGranularity(new Granularity(Granularity.Unit.HOURS, 1))
				.setCompanyCode("dc4c").setAssetCode(assetCode);

		QueryResponse queryResponse = null;

		try {
			queryResponse = client.query(queryBuilder);
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return queryResponse;
	}
}
