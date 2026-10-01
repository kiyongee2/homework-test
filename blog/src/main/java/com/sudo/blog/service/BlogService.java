package com.sudo.blog.service;

import com.sudo.blog.domain.Article;
import com.sudo.blog.dto.AddArticleRequest;
import com.sudo.blog.dto.UpdateArticleRequest;
import com.sudo.blog.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {
    private final BlogRepository blogRepository;

    // 블로그 글 추가
    public Article save(AddArticleRequest request){
        return blogRepository.save(request.toEntity());
    }

    // 블로글 전체 글 가져오기
    public List<Article> findAll(){
        return blogRepository.findAll();
    }

    // 블로그 글 1건 조회
    public Article findById(long id){
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
    }

    // 블로그 글 삭제
    public void delete(long id){
        blogRepository.deleteById(id);
    }

    // 글 수정하기
    @Transactional
    public Article update(long id, UpdateArticleRequest request){
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));

        article.update(request.getTitle(), request.getContent(),
                request.getImageUrl());
        return article;
    }
}
