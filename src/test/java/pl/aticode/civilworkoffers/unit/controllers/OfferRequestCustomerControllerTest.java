package pl.aticode.civilworkoffers.unit.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import pl.aticode.civilworkoffers.controller.OfferRequestCustomerController;
import pl.aticode.civilworkoffers.entity.offer.Offer;
import pl.aticode.civilworkoffers.entity.offer.OfferAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestComment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequestContent;
import pl.aticode.civilworkoffers.model.OfferData;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.OfferService;

class OfferRequestCustomerControllerTest {
	
	private MockMvc mockMvc;
	
	@InjectMocks
	private OfferRequestCustomerController offerRequestCustomerController;
    @Mock
    private CharFilterService charFilterService;
    @Mock
    private OfferService offerService;

	@BeforeEach
	void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestCustomerController).build();
	}

	@Test
	void testNewOfferRequestModel() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/customer/new"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/new"))
		        .andExpect(MockMvcResultMatchers.model().attributeHasNoErrors("offerRequest"));
	}

	@Test
	void testNewOfferRequestOfferRequestBindingResultModelRedirectAttributes() throws Exception {
        offerRequestCustomerController = new OfferRequestCustomerController(10, 512, charFilterService, offerService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestCustomerController).build();
        
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		
		String content = "Offer request content size > 10 characters";
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
		
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.add.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).addOfferRequestByCustomer(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.add.defeat"));
        
        offerRequestCustomerController = new OfferRequestCustomerController(1, 512, charFilterService, offerService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestCustomerController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/new"));        
        
        offerRequestCustomerController = new OfferRequestCustomerController(10, 5, charFilterService, offerService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestCustomerController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/new"));
        
        offerRequest.setExecutionDate(LocalDate.now().minusDays(2));
        offerRequestCustomerController = new OfferRequestCustomerController(10, 512, charFilterService, offerService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestCustomerController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/new"));
	}

	@Test
	void testMyRequest() throws Exception {
		List<OfferRequest> allCustomerOfferRequests = new ArrayList<>(); 
		Mockito.when(offerService.getAllCustomerOfferRequests()).thenReturn(allCustomerOfferRequests);
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/customer/myrequests"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/myrequests"))
		        .andExpect(MockMvcResultMatchers.model().attribute("allCustomerOfferRequests", allCustomerOfferRequests));
	}

	@Test
	void testMyRequestDetailsOfferRequestModel() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(77L);
		OfferRequest offerRequestAllInitialized = new OfferRequest(); 
		Mockito.when(offerService.getOfferRequest(offerRequest.getId())).thenReturn(offerRequestAllInitialized);
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/customer/details")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/details"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerRequest", offerRequestAllInitialized));
	}

	@Test
	void testMyRequestDetailsLongModel() throws Exception {
		Long offerRequestId = 88L;
		OfferRequest offerRequest = new OfferRequest();
		Mockito.when(offerService.getOfferRequest(offerRequestId)).thenReturn(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/details")
        		.param("offerRequestId", offerRequestId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/offerrequest/customer/details"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerRequest", offerRequest));
	}

	@Test
	void testEditOfferRequestContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/customer/edit/content"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/content"));
	}

	@Test
	void testEditOfferRequestContentOfferRequestStringModelRedirectAttributes() throws Exception {
		String requestContent = "requestContent";
		OfferRequest offerRequest = new OfferRequest();
		List<OfferRequestContent> offerRequestContentList = new ArrayList<>();
		offerRequest.setOfferRequestContent(offerRequestContentList);
		
		ArgumentCaptor<OfferRequest> offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerService).editOfferRequestContent(offerRequestCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/content")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestContent", requestContent))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.content.success"));
        assertEquals("requestContent", offerRequestCaptor.getValue().getOfferRequestContent().get(0).getContent());
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).editOfferRequestContent(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/content")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestContent", requestContent))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.content.defeat"));
        
        requestContent = "content"; //less than 10 char.
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/content")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestContent", requestContent))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/content"))
		        .andExpect(MockMvcResultMatchers.model().attribute("requestContentError", "offer.request.edit.content.size"));
	}

	@Test
	void testEditOfferRequestFile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/customer/edit/file"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/file"));
	}

	@Test
	void testEditOfferRequestFileOfferRequestBindingResultModelRedirectAttributes() throws IOException, Exception {
        offerRequestCustomerController = new OfferRequestCustomerController(10, 512, charFilterService, offerService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestCustomerController).build();
        
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		
		OfferRequestAttachment offerRequestAttachment1 = new OfferRequestAttachment();
		offerRequestAttachment1.setFileSize(10);
		OfferRequestAttachment offerRequestAttachment2 = new OfferRequestAttachment();
		offerRequestAttachment2.setFileSize(10);
		OfferRequestAttachment offerRequestAttachment3 = new OfferRequestAttachment();
		offerRequestAttachment3.setFileSize(10);
		List<OfferRequestAttachment> offerRequestAttachmentList = new ArrayList<>();
		offerRequestAttachmentList.add(offerRequestAttachment1);
		offerRequestAttachmentList.add(offerRequestAttachment2);
		offerRequestAttachmentList.add(offerRequestAttachment3);
		offerRequest.setOfferRequestAttachment(offerRequestAttachmentList);
		
		String content = "Offer request content size > 10 characters";
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
		
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.file.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).editOfferRequestFile(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.file.defeat"));
        
        offerRequestCustomerController = new OfferRequestCustomerController(1, 512, charFilterService, offerService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestCustomerController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/file"));        
        
        offerRequestCustomerController = new OfferRequestCustomerController(10, 5, charFilterService, offerService);
        mockMvc = MockMvcBuilders.standaloneSetup(offerRequestCustomerController).build();
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/file"));
	}

	@Test
	void testEditOfferRequestDate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/customer/edit/date"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/date"));
	}

	@Test
	void testEditOfferRequestDateOfferRequestBindingResultModelRedirectAttributes() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setExecutionDate(LocalDate.now().plusDays(2));
		
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/date")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.date.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).editOfferRequestDate(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/date")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.date.defeat"));
        
        offerRequest.setExecutionDate(LocalDate.now().minusDays(2));
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/date")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/date"));
	}

	@Test
	void testCommentOfferRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/offerrequest/customer/edit/comment"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/comment"));
	}

	@Test
	void testCommentOfferRequestOfferRequestStringModelRedirectAttributes() throws Exception {
		String requestComment = "requestComment";
		OfferRequest offerRequest = new OfferRequest();
		List<OfferRequestComment> offerRequestCommentList = new ArrayList<>();
		offerRequest.setOfferRequestComment(offerRequestCommentList);
		
		ArgumentCaptor<OfferRequest> offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		doNothing().when(offerService).editOfferRequestComment(offerRequestCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/comment")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestComment", requestComment))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.comment.success"));
        assertEquals("requestComment", offerRequestCaptor.getValue().getOfferRequestComment().get(0).getComment());
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).editOfferRequestComment(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/comment")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestComment", requestComment))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/customer/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.request.edit.comment.defeat"));
        
        requestComment = "comment"; //less than 10 char.
        mockMvc.perform(MockMvcRequestBuilders.post("/offerrequest/customer/edit/comment")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("requestComment", requestComment))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offerrequest/customer/edit/comment"))
		        .andExpect(MockMvcResultMatchers.model().attribute("requestCommentError", "offer.request.edit.comment.size"));
	}

	@Test
	void testAnswers() throws Exception {
		List<OfferData> gettingOfferList = new ArrayList<>(); 
		Mockito.when(offerService.createModelGettingOffer()).thenReturn(gettingOfferList);
        mockMvc.perform(MockMvcRequestBuilders.get("/offer/customer/answers"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/customer/answers"))
		        .andExpect(MockMvcResultMatchers.model().attribute("gettingOfferList", gettingOfferList));
	}

	@Test
	void testAnswersDetails() throws Exception {
		Long offerId = 33L;
		Offer offer = new Offer(); 
		offer.setContent("offerContent");
		Mockito.when(offerService.getOffer(offerId)).thenReturn(offer);
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/customer/details")
        		.param("offerId", offerId.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/customer/offerDetails"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerContent", offer.getContent()));
	}

	@Test
	void testAnswersFile() throws Exception {
		Long attachmentId = 44L;
		OfferAttachment offerAttachment = new OfferAttachment();
		offerAttachment.setFileType("application/pdf");
		offerAttachment.setFile("file".getBytes());
		Mockito.when(offerService.getOfferAttachment(attachmentId)).thenReturn(offerAttachment);
		mockMvc.perform(MockMvcRequestBuilders.post("/offer/customer/file")
				.param("attachmentId", attachmentId.toString()))
        		.andExpect(MockMvcResultMatchers.status().isOk());
	}

}
