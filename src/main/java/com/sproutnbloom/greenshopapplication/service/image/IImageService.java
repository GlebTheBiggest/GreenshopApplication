package com.sproutnbloom.greenshopapplication.service.image;

import com.sproutnbloom.greenshopapplication.dto.ImageDto;
import com.sproutnbloom.greenshopapplication.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    Image getImageById(Long id);
    void deleteImageById(Long id);
    List<ImageDto> saveImages(Long productId, List<MultipartFile> files);
    void updateImage(MultipartFile file, Long imageId);
}
