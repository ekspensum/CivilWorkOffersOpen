package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.ThreadExecutor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;

import pl.aticode.civilworkoffers.dao.UserRepository;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.User;
import pl.aticode.civilworkoffers.service.RemoveCustomerQuartzJobService;
import pl.aticode.civilworkoffers.service.RemoveCustomerQuartzJobService.RemoveCustomerStatus;
import pl.aticode.civilworkoffers.service.SendEmailService;
import pl.aticode.civilworkoffers.service.UserService;

class RemoveCustomerQuartzJobServiceTest {

	@InjectMocks
	private RemoveCustomerQuartzJobService removeCustomerQuartzJobService;
	@Mock
	private UserService userService;
	@Mock
	private UserRepository userRepository;
	@Mock
	private JobExecutionContext context;
	@Mock
	private Trigger trigger; 
	@Mock
	private SessionRegistry sessionRegistry;
	@Mock
	private org.springframework.security.oauth2.core.user.DefaultOAuth2User defaultOAuth2User;
	@Mock
	private UserDetails userDetails;
	@Mock
	private SessionInformation sessionInformation;
    @Mock
    private SendEmailService sendEmail;
	@Mock
	private MessageSource messageSource;
	@Mock
	private ThreadExecutor threadExecutor;
	@Mock
	private SchedulerSignaler schedulerSignaler;
	@Mock
	private ClassLoadHelper loadHelper;
	
	private Scheduler scheduler;
	private QuartzScheduler quartzScheduler;
	private QuartzSchedulerResources quartzSchedulerResources;
	private JobStore jobStore;
	private JobDetail jobDetail;
	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testExecuteInternalJobExecutionContext() throws Exception {
		scheduler = Mockito.mock(Scheduler.class);
		JobKey jobKey = new JobKey("jobKey");
		User user = new User();
		user.setUsername("username");
		Customer customer = new Customer();
		customer.setId(33L);
		customer.setFirstName("firstName");
		customer.setSocialUserId("socialUserId");
		customer.setUser(user);
		Mockito.when(context.getTrigger()).thenReturn(trigger);
		Mockito.when(trigger.getJobKey()).thenReturn(jobKey);
		Mockito.when(context.getScheduler()).thenReturn(scheduler);
		
		jobDetail = new JobDetailImpl();
		jobDetail.getJobDataMap().put(jobKey.getName(), customer.getId());
		Mockito.when(context.getJobDetail()).thenReturn(jobDetail);
		Mockito.when(userRepository.findCustomer(customer.getId())).thenReturn(customer);
		
		Mockito.when(defaultOAuth2User.getName()).thenReturn("socialUserId");
		List<Object> allPrincipals = new ArrayList<>();
		allPrincipals.add(defaultOAuth2User);
		Mockito.when(sessionRegistry.getAllPrincipals()).thenReturn(allPrincipals);
		List<SessionInformation> allSessions = new ArrayList<>();
		allSessions.add(sessionInformation);
		Mockito.when(sessionRegistry.getAllSessions(defaultOAuth2User, false)).thenReturn(allSessions);
		
		Mockito.when(userDetails.getUsername()).thenReturn("username");
		allPrincipals.add(userDetails);
		Mockito.when(sessionRegistry.getAllSessions(userDetails, false)).thenReturn(allSessions);
		
		removeCustomerQuartzJobService = new RemoveCustomerQuartzJobService(userService, userRepository, scheduler, sendEmail, messageSource, sessionRegistry);
		
		ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
		doNothing().when(userService).removeCustomer(argumentCaptor.capture());
		removeCustomerQuartzJobService.execute(context);
		assertEquals("firstName", argumentCaptor.getValue().getFirstName());
	}

	@Test
	void testRunRemoveCustomerJob() throws SchedulerException {
		User user = new User();
		user.setUsername("username");
		Customer customerToRemove = new Customer();
		customerToRemove.setId(66L);
		customerToRemove.setFirstName("firstName");
		customerToRemove.setLastName("lastName");
		customerToRemove.setLanguage("pl");
		customerToRemove.setUser(user);
	
		quartzSchedulerResources = new QuartzSchedulerResources();
		String customerToRemoveId = "customerToRemove"+String.valueOf(customerToRemove.getId());
		
		JobKey jobKey = new JobKey(customerToRemoveId);
		JobDetailImpl jobDetailImpl = new JobDetailImpl();
		jobDetailImpl.setKey(jobKey);

		jobStore = new RAMJobStore();
		jobStore.storeJob(jobDetailImpl, false);		
		
		quartzSchedulerResources.setJobStore(jobStore);
		quartzSchedulerResources.setName("name123");
		quartzSchedulerResources.setThreadExecutor(threadExecutor);
		quartzScheduler = new QuartzScheduler(quartzSchedulerResources, 1000, 1000);
		scheduler = new StdScheduler(quartzScheduler);
		removeCustomerQuartzJobService = new RemoveCustomerQuartzJobService(userService, userRepository, scheduler, sendEmail, messageSource, sessionRegistry);
		
		assertEquals(RemoveCustomerStatus.JOB_REMOVE_ADD_SUCCESS, removeCustomerQuartzJobService.runRemoveCustomerJob(customerToRemove));
		
		assertEquals(RemoveCustomerStatus.JOB_REMOVE_EXIST, removeCustomerQuartzJobService.runRemoveCustomerJob(customerToRemove));		
	}

	@Test
	void testCancelRemoveCustomerJob() throws SchedulerException {
		User user = new User();
		user.setUsername("username");
		Customer customerToRemove = new Customer();
		customerToRemove.setId(66L);
		customerToRemove.setFirstName("firstName");
		customerToRemove.setLastName("lastName");
		customerToRemove.setLanguage("pl");
		customerToRemove.setUser(user);
		String customerToRemoveId = "customerToRemove"+String.valueOf(customerToRemove.getId());
		
		scheduler = Mockito.mock(Scheduler.class);
		removeCustomerQuartzJobService = new RemoveCustomerQuartzJobService(userService, userRepository, scheduler, sendEmail, messageSource, sessionRegistry);
		assertEquals(RemoveCustomerStatus.JOB_REMOVE_TRIGGER_NOT_EXIST, removeCustomerQuartzJobService.cancelRemoveCustomerJob(customerToRemove));		

		
		JobKey jobKey = new JobKey(customerToRemoveId);
		JobDetail jobDetail = JobBuilder.newJob(RemoveCustomerQuartzJobService.class)
				.withIdentity(customerToRemoveId, "removeCustomerJob")
				.withDescription("Remove customer from application - job detail")
				.withIdentity(jobKey)
				.storeDurably()
				.build();
		
		TriggerKey triggerKey = new TriggerKey(customerToRemoveId);
		SimpleTriggerImpl simpleTriggerImpl = new SimpleTriggerImpl();
		simpleTriggerImpl.setKey(triggerKey);
		simpleTriggerImpl.setName(customerToRemoveId);
		simpleTriggerImpl.setGroup("removeCustomerTrigger");
		simpleTriggerImpl.setJobName(customerToRemoveId);
		simpleTriggerImpl.setJobKey(jobKey);
		simpleTriggerImpl.setStartTime(DateBuilder.evenHourDateAfterNow());
		
		jobStore = new RAMJobStore();
		jobStore.storeJobAndTrigger(jobDetail, simpleTriggerImpl);
		jobStore.initialize(loadHelper, schedulerSignaler);
		
		quartzSchedulerResources = new QuartzSchedulerResources();
		quartzSchedulerResources.setJobStore(jobStore);
		quartzSchedulerResources.setName("name123");
		quartzSchedulerResources.setThreadExecutor(threadExecutor);
		quartzScheduler = new QuartzScheduler(quartzSchedulerResources, 1000, 1000);
		scheduler = new StdScheduler(quartzScheduler);
		
		removeCustomerQuartzJobService = new RemoveCustomerQuartzJobService(userService, userRepository, scheduler, sendEmail, messageSource, sessionRegistry);
		assertEquals(RemoveCustomerStatus.JOB_REMOVE_CANCEL_DEFEAT, removeCustomerQuartzJobService.cancelRemoveCustomerJob(customerToRemove));
	}


}
