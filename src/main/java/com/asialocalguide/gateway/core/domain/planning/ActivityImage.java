package com.asialocalguide.gateway.core.domain.planning;

import com.asialocalguide.gateway.core.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Entity
@NoArgsConstructor
@Getter
public class ActivityImage extends BaseEntity {

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
}
