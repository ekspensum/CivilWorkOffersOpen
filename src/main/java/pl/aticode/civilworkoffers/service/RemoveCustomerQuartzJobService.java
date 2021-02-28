package pl.aticode.civilworkoffers.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import pl.aticode.civilworkoffers.dao.UserRepository;

import pl.aticode.civilworkoffers.entity.user.Customer;

@Service
public class RemoveCustomerQuartzJobService extends QuartzJobBean {

    private final static Logger logger = LoggerFactory.getLogger(RemoveCustomerQuartzJobService.class);

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private SendEmail sendEmail;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private SessionRegistry sessionRegistry;

    public RemoveCustomerQuartzJobService() {
    }

    public RemoveCustomerQuartzJobService(UserService userService, UserRepository userRepository, Scheduler scheduler, SendEmail sendEmail,
            MessageSource messageSource, SessionRegistry sessionRegistry) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.scheduler = scheduler;
        this.sendEmail = sendEmail;
        this.messageSource = messageSource;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Execute remove customer job.Before remove user is logout.After customer remove job is delete.
     * @param context
     * @throws org.quartz.JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            JobKey jobKey = context.getTrigger().getJobKey();
            final long customerId = (long) context.getJobDetail().getJobDataMap().get(jobKey.getName());
            final Customer customer = userRepository.findCustomer(customerId);
            List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
            for (Object object : allPrincipals) {
                if (object instanceof DefaultOAuth2User) {
                    DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) object;
                    List<SessionInformation> allSessions = sessionRegistry.getAllSessions(defaultOAuth2User, false);
                    for (Object session : allSessions) {
                        if (session instanceof SessionInformation) {
                            SessionInformation sessionInformation = (SessionInformation) session;
                            if (defaultOAuth2User.getName().equals(customer.getSocialUserId())) {
                                sessionInformation.expireNow();
                            }
                        }
                    }
                }
            }
            for (Object object : allPrincipals) {
                if (object instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) object;
                    List<SessionInformation> allSessions = sessionRegistry.getAllSessions(userDetails, false);
                    for (Object session : allSessions) {
                        if (session instanceof SessionInformation) {
                            SessionInformation sessionInformation = (SessionInformation) session;
                            if (userDetails.getUsername().equals(customer.getUser().getUsername())) {
                                sessionInformation.expireNow();
                            }
                        }
                    }
                }
            }
            userService.removeCustomer(customer);
            scheduler.deleteJob(jobKey);
            logger.info("Customer {} has been removed by Quartz Job.", customer.getFirstName() + " " + customer.getLastName());
        } catch (SchedulerException e) {
            logger.error("ERROR remove customer by Quartz Job {}", e);
        } catch (Exception e) {
            logger.error("ERROR remove customer by Quartz Job {}", e);
        }

    }

    /**
     * Status job of customer remove.
     *
     * @author aticode.pl
     *
     */
    public enum RemoveCustomerStatus {
        JOB_REMOVE_ADD_SUCCESS, JOB_REMOVE_EXIST, JOB_REMOVE_ADD_DEFEAT, JOB_REMOVE_CANCEL_SUCCESS, JOB_REMOVE_CANCEL_DEFEAT,
        JOB_REMOVE_TRIGGER_NOT_EXIST, JOB_REMOVE_CANCEL_DEFEAT_MAIL_FAILD;
    }

    /**
     * Run remove customer job. Trigger is set to start at 10 pm o'clock. Method
     * send email to customer with notification about planned remove job
     *
     * @param customerToRemove
     * @return
     */
    public RemoveCustomerStatus runRemoveCustomerJob(Customer customerToRemove) {
        String customerToRemoveId = "customerToRemove" + String.valueOf(customerToRemove.getId());
        TriggerKey triggerKey = TriggerKey.triggerKey(customerToRemoveId, "removeCustomerTrigger");
        JobKey jobKey = JobKey.jobKey(customerToRemoveId, "removeCustomerJob");
        try {
            JobDetail jobDetail = JobBuilder.newJob(RemoveCustomerQuartzJobService.class)
                    .withIdentity(customerToRemoveId, "removeCustomerJob")
                    .withDescription("Remove customer from application - job detail")
                    .storeDurably()
                    .build();
            jobDetail.getJobDataMap().put(customerToRemoveId, customerToRemove.getId());
            final LocalDateTime localDateTimeNow = LocalDateTime.now();
            SimpleTrigger trigger = TriggerBuilder.newTrigger()
                    .forJob(jobDetail)
                    .withIdentity(jobDetail.getKey().getName(), "removeCustomerTrigger")
                    .withDescription("Remove customer from application - trigger")
                    .startAt(DateBuilder.newDate()
                            .onDay(localDateTimeNow.getDayOfMonth())
                            .atHourOfDay(localDateTimeNow.plusHours(1).getHour())
                            .atMinute(localDateTimeNow.getMinute())
                            .inMonth(localDateTimeNow.getMonthValue())
                            .inYear(localDateTimeNow.getYear())
                            .build())
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                    .build();
            boolean checkExistsJob = scheduler.checkExists(jobKey);
            if (!checkExistsJob) {
                scheduler.scheduleJob(jobDetail, trigger);
                String emailSubject = messageSource.getMessage("customer.remove.account.email.add.subject", null, new Locale(customerToRemove.getLanguage()));
                String emailContent = messageSource.getMessage("customer.remove.account.email.add.content",
                        new String[]{customerToRemove.getFirstName(), customerToRemove.getLastName(), customerToRemove.getUser().getUsername(), sendEmail.getMailFrom()},
                        new Locale(customerToRemove.getLanguage()));
                sendEmail.sendEmail(customerToRemove.getEmail(), emailSubject, emailContent);
                logger.info("Customer {} added to remove queue", customerToRemove.getFirstName() + " " + customerToRemove.getLastName());
                return RemoveCustomerStatus.JOB_REMOVE_ADD_SUCCESS;
            } else {
                return RemoveCustomerStatus.JOB_REMOVE_EXIST;
            }
        } catch (SchedulerException e) {
            logger.error("ERROR add remove customer to Quartz Job {}", e.getMessage());
            return RemoveCustomerStatus.JOB_REMOVE_ADD_DEFEAT;
        } catch (MailException e) {
            try {
                scheduler.pauseTrigger(triggerKey);
                scheduler.unscheduleJob(triggerKey);
                scheduler.deleteJob(jobKey);
            } catch (SchedulerException e1) {
                logger.error("ERROR unchedule trigger or delete job QUARTZ {}", e1.getMessage());
            }
            logger.error("ERROR add remove customer to Quartz Job - email exception: {}", e.getMessage());
            return RemoveCustomerStatus.JOB_REMOVE_ADD_DEFEAT;
        }
    }

    /**
     * Cancel planned remove customer if job exists. Method send email wit
     * notification about cancel remove customer job.
     *
     * @param customerToRemove
     * @return
     */
    public RemoveCustomerStatus cancelRemoveCustomerJob(Customer customerToRemove) {
        String customerToRemoveId = "customerToRemove" + String.valueOf(customerToRemove.getId());
        TriggerKey triggerKey = TriggerKey.triggerKey(customerToRemoveId, "removeCustomerTrigger");
        JobKey jobKey = JobKey.jobKey(customerToRemoveId, "removeCustomerJob");
        try {
            boolean checkExistsTrigger = scheduler.checkExists(triggerKey);
            if (checkExistsTrigger) {
                scheduler.pauseTrigger(triggerKey);
                boolean unscheduleJob = scheduler.unscheduleJob(triggerKey);
                boolean deleteJob = scheduler.deleteJob(jobKey);
                if (unscheduleJob && deleteJob) {
                    String emailSubject = messageSource.getMessage("customer.remove.account.email.cancel.subject", null, new Locale(customerToRemove.getLanguage()));
                    String emailContent = messageSource.getMessage("customer.remove.account.email.cancel.content",
                            new String[]{customerToRemove.getFirstName(), customerToRemove.getLastName(), customerToRemove.getUser().getUsername(), sendEmail.getMailFrom()},
                            new Locale(customerToRemove.getLanguage()));
                    sendEmail.sendEmail(customerToRemove.getEmail(), emailSubject, emailContent);
                    logger.info("Customer account {} has been taked back from queue to remove. ", customerToRemove.getFirstName() + " " + customerToRemove.getLastName());
                    return RemoveCustomerStatus.JOB_REMOVE_CANCEL_SUCCESS;
                } else {
                    logger.error("ERROR delete job of remove customer from schedule for customer {} ", customerToRemove.getFirstName() + " " + customerToRemove.getLastName());
                    return RemoveCustomerStatus.JOB_REMOVE_CANCEL_DEFEAT;
                }
            } else {
                return RemoveCustomerStatus.JOB_REMOVE_TRIGGER_NOT_EXIST;
            }
        } catch (SchedulerException e) {
            logger.error("ERROR delete job of remove customer from schedule {}", e.getMessage());
            return RemoveCustomerStatus.JOB_REMOVE_CANCEL_DEFEAT;
        } catch (MailException e) {
            logger.info("Customer account {} has been taked back from queue to remove. ", customerToRemove.getFirstName() + " " + customerToRemove.getLastName());
            return RemoveCustomerStatus.JOB_REMOVE_CANCEL_DEFEAT_MAIL_FAILD;
        }
    }
}
