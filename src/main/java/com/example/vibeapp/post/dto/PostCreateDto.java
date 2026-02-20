package com.example.vibeapp.post.dto;

import com.example.vibeapp.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 생성을 위한 요청 객체")
public record PostCreateDto(
        @Schema(description = "게시글 제목", example = "안녕하세요", maxLength = 100) @NotBlank(message = "제목은 필수입니다.") @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.") String title,

        @Schema(description = "게시글 내용", example = "만나서 반갑습니다.") String content,

        @Schema(description = "해시태그 (쉼표로 구분)", example = "인사,반가움") String tags) {
    public PostCreateDto() {
        this(null, null, null);
    }

    public Post toEntity() {
        Post post = new Post();
        post.setTitle(this.title);
        post.setContent(this.content);
        return post;
    }
}
