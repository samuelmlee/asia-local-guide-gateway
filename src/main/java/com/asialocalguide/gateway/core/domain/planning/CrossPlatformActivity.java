package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CrossPlatformActivity {
  // Core Identification
  private String id; // e.g., "GYG-50872" or "VIATOR-43850P2"
  private String title; // "2-Park 2-Day Universal Orlando™ Theme Park Tickets"
  private String description; // Main activity description
  private String abstractSummary; // Short summary (GYG's "abstract")
  private List<String> languages; // Supported languages (e.g., ["en", "de"])

  // Pricing
  private Price price; // Unified price structure
  private PricingType pricingType; // PER_PERSON, PER_GROUP, etc.

  // Media
  private List<Image> images; // All image variants
  private String coverImageUrl; // Primary image URL

  // Logistics
  private Duration duration; // Activity duration
  private List<Location> locations; // Start/end points, POIs
  private Logistics logistics; // Pickup, redemption details

  // Availability & Booking
  private boolean mobileVoucherAccepted;
  private CancellationPolicy cancellationPolicy;
  private Instant createdAt;
  private Instant lastUpdatedAt;

  // Reviews & Ratings
  private double averageRating;
  private int totalReviews;
  private List<ReviewSource> reviewSources; // Viator, GetYourGuide, TripAdvisor

  // Categorization
  private List<String> tags; // "Bestseller", "Family-Friendly"

  // Vendor-Specific Metadata
  private String sourceVendor; // "GETYOURGUIDE" or "VIATOR"
  private Map<String, Object> vendorSpecificData; // Raw fields not mapped above

  // Nested Classes
  public static class Price {
    private Double amount;
    private String currency; // "USD", "EUR"
    private String description; // "Per person", "Group rate"
  }

  public static class Duration {
    private double value;
    private TimeUnit unit; // HOUR, DAY, etc.
  }

  public static class Image {
    private String url;
    private Integer height;
    private Integer width;
    private String caption;
    private boolean isCover;
  }

  public static class Location {
    private String name;
    private Coordinates coordinates;
    private String city;
    private String country;
  }

  public static class Logistics {
    private boolean hasPickup;
    private String redemptionInstructions;
  }

  public static class CancellationPolicy {
    private String summary; // Free-text description
    private boolean refundable;
  }

  public static class ReviewSource {
    private String provider; // "VIATOR", "GYG", "TRIPADVISOR"
    private double averageRating;
    private int totalReviews;
  }

  // Enums
  public enum PricingType {
    PER_PERSON,
    PER_GROUP,
    FIXED
  }

  public enum TimeUnit {
    HOUR,
    DAY,
    MINUTE
  }
}
