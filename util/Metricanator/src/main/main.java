package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import schema.metrics2.Metrics;
import schema.metrics2.Metrics.Metric;
import schema.metrics2.Metrics.Metric.Values;
import schema.metrics2.Value;
import schema.mylyn.InteractionHistory;
import schema.mylyn.InteractionHistory.InteractionEvent;

public class main {
	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Expected 2 arguments and got: ");
			for (int i = 0; i < args.length; i++) {
				System.err.println(String.format("   %s) %s", i, args[i]));
			}

			System.err.println("Usage: <Mylyn context> <Metrics file,...,Metrics file>");
			System.exit(0);
		}

		String mylynContext = args[0];
		String[] metricsFiles = args[1].split(",");

		Map<String, Value> loadedMetrics = loadMetrics(metricsFiles);

		Pattern pattern = Pattern.compile("=(.*?)/(.*?)&lt;(.*?)(\\(|\\{)(.*?)\\.(class|java)\\[(.*?)~(.*?)($|~).*");

		InteractionHistory mylynOut = JAXB.unmarshal(new File(mylynContext), InteractionHistory.class);
		List<InteractionEvent> interactionEvent = mylynOut.getInteractionEvent();

		double accumulatedMloc = 0;

		for (InteractionEvent interactionEvent2 : interactionEvent) {
			String exp = interactionEvent2.getStructureHandle().replaceAll("\\\\/", "/");
			Matcher m = pattern.matcher(exp);
			System.out.println(interactionEvent2.getStructureKind() + " : " + exp);
			if (m.matches()) {
				String proyecto = m.group(1);
				String jarLocation = m.group(2);
				String packageName = m.group(3);
				String compilationUnit = m.group(5);
				String className = m.group(7).replaceAll("\\[", "\\.");
				String method = m.group(8);
				String fetchMetric = fetchMetric("MLOC", packageName, className, compilationUnit, method, loadedMetrics);

				try {
					accumulatedMloc += Double.parseDouble(fetchMetric);
					System.out.println(String.format("Proyecto: %s", proyecto));
					System.out.println(String.format("Jar Location: %s", jarLocation));
					System.out.println(String.format("Package: %s", packageName));
					System.out.println(String.format("Compilation Unit: %s", compilationUnit));
					System.out.println(String.format("Class Name: %s", className));
					System.out.println(String.format("Metodo: %s", method));
					System.out.println(String.format("MLOC: %s", fetchMetric));
				} catch (Exception e) {
					System.err.println(String.format("Proyecto: %s", proyecto));
					System.err.println(String.format("Jar Location: %s", jarLocation));
					System.err.println(String.format("Package: %s", packageName));
					System.err.println(String.format("Compilation Unit: %s", compilationUnit));
					System.err.println(String.format("Class Name: %s", className));
					System.err.println(String.format("Metodo: %s", method));
					System.err.println(String.format("MLOC: %s", fetchMetric));
					System.err.println("---------------------------------------------");
				}

			} else {
				System.out.println("REGEXP does not match");
			}

			System.out.println("---------------------------------------------");

		}
		
		System.out.println("FINAL ACCUMULATED RESULT: " + accumulatedMloc);
		System.out.println("---------------------------------------------");

	}

	private static Map<String, Value> loadMetrics(String[] metricsFiles) {

		Map<String, Value> metricValues = new LinkedHashMap<String, Value>();

		for (String file : metricsFiles) {
			System.out.println("Loading metrics from: " + file);
			Metrics metrics2 = JAXB.unmarshal(new File(file), Metrics.class);
			List<Metric> thisFileMetrics = metrics2.getMetric();

			loadFileMetrics(metricValues, thisFileMetrics);
		}

		return metricValues;
	}

	private static void loadFileMetrics(Map<String, Value> metricValues, List<Metric> thisFileMetrics) {
		for (Metric metric : thisFileMetrics) {
			List<Values> valuesCollections = metric.getValues();
			for (Values valuesCollection : valuesCollections) {

				List<Value> values = valuesCollection.getValue();

				for (Value value : values) {
					String key = metric.getId() + "-" + value.getPackage() + "-" + value.getSource() + "-" + value.getName();
					metricValues.put(key, value);
				}
			}
		}

	}

	private static String fetchMetric(String metric, String packageName, String className, String compilationUnit, String method, Map<String, Value> loadedMetrics) {

		//if (method.equalsIgnoreCase("drawTextLines")) {
		//	int i = 0;
		//}

		String key = metric + "-" + packageName + "-" + compilationUnit + ".java" + "-" + method;

		if (loadedMetrics.containsKey(key)) {
			return loadedMetrics.get(key).getValue();
		}

		key = metric + "-" + packageName + "-" + compilationUnit + ".java" + "-" + className + "#" + method;

		if (loadedMetrics.containsKey(key)) {
			return loadedMetrics.get(key).getValue();
		}

		return "METRIC NOT FOUND";
	}
}
