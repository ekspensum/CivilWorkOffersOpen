package pl.aticode.civilworkoffers.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;
import pl.aticode.civilworkoffers.dao.OfferRepository;

import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Employee;

@Service
public class AlertOfferDateExecQuartzJobService extends QuartzJobBean {

    private final static Logger logger = LoggerFactory.getLogger(AlertOfferDateExecQuartzJobService.class);

    private String alertExecDateOfferReqId = null;
    private String alertExecDateOfferReqIdTrigger1 = null;
    private String alertExecDateOfferReqIdTrigger2 = null;
    private TriggerKey triggerKey1 = null;
    private TriggerKey triggerKey2 = null;
    private JobKey jobKey = null;

    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private SendEmail sendEmail;
    @Autowired
    private MessageSource messageSource;

    public AlertOfferDateExecQuartzJobService() {
    }

    public AlertOfferDateExecQuartzJobService(OfferRepository offerRepository, Scheduler scheduler, SendEmail sendEmail, MessageSource messageSource) {
        this.offerRepository = offerRepository;
    	this.scheduler = scheduler;
        this.sendEmail = sendEmail;
        this.messageSource = messageSource;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            jobKey = context.getTrigger().getJobKey();
            final long offerRequestId = (long) context.getJobDetail().getJobDataMap().get(jobKey.getName());
            final OfferRequest offerRequest = offerRepository.findOfferRequest(offerRequestId);
            final Employee employee = offerRequest.getEmployee();
            final String requestContent = offerRequest.getOfferRequestContent().get(0).getContent();
            String emailSubject = messageSource.getMessage("offer.request.alert.dateexec.email.subject", null, new Locale(employee.getLanguage()));
            String emailContent = messageSource.getMessage("offer.request.alert.dateexec.email.content",
                    new String[]{employee.getFirstName(), employee.getLastName(), offerRequest.getExecutionDate().toString(),
                        offerRequest.getCustomer().getFirstName(), offerRequest.getCustomer().getLastName(), 
                        offerRequest.getCustomer().getCompanyName() == null ? "" : offerRequest.getCustomer().getCompanyName(),
                        offerRequest.getRegisterDateTime().toLocalDate().toString() + " " + offerRequest.getRegisterDateTime().toLocalTime(),
                        requestContent.substring(0, requestContent.length() < 50 ? requestContent.length() : 50) + " ....",
                        sendEmail.getMailFrom()},
                    new Locale(employee.getLanguage()));
            sendEmail.sendEmail(employee.getEmail(), emailSubject, emailContent);
            if(LocalDate.now().equals(offerRequest.getExecutionDate())){
                scheduler.deleteJob(jobKey);
            }
            logger.info("Sent to {} alert at oncomming date to make an offer.", employee.getFirstName() + " " + employee.getLastName());
        } catch (MailException e) {
            logger.error("Mail ERROR send alert at oncomming date to make an offer {}", e);
        } catch (SchedulerException e) {
            logger.error("Scheduler ERROR send alert at oncomming date to make an offer {}", e);
        } catch (Exception e) {
            logger.error("Unknown ERROR send alert at oncomming date to make an offer {}", e);
        }
    }

    /**
     * Setting alert at oncoming date to make an offer to offer request.
     * @param offerRequest
     */
    public void alertOfferDateExecJob(OfferRequest offerRequest) {
        setAlertOfferDateExecJob(offerRequest);
    }

    /**
     * Update exist alert at oncomming date to make an offer to offer request. If alert not exist, method set new alert.
     * @param offerRequest
     */
    public void updateAlertOfferDateExecJob(OfferRequest offerRequest) {
        setVariableForJobAndTrigger(offerRequest);
        boolean checkExistsJob;
        try {
            checkExistsJob = scheduler.checkExists(jobKey);
            if (!checkExistsJob) {
                setAlertOfferDateExecJob(offerRequest);
            } else {
                scheduler.pauseTrigger(triggerKey1);
                scheduler.unscheduleJob(triggerKey1);
                scheduler.pauseTrigger(triggerKey2);
                scheduler.unscheduleJob(triggerKey2);
                scheduler.deleteJob(jobKey);
                setAlertOfferDateExecJob(offerRequest);
            }
        } catch (SchedulerException ex) {
            logger.error("Scheduler ERROR send alert at oncomming date to make an offer {}", ex);
        }
    }

    /**
     * Remove all alerts at oncoming date to make offer to offer request. This method is in use at remove selected customer form application.
     * @param offerRequests
     */
    public void removeAllAlertsOfferDateExecJobCustomer(List<OfferRequest> offerRequests) {
        for (OfferRequest offerRequest : offerRequests) {
            setVariableForJobAndTrigger(offerRequest);
            try {
                boolean checkExistsJob = scheduler.checkExists(jobKey);
                if (checkExistsJob) {
                    scheduler.pauseTrigger(triggerKey1);
                    scheduler.unscheduleJob(triggerKey1);
                    scheduler.pauseTrigger(triggerKey2);
                    scheduler.unscheduleJob(triggerKey2);
                    scheduler.deleteJob(jobKey);
                }
            } catch (SchedulerException ex) {
                logger.error("Scheduler ERROR remove alert at oncomming date to make an offer {}", ex);
            }
        }
    }

    /**
     * Remove alert at oncoming date to make an offer to offer request - after sent offer to customer. 
     * @param offerRequest
     */
    public void removeAlertOfferDateExecJob(OfferRequest offerRequest) {
        setVariableForJobAndTrigger(offerRequest);
        try {
            boolean checkExistsJob = scheduler.checkExists(jobKey);
            if (checkExistsJob) {
                scheduler.pauseTrigger(triggerKey1);
                scheduler.unscheduleJob(triggerKey1);
                scheduler.pauseTrigger(triggerKey2);
                scheduler.unscheduleJob(triggerKey2);
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException ex) {
            logger.error("Scheduler ERROR remove alert at oncomming date to make an offer {}", ex);
        }
    }   
    
//    PRIVATE METHODS
    private void setVariableForJobAndTrigger(OfferRequest offerRequest) {
        alertExecDateOfferReqId = "alertExecDateOfferReq" + String.valueOf(offerRequest.getId());
        alertExecDateOfferReqIdTrigger1 = alertExecDateOfferReqId + "Trigger1";
        alertExecDateOfferReqIdTrigger2 = alertExecDateOfferReqId + "Trigger2";
        triggerKey1 = TriggerKey.triggerKey(alertExecDateOfferReqIdTrigger1, "alertExecDateOfferReqTrigger");
        triggerKey2 = TriggerKey.triggerKey(alertExecDateOfferReqIdTrigger2, "alertExecDateOfferReqTrigger");
        jobKey = JobKey.jobKey(alertExecDateOfferReqId, "alertExecDateOfferReqJob");
    }
    
    private void setAlertOfferDateExecJob(OfferRequest offerRequest) {
        alertExecDateOfferReqId = "alertExecDateOfferReq" + String.valueOf(offerRequest.getId());
        try {
            JobDetail jobDetail = JobBuilder.newJob(AlertOfferDateExecQuartzJobService.class)
                    .withIdentity(alertExecDateOfferReqId, "alertExecDateOfferReqJob")
                    .withDescription("Notification at execution date to make an offer - job detail")
                    .storeDurably()
                    .build();
            jobDetail.getJobDataMap().put(alertExecDateOfferReqId, offerRequest.getId());
            alertExecDateOfferReqIdTrigger1 = jobDetail.getKey().getName()+"Trigger1";
            SimpleTrigger trigger2DaysBefore = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withIdentity(alertExecDateOfferReqIdTrigger1, "alertExecDateOfferReqTrigger")
                    .withDescription("Notification at execution date to make an offer - trigger")
                    .startAt(DateBuilder.newDate()
                            .atHourOfDay(6).atMinute(30)
                            .onDay(offerRequest.getExecutionDate().minusDays(2).getDayOfMonth())
                            .inMonth(offerRequest.getExecutionDate().getMonthValue())
                            .inYear(offerRequest.getExecutionDate().getYear())
                            .build())
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                    .build();
            alertExecDateOfferReqIdTrigger2 = jobDetail.getKey().getName()+"Trigger2";
            SimpleTrigger triggerInTheSameDay = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withIdentity(alertExecDateOfferReqIdTrigger2, "alertExecDateOfferReqTrigger")
                    .withDescription("Notification at execution date to make an offer - trigger")
                    .startAt(DateBuilder.newDate()
                            .atHourOfDay(6).atMinute(30)
                            .onDay(offerRequest.getExecutionDate().getDayOfMonth())
                            .inMonth(offerRequest.getExecutionDate().getMonthValue())
                            .inYear(offerRequest.getExecutionDate().getYear())
                            .build())
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                    .build();  
            Set<SimpleTrigger> triggersForJob = new HashSet<>();
            triggersForJob.add(trigger2DaysBefore);
            triggersForJob.add(triggerInTheSameDay);
            scheduler.scheduleJob(jobDetail, triggersForJob, true);
            logger.info("Added alert at oncoming date to make an offer to offer request id: {}", offerRequest.getId());
        } catch (SchedulerException e) {
            logger.error("Scheduler ERROR send alert at oncoming date to make an offer {}", e);
        }
    }
}
