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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.LAZY)
  @JoinColumn(name = "activity_id", nullable = false)
  private Activity activity;

  @NotNull @Positive private Integer height;

  @NotNull @Positive private Integer width;

  @NotBlank @URL private String url;

  @NotNull
  @Enumerated(EnumType.STRING)
  private ImageType type;

  public ActivityImage(Integer height, Integer width, String url, ImageType type) {
    this.height = height;
    this.width = width;
    this.url = url;
    this.type = type;
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
