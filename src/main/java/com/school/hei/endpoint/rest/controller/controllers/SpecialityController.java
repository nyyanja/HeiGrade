package com.school.hei.endpoint.rest.controller.controllers;

import com.school.hei.model.Speciality;
import com.school.hei.service.services.SpecialityService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/specialities")
@RequiredArgsConstructor
public class SpecialityController {

    private final SpecialityService specialityService;

    @GetMapping
    public List<Speciality> findAll() {
        return specialityService.findAll();
    }

    @GetMapping("/{id}")
    public Speciality findById(@PathVariable UUID id) {
        return specialityService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Speciality save(@RequestBody Speciality speciality) {
        return specialityService.save(speciality);
    }

    @PutMapping("/{id}")
    public Speciality update(@PathVariable UUID id, @RequestBody Speciality speciality) {
        return specialityService.update(id, speciality);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        specialityService.delete(id);
    }
}