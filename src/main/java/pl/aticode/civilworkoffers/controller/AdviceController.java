package pl.aticode.civilworkoffers.controller;

import java.io.IOException;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.mail.MailException;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import pl.aticode.civilworkoffers.entity.home.LogoSloganFooter;
import pl.aticode.civilworkoffers.service.HomeService;

@ControllerAdvice
public class AdviceController {

    private final static Logger logger = LoggerFactory.getLogger(AdviceController.class);

    @Autowired
    private HomeService homeService;

    @ExceptionHandler(Throwable.class)
    public String exceptionsServing(Exception ex, Model model) {

        if (ex instanceof HttpClientErrorException) {
            int rawStatusCode = ((HttpClientErrorException) ex).getRawStatusCode();
            if (rawStatusCode == 404) {
                model.addAttribute("exception", "exception.404");
            } else if (rawStatusCode == 400) {
                model.addAttribute("exception", "exception.400");
            } else if (rawStatusCode == 401) {
                model.addAttribute("exception", "exception.401");
            } else if (rawStatusCode == 403) {
                model.addAttribute("exception", "exception.403");
            } else if (rawStatusCode >= 500) {
                model.addAttribute("exception", "exception.5xx");
            }
        } else if (ex instanceof ResourceAccessException) {
            model.addAttribute("exception", "exception.connect");
        } else if (ex instanceof HttpServerErrorException) {
            int rawStatusCode = ((HttpServerErrorException) ex).getRawStatusCode();
            if (rawStatusCode == 500) {
                model.addAttribute("exception", "exception.server");
            }
        } else if (ex instanceof ServletRequestBindingException) {
            model.addAttribute("exception", "exception.403");
        } else if (ex instanceof EmptyResultDataAccessException || ex instanceof NoResultException) {
            model.addAttribute("exception", "exception.nouser");
        } else if (ex instanceof MailException) {
            model.addAttribute("exception", "exception.mail");
        } else if (ex instanceof NullPointerException) {
            model.addAttribute("exception", "exception.null");
        } else if (ex instanceof IOException) {
            model.addAttribute("exception", "exception.io");
        } else if (ex instanceof HttpRequestMethodNotSupportedException) {
            model.addAttribute("exception", "exception.post");
        } else {
            model.addAttribute("exception", "exception.unknown");
        }
        logger.error("ERROR from AdviceController {}", ex);
        return "error";
    }

    @ModelAttribute
    public void globalAttributes(Model model) {
        LogoSloganFooter logoSloganFooter = homeService.getLogoSloganFooter();
        model.addAttribute("logoSloganFooter", logoSloganFooter);
    }

    @InitBinder
    public void dataBinding(WebDataBinder binder) {
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
    }

}
