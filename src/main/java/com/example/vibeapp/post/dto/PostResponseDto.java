package com.example.vibeapp.post.dto;

import com.example.vibeapp.post.Post;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 상세 정보 응답 객체")
public record PostResponseDto(
        @Schema(description = "게시글 번호", example = "1") Long no,
        @Schema(description = "제목", example = "제목입니다") String title,
        @Schema(description = "내용", example = "내용입니다") String content,
        @Schema(description = "생성 일시") LocalDateTime createdAt,
        @Schema(description = "수정 일시") LocalDateTime updatedAt,
        @Schema(description = "조회수", example = "10") Integer views,
        @Schema(description = "태그 리스트 (쉼표로 구분)", example = "java,spring") String tags,
        @Schema(description = "작성자 이름", example = "홍길동") String authorName,
        @Schema(description = "작성자 번호", example = "1") Long authorNo) {
    public PostResponseDto() {
        this(null, null, null, null, null, null, null, null, null);
    }

    public static PostResponseDto from(Post post, String tags) {
        return new PostResponseDto(
                post.getNo(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViews(),
                tags,
                post.getAuthor() != null ? post.getAuthor().getName() : "Anonymous",
                post.getAuthor() != null ? post.getAuthor().getNo() : null);
    }
}
