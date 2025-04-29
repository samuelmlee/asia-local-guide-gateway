package com.asialocalguide.gateway.core.domain.planning;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Entity
@NoArgsConstructor
@Getter
public class ActivityImage {

  @Id private Long id;

  @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.LAZY)
  @JoinColumn(name = "activity_id", nullable = false)
  private Activity activity;

  @NotNull
  @Positive
  @Column(name = "activity_image_height")
  private Integer height;

  @NotNull
  @Positive
  @Column(name = "activity_image_width")
  private Integer width;

  @NotBlank
  @URL
  @Column(name = "activity_image_url")
  private String url;

  public ActivityImage(Integer height, Integer width, String url) {
    this.height = height;
    this.width = width;
    this.url = url;
  }

  public static ActivityImage fromCommonImage(CommonActivity.CommonImage image) {
    if (image == null) return null;
    return new ActivityImage(image.height(), image.width(), image.url());
  }

  public CommonActivity.CommonImage toCommonImage() {
    return new CommonActivity.CommonImage(height, width, url);
  }

  void setActivity(Activity activity) {
    this.activity = activity;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ActivityImage that = (ActivityImage) o;
    return Objects.equals(height, that.height) && Objects.equals(width, that.width) && Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(height, width, url);
  }
}
