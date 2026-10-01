package com.sudo.blog.controller;

import com.sudo.blog.domain.Article;
import com.sudo.blog.dto.*;
import com.sudo.blog.service.BlogService;
import com.sudo.blog.service.WritingAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController  //객체 데이터를 JSON 형식으로 반환
public class BlogApiController {

    private final BlogService blogService;
    private final WritingAssistantService writingAssistantService;
    // 이미지 생성 미지원(Spring AI 2.0.0-M1)으로 썸네일 기능 임시 비활성화
    // private final ThumbnailGeneratorService thumbnailGeneratorService;

    @PostMapping("/api/articles")
    public ResponseEntity<Article> addArticles(@RequestBody AddArticleRequest request){
        Article savedArticle = blogService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles(){
        List<ArticleResponse> articles = blogService.findAll()
                .stream()
                .map(ArticleResponse::new)
                .toList();
        return ResponseEntity.ok()
                .body(articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable long id){
        Article article = blogService.findById(id);

        return ResponseEntity.ok()
                .body(new ArticleResponse(article));
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable long id){
        blogService.delete(id);

        return ResponseEntity.ok()
                .build();
    }

    @PutMapping("/api/articles/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id,
                     @RequestBody UpdateArticleRequest request){
        Article updatedArticle = blogService.update(id, request);

        return ResponseEntity.ok()
                .body(updatedArticle);
    }

    @PostMapping("/api/ai-suggestions")
    public ResponseEntity<WritingSuggestionResponse> writingAssist(@RequestBody
               WritingSuggestionRequest request){
        WritingSuggestionResponse response =
                writingAssistantService.getWritingAssist(request);

        return ResponseEntity.ok()
                .body(response);
    }

    // 이미지 생성(썸네일)은 Spring AI 2.0.0-M1에서 google-genai가 지원하지 않아 임시 비활성화.
    // 지원 버전으로 올린 뒤 아래 엔드포인트와 위의 thumbnailGeneratorService 주입을 되살리세요.
    // @PostMapping("/api/ai-thumbnails")
    // public ResponseEntity<GeneratorThumbnailResponse> thumbnailGenerator(@RequestBody
    //              GeneratorThumbnailRequest request){
    //     GeneratorThumbnailResponse response
    //             = thumbnailGeneratorService.generateThumbnail(request);
    //
    //     return ResponseEntity.ok()
    //             .body(response);
    // }
}
