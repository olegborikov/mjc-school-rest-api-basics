package com.epam.esm.service.impl;

import com.epam.esm.dao.GiftCertificateDao;
import com.epam.esm.dto.GiftCertificateDto;
import com.epam.esm.dto.GiftCertificateQueryParametersDto;
import com.epam.esm.dto.TagDto;
import com.epam.esm.entity.GiftCertificate;
import com.epam.esm.exception.ResourceNotFoundException;
import com.epam.esm.service.GiftCertificateService;
import com.epam.esm.service.TagService;
import com.epam.esm.util.GiftCertificateQueryParameters;
import com.epam.esm.validator.GiftCertificateValidator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GiftCertificateServiceImpl implements GiftCertificateService {
    private final GiftCertificateDao giftCertificateDao;
    private final TagService tagService;
    private final ModelMapper modelMapper;

    @Autowired
    public GiftCertificateServiceImpl(GiftCertificateDao giftCertificateDao,
                                      TagService tagService, ModelMapper modelMapper) {
        this.giftCertificateDao = giftCertificateDao;
        this.tagService = tagService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public GiftCertificateDto addGiftCertificate(GiftCertificateDto giftCertificateDto) {
        addAndSetTags(giftCertificateDto);
        GiftCertificate giftCertificate = modelMapper.map(giftCertificateDto, GiftCertificate.class);
        GiftCertificateValidator.validate(giftCertificate);
        LocalDateTime currentTime = LocalDateTime.now();
        giftCertificate.setCreateDate(currentTime);
        giftCertificate.setLastUpdateDate(currentTime);
        GiftCertificate addedGiftCertificate = giftCertificateDao.add(giftCertificate);
        giftCertificateDao.addGiftCertificateHasTag(addedGiftCertificate);
        return modelMapper.map(addedGiftCertificate, GiftCertificateDto.class);
    }

    @Override
    public List<GiftCertificateDto> findGiftCertificates(
            GiftCertificateQueryParametersDto giftCertificateQueryParametersDto) {
        GiftCertificateQueryParameters giftCertificateQueryParameters
                = modelMapper.map(giftCertificateQueryParametersDto, GiftCertificateQueryParameters.class);
        List<GiftCertificate> foundGiftCertificates
                = giftCertificateDao.findByQueryParameters(giftCertificateQueryParameters);
        return foundGiftCertificates.stream()
                .map(this::convertGiftCertificateAndSetTags)
                .collect(Collectors.toList());
    }

    @Override
    public GiftCertificateDto findGiftCertificateById(Long id) {
        GiftCertificateValidator.validateId(id);
        Optional<GiftCertificate> foundGiftCertificate = giftCertificateDao.findById(id);
        return foundGiftCertificate
                .map(this::convertGiftCertificateAndSetTags)
                .orElseThrow(() -> new ResourceNotFoundException("Gift certificate with id " + id + " not found."));
    }

    @Transactional
    @Override
    public GiftCertificateDto updateGiftCertificate(GiftCertificateDto giftCertificateDto) {
        addAndSetTags(giftCertificateDto);
        GiftCertificateDto foundGiftCertificateDto = findGiftCertificateById(giftCertificateDto.getId());
        updateFields(foundGiftCertificateDto, giftCertificateDto);
        GiftCertificate foundGiftCertificate = modelMapper.map(foundGiftCertificateDto, GiftCertificate.class);
        GiftCertificateValidator.validate(foundGiftCertificate);
        GiftCertificate updatedGiftCertificate = giftCertificateDao.update(foundGiftCertificate);
        giftCertificateDao.removeGiftCertificateHasTag(updatedGiftCertificate.getId());
        giftCertificateDao.addGiftCertificateHasTag(updatedGiftCertificate);
        return modelMapper.map(updatedGiftCertificate, GiftCertificateDto.class);
    }

    @Transactional
    @Override
    public void removeGiftCertificate(Long id) {
        GiftCertificateValidator.validateId(id);
        giftCertificateDao.removeGiftCertificateHasTag(id);
        giftCertificateDao.remove(id);
    }

    private void addAndSetTags(GiftCertificateDto giftCertificateDto) {
        List<TagDto> tags = new ArrayList<>();
        if (giftCertificateDto.getTags() != null) {
            tags = giftCertificateDto.getTags().stream()
                    .map(tagService::addTag)
                    .collect(Collectors.toList());
        }
        giftCertificateDto.setTags(tags);
    }

    private GiftCertificateDto convertGiftCertificateAndSetTags(GiftCertificate giftCertificate) {
        GiftCertificateDto giftCertificateDto = modelMapper.map(giftCertificate, GiftCertificateDto.class);
        giftCertificateDto.setTags(tagService.findTagsByGiftCertificateId(giftCertificate.getId()));
        return giftCertificateDto;
    }

    private void updateFields(GiftCertificateDto foundGiftCertificate, GiftCertificateDto receivedGiftCertificate) {
        if (receivedGiftCertificate.getDuration() != 0) {
            foundGiftCertificate.setDuration(receivedGiftCertificate.getDuration());
        }
        if (receivedGiftCertificate.getName() != null) {
            foundGiftCertificate.setName(receivedGiftCertificate.getName());
        }
        if (receivedGiftCertificate.getDescription() != null) {
            foundGiftCertificate.setDescription(receivedGiftCertificate.getDescription());
        }
        if (receivedGiftCertificate.getPrice() != null) {
            foundGiftCertificate.setPrice(receivedGiftCertificate.getPrice());
        }
        foundGiftCertificate.setLastUpdateDate(LocalDateTime.now());
        foundGiftCertificate.setTags(receivedGiftCertificate.getTags());
    }
}
