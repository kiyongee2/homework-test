package com.sudo.blog.service;


import com.sudo.blog.dto.GeneratorThumbnailRequest;
import com.sudo.blog.dto.GeneratorThumbnailResponse;
import com.sudo.blog.dto.UploadResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;

// Spring AI 2.0.0-M1에서는 google-genai 이미지 생성(ImageModel)을 지원하지 않습니다.
// @Service 를 활성화하면 ImageModel 빈이 없어 앱이 시작되지 않으므로 임시로 비활성화합니다.
// 이미지 생성이 지원되는 버전으로 올린 뒤 아래 @Service 주석을 해제하세요.
// @Service
public class ThumbnailGeneratorService {
    private final ImageModel imageModel;
    private final PromptTemplate template;
    private final FileStorageService fileStorageService;
    private final RestClient restClient = RestClient.create();

    public ThumbnailGeneratorService(
            ImageModel imageModel,
            FileStorageService fileStorageService,
            @Value("classpath:prompts/thumbnail-generator.st") Resource promptResource
            ){
        this.imageModel = imageModel;
        this.fileStorageService = fileStorageService;
        this.template = new PromptTemplate(promptResource);
    }

    public GeneratorThumbnailResponse generateThumbnail(GeneratorThumbnailRequest
            request){
        String prompt = template.create(request.toMap()).getContents();

        ImageResponse response = imageModel.call(new ImagePrompt(prompt));
        Image image = response.getResult().getOutput(); //생성된 이미지 정보 가져오기

        byte[] bytes = restClient.get()
                .uri(URI.create(image.getUrl()))
                .retrieve()
                .body(byte[].class);
        // 로컬에 이미지 저장
        UploadResponse saved = fileStorageService.store(bytes, "thumbnail.png");

        return new GeneratorThumbnailResponse(saved.imageUrl());
    }
}
