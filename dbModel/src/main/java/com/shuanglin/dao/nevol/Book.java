package com.shuanglin.dao.nevol;

import com.shuanglin.enums.book.ClassifyEnum;
import com.shuanglin.enums.book.StyleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("Articles_store")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {
  private String id;

  private String name;

  private String author;

  private String cover;

  private String coverType;

  private ClassifyEnum classify;

  private String otherClassify;

  private String tags;

  private StyleEnum style;

  private String otherStyle;

  private String status;

  private String description;

  private LocalDateTime lastUpdate;

  private Integer totalChapters;

  private Long totalWords;

  public Book(String name){
    this.name = name;
  }
}
