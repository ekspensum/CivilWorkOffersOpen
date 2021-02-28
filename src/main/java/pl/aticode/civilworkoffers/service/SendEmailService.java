package pl.aticode.civilworkoffers.service;

import java.util.List;

import javax.mail.internet.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.aticode.civilworkoffers.entity.offer.OfferAttachment;

@Service
public class SendEmailService {

    @Value(value = "${mail.from}")
    private String mailFrom;

    @Autowired
    private JavaMailSender javaMailSender;

    public SendEmailService() {
    }

    public SendEmailService(String mailFrom, JavaMailSender javaMailSender) {
        this.mailFrom = mailFrom;
        this.javaMailSender = javaMailSender;
    }

    public void sendEmailMultipartFileAttachment(String mailTo, String emailSubject, String emailContent, List<MultipartFile> attachment) {

        MimeMessagePreparator messagePreparator = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setSubject(emailSubject);
                helper.setFrom(mailFrom);
                helper.setTo(mailTo);
                helper.setText(emailContent, true);
                for (int i = 0; i < attachment.size(); i++) {
                    helper.addAttachment(attachment.get(i).getOriginalFilename(), new ByteArrayResource(attachment.get(i).getBytes()));
                }
            }
        };
        javaMailSender.send(messagePreparator);
    }

    public void sendEmailOfferAttachment(String mailTo, String emailSubject, String emailContent, List<OfferAttachment> attachment) {
        
        MimeMessagePreparator messagePreparator = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setSubject(emailSubject);
                helper.setFrom(mailFrom);
                helper.setTo(mailTo);
                helper.setText(emailContent, true);
                for (int i = 0; i < attachment.size(); i++) {
                    helper.addAttachment(attachment.get(i).getFileName(), new ByteArrayResource(attachment.get(i).getFile()));
                }
            }
        };
        javaMailSender.send(messagePreparator);
    }

    public void sendEmail(String mailTo, String emailSubject, String emailContent) {
        MimeMessagePreparator messagePreparator = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
                helper.setSubject(emailSubject);
                helper.setFrom(mailFrom);
                helper.setTo(mailTo);
                helper.setText(emailContent, true);
            }
        };
        javaMailSender.send(messagePreparator);
    }

    public String getMailFrom() {
        return mailFrom;
    }

}

