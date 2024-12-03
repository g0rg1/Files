import org.apache.commons.cli.*;
import parser.AmazonReviewParser;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();

        Option input = Option.builder("i")
                .longOpt("input")
                .hasArg()
                .desc("Input JSON file path")
                .required()
                .build();

        Option output = Option.builder("o")
                .longOpt("output")
                .hasArg()
                .desc("Output directory for CSV files")
                .required()
                .build();

        Option startDate = Option.builder("s")
                .longOpt("start-date")
                .hasArg()
                .desc("Start date for period analysis (yyyy-MM-dd)")
                .build();

        Option endDate = Option.builder("e")
                .longOpt("end-date")
                .hasArg()
                .desc("End date for period analysis (yyyy-MM-dd)")
                .build();

        Option searchText = Option.builder("t")
                .longOpt("text")
                .hasArg()
                .desc("Text to search in reviews")
                .build();

        options.addOption(input);
        options.addOption(output);
        options.addOption(startDate);
        options.addOption(endDate);
        options.addOption(searchText);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);
            String inputFile = cmd.getOptionValue("input");
            String outputDir = cmd.getOptionValue("output");

            AmazonReviewParser reviewParser = new AmazonReviewParser();
            reviewParser.parseFile(inputFile);

            // Export most popular products
            reviewParser.exportMostPopularProducts(outputDir + "/popular_products.csv");

            // Export products by rating
            reviewParser.exportProductsByRating(outputDir + "/products_by_rating.csv");

            // Export products by period if dates are provided
            if (cmd.hasOption("start-date") && cmd.hasOption("end-date")) {
                Instant start = Instant.parse(cmd.getOptionValue("start-date") + "T00:00:00Z");
                Instant end = Instant.parse(cmd.getOptionValue("end-date") + "T23:59:59Z");
                reviewParser.exportPopularProductsByPeriod(
                        outputDir + "/popular_products_by_period.csv",
                        start,
                        end
                );
            }

            // Search products by text if provided
            if (cmd.hasOption("text")) {
                reviewParser.searchProductsByReviewText(
                        outputDir + "/search_results.csv",
                        cmd.getOptionValue("text")
                );
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("amazon-review-parser", options);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
