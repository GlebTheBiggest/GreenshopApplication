package com.sprout_bloom.greenshop_application.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    // Головна сторінка
    @GetMapping("/")
    public String index() {
        log.info("Перехід на головну сторінку (index)");
        return "index";
    }

    // Додаткова сторінка для перевірки
    @GetMapping("/test")
    public String testPage() {
        log.info("Отримано запит на /test");
        return "index";
    }

    // Сторінка після логування чи реєстрації
    @GetMapping("/home")
    public String homePage() {
        log.info("Успішний логін, перенаправлення на /home");
        return "index";
    }
}