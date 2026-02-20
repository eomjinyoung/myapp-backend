package com.example.vibeapp.post.dto;

import com.example.vibeapp.post.Post;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 목록 항목 응답 객체")
public record PostListDto(
        @Schema(description = "게시글 번호", example = "1") Long no,
        @Schema(description = "제목", example = "제목입니다") String title,
        @Schema(description = "생성 일시") LocalDateTime createdAt,
        @Schema(description = "조회수", example = "5") Integer views,
        @Schema(description = "작성자 이름", example = "홍길동") String authorName) {
    public PostListDto() {
        this(null, null, null, null, null);
    }

    public static PostListDto from(Post post) {
        return new PostListDto(
                post.getNo(),
                post.getTitle(),
                post.getCreatedAt(),
                post.getViews(),
                post.getAuthor() != null ? post.getAuthor().getName() : "Anonymous");
    }
}
