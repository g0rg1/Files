package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class Review {
    @JsonProperty("reviewerID")
    private String reviewerId;
    
    @JsonProperty("asin")
    private String productId;
    
    @JsonProperty("reviewerName")
    private String reviewerName;
    
    @JsonProperty("helpful")
    private int[] helpful;
    
    @JsonProperty("reviewText")
    private String reviewText;
    
    @JsonProperty("overall")
    private double rating;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("unixReviewTime")
    private long reviewTime;
    
    public double getHelpfulnessScore() {
        if (helpful == null || helpful.length != 2 || helpful[1] == 0) {
            return 0.0;
        }
        return (double) helpful[0] / helpful[1];
    }
    
    public Instant getReviewDate() {
        return Instant.ofEpochSecond(reviewTime);
    }
}
