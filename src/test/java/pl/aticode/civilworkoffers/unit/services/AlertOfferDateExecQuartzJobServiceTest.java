package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.ThreadExecutor;
import org.springframework.context.MessageSource;

import pl.aticode.civilworkoffers.dao.OfferRepository;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestContent;
import pl.aticode.civilworkoffers.entity.user.Customer;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.service.AlertOfferDateExecQuartzJobService;
import pl.aticode.civilworkoffers.service.SendEmailService;
import pl.aticode.civilworkoffers.service.UserService;

class AlertOfferDateExecQuartzJobServiceTest {
	
	
	@InjectMocks
	private AlertOfferDateExecQuartzJobService alertOfferDateExecQuartzJobService;
	@Mock
	private UserService userService;
	@Mock
	private OfferRepository offerRepository;
	@Mock
	private JobExecutionContext context;
	@Mock
	private SchedulerContext schedulerContext;
	@Mock
	private Trigger trigger; 
    @Mock
    private SendEmailService sendEmail;
	@Mock
	private MessageSource messageSource;
	@Mock
	private ThreadExecutor threadExecutor;
	@Mock
	private SchedulerSignaler schedulerSignaler;
	@Mock
	private JobDetail jobDetail;
	@Mock
	private JobDataMap jobDataMap;
	@Mock
	private Scheduler scheduler;

	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	
	@Test
	void testExecuteInternalJobExecutionContext() throws SchedulerException {
		Employee employee = new Employee();
		employee.setFirstName("firstName");
		employee.setLastName("lastName");
		employee.setEmail("email_employee");
		employee.setLanguage("PL");
		Customer customer = new Customer();
		customer.setFirstName("firstName");
		customer.setLastName("lastName");
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent("content");
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(13L);
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		offerRequest.setEmployee(employee);
		offerRequest.setCustomer(customer);
		offerRequest.setRegisterDateTime(LocalDateTime.now().minusDays(5));
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		JobKey jobKey = new JobKey("jobKey");
		Mockito.when(context.getTrigger()).thenReturn(trigger);
		Mockito.when(trigger.getJobKey()).thenReturn(jobKey);
		Mockito.when(context.getScheduler()).thenReturn(scheduler);
		Mockito.when(scheduler.getContext()).thenReturn(schedulerContext);
		Mockito.when(context.getJobDetail()).thenReturn(jobDetail);
		Mockito.when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
		Mockito.when(jobDataMap.get(jobKey.getName())).thenReturn(offerRequest.getId());
		Mockito.when(offerRepository.findOfferRequest(offerRequest.getId())).thenReturn(offerRequest);
		
		ArgumentCaptor<String> argCaptorMailTo = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> argCaptorMailSubject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> argCaptorMailContent = ArgumentCaptor.forClass(String.class);
		doNothing().when(sendEmail).sendEmail(argCaptorMailTo.capture(), argCaptorMailSubject.capture(), argCaptorMailContent.capture());
		alertOfferDateExecQuartzJobService.execute(context);
		assertEquals("email_employee", argCaptorMailTo.getValue());
		
		offerRequest.setCustomer(null);
		alertOfferDateExecQuartzJobService.execute(context);
		assertThrows(NullPointerException.class, () -> offerRequest.getCustomer().getFirstName());
	}

	@Test
	void testAlertOfferDateExecJob() throws SchedulerException {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(23L);
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		alertOfferDateExecQuartzJobService.alertOfferDateExecJob(offerRequest);
		
		ArgumentCaptor<JobDetail> argCaptorJobDetail = ArgumentCaptor.forClass(JobDetail.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<SimpleTrigger>> argCaptorSimpleTrgger = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Boolean> argCaptorReplace = ArgumentCaptor.forClass(Boolean.class);
		doNothing().when(scheduler).scheduleJob(argCaptorJobDetail.capture(), argCaptorSimpleTrgger.capture(), argCaptorReplace.capture());
		alertOfferDateExecQuartzJobService.updateAlertOfferDateExecJob(offerRequest);
		assertEquals("alertExecDateOfferReq23", argCaptorJobDetail.getValue().getKey().getName());
	}

	@Test
	void testUpdateAlertOfferDateExecJob() throws SchedulerException {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(23L);
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		
		ArgumentCaptor<JobDetail> argCaptorJobDetail = ArgumentCaptor.forClass(JobDetail.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<SimpleTrigger>> argCaptorSimpleTrgger = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Boolean> argCaptorReplace = ArgumentCaptor.forClass(Boolean.class);
		doNothing().when(scheduler).scheduleJob(argCaptorJobDetail.capture(), argCaptorSimpleTrgger.capture(), argCaptorReplace.capture());
		alertOfferDateExecQuartzJobService.updateAlertOfferDateExecJob(offerRequest);
		assertEquals("alertExecDateOfferReq23", argCaptorJobDetail.getValue().getKey().getName());
	}

	@Test
	void testRemoveAllAlertsOfferDateExecJobCustomer() {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(23L);
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		List<OfferRequest> offerRequestList = new ArrayList<>();
		offerRequestList.add(offerRequest);
		alertOfferDateExecQuartzJobService.removeAllAlertsOfferDateExecJobCustomer(offerRequestList);
	}

	@Test
	void testRemoveAlertOfferDateExecJob() throws SchedulerException {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(23L);
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		
		ArgumentCaptor<JobDetail> argCaptorJobDetail = ArgumentCaptor.forClass(JobDetail.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Set<SimpleTrigger>> argCaptorSimpleTrgger = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Boolean> argCaptorReplace = ArgumentCaptor.forClass(Boolean.class);
		doNothing().when(scheduler).scheduleJob(argCaptorJobDetail.capture(), argCaptorSimpleTrgger.capture(), argCaptorReplace.capture());
		alertOfferDateExecQuartzJobService.updateAlertOfferDateExecJob(offerRequest);
		assertEquals("alertExecDateOfferReq23", argCaptorJobDetail.getValue().getKey().getName());
	}

}
