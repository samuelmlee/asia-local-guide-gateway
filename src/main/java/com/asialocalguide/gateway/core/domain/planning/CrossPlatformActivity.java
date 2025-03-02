package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.destination.Coordinates;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CrossPlatformActivity {
  private String id;
  private String title;
  private String description;
  private String abstractSummary;
  private List<String> languages;

  private Price price;
  private PricingType pricingType;

  private List<Image> images;
  private String coverImageUrl;

  private Duration duration;
  private List<Location> locations;
  private Logistics logistics;

  private boolean mobileVoucherAccepted;
  private CancellationPolicy cancellationPolicy;
  private Instant createdAt;
  private Instant lastUpdatedAt;

  private double averageRating;
  private int totalReviews;
  private List<ReviewSource> reviewSources;

  private List<String> tags;

  private String sourceVendor;
  private Map<String, Object> vendorSpecificData;

  public static class Price {
    private Double amount;
    private String currency;
    private String description;
  }

  public static class Duration {
    private double value;
    private TimeUnit unit;
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
    private String summary;
    private boolean refundable;
  }

  public static class ReviewSource {
    private String provider;
    private double averageRating;
    private int totalReviews;
  }

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
