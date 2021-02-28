package pl.aticode.civilworkoffers.service;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import pl.aticode.civilworkoffers.dao.OfferRepository;
import pl.aticode.civilworkoffers.entity.estimate.Estimate;
import pl.aticode.civilworkoffers.entity.estimate.EstimateItem;
import pl.aticode.civilworkoffers.entity.estimate.TypeOfCivilWork;
import pl.aticode.civilworkoffers.entity.estimate.UnitOfMeasurement;
import pl.aticode.civilworkoffers.entity.offer.ByteAttachment;
import pl.aticode.civilworkoffers.entity.offer.Offer;
import pl.aticode.civilworkoffers.entity.offer.OfferAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestComment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestContent;
import pl.aticode.civilworkoffers.entity.offer.OfferStage;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.entity.user.Owner;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.model.OfferAttachmentData;
import pl.aticode.civilworkoffers.model.OfferData;

/**
 *
 * @author aticode.pl
 */
@Service
public class OfferService {

    private final static Logger logger = LoggerFactory.getLogger(OfferService.class);

    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private DrawEmployeeService drawEmployeeService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private SendEmail sendEmail;
    @Autowired
    private AlertOfferDateExecQuartzJobService alertOfferDateExecQuartzJobService;

    /**
     * Adding new offer request by customer.
     * @param offerRequest
     * @throws IOException
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addOfferRequestByCustomer(OfferRequest offerRequest) throws IOException, Exception {
        final Employee drawEmployee = drawEmployeeService.drawEmployee();
        offerRequest.setEmployee(drawEmployee);
        final Customer loggedCustomer = userService.getLoggedCustomer();
        offerRequest.setCustomer(loggedCustomer);
        offerRequest.setUserRegister(loggedCustomer.getUser());
        offerRequest.setRegisterDateTime(LocalDateTime.now().withNano(0));
        final List<OfferRequestContent> offerRequestContentList = offerRequest.getOfferRequestContent();
        final OfferRequestContent offerRequestContent = offerRequestContentList.get(0);
        offerRequestContent.setRegisterDateTime(LocalDateTime.now().withNano(0));
        offerRequestContent.setUserRegister(loggedCustomer.getUser());
        List<OfferRequestAttachment> offerRequestAttachment = new ArrayList<>();
        final List<MultipartFile> attachments = offerRequest.getAttachments();
        OfferRequestAttachment requestAttachment;
        ByteAttachment byteAttachment;
        for (MultipartFile attachment : attachments) {
            if (!attachment.isEmpty()) {
                byteAttachment = new ByteAttachment();
                byteAttachment.setFile(attachment.getBytes());
                requestAttachment = new OfferRequestAttachment();
                requestAttachment.setByteAttachment(byteAttachment);
                requestAttachment.setFileName(attachment.getOriginalFilename());
                requestAttachment.setFileType(attachment.getContentType());
                requestAttachment.setFileSize(attachment.getBytes().length);
                requestAttachment.setRegisterDateTime(LocalDateTime.now().withNano(0));
                requestAttachment.setUserRegister(loggedCustomer.getUser());
                offerRequestAttachment.add(requestAttachment);
            }
        }
        offerRequest.setOfferRequestAttachment(offerRequestAttachment);
        offerRepository.saveOfferRequest(offerRequest);
        employeeEmailNotification(offerRequest);
        customerEmailNotification(offerRequest);
        alertOfferDateExecQuartzJobService.alertOfferDateExecJob(offerRequest);
    }

