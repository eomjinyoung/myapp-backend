package com.example.vibeapp.post.dto;

import com.example.vibeapp.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 수정을 위한 요청 객체")
public record PostUpdateDto(
        @Schema(description = "게시글 번호 (Path Variable로 전달 시 생략 가능)", example = "1") Long no,

        @Schema(description = "변경할 제목", example = "새로운 제목", maxLength = 100) @NotBlank(message = "제목은 필수입니다.") @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.") String title,

        @Schema(description = "변경할 내용", example = "새로운 내용") String content,

        @Schema(description = "변경할 태그 (쉼표로 구분)", example = "태그1,태그2") String tags) {
    public PostUpdateDto() {
        this(null, null, null, null);
    }

    public Post toEntity() {
        Post post = new Post();
        post.setNo(this.no);
        post.setTitle(this.title);
        post.setContent(this.content);
        return post;
    }
}
