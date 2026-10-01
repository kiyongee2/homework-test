package com.sudo.blog.dto;

import com.sudo.blog.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddArticleRequest {
    private String title;
    private String content;
    private String imageUrl;

    public AddArticleRequest(String title, String content){
        this.title = title;
        this.content = content;
    }

    public Article toEntity(){ // 생성자를 사용해 객체 생성
        return Article.builder()
                .title(title)
                .content(content)
                .imageUrl((imageUrl))
                .build();
    }
}
