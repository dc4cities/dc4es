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

package eu.dc4cities.dc4es.controller.replay.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.quantity.EnergyPrice;
import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import eu.dc4cities.controlsystem.model.unit.Units;

/**
 *
 * 
 */
public class CSVReader {

	private static final Logger LOG = LoggerFactory.getLogger(CSVReader.class);

	private String delimiter = ",";

	/**
	 * Constructor setting the delimiter
	 * 
	 * @param delimiter
	 *            The delimiter that is used to read the csv. E.g. "," or ";"
	 */
	public CSVReader(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Method to read the csv file
	 * 
	 * @param filename
	 *            The filename including the path where the csv file is located
	 * @param dateTimePattern
	 *            DateTime pattern to be used with Joda
	 * @param from
	 *            The start time
	 * @param to
	 *            The end time
	 * @param timeInterval
	 *            the time interval in milliseconds. Note, that this not
	 *            necessary the same interval than in the csv file
	 * @return Returns all values in the "from-to" timeframe in the provided
	 *         time interval steps
	 * @throws NumberFormatException if the CSV contains invalid values
	 * @throws IOException if there is an error reading the CSV
	 */
	public List<TimeSlotErdsForecast> read(String filename,
			String dateTimePattern, DateTime from, DateTime to, int timeInterval)
			throws NumberFormatException, IOException {
		// determine CSV interval automatically (millis)
		int csvInterval = determineCsvInterval(filename, delimiter,
				dateTimePattern);

		// check time interval
		if ((to.getMillis() - from.getMillis()) % timeInterval > 0) {
			throw new IllegalArgumentException(
					"TimeSlot definition := (dateTo - dateFrom) mod slotDuration = 0 not satisfied by given time interval = "
							+ timeInterval + " millis");
		}

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = null;
		Scanner scanner = null;
		int index = 0;
		List<TimeSlotErdsForecast> erdsList = new ArrayList<>();
		DateTime lastDate = new DateTime(from);
		DateTime thisSlot = new DateTime(from);
		List<Double> difs = new ArrayList<>();
		List<TimeSlotErdsForecast> tempERDSList = new ArrayList<>();
		boolean isInTimeRange;
		long lastPower = 0;
		int lastRenPerc = 0;
		int lastEmF = 0;
		double lastPef = 0;
		double lastCp = 0;
		boolean laterThanToDate = false;

		// increment timeslot, starting at 1
		int timeSlot = 0;

		while ((line = reader.readLine()) != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Reading LINE = " + line);
			}

			// Stop if ToDate is reached
			if (laterThanToDate) {
				break;
			}
			isInTimeRange = false;
			difs.clear();
			scanner = new Scanner(line);
			scanner.useDelimiter(delimiter);
			// Index = 0: time
			// Index = 1: power
			// Index = 2: renewable percentage
			// Index = 3: emission factor
			// Index = 4: primaryEnergyFactor
			// Index = 5: consumptionPrice
			while (scanner.hasNext()) {
				String data = scanner.next();

				if (index == 0) {
					// Get the time from the CSV
					DateTime date = DateTime.parse(data,
							DateTimeFormat.forPattern(dateTimePattern));
					if (lastDate.isAfter(to.getMillis())) {
						laterThanToDate = true;
					}
					if (!laterThanToDate) {
						if (date.equals(thisSlot)) {
							isInTimeRange = true;
						} else if (thisSlot.isBefore(date)
								&& thisSlot.isAfter(lastDate)) {
							isInTimeRange = true;
						}

						// direct or interpolation match (interpolation
						// precedes!)
						if (isInTimeRange) {
							// subject for interpolation
							if (!from.isEqual(thisSlot)
									&& timeInterval < csvInterval) {
								for (long i = timeInterval; i < csvInterval; i += timeInterval) {
									// add diff
									difs.add((double) i / csvInterval);

									TimeSlotErdsForecast interErds = new TimeSlotErdsForecast(
											timeSlot++);

									// add to list
									tempERDSList.add(interErds);

									if (LOG.isDebugEnabled()) {
										LOG.debug("Added interpolation slot + "
												+ lastDate.plusMillis((int) i));
									}
								}
							}

							// direct match, no interpolation needed
							TimeSlotErdsForecast erds = new TimeSlotErdsForecast(
									timeSlot++);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Added direct slot + " + thisSlot);
							}

							// add diff
                            // add diff
                            difs.add(((double)(date.getMillis() - lastDate
                                    .getMillis()) != 0)?(double) ((double) (thisSlot.getMillis() - lastDate
                                    .getMillis()) / (double) (date.getMillis() - lastDate
                                    .getMillis())):1);

                            // add to list
							tempERDSList.add(erds);

							if (date.equals(to)) {
								LOG.debug("DATE == TO");

								// to reached
								TimeSlotErdsForecast erds1 = new TimeSlotErdsForecast(
										timeSlot++);
								if (LOG.isDebugEnabled()) {
									LOG.debug("Added DATE = TO slot + "
											+ thisSlot);
								}

								// add diff
								difs.add((double) 1);

								// add to list
								tempERDSList.add(erds1);
							}

							// compute next slot
							if (csvInterval < timeInterval) { // less
								thisSlot = thisSlot.plusMillis(timeInterval);
							} else { // greater equals
								thisSlot = thisSlot.plusMillis(csvInterval);
							}

							if (LOG.isDebugEnabled()) {
								LOG.debug("New SLOT = " + thisSlot + " SIZE "
										+ difs.size());
							}
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("NOT in range " + data + "(" + from
										+ " TO " + to + ") SLOT " + thisSlot
										+ " LAST DATE " + lastDate);
							}
						}
					}
					lastDate = new DateTime(date);
				} else if (index == 1 && !laterThanToDate) {
					// Get the power from the CSV
					long currentPow = Long.parseLong(data);
					if (isInTimeRange) {
						long power = currentPow - lastPower;
						int i = 0;
						// Do linear interpolation
						for (TimeSlotErdsForecast bean : tempERDSList) {
							LOG.debug("Current POWER " + currentPow + " DIFF "
									+ difs.get(i) + " Last POWER " + lastPower);

							Amount<Power> amount = Amount.valueOf(
									(long) (lastPower + (power * difs.get(i))),
									javax.measure.unit.SI.WATT);
							bean.setPower(amount);

							LOG.debug("Interpolated " + amount);

							i++;
						}
					}
					lastPower = currentPow;
				} else if (index == 2 && !laterThanToDate) {
					// Get the renewable percentage from the CSV
					int currentRenPerc = Integer.parseInt(data);
					if (isInTimeRange) {
						int renPerc = currentRenPerc - lastRenPerc;
						int i = 0;
						// Do linear interpolation
						for (TimeSlotErdsForecast bean : tempERDSList) {
							LOG.debug("Current PERC " + currentRenPerc
									+ " DIFF " + difs.get(i) + " Last PERC "
									+ lastRenPerc);

							Amount<Dimensionless> amount = Amount.valueOf(
									(long) (lastRenPerc + (renPerc * difs
											.get(i))),
									javax.measure.unit.NonSI.PERCENT);
							bean.setRenewablePercentage(amount);

							LOG.debug("Interpolated " + amount);

							i++;
						}
					}
					lastRenPerc = Integer.parseInt(data);
				} else if (index == 3 && !laterThanToDate) {
					// Get the emission factor from the CSV
					int currentEmF = Integer.parseInt(data);
					if (isInTimeRange) {
						int EmF = currentEmF - lastEmF;
						int i = 0;
						// Do linear interpolation
						for (TimeSlotErdsForecast bean : tempERDSList) {
							Amount<GasEmission> amount = Amount.valueOf(
									(long) (lastEmF + (EmF * difs.get(i))),
									GasEmission.UNIT);
							bean.setCo2Factor(amount);
							i++;
						}
					}
					lastEmF = Integer.parseInt(data);
				}else if (index == 4 && !laterThanToDate) {
					// primaryEnergyFactor: Pef
					double currentPef = Double.parseDouble(data);
					if (isInTimeRange) {
						double Pef = currentPef - lastPef;
						int i = 0;
						// Do linear interpolation
						for (TimeSlotErdsForecast bean : tempERDSList) {
									double amount = (double) lastPef + (Pef * difs.get(i));
							bean.setPrimaryEnergyFactor(amount);
							i++;
						}
					}
					lastPef = Double.parseDouble(data);
				}else if (index == 5 && !laterThanToDate) {
					// consumptionPrice : Cp
					double currentCp = Double.parseDouble(data);
					if (isInTimeRange) {
						double Cp = currentCp - lastCp;
						int i = 0;
						// Do linear interpolation
						for (TimeSlotErdsForecast bean : tempERDSList) {
							Amount<EnergyPrice> amount = Amount.valueOf(
									lastCp + (Cp * difs.get(i)),
									Units.EUR_PER_KWH);
							bean.setConsumptionPrice(amount);
							i++;
						}
					}
					lastCp = Double.parseDouble(data);
				}
				index++;
			}
			index = 0;
			erdsList.addAll(tempERDSList);
			tempERDSList.clear();
		}
		scanner.close();
		reader.close();
		return erdsList;
	}

	/**
	 * @return The delimiter set for parsing the csv file. E.g.: ","
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * @param delimiter
	 *            The delimiter to be set for parsing the csv file. E.g.: ","
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Determine CSV interval automatically
	 * 
	 * @param fileName
	 * @param delimiter
	 * @param dateTimePattern
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private int determineCsvInterval(String fileName, String delimiter,
			String dateTimePattern) throws FileNotFoundException, IOException {
		// determine CSV interval
		List<String> head = IOUtils.readLines(new FileReader(fileName))
				.subList(0, 2);
		DateTime first = DateTime.parse(
				StringUtils.substringBefore(head.get(0), delimiter),
				DateTimeFormat.forPattern(dateTimePattern));
		DateTime second = DateTime.parse(
				StringUtils.substringBefore(head.get(1), delimiter),
				DateTimeFormat.forPattern(dateTimePattern));

		// determine CSV interval automatically (millis)
		int csvInterval = (int) (second.getMillis() - first.getMillis());
		LOG.debug("Determined CSV interval automatically: "
				+ (csvInterval / 1000) + " seconds");

		return csvInterval;
	}
}
