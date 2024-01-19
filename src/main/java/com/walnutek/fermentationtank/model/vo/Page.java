package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Schema(title = "分頁物件")
@Data
public class Page<T> {

    @Schema(title = "資料清單")
    private List<T> records = List.of();

    @Schema(title = "總資料筆數")
    private Long total = 0L;

    @Schema(title = "每頁的資料筆數")
    private Long size = 0L;

    @Schema(title = "目前頁數")
    private Long current = 1L;

    @Schema(title = "總頁數")
    private Long pages = 1L;

    public Page() {}

    public static <T> Page<T> emptyPage() {
        var data = new Page();
        List<T> emptyList = new ArrayList<>();
        data.setRecords(emptyList);
        data.setTotal(0L);
        data.setSize(0L);
        data.setCurrent(1L);
        data.setPages(1L);
        return data;
    }

    public static <T> Page<T> of(org.springframework.data.domain.Page<T> source) {
        var vo = new Page<T>();
        vo.records = source.getContent();
        vo.total = source.getTotalElements();
        vo.size = Integer.valueOf(source.getPageable().getPageSize()).longValue();
        vo.current = Integer.valueOf(source.getNumber() + 1).longValue();
        vo.pages = Integer.valueOf(source.getTotalPages()).longValue();

        return vo;
    }

    public <U> Page<U> map(Function<? super T, U> converter) {
        var recordList = this.records.stream().map(converter).toList();
        var vo = new Page<U>();
        vo.records = recordList;
        vo.total = total;
        vo.size = size;
        vo.current = current;
        vo.pages = pages;

        return vo;
    }
}
