package com.example.vibeapp.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이징 처리된 게시글 목록 응답 객체")
public record PostListResponseDto(
        @Schema(description = "게시글 목록") List<PostListDto> posts,
        @Schema(description = "현재 페이지 번호", example = "1") int currentPage,
        @Schema(description = "전체 페이지 수", example = "10") int totalPages) {
}
