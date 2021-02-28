package pl.aticode.civilworkoffers.unit.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.itextpdf.text.DocumentException;

import pl.aticode.civilworkoffers.controller.OfferController;
import pl.aticode.civilworkoffers.entity.offer.Offer;
import pl.aticode.civilworkoffers.entity.offer.OfferAttachment;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.offer.OfferStage;
import pl.aticode.civilworkoffers.service.OfferService;

class OfferControllerTest {
	
	private MockMvc mockMvc;
	
	@InjectMocks
	private OfferController offerController;
    @Mock
    private OfferService offerService;
    @Mock
    private MessageSource messageSource;

	@BeforeEach
	void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(offerController).build();
	}

	@Test
	void testShowOfferOfferRequestModel() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		List<Offer> offerList = new ArrayList<>(); 
		Mockito.when(offerService.getInitializeOffer(offerRequest)).thenReturn(offerList);
        mockMvc.perform(MockMvcRequestBuilders.get("/offer/employee/showoffer")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/showOffer"));
	}

	@Test
	void testShowOfferOfferRequestIntegerRedirectAttributes() throws Exception {
		Integer sendOfferAgainId = 0;
		OfferRequest offerRequest = new OfferRequest();
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/showoffer")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/showOffer"));
        
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/showoffer")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("sendOfferAgainId", sendOfferAgainId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.send.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).sendOfferAgain(offerRequest, sendOfferAgainId);
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/showoffer")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("sendOfferAgainId", sendOfferAgainId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.send.defeat"));
	}

	@Test
	void testOfferDetails() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		Offer offer = new Offer();
		offer.setContent("offerContent");
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer);
		Mockito.when(offerService.getInitializeOffer(offerRequest)).thenReturn(offerList);
		
        mockMvc.perform(MockMvcRequestBuilders.get("/offer/employee/offerdetails/0")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/offerDetails"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerContent", offer.getContent()));
	}

	@Test
	void testAddNewOfferOfferRequestModel() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		Offer offer1 = new Offer();
		List<Offer> offerList = new ArrayList<>();
		Mockito.when(offerService.getInitializeOffer(offerRequest)).thenReturn(offerList);
		
        mockMvc.perform(MockMvcRequestBuilders.get("/offer/employee/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/newOffer"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerIdx", 0));

        offer1.setOfferStage(OfferStage.PREPARE);
        offerList.add(offer1);
        mockMvc.perform(MockMvcRequestBuilders.get("/offer/employee/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/newOffer"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerIdx", 0));

        offer1.setOfferStage(OfferStage.SENT);
        Offer offer2 = new Offer();
        offer2.setOfferStage(OfferStage.SENT);
        offerList.add(offer2);
        mockMvc.perform(MockMvcRequestBuilders.get("/offer/employee/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/newOffer"))
		        .andExpect(MockMvcResultMatchers.model().attribute("offerIdx", offerList.size()));
	}

	@Test
	void testAddNewOfferOfferRequestBindingResultStringModelRedirectAttributes() throws Exception {
		String sendOffer = "sendOffer";
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setExecutionDate(LocalDate.now());
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/new")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/newOffer"))
		        .andExpect(MockMvcResultMatchers.model().attribute("message", "offer.save.success"));
        
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/new")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("sendOffer", sendOffer))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.send.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).sendOffer(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/new")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("sendOffer", sendOffer))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.send.defeat"));
	}

	@Test
	void testOfferFileOfferRequestModel() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
        mockMvc.perform(MockMvcRequestBuilders.get("/offer/employee/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/offerFile"));
	}

	@Test
	void testOfferFileOfferRequestLongLongModelRedirectAttributes() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/file")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/offerFile"));
        
		Long estimateId = 33L;
		Long attachmentId = 44L;
		Offer offer = new Offer();
        offer.setOfferStage(OfferStage.SENT);
		List<Offer> offerList = new ArrayList<>();
		offerList.add(offer);
		Mockito.when(offerService.getInitializeOffer(offerRequest)).thenReturn(offerList);
		Mockito.when(messageSource.getMessage("alert.offer.sent", null, Locale.ENGLISH)).thenReturn("alert.offer.sent");
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/file")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("estimateId", estimateId.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/offerFile"))
		        .andExpect(MockMvcResultMatchers.model().attribute("alert", "alert.offer.sent"));
        
        Mockito.when(messageSource.getMessage("alert.thesame.file", new String[]{"estimate" + estimateId}, Locale.ENGLISH)).thenReturn("alert.thesame.file");
        offer.setOfferStage(OfferStage.PREPARE);
        OfferAttachment offerAttachment = new OfferAttachment();
        offerAttachment.setFileName("estimate" + estimateId);
        List<OfferAttachment> offerAttachmentList = new ArrayList<>();
        offerAttachmentList.add(offerAttachment);
        offer.setOfferAttachment(offerAttachmentList);
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/file")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("estimateId", estimateId.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/offerFile"))
		        .andExpect(MockMvcResultMatchers.model().attribute("alert", "alert.thesame.file"));
		
        offerAttachment.setFileName("estimateERROR" + estimateId);
        Mockito.doThrow(new DocumentException("TEST")).when(offerService).generateAndAddPdfEstimateAttachmentToOffer(offerRequest, estimateId);
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/file")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("estimateId", estimateId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.attachments.generate_pdf.defeat"));
        
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/file")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("attachmentId", attachmentId.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("offer/employee/offerFile"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).deleteOfferAttachment(offerRequest, attachmentId);
        mockMvc.perform(MockMvcRequestBuilders.post("/offer/employee/file")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("attachmentId", attachmentId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/employee/main"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "offer.attachments.delete.defeat"));
	}

	@Test
	void testShowEstimatePdf() throws Exception {
		Long estimateId = 77L;
		OfferAttachment offerAttachment = new OfferAttachment(); 
		offerAttachment.setFileType("application/pdf");
		offerAttachment.setFile("file".getBytes());
		Mockito.when(offerService.getOfferAttachment(estimateId)).thenReturn(offerAttachment);
        mockMvc.perform(MockMvcRequestBuilders.get("/offer/employee/pdf/77"))
		        .andExpect(MockMvcResultMatchers.status().isOk());
	}

}
