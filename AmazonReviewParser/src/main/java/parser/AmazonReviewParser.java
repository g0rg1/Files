package parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Review;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class AmazonReviewParser {
    private final List<Review> reviews = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void parseFile(String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(new File(filePath).toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                Review review = objectMapper.readValue(line, Review.class);
                reviews.add(review);
            }
        }
    }

    public void exportMostPopularProducts(String outputFile) throws IOException {
        Map<String, Long> productCounts = reviews.stream()
                .collect(Collectors.groupingBy(Review::getProductId, Collectors.counting()));

        List<Map.Entry<String, Long>> sortedProducts = productCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputFile), CSVFormat.DEFAULT
                .withHeader("Product ID", "Review Count"))) {
            for (Map.Entry<String, Long> entry : sortedProducts) {
                printer.printRecord(entry.getKey(), entry.getValue());
            }
        }
    }

    public void exportProductsByRating(String outputFile) throws IOException {
        Map<String, DoubleSummaryStatistics> productStats = reviews.stream()
                .collect(Collectors.groupingBy(Review::getProductId,
                        Collectors.summarizingDouble(review -> review.getRating() * review.getHelpfulnessScore())));

        List<Map.Entry<String, Double>> weightedRatings = productStats.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(),
                        entry.getValue().getAverage()))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputFile), CSVFormat.DEFAULT
                .withHeader("Product ID", "Weighted Rating"))) {
            for (Map.Entry<String, Double> entry : weightedRatings) {
                printer.printRecord(entry.getKey(), String.format("%.2f", entry.getValue()));
            }
        }
    }

    public void exportPopularProductsByPeriod(String outputFile, Instant startDate, Instant endDate) throws IOException {
        Map<String, Long> productCounts = reviews.stream()
                .filter(review -> {
                    Instant reviewDate = review.getReviewDate();
                    return !reviewDate.isBefore(startDate) && !reviewDate.isAfter(endDate);
                })
                .collect(Collectors.groupingBy(Review::getProductId, Collectors.counting()));

        List<Map.Entry<String, Long>> sortedProducts = productCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputFile), CSVFormat.DEFAULT
                .withHeader("Product ID", "Review Count", "Period"))) {
            for (Map.Entry<String, Long> entry : sortedProducts) {
                printer.printRecord(entry.getKey(), entry.getValue(),
                        String.format("%s to %s", startDate, endDate));
            }
        }
    }

    public void searchProductsByReviewText(String outputFile, String searchText) throws IOException {
        List<Review> matchingReviews = reviews.stream()
                .filter(review -> review.getReviewText().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputFile), CSVFormat.DEFAULT
                .withHeader("Product ID", "Rating", "Review Text", "Helpful Score"))) {
            for (Review review : matchingReviews) {
                printer.printRecord(
                        review.getProductId(),
                        review.getRating(),
                        review.getReviewText(),
                        review.getHelpfulnessScore()
                );
            }
        }
    }
}
