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

import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import eu.dc4cities.dc4es.controller.ForecastController;
import eu.dc4cities.dc4es.controller.replay.csv.CSVReader;
import eu.dc4cities.dc4es.model.ForecastRequest;

/**
 * The implementation of the Forecast Controller Interface. With the help of
 * this class it is possible to get a forecast. The replay simulator is based on
 * a csv file containing the time, and the corresponding power, renewable energy
 * percentage and emission factor.
 * 
 *
 * 
 */
public class ReplaySimControllerImpl implements ForecastController {

	private static final Logger LOG = LoggerFactory
			.getLogger(ReplaySimControllerImpl.class);

	// int timeInverval;
	String filename;
	String dateTimePattern;
	String delimiter;
	double fuzzynessFactor;

	/**
	 * Constructor initializing the following fields:
	 * 
	 * @param filename
	 *            The filename including the path where the csv input file is
	 *            found
	 * @param dateTimePattern
	 *            DateTime pattern to be used with Joda
	 * @param delimiter
	 *            The seperator which is used in the csv file. E.g. "," or ";"
	 * @param fuzzynessFactor
	 *            A factor which influences the preciseness of the forecast. The
	 *            further away the requested forecast from the actual time of
	 *            request the more fuzzy the forecast gets. It uses the function
	 *            x -&gt; a^x. The fuzzyness factor is "a". An example for "a"
	 *            could be 1.01 concluding in a growth rate of 1% per interval.
	 */
	public ReplaySimControllerImpl(String filename, String dateTimePattern,
			String delimiter, double fuzzynessFactor) {
		this.filename = filename;
		this.dateTimePattern = dateTimePattern;
		this.delimiter = delimiter;
		this.fuzzynessFactor = fuzzynessFactor;
	}

	/**
	 * Constructor initializing the following fields:
	 * 
	 * @param filename
	 *            The filename including the path where the csv input file is
	 *            found
	 * @param delimiter
	 *            The seperator which is used in the csv file. E.g. "," or ";"
	 */
	public ReplaySimControllerImpl(String filename, String delimiter) {
		this.filename = filename;
		this.dateTimePattern = "dd-MMM-yy hh.mm.ss aa";
		this.delimiter = delimiter;
		this.fuzzynessFactor = 0;
	}

	/**
	 * Constructor initializing the following field (Note the delimiter is set
	 * to "," and the fuzzynessFactor to 0 here):
	 * 
	 * @param filename
	 *            The filename including the path where the csv input file is
	 *            found
	 */
	public ReplaySimControllerImpl(String filename) {
		this.filename = filename;
		this.dateTimePattern = ReplayConstants.DATE_TIME_PATTERN;
		this.delimiter = ",";
		this.fuzzynessFactor = 0;
	}

	/**
	 * Constructor initializing the following fields (Note the delimiter is set
	 * to "," here):
	 * 
	 * @param filename
	 *            The filename including the path where the csv input file is
	 *            found
	 * @param fuzzynessFactor
	 *            A factor which influences the preciseness of the forecast. The
	 *            further away the requested forecast from the actual time of
	 *            request the more fuzzy the forecast gets. It uses the function
	 *            x -&gt; a^x. The fuzzyness factor is "a". An example for "a"
	 *            could be 1.01 concluding in a growth rate of 1% per interval.
	 */
	public ReplaySimControllerImpl(String filename, double fuzzynessFactor) {
		this.filename = filename;
		this.dateTimePattern = ReplayConstants.DATE_TIME_PATTERN;
		this.delimiter = ",";
		this.fuzzynessFactor = fuzzynessFactor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.dc4cities.dc4es.controller.ForecastController#getForecast(eu.dc4cities
	 * .dc4es.model.ForecastRequest)
	 */
	@Override
	public ErdsForecast getForecast(ForecastRequest req) {
		ErdsForecast response = new ErdsForecast(req.getErdsName());
		List<TimeSlotErdsForecast> timeSlotErdsForecastList = new ArrayList<TimeSlotErdsForecast>();
		int timeInverval = (int) req.getTimeSlotDuration().longValue(
				MILLI(SECOND));
		try {
			timeSlotErdsForecastList = new CSVReader(delimiter).read(filename,
					dateTimePattern, req.getDateFrom(), req.getDateTo(),
					timeInverval);
		} catch (Throwable e) {
			LOG.warn("Cannot read and evaluate CSV file " + filename
					+ ". Error: " + e.getMessage(), e);

			throw new IllegalArgumentException(
					"Cannot read and evaluate CSV file " + filename
							+ ". Error: " + e.getMessage(), e);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("No. of TimeSlotErdsForecast returned by CSVReader "
					+ timeSlotErdsForecastList.size());
		}

		int maxNumberOfSlots = ((int) ((double) (req.getDateTo().getMillis() - req
				.getDateFrom().getMillis()) / (double) timeInverval));
		List<TimeSlotErdsForecast> timeSlotForecastList = new ArrayList<>();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Maximum no. of slots " + maxNumberOfSlots);
		}

		// slot
		int slot = 0;
		while (slot < maxNumberOfSlots) {
			// for (int slot : slots) {
			if (slot < timeSlotErdsForecastList.size()) {
				// if (slot < maxNumberOfSlots) {
				TimeSlotErdsForecast temp = timeSlotErdsForecastList.get(slot);
				double rand = 0;

				int timeDiff = (int) ((req.getTimeSlotDuration()
						.times(temp.getTimeSlot()).longValue(MILLI(SECOND)) - req
						.getDateFrom().getMillis()) / timeInverval);
				if (fuzzynessFactor > 0) {
					if (timeDiff > 0) {
						rand = 1 + (Math.pow(fuzzynessFactor, timeDiff) - 1)
								* new Random().nextDouble();
					}
					rand = rand - 1;
					if (new Random().nextDouble() >= 0.5d) {
						rand = rand * -1;
					}
				}

				temp.setCo2Factor((Amount<GasEmission>) applyFuzzyness(rand, temp.getCo2Factor()));
				temp.setPower((Amount<Power>) applyFuzzyness(rand, temp.getPower()));
				temp.setRenewablePercentage((Amount<Dimensionless>) applyFuzzyness(rand, temp.getRenewablePercentage()));
				//temp.setConsumptionPrice();
				//temp.setPrimaryEnergyFactor();
				timeSlotForecastList.add(temp);
			}

			slot++;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("TimeValuePairList size = " + timeSlotForecastList.size());
		}

		response.setDateFrom(req.getDateFrom());
		response.setDateTo(req.getDateTo());
		response.setTimeSlotDuration(req.getTimeSlotDuration());
		response.setTimeSlotForecasts(timeSlotForecastList);

		return response;
	}

	private Amount<?> applyFuzzyness(double rand, Amount amount) {
		if(rand != 1.0) {
			Amount nAmount = amount.plus(
					amount.times(rand));
			// make sure we use a rounded measure
			nAmount = Amount.valueOf(amount.longValue(amount.getUnit()), amount.getUnit());
		}
		
		return amount;
	}
}
