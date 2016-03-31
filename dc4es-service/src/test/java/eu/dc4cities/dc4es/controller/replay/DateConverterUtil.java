//package eu.dc4cities.dc4es.controller.replay;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//
///**
// * !!IGNORE!! (just a utility class to convert dates)
// *
// *
// *
// */
//public class DateConverterUtil {
//
//	/**
//	 * @param args
//	 * @throws IOException
//	 */
//	public static void main(String[] args) throws IOException {
//		String delimiter = ";";
//
//		File input = new File("src/test/resources/replay-test/forecastreplay_AdvancedForecastTest_1402064205477.csv");
//		File output = new File("/tmp/" + input.getName());
//
//		List<String> lines = FileUtils.readLines(input);
//
//		List<String> outLines = new ArrayList<String>(lines.size());
//		for(String line : lines) {
//			String[] columns = StringUtils.split(line, delimiter);
//
//			DateTime date = DateTimeFormat.forPattern(
//					ReplayConstants.DATE_TIME_PATTERN).parseDateTime(
//							columns[0]);
//
//			String convertedDate= eu.dc4cities.dc4es.converter.DateTimeFormat.FORMAT.print(date);
//
//			outLines.add(convertedDate + delimiter + StringUtils.join(ArrayUtils.subarray(columns, 1, columns.length), delimiter));
//
//			System.out.println(outLines.get(outLines.size() -1));
//		}
//
//		FileUtils.writeLines(output, outLines);
//	}
//
//}
