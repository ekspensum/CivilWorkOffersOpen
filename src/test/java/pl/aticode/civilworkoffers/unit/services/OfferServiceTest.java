package pl.aticode.civilworkoffers.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;

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
import pl.aticode.civilworkoffers.model.OfferData;
import pl.aticode.civilworkoffers.service.AlertOfferDateExecQuartzJobService;
import pl.aticode.civilworkoffers.service.DrawEmployeeService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.SendEmailService;
import pl.aticode.civilworkoffers.service.UserService;

class OfferServiceTest {
	
	@InjectMocks
	private OfferService offerService;
	@Mock
    private OfferRepository offerRepository;
	@Mock
    private UserService userService;
	@Mock
    private DrawEmployeeService drawEmployeeService;
	@Mock
    private MessageSource messageSource;
	@Mock
    private SendEmailService sendEmail;
	@Mock
    private AlertOfferDateExecQuartzJobService alertOfferDateExecQuartzJobService;
	
	private Model model;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testAddOfferRequestByCustomer() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(13L);
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		offerRequest.setRegisterDateTime(LocalDateTime.now().minusDays(2));
		
		Employee employee = new Employee();
		employee.setFirstName("firstName");
		employee.setLastName("lastName");
		employee.setLanguage("PL");
		offerRequest.setEmployee(employee);
		Mockito.when(drawEmployeeService.drawEmployee()).thenReturn(employee);
		
		User user = new User();
		user.setUsername("username1");
		Customer customer = new Customer();
		customer.setFirstName("firstName");
		customer.setLastName("lastName");
		customer.setLanguage("EN");
		customer.setUser(user);
		offerRequest.setCustomer(customer);
		Mockito.when(userService.getLoggedCustomer()).thenReturn(customer);
		
		Mockito.when(sendEmail.getMailFrom()).thenReturn("emailFrom");
		
		String content = "content";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		MultipartFile multipartFile1 = new MockMultipartFile("fileName1", "content1".getBytes());
		MultipartFile multipartFile2 = new MockMultipartFile("fileName2", "content2".getBytes());
		List<MultipartFile> attachments = new ArrayList<>();
		attachments.add(multipartFile1);
		attachments.add(multipartFile2);
		offerRequest.setAttachments(attachments);
		
		Mockito.when(messageSource.getMessage("employee.offerrequest.assign.subject", null, new Locale(offerRequest.getEmployee().getLanguage()))).thenReturn("mailSubject");
		Mockito.when(messageSource.getMessage("employee.offerrequest.assign.content",
                new String[]{offerRequest.getEmployee().getFirstName(), offerRequest.getEmployee().getLastName(),
                    offerRequest.getCustomer().getFirstName(), offerRequest.getCustomer().getLastName(),
                    offerRequest.getExecutionDate().toString(),
                    offerRequest.getRegisterDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    content.substring(0, content.length() < 50 ? content.length() : 50) + " ....",
                    sendEmail.getMailFrom()},
                new Locale(offerRequest.getEmployee().getLanguage()))).thenReturn("mailContent");
		
		Mockito.when(messageSource.getMessage("customer.offerrequest.add.subject", null, new Locale(offerRequest.getCustomer().getLanguage()))).thenReturn("mailSubject");
		Mockito.when(messageSource.getMessage("customer.offerrequest.add.content",
                new String[]{offerRequest.getCustomer().getFirstName(), offerRequest.getCustomer().getLastName(),
                    offerRequest.getExecutionDate().toString(),
                    offerRequest.getRegisterDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    content.substring(0, content.length() < 50 ? content.length() : 50) + " ....",
                    sendEmail.getMailFrom()},
                new Locale(offerRequest.getCustomer().getLanguage()))).thenReturn("mailContent");
		
		offerService.addOfferRequestByCustomer(offerRequest);
		assertEquals(50, offerRequest.getOfferRequestAttachment().get(1).getByteAttachment().getFile()[7]);
		assertEquals("username1", offerRequest.getOfferRequestContent().get(0).getUserRegister().getUsername());
	}

	@Test
	void testAddOfferRequestByEmployee() throws IOException, Exception {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(23L);
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		offerRequest.setRegisterDateTime(LocalDateTime.now().minusDays(2));

		User user1 = new User();
		user1.setUsername("username1");
		Employee employee = new Employee();
		employee.setFirstName("firstName");
		employee.setLastName("lastName");
		employee.setLanguage("PL");
		employee.setUser(user1);
		offerRequest.setEmployee(employee);
		Mockito.when(userService.getLoggedEmployee()).thenReturn(employee);
		
		User user2 = new User();
		user2.setUsername("username2");
		Customer customer = new Customer();
		customer.setFirstName("firstName");
		customer.setLastName("lastName");
		customer.setLanguage("EN");
		customer.setUser(user2);
		offerRequest.setCustomer(customer);
		Mockito.when(userService.getLoggedCustomer()).thenReturn(customer);
		
		Mockito.when(sendEmail.getMailFrom()).thenReturn("emailFrom");
		
		String content = "content";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		MultipartFile multipartFile1 = new MockMultipartFile("fileName1", "content1".getBytes());
		MultipartFile multipartFile2 = new MockMultipartFile("fileName2", "content2".getBytes());
		List<MultipartFile> attachments = new ArrayList<>();
		attachments.add(multipartFile1);
		attachments.add(multipartFile2);
		offerRequest.setAttachments(attachments);
		
		Mockito.when(messageSource.getMessage("employee.offerrequest.assign.subject", null, new Locale(offerRequest.getEmployee().getLanguage()))).thenReturn("mailSubject");
		Mockito.when(messageSource.getMessage("employee.offerrequest.assign.content",
                new String[]{offerRequest.getEmployee().getFirstName(), offerRequest.getEmployee().getLastName(),
                    offerRequest.getCustomer().getFirstName(), offerRequest.getCustomer().getLastName(),
                    offerRequest.getExecutionDate().toString(),
                    offerRequest.getRegisterDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    content.substring(0, content.length() < 50 ? content.length() : 50) + " ....",
                    sendEmail.getMailFrom()},
                new Locale(offerRequest.getEmployee().getLanguage()))).thenReturn("mailContent");
		
		Mockito.when(messageSource.getMessage("customer.offerrequest.add.subject", null, new Locale(offerRequest.getCustomer().getLanguage()))).thenReturn("mailSubject");
		Mockito.when(messageSource.getMessage("customer.offerrequest.add.content",
                new String[]{offerRequest.getCustomer().getFirstName(), offerRequest.getCustomer().getLastName(),
                    offerRequest.getExecutionDate().toString(),
                    offerRequest.getRegisterDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    content.substring(0, content.length() < 50 ? content.length() : 50) + " ....",
                    sendEmail.getMailFrom()},
                new Locale(offerRequest.getCustomer().getLanguage()))).thenReturn("mailContent");
		
		offerService.addOfferRequestByEmployee(offerRequest, customer);
		assertEquals(50, offerRequest.getOfferRequestAttachment().get(1).getByteAttachment().getFile()[7]);
		assertEquals("username1", offerRequest.getOfferRequestAttachment().get(0).getUserRegister().getUsername());
		assertEquals("username1", offerRequest.getOfferRequestContent().get(0).getUserRegister().getUsername());
	}

	@Test
	void testGetOfferRequest() {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(23L);
		Mockito.when(offerRepository.findOfferRequest(offerRequest.getId())).thenReturn(offerRequest);
		assertEquals(23, offerService.getOfferRequest(offerRequest.getId()).getId());
	}

	@Test
	void testGetAllCustomerOfferRequests() {
		Customer customer = new Customer();
		Mockito.when(userService.getLoggedCustomer()).thenReturn(customer);
		OfferRequest offerRequest1 = new OfferRequest();
		offerRequest1.setId(33L);
		offerRequest1.setRegisterDateTime(LocalDateTime.now().minusDays(2));
		OfferRequest offerRequest2 = new OfferRequest();
		offerRequest2.setRegisterDateTime(LocalDateTime.now());
		List<OfferRequest> offerRequestList = new ArrayList<>();
		offerRequestList.add(offerRequest1);
		offerRequestList.add(offerRequest2);
		Mockito.when(offerRepository.findAllOfferRequest(customer)).thenReturn(offerRequestList);
		assertEquals(33, offerService.getAllCustomerOfferRequests().get(1).getId());
	}

	@Test
	void testGetAllEmployeeOfferRequests() {
		Employee employee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(employee);
		OfferRequest offerRequest1 = new OfferRequest();
		offerRequest1.setId(33L);
		offerRequest1.setRegisterDateTime(LocalDateTime.now().minusDays(2));
		OfferRequest offerRequest2 = new OfferRequest();
		offerRequest2.setRegisterDateTime(LocalDateTime.now());
		List<OfferRequest> offerRequestList = new ArrayList<>();
		offerRequestList.add(offerRequest1);
		offerRequestList.add(offerRequest2);
		Mockito.when(offerRepository.findAllOfferRequest(employee)).thenReturn(offerRequestList);
		assertEquals(33, offerService.getAllEmployeeOfferRequests().get(1).getId());
	}

	@Test
	void testEditOfferRequestContent() throws Exception {
		User loggedUser = new User();
		loggedUser.setId(33L);
		Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestContent offerRequestContent1 = new OfferRequestContent();
		OfferRequestContent offerRequestContent2 = new OfferRequestContent();
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent1);
		offerRequestContentList.add(offerRequestContent2);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		offerService.editOfferRequestContent(offerRequest);
		assertEquals(33, offerRequest.getOfferRequestContent().get(1).getUserRegister().getId());
	}

	@Test
	void testEditOfferRequestComment() throws Exception {
		User loggedUser = new User();
		loggedUser.setId(33L);
		Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestComment offerRequestComment1 = new OfferRequestComment();
		OfferRequestComment offerRequestComment2 = new OfferRequestComment();
		List<OfferRequestComment> offerRequestCommentList = new ArrayList<>();
		offerRequestCommentList.add(offerRequestComment1);
		offerRequestCommentList.add(offerRequestComment2);
		offerRequest.setOfferRequestComment(offerRequestCommentList);
		
		offerService.editOfferRequestComment(offerRequest);
		assertEquals(33, offerRequest.getOfferRequestComment().get(1).getUserRegister().getId());
	}

	@Test
	void testEditOfferRequestFile() throws Exception {
		User loggedUser = new User();
		loggedUser.setId(33L);
		Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
		
		OfferRequest offerRequest = new OfferRequest();
		MultipartFile multipartFile1 = new MockMultipartFile("fileName1", "content1".getBytes());
		MultipartFile multipartFile2 = new MockMultipartFile("fileName2", "content2".getBytes());
		List<MultipartFile> attachments = new ArrayList<>();
		attachments.add(multipartFile1);
		attachments.add(multipartFile2);
		offerRequest.setAttachments(attachments);
		offerRequest.setOfferRequestAttachment(new ArrayList<OfferRequestAttachment>());
		
		offerService.editOfferRequestFile(offerRequest);
		assertEquals(50, offerRequest.getOfferRequestAttachment().get(1).getByteAttachment().getFile()[7]);
	}

	@Test
	void testGetByteAttachment() {
		ByteAttachment byteAttachment = new ByteAttachment();
		Mockito.when(offerRepository.findByteAttachment(11L)).thenReturn(byteAttachment);
		assertNotNull(offerService.getByteAttachment(11L));
	}

	@Test
	void testEditOfferRequestDate() throws Exception {
		User loggedUser = new User();
		loggedUser.setId(33L);
		Mockito.when(userService.getLoggedUser()).thenReturn(loggedUser);
		OfferRequest offerRequest = new OfferRequest();
		offerService.editOfferRequestDate(offerRequest);
		assertNotNull(offerRequest.getEditDateTime());
	}

	@Test
	void testUpdateOfferRequestSaveEstimate() throws Exception {
		int estimateIdx = 0;
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		EstimateItem estimateItem = new EstimateItem();
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem);
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		offerService.updateOfferRequestSaveEstimate(offerRequest, estimateIdx, loggedEmployee);
		assertNotNull(offerRequest.getEstimate().get(0).getLastSaved());
		assertNotNull(offerRequest.getEstimate().get(0).getEstimateItem().get(0).getLastSaved());
	}

	@Test
	void testUpdateEstimateTypeOfCivilWork() throws Exception {
		int estimateIdx = 0;
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		
		Estimate estimate = new Estimate();
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		String typeOfCivilWorkInput1 = "typeOfCivilWorkInput1";
		String typeOfCivilWorkInput2 = "typeOfCivilWorkInput2";
		TypeOfCivilWork typeOfCivilWork1 = new TypeOfCivilWork();
		typeOfCivilWork1.setName(typeOfCivilWorkInput1);
		TypeOfCivilWork typeOfCivilWork2 = new TypeOfCivilWork();
		typeOfCivilWork2.setName(typeOfCivilWorkInput2);
		List<TypeOfCivilWork> allTypeOfCivilWorkList = new ArrayList<>();
		allTypeOfCivilWorkList.add(typeOfCivilWork1);
		allTypeOfCivilWorkList.add(typeOfCivilWork2);
		Mockito.when(offerRepository.findAllTypeOfCivilWorks()).thenReturn(allTypeOfCivilWorkList);
		
		offerService.updateEstimateTypeOfCivilWork(offerRequest, typeOfCivilWorkInput2, estimateIdx, loggedEmployee);
		assertEquals("typeOfCivilWorkInput2", offerRequest.getEstimate().get(estimateIdx).getTypeOfCivilWork());
		
		ArgumentCaptor<TypeOfCivilWork> newTypeOfCivilWork = ArgumentCaptor.forClass(TypeOfCivilWork.class);
		doNothing().when(offerRepository).saveTypeOfCivilWorks(newTypeOfCivilWork.capture());
		offerService.updateEstimateTypeOfCivilWork(offerRequest, "typeOfCivilWorkInputXX", estimateIdx, loggedEmployee);
		assertEquals("typeOfCivilWorkInputXX", newTypeOfCivilWork.getValue().getName());
		assertNotNull(newTypeOfCivilWork.getValue().getEmployeeRegister());
	}

	@Test
	void testUpdateEstimateUnitOfMeasurement() throws Exception {
		int estimateIdx = 0;
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		
		String[] unitOfMeasurementArray = {"unitOfMeasurement1", "unitOfMeasurement2", "unitOfMeasurement3"};
		
		EstimateItem estimateItem1 = new EstimateItem();
		EstimateItem estimateItem2 = new EstimateItem();
		EstimateItem estimateItem3 = new EstimateItem();
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		UnitOfMeasurement unitOfMeasurement11 = new UnitOfMeasurement();
		unitOfMeasurement11.setName("unitOfMeasurement11");
		UnitOfMeasurement unitOfMeasurement2 = new UnitOfMeasurement();
		unitOfMeasurement2.setName("unitOfMeasurement2");
		List<UnitOfMeasurement> allUnitOfMeasurementList = new ArrayList<>();
		allUnitOfMeasurementList.add(unitOfMeasurement11);
		allUnitOfMeasurementList.add(unitOfMeasurement2);
		Mockito.when(offerRepository.findAllUnitOfMeasurement()).thenReturn(allUnitOfMeasurementList);
		
		offerService.updateEstimateUnitOfMeasurement(offerRequest, unitOfMeasurementArray, estimateIdx, loggedEmployee);
		assertEquals("unitOfMeasurement2", offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(1).getUnitOfMeasurement());

		ArgumentCaptor<UnitOfMeasurement> newUnitOfMeasurement = ArgumentCaptor.forClass(UnitOfMeasurement.class);
		doNothing().when(offerRepository).saveUnitOfMeasurement(newUnitOfMeasurement.capture());
		offerService.updateEstimateUnitOfMeasurement(offerRequest, unitOfMeasurementArray, estimateIdx, loggedEmployee);
		assertEquals("unitOfMeasurement3", newUnitOfMeasurement.getValue().getName());
	}

	@Test
	void testUpdateEstimateCopyItem() {
		long itemIdBox = 15;
		int estimateIdx = 0;
		EstimateItem foundEstimateItem = new EstimateItem();
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(foundEstimateItem);
		Mockito.when(offerRepository.findEstimateItemDetach(itemIdBox)).thenReturn(foundEstimateItem);
		
		Estimate estimate = new Estimate();
		estimate.setId(44L);
		estimate.setEstimateItem(new ArrayList<EstimateItem>());
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		offerService.updateEstimateCopyItem(offerRequest, estimateIdx, itemIdBox);
		assertEquals(44, foundEstimateItem.getEstimate().getId());
		assertEquals(0, foundEstimateItem.getSortIndex());
		assertEquals(1, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().size());
	}

	@Test
	void testUpdateEstimateAddItem() {
		int estimateIdx = 0;
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(new ArrayList<EstimateItem>());
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		offerService.updateEstimateAddItem(offerRequest, estimateIdx);
		assertEquals(0, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(0).getSortIndex());
		assertNull(offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(0).getId());
	}

	@Test
	void testUpdateEstimateInsertItemAbove() {
		Integer selectBox = 0;
		int estimateIdx = 0;
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setSortIndex(0);
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setSortIndex(1);
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setSortIndex(2);
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		offerService.updateEstimateInsertItemAbove(offerRequest, estimateIdx, selectBox);
		assertEquals(2, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(2).getSortIndex());
		assertEquals(3, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(3).getSortIndex());
		
		selectBox = 1;
		offerService.updateEstimateInsertItemAbove(offerRequest, estimateIdx, selectBox);
		assertEquals(1, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(1).getSortIndex());
		assertEquals(3, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(3).getSortIndex());
	}

	@Test
	void testUpdateEstimateInsertItemBelow() {
		Integer selectBox = 1;
		int estimateIdx = 0;
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setSortIndex(0);
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setSortIndex(1);
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setSortIndex(2);
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		offerService.updateEstimateInsertItemBelow(offerRequest, estimateIdx, selectBox);
		assertEquals(1, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(1).getSortIndex());
		assertEquals(3, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(3).getSortIndex());
	}

	@Test
	void testUpdateEstimateMoveItemUp() {
		Integer selectBox = 1;
		int estimateIdx = 0;
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setSortIndex(0);
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setSortIndex(1);
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setSortIndex(2);
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		offerService.updateEstimateMoveItemUp(offerRequest, estimateIdx, selectBox);
		assertEquals(1, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(0).getSortIndex());
		assertEquals(0, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(1).getSortIndex());
		assertEquals(2, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(2).getSortIndex());
	}

	@Test
	void testUpdateEstimateMoveItemDown() {
		Integer selectBox = 1;
		int estimateIdx = 0;
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setSortIndex(0);
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setSortIndex(1);
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setSortIndex(2);
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		offerService.updateEstimateMoveItemDown(offerRequest, estimateIdx, selectBox);
		assertEquals(0, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(0).getSortIndex());
		assertEquals(2, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(1).getSortIndex());
		assertEquals(1, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(2).getSortIndex());
	}

	@Test
	void testUpdateEstimateRemoveItem() {
		Integer selectBox = 1;
		int estimateIdx = 0;
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setSortIndex(0);
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setSortIndex(1);
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setSortIndex(2);
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		offerService.updateEstimateRemoveItem(offerRequest, estimateIdx, selectBox);
		assertEquals(2, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().size());
		assertEquals(0, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(0).getSortIndex());
		assertEquals(1, offerRequest.getEstimate().get(estimateIdx).getEstimateItem().get(1).getSortIndex());
	}

	@Test
	void testUpdateOfferRequestEstimateDetach() throws Exception {
		Estimate estimate = new Estimate();
		OfferRequest offerRequest = new OfferRequest();
		offerService.updateOfferRequestEstimateDetach(offerRequest, estimate);
	}

	@Test
	void testUpdateOfferRequestEstimateConnect() throws Exception {
		Estimate estimate = new Estimate();
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		Estimate foundEstimateDetach = new Estimate();
		foundEstimateDetach.setId(11L);
		Mockito.when(offerRepository.findEstimateDetach(foundEstimateDetach.getId())).thenReturn(foundEstimateDetach);
		
		ArgumentCaptor<OfferRequest> offerRequestToEdit = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerRepository).editOfferRequest(offerRequestToEdit.capture());
		offerService.updateOfferRequestEstimateConnect(offerRequest, foundEstimateDetach.getId());
		assertEquals(11, offerRequestToEdit.getValue().getEstimate().get(0).getId());
	}

	@Test
	void testGetOfferRequestInitializeEstimate() {
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setId(22L);
		estimateItem1.setSortIndex(0);
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setId(33L);
		estimateItem2.setSortIndex(1);
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setId(11L);
		estimateItem3.setSortIndex(2);
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimateItemList.sort((id1, id2) -> id1.getId().compareTo(id2.getId()));
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		Mockito.when(offerRepository.findOfferRequestInitializeEstimate(offerRequest)).thenReturn(offerRequest);
		
		OfferRequest offerRequestInitializeEstimate = offerService.getOfferRequestInitializeEstimate(offerRequest);
		assertEquals(22, offerRequestInitializeEstimate.getEstimate().get(0).getEstimateItem().get(0).getId());
		assertEquals(33, offerRequestInitializeEstimate.getEstimate().get(0).getEstimateItem().get(1).getId());
		assertEquals(11, offerRequestInitializeEstimate.getEstimate().get(0).getEstimateItem().get(2).getId());
	}

	@Test
	void testCopyEstimate() throws Exception {
		Estimate estimate1 = new Estimate();
		Estimate estimate2 = new Estimate();
		estimate2.setId(55L);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate1);
		estimateList.add(estimate2);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setId(22L);
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setId(33L);
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setId(11L);
		estimateItem3.setSortIndex(2);
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate foundEstimateDetach = new Estimate();
		foundEstimateDetach.setId(11L);
		foundEstimateDetach.setEstimateItem(estimateItemList);
		Mockito.when(offerRepository.findEstimateDetach(foundEstimateDetach.getId())).thenReturn(foundEstimateDetach);
		
		ArgumentCaptor<OfferRequest> offerRequestToEdit = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerRepository).editOfferRequest(offerRequestToEdit.capture());
		offerService.copyEstimate(offerRequest, foundEstimateDetach.getId());
		assertEquals(2, offerRequest.getEstimate().size());
		assertEquals(55, offerRequestToEdit.getValue().getEstimate().get(0).getId());
		assertNull(offerRequestToEdit.getValue().getEstimate().get(1).getEstimateItem().get(0).getId());
		assertEquals(2, offerRequestToEdit.getValue().getEstimate().get(1).getEstimateItem().get(2).getSortIndex());
	}

	@Test
	void testDeleteEstimate() throws Exception {
		offerService.deleteEstimate(3L);
	}

	@Test
	void testGetOrphanEstimatesEmployee() {
		Estimate estimate = new Estimate();
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		Mockito.when(offerRepository.findOrphanEstimatesEmployee(loggedEmployee)).thenReturn(estimateList);
		
		List<Estimate> orphanEstimatesEmployee = offerService.getOrphanEstimatesEmployee();
		assertNotNull(orphanEstimatesEmployee.get(0));
	}

	@Test
	void testGetEstimate() {
		Mockito.when(offerRepository.findEstimateDetach(3)).thenReturn(new Estimate());
		assertNotNull(offerService.getEstimate(3));
	}

	@Test
	void testLoadDataForOfferRequestEsimate() {
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setId(22L);
		estimateItem1.setSortIndex(0);
		estimateItem1.setPrice(BigDecimal.valueOf(22.22));
		estimateItem1.setQuantity(BigDecimal.valueOf(22.22));
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setId(33L);
		estimateItem2.setSortIndex(1);
		estimateItem1.setPrice(BigDecimal.valueOf(33.33));
		estimateItem1.setQuantity(BigDecimal.valueOf(33.33));
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setId(11L);
		estimateItem3.setSortIndex(2);
		estimateItem1.setPrice(BigDecimal.valueOf(11.11));
		estimateItem1.setQuantity(BigDecimal.valueOf(11.11));
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimateItemList.sort((id1, id2) -> id1.getId().compareTo(id2.getId()));
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		Mockito.when(offerRepository.findOfferRequestInitializeEstimate(offerRequest)).thenReturn(offerRequest);
		
		model = new ConcurrentModel();
		OfferRequest offerRequestInitializeEstimate = offerService.getOfferRequestInitializeEstimate(offerRequest);
		offerService.loadDataForOfferRequestEsimate(offerRequest, model);
		
		assertEquals(22, offerRequestInitializeEstimate.getEstimate().get(0).getEstimateItem().get(0).getId());
		assertEquals(33, offerRequestInitializeEstimate.getEstimate().get(0).getEstimateItem().get(1).getId());
		assertEquals(11, offerRequestInitializeEstimate.getEstimate().get(0).getEstimateItem().get(2).getId());
		
		@SuppressWarnings("unchecked")
		List<BigDecimal> totalPriceList = (List<BigDecimal>) model.getAttribute("totalPriceList");
		assertEquals(123.4321, totalPriceList.get(0).doubleValue());
	}

	@Test
	void testLoadDataForOfferRequestOfferEsimate() {
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setId(22L);
		estimateItem1.setSortIndex(0);
		estimateItem1.setPrice(BigDecimal.valueOf(22.22));
		estimateItem1.setQuantity(BigDecimal.valueOf(22.22));
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setId(33L);
		estimateItem2.setSortIndex(1);
		estimateItem1.setPrice(BigDecimal.valueOf(33.33));
		estimateItem1.setQuantity(BigDecimal.valueOf(33.33));
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setId(11L);
		estimateItem3.setSortIndex(2);
		estimateItem1.setPrice(BigDecimal.valueOf(11.11));
		estimateItem1.setQuantity(BigDecimal.valueOf(11.11));
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimateItemList.sort((id1, id2) -> id1.getId().compareTo(id2.getId()));
		estimate.setEstimateItem(estimateItemList);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		LocalDateTime localDateTimeNow = LocalDateTime.now();
		Offer offer1 = new Offer();
		offer1.setSaveDateTime(localDateTimeNow.minusDays(2));
		Offer offer2 = new Offer();
		offer2.setSaveDateTime(localDateTimeNow.minusDays(1));
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer1);
		offerList.add(offer2);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		offerRequest.setOffer(offerList);
		Mockito.when(offerRepository.findOfferRequestInitializeOfferAndEstimate(offerRequest)).thenReturn(offerRequest);
		
		model = new ConcurrentModel();
		offerService.loadDataForOfferRequestOfferEsimate(offerRequest, model);
		OfferRequest offerRequestInitializeOfferAndEstimate = (OfferRequest) model.getAttribute("offerRequest");
		
		assertEquals(11, offerRequestInitializeOfferAndEstimate.getEstimate().get(0).getEstimateItem().get(0).getId());
		assertEquals(22, offerRequestInitializeOfferAndEstimate.getEstimate().get(0).getEstimateItem().get(1).getId());
		assertEquals(33, offerRequestInitializeOfferAndEstimate.getEstimate().get(0).getEstimateItem().get(2).getId());
		
		@SuppressWarnings("unchecked")
		List<BigDecimal> totalPriceList = (List<BigDecimal>) model.getAttribute("totalPriceList");
		assertEquals(123.4321, totalPriceList.get(0).doubleValue());
	}

	@Test
	void testGetAllEstimatesEmployee() {
		LocalDateTime localDateTimeNow = LocalDateTime.now();
		Estimate estimate1 = new Estimate();
		estimate1.setLastSaved(localDateTimeNow.minusDays(1));
		Estimate estimate2 = new Estimate();
		estimate2.setLastSaved(localDateTimeNow.minusDays(2));
		Estimate estimate3 = new Estimate();
		estimate3.setLastSaved(localDateTimeNow.minusDays(3));
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate1);
		estimateList.add(estimate2);
		estimateList.add(estimate3);
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		Mockito.when(offerRepository.findAllEstimatesEmployee(loggedEmployee)).thenReturn(estimateList);
		
		List<Estimate> allEstimatesEmployee = offerService.getAllEstimatesEmployee();
		assertEquals(localDateTimeNow.minusDays(1), allEstimatesEmployee.get(0).getLastSaved());
		assertEquals(localDateTimeNow.minusDays(2), allEstimatesEmployee.get(1).getLastSaved());
		assertEquals(localDateTimeNow.minusDays(3), allEstimatesEmployee.get(2).getLastSaved());
	}

	@Test
	void testGetEstimateTotalPrice() {
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setPrice(BigDecimal.valueOf(22.22));
		estimateItem1.setQuantity(BigDecimal.valueOf(22.22));
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem1.setPrice(BigDecimal.valueOf(33.33));
		estimateItem1.setQuantity(BigDecimal.valueOf(33.33));
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem1.setPrice(BigDecimal.valueOf(11.11));
		estimateItem1.setQuantity(BigDecimal.valueOf(11.11));
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimate.setEstimateItem(estimateItemList);
		
		BigDecimal estimateTotalPrice = offerService.getEstimateTotalPrice(estimate);
		assertEquals(123.4321, estimateTotalPrice.doubleValue());
	}

	@Test
	void testGenerateAndAddPdfEstimateAttachmentToOffer() throws DocumentException, IOException {
		Employee employee = new Employee();
		employee.setFirstName("firstName");
		employee.setLastName("lastName");
		Long estimateId = 77L;
		EstimateItem estimateItem1 = new EstimateItem();
		estimateItem1.setId(22L);
		estimateItem1.setSortIndex(0);
		estimateItem1.setPrice(BigDecimal.valueOf(22.22));
		estimateItem1.setQuantity(BigDecimal.valueOf(22.22));
		EstimateItem estimateItem2 = new EstimateItem();
		estimateItem2.setId(33L);
		estimateItem2.setSortIndex(1);
		estimateItem1.setPrice(BigDecimal.valueOf(33.33));
		estimateItem1.setQuantity(BigDecimal.valueOf(33.33));
		EstimateItem estimateItem3 = new EstimateItem();
		estimateItem3.setId(11L);
		estimateItem3.setSortIndex(2);
		estimateItem1.setPrice(BigDecimal.valueOf(11.11));
		estimateItem1.setQuantity(BigDecimal.valueOf(11.11));
		List<EstimateItem> estimateItemList = new ArrayList<>();
		estimateItemList.add(estimateItem1);
		estimateItemList.add(estimateItem2);
		estimateItemList.add(estimateItem3);
		Estimate estimate = new Estimate();
		estimate.setTypeOfCivilWork("typeOfCivilWork");
		estimate.setDescription("description");
		estimate.setLastSaved(LocalDateTime.now());
		estimate.setEmployee(employee);
		estimateItemList.sort((id1, id2) -> id1.getId().compareTo(id2.getId()));
		estimate.setEstimateItem(estimateItemList);
		Mockito.when(offerRepository.findEstimateDetach(estimateId)).thenReturn(estimate);
		
		LocalDateTime localDateTimeNow = LocalDateTime.now();
		Offer offer1 = new Offer();
		offer1.setOfferAttachment(new ArrayList<OfferAttachment>());
		offer1.setSaveDateTime(localDateTimeNow.minusDays(2));
		Offer offer2 = new Offer();
		offer2.setOfferAttachment(new ArrayList<OfferAttachment>());
		offer2.setSaveDateTime(localDateTimeNow.minusDays(1));
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer1);
		offerList.add(offer2);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setOffer(offerList);
		Mockito.when(offerRepository.findOffer(offerRequest)).thenReturn(offerList);
		
		Owner owner = new Owner();
		owner.setCompanyName("companyName");
		owner.setZipCode("zipCode");
		owner.setCity("city");
		owner.setStreet("street");
		owner.setStreetNo("streetNo");
		Mockito.when(userService.getOwner("1")).thenReturn(owner);
		
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		
		ArgumentCaptor<OfferRequest> offerRequestToEdit = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerRepository).editOfferRequest(offerRequestToEdit.capture());
		offerService.generateAndAddPdfEstimateAttachmentToOffer(offerRequest, estimateId);
		assertEquals(OfferStage.PREPARE, offerRequestToEdit.getValue().getOffer().get(0).getOfferAttachment().get(0).getOfferStage());
		assertEquals("estimate"+estimateId, offerRequestToEdit.getValue().getOffer().get(0).getOfferAttachment().get(0).getFileName());
		assertNotNull(offerRequestToEdit.getValue().getOffer().get(0).getOfferAttachment().get(0).getEmployeeRegister());
	}

	@Test
	void testGetOfferAttachment() {
		Long estimateId = 66L;
		OfferAttachment offerAttachment = new OfferAttachment();
		Mockito.when(offerRepository.findOfferAttachment(estimateId)).thenReturn(offerAttachment);
		assertNotNull(offerService.getOfferAttachment(estimateId));
	}

	@Test
	void testDeleteOfferAttachment() throws Exception {
		Long attachmentId = 2L;
		OfferAttachment offerAttachment1 = new OfferAttachment();
		offerAttachment1.setId(1L);
		OfferAttachment offerAttachment2 = new OfferAttachment();
		offerAttachment2.setId(2L);
		List<OfferAttachment> offerAttachmentList = new ArrayList<>();
		offerAttachmentList.add(offerAttachment1);
		offerAttachmentList.add(offerAttachment2);
		Offer offer = new Offer();
		offer.setSaveDateTime(LocalDateTime.now().minusDays(2));
		offer.setOfferAttachment(offerAttachmentList);
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setOffer(offerList);
		Mockito.when(offerRepository.findOffer(offerRequest)).thenReturn(offerList);
		
		ArgumentCaptor<OfferRequest> offerRequestToEdit = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerRepository).editOfferRequest(offerRequestToEdit.capture());
		offerService.deleteOfferAttachment(offerRequest, attachmentId);
		assertEquals(1, offerRequestToEdit.getValue().getOffer().get(0).getOfferAttachment().get(0).getId());
	}

	@Test
	void testAddTypeOfCivilWork() throws Exception {
		Employee loggedEmployee = new Employee();
		loggedEmployee.setId(55L);
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		TypeOfCivilWork typeOfCivilWork = new TypeOfCivilWork();
		
		ArgumentCaptor<TypeOfCivilWork> typeOfCivilWorkCaptor = ArgumentCaptor.forClass(TypeOfCivilWork.class);
		doNothing().when(offerRepository).saveTypeOfCivilWorks(typeOfCivilWorkCaptor.capture());
		offerService.addTypeOfCivilWork(typeOfCivilWork);
		assertEquals(55, typeOfCivilWorkCaptor.getValue().getEmployeeRegister().getId());
	}

	@Test
	void testGetAllTypeOfCivilWork() {
		TypeOfCivilWork typeOfCivilWork1 = new TypeOfCivilWork();
		typeOfCivilWork1.setName("name1");
		TypeOfCivilWork typeOfCivilWork2 = new TypeOfCivilWork();
		typeOfCivilWork2.setName("name2");
		List<TypeOfCivilWork> typeOfCivilWorkList = new ArrayList<>();
		typeOfCivilWorkList.add(typeOfCivilWork1);
		typeOfCivilWorkList.add(typeOfCivilWork2);
		Mockito.when(offerRepository.findAllTypeOfCivilWorks()).thenReturn(typeOfCivilWorkList);
		
		List<TypeOfCivilWork> allTypeOfCivilWork = offerService.getAllTypeOfCivilWork();
		assertEquals("name2", allTypeOfCivilWork.get(1).getName());
	}

	@Test
	void testGetAllTypeOfCivilWorkEmployee() {
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		
		TypeOfCivilWork typeOfCivilWork1 = new TypeOfCivilWork();
		typeOfCivilWork1.setName("name1");
		TypeOfCivilWork typeOfCivilWork2 = new TypeOfCivilWork();
		typeOfCivilWork2.setName("name2");
		List<TypeOfCivilWork> typeOfCivilWorkList = new ArrayList<>();
		typeOfCivilWorkList.add(typeOfCivilWork1);
		typeOfCivilWorkList.add(typeOfCivilWork2);
		Mockito.when(offerRepository.findAllTypeOfCivilWorks(loggedEmployee)).thenReturn(typeOfCivilWorkList);
		
		List<TypeOfCivilWork> allTypeOfCivilWork = offerService.getAllTypeOfCivilWorkEmployee();
		assertEquals("name2", allTypeOfCivilWork.get(1).getName());
	}

	@Test
	void testGetTypeOfCivilWork() {
		String name = "name";
		TypeOfCivilWork typeOfCivilWork = new TypeOfCivilWork();
		List<TypeOfCivilWork> typeOfCivilWorkList = new ArrayList<>();
		typeOfCivilWorkList.add(typeOfCivilWork);
		Mockito.when(offerRepository.findTypeOfCivilWork(name)).thenReturn(typeOfCivilWorkList);
		
		assertNotNull(offerService.getTypeOfCivilWork(name).get(0));
	}

	@Test
	void testDeleteTypeOfCivilWork() throws Exception {
		offerService.deleteTypeOfCivilWork(1L);
	}

	@Test
	void testAddUnitOfMeasurement() throws Exception {
		Employee loggedEmployee = new Employee();
		loggedEmployee.setId(55L);
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement();
		
		ArgumentCaptor<UnitOfMeasurement> unitOfMeasurementCaptor = ArgumentCaptor.forClass(UnitOfMeasurement.class);
		doNothing().when(offerRepository).saveUnitOfMeasurement(unitOfMeasurementCaptor.capture());
		offerService.addUnitOfMeasurement(unitOfMeasurement);
		assertEquals(55, unitOfMeasurementCaptor.getValue().getEmployeeRegister().getId());
	}

	@Test
	void testGetAllUnitOfMeasurement() {
		UnitOfMeasurement unitOfMeasurement1 = new UnitOfMeasurement();
		unitOfMeasurement1.setName("name1");
		UnitOfMeasurement unitOfMeasurement2 = new UnitOfMeasurement();
		unitOfMeasurement2.setName("name2");
		List<UnitOfMeasurement> unitOfMeasurementList = new ArrayList<>();
		unitOfMeasurementList.add(unitOfMeasurement1);
		unitOfMeasurementList.add(unitOfMeasurement2);
		Mockito.when(offerRepository.findAllUnitOfMeasurement()).thenReturn(unitOfMeasurementList);
		
		List<UnitOfMeasurement> allUnitOfMeasurement = offerService.getAllUnitOfMeasurement();
		assertEquals("name2", allUnitOfMeasurement.get(1).getName());
	}

	@Test
	void testGetAllUnitOfMeasurementEmployee() {
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		
		UnitOfMeasurement unitOfMeasurement1 = new UnitOfMeasurement();
		unitOfMeasurement1.setName("name1");
		UnitOfMeasurement unitOfMeasurement2 = new UnitOfMeasurement();
		unitOfMeasurement2.setName("name2");
		List<UnitOfMeasurement> unitOfMeasurementList = new ArrayList<>();
		unitOfMeasurementList.add(unitOfMeasurement1);
		unitOfMeasurementList.add(unitOfMeasurement2);
		Mockito.when(offerRepository.findAllUnitOfMeasurement(loggedEmployee)).thenReturn(unitOfMeasurementList);
		
		List<UnitOfMeasurement> allUnitOfMeasurement = offerService.getAllUnitOfMeasurementEmployee();
		assertEquals("name2", allUnitOfMeasurement.get(1).getName());
	}

	@Test
	void testGetUnitOfMeasurement() {
		String name = "name";
		UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement();
		List<UnitOfMeasurement> unitOfMeasurementList = new ArrayList<>();
		unitOfMeasurementList.add(unitOfMeasurement);
		Mockito.when(offerRepository.findUnitOfMeasurement(name)).thenReturn(unitOfMeasurementList);
		
		assertNotNull(offerService.getUnitOfMeasurement(name).get(0));
	}

	@Test
	void testDeleteUnitOfMeasurement() {
		offerRepository.removeUnitOfMeasurement(1L);
	}

	@Test
	void testGetFileAttachment() {
		String attachmentId = "2";
		OfferRequestAttachment offerRequestAttachment = new OfferRequestAttachment();
		Mockito.when(offerRepository.findFile(Long.valueOf(attachmentId))).thenReturn(offerRequestAttachment);
		
		assertNotNull(offerService.getFileAttachment(attachmentId));
	}

	@Test
	void testGetOfferRequestsByDate() {
		LocalDateTime localDateTimeNow = LocalDateTime.now();
		OfferRequest offerRequest1 = new OfferRequest();
		offerRequest1.setRegisterDateTime(localDateTimeNow.minusDays(1));
		OfferRequest offerRequest2 = new OfferRequest();
		offerRequest2.setRegisterDateTime(localDateTimeNow.minusDays(2));
		OfferRequest offerRequest3 = new OfferRequest();
		offerRequest3.setRegisterDateTime(localDateTimeNow.minusDays(3));
		List<OfferRequest> offerRequestList = new ArrayList<>();
		offerRequestList.add(offerRequest1);
		offerRequestList.add(offerRequest2);
		offerRequestList.add(offerRequest3);
		Mockito.when(offerRepository.findOfferRequestByDate(localDateTimeNow.minusDays(5), localDateTimeNow.plusDays(5))).thenReturn(offerRequestList);
		
		List<OfferRequest> offerRequestsByDate = offerService.getOfferRequestsByDate(localDateTimeNow.minusDays(5), localDateTimeNow.plusDays(5));
		assertEquals(localDateTimeNow.minusDays(3), offerRequestsByDate.get(2).getRegisterDateTime());
	}

	@Test
	void testAssignEmployee() throws Exception {
		String offerRequestData = "13;2";
		String[] employeeIdArray = {"1", "2", "66"};
		
		Employee employee = new Employee();
		employee.setId(77L);
		employee.setFirstName("firstName");
		employee.setLastName("lastName");
		employee.setLanguage("PL");
		employee.setEmail("email");
		Mockito.when(userService.getEmployee(employeeIdArray[2])).thenReturn(employee);
		
		Customer customer = new Customer();
		customer.setFirstName("firstName");
		customer.setLastName("lastName");
		customer.setLanguage("EN");
		
		String content = "content";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEmployee(employee);
		offerRequest.setCustomer(customer);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		offerRequest.setExecutionDate(LocalDate.now());
		offerRequest.setRegisterDateTime(LocalDateTime.now());
		Mockito.when(offerRepository.findOfferRequest(Long.valueOf("13"))).thenReturn(offerRequest);
		Mockito.when(userService.getLoggedEmployee()).thenReturn(employee);
		
		ArgumentCaptor<OfferRequest> offerRequestToEdit = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerRepository).editOfferRequest(offerRequestToEdit.capture());
		offerService.assignEmployee(offerRequestData, employeeIdArray);
		assertEquals(77, offerRequestToEdit.getValue().getEmployee().getId());
		assertNotNull(offerRequestToEdit.getValue().getEditDateTime());
		assertEquals("lastName", offerRequestToEdit.getValue().getCustomer().getLastName());
		assertEquals(content, offerRequestToEdit.getValue().getOfferRequestContent().get(0).getContent());
	}

	@Test
	void testGetInitializeOffer() {
		LocalDateTime localDateTimeNow = LocalDateTime.now();
		OfferRequest offerRequest = new OfferRequest();
		Offer offer1 = new Offer();
		offer1.setSaveDateTime(localDateTimeNow.minusDays(1));
		Offer offer2 = new Offer();
		offer2.setSaveDateTime(localDateTimeNow.minusDays(2));
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer1);
		offerList.add(offer2);
		Mockito.when(offerRepository.findOffer(offerRequest)).thenReturn(offerList);
		
		List<Offer> initializeOffer = offerService.getInitializeOffer(offerRequest);
		assertEquals(localDateTimeNow.minusDays(2), initializeOffer.get(1).getSaveDateTime());
	}

	@Test
	void testGetOffer() {
		Long id = 33L;
		Offer offer = new Offer();
		Mockito.when(offerRepository.findOffer(id)).thenReturn(offer);
		
		assertNotNull(offerService.getOffer(id));
	}

	@Test
	void testAddOrEditOffer() throws Exception {
		Employee loggedEmployee = new Employee();
		loggedEmployee.setId(66L);
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		
		Offer offer1 = new Offer();
		offer1.setId(22L);
		Offer offer2 = new Offer();
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer1);
		offerList.add(offer2);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setOffer(offerList);
		
		ArgumentCaptor<OfferRequest> offerRequestToEdit = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerRepository).editOfferRequest(offerRequestToEdit.capture());
		offerService.addOrEditOffer(offerRequest);
		assertEquals(OfferStage.PREPARE, offerRequestToEdit.getValue().getOffer().get(1).getOfferStage());
		
		offer2.setId(77L);
		offerService.addOrEditOffer(offerRequest);
		assertEquals(22, offerRequestToEdit.getValue().getOffer().get(0).getId());
	}

	@Test
	void testCreateModelGettingOffer() {
		Customer customer = new Customer();
		Mockito.when(userService.getLoggedCustomer()).thenReturn(customer);
		Employee employee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(employee);
		
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(33L);
		offerRequest.setRegisterDateTime(LocalDateTime.now().minusDays(2));
		offerRequest.setEmployee(employee);
		offerRequest.setCustomer(customer);
		offerRequest.setExecutionDate(LocalDate.now());
		offerRequest.setRegisterDateTime(LocalDateTime.now());
		List<OfferRequest> offerRequestList = new ArrayList<>();
		offerRequestList.add(offerRequest);
		Mockito.when(offerRepository.findAllOfferRequest(customer)).thenReturn(offerRequestList);
		
		OfferAttachment offerAttachment1 = new OfferAttachment();
		offerAttachment1.setId(99L);
		offerAttachment1.setOfferStage(OfferStage.SENT);
		offerAttachment1.setFile("attachment".getBytes());
		OfferAttachment offerAttachment2 = new OfferAttachment();
		offerAttachment2.setId(2L);
		offerAttachment2.setOfferStage(OfferStage.PREPARE);
		List<OfferAttachment> offerAttachmentList1 = new ArrayList<>();
		offerAttachmentList1.add(offerAttachment1);
		offerAttachmentList1.add(offerAttachment2);
		
		Offer offer1 = new Offer();
		offer1.setId(22L);
		offer1.setOfferStage(OfferStage.SENT);
		offer1.setContent("contentOffer1<br/>");
		offer1.setOfferAttachment(offerAttachmentList1);
		Offer offer2 = new Offer();
		offer2.setOfferStage(OfferStage.PREPARE);
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer1);
		offerList.add(offer2);
		offerRequest.setOffer(offerList);
		
		String content = "content";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		List<OfferData> modelGettingOffer = offerService.createModelGettingOffer();
		assertEquals(1, modelGettingOffer.size());
		assertEquals("contentOffer1", modelGettingOffer.get(0).getOfferContent());
		assertEquals(1, modelGettingOffer.get(0).getOfferAttachmentDataList().size());
		assertEquals(99, modelGettingOffer.get(0).getOfferAttachmentDataList().get(0).getAttachmentId());
	}

	@Test
	void testSendOffer() throws Exception {
		Customer customer = new Customer();
		customer.setFirstName("firstName");
		customer.setLastName("lastName");
		customer.setLanguage("EN");
		Mockito.when(userService.getLoggedCustomer()).thenReturn(customer);
		Employee employee = new Employee();
		employee.setFirstName("firstName");
		employee.setLastName("lastName");
		employee.setLanguage("PL");
		Mockito.when(userService.getLoggedEmployee()).thenReturn(employee);
		
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(33L);
		offerRequest.setRegisterDateTime(LocalDateTime.now().minusDays(2));
		offerRequest.setEmployee(employee);
		offerRequest.setCustomer(customer);
		offerRequest.setExecutionDate(LocalDate.now());
		offerRequest.setRegisterDateTime(LocalDateTime.now());
		List<OfferRequest> offerRequestList = new ArrayList<>();
		offerRequestList.add(offerRequest);
		Mockito.when(offerRepository.findAllOfferRequest(customer)).thenReturn(offerRequestList);
		
		OfferAttachment offerAttachment1 = new OfferAttachment();
		offerAttachment1.setId(99L);
		offerAttachment1.setOfferStage(OfferStage.PREPARE);
		offerAttachment1.setFile("attachment".getBytes());
		OfferAttachment offerAttachment2 = new OfferAttachment();
		offerAttachment2.setId(2L);
		offerAttachment2.setOfferStage(OfferStage.PREPARE);
		List<OfferAttachment> offerAttachmentList1 = new ArrayList<>();
		offerAttachmentList1.add(offerAttachment1);
		offerAttachmentList1.add(offerAttachment2);
		
		Offer offer = new Offer();
		offer.setId(22L);
		offer.setOfferStage(OfferStage.PREPARE);
		offer.setContent("contentOffer1<br/>");
		offer.setSaveDateTime(LocalDateTime.now().minusDays(1));
		offer.setOfferAttachment(offerAttachmentList1);
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer);
		offerRequest.setOffer(offerList);
		
		String content = "offerRequestContent";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		ArgumentCaptor<OfferRequest> offerRequestToEdit = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerRepository).editOfferRequest(offerRequestToEdit.capture());
		offerService.sendOffer(offerRequest);
		assertEquals(OfferStage.SENT, offerRequestToEdit.getValue().getOffer().get(0).getOfferStage());
		assertEquals(OfferStage.SENT, offerRequestToEdit.getValue().getOffer().get(0).getOfferAttachment().get(1).getOfferStage());
		assertEquals("contentOffer1<br/>", offerRequestToEdit.getValue().getOffer().get(0).getContent());
		assertEquals("firstName", offerRequestToEdit.getValue().getEmployee().getFirstName());
		assertEquals("lastName", offerRequestToEdit.getValue().getCustomer().getLastName());
	}

	@Test
	void testSendOfferAgain() throws Exception {
		Integer sendOfferAgainId = 0;
		
		Customer customer = new Customer();
		customer.setFirstName("firstName");
		customer.setLastName("lastName");
		customer.setLanguage("EN");
		customer.setEmail("customerEmail");
		Mockito.when(userService.getLoggedCustomer()).thenReturn(customer);
		Employee employee = new Employee();
		employee.setFirstName("firstName");
		employee.setLastName("lastName");
		employee.setLanguage("PL");
		Mockito.when(userService.getLoggedEmployee()).thenReturn(employee);
		
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(33L);
		offerRequest.setRegisterDateTime(LocalDateTime.now().minusDays(2));
		offerRequest.setEmployee(employee);
		offerRequest.setCustomer(customer);
		offerRequest.setExecutionDate(LocalDate.now());
		offerRequest.setRegisterDateTime(LocalDateTime.now());
		List<OfferRequest> offerRequestList = new ArrayList<>();
		offerRequestList.add(offerRequest);
		Mockito.when(offerRepository.findAllOfferRequest(customer)).thenReturn(offerRequestList);
		
		OfferAttachment offerAttachment1 = new OfferAttachment();
		offerAttachment1.setId(99L);
		offerAttachment1.setOfferStage(OfferStage.PREPARE);
		offerAttachment1.setFile("attachment".getBytes());
		OfferAttachment offerAttachment2 = new OfferAttachment();
		offerAttachment2.setId(2L);
		offerAttachment2.setOfferStage(OfferStage.PREPARE);
		List<OfferAttachment> offerAttachmentList1 = new ArrayList<>();
		offerAttachmentList1.add(offerAttachment1);
		offerAttachmentList1.add(offerAttachment2);
		
		Offer offer = new Offer();
		offer.setId(22L);
		offer.setOfferStage(OfferStage.PREPARE);
		offer.setContent("contentOffer1<br/>");
		offer.setSaveDateTime(LocalDateTime.now().minusDays(1));
		offer.setOfferAttachment(offerAttachmentList1);
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer);
		offerRequest.setOffer(offerList);
		
		String content = "offerRequestContent";
		OfferRequestContent offerRequestContent = new OfferRequestContent();
		offerRequestContent.setContent(content);
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequestContentList.add(offerRequestContent);
		offerRequest.setOfferRequestContent(offerRequestContentList);
		Mockito.when(offerRepository.findOfferRequestContentInitialize(offerRequest)).thenReturn(offerRequestContentList);
		
		ArgumentCaptor<String> mailTo = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> emailSubject = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> emailContent = ArgumentCaptor.forClass(String.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<OfferAttachment>> attachment = ArgumentCaptor.forClass(List.class);
		doNothing().when(sendEmail).sendEmailOfferAttachment(mailTo.capture(), emailSubject.capture(), emailContent.capture(), attachment.capture());
		offerService.sendOfferAgain(offerRequest, sendOfferAgainId);
		assertEquals("customerEmail", mailTo.getValue());
		assertEquals(99, attachment.getValue().get(0).getId());
	}

}
