package com.picturebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ChildDTO {
    
    private Long id;
    
    @NotBlank(message = "儿童姓名不能为空")
    private String name;
    
    private LocalDate birthDate;

    private Integer gender;
    
    private Long parentId;
    
    private Long classId;
    
    private String avatar;
}
